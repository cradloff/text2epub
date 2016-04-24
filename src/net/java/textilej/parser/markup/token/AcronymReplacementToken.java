package net.java.textilej.parser.markup.token;

import net.java.textilej.parser.markup.PatternBasedElement;
import net.java.textilej.parser.markup.PatternBasedElementProcessor;

public class AcronymReplacementToken extends PatternBasedElement {

	@Override
	protected String getPattern(int groupOffset) {
		return "(?:(?:(?<=\\W)|^)([A-Z]{3,})\\(([^\\)]+)\\))";
	}

	@Override
	protected int getPatternGroupCount() {
		return 2;
	}

	@Override
	protected PatternBasedElementProcessor newProcessor() {
		return new AcronymReplacementTokenProcessor();
	}

	private static class AcronymReplacementTokenProcessor extends PatternBasedElementProcessor {

		@Override
		public void emit() {
			String acronym = group(1);
			String acronymDef = group(2);
			state.addGlossaryTerm(acronym, acronymDef);
			builder.acronym(acronym, acronymDef);
		}
		
	}
}
