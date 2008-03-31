package jhilbert.exceptions;

import jhilbert.exceptions.GeneralException;

/**
 * Exception thrown when something goes wrong with the data.
 *
 * @see jhilbert.data.ModuleData
 *
 * FIXME: more stuff
 */
public final class DataException extends GeneralException {

	/**
	 * Create a new DataException with the specified detail message, context, and cause.
	 *
	 * @param message the detail message.
	 * @param context context String of this exception.
	 * @param cause Throwable which caused this exception.
	 */
	public DataException(final String message, final String context, final Throwable cause) {
		super(message, context, cause);
	}

	/**
	 * Create a new DataException with the specified detail message and context.
	 *
	 * @param message the detail message.
	 * @param context context String of this exception.
	 */
	public DataException(final String message, final String context) {
		super(message, context);
	}

}
