package net.java.textilej.parser.markup.token;

import net.java.textilej.parser.markup.PatternBasedElementProcessor;

public class LineBreakReplacementTokenProcessor extends PatternBasedElementProcessor {

	@Override
	public void emit() {
		getBuilder().lineBreak();
	}

}
