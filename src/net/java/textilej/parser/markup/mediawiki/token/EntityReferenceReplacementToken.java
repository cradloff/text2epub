package net.java.textilej.parser.markup.mediawiki.token;

import java.util.HashSet;
import java.util.Set;

import net.java.textilej.parser.markup.PatternBasedElement;
import net.java.textilej.parser.markup.PatternBasedElementProcessor;

public class EntityReferenceReplacementToken extends PatternBasedElement {

	private static final Set<String> allowedEntities = new HashSet<String>();
	static {
		allowedEntities.add("");
	}
	
	@Override
	protected String getPattern(int groupOffset) {
		return "&(#?[a-zA-Z0-9]{2,7});";
	}

	@Override
	protected int getPatternGroupCount() {
		return 1;
	}

	@Override
	protected PatternBasedElementProcessor newProcessor() {
		return new EntityReferenceProcessor();
	}

	private static class EntityReferenceProcessor extends PatternBasedElementProcessor {

		@Override
		public void emit() {
			String entity = group(1);
			getBuilder().entityReference(entity);
		}
		
	}
}
