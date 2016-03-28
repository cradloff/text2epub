package org.markdown2epub;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.github.rjeschke.txtmark.Configuration;
import com.github.rjeschke.txtmark.Processor;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/**
 * Konvertiert Markdown-Dateien nach Epub.
 *
 * @author Claus Radloff
 */
public class Markdown2Epub {
	private static final String MIMETYPE_XHTML = "application/xhtml+xml";
	/** Zeichensatz */
	private static final String ENCODING = "UTF-8";
	private static final String OPF = "content.opf";
	private static final String COVER = "cover.xhtml";
	private static final String COVER_ID = "cover-image";
	private static final String NCX = "content.ncx";
	private static final String TOC = "toc.xhtml";
	private static final String CSS = "book.css";
	private static final String PROPERTIES = "epub.xml";
	/** Mime-Types und zugehörige Endungen für Bilder */
	private static final String[][] MIME_TYPES_IMAGE = {
		{ "image/gif",	".gif" }, // GIF-Dateien
		{ "image/jpeg",	".jpeg", ".jpg", ".jpe"}, // JPEG-Dateien
		{ "image/png",	".png" } // PNG-Dateien
	};

	private ZipOutputStream zip;
	private PrintWriter out;
	private Book book;
	/** FreeMarker-Konfiguration */
	private freemarker.template.Configuration fmCfg;

	public static void main(String... args) throws IOException {
		new Markdown2Epub().createEpub(args);
	}

	private void createEpub(String... args) throws IOException {
		File basedir = new File(args[0]);
		if (! exists(basedir, PROPERTIES)) {
			copyCP2FS(PROPERTIES, basedir);
		}
		book = new Book();
		book.readProperties(new File(basedir, PROPERTIES));
		File epub = new File(mkFilename(basedir));
		book.setFilename(epub);
		zip = new ZipOutputStream(new FileOutputStream(epub));
		zip.setLevel(9);
		out = new PrintWriter(new OutputStreamWriter(zip, ENCODING), false);

		fmCfg = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_22);
		// Templates werden zuerst im Basis-Verzeichnis gesucht, dann im Classpath
		FileTemplateLoader ftl1 = new FileTemplateLoader(basedir);
		ClassTemplateLoader ctl = new ClassTemplateLoader(getClass(), "/");
		MultiTemplateLoader mtl = new MultiTemplateLoader(new TemplateLoader[] { ftl1, ctl });
		fmCfg.setTemplateLoader(mtl);
		fmCfg.setDefaultEncoding(ENCODING);
		fmCfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

		// als erster Eintrag muss der Mime-Type angelegt werden
		writeMimetype();

		// Container-Beschreibung
		writeContainer();

		// CSS
		File css = new File(basedir, book.getStylesheet());
		if (! css.exists()) {
			// Vorlage aus Classpath kopieren
			copyCP2FS(book.getStylesheet(), basedir);
		}
		writeMedia(basedir, book.getStylesheet(), "text/css", "style-sheet");

		// Cover
		writeCover(basedir);

		book.addContentFile(new FileEntry(TOC, MIMETYPE_XHTML, "toc"));

		// Markdown-Dateien konvertieren und schreiben
		Set<String> images = new HashSet<>();
		File[] files = basedir.listFiles();
		Arrays.sort(files);
		for (File file : files) {
			if (file.getName().endsWith(".md")) {
				convert(file, images);
			} else if (file.getName().endsWith(".xhtml")) {
				writeHtml(file, images);
			}
		}

		// Bilder ausgeben
		writeImages(basedir, images);

		// Inhaltsverzeichnis ausgeben
		writeTemplate("content.ncx.ftl", NCX);
		writeTemplate("toc.xhtml.ftl", TOC);

		// Stammdatei schreiben
		writeTemplate("content.opf.ftl", OPF);

