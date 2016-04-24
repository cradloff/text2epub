package net.java.textilej.parser.markup.textile;

import java.util.ArrayList;
import java.util.List;

import net.java.textilej.parser.DocumentBuilder.SpanType;
import net.java.textilej.parser.markup.Block;
import net.java.textilej.parser.markup.ContentState;
import net.java.textilej.parser.markup.Dialect;
import net.java.textilej.parser.markup.phrase.HtmlEndTagPhraseModifier;
import net.java.textilej.parser.markup.phrase.HtmlStartTagPhraseModifier;
import net.java.textilej.parser.markup.textile.block.CodeBlock;
import net.java.textilej.parser.markup.textile.block.FootnoteBlock;
import net.java.textilej.parser.markup.textile.block.HeadingBlock;
import net.java.textilej.parser.markup.textile.block.ListBlock;
import net.java.textilej.parser.markup.textile.block.ParagraphBlock;
import net.java.textilej.parser.markup.textile.block.PreformattedBlock;
import net.java.textilej.parser.markup.textile.block.QuoteBlock;
import net.java.textilej.parser.markup.textile.block.TableBlock;
import net.java.textilej.parser.markup.textile.block.TableOfContentsBlock;
import net.java.textilej.parser.markup.textile.block.TextileGlossaryBlock;
import net.java.textilej.parser.markup.textile.phrase.EscapeTextilePhraseModifier;
import net.java.textilej.parser.markup.textile.phrase.ImageTextilePhraseModifier;
import net.java.textilej.parser.markup.textile.phrase.SimpleTextilePhraseModifier;
import net.java.textilej.parser.markup.textile.token.FootnoteReferenceReplacementToken;
import net.java.textilej.parser.markup.textile.token.HyperlinkReplacementToken;
import net.java.textilej.parser.markup.token.AcronymReplacementToken;
import net.java.textilej.parser.markup.token.EntityReferenceReplacementToken;
import net.java.textilej.parser.markup.token.EntityWrappingReplacementToken;
import net.java.textilej.parser.markup.token.PatternEntityReferenceReplacementToken;

/**
 * A textile dialect that parses <a href="http://en.wikipedia.org/wiki/Textile_(markup_language)">Textile markup</a>.
 * 
 * Based on the spec available at <a href="http://textile.thresholdstate.com/">http://textile.thresholdstate.com/</a>,
 * supports all current Textile markup constructs.
 * 
 * Additionally supported are <code>{toc}</code> and <code>{glossary}</code>.
 * 
 * @author dgreen
 */
public class TextileDialect extends Dialect {
	
	// we use the template pattern for creating new blocks
	private List<Block> blocks = new ArrayList<Block>();
	private List<Block> paragraphBreakingBlocks = new ArrayList<Block>();
	

	private static PatternBasedSyntax tokenSyntax = new PatternBasedSyntax();
	private static PatternBasedSyntax phraseModifierSyntax = new PatternBasedSyntax();
	
	
	@Override 
	protected PatternBasedSyntax getPhraseModifierSyntax() {
		return phraseModifierSyntax;
	}

	@Override
	protected PatternBasedSyntax getReplacementTokenSyntax() {
		return tokenSyntax;
	}	
	
