package text2epub;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import text2epub.xml.NamedEntitesConverter;

public class NamedEntitiesConverterTest {
	@Test public void testConvert() {
		NamedEntitesConverter converter = NamedEntitesConverter.instance();
		assertEquals("", converter.convert(""));
		assertEquals("<html><head><title>Title</title></head></html>",
				converter.convert("<html><head><title>Title</title></head></html>"));
		assertEquals("Dies &amp; &lt;das&gt;", converter.convert("Dies &amp; &lt;das&gt;"));
		assertEquals("Über Täler und reißende Bäche",
				converter.convert("&Uuml;ber T&auml;ler und rei&szlig;ende B&auml;che"));
		assertEquals("Æther", converter.convert("&AElig;ther"));
		assertEquals("—–…", converter.convert("&mdash;&ndash;&hellip;"));
		assertEquals("&#8220;Quoted&#8221;", converter.convert("&#8220;Quoted&#8221;"));
	}
}
