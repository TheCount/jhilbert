/*
    JHilbert, a verifier for collaborative theorem proving
    Copyright © 2008, 2009 Alexander Klauer

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

import java.util.Map;

import jhilbert.data.Variable;

/**
 * A <code>Substituter</code> is able to perform simultaneous well-formed
 * substitution on {@link Expression}s, as well as the reverse operation,
 * unification.
 *
 * Classes implementing this interface must provide a constructor accepting
 * a {@link Variable}&bsp;-&gt;{@link Expression} mapping to implement the
 * substitutions in form of a
 * {@link Map}&lt;{@link Variable}, {@link Expression}&gt;.
 */
public interface Substituter {

	/**
	 * Returns the current {@link Variable} to {@link Expression}
	 * assignments of this <code>Substituter</code>.
	 *
	 * @return the current variable to expression assigments.
	 */
	public Map<Variable, Expression> getAssignments();

	/**
	 * Creates a new {@link Expression} by substituting the specified
	 * expression using the substitution rule inherent to this
	 * <code>Substituter</code>.
	 *
	 * @param expr expression to be substituted.
	 *
	 * @return new substituted expression.
	 */
	public Expression substitute(Expression expr);

	/**
	 * This method performs the reverse of {@link #substitute}. The
	 * internal variable assignments are updated so that applying
	 * {@link #substitute} to the specified source expression would yield
	 * the specified target expression.
	 *
	 * @param source source expression.
	 * @param target target expression.
	 *
	 * @throws UnifyException if a well-formed update of the internal
	 * 	variable assigments to meet the specification above is not
	 * 	possible. If this exception is thrown, the internal assignment
	 * 	map may be left in an undefined state.
	 *
	 * @see #getAssignments
	 */
	public void unify(Expression source, Expression target) throws UnifyException;

	/**
	 * This is a special method to unify cross-module expressions.
	 * Source expressions are completely unfolded before translated and
	 * unified. This is necessary for definition compatibility.
	 *
	 * @param source source expression.
	 * @param target target expression.
	 * @param translator module translator.
	 *
	 * @throws UnifyException if the unfolded expressions cannot be
	 * 	unified with {@link #unify(Expression, Expression)}.
	 */
	public void crossUnify(Expression source, Expression target, Translator translator) throws UnifyException;

}
