package net.java.textilej.parser.markup.textile;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.java.textilej.parser.markup.ContentState;

public class TextileContentState extends ContentState {
	private static final Pattern NAMED_LINK_PATTERN = Pattern.compile("\\[(\\S+)\\]([a-zA-Z]{3,5}:\\S+)",Pattern.MULTILINE);
	
	private Map<String,String> nameToUrl = new HashMap<String, String>(); 
	
	@Override
	protected void setMarkupContent(String markupContent) {
		super.setMarkupContent(markupContent);
		preprocessContent(markupContent);
	}

	private void preprocessContent(String markupContent) {
		// look for named links
		Matcher matcher = NAMED_LINK_PATTERN.matcher(markupContent);
		while (matcher.find()) {
			String name = matcher.group(1);
			String href = matcher.group(2);
			nameToUrl.put(name, href);
		}
	}
	
	public String getNamedLinkUrl(String name) {
		return nameToUrl.get(name);
	}
}
