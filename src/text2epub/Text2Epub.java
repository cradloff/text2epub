package text2epub;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.java.textilej.parser.MarkupParser;
import net.java.textilej.parser.builder.HtmlDocumentBuilder;
import net.java.textilej.parser.markup.Dialect;
import net.java.textilej.parser.markup.confluence.ConfluenceDialect;
import net.java.textilej.parser.markup.mediawiki.MediaWikiDialect;
import net.java.textilej.parser.markup.textile.TextileDialect;
import net.java.textilej.parser.markup.trac.TracWikiDialect;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import text2epub.xml.ChainedContentHandler;
import text2epub.xml.CompressingXMLWriter;
import text2epub.xml.IdGeneratorFilter;
import text2epub.xml.XMLWriter;
import text2epub.xml.XmlScanner;

import com.github.rjeschke.txtmark.Configuration;
import com.github.rjeschke.txtmark.Processor;

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
			echo("MsgFileCreated", PROPERTIES);
			return;
		}

		book = new Book();
		book.readProperties(new File(basedir, PROPERTIES));
		File epub = new File(args.length > 1 ? args[1] : mkFilename(basedir));
		book.setFilename(epub);
		writer = new ZipWriter(epub);

		freeMarker = new FreeMarker(basedir, writer, book);

		// als erster Eintrag muss der Mime-Type angelegt werden (unkomprimiert)
		writer.storeEntry("mimetype", MimeTypes.MIMETYPE_EPUB);

		// Container-Beschreibung
		freeMarker.writeTemplate("container.xml.ftlx", "META-INF/container.xml");

		// Stylesheet
		book.addMediaFile(Book.CSS);
		freeMarker.writeTemplate(Book.CSS.getFilename(), Book.CSS.getFilename());

		// Cover
		Set<FileEntry> images = new HashSet<>();
		writeCover(basedir, images);

		boolean createToc = Boolean.parseBoolean(book.getProperty().getProperty("toc", "true"));
		if (createToc) {
			book.addContentFile(TOC);
			book.setParam("TOC", TOC.getFilename());
		}

		// XHTML- und Markdown-Dateien konvertieren und schreiben
		File[] files = basedir.listFiles();
		Arrays.sort(files, new Comparator<File>() {
			@Override
			public int compare(File f1, File f2) {
				return f1.getName().compareToIgnoreCase(f2.getName());
			}
		});
		for (File file : files) {
			String filename = file.getName().toLowerCase();
			if (filename.endsWith(".md")
					|| filename.endsWith(".txt")) {
				writeMarkdown(file, images);
			} else if (filename.endsWith(".xhtml")) {
				writeHtml(file, images);
			} else if (filename.endsWith(".textile")) {
				writeTextile(file, new TextileDialect(), images);
			} else if (filename.endsWith(".wiki")
					|| filename.endsWith(".mediawiki")) {
				writeTextile(file, new MediaWikiDialect(), images);
			} else if (filename.endsWith(".trac")) {
				writeTextile(file, new TracWikiDialect(), images);
			} else if (filename.endsWith(".confluence")) {
				writeTextile(file, new ConfluenceDialect(), images);
			}
		}

		// Bilder ausgeben
		writeImages(basedir, images);
		// sonstige Medien
		writeMedia(basedir);

		// Inhaltsverzeichnis ausgeben
		freeMarker.writeTemplate("content.ncx.ftlx", Book.NCX);
		if (createToc) {
			freeMarker.writeTemplate("toc.xhtml.ftlx", TOC.getFilename());
		}

		// ggf. Page-Map ausgeben
		if (book.getPageEntries() != null) {
			freeMarker.writeTemplate("page-map.xml.ftlx", "page-map.xml");
		}

		// Stammdatei schreiben
		freeMarker.writeTemplate("content.opf.ftlx", Book.OPF);

		writer.close();

		echo("MsgSuccess", epub.getName());
	}

	/** Liefert den Dateinamen für das EPUB zurück */
	private String mkFilename(File basedir) {
		// explizit angegebener Dateiname?
		String filename = book.getProperty("filename");
		if (! isEmpty(filename)) {
			return filename;
		}

		// Titel - Author.epub
		String author = book.getProperty("authorFileAs");
		if (isEmpty(author)) {
			author = book.getProperty("author");
		}
		String title = book.getProperty("title");

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

		// Dateiendung
		if (! filename.endsWith(".")) {
			filename += ".";
		}
		filename += "epub";

		return filename;
	}

	private void writeMedia(File basedir, FileEntry mediaFile) throws IOException {
		book.addMediaFile(mediaFile);
		File file = new File(basedir, mediaFile.getFilename());
		writer.writeFile(file);
	}

	private void writeCover(File basedir, Set<FileEntry> images) throws IOException {
		// Cover suchen
		boolean found = false;
		// explizit angegeben?
		String filename = book.getProperty("cover");
		// sonst danach suchen
		if (isEmpty(filename)) {
			outer: for (String[] entry : MimeTypes.MIME_TYPES_IMAGE) {
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

		FileEntry cover = new FileEntry(filename, MimeTypes.getMimeType(filename), "cover-img");
		images.add(cover);

		book.addContentFile(new FileEntry(COVER, MimeTypes.MIMETYPE_XHTML, "cover"));
		book.setParam("COVER", COVER);
		book.setParam("COVER_ID", cover.getId());
		book.setParam("cover_url", filename);
		freeMarker.writeTemplate("cover.xhtml.ftlx", COVER);
	}

	private void writeImages(File basedir, Set<FileEntry> images) throws IOException {
		// zuerst nach eingebetteten Bildern in SVG-Grafiken suchen
		for (FileEntry image : new ArrayList<FileEntry>(images)) {
			if (MimeTypes.MIME_TYPE_SVG.equals(image.getMimeType())) {
				ImageScanner scanner = new ImageScanner(images);
				XmlScanner.scanXml(new File(basedir, image.getFilename()), scanner);
			}
		}

		// jetzt die Bilder schreiben
		for (FileEntry image : images) {
			// Bild ausgeben
			writeMedia(basedir, image);
		}
	}

	/**
	 * Gibt alle zusätzlichen Medien aus den Properties aus.
	 */
	private void writeMedia(File basedir) throws IOException {
		// weitere Dateien ausgeben
		String additionalMedia = this.book.getProperty("additional-media");
		if (additionalMedia != null && ! "".equals(additionalMedia)) {
			String files[] = additionalMedia.split("\\s*,\\s*");
			int count = 0;
			for (String filename : files) {
				String id = String.format("media-%02d", ++count);
				FileEntry entry = new FileEntry(filename, MimeTypes.getMimeType(filename), id);
				writeMedia(basedir, entry);
			}
		}
	}

	/** XHTML übernehmen */
	private void writeHtml(File file, Set<FileEntry> images) throws IOException {
		String outputFilename = file.getName();
		writeHtml(new InputSource(new FileInputStream(file)), outputFilename, images);
	}

	/** Markdown nach HTML konvertieren */
	private void writeMarkdown(File file, Set<FileEntry> images) throws IOException {
		String outputFilename = replaceSuffix(file, ".xhtml");

		// Inhalt
		Configuration config = Configuration.builder().forceExtentedProfile().build();
		String output = Processor.process(file, config);
		book.setParam("content", output);
		output = freeMarker.applyTemplate("content.xhtml.ftlx");
		writeHtml(new InputSource(new StringReader(output)), outputFilename, images);
	}

	private void writeHtml(InputSource input, String outputFilename, Set<FileEntry> images)
			throws IOException {
		try {
			// TOC-Entries einlesen
			String s = book.getProperty().getProperty("toc-entries", "h1");
			List<String> headings = Arrays.asList(s.split("\\s*,\\s*"));
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
			// Seitenzahlen suchen
			PageEntryScanner pes = new PageEntryScanner(book, outputFilename);
			ContentHandler handler = new ChainedContentHandler(toc, img, pes, cxWriter);
			filter.setContentHandler(handler);
			// Dokument ausgeben
			filter.parse(input);

			String id = String.format("content-%02d", book.getContentFiles().size() + 1);
			book.addContentFile(new FileEntry(outputFilename, MimeTypes.MIMETYPE_XHTML, id));

			echo("MsgFileImported", outputFilename);
		} catch (SAXException e) {
			echo("MsgExceptionImport", outputFilename, e.getLocalizedMessage());
			throw new RuntimeException(e);
		}
	}

	/** mit Textile-J nach HTML konvertieren */
	private void writeTextile(File file, Dialect dialect, Set<FileEntry> images) throws IOException {
		String outputFilename = replaceSuffix(file, ".xhtml");

		// Inhalt
		String content = IOUtils.read(file);
		StringWriter out = new StringWriter();
		MarkupParser parser = new MarkupParser(dialect, new HtmlDocumentBuilder(out));
		parser.parse(content, false);
		String output = out.toString();
		book.setParam("content", output);
		output = freeMarker.applyTemplate("content.xhtml.ftlx");
		writeHtml(new InputSource(new StringReader(output)), outputFilename, images);
	}

	/** Ersetzt die Dateiendung durch die angegebene Endung */
	private static String replaceSuffix(File file, String newSuffix) {
		return replaceSuffix(file.getName(), newSuffix);
	}

	/** Ersetzt die Dateiendung durch die angegebene Endung */
	private static String replaceSuffix(String filename, String newSuffix) {
		String outputFilename = filename.substring(0, filename.lastIndexOf("."));
		outputFilename += newSuffix;

		return outputFilename;
	}

	private static boolean isEmpty(String s) {
		return s == null || s.isEmpty();
	}

	private void echo(String key, Object... param) {
		System.out.println(String.format(book.getResource(key), param));
	}
}
