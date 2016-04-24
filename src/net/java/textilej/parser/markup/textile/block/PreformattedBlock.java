package net.java.textilej.parser.markup.textile.block;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.java.textilej.parser.Attributes;
import net.java.textilej.parser.DocumentBuilder.BlockType;
import net.java.textilej.parser.markup.Block;
import net.java.textilej.parser.markup.textile.Textile;

/**
 * Preformatted text block, matches blocks that start with <code>pre. </code>
 * 
 * @author dgreen
 */
public class PreformattedBlock extends Block {

	private static final int LINE_REMAINDER_GROUP_OFFSET = Textile.ATTRIBUTES_BLOCK_GROUP_COUNT+2;
	private static final int EXTENDED_GROUP = Textile.ATTRIBUTES_BLOCK_GROUP_COUNT+1;

	static final Pattern startPattern = Pattern.compile("pre"+Textile.REGEX_BLOCK_ATTRIBUTES+"\\.(\\.)?\\s+(.*)");

	
	private boolean extended;
	private int blockLineCount = 0;
	private Matcher matcher;
	
	public PreformattedBlock() {
	}

	@Override
	public int processLineContent(String line,int offset) {
		if (blockLineCount == 0) {
			Attributes attributes = new Attributes();
	
			Textile.configureAttributes(attributes,matcher, 1,true);
			offset = matcher.start(LINE_REMAINDER_GROUP_OFFSET);
			extended = matcher.group(EXTENDED_GROUP) != null;
			
			builder.beginBlock(BlockType.PREFORMATTED, attributes);
		}
		if (dialect.isEmptyLine(line) && !extended) {
			setClosed(true);
			return 0;
		} else if (extended && Textile.explicitBlockBegins(line,offset)) {
			setClosed(true);
			return offset;
		}
		++blockLineCount;
		
		
		builder.characters(offset>0?line.substring(offset):line);
		builder.characters("\n");
		
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
