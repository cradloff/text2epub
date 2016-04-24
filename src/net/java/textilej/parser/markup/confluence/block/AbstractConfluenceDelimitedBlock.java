package net.java.textilej.parser.markup.confluence.block;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractConfluenceDelimitedBlock extends ParameterizedBlock {

	private final Pattern startPattern; 
	private final Pattern endPattern;

	
	protected int blockLineCount = 0;
	private Matcher matcher;
	
	public AbstractConfluenceDelimitedBlock(String blockName) {
		startPattern = Pattern.compile("\\s*\\{"+blockName+"(?::([^\\}]*))?\\}(.*)");
		endPattern = Pattern.compile("\\s*(\\{"+blockName+"\\})(.*)");
	}

	@Override
	public int processLineContent(String line,int offset) {
		if (blockLineCount == 0) {
			setOptions(matcher.group(1));
			
			offset = matcher.start(2);

			beginBlock();
		}

		int end = line.length();
		int segmentEnd = end;
		boolean terminating = false;
		
		Matcher endMatcher = endPattern.matcher(line);
		if (offset < end) {
			if (blockLineCount == 0) {
				endMatcher.region(offset, end);
			}
			if (endMatcher.find()) {
				terminating = true;
				end = endMatcher.start(2);
				segmentEnd = endMatcher.start(1);
			}
		}

		if (end < line.length()) {
			state.setLineSegmentEndOffset(end);
		}

		++blockLineCount;
		
		final String content = line.substring(offset,segmentEnd);
		handleBlockContent(content);

		if (terminating) {
			setClosed(true);
		}
		return end==line.length()?-1:end;
	}

	protected abstract void handleBlockContent(String content);
	protected abstract void beginBlock();
	protected abstract void endBlock();

	@Override
	public void setClosed(boolean closed) {
		if (closed && !isClosed()) {
			endBlock();
		}
		super.setClosed(closed);
	}
	
	@Override
	public boolean canStart(String line, int lineOffset) {
		resetState();
		matcher = startPattern.matcher(line);
		if (lineOffset > 0) {
			matcher.region(lineOffset, line.length());
		} 
		return matcher.matches();
	}

	protected void resetState() {
		blockLineCount = 0;
	}

}
