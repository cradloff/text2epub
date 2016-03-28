package text2epub;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Klasse zum Schreiben in eine Zip-Datei */
public class ZipWriter extends Writer {
	/** Zeichensatz */
	private static final String ENCODING = "UTF-8";
	private ZipOutputStream zip;
	private PrintWriter out;
	private boolean isEntryOpen;

	/**
	 * Konstruktor.
	 * @param file Datei
	 */
	public ZipWriter(File file) throws IOException {
		zip = new ZipOutputStream(new FileOutputStream(file));
		zip.setLevel(9);
		out = new PrintWriter(new OutputStreamWriter(zip, ENCODING), false);
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		out.write(cbuf, off, len);
	}

	@Override
	public void flush() throws IOException {
		out.flush();
	}

	@Override
	public void close() throws IOException {
		out.close();
		closeEntry();
		zip.close();
	}

	/**
	 * Liefert den zugrundeliegenden Output-Stream zurück.
	 * @return Output-Stream
	 */
	public ZipOutputStream getOutputStream() {
		return zip;
	}

	/**
	 * Startet einen neuen Eintrag.
	 * @param path Dateiname
	 */
	public void newEntry(String path) throws IOException {
		closeEntry();
		zip.putNextEntry(new ZipEntry(path));
	}

	/**
	 * Erstellt einen neuen unkomprimierten Eintrag.
	 * @param path Dateiname des Eintrags
	 * @param content Inhalt
	 */
	public void storeEntry(String path, String content) throws IOException {
		closeEntry();
		// die Datei darf nicht komprimiert werden
		ZipEntry entry = new ZipEntry(path);
		entry.setMethod(ZipEntry.STORED);
		// dummerweise muss die Größe und CRC-32 jetzt explizit gesetzt werden:
		entry.setCompressedSize(content.length());
		CRC32 crc = new CRC32();
		crc.update(content.getBytes());
		entry.setCrc(crc.getValue());
		zip.putNextEntry(entry);
		out.print(content);
		out.flush();
		zip.closeEntry();
	}

	/** Schreibt eine Datei aus dem Dateisystem in die Zip-Datei */
	public void writeFile(File file) throws IOException {
		newEntry(file.getName());
		IOUtils.copy(file, zip);
	}

	private void closeEntry() throws IOException {
		if (isEntryOpen) {
			isEntryOpen = false;
			zip.closeEntry();
		}
	}
}
