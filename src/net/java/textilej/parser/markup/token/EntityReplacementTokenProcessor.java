package net.java.textilej.parser.markup.token;

import net.java.textilej.parser.markup.PatternBasedElementProcessor;

public class EntityReplacementTokenProcessor extends PatternBasedElementProcessor {

	private final String entity;

	public EntityReplacementTokenProcessor(String entity) {
		this.entity = entity;
	}

	@Override
	public void emit() {
		getBuilder().entityReference(entity);
	}

}
