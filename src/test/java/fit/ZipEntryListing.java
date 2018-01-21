package fit;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import fit.TypeAdapter.IntAdapter;

/**
 * Lists the content of a textfile from a zip archive. The first argument is the name
 * of the zip archive, the second the zip entry.
 */
public class ZipEntryListing extends RowFixture {
	private static final class ContentTypeAdapter extends TypeAdapter {
		@Override
		public Object get() throws IllegalAccessException, InvocationTargetException {
			// unescape smart quotes like in Parse.text();
			String t = super.get().toString();
			t = t.replace('\u201c', '"');
			t = t.replace('\u201d', '"');
			t = t.replace('\u2018', '\'');
			t = t.replace('\u2019', '\'');

			return t;
		}
	}

	private static final class LineTypeAdapter extends IntAdapter {

	}

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

	@Override
	protected TypeAdapter bindField(String name) throws Exception {
		TypeAdapter ta;
		if (name.equals("content")) {
			ta = new ContentTypeAdapter();
		} else if (name.equals("line")) {
			ta = new LineTypeAdapter();
		} else {
			ta = super.bindField(name);
		}
		ta.field = getTargetClass().getField(camel(name));
		ta.target = this;
		ta.init(this, String.class);

		return ta;
	}

	@Override
	protected List<Parse> list(Parse rows) {
		// automatically set line if absent
		int col = -1;
		LineTypeAdapter ta = null;
		for (int i = 0; i < columnBindings.length; i++) {
			if (columnBindings[i] instanceof LineTypeAdapter) {
				col = i;
				ta = (LineTypeAdapter) columnBindings[i];
			}
		}

		int line = 1;
		List<Parse> result = new LinkedList<>();
		while (rows != null) {
			result.add(rows);
			if (col >= 0) {
				Parse cell = rows.parts.at(col);
				String value = cell.text();
				if (value.isEmpty()) {
					cell.body = info("" + line);
				} else {
					line = Integer.parseInt(value);
				}
			}
			rows = rows.more;
			line++;
		}

		return result;
	}

}
