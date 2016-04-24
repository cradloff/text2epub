package net.java.textilej.parser.markup.trac.block;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.java.textilej.parser.Attributes;
import net.java.textilej.parser.markup.Block;

public class HeadingBlock extends Block {

	private static final Pattern pattern = Pattern.compile("\\s*(\\={1,6})([^=]+)\\1(?:\\s+\\#(\\S+)?)?");
	
	private int blockLineCount = 0;
	private Matcher matcher; 
	
		
	@Override
	public boolean canStart(String line, int lineOffset) {
		blockLineCount = 0;
		if (lineOffset == 0) {
			matcher = pattern.matcher(line);
			return matcher.matches();
		} else {
			matcher = null;
			return false;
		}
	}

	@Override
	public int processLineContent(String line,int offset) {
		if (blockLineCount > 0) {
			throw new IllegalStateException();
		}
		++blockLineCount;
		
		int level = matcher.group(1).length();
		
		String text = matcher.group(2);
		
		String id = matcher.group(3);
		
		Attributes attributes = new Attributes();
		attributes.setId(id);
		
		builder.beginHeading(level,attributes);
		builder.characters(text);
		builder.endHeading();
		
		setClosed(true);
		return -1;
	}

}
