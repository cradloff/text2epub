/**
 * 
 */
package net.java.textilej.parser;

/**
 * Attributes for a markup element
 * 
 * @author dgreen
 */
public class Attributes {

	private String cssClass;
	private String id;
	private String cssStyle;
	private String language;
	private String title;
	
	public Attributes() {}
	
	
	public Attributes(String id, String cssClass, String cssStyle,
			String language) {
		this.id = id;
		this.cssClass = cssClass;
		this.cssStyle = cssStyle;
		this.language = language;
	}

	public String getCssClass() {
		return cssClass;
	}
	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getCssStyle() {
		return cssStyle;
	}
	public void setCssStyle(String cssStyle) {
		this.cssStyle = cssStyle;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
}