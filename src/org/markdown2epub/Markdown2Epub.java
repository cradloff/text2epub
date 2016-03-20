package org.markdown2epub;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.github.rjeschke.txtmark.Configuration;
import com.github.rjeschke.txtmark.Processor;

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
	private List<FileEntry> contentFiles = new ArrayList<>();
	/** Medien (Dateiname / Mime-Type) */
	private List<FileEntry> mediaFiles = new ArrayList<>();
	/** Einträge für Inhaltsverzeichnis (Link / Titel) */
	private Map<String, String> tocEntries = new LinkedHashMap<>();
	private Properties props;
	private String uuid;
	private ResourceBundle res;

	public static void main(String... args) throws IOException {
		new Markdown2Epub().createEpub(args);
	}

	private void createEpub(String... args) throws IOException {
		File basedir = new File(args[0]);
		props = new Properties();
		if (! exists(basedir, PROPERTIES)) {
			copy(PROPERTIES, basedir);
		}
		try (InputStream pin = new FileInputStream(new File(basedir, PROPERTIES))) {
			props.loadFromXML(pin);
		}
		res = ResourceBundle.getBundle("Markdown2Epub", Locale.forLanguageTag(props.getProperty("language")));
		File epub = new File(basedir, mkFilename(basedir));
		zip = new ZipOutputStream(new FileOutputStream(epub));
		zip.setLevel(9);
		out = new PrintWriter(new OutputStreamWriter(zip, ENCODING), false);
		// Die folgende Buchidentifikation ist einzigartig für jedes Buch zu wählen
		uuid = props.getProperty("UUID");
		if (uuid == null || uuid.isEmpty()) {
			uuid = "UUID-gen" + epub.getName().hashCode();
		}

		// als erster Eintrag muss der Mime-Type angelegt werden
		writeMimetype();

		// Container-Beschreibung
		writeContainer();

		// CSS
		writeFile(basedir, CSS, "text/css", "style-sheet");

		// Cover
		writeCover(basedir);

		contentFiles.add(new FileEntry(TOC, MIMETYPE_XHTML, "toc"));

		// Markdown-Dateien konvertieren und schreiben
		Set<String> images = new HashSet<>();
		File[] files = basedir.listFiles();
		Arrays.sort(files);
		for (File file : files) {
			if (file.getName().endsWith(".md")) {
				convert(file, images);
			}
		}

		// Bilder ausgeben
		writeImages(basedir, images);

		// Inhaltsverzeichnis ausgeben
		writeNCX();
		writeTOC();

		// Stammdatei schreiben
		writeContent();

		zip.close();

		echo("MsgSuccess", epub.getName());
	}

	/** Liefert den Dateinamen für das EPUB zurück */
	private String mkFilename(File basedir) {
		// Titel - Author.epub
		String author = props.getProperty("authorFileAs");
		if (isEmpty(author)) {
			author = props.getProperty("author");
		}
		String title = props.getProperty("title");

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

	private void writeFile(File basedir, String filename, String mimeType, String id) throws IOException {
		File file = new File(basedir, filename);
		if (! file.exists()) {
			// Vorlage aus Classpath kopieren
			copy(CSS, basedir);
		}

		mediaFiles.add(new FileEntry(filename, mimeType, id));
		zip.putNextEntry(new ZipEntry(filename));
		try (FileInputStream fis = new FileInputStream(file)) {
			copy(fis, zip);
		}
		zip.closeEntry();
	}

	/**
	 * Kopiert die Datei aus dem Classpath ins angegebene Verzeichnis.
	 */
	private void copy(String filename, File basedir) throws IOException {
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
		// dummerweise muss die Größe und CRC-32 jetzt explizit gesetzt werden (aus vorhandener Zip-Datei übernommen):
		entry.setCompressedSize(20);
		entry.setCrc(0x2cab616f);
		zip.putNextEntry(entry);
		out.print("application/epub+zip");
		out.flush();
		zip.closeEntry();
	}

	private void writeContainer() throws IOException {
		zip.putNextEntry(new ZipEntry("META-INF/container.xml"));
		out.printf("<?xml version='1.0' encoding='%s'?>%n", ENCODING);
		out.println("<container xmlns='urn:oasis:names:tc:opendocument:xmlns:container' version='1.0'>");
		out.println("  <rootfiles>");
		out.println("    <rootfile full-path='content.opf' media-type='application/oebps-package+xml'/>");
		out.println("  </rootfiles>");
		out.println("</container>");
		out.flush();
		zip.closeEntry();
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

		writeFile(basedir, filename, mimeType, COVER_ID);

		zip.putNextEntry(new ZipEntry(COVER));
		contentFiles.add(0, new FileEntry(COVER, MIMETYPE_XHTML, "cover"));
		out.printf("<?xml version='1.0' encoding='%s'?>%n", ENCODING);
		out.println("<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.1//EN' 'http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd'>");
		out.println("<html xmlns='http://www.w3.org/1999/xhtml'>");
		out.println("<head>");
		out.printf("<title>%s</title>%n", res.getString("cover"));
		out.printf("<link href='%s' type='text/css' rel='stylesheet'/>%n", CSS);
		out.println("</head>");
		out.println("<body>");
		out.printf("  <img id='cover' src='%s' alt='Cover %s'/>%n", filename, props.getProperty("title"));
		out.println("</body>");
		out.println("</html>");
		out.flush();
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
			writeFile(basedir, image, mimeType, id);
			mediaFiles.add(new FileEntry(image, mimeType, id));
		}
	}

	private void convert(File file, Set<String> images) throws IOException {
		String outputFilename = file.getName();
		outputFilename = outputFilename.substring(0, outputFilename.lastIndexOf(".md"));
		String id = outputFilename;
		outputFilename += ".xhtml";
		zip.putNextEntry(new ZipEntry(outputFilename));
		contentFiles.add(new FileEntry(outputFilename, MIMETYPE_XHTML, id));
		// Header ausgeben
		out.printf("<?xml version='1.0' encoding='%s'?>%n", ENCODING);
		out.println("<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.1//EN' 'http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd'>");
		out.println("<html xmlns='http://www.w3.org/1999/xhtml'>");
		out.println("<head>");
		out.printf("<title>%s</title>%n", props.getProperty("title"));
		out.printf("<link href='%s' type='text/css' rel='stylesheet'/>%n", CSS);
		out.println("</head>");
		out.println("<body>");

		// Inhalt
		Configuration config = Configuration.builder().forceExtentedProfile().build();
		String output = Processor.process(file, config);
		out.write(output);
		// Überschrift suchen
		Matcher matcher = Pattern.compile("<h1>([^<]*)</h1>").matcher(output);
		if (matcher.find()) {
			tocEntries.put(outputFilename, matcher.group(1));
		}
		// Bilder suchen
		matcher = Pattern.compile("<img [^>]*src=[\"']([^\"']*)[\"'][^>]*>").matcher(output);
		while (matcher.find()) {
			images.add(matcher.group(1));
		}

		// Footer
		out.println("</body>");
		out.println("</html>");
		out.flush();
		zip.closeEntry();

		echo("MsgFileImported", outputFilename);
	}

	private void writeNCX() throws IOException {
		zip.putNextEntry(new ZipEntry(NCX));
		out.printf("<?xml version='1.0' encoding='%s'?>%n", ENCODING);
		out.println("  <ncx xmlns='http://www.daisy.org/z3986/2005/ncx/' version='2005-1'");
		out.printf("    xml:lang='%s'>%n", props.getProperty("language"));
		out.println("    <head>");
		// Dieselbe Buchidentifikation wie in der OPF-Datei:
		out.printf("      <meta name='dtb:uid' content='%s'/>%n", uuid);
		out.println("    </head>");
		out.println("    <docTitle>");
		out.printf("      <text>%s</text>%n", props.getProperty("title"));
		out.println("    </docTitle>");
		out.println("    <docAuthor>");
		out.printf("      <text>%s</text>%n", props.getProperty("author"));
		out.println("    </docAuthor>");
		int index = 1;
		out.println("    <navMap>");
		// Cover und Index-Seite auf oberster Ebene
		out.printf("      <navPoint playOrder='%1$d' id='navPoint-%1$d'>%n", index++);
		out.println("        <navLabel>");
		out.printf("          <text>%s</text>%n", res.getString("cover"));
		out.println("        </navLabel>");
		out.printf("        <content src='%s'/>%n", COVER);
		out.println("      </navPoint>");
		out.printf("      <navPoint playOrder='%1$d' id='navPoint-%1$d'>%n", index++);
		out.println("        <navLabel>");
		out.printf("          <text>%s</text>%n", props.getProperty("title"));
		out.println("        </navLabel>");
		out.printf("        <content src='%s'/>%n", TOC);
		// untergeordnet die Kapitel
		for (Entry<String, String> entry : tocEntries.entrySet()) {
			out.printf("        <navPoint playOrder='%1$d' id='navPoint-%1$d'>%n", index++);
			out.println("          <navLabel>");
			out.printf("            <text>%s</text>%n", entry.getValue());
			out.println("          </navLabel>");
			out.printf("          <content src='%s'/>%n", entry.getKey());
			out.println("        </navPoint>");
		}
		out.println("      </navPoint>");
		out.println("    </navMap>");
		out.println("  </ncx>");
		out.flush();
		zip.closeEntry();
	}

	private void writeTOC() throws IOException {
		zip.putNextEntry(new ZipEntry(TOC));
		out.printf("<?xml version='1.0' encoding='%s'?>%n", ENCODING);
		out.println("<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.1//EN'");
		out.println("     'http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd'>");
		out.println("<html xmlns='http://www.w3.org/1999/xhtml'");
		out.printf("      xml:lang='%s'>%n", props.getProperty("language"));
		out.println("  <head>");
		out.printf("    <title>%s</title>%n", props.getProperty("title"));
		out.printf("    <link href='%s' type='text/css' rel='stylesheet'/>%n", CSS);
		out.println("  </head>");
		out.println("  <body class='toc'>");
		out.printf("    <div id='author'>%s</div>%n", props.getProperty("author"));
		out.printf("    <div id='title'>%s</div>%n", props.getProperty("title"));
		String subtitle = props.getProperty("subtitle");
		if (! isEmpty(subtitle)) {
			out.printf("    <div id='subtitle'>%s</div>%n", subtitle);
		}
		out.printf("    <h2>%s</h2>%n", res.getString("toc"));
		out.println("    <ul>");
		for (Entry<String, String> entry : tocEntries.entrySet()) {
			out.printf("      <li><a href='%s'>%s</a></li>%n", entry.getKey(), entry.getValue());
		}
		out.println("    </ul>");
		out.println("  </body>");
		out.println("</html>");
		out.flush();
		zip.closeEntry();
	}

	private void writeContent() throws IOException {
		zip.putNextEntry(new ZipEntry(OPF));
		out.printf("<?xml version='1.0' encoding='%s'?>%n", ENCODING);
		out.println("  <package version='2.0'");
		out.println("    xmlns:dc='http://purl.org/dc/elements/1.1/'");
		out.println("    xmlns:opf='http://www.idpf.org/2007/opf'");
		out.println("    xmlns='http://www.idpf.org/2007/opf'");
		out.println("    unique-identifier='BookId'>");
		out.println("    <metadata>");
		out.printf("      <dc:identifier id='BookId' opf:scheme='UUID'>%s</dc:identifier>%n", uuid);
		// Hauptsprache des Buches
		String language = props.getProperty("language");
		out.printf("      <dc:language>%s</dc:language>%n", language);
		// Buchtitel
		out.printf("      <dc:title xml:lang='%s'>%s</dc:title>%n", language, props.getProperty("title"));
		// bis hier notwendige Metainformationen, es folgen einige optionale:
		// Beschreibung
		String description = props.getProperty("description");
		if (! isEmpty(description)) {
			out.printf("      <dc:description xml:lang='%s'>%s</dc:description>%n", language, description);
		}
		// Erzeuger, Erschaffer des digitalen Buches, hier auch der Autor
		String author = props.getProperty("author");
		if (! isEmpty(author)) {
			out.println("      <dc:creator");
			out.printf("        opf:file-as='%s' opf:role='aut'%n", nvl(props.getProperty("authorFileAs"), author));
			out.printf("        xml:lang='%s'>%s</dc:creator>%n", language, author);
		}
		// Cover
		out.printf("      <meta name='cover' content='%s'/>%n", COVER_ID);
		// Charakteristischer Zeitpunkt der Erstellung des Buches
		out.printf("      <dc:date opf:event='creation'>%tF</dc:date>%n", new Date());
		// Zeitpunkt der Veröffentlichung
		String pubDate = props.getProperty("pubDate");
		if (! isEmpty(pubDate)) {
			out.printf("      <dc:date opf:event='publication'>%s</dc:date>%n", pubDate);
		}
		out.println("    </metadata>");
		// Verzeichnis der Dateien des Buches
		out.println("    <manifest>");
		out.printf("      <item id='ncx' href='%s' media-type='application/x-dtbncx+xml'/>%n", NCX);
		for (int i = 0; i < contentFiles.size(); i++) {
			FileEntry entry = contentFiles.get(i);
			out.printf("      <item id='%s' href='%s' media-type='%s'/>%n", entry.getId(), entry.getFilename(), entry.getMimeType());
		}
		for (FileEntry entry : mediaFiles) {
			out.printf("      <item id='%s' href='%s' media-type='%s'/>%n", entry.getId(), entry.getFilename(), entry.getMimeType());
		}
		out.println("    </manifest>");
		// Reihenfolge der Inhalte des Buches
		out.println("    <spine toc='ncx'>");
		for (int i = 0; i < contentFiles.size(); i++) {
			FileEntry entry = contentFiles.get(i);
			out.printf("      <itemref idref='%s'/>%n", entry.getId());
		}
		out.println("    </spine>");
		out.println("    <guide>");
		out.printf("      <reference type='cover' title='%s' href='%s'/>%n", res.getString("cover"), COVER);
		out.println("    </guide>");
		out.println("  </package>");
		out.flush();
		zip.closeEntry();
	}

	private boolean exists(File basedir, String filename) {
		File f = new File(basedir, filename);
		return f.exists();
	}

	private static boolean isEmpty(String s) {
		return s == null || s.isEmpty();
	}

	private static String nvl(String s1, String s2) {
		return s1 == null ? s2 : s1;
	}

	private void echo(String key, Object... param) {
		System.out.println(String.format(res.getString(key), param));
	}
}
