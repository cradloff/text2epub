package net.java.textilej.parser.markup.textile.block;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.java.textilej.parser.Attributes;
import net.java.textilej.parser.DocumentBuilder.BlockType;
import net.java.textilej.parser.markup.Block;
import net.java.textilej.parser.markup.textile.TextileDialect;
import net.java.textilej.parser.outline.OutlineItem;
import net.java.textilej.parser.outline.OutlineParser;

public class TableOfContentsBlock extends Block {

	static final Pattern startPattern = Pattern.compile("\\s*\\{toc(?::([^\\}]+))?\\}\\s*");
	
	private int blockLineNumber = 0;

	private String style = "none";
	private int maxLevel = Integer.MAX_VALUE;

	
	private Matcher matcher;
	
	@Override
	public int processLineContent(String line,int offset) {
		if (blockLineNumber++ > 0) {
			setClosed(true);
			return 0;
		}

		if (!getDialect().isFilterGenerativeContents()) {
			String options = matcher.group(1);
			if (options != null) {
				String[] optionPairs = options.split("\\s*\\|\\s*");
				for (String optionPair: optionPairs) {
					String[] keyValue = optionPair.split("\\s*=\\s*");
					if (keyValue.length == 2) {
						String key = keyValue[0].trim();
						String value = keyValue[1].trim();
						
						if (key.equals("style")) {
							setStyle(value);
						} else if (key.equals("maxLevel")) {
							try {
								maxLevel = Integer.parseInt(value);
							} catch (NumberFormatException e) {}
						}
					}
				}
			}
			
			OutlineParser outlineParser = new OutlineParser(new TextileDialect());
			OutlineItem rootItem = outlineParser.parse(state.getMarkupContent());
			
			emitToc(rootItem);
		}
		return -1;
	}


	private void emitToc(OutlineItem item) {
		if (item.getChildren().isEmpty()) {
			return;
		}
		if ((item.getLevel()+1) > maxLevel) {
			return;
		}
		Attributes nullAttributes = new Attributes();
		
		builder.beginBlock(BlockType.NUMERIC_LIST, new Attributes(null,null,"list-style: "+style+";",null));
		for (OutlineItem child: item.getChildren()) {
			builder.beginBlock(BlockType.LIST_ITEM, nullAttributes);
			builder.link('#'+child.getId(), child.getLabel());
			emitToc(child);
			builder.endBlock();
		}
		builder.endBlock();
	}

	@Override
	public boolean canStart(String line,int lineOffset) {
		if (lineOffset == 0) {
			matcher = startPattern.matcher(line);
			return matcher.matches();
		} else {
			matcher = null;
			return false;
		}
	}
	
	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public int getMaxLevel() {
		return maxLevel;
	}

	public void setMaxLevel(int maxLevel) {
		this.maxLevel = maxLevel;
	}



}
