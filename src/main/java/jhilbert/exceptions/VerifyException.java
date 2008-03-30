package jhilbert.exceptions;

import jhilbert.exceptions.GeneralException;

/**
 * Execption occurring during {@link jhilbert.commands.Command} execution.
 *
 * @see jhilbert.commands.Command#execute()
 */
public final class VerifyException extends GeneralException {

	/**
	 * Creates a new VerifyException with specified detail message, context, and cause.
	 *
	 * @param message detail message.
	 * @param context context String.
	 * @param cause Throwable which caused this exception.
	 */
	public VerifyException(final String message, final String context, final Throwable cause) {
		super(message, context, cause);
	}

	/**
	 * Creates a new VerifyException with specified detail message and context.
	 *
	 * @param message detail message.
	 * @param context context String.
	 */
	public VerifyException(final String message, final String context) {
		super(message, context);
	}

}
