package text2epub;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;

import com.adobe.epubcheck.api.EpubCheck;

import text2epub.converter.*;
import text2epub.xml.*;

/**
 * Konvertiert Text-Dateien nach Epub.
 *
 * @author Claus Radloff
 */
public class Text2Epub {
	private static final FileEntry TOC = new FileEntry("toc.xhtml", MimeTypes.MIMETYPE_XHTML, "toc");
	private static final String COVER = "cover.xhtml";
	private static final String PROPERTIES = "epub.xml";

	private ZipWriter writer;
	private Book book;
	private FreeMarker freeMarker;
	private File basedir;
 
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

	public boolean createEpub(String... args) throws IOException {
		basedir = new File(args.length == 0 ? "." : args[0]);
		basedir = basedir.getCanonicalFile();
		book = new Book();
		if (! IOUtils.exists(basedir, PROPERTIES)) {
			createProperties();
			return false;
		}

		book.readProperties(new File(basedir, PROPERTIES));
		Locale locale = Locale.forLanguageTag(book.getProperty("language"));
		book.initResources(locale);
		File epub = new File(args.length > 1 ? args[1] : mkFilename());
		book.setFilename(epub);
		writer = new ZipWriter(epub);

		freeMarker = new FreeMarker(writer, book);
		freeMarker.configure(basedir);

		// als erster Eintrag muss der Mime-Type angelegt werden (unkomprimiert)
		writer.storeEntry("mimetype", MimeTypes.MIMETYPE_EPUB);

		// Container-Beschreibung
		freeMarker.writeTemplate("container.xml.ftlx", "META-INF/container.xml");

		// Stylesheet
		book.addMediaFile(Book.CSS);
		freeMarker.writeTemplate(Book.CSS.getFilename(), Book.CSS.getFilename());

		// Cover
		Set<FileEntry> images = new HashSet<>();
		writeCover(images);

		boolean createToc = Boolean.parseBoolean(book.getProperty().getProperty("toc", "true"));
		TOC.setProperty("nav");
		book.setParam("TOC", TOC.getFilename());
		if (createToc) {
			book.addContentFile(TOC);
		} else {
			book.addMediaFile(TOC);
		}

		// Converter registrieren
		Map<String, Converter> converters = new HashMap<>();
		MarkdownConverter.register(converters);
		XHtmlConverter.register(converters);
		TextileConverter.register(converters);
		AsciiDocConverter.register(converters);

		// Liste der Dateien erstellen
		File[] files = basedir.listFiles();
		Arrays.sort(files, (f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName()));
		int firstEntry = book.getContentFiles().size();
		List<Content> contents = new ArrayList<>();
		for (File file : files) {
			String suffix = IOUtils.suffix(file);
			Converter converter = converters.get(suffix);
			if (converter != null) {
				String outputFilename = IOUtils.buildOutputFilename(file);
				contents.add(new Content(file, outputFilename, converter));
				String id = String.format("content-%02d", book.getContentFiles().size() + 1);
				book.addContentFile(new FileEntry(file.getName(), outputFilename, MimeTypes.MIMETYPE_XHTML, id));
			}
		}

		// Dateien ausgeben
		for (Content content : contents) {
			writeContent(content, images);
		}

		// Bilder ausgeben
		writeImages(images);
		// sonstige Medien
		writeMedia();

		// Inhaltsverzeichnis ausgeben
		// ggf. default Eintrag anlegen
		if (book.getTocEntries().isEmpty()) {
			FileEntry first = book.getContentFiles().get(firstEntry);
			TocEntry entry = new TocEntry("h1", book.getResource("content"), first.getFilename());
			book.addTocEntry(entry);
		}
		freeMarker.writeTemplate("content.ncx.ftlx", Book.NCX);
		freeMarker.writeTemplate("toc.xhtml.ftlx", TOC.getFilename());

		// Stammdatei schreiben
		freeMarker.writeTemplate("content.opf.ftlx", Book.OPF);

		writer.close();

		EpubCheck check = new EpubCheck(epub);
		if (check.doValidate() == 0) {
			echo("MsgSuccess", epub.getName());
			return true;
		}
		
		echo("MsgWarnings", epub.getName());
		return false;
	}

