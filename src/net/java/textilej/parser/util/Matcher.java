package net.java.textilej.parser.util;

public interface Matcher {
	public String group(int group);
	public int start(int group);
	public int end(int group);
}
