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

import jhilbert.data.impl.NameImpl;
import jhilbert.data.Kind;
import jhilbert.data.Variable;
import jhilbert.data.impl.DataImpl;

/**
 * Default implementation of the {@link Variable} interface.
 */
class VariableImpl extends NameImpl implements Variable {

	/**
	 * Serialization ID.
	 */
	private static final long serialVersionUID = DataImpl.FORMAT_VERSION;

	/**
	 * Kind of this variable.
	 */
	private Kind kind;

	/**
	 * Creates a new Variable with the specified name and kind.
	 *
	 * @param name name of this variable (must not be <code>null</code>).
	 * @param kind kind of this variable (must not be <code>null</code>).
	 */
	VariableImpl(final String name, final Kind kind) {
		super(name);
		assert (kind != null): "Supplied kind is null.";
		this.kind = kind;
	}

	/**
	 * Creates an uninitialized variable.
	 * Used by serialization.
	 */
	public VariableImpl() {
		super();
		kind = null;
	}

	/**
	 * Returns the kind of this variable.
	 *
	 * @return the kind of this variable.
	 */
	public Kind getKind() {
		return kind;
	}

	public final boolean isVariable() {
		return true;
	}

}
