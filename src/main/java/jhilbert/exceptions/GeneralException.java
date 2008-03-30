package jhilbert.exceptions;

/**
 * General exception for JHilbert.
 * Allows for an extra string denoting the context in the LISP stream.
 */
public abstract class GeneralException extends Exception {

	/**
	 * Constructs a new GeneralException in the specified context.
	 *
	 * @param context context String.
	 */
	public GeneralException(final String context) {
		super("in context: " + context);
	}

	/**
	 * Constructs a new GeneralException with the specified detail message and context.
	 *
	 * @param message detail message String.
	 * @param context context String.
	 */
	public GeneralException(final String message, final String context) {
		this(message, context, null);
	}

	/**
	 * Constructs a new GeneralException with the specified detail message, context and cause.
	 *
	 * @param message detail message String.
	 * @param context context String.
	 * @param cause the Throwable that caused this exception.
	 */
	public GeneralException(final String message, final String context, final Throwable cause) {
		super(message + " in context " + context, cause);
	}

}
