package net.java.textilej.parser.markup.token;

import net.java.textilej.parser.markup.PatternBasedElement;
import net.java.textilej.parser.markup.PatternBasedElementProcessor;

public class EntityWrappingReplacementToken extends PatternBasedElement {

	private String delimiter;
	private String leftEntity;
	private String rightEntity;
	
	public EntityWrappingReplacementToken(String delimiter, String leftEntity, String rightEntity) {
		this.delimiter = delimiter;
		this.leftEntity = leftEntity;
		this.rightEntity = rightEntity;
		if (delimiter.length() != 1) {
			throw new IllegalArgumentException(delimiter);
		}
	}

	@Override
	protected String getPattern(int groupOffset) {
		String quoted = Character.isLetterOrDigit(delimiter.charAt(0))?delimiter:"\\"+delimiter;
		return "(?:(?:(?<=\\W)|^)"+quoted+"([^"+quoted+"]+)"+quoted+"(?=\\W))";
	}

	@Override
	protected int getPatternGroupCount() {
		return 1;
	}

	@Override
	protected PatternBasedElementProcessor newProcessor() {
		return new EntityWrappingReplacementTokenProcessor(leftEntity,rightEntity);
	}
	
	private static class EntityWrappingReplacementTokenProcessor extends PatternBasedElementProcessor {
		private final String leftEntity;
		private final String rightEntity;

		public EntityWrappingReplacementTokenProcessor(String leftEntity, String rightEntity) {
			this.leftEntity = leftEntity;
			this.rightEntity = rightEntity;
		}

		@Override
		public void emit() {
			String content = group(1);
			builder.entityReference(leftEntity);
			builder.characters(content);
			builder.entityReference(rightEntity);
		}
	}

}
