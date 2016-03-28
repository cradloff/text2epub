package text2epub;


/**
 * Daten zu einer Datei.
 *
 * @author Claus Radloff
 */
public class FileEntry {
	private String filename;
	private String mimeType;
	private String id;

	/**
	 * Konstruktor.
	 * @param filename Dateiname
	 * @param mimeType Mime-Type
	 * @param id ID
	 */
	public FileEntry(String filename, String mimeType, String id) {
		super();
		this.filename = filename;
		this.mimeType = mimeType;
		this.id = escape(id);
	}

	public String getFilename() {
		return filename;
	}

	public String getMimeType() {
		return mimeType;
	}

	public String getId() {
		return id;
	}

	/** Erzeugt eine gültige XML-ID aus der Zeichenkette */
	private static String escape(String s) {
		String t = s;
		// ungültige Zeichen entfernen
		t = s.replaceAll("[^a-zA-Z0-9-_]", "");
		// ID darf nicht mit 'xml' oder einer Ziffer beginnen
		if (t.startsWith("xml") || t.matches("\\d.*")) {
			t = "_" + t;
		}

		return t;
	}
}