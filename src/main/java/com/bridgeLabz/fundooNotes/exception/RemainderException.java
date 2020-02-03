package com.bridgeLabz.fundooNotes.exception;

/**
 * This class extends {@link RuntimeException} and creates a custom exception
 * which would be thrown during the user's abnormal activity during remainder
 * operation.
 * 
 * @author Durgasankar Mishra
 * @created 2020-01-30
 * @version 1.0
 */
public class RemainderException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final int status;

	/**
	 * Constructor takes message and Status code as input parameter and fetch
	 * message from its superclass.
	 * 
	 * @param message as String input parameter
	 * @param status  as Integer input parameter
	 */
	public RemainderException(String message, int status) {
		super(message);
		this.status = status;
	}

	public int getStatus() {
		return status;
	}
}