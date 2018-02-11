package text2epub.converter;

import java.util.Map;

import com.github.rjeschke.txtmark.Configuration;
import com.github.rjeschke.txtmark.Configuration.Builder;
import com.github.rjeschke.txtmark.Processor;

/** Markdown nach HTML konvertieren */
public class MarkdownConverter implements Converter {

	public static void register(Map<String, Converter> converters) {
		converters.put(".txt", new MarkdownConverter(false));
		converters.put(".md", new MarkdownConverter(true));
	}

	boolean extended;

	public MarkdownConverter(boolean extended) {
		this.extended = extended;
	}

	@Override
	public String convert(String content) {
		// Markdown nach Html konvertieren
		Builder builder = Configuration.builder();
		if (extended) {
			builder = builder.forceExtentedProfile();
		}
		Configuration config = builder.build();
		String output = Processor.process(content, config);

		return output;
	}

	@Override
	public boolean isFragment() {
		return true;
	}

}
