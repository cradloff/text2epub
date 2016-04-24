package net.java.textilej.parser.markup.confluence.block;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.java.textilej.parser.Attributes;
import net.java.textilej.parser.DocumentBuilder.BlockType;
import net.java.textilej.parser.markup.Block;
import net.java.textilej.parser.markup.confluence.ConfluenceDialect;

/**
 * Matches any textile text, including lines starting with <code>p. </code>.
 * 
 * @author dgreen
 */
public class ParagraphBlock extends Block {
	
	private static final Pattern confluenceBlockStart = Pattern.compile("\\{(code|info|tip|warning|panel|note|toc)(?:(:[^\\}]*))?\\}");
	
	private int blockLineCount = 0;
	
	public ParagraphBlock() {
	}

	@Override
	public int processLineContent(String line,int offset) {
		if (blockLineCount == 0) {
			Attributes attributes = new Attributes();
			
			builder.beginBlock(BlockType.PARAGRAPH, attributes);
		}
		
		if (dialect.isEmptyLine(line)) {
			setClosed(true);
			return 0;
		}
		
		++blockLineCount;
		
		ConfluenceDialect dialect = (ConfluenceDialect) getDialect();
		
		// NOTE: in Textile paragraphs can have nested lists and other things, however
		//       the resulting XHTML is invalid -- so here we allow for similar constructs
		//       however we cause them to end the paragraph rather than being nested.
		for (Block block: dialect.getParagraphBreakingBlocks()) {
			if (block.canStart(line, offset)) {
				setClosed(true);
				return 0;
			}
		}
		
		Matcher blockStartMatcher = confluenceBlockStart.matcher(line);
		if (offset > 0) {
			blockStartMatcher.region(offset, line.length());
		}
		if (blockStartMatcher.find()) {
			int end = blockStartMatcher.start();
			if (end > offset) {
				dialect.emitMarkupLine(getParser(),state,offset,line.substring(offset,end), 0);		
			}
			setClosed(true);
			return end;
		}
		if (blockLineCount > 1) {
			builder.lineBreak();
		}
		dialect.emitMarkupLine(getParser(),state,line, offset);
		
		return -1;
	}

	@Override
	public boolean canStart(String line, int lineOffset) {
		blockLineCount = 0;
		return true;
	}

	@Override
	public void setClosed(boolean closed) {
		if (closed && !isClosed()) {
			builder.endBlock();
		}
		super.setClosed(closed);
	}

	
}
