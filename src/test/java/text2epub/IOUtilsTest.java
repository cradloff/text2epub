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
}
