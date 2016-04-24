package net.java.textilej.parser.markup.mediawiki;

import java.util.ArrayList;
import java.util.List;

import net.java.textilej.parser.DocumentBuilder.SpanType;
import net.java.textilej.parser.markup.Block;
import net.java.textilej.parser.markup.Dialect;
import net.java.textilej.parser.markup.mediawiki.block.HeadingBlock;
import net.java.textilej.parser.markup.mediawiki.block.ListBlock;
import net.java.textilej.parser.markup.mediawiki.block.ParagraphBlock;
import net.java.textilej.parser.markup.mediawiki.block.PreformattedBlock;
import net.java.textilej.parser.markup.mediawiki.block.TableBlock;
import net.java.textilej.parser.markup.mediawiki.phrase.EscapePhraseModifier;
import net.java.textilej.parser.markup.mediawiki.phrase.SimplePhraseModifier;
import net.java.textilej.parser.markup.mediawiki.token.HyperlinkExternalReplacementToken;
import net.java.textilej.parser.markup.mediawiki.token.HyperlinkInternalReplacementToken;
import net.java.textilej.parser.markup.mediawiki.token.ImageReplacementToken;
import net.java.textilej.parser.markup.mediawiki.token.LineBreakToken;
import net.java.textilej.parser.markup.mediawiki.token.TemplateReplacementToken;
import net.java.textilej.parser.markup.phrase.HtmlCommentPhraseModifier;
import net.java.textilej.parser.markup.phrase.LimitedHtmlEndTagPhraseModifier;
import net.java.textilej.parser.markup.phrase.LimitedHtmlStartTagPhraseModifier;
import net.java.textilej.parser.markup.token.EntityReferenceReplacementToken;
import net.java.textilej.parser.markup.token.ImpliedHyperlinkReplacementToken;
import net.java.textilej.parser.markup.token.PatternLiteralReplacementToken;

/**
 * A dialect for <a href="http://www.mediawiki.org">MediaWiki</a> 
 * <a href="http://en.wikipedia.org/wiki/Wikitext">Wikitext markup</a>, which is the wiki format
 * used by <a href="http://www.wikipedia.org>WikiPedia</a> and 
 * <a href="http://www.wikimedia.org/">several other major sites</a>.
 * 
 * @author dgreen
 *
 */
public class MediaWikiDialect extends Dialect {
	private List<Block> blocks = new ArrayList<Block>();
	private List<Block> paragraphBreakingBlocks = new ArrayList<Block>();

	private static PatternBasedSyntax tokenSyntax = new PatternBasedSyntax();
	private static PatternBasedSyntax phraseModifierSyntax = new PatternBasedSyntax();
	
	private String internalPageHrefPrefix = "/wiki/";
		
	{
		
		// IMPORTANT NOTE: Most items below have order dependencies.  DO NOT REORDER ITEMS BELOW!!
		
		blocks.add(new HeadingBlock());
		blocks.add(new ListBlock());
		blocks.add(new PreformattedBlock());
		blocks.add(new TableBlock());
		final ParagraphBlock paragraphBlock = new ParagraphBlock();
		blocks.add(paragraphBlock); // ORDER DEPENDENCY: this one must be last!!
		
		for (Block block: blocks) {
			if (block == paragraphBlock) {
				continue;
			}
			paragraphBreakingBlocks.add(block);
		}
	}
	static {
		phraseModifierSyntax.beginGroup("(?:(?<=[\\s\\.,\\\"'?!;:\\)\\(\\{\\}\\[\\]])|^)(?:",0);
		phraseModifierSyntax.add(new EscapePhraseModifier());
		phraseModifierSyntax.add(new SimplePhraseModifier("'''''",new SpanType[] { SpanType.BOLD, SpanType.ITALIC },true));
		phraseModifierSyntax.add(new SimplePhraseModifier("'''",SpanType.BOLD,true));
		phraseModifierSyntax.add(new SimplePhraseModifier("''",SpanType.ITALIC,true));
		phraseModifierSyntax.endGroup(")(?=\\W|$)",0);
		
		String[] allowedHtmlTags = new String[] {
			// HANDLED BY LineBreakToken "<br>",
			// HANDLED BY LineBreakToken "<br/>",
				"b", "big", "blockquote", "caption", "center", "cite", "code", "dd", "del", "div", "dl", "dt", "em", "font", "h1", "h2", "h3", "h4", "h5", "h6", "hr", "i", "ins", "li", "ol", "p", "pre", "rb", "rp", "rt", "ruby", "s", "small", "span", "strike", "strong", "sub", "sup", "table", "td", "th", "tr", "tt", "u", "ul", "var" };
		phraseModifierSyntax.add(new LimitedHtmlEndTagPhraseModifier(allowedHtmlTags));
		phraseModifierSyntax.add(new LimitedHtmlStartTagPhraseModifier(allowedHtmlTags));
		phraseModifierSyntax.add(new HtmlCommentPhraseModifier());
		
		tokenSyntax.add(new LineBreakToken());
		tokenSyntax.add(new EntityReferenceReplacementToken("(tm)","#8482"));
		tokenSyntax.add(new EntityReferenceReplacementToken("(TM)","#8482"));
		tokenSyntax.add(new EntityReferenceReplacementToken("(c)","#169"));
		tokenSyntax.add(new EntityReferenceReplacementToken("(C)","#169"));
		tokenSyntax.add(new EntityReferenceReplacementToken("(r)","#174"));
		tokenSyntax.add(new EntityReferenceReplacementToken("(R)","#174"));
		tokenSyntax.add(new ImageReplacementToken());
		tokenSyntax.add(new HyperlinkInternalReplacementToken());
		tokenSyntax.add(new HyperlinkExternalReplacementToken());
		tokenSyntax.add(new ImpliedHyperlinkReplacementToken());
		tokenSyntax.add(new PatternLiteralReplacementToken("(?:(?<=\\w\\s)(----)(?=\\s\\w))","<hr/>")); // horizontal rule
		tokenSyntax.add(new TemplateReplacementToken());
		tokenSyntax.add(new net.java.textilej.parser.markup.mediawiki.token.EntityReferenceReplacementToken());
	}


	@Override
	protected PatternBasedSyntax getPhraseModifierSyntax() {
		return phraseModifierSyntax;
	}

	@Override
	protected PatternBasedSyntax getReplacementTokenSyntax() {
		return tokenSyntax;
	}	


	
	@Override
	public List<Block> getBlocks() {
		return blocks;
	}

	public List<Block> getParagraphBreakingBlocks() {
		return paragraphBreakingBlocks;
	}
	
	/**
	 * Convert a page name to an href to the page.
	 * 
	 * @param pageName the name of the page to target
	 * 
	 * @return the href to access the page
	 * 
	 * @see #getInternalPageHrefPrefix()
	 */
	public String toInternalHref(String pageName) {
		String pageId = pageName.replace(' ', '_');
		if (pageId.startsWith(":")) { // category
			pageId = pageId.substring(1);
		} else if (pageId.startsWith("#")) {
			// internal anchor
			return pageId;
		}
		return internalPageHrefPrefix + pageId;
	}

	/**
	 * Get the href prefix for references to internal pages.  The default value is <code>/wiki/</code>.
	 */
	public String getInternalPageHrefPrefix() {
		return internalPageHrefPrefix;
	}

	/**
	 * Set the href prefix for references to internal pages.  The default value is <code>/wiki/</code>.
	 */
	public void setInternalPageHrefPrefix(String internalPageHrefPrefix) {
		this.internalPageHrefPrefix = internalPageHrefPrefix;
	}
	
	
}
