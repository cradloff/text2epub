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
		Options options = new Options();
		options.setHeaderFooter(false);
		options.setDocType("book");
		options.setBackend("xhtml5");
		String output = asciidoctor.convert(content, options);

		return output;
	}

	@Override
	public boolean isFragment() {
		return true;
	}
}
