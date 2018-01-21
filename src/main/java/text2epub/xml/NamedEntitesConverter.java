package text2epub.xml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Ersetzt named entities durch die entsprechenden Unicode-Zeichen. Das ist notwendig, da
 * XHTML5 keine named entities erlaubt außer lt, gt, amp
 */
public class NamedEntitesConverter {
	private static final NamedEntitesConverter INSTANCE = new NamedEntitesConverter();
	private Map<String, String> entities = new HashMap<>();

	private NamedEntitesConverter() {
		try (
				InputStream is = getClass().getResourceAsStream("/named_entities.csv");
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				) {
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				// Leerzeilen und Kommentare überspringen
				if (! line.trim().isEmpty() && ! line.startsWith("#")) {
					String[] tokens = line.split("\t");
					String entity = tokens[0];
					if (entity.endsWith(";")) {
						entity = entity.substring(0, entity.length() - 1);
					}
					if (tokens.length == 2) {
						String unicode = tokens[1];
						unicode = unicode.substring(2);
						Character.toChars(Integer.parseInt(unicode, 16));
					} else {
						String character = tokens[2];
						entities.put(entity, character);
					}
				}

				// geschützte Entities durch sich selbst ersetzen
				entities.put("lt", "&lt;");
				entities.put("gt", "&gt;");
				entities.put("amp", "&amp;");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static NamedEntitesConverter instance() {
		return INSTANCE;
	}

	private static final int TEXT = 0;
	private static final int ENTITY = 1;

	public String convert(String s) {
		int state = TEXT;
		StringBuilder sb = new StringBuilder(s.length());
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (state == TEXT) {
				if (c == '&') {
					state = ENTITY;
					buf.setLength(0);
				} else {
					sb.append(c);
				}
			} else if (state == ENTITY) {
				if (c == ';') {
					state = TEXT;
					String entity = buf.toString();
					sb.append(entities.get(entity));
				} else if (c == '#') {
					state = TEXT;
					sb.append("&#");
				} else {
					buf.append(c);
				}
			}
		}

		return sb.toString();
	}
}
