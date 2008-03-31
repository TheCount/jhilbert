/*
    JHilbert, a verifier for collaborative theorem proving
    Copyright © 2008 Alexander Klauer

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    You may contact the author on these Wiki pages:
    http://planetx.cc.vt.edu/AsteroidMeta//GrafZahl (preferred)
    http://en.wikisource.org/wiki/User_talk:GrafZahl
*/

package jhilbert.exceptions;

import jhilbert.data.TermExpression;
import jhilbert.exceptions.GeneralException;

/**
 * Exception thrown when an expression unification fails.
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
