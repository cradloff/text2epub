package text2epub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.File;

import org.junit.jupiter.api.Test;

public class IOUtilsTest {
	@Test
	public void suffix() {
		assertThat(IOUtils.suffix("test.txt"))
			.isEqualTo(".txt");
		assertThat(IOUtils.suffix(new File("test.txt")))
			.isEqualTo(".txt");
		assertThat(IOUtils.suffix("test.xml.txt"))
			.isEqualTo(".txt");
		assertThat(IOUtils.suffix(new File("test.xml.txt")))
			.isEqualTo(".txt");
		assertThat(IOUtils.suffix("test"))
			.isNull();
		assertThat(IOUtils.suffix(new File("test")))
			.isNull();
	}

	@Test
	public void replaceSuffix() {
		assertThat(IOUtils.replaceSuffix("test.old", ".new"))
			.isEqualTo("test.new");
		assertThat(IOUtils.replaceSuffix(new File("test.old"), ".new"))
			.isEqualTo("test.new");
	}

	@Test
	public void replaceSuffixFail() {
		assertThatExceptionOfType(RuntimeException.class)
			.isThrownBy(() -> IOUtils.replaceSuffix("test", ".new"));
	}

	@Test
	public void buildOutputFilename() {
		verifyOutputFilename("text.xhtml", " text.md ");
		verifyOutputFilename("aaaaaa_c_eeee_iiii_n_ooooo_uuuu_yy.xhtml", "àáâãäå ç èéêë ìíîï ñ òóôõö ùúûü ýÿ.wiki");
		verifyOutputFilename("text.xhtml", "folders/with/text.txt");
	}
	
	@Test
	public void normalize() {
		assertThat(IOUtils.normalize(" \t\r\nwhite \t\r\nspace.txt "))
			.isEqualTo("white_space.txt");
		assertThat(IOUtils.normalize("Ümläütß.adoc"))
			.isEqualTo("Umlauts.adoc");
		assertThat(IOUtils.normalize("Áççèñt.confluence"))
			.isEqualTo("Accent.confluence");
		assertThat(IOUtils.normalize("Text (with special chars:,;-).txt"))
			.isEqualTo("Text_with_special_chars.txt");
	}

	private void verifyOutputFilename(String expected, String filename) {
		assertThat(IOUtils.buildOutputFilename(new File(filename)))
			.isEqualTo(expected);
	}
}
