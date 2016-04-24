package net.java.textilej.parser.markup.confluence.block;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.java.textilej.parser.Attributes;
import net.java.textilej.parser.DocumentBuilder.BlockType;
import net.java.textilej.parser.markup.Block;

/**
 * Table block, matches blocks that start with <code>table. </code> or those that
 * start with a table row.
 * 
 * @author dgreen
 */
public class TableBlock extends Block {

	static final Pattern startPattern = Pattern.compile("(\\|(.*)?(\\|\\s*$))");

	static final Pattern TABLE_ROW_PATTERN = Pattern.compile("\\|(\\|)?" + "((?:(?:[^\\|\\[]*)(?:\\[[^\\]]*\\])?)*)"
			+ "(\\|\\|?\\s*$)?");

	private int blockLineCount = 0;

	private Matcher matcher;

	public TableBlock() {
	}

	@Override
	public int processLineContent(String line, int offset) {
		if (blockLineCount == 0) {
			Attributes attributes = new Attributes();
			builder.beginBlock(BlockType.TABLE, attributes);
		} else if (dialect.isEmptyLine(line)) {
			setClosed(true);
			return 0;
		}
		++blockLineCount;

		if (offset == line.length()) {
			return -1;
		}

		String textileLine = offset == 0 ? line : line.substring(offset);
		Matcher rowMatcher = TABLE_ROW_PATTERN.matcher(textileLine);
		if (!rowMatcher.find()) {
			setClosed(true);
			return 0;
		}

		builder.beginBlock(BlockType.TABLE_ROW, new Attributes());

		do {
			int start = rowMatcher.start();
			if (start == textileLine.length() - 1) {
				break;
			}

			String headerIndicator = rowMatcher.group(1);
			String text = rowMatcher.group(2);
			int lineOffset = offset + rowMatcher.start(2);

			boolean header = headerIndicator != null && "|".equals(headerIndicator);

			Attributes attributes = new Attributes();
			builder.beginBlock(header ? BlockType.TABLE_CELL_HEADER : BlockType.TABLE_CELL_NORMAL, attributes);

			dialect.emitMarkupLine(getParser(), state, lineOffset, text, 0);

			builder.endBlock(); // table cell
		} while (rowMatcher.find());

		builder.endBlock(); // table row

		return -1;
	}

	@Override
	public boolean canStart(String line, int lineOffset) {
		blockLineCount = 0;
		if (lineOffset == 0) {
			matcher = startPattern.matcher(line);
			return matcher.matches();
		} else {
			matcher = null;
			return false;
		}
	}

	@Override
	public void setClosed(boolean closed) {
		if (closed && !isClosed()) {
			builder.endBlock();
		}
		super.setClosed(closed);
	}
	
}
