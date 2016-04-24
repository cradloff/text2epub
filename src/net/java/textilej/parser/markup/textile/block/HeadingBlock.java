package net.java.textilej.parser.markup.textile.block;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.java.textilej.parser.Attributes;
import net.java.textilej.parser.markup.Block;
import net.java.textilej.parser.markup.textile.Textile;

/**
 * Matches any textile text, including lines starting with <code>p. </code>.
 * 
 * @author dgreen
 */
public class HeadingBlock extends Block {

	private static final int LINE_REMAINDER_GROUP_OFFSET = Textile.ATTRIBUTES_BLOCK_GROUP_COUNT+2;

	static final Pattern startPattern = Pattern.compile("h([1-6])"+Textile.REGEX_BLOCK_ATTRIBUTES+"\\.\\s+(.*)");
	
	private int blockLineCount = 0;
	private int level = -1;
	private Matcher matcher;
	
	public HeadingBlock() {
	}

	@Override
	public int processLineContent(String line,int offset) {
		if (blockLineCount == 0) {
			Attributes attributes = new Attributes();
			if (offset == 0) {
				// 0-offset matches may start with the "hn. " prefix.
				level = Integer.parseInt(matcher.group(1));
				Textile.configureAttributes(attributes,matcher, 2,true);
				offset = matcher.start(LINE_REMAINDER_GROUP_OFFSET);
			}
			if (attributes.getId() == null) {
				attributes.setId(state.getIdGenerator().newId("h"+level,line.substring(offset)));
			}
			builder.beginHeading(level, attributes);
		}
		if (dialect.isEmptyLine(line)) {
			setClosed(true);
			return 0;
		}
		if (blockLineCount != 0) {
			getDialect().emitMarkupText(getParser(), state, "\n");
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
			builder.endHeading();
		}
		super.setClosed(closed);
	}

	
}
