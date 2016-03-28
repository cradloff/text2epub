package text2epub;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

/** Daten zum Buch */
public class Book {
	public static final String OPF = "content.opf";
	public static final String COVER = "cover.xhtml";
	public static final String COVER_ID = "cover-image";
	public static final String NCX = "content.ncx";
	public static final String TOC = "toc.xhtml";
	public static final String CSS = "book.css";
	private Properties props = new Properties();
	private Map<String, String> res = new HashMap<>();
	private Map<String, Object> params = new HashMap<>();
	private List<FileEntry> contentFiles = new ArrayList<>();
	/** Medien (Dateiname / Mime-Type) */
	private List<FileEntry> mediaFiles = new ArrayList<>();
	/** Einträge für Inhaltsverzeichnis (Link / Titel) */
	private List<TocEntry> tocEntries = new ArrayList<>();
	private File filename;

	/**
	 * Konstruktor.
	 */
	public Book() {
		params.put("OPF", OPF);
		params.put("COVER", COVER);
		params.put("COVER_ID", COVER_ID);
		params.put("NCX", NCX);
		params.put("TOC", TOC);
	}

	/**
	 * Liest die Properties ein.
	 * @param file Dateiname
	 * @throws IOException
	 */
	public void readProperties(File file) throws IOException {
		try (InputStream pin = new FileInputStream(file)) {
			props.loadFromXML(pin);
		}
		// Resource-Bundle in Map umkopieren
		ResourceBundle resBundle = ResourceBundle.getBundle("Text2Epub", Locale.forLanguageTag(getProperty("language")));
		for (String key : resBundle.keySet()) {
			res.put(key, resBundle.getString(key));
		}
	}

	/**
	 * Liefert die Properties zurück.
	 * @return Properties
	 */
	public Properties getProperty() {
		return props;
	}

	/**
	 * Liefert das Property zurück.
	 * @param name Name des Properties
	 * @return Property
	 */
	public String getProperty(String name) {
		return props.getProperty(name);
	}

	/**
	 * Liefert die Resourcen als Map zurück.
	 * @return Resourcen
	 */
	public Map<String, String> getResource() {
		return res;
	}

	/**
	 * Liefert die Resource zurück.
	 * @param name Name der Resource
	 * @return Resource
	 */
	public String getResource(String name) {
		return res.get(name);
	}

	/**
	 * Liefert die Parameter zurück.
	 * @return Parameter
	 */
	public Map<String, Object> getParams() {
		return params;
	}

	/**
	 * Liefert den Namen des Stylesheets zurück.
	 * @return Stylesheet
	 */
	public String getStylesheet() {
		return CSS;
	}

	/**
	 * Liefert die eindeutige UUID des Buches zurück.
	 * @return UUID
	 */
	public String getUUID() {
		// Die folgende Buchidentifikation ist einzigartig für jedes Buch zu wählen
		String uuid = props.getProperty("UUID");
		if (uuid == null || uuid.isEmpty()) {
			uuid = "UUID-gen" + filename.getName().hashCode();
		}

		return uuid;
	}

	/**
	 * Liefert den Dateinamen zurück.
	 * @return Dateiname
	 */
	public File getFilename() {
		return filename;
	}

	/**
	 * Setzt den Dateinamen.
	 * @param filename Dateiname
	 */
	public void setFilename(File filename) {
		this.filename = filename;
	}

	/**
	 * Liefert das Erstellungsdatum zurück.
	 * @return Erstellungsdatum
	 */
	public String getCreation() {
		return String.format("%tF", new Date());
	}

	public List<FileEntry> getContentFiles() {
		return contentFiles;
	}

	public void addContentFile(FileEntry entry) {
		contentFiles.add(entry);
	}

	public List<FileEntry> getMediaFiles() {
		return mediaFiles;
	}

	public void addMediaFile(FileEntry entry) {
		mediaFiles.add(entry);
	}

	public List<TocEntry> getTocEntries() {
		return tocEntries;
	}

	public void addTocEntry(TocEntry entry) {
		tocEntries.add(entry);
	}

}
