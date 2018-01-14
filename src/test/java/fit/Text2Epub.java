package fit;

/**
 * Create a ebook. The argument <code>folder</code> is the directory name where the
 * content is and <code>filename</code> is the filename of the ebook.
 */
public class Text2Epub extends ColumnFixture {
	public String folder;
	public String filename;

	@Override
	public void execute() throws Exception {
		text2epub.Text2Epub.main("src/test/fit/" + folder, "target/fit/" + filename);
	}
}
