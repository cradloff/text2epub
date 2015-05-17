package org.markdown2epub;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ResourceBundle;
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
		try (InputStream pin = new FileInputStream(new File(basedir, "epub.xml"))) {
			props.loadFromXML(pin);
		}
		res = ResourceBundle.getBundle("Markdown2Epub", Locale.forLanguageTag(props.getProperty("language")));
		File epub = new File(basedir, basedir.getName() + ".epub");
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
		writeFile(basedir, "cover.png", "image/png", COVER_ID);
		writeCover();

		contentFiles.add(new FileEntry(TOC, MIMETYPE_XHTML, "toc"));

		// Markdown-Dateien konvertieren und schreiben
		File[] files = basedir.listFiles();
		Arrays.sort(files);
		for (File file : files) {
			if (file.getName().endsWith(".md")) {
				convert(file);
			}
		}

		// Inhaltsverzeichnis ausgeben
		writeNCX();
		writeTOC();

		// Stammdatei schreiben
		writeContent();

		zip.close();

		echo("MsgSuccess", epub.getName());
	}

	private void writeFile(File basedir, String filename, String mimeType, String id) throws IOException {
		File file = new File(basedir, filename);
		if (! file.exists()) {
			echo("MsgSkipFile", filename);
			return;
		}

		mediaFiles.add(new FileEntry(filename, mimeType, id));
		zip.putNextEntry(new ZipEntry(filename));
		byte[] buf = new byte[1024];
		try (FileInputStream fis = new FileInputStream(file)) {
			int len;
			while ((len = fis.read(buf)) > 0) {
				zip.write(buf, 0, len);
			}
		}
		zip.closeEntry();
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

	private void writeCover() throws IOException {
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
		out.printf("  <img id='cover' src='cover.png' alt='Cover %s'/>%n", props.getProperty("title"));
		out.println("</body>");
		out.println("</html>");
		out.flush();
		zip.closeEntry();
	}

	private void convert(File file) throws IOException {
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
