package jhilbert.exceptions;

import jhilbert.exceptions.GeneralException;

/**
 * Thrown when an {@link jhilbert.util.InputSource} is unable to provide input (or EOF),
 * for example, when an I/O error occurs.
 */
public final class InputException extends GeneralException {

	/**
	 * Creates a new InputException with specified detail message, context, and cause.
	 *
	 * @param message detail message String.
	 * @param context context in which the exception ocurred.
	 * @param cause Throwable which caused this exception.
	 */
	public InputException(final String message, final String context, final Throwable cause) {
		super(message, context, cause);
	}

	/**
	 * Creates a new InputException with specified detail message and context, without initializing the cause.
	 *
	 * @param message detail message String.
	 * @param context context in which the exception ocurred.
	 */
	public InputException(final String message, final String context) {
		super(message, context);
	}

}
