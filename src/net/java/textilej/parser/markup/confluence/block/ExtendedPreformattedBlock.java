package net.java.textilej.parser.markup.confluence.block;

import net.java.textilej.parser.Attributes;
import net.java.textilej.parser.DocumentBuilder.BlockType;

/**
 * quoted text block, matches blocks that start with <code>bc. </code>.  
 * Creates an extended block type of {@link ParagraphBlock paragraph}.
 * 
 * @author dgreen
 */
public class ExtendedPreformattedBlock extends AbstractConfluenceDelimitedBlock {

	
	public ExtendedPreformattedBlock() {
		super("noformat");
	}
	
	@Override
	protected void beginBlock() {
		Attributes attributes = new Attributes();
		builder.beginBlock(BlockType.PREFORMATTED, attributes);
	}

	@Override
	protected void endBlock() {
		builder.endBlock(); // pre
	}
	
	@Override
	protected void handleBlockContent(String content) {
		if (content.length() > 0) {
			builder.characters(content);
		} else if (blockLineCount == 1) {
			return;
		}
		builder.characters("\n");
	}
	
	@Override
	protected void setOption(String key, String value) {
		// no options
	}
		
}
