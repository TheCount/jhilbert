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

package jhilbert.data.impl;

import jhilbert.data.Variable;
import jhilbert.data.VariablePair;
import jhilbert.util.Pair;

/**
 * A Pair of {@link Variable}s.
 * Variable Pairs are Cloneable and Comparable.
 */
class VariablePairImpl extends Pair<Variable, Variable> implements VariablePair, Comparable<VariablePair> {

	/**
	 * Serialization ID.
	 */
	private static final long serialVersionUID = jhilbert.Main.VERSION;

	/**
	 * Creates a new VariablePair.
	 *
	 * @param first first Variable.
	 * @param second second Variable.
	 *
	 * @throws NullPointerException if one of the parameters is <code>null</code>.
	 */
	public VariablePairImpl(final Variable first, final Variable second) {
		super(first, second);
	}

	/**
	 * Creates an uninitalized variable pair.
	 * Used by serialization.
	 */
	public VariablePairImpl() {
		super();
	}

	/**
	 * Returns a new VariablePair with reversed values.
	 *
	 * @return new VariablePair with reversed values.
	 */
	public VariablePairImpl reverse() {
		return new VariablePairImpl(getSecond(), getFirst());
	}

	/**
	 * Implements lexicographic order as natural order.
	 *
	 * @param p the VariablePair this object should be compared with.
	 *
	 * @return an integer less than, equal to, or greater than zero as this object is less than, equal to, or greater than the specified object.
	 */
	public int compareTo(final VariablePair p) {
		final int firstComp = getFirst().compareTo(p.getFirst());
		if (firstComp != 0)
			return firstComp;
		return getSecond().compareTo(p.getSecond());
	}

}
