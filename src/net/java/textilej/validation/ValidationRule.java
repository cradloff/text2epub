package net.java.textilej.validation;


/**
 * A validation rule
 * 
 * @author dgreen
 */
public abstract class ValidationRule {

	/**
	 * Starting at the given offset find the next validation problem.
	 * 
	 * @param markup the markup content in which a validation problem should be found
	 * @param offset the offset at which to start looking for problems
	 * @param length the length at which to stop looking for problems
	 * 
	 * @return the validation problem if found, or null if no validation problem was detected
	 */
	public abstract ValidationProblem findProblem(String markup,int offset,int length);
}
