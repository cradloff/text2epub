package net.java.textilej.parser.markup.token;

import net.java.textilej.parser.markup.PatternBasedElementProcessor;

public class LiteralReplacementTokenProcessor extends PatternBasedElementProcessor {

	private final String literal;

	public LiteralReplacementTokenProcessor(String literal) {
		this.literal = literal;
	}

	@Override
	public void emit() {
		getBuilder().charactersUnescaped(literal);
	}

}
