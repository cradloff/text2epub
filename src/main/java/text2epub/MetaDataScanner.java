package text2epub;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Die Klasse versucht, aus Verzeichnisnamen die Meta-Daten für ein Buch zu extrahieren */
public class MetaDataScanner {
	private static final Pattern GROUP_TITLE_AUTHOR = Pattern.compile("(?<groupPosition>\\d+) *[-_] *(?<title>.*) *[-_] *(?<author>.*)");
	private static final Pattern GROUP_TITLE = Pattern.compile("(?<groupPosition>\\d+) *[-_] *(?<title>.*)(?<author>)");
	private static final Pattern TITLE_AUTHOR = Pattern.compile("(?<groupPosition>)(?<title>.*) *[-_] *(?<author>.*)");
	private static final Pattern TITLE_ONLY = Pattern.compile("(?<groupPosition>)(?<title>.*)(?<author>)");
	private static final Pattern[] PATTERNS = {
			GROUP_TITLE_AUTHOR, GROUP_TITLE, TITLE_AUTHOR, TITLE_ONLY
	};
	private static final Pattern LASTNAME_FIRSTNAME = Pattern.compile("(?<lastname>.*),(?<firstname>.*)");
	private static final Pattern FIRSTNAME_LASTNAME = Pattern.compile("(?<firstname>.*) (?<lastname>.*)");
	private static final Pattern LASTNAME = Pattern.compile("(?<lastname>.*)(?<firstname>)");
	private static final Pattern[] NAMEPATTERNS = {
			LASTNAME_FIRSTNAME, FIRSTNAME_LASTNAME, LASTNAME
	};
	static Properties parseDirectoryName(String directory) {
		Properties props = new Properties();
		for (Pattern pattern : PATTERNS) {
			if (applyPattern(directory, pattern, props)) {
				break;
			}
		}

		return props;
	}

	private static boolean applyPattern(String value, Pattern pattern, Properties props) {
		Matcher matcher = pattern.matcher(value);
		boolean match = matcher.matches();
		if (match) {
			putIfFound(props, matcher, "title");
			putIfFound(props, matcher, "author");
			putIfFound(props, matcher, "groupPosition");
			// Author in beiden Formaten speichern
			if (props.containsKey("author")) {
				putAuthor(props);
			}
		}

		return match;
	}

	private static void putIfFound(Properties props, Matcher matcher, String property) {
		try {
			String value = matcher.group(property);
			props.put(property, value.trim());
		} catch (IllegalArgumentException ignored) {}
	}

	private static void putAuthor(Properties props) {
		String author = props.getProperty("author");
		for (Pattern pattern : NAMEPATTERNS) {
			Matcher matcher = pattern.matcher(author);
			if (matcher.matches()) {
				String firstname = matcher.group("firstname").trim();
				String lastname = matcher.group("lastname").trim();
				String normal = firstname + " " + lastname;
				String fileAs = lastname + ", " + firstname;
				if (firstname.isBlank() || lastname.isBlank()) {
					fileAs = "";
				}
				props.put("author", normal.trim());
				props.put("authorFileAs", fileAs);
				break;
			}
		}
	}
}
