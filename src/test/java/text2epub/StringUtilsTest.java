package text2epub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class StringUtilsTest {

	@Test
	public void splitCSV() {
		assertTrue(StringUtils.splitCSV("")
					.isEmpty());
		assertTrue(StringUtils.splitCSV(null)
					.isEmpty());
		assertThat(StringUtils.splitCSV("one"))
			.containsExactly("one");
		assertThat(StringUtils.splitCSV(" one "))
			.containsExactly("one");
		assertThat(StringUtils.splitCSV("one,two"))
			.containsExactly("one", "two");
		assertThat(StringUtils.splitCSV("one,two , , four"))
			.containsExactly("one", "two", "", "four");
	}

	@Test
	public void isEmpty() {
		assertThat(StringUtils.isEmpty(null))
			.isTrue();
		assertThat(StringUtils.isEmpty(""))
			.isTrue();
		assertThat(StringUtils.isEmpty(" \t\r\n"))
			.isTrue();
		assertThat(StringUtils.isEmpty("."))
			.isFalse();
		assertThat(StringUtils.isEmpty(" . "))
			.isFalse();
	}

}
