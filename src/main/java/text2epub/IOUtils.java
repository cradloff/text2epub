package text2epub;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/** Hilfsfunktionen für IO */
public class IOUtils {
	/** Zeichensatz */
	private static final String ENCODING = "UTF-8";

	/**
	 * Kopiert die Datei aus dem Classpath ins angegebene Verzeichnis.
	 */
	public static void copyCP2FS(String filename, File basedir) throws IOException {
		try (InputStream is = IOUtils.class.getResourceAsStream("/" + filename);
			OutputStream os = new FileOutputStream(new File(basedir, filename));) {
			copy(is, os);
		}
	}

	/**
	 * Kopiert die Daten aus der Datei auf den Output-Stream.
	 */
	public static void copy(File file, OutputStream os) throws IOException {
		try (FileInputStream fis = new FileInputStream(file)) {
			copy(fis, os);
		}
	}

	/**
	 * Kopiert die Daten aus dem Input-Stream auf den Output-Stream.
	 */
	public static void copy(InputStream is, OutputStream os) throws IOException {
		byte[] buf = new byte[1024];
		int len;
		while ((len = is.read(buf)) > 0) {
			os.write(buf, 0, len);
		}
	}

	/**
	 * Prüft, ob die Datei vorhanden ist.
	 * @param basedir Verzeichnis
	 * @param filename Dateiname
	 * @return true oder false
	 */
	public static boolean exists(File basedir, String filename) {
		File f = new File(basedir, filename);
		return f.exists();
	}

	/**
	 * Liest die übergebene Datei ein.
	 * @param file Datei
	 * @return Inhalt der Datei
	 */
	public static String read(File file) throws IOException {
		StringBuilder sb = new StringBuilder();
		char[] buf = new char[1024];
		try (FileReader fr = new FileReader(file)) {
			int len;
			while ((len = fr.read(buf)) > 0) {
				sb.append(buf, 0, len);
			}
		}

		return sb.toString();
	}

	/**
	 * Schreibt die Daten in die Datei.
	 * @throws IOException
	 */
	public static void write(String data, File file) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(file);
				Writer out = new OutputStreamWriter(fos, ENCODING);) {
			out.write(data);
		}
	}

	/**
	 * Liefert die Datei-Endung zurück.
	 * @param file Datei
	 * @return Datei-Endung
	 */
	public static String suffix(File file) {
		return suffix(file.getName());
	}

	/**
	 * Liefert die Datei-Endung zurück.
	 * @param filename Dateiname
	 * @return Datei-Endung
	 */
	public static String suffix(String filename) {
		int idx = filename.lastIndexOf('.');
		if (idx < 0) {
			return null;
		}

		return filename.substring(idx);
	}

	/** Ersetzt die Dateiendung durch die angegebene Endung */
	static String replaceSuffix(File file, String newSuffix) {
		return replaceSuffix(file.getName(), newSuffix);
	}

	/** Ersetzt die Dateiendung durch die angegebene Endung */
	static String replaceSuffix(String filename, String newSuffix) {
		String outputFilename = filename.substring(0, filename.lastIndexOf("."));
		outputFilename += newSuffix;

		return outputFilename;
	}

}
