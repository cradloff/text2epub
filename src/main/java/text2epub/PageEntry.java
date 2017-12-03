package text2epub;


/**
 * Daten zu einem Seiten-Eintrag im Inhaltsverzeichnis.
 */
public class PageEntry {
	private String id;
	private String title;
	private String filename;

	/**
	 * Konstruktor.
	 * @param id ID
	 * @param title Überschrift
	 * @param filename Dateiname
	 */
	public PageEntry(String id, String title, String filename) {
		this.id = id;
		this.title = title;
		this.filename = filename;
	}

	/**
	 * Liefert die ID des Eintrags zurück.
	 * @return ID
	 */
	public String getId() {
		return id;
	}

	/**
	 * Liefert die Überschrift zurück.
	 * @return Überschrift
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Liefert den Dateinamen zurück.
	 * @return Dateiname
	 */
	public String getFilename() {
		return filename;
	}
}
