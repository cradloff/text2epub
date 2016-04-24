package net.java.textilej.parser.markup.confluence.block;

import net.java.textilej.parser.markup.Block;

public abstract class ParameterizedBlock extends Block {

	public void setOptions(String options) {
		if (options == null) {
			return;
		}
		String[] opts = options.split("\\s*\\|\\s*");
		for (String optionPair: opts) {
			String[] keyValue = optionPair.split("\\s*=\\s*");
			if (keyValue.length == 2) {
				String key = keyValue[0].trim();
				String value = keyValue[1].trim();
				setOption(key,value);
			}
		}
	}

	protected abstract void setOption(String key, String value);
}
