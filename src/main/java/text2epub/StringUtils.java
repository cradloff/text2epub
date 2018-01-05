package text2epub;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StringUtils {
	private StringUtils() {}

	/**
	 * Splittet eine komma-separierte Liste von Teil-Strings auf. Leerzeichen werden ignoriert.
	 * Beispiel:
	 * <pre>
	 * splitCSV(null) == { }
	 * splitCSV("") == { }
	 * splitCSV("one") == { "one" }
	 * splitCSV("one,two") == { "one", "two" }
	 * splitCSV("one , two,, four") == { "one", "two", "", "four" }
	 * </pre>
	 * @param s Zeichenkette
	 * @return Liste der Teil-Strings
	 */
	public static List<String> splitCSV(String s) {
		if (isEmpty(s)) {
			return Collections.emptyList();
		}

		return Arrays.asList(s.trim().split("\\s*,\\s*"));
	}

	/**
	 * Prüft, ob die übergeben Zeichenkette null oder leer ist.
	 * @param s Zeichenkette
	 * @return true oder false
	 */
	public static boolean isEmpty(String s) {
		return s == null || s.trim().isEmpty();
	}
}
