package fit;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Lists the content of a textfile from a zip archive. The first argument is the name
 * of the zip archive, the second the zip entry.
 */
public class ZipEntryListing extends RowFixture {
	public static class FileEntry {
		public int line;
		public String content;
		public FileEntry(int line, String content) {
			this.line = line;
			this.content = content;
		}
	}

	@Override
	public Class<FileEntry> getTargetClass() {
		return FileEntry.class;
	}

	@Override
	public Object[] query() throws Exception {
		int line = 0;
		List<FileEntry> result = new ArrayList<>();
		String archive = args[0];
		String filename = args[1];
		try (
				FileInputStream fis = new FileInputStream(ZipFileListing.resolve(archive));
				ZipInputStream zis = new ZipInputStream(fis);
				) {
			for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
				if (filename.equals(entry.getName())) {
					Reader is = new InputStreamReader(zis, Charset.forName("UTF-8"));
					BufferedReader reader = new BufferedReader(is);
					for (String s = reader.readLine(); s != null; s = reader.readLine()) {
						if (! s.trim().isEmpty()) {
							result.add(new FileEntry(++line, s.trim()));
						}
					}
				}
			}
		}

		if (line == 0) {
			throw new FileNotFoundException(filename);
		}

		return result.toArray();
	}

}
