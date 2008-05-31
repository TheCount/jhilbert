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

/**
 * Name to be used as unique identifier in a namespace.
 */
public interface Name extends Comparable<Name> {

	/**
	 * Returns this name as a String.
	 *
	 * @return name as a String.
	 *
	 * @see #toString()
	 */
	// FIXME
	// public String getName();

	/**
	 * Returns this Name as a String.
	 *
	 * @return this Name as a String.
	 */
	public String toString();

	/**
	 * Checks whether this Name is equal to another object.
	 *
	 * @param o Object to be compared with this Name.
	 *
	 * @return <code>true</code> if and only if o is an instance of Name, and <code>getName().equals(o.toString())</code>.
	 */
	// FIXME
	// public boolean equals(final Object o);

	/**
	 * Returns a hash code for this name.
	 * Reimplemented so that equal names return equal hash codes.
	 */
	// FIXME
	// public int hashCode();

}
