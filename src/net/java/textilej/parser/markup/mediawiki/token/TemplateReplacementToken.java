package net.java.textilej.parser.markup.mediawiki.token;

import java.util.HashMap;
import java.util.Map;

import net.java.textilej.parser.markup.PatternBasedElement;
import net.java.textilej.parser.markup.PatternBasedElementProcessor;

public class TemplateReplacementToken extends PatternBasedElement {

	private static Map<String,Class<? extends PatternBasedElementProcessor>> processorByTemplate = new HashMap<String, Class<? extends PatternBasedElementProcessor>>();
	static {
		processorByTemplate.put("endash",EndashElementProcessor.class);
		processorByTemplate.put("ndash",EndashElementProcessor.class);
		processorByTemplate.put("mdash",EmdashElementProcessor.class);
		processorByTemplate.put("emdash",EmdashElementProcessor.class);
	}
	
	@Override
	protected String getPattern(int groupOffset) {
		return "(\\{\\{([^\\s]+)\\}\\})";
	}

	@Override
	protected int getPatternGroupCount() {
		return 2;
	}

	@Override
	protected PatternBasedElementProcessor newProcessor() {
		return new DispatchingProcessor();
	}

	private static class DispatchingProcessor extends PatternBasedElementProcessor {
		@Override
		public void emit() {
			Class<? extends PatternBasedElementProcessor> processor = processorByTemplate.get(group(2));
			if (processor == null) {
				getBuilder().characters(group(1));
			} else {
				PatternBasedElementProcessor delegate;
				try {
					delegate = processor.newInstance();
				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
				delegate.setLineStartOffset(getLineStartOffset());
				delegate.setLineEndOffset(getLineEndOffset());
				delegate.setParser(getParser());
				delegate.setState(getState());
				delegate.setGroup(1,group(1),start(1),end(1));
				delegate.setGroup(2,group(2),start(2),end(2));
				delegate.emit();
			}
		}
	}
	
	public static class EndashElementProcessor extends PatternBasedElementProcessor {
		@Override
		public void emit() {
			getBuilder().entityReference("nbsp");
			getBuilder().entityReference("ndash");
			getBuilder().characters(" ");
		}
	}
	public static class EmdashElementProcessor extends PatternBasedElementProcessor {
		@Override
		public void emit() {
			getBuilder().entityReference("nbsp");
			getBuilder().entityReference("mdash");
			getBuilder().characters(" ");
		}
	}
}
