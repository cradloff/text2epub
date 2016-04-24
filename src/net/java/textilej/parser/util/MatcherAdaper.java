package net.java.textilej.parser.util;


public class MatcherAdaper implements Matcher {

	private java.util.regex.Matcher delegate;

	public MatcherAdaper(java.util.regex.Matcher delegate) {
		this.delegate = delegate;
	}

	public int end(int group) {
		return delegate.end(group);
	}

	public String group(int group) {
		return delegate.group(group);
	}

	public int start(int group) {
		return delegate.start(group);
	}

}
