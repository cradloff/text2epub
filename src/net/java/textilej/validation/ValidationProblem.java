package net.java.textilej.validation;


public class ValidationProblem {
	public enum Severity {
		WARNING,
		ERROR
	}
	
	private String markerId = "net.java.textilej.validation.problem";
	private Severity severity;
	private String message;
	private int offset;
	private int length;

	/**
	 * create a validation problem
	 * 
	 * @param severity
	 *            a severity, which must be one of the <code>SEVERITY_*</code>
	 *            constants from {@link org.eclipse.core.resources.IMarker}
	 * @param message
	 *            the message describing the problem
	 * @param offset
	 *            the offset into the document that the problem starts
	 * @param length
	 *            the length of the problem, which may be 0
	 * 
	 * @throws IllegalArgumentException
	 *             if the severity is invalid, the offset is < 0, the length is <
	 *             0, or if no message is provided
	 */
	public ValidationProblem(Severity severity, String message, int offset,
			int length) {
		setSeverity(severity);
		setMessage(message);
		setOffset(offset);
		setLength(length);
	}

	public String getMarkerId() {
		return markerId;
	}

	public void setMarkerId(String markerId) {
		this.markerId = markerId;
	}

	public Severity getSeverity() {
		return severity;
	}

	/**
	 * @param severity a severity
	 */
	public void setSeverity(Severity severity) {
		if (severity == null) {
			throw new IllegalArgumentException();
		}
		this.severity = severity;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		if (message == null || message.length() == 0) {
			throw new IllegalArgumentException();
		}
		this.message = message;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		if (offset < 0) {
			throw new IllegalArgumentException();
		}
		this.offset = offset;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		if (length < 0) {
			throw new IllegalArgumentException();
		}
		this.length = length;
	}
	
	@Override
	public String toString() {
		return severity+"["+offset+","+length+"]: "+message;
	}
}
