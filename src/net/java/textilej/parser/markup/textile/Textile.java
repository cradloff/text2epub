package net.java.textilej.parser.markup.textile;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.java.textilej.parser.Attributes;
import net.java.textilej.parser.util.MatcherAdaper;


public class Textile {

	private static final String REGEX_TEXTILE_CLASS_ID = "(?:\\(([^#\\)]+)?(?:#([^\\)]+))?\\))";
	private static final String REGEX_TEXTILE_STYLE = "(?:\\{([^\\}]+)\\})";
	private static final String REGEX_LANGUAGE = "(?:\\[([^\\]]+)\\])";
	
	public static final String REGEX_ATTRIBUTES = "(?:"+REGEX_TEXTILE_CLASS_ID+"|"+REGEX_TEXTILE_STYLE+"|"+REGEX_LANGUAGE+"){0,3}";
	public static final String REGEX_BLOCK_ATTRIBUTES = "(\\(+)?(\\)+)?(\\<|\\>|\\=|\\<\\>)?"+REGEX_ATTRIBUTES;
	
	public static final int ATTRIBUTES_GROUP_COUNT = 4;
	public static final int ATTRIBUTES_BLOCK_GROUP_COUNT = 7;
	
	private static final Pattern explicitBlockBeginPattern = Pattern.compile("(((h[1-6])|p|pre|bc|bq|table)|(fn([0-9]{1,2})))" + REGEX_ATTRIBUTES +"\\.\\.?\\s+.*");

	private static final Map<String,String> alignmentToStyle = new HashMap<String, String>();
	static {
		alignmentToStyle.put("<","text-align: left;");
		alignmentToStyle.put(">","text-align: right;");
		alignmentToStyle.put("=","text-align: center;");
		alignmentToStyle.put("<>","text-align: justify;");
	}
	
	public static Attributes configureAttributes(Attributes attributes, Matcher matcher, int offset,boolean block) {
		return configureAttributes(new MatcherAdaper(matcher),attributes, offset,block);
	}

	private static void appendStyles(Attributes attributes, String cssStyles) {
		if (cssStyles == null || cssStyles.length() == 0) {
			return;
		}
		String styles = attributes.getCssStyle();
		if (styles == null) {
			attributes.setCssStyle(cssStyles);
		} else {
			if (styles.endsWith(";")) {
				styles += " ";
			} else {
				styles += "; ";
			}
			styles += cssStyles;
			attributes.setCssStyle(styles);
		}
	}
	
	public static Attributes configureAttributes(net.java.textilej.parser.util.Matcher matcher,Attributes attributes,int offset,boolean block) {
		if (offset < 1) {
			throw new IllegalArgumentException();
		}
		if (block) {
			// padding (left)
			{
				String padding = matcher.group(offset);
				if (padding != null) {
					appendStyles(attributes, "padding-left: "+padding.length()+"em;");
				}
				++offset;
			}
			
			// padding (right)
			{
				String padding = matcher.group(offset);
				if (padding != null) {
					appendStyles(attributes, "padding-right: "+padding.length()+"em;");
				}
				++offset;
			}
			
			// alignment
			{
				String alignment = matcher.group(offset);
				if (alignment != null) {
					appendStyles(attributes, alignmentToStyle.get(alignment));
				}
				++offset;
			}
		}
		

		String cssClass2 = matcher.group(offset);
		String id = matcher.group(offset+1);
		String cssStyles2 = matcher.group(offset+2);
		String language = matcher.group(offset+3);

		if (id != null && attributes.getId() == null) {
			attributes.setId(id);
		}

		if (attributes.getCssClass() != null || cssClass2 != null) {
			attributes.setCssClass(attributes.getCssClass()==null?cssClass2:cssClass2==null?attributes.getCssClass():attributes.getCssClass()+' '+cssClass2);
		}
		appendStyles(attributes, cssStyles2);
		
		attributes.setLanguage(language);
		
		return attributes;		
	}

	public static boolean explicitBlockBegins(String line, int offset) {
		if (offset != 0) {
			return false;
		}
		return explicitBlockBeginPattern.matcher(line).matches();
	}
}
