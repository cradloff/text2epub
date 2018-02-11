package text2epub.converter;

import java.util.Map;

public class XHtmlConverter implements Converter {

	public static void register(Map<String, Converter> converters) {
		converters.put(".xhtml", new XHtmlConverter());
	}

	@Override
	public String convert(String content) {
		// Content ist bereits XHtml
		return content;
	}

	@Override
	public boolean isFragment() {
		return false;
	}

}
