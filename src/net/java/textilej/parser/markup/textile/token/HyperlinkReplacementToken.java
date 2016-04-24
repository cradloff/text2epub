package net.java.textilej.parser.markup.textile.token;

import net.java.textilej.parser.ImageAttributes;
import net.java.textilej.parser.markup.PatternBasedElement;
import net.java.textilej.parser.markup.PatternBasedElementProcessor;
import net.java.textilej.parser.markup.textile.TextileContentState;

public class HyperlinkReplacementToken extends PatternBasedElement {
	// FROM RFC 1738

//	alpha          = lowalpha | hialpha
//	digit          = "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" |
//	                 "8" | "9"
//	safe           = "$" | "-" | "_" | "." | "+"
//	extra          = "!" | "*" | "'" | "(" | ")" | ","
//	national       = "{" | "}" | "|" | "\" | "^" | "~" | "[" | "]" | "`"
//	punctuation    = "<" | ">" | "#" | "%" | <">
//
//
//	reserved       = ";" | "/" | "?" | ":" | "@" | "&" | "="
//	hex            = digit | "A" | "B" | "C" | "D" | "E" | "F" |
//	                 "a" | "b" | "c" | "d" | "e" | "f"
//	escape         = "%" hex hex
//
//	unreserved     = alpha | digit | safe | extra
//	uchar          = unreserved | escape
//	xchar          = unreserved | reserved | escape
//	digits         = 1*digit

	// *[ uchar | ";" | ":" | "@" | "&" | "=" ]
	
	@Override
	protected String getPattern(int groupOffset) {
		return "(?:(\"|\\!)([^\"\\!]+)\\"+(1+groupOffset)+":([^\\s]*[^\\s!.)(,]))";
	}

	@Override
	protected int getPatternGroupCount() {
		return 3;
	}

	@Override
	protected PatternBasedElementProcessor newProcessor() {
		return new HyperlinkReplacementTokenProcessor();
	}
	
	private static class HyperlinkReplacementTokenProcessor extends PatternBasedElementProcessor {
		@Override
		public void emit() {
			String hyperlinkBoundaryText = group(1);
			String hyperlinkSrc = group(2);
			String href = group(3);
			String namedLinkUrl = ((TextileContentState)getState()).getNamedLinkUrl(href);
			if (namedLinkUrl != null) {
				href = namedLinkUrl;
			}
			
			if (hyperlinkBoundaryText.equals("\"")) {
				builder.link(href, hyperlinkSrc);
			} else {
				builder.imageLink(new ImageAttributes(),href, hyperlinkSrc);
			}
		}
	}

}
