package net.java.textilej.parser.markup.token;

import net.java.textilej.parser.markup.PatternBasedElement;
import net.java.textilej.parser.markup.PatternBasedElementProcessor;

public class PatternLineBreakReplacementToken extends PatternBasedElement {

	private String pattern;
	
	public PatternLineBreakReplacementToken(String pattern) {
		this.pattern = pattern;
	}

	@Override
	protected String getPattern(int groupOffset) {
		return pattern;
	}

	@Override
	protected int getPatternGroupCount() {
		return 1;
	}

	@Override
	protected PatternBasedElementProcessor newProcessor() {
		return new LineBreakReplacementTokenProcessor();
	}

}
