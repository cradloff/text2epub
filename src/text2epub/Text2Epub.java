package text2epub;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.github.rjeschke.txtmark.Configuration;
import com.github.rjeschke.txtmark.Processor;

/**
 * Konvertiert Text-Dateien nach Epub.
 *
 * @author Claus Radloff
 */
public class Text2Epub {
	private static final String TOC = "toc.xhtml";
	private static final String COVER = "cover.xhtml";
	private static final String MIMETYPE_XHTML = "application/xhtml+xml";
	private static final String PROPERTIES = "epub.xml";
	/** Mime-Types und zugehörige Endungen für Bilder */
	private static final String[][] MIME_TYPES_IMAGE = {
		{ "image/gif",	".gif" }, // GIF-Dateien
		{ "image/jpeg",	".jpeg", ".jpg", ".jpe"}, // JPEG-Dateien
		{ "image/png",	".png" }, // PNG-Dateien
		{ "image/svg+xml", ".svg" } // SVG-Grafiken
	};

	private ZipWriter writer;
	private Book book;
	private FreeMarker freeMarker;

	/**
	 * Startet die Erstellung des Buches. Wird das Programm ohne Parameter aufgerufen,
	 * werden die Quellen im aktuellen Verzeichnis gesucht, der Dateiname des Buchs wird
	 * aus Titel und Author zusammengesetzt.
	 * @param args [Verzeichnis mit Quellen] [Dateiname]
	 * @throws IOException
	 */
	public static void main(String... args) throws IOException {
		new Text2Epub().createEpub(args);
	}

	private void createEpub(String... args) throws IOException {
		File basedir = new File(args.length == 0 ? "." : args[0]);
		if (! IOUtils.exists(basedir, PROPERTIES)) {
			IOUtils.copyCP2FS(PROPERTIES, basedir);
		}
		book = new Book();
		book.readProperties(new File(basedir, PROPERTIES));
		File epub = new File(args.length > 1 ? args[1] : mkFilename(basedir));
		book.setFilename(epub);
		writer = new ZipWriter(epub);

		freeMarker = new FreeMarker(basedir, writer, book);

		// als erster Eintrag muss der Mime-Type angelegt werden (unkomprimiert)
		writer.storeEntry("mimetype", "application/epub+zip");

		// Container-Beschreibung
		freeMarker.writeTemplate("container.xml.ftl", "META-INF/container.xml");

		// CSS
		File css = new File(basedir, book.getStylesheet());
		if (! css.exists()) {
			// Vorlage aus Classpath kopieren
			IOUtils.copyCP2FS(book.getStylesheet(), basedir);
		}
		writeMedia(basedir, book.getStylesheet(), "text/css", "style-sheet");

		// Cover
		writeCover(basedir);

		boolean createToc = Boolean.parseBoolean(book.getProperty().getProperty("toc", "true"));
		if (createToc) {
			book.addContentFile(new FileEntry(TOC, MIMETYPE_XHTML, "toc"));
			book.setParam("TOC", TOC);
		}

		// Markdown-Dateien konvertieren und schreiben
		Set<String> images = new HashSet<>();
		File[] files = basedir.listFiles();
		Arrays.sort(files);
		for (File file : files) {
			String filename = file.getName().toLowerCase();
			if (filename.endsWith(".md")
					|| filename.endsWith(".txt")) {
				writeMarkdown(file, images);
			} else if (filename.endsWith(".xhtml")) {
				writeHtml(file, images);
			}
		}

		// Bilder ausgeben
		writeImages(basedir, images);

		// Inhaltsverzeichnis ausgeben
		freeMarker.writeTemplate("content.ncx.ftl", Book.NCX);
		if (createToc) {
			freeMarker.writeTemplate("toc.xhtml.ftl", TOC);
		}

		// Stammdatei schreiben
		freeMarker.writeTemplate("content.opf.ftl", Book.OPF);

		writer.close();

		echo("MsgSuccess", epub.getName());
	}

