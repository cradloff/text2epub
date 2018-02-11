package text2epub.converter;

import java.io.StringWriter;
import java.util.Map;

import net.java.textilej.parser.MarkupParser;
import net.java.textilej.parser.builder.HtmlDocumentBuilder;
import net.java.textilej.parser.markup.Dialect;
import net.java.textilej.parser.markup.confluence.ConfluenceDialect;
import net.java.textilej.parser.markup.mediawiki.MediaWikiDialect;
import net.java.textilej.parser.markup.textile.TextileDialect;
import net.java.textilej.parser.markup.trac.TracWikiDialect;

/** mit Textile-J nach HTML konvertieren */
public class TextileConverter implements Converter {

	public static void register(Map<String, Converter> converters) {
		converters.put(".textile", new TextileConverter(new TextileDialect()));
		converters.put(".wiki", new TextileConverter(new MediaWikiDialect()));
		converters.put(".mediawiki", new TextileConverter(new MediaWikiDialect()));
		converters.put(".trac", new TextileConverter(new TracWikiDialect()));
		converters.put(".confluence", new TextileConverter(new ConfluenceDialect()));
	}

	private Dialect dialect;

	public TextileConverter(Dialect dialect) {
		this.dialect = dialect;
	}

	@Override
	public String convert(String content) {
		// Textile nach Html konvertieren
		StringWriter out = new StringWriter();
		MarkupParser parser = new MarkupParser(dialect, new HtmlDocumentBuilder(out));
		parser.parse(content, false);
		String output = out.toString();

		return output;
	}

	@Override
	public boolean isFragment() {
		return true;
	}
}
