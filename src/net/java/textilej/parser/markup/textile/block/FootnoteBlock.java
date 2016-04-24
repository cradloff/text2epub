package net.java.textilej.parser.markup.textile.block;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.java.textilej.parser.Attributes;
import net.java.textilej.parser.DocumentBuilder.BlockType;
import net.java.textilej.parser.DocumentBuilder.SpanType;
import net.java.textilej.parser.markup.Block;
import net.java.textilej.parser.markup.textile.Textile;

/**
 * Footnote block, matching lines starting with <code>fn\d\d?. </code>.
 * 
 * @author dgreen
 */
public class FootnoteBlock extends Block {

	private static final int LINE_REMAINDER_GROUP_OFFSET = Textile.ATTRIBUTES_BLOCK_GROUP_COUNT+2;

	static final Pattern startPattern = Pattern.compile("fn([0-9]{1,2})"+Textile.REGEX_BLOCK_ATTRIBUTES+"\\.\\s+(.*)");
	
	private int blockLineCount = 0;

	private Matcher matcher;

	private String footnote;
	
	public FootnoteBlock() {
	}

	@Override
	public int processLineContent(String line,int offset) {
		if (blockLineCount == 0) {
			Attributes attributes = new Attributes();
			attributes.setCssClass("footnote");

			// 0-offset matches may start with the "fnnn. " prefix.
			footnote = matcher.group(1);
			attributes.setId(state.getFootnoteId(footnote));

			Textile.configureAttributes(attributes,matcher, 2,true);
			offset = matcher.start(LINE_REMAINDER_GROUP_OFFSET);

			builder.beginBlock(BlockType.PARAGRAPH, attributes);
			builder.beginSpan(SpanType.SUPERSCRIPT, new Attributes());
			builder.characters(footnote);
			builder.endSpan();
			builder.characters(" ");
		}
		if (dialect.isEmptyLine(line)) {
			setClosed(true);
			return 0;
		}
		if (blockLineCount != 0) {
			builder.lineBreak();
		}
		++blockLineCount;
		
		getDialect().emitMarkupLine(getParser(),state,line, offset);
		
		return -1;
	}

	@Override
	public boolean canStart(String line, int lineOffset) {
		blockLineCount = 0;
		if (lineOffset == 0) {
			matcher = startPattern.matcher(line);
			return matcher.matches();
		} else {
			matcher = null;
			return false;
		}
	}


	@Override
	public void setClosed(boolean closed) {
		if (closed && !isClosed()) {
			builder.endBlock();
		}
		super.setClosed(closed);
	}

	
}
