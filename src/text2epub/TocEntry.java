package text2epub;

import java.util.ArrayList;
import java.util.List;

/**
 * Daten zu einem Eintrag im Inhaltsverzeichnis.
 */
public class TocEntry {
	private String level;
	private String title;
	private String filename;
	private List<TocEntry> subEntries = new ArrayList<>();

	/**
	 * Konstruktor.
	 * @param level Level
	 * @param title Überschrift
	 * @param filename Dateiname
	 */
	public TocEntry(String level, String title, String filename) {
		this.level = level;
		this.title = title;
		this.filename = filename;
	}

	/**
	 * Liefert den Level des Eintrags zurück.
	 * @return Level
	 */
	public String getLevel() {
		return level;
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

	/**
	 * Liefert die Unter-Einträge zurück.
	 * @return Unter-Einträge
	 */
	public List<TocEntry> getSubEntries() {
		return subEntries;
	}

	/**
	 * Fügt dem Eintrag einen Sub-Eintrag hinzu.
	 * @param entry Sub-Eintrag
	 */
	public void add(TocEntry entry) {
		if (subEntries.isEmpty()) {
			subEntries.add(entry);
		} else {
			// hat der Eintrag den selben (oder höheren) Level wie der letzte?
			TocEntry last = subEntries.get(subEntries.size() - 1);
			if (entry.getLevel().compareTo(last.getLevel()) <= 0) {
				subEntries.add(entry);
			} else {
				last.add(entry);
			}
		}
	}
}
