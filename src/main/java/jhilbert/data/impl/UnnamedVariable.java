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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unnamed variable.
 * This class creates variables not part of any namespace which are unequal to
 * all other variables except themselves.
 */
final class UnnamedVariable extends VariableImpl implements Serializable {

	/**
	 * Serialisation ID.
	 */
	private static final long serialVersionUID = jhilbert.Main.VERSION;

	/**
	 * Variable ID.
	 */
	private static AtomicInteger id = new AtomicInteger();

	/**
	 * Default constructor, for serialisation use only!
	 */
	public UnnamedVariable() {
		super();
	}

	/**
	 * Creates a new <code>UnnamedVariable</code> with the specified
	 * {@link KindImpl}.
	 * This variable actually has an internal name, which is different from
	 * all others.
	 *
	 * @param kind kind of this unnamed variable.
	 */
	UnnamedVariable(final KindImpl kind) {
		super("(?" + id.getAndIncrement() + ")", kind);
	}

	@Override final void setNamespace(final NamespaceImpl<? extends AbstractName> namespace) {
		throw new AssertionError("Attempt to set namespace for unnamed variable");
	}

}
