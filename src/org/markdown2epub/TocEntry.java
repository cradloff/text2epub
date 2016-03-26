package org.markdown2epub;

/**
 * Daten zu einem Eintrag im Inhaltsverzeichnis.
 */
public class TocEntry {
	private String title;
	private String filename;

	/**
	 * Konstruktor.
	 * @param title Überschrift
	 * @param filename Dateiname
	 */
	public TocEntry(String title, String filename) {
		this.title = title;
		this.filename = filename;
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
