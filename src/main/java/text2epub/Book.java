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
	public static final String NCX = "content.ncx";
	public static final FileEntry CSS = new FileEntry("book.css", "book.css", MimeTypes.MIME_TYPE_CSS, "style-sheet");
	private Properties props = new Properties();
	private Map<String, String> res = new HashMap<>();
	private Map<String, Object> params = new HashMap<>();
	private List<FileEntry> contentFiles = new ArrayList<>();
	/** Medien (Dateiname / Mime-Type) */
	private List<FileEntry> mediaFiles = new ArrayList<>();
	/** Einträge für Inhaltsverzeichnis (Link / Titel) */
	private TocEntry tocRoot = new TocEntry("h0", "tocRoot", "n/a");
	/** Einträge für Seiten-Einträge */
	private List<PageEntry> pageEntries;
	private File filename;
	private String stylesheet;
	private int page;

	/**
	 * Konstruktor.
	 */
	public Book() {
		params.put("OPF", OPF);
		params.put("NCX", NCX);
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
	}

	public void setProperties(Properties props) {
		this.props = props;
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
	 * Initialisiert die Resourcen mit der angegebenen Locale.
	 * @param locale Locale
	 */
	public void initResources(Locale locale) {
		// Resource-Bundle in Map umkopieren
		ResourceBundle resBundle = ResourceBundle.getBundle("Text2Epub", locale);
		for (String key : resBundle.keySet()) {
			res.put(key, resBundle.getString(key));
		}
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
	 * Setzt einen Parameter.
	 * @param key Name des Parameters
	 * @param value Wert des Parameters
	 */
	public void setParam(String key, String value) {
		params.put(key, value);
	}

	/**
	 * Liefert den Namen des Stylesheets zurück.
	 * @return Stylesheet
	 */
	public String getStylesheet() {
		if (stylesheet != null) {
			return stylesheet;
		}

		return CSS.getFilename();
	}

	/**
	 * Setzt den Namen des Stylesheets. Wird <code>null</code> übergeben wird als
	 * Default der Name des CSS-Eintrags verwendet.
	 * @param stylesheet Stylesheet
	 */
	public void setStylesheet(String stylesheet) {
		this.stylesheet = stylesheet;
	}

	/**
	 * Liefert die eindeutige UUID des Buches zurück.
	 * @return UUID
	 */
	public String getUUID() {
		// Die folgende Buchidentifikation ist einzigartig für jedes Buch zu wählen
		String uuid = props.getProperty("UUID");
		if (StringUtils.isEmpty(uuid)) {
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
	 * Liefert zum übergebenen Quell-Dateinamen den Ziel-Dateinamen im epub zurück.
	 * @param srcFilename Quell-Dateiname
	 * @return Ziel-Dateiname
	 */
	public String resolve(String srcFilename) {
		String dstFilename = srcFilename;
		for (FileEntry entry : contentFiles) {
			if (srcFilename.equals(entry.getSrcFilename())) {
				dstFilename = entry.getFilename();
			}
		}

		return dstFilename;
	}

	/**
	 * Setzt die aktuelle Seite.
	 * @param page aktuelle Seite
	 */
	public int initpage(int page) {
		this.page = page;
		return page;
	}

	/**
	 * Liefert die nächste Seite zurück.
	 */
	public int getNextpage() {
		return ++page;
	}

	/**
	 * Liefert das Erstellungsdatum zurück.
	 * @return Erstellungsdatum
	 */
	public String getCreation() {
		String creation = props.getProperty("creationDate");
		if (creation == null) {
			creation = String.format("%1$tFT%1$tTZ", new Date());
		}

		return creation;
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
		return tocRoot.getSubEntries();
	}

	public void addTocEntry(TocEntry entry) {
		// neuen Eintrag einsortieren
		tocRoot.add(entry);
	}

	public List<PageEntry> getPageEntries() {
		return pageEntries;
	}

	public void addPageEntry(PageEntry entry) {
		if (pageEntries == null) {
			pageEntries = new ArrayList<>();
		}

		pageEntries.add(entry);
	}
}
