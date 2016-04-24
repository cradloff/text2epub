package net.java.textilej.parser.markup.confluence.block;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.java.textilej.parser.Attributes;
import net.java.textilej.parser.DocumentBuilder.BlockType;

public class TextBoxBlock extends ParameterizedBlock {


	private final Pattern startPattern;
	private final Pattern endPattern;

	private final BlockType blockType;
	
	private int blockLineCount = 0;
	private Matcher matcher;
	
	private String title;
	private StringBuilder markupContent;
	
	public TextBoxBlock(BlockType blockType,String name) {
		this.blockType = blockType;
		startPattern = Pattern.compile("\\{"+name+"(?::([^\\}]*))?\\}(.*)");
		endPattern = Pattern.compile("(\\{"+name+"\\})(.*)");
	}

	@Override
	public int processLineContent(String line,int offset) {
		if (blockLineCount == 0) {
			setOptions(matcher.group(1));
			
			Attributes attributes = new Attributes();
			attributes.setTitle(title);
			
			offset = matcher.start(2);

			builder.beginBlock(blockType, attributes);
			markupContent = new StringBuilder();
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
		++blockLineCount;
		 

		if (end < line.length()) {
			state.setLineSegmentEndOffset(end);
		}
		markupContent.append(line.substring(offset,segmentEnd));
		markupContent.append("\n");
		
		if (terminating) {
			setClosed(true);
		}
		return end==line.length()?-1:end;
	}

	@Override
	public boolean canStart(String line, int lineOffset) {
		blockLineCount = 0;
		title = null;
		markupContent = null;
		matcher = startPattern.matcher(line);
		if (lineOffset > 0) {
			matcher.region(lineOffset, line.length());
		} 
		return matcher.matches();
	}

	@Override
	public void setClosed(boolean closed) {
		if (closed && !isClosed()) {
			if (markupContent != null) {

				getParser().parse(markupContent.toString(),false);
				markupContent = null;
				builder.endBlock(); // the block	
			}
		}
		super.setClosed(closed);
	}

	@Override
	protected void setOption(String key, String value) {
		if (key.equals("title")) {
			title = value;
		}
	}	
}
