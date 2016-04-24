package net.java.textilej.parser.markup.confluence.block;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.java.textilej.parser.Attributes;
import net.java.textilej.parser.DocumentBuilder.BlockType;
import net.java.textilej.parser.markup.Block;

/**
 * quoted text block, matches blocks that start with <code>bc. </code>.  
 * Creates an extended block type of {@link ParagraphBlock paragraph}.
 * 
 * @author dgreen
 */
public class QuoteBlock extends Block {


	static final Pattern startPattern = Pattern.compile("bq\\.\\s+(.*)");
	
	private int blockLineCount = 0;
	private Matcher matcher;
	
	public QuoteBlock() {
	}

	@Override
	public int processLineContent(String line,int offset) {
		if (blockLineCount == 0) {
			Attributes attributes = new Attributes();

			offset = matcher.start(1);

			builder.beginBlock(BlockType.QUOTE, attributes);
			builder.beginBlock(BlockType.PARAGRAPH, new Attributes());
		}
		if (dialect.isEmptyLine(line)) {
			setClosed(true);
			return 0;
		}
		if (blockLineCount != 0) {
			builder.lineBreak();
		}
		++blockLineCount;
		
		getDialect().emitMarkupLine(getParser(),state,line, offset);
		
		return -1;
	}

	@Override
	public boolean canStart(String line, int lineOffset) {
		blockLineCount = 0;
		matcher = startPattern.matcher(line);
		if (lineOffset > 0) {
			matcher.region(lineOffset, line.length());
		} 
		return matcher.matches();
	}

	@Override
	public void setClosed(boolean closed) {
		if (closed && !isClosed()) {
			builder.endBlock(); // para
			builder.endBlock(); // quote
		}
		super.setClosed(closed);
	}	
}
