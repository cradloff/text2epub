package net.java.textilej.parser.markup;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.java.textilej.parser.DocumentBuilder;
import net.java.textilej.parser.MarkupParser;
import net.java.textilej.parser.outline.OutlineParser;
import net.java.textilej.util.LocationTrackingReader;

/**
 * A markup dialect, which knows its formatting rules and is able to
 * process content based on {@link Block}, {@link PatternBasedElementProcessor} and {@link PatternBasedElement}
 * concepts.  All markup languages supported by Textile-J extend this class.
 * 
 * The Dialect class provides basic functionality for determining which blocks process
 * which markup content in a particular document.  In general multi-line documents are split into
 * consecutive regions called blocks, and each line in a block is processed with spanning sections
 * called phrase modifiers, and tokens within a span are replaced with their respective replacement
 * tokens.  These rules apply to most markup languages, however subclasses may override this default
 * functionality if required.  For example, by default phrase modifiers are non-overlapping and non-nested, 
 * however if required a subclass could permit such nesting.
 * 
 * Generally dialect classes are not accessed directly by client code, instead client code should
 * configure and call {@link MarkupParser}.
 * 
 * @author dgreen
 */
public abstract class Dialect {

	private String name;
	
	private boolean filterGenerativeBlocks;
	private boolean blocksOnly;
	
	
	/**
	 * Create new state for tracking a document and its contents during a parse session.
	 * Subclasses may override this method to provide additional state tracking capability.
	 * 
	 * @return the new state.
	 */
	protected ContentState createState() {
		return new ContentState();
	}
	
