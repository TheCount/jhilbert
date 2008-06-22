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

package jhilbert.data;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import jhilbert.data.Kind;
import jhilbert.data.UnifyException;
import jhilbert.data.Variable;

/**
 * A term expression.
 */
public interface TermExpression {

	/**
	 * Returns the kind of this TermExpression.
	 *
	 * @return kind of this TermExpression.
	 */
	public Kind getKind();

	/**
	 * Returns the node value of this term expression.
	 */
	public Term getValue();

	/**
	 * Returns the variables occurring in this TermExpression, in order of first occurrence
	 * when the term is read from left to right.
	 *
	 * @return ordered set of mutually different variables occurring in this TermExpression.
	 */
	public LinkedHashSet<Variable> variables();

	/**
	 * Returns a TermExpression with the specified variable assignments.
	 * This TermExpression will not be altered.
	 *
	 * @param varAssignments variable assignments to apply (must not be <code>null</code>).
	 *
	 * @return TermExpression with the specified variable assignments applied.
	 */
	public TermExpression subst(final Map<Variable, ? extends TermExpression> varAssignments);

	/**
	 * Matches the target against this TermExpression.
	 * Here, dummy variables may be bound to non-dummy variables,
	 * as long as these non-dummy variables are not on the specified blacklist.
	 *
	 * @param target target expression to match against (must not be <code>null</code>).
	 * @param blacklist list of variables that may not be bound to dummies (must not be <code>null</code>).
	 *
	 * @return <code>true</code> if this term expression matches the target, <code>false</code> otherwise.
	 */
	public boolean dummyMatches(final TermExpression target, final Set<Variable> blacklist);

	/**
	 * Attempts to unify this TermExpression with the specified target.
	 * This TermExpression will not be altered.
	 * Instead, variable to term expression mappings are provided.
	 * If this method returns successfully, the target can be generated by
	 * calling {@link #subst} with the resulting variable mapping.
	 *
	 * @param target target expression of unification (must not be <code>null</code>).
	 * @param varMap variable to term expression mapping. This parameter may be altered by this method
	 * 	(must not be <code>null</code>).
	 *
	 * @throws UnifyException if unification to the specified target is not possible.
	 */
	public <E extends TermExpression> void unify(final E target, final Map<Variable, E> varMap) throws UnifyException;

}
