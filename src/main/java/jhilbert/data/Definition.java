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

package jhilbert.data;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jhilbert.expressions.Expression;

/**
 * A <code>Definition</code>.
 * See the JHilbert specification for more information.
 */
public interface Definition extends Functor {

	/**
	 * Returns the argument variables of this <code>Definition</code>.
	 *
	 * @return argument variables of this definition.
	 */
	public LinkedHashSet<Variable> getArguments();

	/**
	 * Returns the definiens of this <code>Definition</code>.
	 * The definiens is an {@link Expression} consisting either of a single
	 * {@link Variable}, or its leading {@link Functor} has a definition
	 * depth smaller than this definition.
	 * <p>
	 * Any variables occurring in the definiens, but not in the set
	 * returned by {@link #getArguments} are dummy variables.
	 *
	 * @return the definiens.
	 *
	 * @see Variable#isDummy
	 */
	public Expression getDefiniens();

	/**
	 * Returns the accumulated DV constraints of this
	 * <code>Definition</code>.
	 *
	 * These are the DV constraints specified by the user, plus the
	 * constraints accumulated through the definiens.
	 *
	 * @return accumulated DV constraints
	 */
	public DVConstraints getDVConstraints();

	/**
	 * Returns the accumulated dummy variables appearing in this
	 * definition. (That is, the dummy variables from the definiens plus
	 * the new dummy variables.)
	 *
	 * @return accumulated dummy variables.
	 *
	 * @see Variable#isDummy
	 */
	public Set<Variable> getDummyVariables();

	/**
	 * Unfolds this <code>Definition</code> with the specified list of
	 * {@link Expression}s. The specified list of expressions must match
	 * size and kinds of the arguments.
	 *
	 * @param exprList list of expressions.
	 *
	 * @return unfolded definition.
	 *
	 * @see #getArguments
	 */
	public Expression unfold(List<Expression> exprList);

}