	public void processContent(MarkupParser parser, String markupContent, boolean asDocument) {
		ContentState state = createState();
		state.setMarkupContent(markupContent);
		LocationTrackingReader reader = new LocationTrackingReader(new StringReader(markupContent));
		String line;
		Block currentBlock = null;
		
		DocumentBuilder builder = parser.getBuilder();
		
		builder.setLocator(state);
		try {
			if (asDocument) {
				builder.beginDocument();
			}
			
			try {
				while ((line = reader.readLine()) != null) {
					
					state.setLineNumber(reader.getLineNumber()+1);
					state.setLineOffset(reader.getLineOffset());
					state.setLineCharacterOffset(0);
					state.setLineSegmentEndOffset(0);
					state.setLineLength(line.length());
					
					int lineOffset = 0;
					for (;;) {
						if (currentBlock == null) {
							currentBlock = startBlock(line,lineOffset);
							if (currentBlock == null) {
								break;
							}
							currentBlock.setState(state);
							currentBlock.setParser(parser);
						}
						lineOffset = currentBlock.processLineContent(line,lineOffset);
						if (currentBlock.isClosed()) {
							currentBlock = null;
						}
						if (lineOffset < line.length() && lineOffset >= 0) {
							if (currentBlock != null) {
								throw new IllegalStateException("if a block does not fully process a line then it must be closed");
							}
						} else {
							break;
						}
					}
				}
				state.setLineNumber(reader.getLineNumber()+1);
				state.setLineOffset(reader.getLineOffset());
				state.setLineCharacterOffset(0);
				state.setLineLength(0);
				
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
			
			if (currentBlock != null && !currentBlock.isClosed()) {
				currentBlock.setClosed(true);
			}
			
			if (asDocument) {
				builder.endDocument();
			}
		} finally {
			builder.setLocator(null);
		}
	}

	public Block startBlock(String line,int lineOffset) {
		if (isEmptyLine(line)) {
			// nothing starts on an empty line
			return null;
		}
		for (Block block: getBlocks()) {
			if (block.canStart(line, lineOffset)) {
				return block.clone();
			}
		}
		return null;
	}
	
	public abstract List<Block> getBlocks();


	/**
	 * Emit a markup line that may contain phrase modifiers and replacement tokens, but no
	 * block modifiers.
	 * 
	 * @param parser
	 * @param state
	 * @param textLineOffset the offset of the provided text in the current line
	 * @param line the text to process
	 * @param offset the offset in the <code>text</code> at which processing should begin
	 */
	public void emitMarkupLine(MarkupParser parser, ContentState state,int textLineOffset, String line, int offset) {
		if (blocksOnly) {
			emitMarkupText(parser,state,line.substring(offset));
			return;
		}
		for (;;) {
			PatternBasedElementProcessor phraseModifier = getPhraseModifierSyntax().findPatternBasedElement(line, offset);
			if (phraseModifier != null) {
				int newOffset = phraseModifier.getLineStartOffset();
				if (offset < newOffset) {
					state.setLineCharacterOffset(textLineOffset+offset);
					state.setLineSegmentEndOffset(textLineOffset+newOffset);
					String text = line.substring(offset,newOffset);
					emitMarkupText(parser,state,text);
				}
				phraseModifier.setParser(parser);
				phraseModifier.setState(state);
				state.setLineCharacterOffset(textLineOffset+phraseModifier.getLineStartOffset());
				state.setLineSegmentEndOffset(textLineOffset+phraseModifier.getLineEndOffset());
				phraseModifier.emit();
				offset = phraseModifier.getLineEndOffset();
				if (offset >= line.length()) {
					break;
				}
			} else {
				state.setLineCharacterOffset(textLineOffset+offset);
				state.setLineSegmentEndOffset(textLineOffset+line.length());
				emitMarkupText(parser,state,line.substring(offset));
				break;
			}
		}
	}
	
	/**
	 * Emit a markup line that may contain phrase modifiers and replacement tokens, but no
	 * block modifiers.
	 * 
	 * @param parser
	 * @param state
	 * @param line
	 * @param offset
	 */
	public void emitMarkupLine(MarkupParser parser,ContentState state,String line,int offset) {
		emitMarkupLine(parser, state,0, line, offset);
	}
	
	/**
	 * Emit markup that may contain replacement tokens but no phrase or block modifiers.
	 * 
	 * @param parser
	 * @param state
	 * @param text
	 */
	public void emitMarkupText(MarkupParser parser,ContentState state,String text) {
		if (blocksOnly) {
			parser.getBuilder().characters(text);
			return;
		}
		int offset = 0;
		for (;;) {
			PatternBasedElementProcessor patternBasedElement = getReplacementTokenSyntax().findPatternBasedElement(text, offset);
			if (patternBasedElement != null) {
				int newOffset = patternBasedElement.getLineStartOffset();
				if (offset < newOffset) {
					String text2 = text.substring(offset,newOffset);
					emitMarkupText(parser,state,text2);
				}
				patternBasedElement.setParser(parser);
				patternBasedElement.setState(state);
				patternBasedElement.emit();
				offset = patternBasedElement.getLineEndOffset();
				if (offset >= text.length()) {
					break;
				}
			} else {
				parser.getBuilder().characters(offset>0?text.substring(offset):text);
				break;
			}
		}
	}

	private static class Group {
		int count;
	}
	
	public static class PatternBasedSyntax {
		protected List<PatternBasedElement> elements = new ArrayList<PatternBasedElement>();
		protected Pattern elementPattern;
		protected List<Integer> elementGroup = new ArrayList<Integer>();

		private StringBuilder patternBuffer = new StringBuilder();
		private int patternGroup = 0;
		private Stack<Group> groups = new Stack<Group>();
		{
			groups.push(new Group());
		}
		
		public PatternBasedSyntax() {}

		public void add(PatternBasedElement element) {
			elementPattern = null;
			elements.add(element);
			if (groups.peek().count++ > 0) {
				patternBuffer.append('|');
			}
			++patternGroup;
			patternBuffer.append('(');
			patternBuffer.append(element.getPattern(patternGroup));
			patternBuffer.append(')');
			elementGroup.add(patternGroup);
			patternGroup += element.getPatternGroupCount();
		}
		public void beginGroup(String regexFragment, int size) {
			add(regexFragment,size,true);
		}
		public void endGroup(String regexFragment, int size) {
			add(regexFragment,size,false);
		}
		private void add(String regexFragment, int size,boolean beginGroup) {
			elementPattern = null;
			if (beginGroup) {
				if (groups.peek().count++ > 0) {
					patternBuffer.append('|');
				}
				groups.push(new Group());
				patternBuffer.append("(?:");
			} else {
				groups.pop();
			}
			patternBuffer.append(regexFragment);
			if (!beginGroup) {
				patternBuffer.append(")");
			}
			patternGroup += size;
		}
		
		public PatternBasedElementProcessor findPatternBasedElement(String lineText,int offset) {
			Matcher matcher = getPattern().matcher(lineText);
			if (offset > 0) {
				matcher.region(offset, lineText.length());
			}
			if (matcher.find()) {
				int size = elementGroup.size();
				for (int x = 0;x<size;++x) {
					int group = elementGroup.get(x);
					String value = matcher.group(group);
					if (value != null) {
						PatternBasedElement element = elements.get(x);
						PatternBasedElementProcessor processor = element.newProcessor();
						processor.setLineStartOffset(matcher.start());
						processor.setLineEndOffset(matcher.end());
						for (int y = 0;y<element.getPatternGroupCount();++y) {
							final int groupIndex = group+y+1;
							processor.setGroup(y+1,matcher.group(groupIndex),matcher.start(groupIndex),matcher.end(groupIndex));
						}
						return processor;
					}
				}
				throw new IllegalStateException();
			} else {
				return null;
			}
		}

		public Pattern getPattern() {
			if (elementPattern == null) {
				if (patternBuffer.length() > 0) {
					elementPattern = Pattern.compile(patternBuffer.toString());
				} else {
					return null;
				}
			}
			return elementPattern;
		}

	}


	protected abstract PatternBasedSyntax getPhraseModifierSyntax();
	protected abstract PatternBasedSyntax getReplacementTokenSyntax();

	/**
	 * The name of the dialect, typically the same as the name of the
	 * markup language supported by this dialect.  This value may be displayed to the
	 * user.
	 * 
	 * @return the name, or null if unknown
	 */
	public String getName() {
		return name;
	}

	/**
	 * The name of the dialect, typically the same as the name of the
	 * markup language supported by this dialect.  This value may be displayed to the
	 * user.
	 * 
	 * @param name the name
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public Dialect clone() {
		Dialect dialect;
		try {
			dialect = getClass().newInstance();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		dialect.setName(name);
		return dialect;
	}

	/**
	 * Indicate if generative contents should be filtered.  This option is used with the {@link OutlineParser}.
	 */
	public boolean isFilterGenerativeContents() {
		return filterGenerativeBlocks;
	}

	/**
	 * Indicate if table of contents should be filtered.  This option is used with the {@link OutlineParser}.
	 */
	public void setFilterGenerativeContents(boolean filterGenerativeBlocks) {
		this.filterGenerativeBlocks = filterGenerativeBlocks;
	}

	/**
	 * indicate if the parser should detect blocks only.  This is useful for use in a document partitioner where the partition boundaries are defined by blocks.
	 */
	public boolean isBlocksOnly() {
		return blocksOnly;
	}

	/**
	 * indicate if the parser should detect blocks only.  This is useful for use in a document partitioner where the partition boundaries are defined by blocks.
	 */
	public void setBlocksOnly(boolean blocksOnly) {
		this.blocksOnly = blocksOnly;
	}

	/**
	 * indicate if the given line is considered 'empty'.  The default implementation
	 * returns true for lines of length 0, and for lines whose only content is whitespace.
	 * 
	 * @param line the line content
	 * 
	 * @return true if the given line is considered empty by this dialect 
	 */
	public boolean isEmptyLine(String line) {
		if (line.length() == 0) {
			return true;
		}
		for (int x = 0;x<line.length();++x) {
			if (!Character.isWhitespace(line.charAt(x))) {
				return false;
			}
		}
		return true;
	}
}
