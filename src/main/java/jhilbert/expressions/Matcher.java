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

    You may contact the author on this Wiki page:
    http://www.wikiproofs.de/w/index.php?title=User_talk:GrafZahl
*/

package jhilbert.expressions;

import java.util.Map;
import java.util.Set;

import jhilbert.data.Variable;

/**
 * The <code>Matcher</code> interface makes concrete the different notions of
 * equality of expressions.
 * As such, a <code>Matcher</code> is solely concerned with
 * {@link Variable}/{@link Variable} comparisons. For
 * {@link Variable}/{@link Expression} comparisons, use a {@link Substituter}.
 * <p>
 * <code>Matcher</code> currently supports two notions of equality:
 * <ol>
 * <li><strong>Definition equivalence:</strong> When source and target
 * 	expression are unfolded to
 * 	{@link jhilbert.data.Functor#definitionDepth} zero, they are equal,
 * 	with no substitutions necessary whatsoever.
 * <li><strong>Variable equivalence:</strong> When source and target expression
 * 	can be made <em>definition equivalent</em> with a well-formed
 * 	translation of dummy variables. In order to avoid unsound replacement
 * 	of dummy variables with non-dummies, a blacklist of
 * 	not-to-dummy-assignable variables may be specified, causing an
 * 	exception to be thrown if such an assignment would be necessary.
 * </ol>
 * A single matcher can be used several times for simultaneous well-formed
 * matching.
 */
public interface Matcher {

	/**
	 * Checks the source and the target expression for <em>definition
	 * equivalence</em> as specified in the interface description.
	 *
	 * @param source source expression.
	 * @param target target expression.
	 *
	 * @return <code>true</code> if <code>source</code> and
	 * 	<code>target</code> are equal as specified, <code>false</code>
	 * 	otherwise.
	 */
	public boolean checkDEquality(Expression source, Expression target);

	/**
	 * Checks the source and the target expression for <em>variable
	 * equivalence</em> as specified in the interface description.
	 * The internal variable assignment map is updated accordingly. This
	 * means it may be left in an undefined state if this method throws an
	 * exception or returns <code>false</code>.
	 *
	 * @param source source expression.
	 * @param target target expression.
	 * @param blacklist list of not-to-dummy-assignable variables.
	 *
	 * @return <code>true</code> if <code>source</code> and
	 * 	<code>target</code> are equal as specified, <code>false</code>
	 * 	otherwise.
	 *
	 * @throws UnifyException if an otherwise well-formed assignment is
	 * 	forbidden by the <code>blacklist</code>.
	 */
	public boolean checkVEquality(Expression source, Expression target, Set<Variable> blacklist) throws UnifyException;

	/**
	 * Like {@link #checkVEquality(Expression, Expression, Set)}, except
	 * that the <code>blacklist</code> is assumed to be empty.
	 *
	 * @param source source expression.
	 * @param target target expression.
	 *
	 * @return <code>true</code> if <code>source</code> and
	 * 	<code>target</code> are equal as specified, <code>false</code>
	 * 	otherwise.
	 *
	 * @see #checkVEquality(Expression, Expression, Set)
	 */
	public boolean checkVEquality(Expression source, Expression target);

	/**
	 * Returns the current internal assignment map of this
	 * <code>Matcher</code>.
	 *
	 * @return translation map of this matcher.
	 */
	public Map<Variable, Variable> getAssignmentMap();

}
