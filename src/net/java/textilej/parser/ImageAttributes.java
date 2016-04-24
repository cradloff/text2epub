package net.java.textilej.parser;

/**
 * Attributes for images.
 * 
 * @author dgreen
 */
public class ImageAttributes extends Attributes {
	
	public enum Align {
		Left, Right, Top, Texttop, Middle, Absmiddle, Baseline, Bottom, Absbottom, Center
	}
	
	private int width = -1;
	private int height = -1;
	private int border = 0;
	private Align align = null;
	private String alt;
	
	
	public String getAlt() {
		return alt;
	}
	public void setAlt(String alt) {
		this.alt = alt;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public int getBorder() {
		return border;
	}
	public void setBorder(int border) {
		this.border = border;
	}
	public Align getAlign() {
		return align;
	}
	public void setAlign(Align align) {
		this.align = align;
	}
}
