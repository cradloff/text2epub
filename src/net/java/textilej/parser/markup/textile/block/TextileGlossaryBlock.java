package net.java.textilej.parser.markup.textile.block;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.java.textilej.parser.markup.block.GlossaryBlock;

public class TextileGlossaryBlock extends GlossaryBlock {

	static final Pattern startPattern = Pattern.compile("\\s*\\{glossary(?::([^\\}]+))?\\}\\s*");
	
	private Matcher matcher;
	
	@Override
	public int processLineContent(String line,int offset) {
		if (blockLineNumber == 0) {
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
						}
					}
				}
			}
		}
		return super.processLineContent(line, offset);
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
}
