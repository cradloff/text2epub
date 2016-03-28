package org.markdown2epub;

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
		this.id = id;
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
}