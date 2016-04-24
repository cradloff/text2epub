package net.java.textilej.parser.markup.trac;

import java.util.ArrayList;
import java.util.List;

import net.java.textilej.parser.DocumentBuilder.SpanType;
import net.java.textilej.parser.markup.Block;
import net.java.textilej.parser.markup.Dialect;
import net.java.textilej.parser.markup.token.ImpliedHyperlinkReplacementToken;
import net.java.textilej.parser.markup.trac.block.HeadingBlock;
import net.java.textilej.parser.markup.trac.block.ListBlock;
import net.java.textilej.parser.markup.trac.block.ParagraphBlock;
import net.java.textilej.parser.markup.trac.block.PreformattedBlock;
import net.java.textilej.parser.markup.trac.block.QuoteBlock;
import net.java.textilej.parser.markup.trac.block.TableBlock;
import net.java.textilej.parser.markup.trac.phrase.EscapePhraseModifier;
import net.java.textilej.parser.markup.trac.phrase.SimplePhraseModifier;
import net.java.textilej.parser.markup.trac.token.BangEscapeToken;
import net.java.textilej.parser.markup.trac.token.HyperlinkReplacementToken;
import net.java.textilej.parser.markup.trac.token.LineBreakToken;

public class TracWikiDialect extends Dialect {
	private List<Block> blocks = new ArrayList<Block>();
	private List<Block> paragraphNestableBlocks = new ArrayList<Block>();

	private static PatternBasedSyntax tokenSyntax = new PatternBasedSyntax();
	private static PatternBasedSyntax phraseModifierSyntax = new PatternBasedSyntax();
	
	{

		// IMPORTANT NOTE: Most items below have order dependencies.  DO NOT REORDER ITEMS BELOW!!
		
		// TODO: traclinks, images, macros, processors
		
		ListBlock listBlock = new ListBlock();
		blocks.add(listBlock);
		paragraphNestableBlocks.add(listBlock);
		HeadingBlock headingBlock = new HeadingBlock();
		blocks.add(headingBlock);
		paragraphNestableBlocks.add(listBlock);
		PreformattedBlock preformattedBlock = new PreformattedBlock();
		blocks.add(preformattedBlock);
		paragraphNestableBlocks.add(preformattedBlock);
		QuoteBlock quoteBlock = new QuoteBlock();
		blocks.add(quoteBlock);
		paragraphNestableBlocks.add(quoteBlock);
		TableBlock tableBlock = new TableBlock();
		blocks.add(tableBlock);
		paragraphNestableBlocks.add(tableBlock);
		blocks.add(new ParagraphBlock()); // ORDER DEPENDENCY: this one must be last!!
	}
	static {
		phraseModifierSyntax.beginGroup("(?:(?<=[\\s\\.\\\"'?!;:\\)\\(\\{\\}\\[\\]])|^)(?:",0); // always starts at the start of a line or after a non-word character excluding '!'
		phraseModifierSyntax.add(new EscapePhraseModifier());
		phraseModifierSyntax.add(new SimplePhraseModifier("'''''",new SpanType[] { SpanType.BOLD, SpanType.ITALIC },true));
		phraseModifierSyntax.add(new SimplePhraseModifier("'''",SpanType.BOLD,true));
		phraseModifierSyntax.add(new SimplePhraseModifier("''",SpanType.ITALIC,true));
		phraseModifierSyntax.add(new SimplePhraseModifier("__",SpanType.UNDERLINED,true));
		phraseModifierSyntax.add(new SimplePhraseModifier("--",SpanType.DELETED,true));
		phraseModifierSyntax.add(new SimplePhraseModifier("^",SpanType.SUPERSCRIPT,true));
		phraseModifierSyntax.add(new SimplePhraseModifier(",,",SpanType.SUBSCRIPT,true));
		phraseModifierSyntax.endGroup(")(?=\\W|$)",0);
		
		tokenSyntax.add(new BangEscapeToken());
		tokenSyntax.add(new LineBreakToken());
		tokenSyntax.add(new HyperlinkReplacementToken());
		tokenSyntax.add(new ImpliedHyperlinkReplacementToken());
	}
	
	@Override
	public List<Block> getBlocks() {
		return blocks;
	}

	public List<Block> getParagraphNestableBlocks() {
			return paragraphNestableBlocks;
	}

	@Override
	protected PatternBasedSyntax getPhraseModifierSyntax() {
		return phraseModifierSyntax;
	}

	@Override
	protected PatternBasedSyntax getReplacementTokenSyntax() {
		return tokenSyntax;
	}

}
