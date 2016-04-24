package net.java.textilej.parser.markup.confluence.block;

import net.java.textilej.parser.Attributes;
import net.java.textilej.parser.DocumentBuilder.BlockType;

public class CodeBlock extends AbstractConfluenceDelimitedBlock {


	private String title;
	
	public CodeBlock() {
		super("code");
	}

	@Override
	protected void beginBlock() {
		if (title != null) {
			Attributes attributes = new Attributes();
			attributes.setTitle(title);
			builder.beginBlock(BlockType.PANEL, attributes);
		}
		Attributes attributes = new Attributes();

		builder.beginBlock(BlockType.PREFORMATTED, new Attributes());
		builder.beginBlock(BlockType.CODE, attributes);
	}

	@Override
	protected void handleBlockContent(String content) {
		builder.characters(content);
		builder.characters("\n");
	}
	
	@Override
	protected void endBlock() {
		if (title != null) {
			builder.endBlock(); // panel	
		}
		builder.endBlock(); // code
		builder.endBlock(); // pre
	}
	
	@Override
	protected void resetState() {
		super.resetState();
		title = null;
	}

	
	@Override
	protected void setOption(String key, String value) {
		if (key.equals("title")) {
			title = value;
		}
	}	
}
