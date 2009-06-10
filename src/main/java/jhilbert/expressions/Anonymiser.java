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

import jhilbert.data.DVConstraints;
import jhilbert.data.Variable;

/**
 * An <code>Anonymiser</code> replaces {@link Variable}s in {@link Expression}s
 * with unnamed variables according to a replacement set which must be
 * provided to constructors of implementing classes.
 */
public interface Anonymiser {

	/**
	 * Returns an anonymised version of the specified variable.
	 * If the variable is not from the set of variables inherent to this
	 * <code>Anonymiser</code>, a dummy variable will be returned.
	 * Repeated calls to this method will result in consistent assignments
	 * of variables.
	 *
	 * @param var variable to anonymise.
	 *
	 * @return the anonymised variable.
	 */
	public Variable anonymise(Variable var);

	/**
	 * Anonymises the specified {@link DVConstraints} by calling
	 * {@link #anonymise(Variable)} on each variable in the constraints.
	 *
	 * @param dv disjoint variable constraints.
	 *
	 * @return anonymised disjoint variable constraints.
	 */
	public DVConstraints anonymise(DVConstraints dv);

	/**
	 * Anonymises the specified {@link Expression} by performing a
	 * well-formed substitution of {@link Variable}s from a set of
	 * variables inherent to this <code>Anonymiser</code> with unnamed
	 * variables.
	 * All other variables encountered are well-formedly substituted with
	 * dummy variables.
	 *
	 * @param expr expression to anonymise.
	 *
	 * @return the anonymised expression.
	 */
	public Expression anonymise(Expression expr);

}
