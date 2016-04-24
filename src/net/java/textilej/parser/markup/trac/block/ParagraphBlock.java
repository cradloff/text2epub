package net.java.textilej.parser.markup.trac.block;

import net.java.textilej.parser.Attributes;
import net.java.textilej.parser.DocumentBuilder.BlockType;
import net.java.textilej.parser.markup.Block;
import net.java.textilej.parser.markup.trac.TracWikiDialect;

/**
 * Matches any textile text, including lines starting with <code>p. </code>.
 * 
 * @author dgreen
 */
public class ParagraphBlock extends Block {
	
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
			return -1;
		}
		++blockLineCount;
		
		TracWikiDialect dialect = (TracWikiDialect) getDialect();
		
		// paragraphs can have nested lists and other things
		for (Block block: dialect.getParagraphNestableBlocks()) {
			if (block.canStart(line, offset)) {
				setClosed(true);
				return 0;
			}
		}

		if (blockLineCount != 1) {
			// note: newlines don't automatically convert to line breaks
			builder.characters("\n");
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
