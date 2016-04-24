package net.java.textilej.parser.markup.confluence.block;

import net.java.textilej.parser.Attributes;
import net.java.textilej.parser.DocumentBuilder.BlockType;

/**
 * quoted text block, matches blocks that start with <code>bc. </code>.  
 * Creates an extended block type of {@link ParagraphBlock paragraph}.
 * 
 * @author dgreen
 */
public class ExtendedQuoteBlock extends AbstractConfluenceDelimitedBlock {
	
	private int paraLine = 0;
	private boolean paraOpen = false;
	
	public ExtendedQuoteBlock() {
		super("quote");
	}

	@Override
	protected void resetState() {
		super.resetState();
		paraOpen = false;
		paraLine = 0;
	}
	
	@Override
	protected void beginBlock() {
		Attributes attributes = new Attributes();
		builder.beginBlock(BlockType.QUOTE, attributes);
	}
	
	@Override
	protected void endBlock() {
		if (paraOpen) {
			builder.endBlock(); // para
			paraLine = 0;
			paraOpen = false;
		}
		builder.endBlock(); // quote
	}
	
	@Override
	protected void handleBlockContent(String content) {
		if (blockLineCount == 1 && content.length() == 0) {
			return;
		}
		if (dialect.isEmptyLine(content) && blockLineCount > 1 && paraOpen) {
			builder.endBlock(); // para
			paraOpen = false;
			paraLine = 0;
			return;
		}
		if (!paraOpen) {
			builder.beginBlock(BlockType.PARAGRAPH, new Attributes());
			paraOpen = true;
		}
		if (paraLine != 0) {
			builder.lineBreak();
		}
		++paraLine;
		getDialect().emitMarkupLine(getParser(),state,content,0);
		
	}

	@Override
	protected void setOption(String key, String value) {
		// no options
	}	
}
