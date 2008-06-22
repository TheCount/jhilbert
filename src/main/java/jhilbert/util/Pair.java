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

package jhilbert.util;

import java.io.Serializable;

/**
 * A pair (2-tuple) of values.
 * <code>null</code> values are not allowed.
 *
 * @param E1 type of first entry.
 * @param E2 type of second entry.
 */
public class Pair<E1, E2> implements Serializable {

	/**
	 * Serialization ID.
	 */
	private static final long serialVersionUID = jhilbert.Main.VERSION;

	/**
	 * First value.
	 */
	private E1 first;

	/**
	 * Second value.
	 */
	private E2 second;

	/**
	 * Creates a new Pair.
	 * The parameters must not be <code>null</code>.
	 *
	 * @param first first value.
	 * @param second second value.
	 */
	public Pair(final E1 first, final E2 second) {
		assert ((first != null) && (second != null)): "Null values are not allowed in Pairs.";
		this.first = first;
		this.second = second;
	}

	/**
	 * Creates an uninitialized pair.
	 * Used by serialization.
	 */
	public Pair() {
		first = null;
		second = null;
	}

	/**
	 * Copy constructor.
	 * Creates a shallow copy of this Pair.
	 *
	 * @param p Pair to be copied.
	 */
	public Pair(final Pair<E1, E2> p) {
		first = p.first;
		second = p.second;
	}

	/**
	 * Returns the first value.
	 *
	 * @return first value.
	 */
	public E1 getFirst() {
		return first;
	}

	/**
	 * Returns the second value.
	 *
	 * @return second value.
	 */
	public E2 getSecond() {
		return second;
	}

	public @Override int hashCode() {
		return first.hashCode() + (~second.hashCode());
	}

	public @Override boolean equals(final Object o) {
		Pair<E1, E2> p;
		try {
			p = (Pair<E1, E2>) o;
		} catch (ClassCastException e) {
			return false;
		}
		return (first.equals(p.first) && second.equals(p.second));
	}

	public @Override String toString() {
		return "(" + first.toString() + ", " + second.toString() + ")";
	}

}
