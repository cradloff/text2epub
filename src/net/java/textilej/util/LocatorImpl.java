package net.java.textilej.util;

import net.java.textilej.parser.Locator;

public class LocatorImpl implements Locator {

	private int documentOffset;
	private int lineCharacterOffset;
	private int lineDocumentOffset;
	private int lineLength;
	private int lineNumber;
	private int lineSegmentEndOffset;

	public LocatorImpl(Locator other) {
		documentOffset = other.getDocumentOffset();
		lineCharacterOffset = other.getLineCharacterOffset();
		lineDocumentOffset = other.getLineDocumentOffset();
		lineLength = other.getLineLength();
		lineNumber = other.getLineNumber();
		lineSegmentEndOffset = other.getLineSegmentEndOffset();
	}

	public int getDocumentOffset() {
		return documentOffset;
	}

	public int getLineCharacterOffset() {
		return lineCharacterOffset;
	}

	public int getLineDocumentOffset() {
		return lineDocumentOffset;
	}

	public int getLineLength() {
		return lineLength;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public int getLineSegmentEndOffset() {
		return lineSegmentEndOffset;
	}
	
}
