package net.java.textilej.parser.outline;

import net.java.textilej.parser.Attributes;
import net.java.textilej.parser.DocumentBuilder;
import net.java.textilej.parser.IdGenerator;
import net.java.textilej.parser.MarkupParser;
import net.java.textilej.parser.markup.Dialect;

/**
 * A parser for creating an outline of a textile document based on the headings in the document. 
 * 
 * @author dgreen
 *
 */
public class OutlineParser {
	
	private int labelMaxLength = 0;
	
	private Dialect dialect;

	public OutlineParser(Dialect dialect) {
		this.dialect = dialect;
	}
	
	public OutlineParser() {}
	
	public int getLabelMaxLength() {
		return labelMaxLength;
	}

	public void setLabelMaxLength(int labelMaxLength) {
		this.labelMaxLength = labelMaxLength;
	}
	
	public OutlineItem parse(String markup) {
		OutlineItem root = createRootItem();
		
		return parse(root, markup);
	}

	public Dialect getDialect() {
		return dialect;
	}
	
	public void setDialect(Dialect dialect) {
		this.dialect = dialect;
	}

	public OutlineItem createRootItem() { 
		return createOutlineItem(null,0,"<root>",-1,0,"<root>");
	}
	
	public OutlineItem parse(OutlineItem root, String markup) {
		if (markup == null || markup.length() == 0 || dialect == null) {
			return root;
		}

		dialect.setFilterGenerativeContents(true);
		dialect.setBlocksOnly(true);
		
		OutlineBuilder outlineBuilder = new OutlineBuilder(root,labelMaxLength);
		MarkupParser markupParser = new MarkupParser();
		markupParser.setBuilder(outlineBuilder);
		markupParser.setDialect(dialect);
		markupParser.parse(markup);
		
		return root;
	}

	protected static OutlineItem createOutlineItem(OutlineItem current,int level, String id, int offset,
			int length, String label) {
		return new OutlineItem(current,level,id,offset,length,label);
	}
	
	public DocumentBuilder createOutlineUpdater(OutlineItem rootItem) {
		return new OutlineBuilder(rootItem,labelMaxLength);
	}
	
	private static class OutlineBuilder extends DocumentBuilder {

		private OutlineItem currentItem;
		
		private int level;
		private StringBuilder buf;

		private IdGenerator idGenerator = new IdGenerator();

		private int offset;

		private int length;

		private OutlineItem rootItem;

		private final int labelMaxLength;
		
		public OutlineBuilder(OutlineItem root,int labelMaxLength) {
			super();
			this.currentItem = root;
			rootItem = root;
			this.labelMaxLength = labelMaxLength;
		}

		@Override
		public void acronym(String text, String definition) {
		}

		@Override
		public void beginBlock(BlockType type, Attributes attributes) {
		}

		@Override
		public void beginDocument() {
			rootItem.clear();
			currentItem = rootItem;
		}

		@Override
		public void beginHeading(int level, Attributes attributes) {
			this.level = level;
			buf = new StringBuilder();
			offset = getLocator().getDocumentOffset();
			length = getLocator().getLineLength();
		}

		@Override
		public void beginSpan(SpanType type, Attributes attributes) {
		}

		@Override
		public void characters(String text) {
			if (buf != null) {
				buf.append(text);
			}
		}

		@Override
		public void charactersUnescaped(String literal) {
			if (buf != null) {
				buf.append(literal);
			}
		}

		@Override
		public void endBlock() {
		}

		@Override
		public void endDocument() {
		}

		@Override
		public void endHeading() {
			String label = buf.toString();
			String fullLabelText = label;
			if (label == null) {
				label = "";
			} else {
				if (labelMaxLength > 0 && label.length() > labelMaxLength) {
					label = label.substring(0,labelMaxLength)+"...";
				}
			}
			String kind = "h"+level;
			
			while (level <= currentItem.getLevel()) {
				currentItem = currentItem.getParent();
			}
			currentItem = createOutlineItem(currentItem,level,idGenerator.newId(kind,fullLabelText), offset, length, label);
			currentItem.setTooltip(fullLabelText);
			currentItem.setKind(kind);
			
			buf = null;
			offset = 0;
			length = 0;
		}

		@Override
		public void endSpan() {
		}

		@Override
		public void entityReference(String entity) {
		}

		@Override
		public void image(Attributes attributes, String url) {
		}

		@Override
		public void imageLink(Attributes linkAttributes, Attributes ImageAttributes,String href, String imageUrl) {
		}

		@Override
		public void lineBreak() {
		}

		@Override
		public void link(Attributes attributes,String hrefOrHashName, String text) {
			if (buf != null) {
				buf.append(text);
			}
		}
		
	}
}
