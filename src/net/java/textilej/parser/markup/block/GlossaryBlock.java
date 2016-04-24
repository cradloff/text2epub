package net.java.textilej.parser.markup.block;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import net.java.textilej.parser.Attributes;
import net.java.textilej.parser.DocumentBuilder.BlockType;
import net.java.textilej.parser.markup.Block;

public abstract class GlossaryBlock extends Block {

	protected int blockLineNumber = 0;

	private String style;
	
	@Override
	public int processLineContent(String line,int offset) {
		if (blockLineNumber++ > 0) {
			setClosed(true);
			return 0;
		}
		if (!getDialect().isFilterGenerativeContents()) {
			SortedMap<String,String> glossary = new TreeMap<String, String>(state.getGlossaryTerms());
			
			builder.beginBlock(BlockType.DEFINITION_LIST, new Attributes(null,
					null, style == null ? null : "list-style: " + style, null));
			Attributes nullAttributes = new Attributes();
			for (Map.Entry<String, String> ent: glossary.entrySet()) {
				builder.beginBlock(BlockType.DEFINITION_TERM, nullAttributes);
				builder.characters(ent.getKey());
				builder.endBlock();
				
				builder.beginBlock(BlockType.DEFINITION_ITEM, nullAttributes);
				builder.characters(ent.getValue());
				builder.endBlock();
			}
			
			builder.endBlock();
		}
		return -1;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

}
