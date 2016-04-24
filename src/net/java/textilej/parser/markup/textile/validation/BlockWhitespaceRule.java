package net.java.textilej.parser.markup.textile.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.java.textilej.validation.ValidationProblem;
import net.java.textilej.validation.ValidationRule;

public class BlockWhitespaceRule extends ValidationRule {

	private static final Pattern pattern = Pattern.compile("((?:bc|bq|pre|table|p)(?:\\.){1,2})(.)?",Pattern.MULTILINE);
	
	@Override
	public ValidationProblem findProblem(String markup, int offset, int length) {
		Matcher matcher = pattern.matcher(markup);
		if (offset > 0) {
			matcher.region(offset,offset+length);
		}
		while (matcher.find()) {
			int start = matcher.start();
			boolean startOfLine = false;
			if (start == 0) {
				startOfLine = true;
			} else {
				char c = markup.charAt(start-1);
				if (c == '\r' || c == '\n') {
					startOfLine = true;
				}
			}
			if (startOfLine) {
				String followingCharacter = matcher.group(2);
				if (followingCharacter == null || !followingCharacter.equals(" ")) {
					int problemLength = matcher.end(1)-start;
					String matched = matcher.group(1);
					return new ValidationProblem(ValidationProblem.Severity.WARNING,String.format("'%s' will not start a new block unless it is followed by a space character (' ')",matched),start,problemLength);
				}
			}
		}
		return null;
	}

}
