package net.java.textilej.parser.markup.trac.phrase;



import net.java.textilej.parser.Attributes;
import net.java.textilej.parser.DocumentBuilder.SpanType;
import net.java.textilej.parser.markup.PatternBasedElement;
import net.java.textilej.parser.markup.PatternBasedElementProcessor;

public class EscapePhraseModifier extends PatternBasedElement {

	@Override
	protected String getPattern(int groupOffset) {
		String escapedContent = "(\\S(?:.*?\\S)?)";
		return 
		"(?:(?:`" +
		escapedContent + // content
		"`)|(?:\\{\\{" +
		escapedContent + // content
		"\\}\\}))";
	}

	@Override
	protected int getPatternGroupCount() {
		return 2;
	}

	@Override
	protected PatternBasedElementProcessor newProcessor() {
		return new EscapeProcessor();
	}
	
	private static class EscapeProcessor extends PatternBasedElementProcessor {

		private EscapeProcessor() {
		}
		
		@Override
		public void emit() {
			getBuilder().beginSpan(SpanType.MONOSPACE, new Attributes());
			String group = group(1);
			if (group == null) {
				group = group(2);
			}
			getBuilder().characters(group);
			getBuilder().endSpan();
	
		}

	}


}
