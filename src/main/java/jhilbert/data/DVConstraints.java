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

import java.io.Serializable;

import java.util.Set;

/**
 * Essentially a collection of variable pairs (represented as
 * {@link Variable}[] of lenghth&nbsp;2).
 * However, this interface does not extend the {@link java.util.Collection}
 * interface.
 */
public interface DVConstraints extends Iterable<Variable[]>, Serializable {

	/**
	 * Adds the specified variables to these <code>DVConstraints</code>
	 * by adding each non-diagonal pair.
	 *
	 * @param vars variables to be added.
	 *
	 * @throws DataException if the same variable appears twice in the
	 * 	specified array.
	 */
	public void add(Variable... vars) throws DataException;

	/**
	 * Adds the cartesian product of the two specified sets of variables.
	 *
	 * @param varSet1 first set.
	 * @param varSet2 second set.
	 *
	 * @throws DataException if the two sets are not disjoint.
	 */
	public void addProduct(Set<Variable> varSet1, Set<Variable> varSet2) throws DataException;

	/**
	 * Checks whether the specified variable pair is contained in these
	 * <code>DVConstraints</code>.
	 *
	 * @param var1 first variable.
	 * @param var2 second variable.
	 *
	 * @return <code>true</code> if {<code>var1</code>, <code>var2</code>}
	 * 	is contained in these constraints, <code>false</code>
	 * 	otherwise.
	 */
	public boolean contains(Variable var1, Variable var2);

	/**
	 * Checks whther the specified DV constraints are contained in these
	 * DV constraints.
	 *
	 * @param dv other disjoint variable constraints.
	 *
	 * @return <code>true</code> if <code>dv</code> is contained in these
	 * 	constraints, <code>false</code> otherwise.
	 */
	public boolean contains(DVConstraints dv);

	/**
	 * Removes all variable pairs from these <code>DVConstraints</code>
	 * whose elements are not both contained in the specified set.
	 *
	 * @param varSet variable set to restrict these constraints to.
	 */
	public void restrict(Set<Variable> varSet);

}