	/** Liefert den Dateinamen für das EPUB zurück */
	private String mkFilename(File basedir) {
		// Titel - Author.epub
		String author = book.getProperty("authorFileAs");
		if (isEmpty(author)) {
			author = book.getProperty("author");
		}
		String title = book.getProperty("title");

		String filename;
		// Ggf. Verzeichnis-Name als Fallback
		if (isEmpty(author) || isEmpty(title)) {
			filename = basedir.getName();
		} else {
			filename = title + " - " + author;
		}

		// ggf. Sonderzeichen entfernen
		filename = filename.replaceAll("[/\\:|'\"&><]", "");
		// Whitespaces durch Leerzeichen ersetzen
		filename = filename.replaceAll("\\s\\s*", " ");

		return filename + ".epub";
	}

	private void writeMedia(File basedir, String filename, String mimeType, String id) throws IOException {
		book.addMediaFile(new FileEntry(filename, mimeType, id));
		File file = new File(basedir, filename);
		writer.writeFile(file);
	}

	private void writeCover(File basedir) throws IOException {
		// Cover suchen
		boolean found = false;
		// explizit angegeben?
		String filename = book.getProperty("cover");
		// sonst danach suchen
		if (isEmpty(filename)) {
			outer: for (String[] entry : MIME_TYPES_IMAGE) {
				for (int i = 1; i < entry.length; i++) {
					filename = "cover" + entry[i];
					if (IOUtils.exists(basedir, filename)) {
						found = true;
						break outer;
					}
				}
			}
			if (! found) {
				echo("MsgNoCover");
				return;
			}
		}

		String coverId = writeImage(basedir, filename);

		book.addContentFile(new FileEntry(COVER, MIMETYPE_XHTML, "cover"));
		book.setParam("COVER", COVER);
		book.setParam("COVER_ID", coverId);
		book.setParam("cover_url", filename);
		freeMarker.writeTemplate("cover.xhtml.ftl", COVER);
	}

	private void writeImages(File basedir, Set<String> images) throws IOException {
		for (String image : images) {
			// Bild ausgeben
			writeImage(basedir, image);
		}
	}

	private String writeImage(File basedir, String image) throws IOException {
		// Mime-Type ermitteln
		String mimeType = null;
		String suffix = image.substring(image.lastIndexOf('.'));
		for (String[] s : MIME_TYPES_IMAGE) {
			for (int i = 1; i < s.length; i++) {
				if (suffix.equalsIgnoreCase(s[i])) {
					mimeType = s[0];
				}
			}
		}

		String id = String.format("img-%02d", book.getMediaFiles().size() + 1);
		writeMedia(basedir, image, mimeType, id);

		return id;
	}

	/** XHTML übernehmen */
	private void writeHtml(File file, Set<String> images) throws IOException {
		String outputFilename = file.getName();
		String id = String.format("content-%02d", book.getContentFiles().size() + 1);
		writer.writeFile(file);
		book.addContentFile(new FileEntry(outputFilename, MIMETYPE_XHTML, id));
		// Überschrift suchen
		TocScanner toc = new TocScanner(book, outputFilename);
		toc.scanXml(file);
		// Bilder suchen
		ImageScanner img = new ImageScanner(images);
		img.scanXml(file);

		echo("MsgFileImported", outputFilename);
	}

	/** Markdown nach HTML konvertieren */
	private void writeMarkdown(File file, Set<String> images) throws IOException {
		String outputFilename = file.getName();
		outputFilename = outputFilename.substring(0, outputFilename.lastIndexOf(".md"));
		String id = outputFilename;
		outputFilename += ".xhtml";
		book.addContentFile(new FileEntry(outputFilename, MIMETYPE_XHTML, id));

		// Inhalt
		Configuration config = Configuration.builder().forceExtentedProfile().build();
		String output = Processor.process(file, config);
		book.setParam("content", output);
		freeMarker.writeTemplate("content.xhtml.ftl", outputFilename);
		// Überschrift suchen
		TocScanner toc = new TocScanner(book, outputFilename);
		toc.scanXmlFragment(output);
		// Bilder suchen
		ImageScanner img = new ImageScanner(images);
		img.scanXmlFragment(output);

		echo("MsgFileImported", outputFilename);
	}

	private static boolean isEmpty(String s) {
		return s == null || s.isEmpty();
	}

	private void echo(String key, Object... param) {
		System.out.println(String.format(book.getResource(key), param));
	}
}
