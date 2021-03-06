/*
    JHilbert, a verifier for collaborative theorem proving

    Copyright © 2008, 2009, 2011 The JHilbert Authors
      See the AUTHORS file for the list of JHilbert authors.
      See the commit logs ("git log") for a list of individual contributions.

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
 * A dummy variable.
 * Dummy variables have the same properties as {@link UnnamedVariable}s in all
 * respects except that {@link DummyVariable#isDummy} returns
 * <code>true</code>.
 */
final class DummyVariable extends VariableImpl implements Serializable {

	/**
	 * Serialisation ID.
	 */
	private static final long serialVersionUID = jhilbert.Main.VERSION;

	/**
	 * Dummy ID.
	 */
	private static AtomicInteger id = new AtomicInteger();

	/**
	 * Default constructor, for serialisation use only!
	 */
	public DummyVariable() {
		super();
	}

	/**
	 * Creates a new <code>DummyVariable</code> with the specified
	 * {@link KindImpl}.
	 *
	 * @param kind kind of this dummy variable.
	 */
	DummyVariable(final KindImpl kind) {
		super("(dummy" + id.getAndIncrement() + ")", kind);
	}

	@Override final void setNamespace(final NamespaceImpl<? extends AbstractName> namespace) {
		throw new AssertionError("Attempt to set namespace for dummy variable");
	}

	/**
	 * @return <code>true</code>.
	 */
	public @Override boolean isDummy() {
		return true;
	}

}
