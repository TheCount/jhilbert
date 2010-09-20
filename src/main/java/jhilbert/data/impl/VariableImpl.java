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

package jhilbert.data.impl;

import java.io.Serializable;

import jhilbert.data.Variable;

/**
 * Implementation of the {@link Variable} interface.
 */
class VariableImpl extends SymbolImpl implements Variable, Serializable {

	/**
	 * Serialisation ID.
	 */
	private static final long serialVersionUID = jhilbert.Main.VERSION;

	/**
	 * Kind of this variable.
	 */
	private final KindImpl kind;

	/**
	 * Default constructor, for serialisation use only!
	 */
	public VariableImpl() {
		super();
		kind = null;
	}

	/**
	 * Creates a new <code>VariableImpl</code> with the specified name
	 * and the specified {@link KindImpl}.
	 *
	 * @param name name of this variable.
	 * @param kind kind of this variable.
	 */
	VariableImpl(final String name, final KindImpl kind) {
		super(name, null, -1);
		assert (kind != null): "Supplied kind is null";
		this.kind = kind;
	}

	public final KindImpl getKind() {
		return kind;
	}

	public final boolean isVariable() {
		return true;
	}

	/**
	 * Default implementation of {@link Variable#isDummy} always returns
	 * <code>false</code>.
	 *
	 * @return <code>false</code>.
	 */
	public boolean isDummy() {
		return false;
	}

}
