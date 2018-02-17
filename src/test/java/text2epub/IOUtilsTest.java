package text2epub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;

import org.junit.Test;

public class IOUtilsTest {
	@Test
	public void suffix() {
		assertEquals(".txt", IOUtils.suffix("test.txt"));
		assertEquals(".txt", IOUtils.suffix(new File("test.txt")));
		assertEquals(".txt", IOUtils.suffix("test.xml.txt"));
		assertEquals(".txt", IOUtils.suffix(new File("test.xml.txt")));
		assertNull(IOUtils.suffix("test"));
		assertNull(IOUtils.suffix(new File("test")));
	}

	@Test
	public void replaceSuffix() {
		assertEquals("test.new", IOUtils.replaceSuffix("test.old", ".new"));
		assertEquals("test.new", IOUtils.replaceSuffix(new File("test.old"), ".new"));
	}

	@Test(expected=RuntimeException.class)
	public void replaceSuffixFail() {
		IOUtils.replaceSuffix("test", ".new");
	}

	@Test
	public void buildOutputFilename() {
		verifyOutputFilename("text.xhtml", " text.md ");
		verifyOutputFilename("white_space.xhtml", " \t\r\nwhite \t\r\nspace.txt ");
		verifyOutputFilename("Umlauts.xhtml", "Ümläütß.adoc");
		verifyOutputFilename("Accent.xhtml", "Áççèñt.confluence");
		verifyOutputFilename("aaaaaa_c_eeee_iiii_n_ooooo_uuuu_yy.xhtml", "àáâãäå ç èéêë ìíîï ñ òóôõö ùúûü ýÿ.wiki");
	}

	private void verifyOutputFilename(String expected, String filename) {
		assertEquals(expected, IOUtils.buildOutputFilename(new File(filename)));
	}
}
