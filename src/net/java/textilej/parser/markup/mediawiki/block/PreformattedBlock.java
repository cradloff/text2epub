package net.java.textilej.parser.markup.mediawiki.block;

import net.java.textilej.parser.Attributes;
import net.java.textilej.parser.DocumentBuilder.BlockType;
import net.java.textilej.parser.markup.Block;

public class PreformattedBlock extends Block {

	private int blockLineCount = 0;
	
	@Override
	public boolean canStart(String line, int lineOffset) {
		if (lineOffset == 0 && line.length() > 0 && line.charAt(0) == ' ') {
			return true;
		}
		return false;
	}

	@Override
	public int processLineContent(String line,int offset) {
		if (dialect.isEmptyLine(line) || (offset == 0 && line.charAt(0) != ' ')) {
			setClosed(true);
			return 0;
		}
		if (blockLineCount++ == 0) {
			builder.beginBlock(BlockType.PREFORMATTED, new Attributes());
		}
		builder.characters(line.substring(1));
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

	
}
