package net.java.textilej.parser.markup.confluence;

import java.util.ArrayList;
import java.util.List;

import net.java.textilej.parser.DocumentBuilder.BlockType;
import net.java.textilej.parser.DocumentBuilder.SpanType;
import net.java.textilej.parser.markup.Block;
import net.java.textilej.parser.markup.Dialect;
import net.java.textilej.parser.markup.confluence.block.CodeBlock;
import net.java.textilej.parser.markup.confluence.block.ExtendedPreformattedBlock;
import net.java.textilej.parser.markup.confluence.block.ExtendedQuoteBlock;
import net.java.textilej.parser.markup.confluence.block.HeadingBlock;
import net.java.textilej.parser.markup.confluence.block.ListBlock;
import net.java.textilej.parser.markup.confluence.block.ParagraphBlock;
import net.java.textilej.parser.markup.confluence.block.QuoteBlock;
import net.java.textilej.parser.markup.confluence.block.TableBlock;
import net.java.textilej.parser.markup.confluence.block.TableOfContentsBlock;
import net.java.textilej.parser.markup.confluence.block.TextBoxBlock;
import net.java.textilej.parser.markup.confluence.phrase.ImagePhraseModifier;
import net.java.textilej.parser.markup.confluence.phrase.SimplePhraseModifier;
import net.java.textilej.parser.markup.confluence.phrase.SimpleWrappedPhraseModifier;
import net.java.textilej.parser.markup.confluence.token.AnchorReplacementToken;
import net.java.textilej.parser.markup.confluence.token.HyperlinkReplacementToken;
import net.java.textilej.parser.markup.token.EntityReferenceReplacementToken;
import net.java.textilej.parser.markup.token.ImpliedHyperlinkReplacementToken;
import net.java.textilej.parser.markup.token.PatternEntityReferenceReplacementToken;
import net.java.textilej.parser.markup.token.PatternLineBreakReplacementToken;
import net.java.textilej.parser.markup.token.PatternLiteralReplacementToken;

public class ConfluenceDialect extends Dialect {

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
		
		HeadingBlock headingBlock = new HeadingBlock();
		blocks.add(headingBlock);
		paragraphBreakingBlocks.add(headingBlock);
		ListBlock listBlock = new ListBlock();
		blocks.add(listBlock);
		paragraphBreakingBlocks.add(listBlock);
		blocks.add(new QuoteBlock());
		TableBlock tableBlock = new TableBlock();
		blocks.add(tableBlock);
		paragraphBreakingBlocks.add(tableBlock);
		ExtendedQuoteBlock quoteBlock = new ExtendedQuoteBlock();
		blocks.add(quoteBlock);
		paragraphBreakingBlocks.add(quoteBlock);
		ExtendedPreformattedBlock noformatBlock = new ExtendedPreformattedBlock();
		blocks.add(noformatBlock);
		paragraphBreakingBlocks.add(noformatBlock);
		// TODO: {color:red}{color}
		blocks.add(new TextBoxBlock(BlockType.PANEL,"panel"));
		blocks.add(new TextBoxBlock(BlockType.NOTE,"note"));
		blocks.add(new TextBoxBlock(BlockType.INFORMATION,"info"));
		blocks.add(new TextBoxBlock(BlockType.WARNING,"warning"));
		blocks.add(new TextBoxBlock(BlockType.TIP,"tip"));
		CodeBlock codeBlock = new CodeBlock();
		blocks.add(codeBlock);
		paragraphBreakingBlocks.add(codeBlock);
		blocks.add(new TableOfContentsBlock());
		
		blocks.add(new ParagraphBlock()); // ORDER DEPENDENCY: this must come last
	}
	static {
		phraseModifierSyntax.beginGroup("(?:(?<=[\\s\\.,\\\"'?!;:\\)\\(\\[\\]])|^)(?:",0);
		phraseModifierSyntax.add(new SimplePhraseModifier("*",SpanType.STRONG, true));
		phraseModifierSyntax.add(new SimplePhraseModifier("_",SpanType.EMPHASIS, true));
		phraseModifierSyntax.add(new SimplePhraseModifier("??",SpanType.CITATION, true));
		phraseModifierSyntax.add(new SimplePhraseModifier("-",SpanType.DELETED, true));
		phraseModifierSyntax.add(new SimplePhraseModifier("+",SpanType.UNDERLINED, true));
		phraseModifierSyntax.add(new SimplePhraseModifier("^",SpanType.SUPERSCRIPT, false));
		phraseModifierSyntax.add(new SimplePhraseModifier("~",SpanType.SUBSCRIPT, false));
		phraseModifierSyntax.add(new SimpleWrappedPhraseModifier("{{","}}",SpanType.MONOSPACE, false));
		phraseModifierSyntax.add(new ImagePhraseModifier());
		phraseModifierSyntax.endGroup(")(?=\\W|$)",0);

		tokenSyntax.add(new EntityReferenceReplacementToken("(tm)","#8482"));
		tokenSyntax.add(new EntityReferenceReplacementToken("(TM)","#8482"));
		tokenSyntax.add(new EntityReferenceReplacementToken("(c)","#169"));
		tokenSyntax.add(new EntityReferenceReplacementToken("(C)","#169"));
		tokenSyntax.add(new EntityReferenceReplacementToken("(r)","#174"));
		tokenSyntax.add(new EntityReferenceReplacementToken("(R)","#174"));
		tokenSyntax.add(new HyperlinkReplacementToken());
		tokenSyntax.add(new PatternEntityReferenceReplacementToken("(?:(?<=\\w\\s)(---)(?=\\s\\w))","#8212")); // emdash
		tokenSyntax.add(new PatternEntityReferenceReplacementToken("(?:(?<=\\w\\s)(--)(?=\\s\\w))","#8211")); // endash
		tokenSyntax.add(new PatternLiteralReplacementToken("(?:(?<=\\w\\s)(----)(?=\\s\\w))","<hr/>")); // horizontal rule
		tokenSyntax.add(new PatternLineBreakReplacementToken("(\\\\\\\\)")); // line break
		tokenSyntax.add(new ImpliedHyperlinkReplacementToken());
		tokenSyntax.add(new AnchorReplacementToken());
	}

	
	public ConfluenceDialect() {}
	
	@Override
	public List<Block> getBlocks() {
		return blocks;
	}

	public List<Block> getParagraphBreakingBlocks() {
		return paragraphBreakingBlocks;
	}

}
