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

package jhilbert.expressions;

/**
 * Thrown by {@link Substituter#unify} if a unification is not possible.
 */
public class UnifyException extends ExpressionException {

	/**
	 * Source term of failed unification.
	 */
	private final Expression source;

	/**
	 * Target term of failed unification.
	 */
	private final Expression target;

	/**
	 * Creates a new <code>UnifyException</code> with the specified detail
	 * message, source and target of the failed unification.
	 *
	 * @param message detail message.
	 * @param source source term.
	 * @param target target term.
	 */
	public UnifyException(final String message, final Expression source, final Expression target) {
		this(message, source, target, null);
	}

	/**
	 * Creates a new <code>UnifyException</code> with the specified detail
	 * message, source, target and cause.
	 *
	 * @param message detail message.
	 * @param source source term.
	 * @param cause the cause.
	 */
	public UnifyException(final String message, final Expression source, final Expression target, final Throwable cause) {
		super(message, cause);
		this.source = source;
		this.target = target;
	}

	/**
	 * Returns the source term of the failed unification.
	 *
	 * @return source term of failed unification.
	 */
	public Expression getSource() {
		return source;
	}

	/**
	 * Returns the target term of the failed unification.
	 *
	 * @return target term of failed unification.
	 */
	public Expression getTarget() {
		return target;
	}

}