	private void createProperties() throws IOException {
		Locale locale = Locale.getDefault();
		book.initResources(locale);

		Properties props = MetaDataScanner.parseDirectoryName(basedir.getName());
		props.setProperty("UUID", UUID.randomUUID().toString());
		props.setProperty("language", locale.getLanguage());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		props.setProperty("date", sdf.format(new Date()));
		book.setProperties(props);

		freeMarker = new FreeMarker(null, book);
		freeMarker.configure(basedir);
		String properties = freeMarker.applyTemplate("epub.xml.ftlx");
		IOUtils.write(properties, new File(basedir, PROPERTIES));

		echo("MsgFileCreated", PROPERTIES);
	}

	/** Liefert den Dateinamen für das EPUB zurück */
	private String mkFilename() {
		// explizit angegebener Dateiname?
		String filename = book.getProperty("filename");
		if (! StringUtils.isEmpty(filename)) {
			return filename;
		}

		// Titel - Author.epub
		String author = book.getProperty("authorFileAs");
		if (StringUtils.isEmpty(author)) {
			author = book.getProperty("author");
		}
		String title = book.getProperty("title");

		// Ggf. Verzeichnis-Name als Fallback
		if (StringUtils.isEmpty(author) || StringUtils.isEmpty(title)) {
			filename = basedir.getName();
		} else {
			filename = title + " - " + author;
		}

		// ggf. Sonderzeichen entfernen
		filename = filename.replaceAll("[/\\:|'\"&><]", "");
		// Whitespaces durch Leerzeichen ersetzen
		filename = filename.replaceAll("\\s\\s*", " ");

		// Dateiendung
		if (! filename.endsWith(".")) {
			filename += ".";
		}
		filename += "epub";

		return filename;
	}

	private void writeCover(Set<FileEntry> images) throws IOException {
		// Cover suchen
		boolean found = false;
		// explizit angegeben?
		String filename = book.getProperty("cover");
		// sonst danach suchen
		if (StringUtils.isEmpty(filename)) {
			outer: for (String[] entry : MimeTypes.MIME_TYPES_IMAGE) {
				for (int i = 1; i < entry.length; i++) {
					filename = "cover" + entry[i];
					if (IOUtils.exists(basedir, filename)) {
						found = true;
						break outer;
					}
				}
			}
			if (found) {
				echo("MsgFileImported", filename);
			} else {
				echo("MsgNoCover");
				return;
			}
		}

		String outputFilename = IOUtils.normalize(filename);
		FileEntry cover = new FileEntry(filename, outputFilename, MimeTypes.getMimeType(filename), "cover-img");
		cover.setProperty("cover-image");
		images.add(cover);

		FileEntry entry = new FileEntry(COVER, MimeTypes.MIMETYPE_XHTML, "cover");
		book.addContentFile(entry);
		book.setParam("COVER", COVER);
		book.setParam("COVER_ID", cover.getId());
		book.setParam("cover_url", outputFilename);
		freeMarker.writeTemplate("cover.xhtml.ftlx", COVER);
	}

	private void writeImages(Set<FileEntry> images) throws IOException {
		// zuerst nach eingebetteten Bildern in SVG-Grafiken suchen
		for (FileEntry image : new ArrayList<>(images)) {
			if (MimeTypes.MIME_TYPE_SVG.equals(image.getMimeType())) {
				ImageScanner scanner = new ImageScanner(images);
				XmlScanner.scanXml(new File(basedir, image.getFilename()), new Filter2HandlerAdapter(scanner));
			}
		}

		// jetzt die Bilder schreiben
		for (FileEntry image : images) {
			// Bild ausgeben
			writeMedia(image);
		}
	}

