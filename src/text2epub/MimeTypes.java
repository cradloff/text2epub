package text2epub;

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

	/**
	 * Ermittelt den Mime-Type zum angegebenen Datei-Namen für ein Bild.
	 * @param filename Dateiname
	 * @return Mime-Type
	 */
	public static String getMimeType(String filename) {
		String mimeType = null;
		String suffix = filename.substring(filename.lastIndexOf('.'));
		for (String[] s : MIME_TYPES_IMAGE) {
			for (int i = 1; i < s.length; i++) {
				if (suffix.equalsIgnoreCase(s[i])) {
					mimeType = s[0];
				}
			}
		}

		if (mimeType == null) {
			throw new IllegalArgumentException("unknown mime-type for suffix " + suffix);
		}

		return mimeType;
	}

}
