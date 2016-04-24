package net.java.textilej.parser.markup.textile.block;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.java.textilej.parser.Attributes;
import net.java.textilej.parser.DocumentBuilder.BlockType;
import net.java.textilej.parser.markup.Block;
import net.java.textilej.parser.markup.textile.Textile;
import net.java.textilej.parser.markup.textile.TextileDialect;

/**
 * Matches any textile text, including lines starting with <code>p. </code>.
 * 
 * @author dgreen
 */
public class ParagraphBlock extends Block {

	private static final int LINE_REMAINDER_GROUP_OFFSET = Textile.ATTRIBUTES_BLOCK_GROUP_COUNT+1;

	static final Pattern startPattern = Pattern.compile("p"+Textile.REGEX_BLOCK_ATTRIBUTES+"\\.\\s+(.*)");
	
	private int blockLineCount = 0;
	private boolean unwrapped = false;
	
	public ParagraphBlock() {
	}

	@Override
	public int processLineContent(String line,int offset) {
		if (blockLineCount == 0) {
			Attributes attributes = new Attributes();
			if (offset == 0) {
				// 0-offset matches may start with the "p. " prefix.
				Matcher matcher = startPattern.matcher(line);
				if (matcher.matches()) {
					Textile.configureAttributes(attributes,matcher, 1,true);
					offset = matcher.start(LINE_REMAINDER_GROUP_OFFSET);
				} else {
					if (line.charAt(0) == ' ') {
						offset = 1;
						unwrapped = true;
					}
				}
			}
			if (!unwrapped) {
				builder.beginBlock(BlockType.PARAGRAPH, attributes);
			}
		}
		
		if (dialect.isEmptyLine(line)) {
			setClosed(true);
			return 0;
		}
		
		TextileDialect dialect = (TextileDialect) getDialect();
		
		// NOTE: in Textile paragraphs can have nested lists and other things, however
		//       the resulting XHTML is invalid -- so here we allow for similar constructs
		//       however we cause them to end the paragraph rather than being nested.
		for (Block block: dialect.getParagraphBreakingBlocks()) {
			if (block.canStart(line, offset)) {
				setClosed(true);
				return 0;
			}
		}

		
		if (blockLineCount != 0) {
			if (unwrapped) {
				builder.characters("\n");
			} else {
				builder.lineBreak();
			}
		}
		++blockLineCount;
		
		
		dialect.emitMarkupLine(getParser(),state,line, offset);
		
		return -1;
	}

	@Override
	public boolean canStart(String line, int lineOffset) {
		unwrapped = false;
		blockLineCount = 0;
		return true;
	}

	@Override
	public void setClosed(boolean closed) {
		if (closed && !isClosed()) {
			if (!unwrapped) {
				builder.endBlock();
			}
		}
		super.setClosed(closed);
	}

	
}
