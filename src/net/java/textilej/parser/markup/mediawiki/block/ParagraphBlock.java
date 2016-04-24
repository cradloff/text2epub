package net.java.textilej.parser.markup.mediawiki.block;

import net.java.textilej.parser.Attributes;
import net.java.textilej.parser.DocumentBuilder.BlockType;
import net.java.textilej.parser.markup.Block;
import net.java.textilej.parser.markup.mediawiki.MediaWikiDialect;

/**
 * Matches any markup text.
 * 
 * @author dgreen
 */
public class ParagraphBlock extends Block {
	
	private int blockLineCount = 0;
	private Block nestedBlock = null; 
	
	public ParagraphBlock() {
	}

	@Override
	public int processLineContent(String line,int offset) {
		if (blockLineCount == 0) {
			Attributes attributes = new Attributes();
			
			builder.beginBlock(BlockType.PARAGRAPH, attributes);
		} else if (nestedBlock != null) { 
			int returnOffset = nestedBlock.processLine(line, offset);
			if (nestedBlock.isClosed()) {
				nestedBlock = null;
			}
			if (returnOffset >= 0) {
				offset = returnOffset;
				if (nestedBlock != null) {
					throw new IllegalStateException();
				}
			} else {
				if (dialect.isEmptyLine(line)) {
					setClosed(true);
					return 0;
				}
				return returnOffset;
			}
		}
		
		if (dialect.isEmptyLine(line)) {
			setClosed(true);
			return 0;
		}
		

		MediaWikiDialect dialect = (MediaWikiDialect) getDialect();
		
		// paragraphs can have nested lists and other things
		for (Block block: dialect.getParagraphBreakingBlocks()) {
			if (block.canStart(line, offset)) {
				setClosed(true);
				return 0;
			}
		}
		
		++blockLineCount;
		
		
		if (nestedBlock != null) {
			if (blockLineCount > 1) {
				builder.lineBreak();
			}
			nestedBlock.processLine(line, offset);
		} else {
			if (offset == 0 && line.length() > 0 && line.charAt(0) == ' ') {
				// a preformatted block.
				setClosed(true);
				return 0;
			}
			if (blockLineCount != 1) {
				// note: newlines don't automatically convert to line breaks
				builder.characters("\n");
			} 
			dialect.emitMarkupLine(getParser(),state,line, offset);
		}
		
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
			if (nestedBlock != null) { 
				nestedBlock.setClosed(closed);
				nestedBlock = null;
			}
			builder.endBlock();
		}
		super.setClosed(closed);
	}

	
}
