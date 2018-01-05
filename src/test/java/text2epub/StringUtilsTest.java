package text2epub;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

public class StringUtilsTest {

	@Test
	public void splitCSV() {
		assertTrue(StringUtils.splitCSV("").isEmpty());
		assertTrue(StringUtils.splitCSV(null).isEmpty());
		assertThat(StringUtils.splitCSV("one"), CoreMatchers.hasItems("one"));
		assertThat(StringUtils.splitCSV(" one "), CoreMatchers.hasItems("one"));
		assertThat(StringUtils.splitCSV("one,two"), CoreMatchers.hasItems("one", "two"));
		assertThat(StringUtils.splitCSV("one,two , , four"), CoreMatchers.hasItems("one", "two", "", "four"));
	}

	@Test
	public void isEmpty() {
		assertTrue(StringUtils.isEmpty(null));
		assertTrue(StringUtils.isEmpty(""));
		assertTrue(StringUtils.isEmpty(" \t\r\n"));
		assertFalse(StringUtils.isEmpty("."));
		assertFalse(StringUtils.isEmpty(" . "));
	}

}
