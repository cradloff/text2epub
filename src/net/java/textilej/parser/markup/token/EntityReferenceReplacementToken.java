package net.java.textilej.parser.markup.token;

import java.util.regex.Pattern;

public class EntityReferenceReplacementToken extends PatternEntityReferenceReplacementToken {

	public EntityReferenceReplacementToken(String token,String replacement) {
		super("("+Pattern.quote(token)+")",replacement);
	}

}
