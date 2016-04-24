package net.java.textilej.parser.markup.confluence.block;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.java.textilej.parser.Attributes;
import net.java.textilej.parser.markup.Block;

/**
 * Matches any textile text, including lines starting with <code>p. </code>.
 * 
 * @author dgreen
 */
public class HeadingBlock extends Block {

	static final Pattern startPattern = Pattern.compile("\\s*h([1-6])\\.\\s+(.*)");
	
	private int blockLineCount = 0;
	private int level = -1;
	private Matcher matcher;
	
	public HeadingBlock() {
	}

	@Override
	public int processLineContent(String line,int offset) {
		if (blockLineCount == 0) {
			Attributes attributes = new Attributes();
			// 0-offset matches may start with the "hn. " prefix.
			level = Integer.parseInt(matcher.group(1));
			offset = matcher.start(2);

			if (attributes.getId() == null) {
				attributes.setId(state.getIdGenerator().newId("h"+level,line.substring(offset)));
			}
			builder.beginHeading(level, attributes);
		}
		if (blockLineCount != 0 || dialect.isEmptyLine(line)) {
			setClosed(true);
			return 0;
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
