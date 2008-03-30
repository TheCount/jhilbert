package jhilbert.exceptions;

import jhilbert.exceptions.GeneralException;

/**
 * Syntax error while scanning a LISP symbolic expression.
 */
public final class SyntaxException extends GeneralException {

	/**
	 * Creates a new SyntaxException with specified detail message, context, and cause.
	 *
	 * @param message the detail message.
	 * @param context context String.
	 * @param cause Throwable which caused this exception.
	 */
	public SyntaxException(final String message, final String context, final Throwable cause) {
		super(message, context, cause);
	}

	/**
	 * Creates a new SyntaxException with specified detail message and context.
	 *
	 * @param message the detail message.
	 * @param context context String.
	 */
	public SyntaxException(final String message, final String context) {
		super(message, context);
	}

}