	{
		
		// IMPORTANT NOTE: Most items below have order dependencies.  DO NOT REORDER ITEMS BELOW!!
		
		blocks.add(new HeadingBlock());
		ListBlock listBlock = new ListBlock();
		blocks.add(listBlock);
		paragraphBreakingBlocks.add(listBlock);
		blocks.add(new PreformattedBlock());
		blocks.add(new QuoteBlock());
		blocks.add(new CodeBlock());
		blocks.add(new FootnoteBlock());
		TableBlock tableBlock = new TableBlock();
		blocks.add(tableBlock);
		paragraphBreakingBlocks.add(tableBlock);
		
		// extensions
		blocks.add(new TextileGlossaryBlock());
		blocks.add(new TableOfContentsBlock());
		// ~extensions
		
		blocks.add(new ParagraphBlock()); // ORDER DEPENDENCY: this must come last
	}
	static {	
		phraseModifierSyntax.add(new HtmlEndTagPhraseModifier());
		phraseModifierSyntax.add(new HtmlStartTagPhraseModifier());
		phraseModifierSyntax.beginGroup("(?:(?<=[\\s\\.,\\\"'?!;:\\)\\(\\{\\}\\[\\]])|^)(?:",0);
		phraseModifierSyntax.add(new EscapeTextilePhraseModifier());
		phraseModifierSyntax.add(new SimpleTextilePhraseModifier("**",SpanType.BOLD, true));
		phraseModifierSyntax.add(new SimpleTextilePhraseModifier("??",SpanType.CITATION, true));
		phraseModifierSyntax.add(new SimpleTextilePhraseModifier("__",SpanType.ITALIC, true));
		phraseModifierSyntax.add(new SimpleTextilePhraseModifier("_",SpanType.EMPHASIS, true));
		phraseModifierSyntax.add(new SimpleTextilePhraseModifier("*",SpanType.STRONG, true));
		phraseModifierSyntax.add(new SimpleTextilePhraseModifier("+",SpanType.INSERTED, true));
		phraseModifierSyntax.add(new SimpleTextilePhraseModifier("~",SpanType.SUBSCRIPT, false));
		phraseModifierSyntax.add(new SimpleTextilePhraseModifier("^",SpanType.SUPERSCRIPT, false));
		phraseModifierSyntax.add(new SimpleTextilePhraseModifier("@",SpanType.CODE, false));
		phraseModifierSyntax.add(new SimpleTextilePhraseModifier("%",SpanType.SPAN, true));
		phraseModifierSyntax.add(new SimpleTextilePhraseModifier("-",SpanType.DELETED, true));
		phraseModifierSyntax.add(new ImageTextilePhraseModifier());
		phraseModifierSyntax.endGroup(")(?=\\W|$)",0);
		
		tokenSyntax.add(new EntityReferenceReplacementToken("(tm)","#8482"));
		tokenSyntax.add(new EntityReferenceReplacementToken("(TM)","#8482"));
		tokenSyntax.add(new EntityReferenceReplacementToken("(c)","#169"));
		tokenSyntax.add(new EntityReferenceReplacementToken("(C)","#169"));
		tokenSyntax.add(new EntityReferenceReplacementToken("(r)","#174"));
		tokenSyntax.add(new EntityReferenceReplacementToken("(R)","#174"));
		tokenSyntax.add(new HyperlinkReplacementToken());
		tokenSyntax.add(new FootnoteReferenceReplacementToken());
		tokenSyntax.add(new EntityWrappingReplacementToken("\"","#8220","#8221"));
		tokenSyntax.add(new EntityWrappingReplacementToken("'","#8216","#8217"));
		tokenSyntax.add(new PatternEntityReferenceReplacementToken("(?:(?<=\\w)(')(?=\\w))","#8217")); // apostrophe
		tokenSyntax.add(new PatternEntityReferenceReplacementToken("(?:(?<=\\w\\s)(--)(?=\\s\\w))","#8212")); // emdash
		tokenSyntax.add(new PatternEntityReferenceReplacementToken("(?:(?<=\\w\\s)(-)(?=\\s\\w))","#8211")); // endash
		tokenSyntax.add(new PatternEntityReferenceReplacementToken("(?:(?<=\\d\\s)(x)(?=\\s\\d))","#215")); // mul
		tokenSyntax.add(new AcronymReplacementToken());
	}
	
	public List<Block> getParagraphBreakingBlocks() {
		return paragraphBreakingBlocks;
	}

	@Override
	public List<Block> getBlocks() {
		return blocks;
	}
	
	@Override
	protected ContentState createState() {
		return new TextileContentState();
	}
}
