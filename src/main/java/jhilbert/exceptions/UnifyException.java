package jhilbert.exceptions;

import jhilbert.data.TermExpression;
import jhilbert.exceptions.GeneralException;

/**
 * Exception throws when an expression unification fails.
 *
 * @see jhilbert.data.TermExpression#unify()
 */
public class UnifyException extends GeneralException {

	/**
	 * Creates a new UnifyException with the specified detail message and cause.
	 * The source and target of the unification attempt must also be specified.
	 * This information is then rendered into a suitable error message.
	 *
	 * @param message detail message.
	 * @param source source of the unification (that is, the TermExpression or subexpression whereof whose {@link jhilbert.data.TermExpression#unify()} method was called).
	 * @param target target of the unification.
	 * @param cause Throwable which caused this exception to be thrown (or <code>null</code> if there is no cause).
	 */
	public UnifyException(final String message, final TermExpression source, final TermExpression target, final Throwable cause) {
		super(message, "unifying " + source.toString() + " with " + target.toString(), cause);
	}

	/**
	 * Creates a new UnifyException with the specified detail message.
	 * The source and target of the unification attempt must also be specified.
	 * This information is then rendered into a suitable error message.
	 *
	 * @param message detail message.
	 * @param source source of the unification (that is, the TermExpression or subexpression whereof whose {@link jhilbert.data.TermExpression#unify()} method was called).
	 * @param target target of the unification.
	 */
	public UnifyException(final String message, final TermExpression source, final TermExpression target) {
		this(message, source, target, null);
	}

}
