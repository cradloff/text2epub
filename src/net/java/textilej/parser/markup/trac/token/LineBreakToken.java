package net.java.textilej.parser.markup.trac.token;

import net.java.textilej.parser.markup.PatternBasedElement;
import net.java.textilej.parser.markup.PatternBasedElementProcessor;

public class LineBreakToken extends PatternBasedElement {

	@Override
	protected String getPattern(int groupOffset) {
		return "(\\[\\[BR\\]\\])";
	}

	@Override
	protected int getPatternGroupCount() {
		return 1;
	}

	@Override
	protected PatternBasedElementProcessor newProcessor() {
		return new LineBreakProcessor();
	}

	private static class LineBreakProcessor extends PatternBasedElementProcessor {
		@Override
		public void emit() {
			builder.lineBreak();
		}
	}
}
