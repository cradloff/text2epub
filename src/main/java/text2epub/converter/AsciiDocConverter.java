package text2epub.converter;

import java.util.Map;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;

public class AsciiDocConverter implements Converter {

	public static void register(Map<String, Converter> converters) {
		converters.put(".adoc", new AsciiDocConverter());
	}

	@Override
	public String convert(String content) {
		// Asciidoc nach Html konvertieren
		Asciidoctor asciidoctor = Asciidoctor.Factory.create();
		Options options = Options.builder()
				.headerFooter(false)
				.docType("book")
				.backend("xhtml5")
				.build();
		String output = asciidoctor.convert(content, options);

		return output;
	}

	@Override
	public boolean isFragment() {
		return true;
	}
}
