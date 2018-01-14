package fit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Lists the contents of a zip file. The fixture takes the archive name as
 * argument and returns all entries in the file.
 */
public class ZipFileListing extends RowFixture {
	private static File SRC_DIR = new File("src/test/fit");
	private static File TARGET  = new File("target/fit");

	@Override
	public Class<ZipEntry> getTargetClass() {
		return ZipEntry.class;
	}

	@Override
	public Object[] query() throws Exception {
		String filename = args[0];
		List<ZipEntry> result = new ArrayList<>();
		try (
				FileInputStream fis = new FileInputStream(resolve(filename));
				ZipInputStream zis = new ZipInputStream(fis);) {
			for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
				result.add(entry);
			}
		}

		return result.toArray();
	}

	public static File resolve(String filename) throws FileNotFoundException {
		File file = new File(SRC_DIR, filename);
		if (! file.exists()) {
			file = new File(TARGET, filename);
		}

		if (! file.exists()) {
			throw new FileNotFoundException(filename);
		}

		return file;
	}

}
