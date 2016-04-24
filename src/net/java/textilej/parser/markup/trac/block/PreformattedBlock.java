package net.java.textilej.parser.markup.trac.block;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.java.textilej.parser.Attributes;
import net.java.textilej.parser.DocumentBuilder.BlockType;
import net.java.textilej.parser.markup.Block;

public class PreformattedBlock extends Block {

	private static final Pattern startPattern = Pattern.compile("\\{\\{\\{(.*)");
	private static final Pattern endPattern = Pattern.compile("\\}\\}\\}(.*)");
	
	private int blockLineCount = 0;
	private Matcher matcher;
	

	@Override
	public int processLineContent(String line,int offset) {
		if (blockLineCount++ == 0) {
			offset = matcher.start(1);
			builder.beginBlock(BlockType.PREFORMATTED, new Attributes());
		} else {
			Matcher endMatcher = endPattern.matcher(line);
			if (endMatcher.matches()) {
				setClosed(true);
				return endMatcher.start(1);
			}
		}
		builder.characters(offset==0?line:line.substring(offset));
		builder.characters("\n");
		return -1;
	}

	@Override
	public void setClosed(boolean closed) {
		if (closed && !isClosed()) {
			builder.endBlock(); // pre
		}
		super.setClosed(closed);
	}

	@Override
	public boolean canStart(String line, int lineOffset) {
		if (lineOffset == 0 ) {
			matcher = startPattern.matcher(line);
			return matcher.matches();
		} else {
			matcher = null;
			return false;
		}
	}
	
}