	/**
	 * Gibt alle zusätzlichen Medien aus den Properties aus.
	 */
	private void writeMedia() throws IOException {
		// weitere Dateien ausgeben
		String additionalMedia = this.book.getProperty("additional-media");
		if (! StringUtils.isEmpty(additionalMedia)) {
			List<String> files = StringUtils.splitCSV(additionalMedia);
			int count = 0;
			for (String filename : files) {
				String id = String.format("media-%02d", ++count);
				FileEntry entry = new FileEntry(filename, filename, MimeTypes.getMimeType(filename), id);
				writeMedia(entry);
			}
		}
	}

	private void writeMedia(FileEntry mediaFile) throws IOException {
		book.addMediaFile(mediaFile);
		File file = new File(basedir, mediaFile.getSrcFilename());
		writer.writeFile(file, mediaFile.getFilename());
	}

	private void writeContent(Content entry, Set<FileEntry> images) throws IOException {
		// Inhalt einlesen
		String content = readContent(entry.getFile());

		// nach Html konvertieren
		String output = entry.getConverter().convert(content);

		// handelt es sich um ein XHtml-Fragment?
		if (entry.getConverter().isFragment()) {
			// gibt es ein eigenes Stylesheet für die Datei?
			String stylesheet = IOUtils.replaceSuffix(entry.getFile(), ".css");
			if (IOUtils.exists(basedir, stylesheet)) {
				writeMedia(new FileEntry(stylesheet, MimeTypes.MIME_TYPE_CSS, stylesheet));
				book.setStylesheet(stylesheet);
			}

			// HTML-Datei erzeugen
			book.setParam("content", output);
			output = freeMarker.applyTemplate("content.xhtml.ftlx");
			book.setStylesheet(null);
		}

		writeHtml(output, entry.getOutputFilename(), images);
	}

	private void writeHtml(String content, String outputFilename, Set<FileEntry> images)
			throws IOException {
		String text = content;
		try {
			// TOC-Entries einlesen
			String s = book.getProperty().getProperty("toc-entries", "h1, h2");
			if (s.isBlank()) {
				s = "h1, h2";
			}
			List<String> headings = StringUtils.splitCSV(s);
			writer.newEntry(outputFilename);
			XMLFilter filter = new IdGeneratorFilter(headings);
			XMLReader reader = XMLReaderFactory.createXMLReader();
			reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			filter.setParent(reader);
			XMLWriter cxWriter = new CompressingXMLWriter();
			cxWriter.setOutput(writer);
			// Überschriften suchen
			TocScanner toc = new TocScanner(book, outputFilename, headings);
			// Bilder suchen
			ImageScanner img = new ImageScanner(images);
			img.setParent(filter);
			// Seitenzahlen suchen
			PageEntryScanner pes = new PageEntryScanner(book, outputFilename);
			ContentHandler handler = new ChainedContentHandler(toc, pes, cxWriter);
			img.setContentHandler(handler);
			// Dokument ausgeben
			text = NamedEntitesConverter.instance().convert(content);
			img.parse(new InputSource(new StringReader(text)));

			echo("MsgFileImported", outputFilename);
		} catch (SAXParseException e) {
			echo("MsgExceptionImport", String.format("%s (%d,%d)", outputFilename, e.getLineNumber(), e.getColumnNumber()),
					e.getLocalizedMessage());
			dump(text, outputFilename);
			throw new RuntimeException(e);
		} catch (SAXException e) {
			echo("MsgExceptionImport", outputFilename, e.getLocalizedMessage());
			dump(text, outputFilename);
			throw new RuntimeException(e);
		}
	}

	private void dump(String text, String outputFilename) throws IOException {
		try (FileWriter fw = new FileWriter(outputFilename)) {
			fw.write(text);
		}
		echo("MsgFileDumped", outputFilename);
	}

	private String readContent(File file) throws IOException {
		String content;
		if (Boolean.parseBoolean(book.getProperty().getProperty("freemarker", "true"))) {
			content = freeMarker.applyTemplate(file);
		} else {
			content = IOUtils.read(file);
		}

		return content;
	}

	private void echo(String key, Object... param) {
		System.out.println(String.format(book.getResource(key), param));
	}
}
