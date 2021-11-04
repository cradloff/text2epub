package text2epub;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import text2epub.xml.NamedEntitesConverter;

public class NamedEntitiesConverterTest {
	@Test public void testConvert() {
		NamedEntitesConverter converter = NamedEntitesConverter.instance();
		assertThat(converter.convert(""))
			.isEqualTo("");
		assertThat(converter.convert("<html><head><title>Title</title></head></html>"))
			.isEqualTo("<html><head><title>Title</title></head></html>");
		assertThat(converter.convert("Dies &amp; &lt;das&gt;"))
			.isEqualTo("Dies &amp; &lt;das&gt;");
		assertThat(converter.convert("&Uuml;ber T&auml;ler und rei&szlig;ende B&auml;che"))
			.isEqualTo("Über Täler und reißende Bäche");
		assertThat(converter.convert("&AElig;ther"))
			.isEqualTo("Æther");
		assertThat(converter.convert("&mdash;&ndash;&hellip;"))
			.isEqualTo("—–…");
		assertThat(converter.convert("&#8220;Quoted&#8221;"))
			.isEqualTo("&#8220;Quoted&#8221;");
	}
}
