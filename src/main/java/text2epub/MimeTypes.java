package text2epub;

import java.util.Map;
import java.util.TreeMap;

/** Hilfsklasse zur Bestimmung von Mime-Types */
public class MimeTypes {
	public static final String MIME_TYPE_CSS = "text/css";
	public static final String MIMETYPE_EPUB = "application/epub+zip";
	public static final String MIMETYPE_XHTML = "application/xhtml+xml";
	public static final String MIME_TYPE_SVG = "image/svg+xml";
	/** Mime-Types und zugehörige Endungen für Bilder */
	public static final String[][] MIME_TYPES_IMAGE = {
		{ "image/gif",	".gif" }, // GIF-Dateien
		{ "image/jpeg",	".jpeg", ".jpg", ".jpe"}, // JPEG-Dateien
		{ "image/png",	".png" }, // PNG-Dateien
		{ MIME_TYPE_SVG, ".svg" } // SVG-Grafiken
	};
	/** Mime-Type je Dateiendung */
	private static final Map<String, String> MIME_TYPES;
	static {
		Map<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		map.put(".css", MIME_TYPE_CSS);
		map.put(".html", MIMETYPE_XHTML);
		map.put(".xhtml", MIMETYPE_XHTML);
		for (String[] values : MIME_TYPES_IMAGE) {
			for (int i = 1; i < values.length; i++) {
				map.put(values[i], values[0]);
			}
		}

		MIME_TYPES = map;
	}

	/**
	 * Ermittelt den Mime-Type zum angegebenen Datei-Namen für ein Bild.
	 * @param filename Dateiname
	 * @return Mime-Type
	 */
	public static String getMimeType(String filename) {
		String suffix = IOUtils.suffix(filename);
		String mimeType = MIME_TYPES.get(suffix);

		if (mimeType == null) {
			throw new IllegalArgumentException("unknown mime-type for suffix " + suffix);
		}

		return mimeType;
	}

}
