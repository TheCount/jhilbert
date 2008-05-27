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

import java.util.HashMap;
import java.util.Map;
import jhilbert.data.Name;

/**
 * Name to be used as unique identifier in a namespace.
 * FIXME: enhance this description.
 */
public abstract class AbstractName implements Name {

	/**
	 * Name.
	 */
	private String name;

	/**
	 * Create a new AbstractName.
	 *
	 * @param name the Name.
	 */
	protected AbstractName(final String name) {
		assert (name != null): "Supplied name is null.";
		this.name = name;
	}

	/**
	 * Returns this name as a String.
	 *
	 * @return name as a String.
	 *
	 * @see #toString()
	 */
	public final String getName() {
		return name;
	}

	public final void prefix(final String prefix) {
		name = prefix + name;
	}

	/**
	 * Returns a String representation of this name.
	 * This is an alias for {@link #getName()}.
	 *
	 * @see #getName()
	 */
	public final String toString() {
		return name;
	}

	public int hashCode() {
		return name.hashCode();
	}

	public boolean equals(final Name n) {
		return name.equals(n.getName());
	}

	public int compareTo(final Name n) {
		return name.compareTo(n.getName());
	}

	public abstract AbstractName clone();

}
