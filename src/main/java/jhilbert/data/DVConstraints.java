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

import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;
import jhilbert.data.Variable;
import jhilbert.data.VariablePair;
import jhilbert.exceptions.DataException;

/**
 * Interface for disjoint variable constraints.
 */
public interface DVConstraints extends Set<VariablePair> {

	/**
	 * Adds a set of mutually distinct variables.
	 *
	 * @param varSet set of variables.
	 *
	 * @return <code>true</code> if this set has been changed, <code>false</code> otherwise.
	 */
	 public boolean add(final SortedSet<Variable> varSet);

	/**
	 * Adds the cartesian product of the two specified sets.
	 *
	 * @param varSet1 first set of variables (must not be <code>null</code>).
	 * @param varSet2 second set of variables (must not be <code>null</code>).
	 *
	 * @throws DataException if the cartesian product of the two specified sets contains an element of the form (x, x).
	 */
	public void addProduct(final SortedSet<Variable> varSet1, final SortedSet<Variable> varSet2) throws DataException;

	/**
	 * Restricts these DVconstraints to those pairs of variables ocurring the specified collection.
	 *
	 * @param vars collection of variables to restrict these contraints to (must not be <code>null</code>).
	 */
	public void restrict(final Collection<Variable> vars);

}
