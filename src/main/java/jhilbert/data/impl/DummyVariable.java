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

import jhilbert.data.Kind;
import jhilbert.data.Variable;
import jhilbert.data.impl.DataImpl;
import jhilbert.data.impl.VariableImpl;

/**
 * Dummy variable.
 * Dummy variables are never equal to non-dummy variables.
 */
class DummyVariable extends VariableImpl {

	/**
	 * Serialization ID.
	 */
	private static final long serialVersionUID = DataImpl.FORMAT_VERSION;

	/**
	 * Internal id of this dummy.
	 */
	private static int id = 0;

	/**
	 * Creates a new dummy variable with the specified kind.
	 * This dummy variable will be unequal to all other currently existing variables.
	 *
	 * @param kind kind of dummy variable.
	 */
	public DummyVariable(final Kind kind) {
		super("(dummy" + id + ")", kind);
		++id;
	}

	/**
	 * Creates an uninitialized dummy variable.
	 * Used by serialization.
	 */
	public DummyVariable() {
		super();
	}

}
