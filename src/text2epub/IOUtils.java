package text2epub;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** Hilfsfunktionen für IO */
public class IOUtils {

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

}