		zip.close();

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
		writeFile(file);
	}

	/** Schreibt eine Datei aus dem Dateisystem in die Zip-Datei */
	private void writeFile(File file) throws IOException {
		zip.putNextEntry(new ZipEntry(file.getName()));
		try (FileInputStream fis = new FileInputStream(file)) {
			copy(fis, zip);
		}
		zip.closeEntry();
	}

	/** Gibt eine Resource aus dem Classpath in die Zip-Datei aus */
	private void writeResource(String resource, String path) throws IOException {
		zip.putNextEntry(new ZipEntry(path));
		try (InputStream is = getClass().getResourceAsStream("/" + resource);) {
			copy(is, zip);
		}
		zip.closeEntry();
	}

	/**
	 * Kopiert die Datei aus dem Classpath ins angegebene Verzeichnis.
	 */
	private void copyCP2FS(String filename, File basedir) throws IOException {
		try (InputStream is = getClass().getResourceAsStream("/" + filename);
			OutputStream os = new FileOutputStream(new File(basedir, filename));) {
			copy(is, os);
		}
	}

	/**
	 * Kopiert die Daten aus dem Input-Stream auf den Output-Stream.
	 */
	private void copy(InputStream is, OutputStream os) throws IOException {
		byte[] buf = new byte[1024];
		int len;
		while ((len = is.read(buf)) > 0) {
			os.write(buf, 0, len);
		}
	}

	private void writeMimetype() throws IOException {
		// die Datei darf nicht komprimiert werden
		ZipEntry entry = new ZipEntry("mimetype");
		entry.setMethod(ZipEntry.STORED);
		// dummerweise muss die Größe und CRC-32 jetzt explizit gesetzt werden:
		String mimetype = "application/epub+zip";
		entry.setCompressedSize(mimetype.length());
		CRC32 crc = new CRC32();
		crc.update(mimetype.getBytes());
		entry.setCrc(crc.getValue());
		zip.putNextEntry(entry);
		out.print(mimetype);
		out.flush();
		zip.closeEntry();
	}

	private void writeContainer() throws IOException {
		writeResource("container.xml", "META-INF/container.xml");
	}

	private void writeCover(File basedir) throws IOException {
		// Cover suchen
		boolean found = false;
		String mimeType = null;
		String filename = null;
		outer: for (String[] entry : MIME_TYPES_IMAGE) {
			mimeType = entry[0];
			for (int i = 1; i < entry.length; i++) {
				filename = "cover" + entry[i];
				if (exists(basedir, filename)) {
					found = true;
					break outer;
				}
			}
		}
		if (! found) {
			echo("MsgNoCover");
			return;
		}

		writeMedia(basedir, filename, mimeType, COVER_ID);

		book.addContentFile(new FileEntry(COVER, MIMETYPE_XHTML, "cover"));
		book.getParams().put("cover_url", filename);
		writeTemplate("cover.xhtml.ftl", COVER);
	}

	/** Schreibt ein FreeMarker-Template in die Zip-Datei */
	private void writeTemplate(String templateName, String path) throws IOException {
		zip.putNextEntry(new ZipEntry(path));
		Template template = fmCfg.getTemplate(templateName);
		try {
			template.process(book, out);
		} catch (TemplateException e) {
			throw new RuntimeException(e);
		}
		zip.closeEntry();
	}

	private void writeImages(File basedir, Set<String> images) throws IOException {
		for (String image : images) {
			// Mime-Type ermitteln
			String mimeType = null;
			String suffix = image.substring(image.lastIndexOf('.'));
			for (String[] s : MIME_TYPES_IMAGE) {
				for (int i = 1; i < s.length; i++) {
					if (suffix.equals(s[i])) {
						mimeType = s[0];
					}
				}
			}
			// Bild ausgeben
			String id = image.substring(0, image.lastIndexOf('.'));
			writeMedia(basedir, image, mimeType, id);
		}
	}

	/** XHTML übernehmen */
	private void writeHtml(File file, Set<String> images) throws IOException {
		String outputFilename = file.getName();
		String id = outputFilename.substring(0, outputFilename.lastIndexOf(".xhtml"));
		writeFile(file);
		book.addContentFile(new FileEntry(outputFilename, MIMETYPE_XHTML, id));
		// Überschrift suchen
		scanTocEntries(outputFilename, file);
		// Bilder suchen
		scanImages(images, file);

		echo("MsgFileImported", outputFilename);
	}

	/** Markdown nach HTML konvertieren */
	private void convert(File file, Set<String> images) throws IOException {
		String outputFilename = file.getName();
		outputFilename = outputFilename.substring(0, outputFilename.lastIndexOf(".md"));
		String id = outputFilename;
		outputFilename += ".xhtml";
		book.addContentFile(new FileEntry(outputFilename, MIMETYPE_XHTML, id));

		// Inhalt
		Configuration config = Configuration.builder().forceExtentedProfile().build();
		String output = Processor.process(file, config);
		book.getParams().put("content", output);
		writeTemplate("content.xhtml.ftl", outputFilename);
		// Überschrift suchen
		scanTocEntries(outputFilename, output);
		// Bilder suchen
		scanImages(images, output);

		echo("MsgFileImported", outputFilename);
	}

	/** Sucht nach Überschriften für das Inhaltsverzeichnis */
	private void scanTocEntries(String outputFilename, String content) {
		// sucht nach <h1>xxx</h1> und <h1 id="...">xxx</h1>
		Matcher matcher = Pattern.compile("<h1(\\s+id=[\"']([^>]*)[\"'])?>([^<]*)</h1>").matcher(content);
		while (matcher.find()) {
			String id = matcher.group(2);
			String link = outputFilename;
			if (! isEmpty(id)) {
				link += "#" + id;
			}
			book.addTocEntry(new TocEntry(matcher.group(3), link));
		}
	}

	/** Sucht nach Überschriften für das Inhaltsverzeichnis */
	private void scanTocEntries(final String outputFilename, File file) {
		DefaultHandler handler = new DefaultHandler() {
			private boolean scan = false;
			private String id;
			private StringBuilder sb = new StringBuilder();

			@Override
			public void startElement(String uri, String localName,
					String qName, Attributes attributes) throws SAXException {
				if ("h1".equals(qName)) {
					scan = true;
					id = attributes.getValue("id");
					sb.setLength(0);
				}
			}

			@Override
			public void endElement(String uri, String localName, String qName)
					throws SAXException {
				if ("h1".equals(qName)) {
					scan = false;
					String link = outputFilename;
					if (id != null) {
						link += "#" + id;
					}
					book.addTocEntry(new TocEntry(sb.toString().trim(), link));
				}
			}

			@Override
			public void characters(char[] ch, int start, int length)
					throws SAXException {
				if (scan) {
					sb.append(ch, start, length);
				}
			}

		};
		parseXml(file, handler);
	}

	/** Sucht nach referenzierten Bildern */
	private void scanImages(Set<String> images, String content) {
		// sucht nach <img ...src="xxx".../>
		Matcher matcher = Pattern.compile("<img [^>]*src=[\"']([^\"']*)[\"'][^>]*>").matcher(content);
		while (matcher.find()) {
			images.add(matcher.group(1));
		}
	}

	/** Sucht nach referenzierten Bildern */
	private void scanImages(final Set<String> images, File file) {
		DefaultHandler handler = new DefaultHandler() {
			@Override
			public void startElement(String uri, String localName,
					String qName, Attributes attributes) throws SAXException {
				if ("img".equals(qName)) {
					images.add(attributes.getValue("src"));
				}
			}
		};
		parseXml(file, handler);
	}

	private void parseXml(File file, DefaultHandler handler) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			SAXParser parser = factory.newSAXParser();
			parser.parse(file, handler);
		} catch (IOException | SAXException | ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	private boolean exists(File basedir, String filename) {
		File f = new File(basedir, filename);
		return f.exists();
	}

	private static boolean isEmpty(String s) {
		return s == null || s.isEmpty();
	}

	private void echo(String key, Object... param) {
		System.out.println(String.format(book.getResource(key), param));
	}
}
