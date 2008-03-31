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

import jhilbert.data.Variable;

/**
 * Dummy variable.
 * Dummy variables are never equal to non-dummy variables.
 */
public class DummyVariable extends Variable {

	/**
	 * Internal id of this dummy.
	 */
	private static int id;

	/**
	 * Initialize id with zero.
	 */
	static {
		id = 0;
	}

	/**
	 * Creates a new dummy variable with the specified kind.
	 * This dummy variable will be unequal to all other currently existing variables.
	 *
	 * @param kind kind of dummy variable.
	 *
	 * @throws NullPointerException if kind is <code>null</code>.
	 */
	public DummyVariable(final String kind) {
		super("(dummy" + id + ")", kind);
		++id;
	}

}
