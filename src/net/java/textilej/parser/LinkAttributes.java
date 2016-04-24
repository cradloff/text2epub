package net.java.textilej.parser;

import net.java.textilej.parser.builder.HtmlDocumentBuilder;

/**
 * Attributes for links (hyperlinks)
 * 
 * @author dgreen
 * @author draft
 */
public class LinkAttributes extends Attributes {
	private String target;
	private String rel;

	/**
	 * The target of a link, as defined by the HTML spec.
	 * 
	 * @param target
	 *            the target or null if there should be none
	 */
	public void setTarget(String target) {
		this.target = target;
	}

	/**
	 * The target of a link, as defined by the HTML spec.
	 * 
	 * @return the target or null if there should be none
	 */
	public String getTarget() {
		return target;
	}

	/**
	 * The 'rel' of a link, as defined by the HTML spec.
	 * 
	 * @return the rel or null if there should be none
	 * 
	 * @see HtmlDocumentBuilder#getLinkRef()
	 */
	public String getRel() {
		return rel;
	}

	/**
	 * The 'rel' of a link, as defined by the HTML spec.
	 * 
	 * @param rel
	 *            the rel or null if there should be none
	 * 
	 * @see HtmlDocumentBuilder#setLinkRel(String)
	 */
	public void setRel(String rel) {
		this.rel = rel;
	}

}
