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

import jhilbert.data.Kind;

/**
 * {@link Kind} implementation.
 */
final class KindImpl extends AbstractName implements Kind, Serializable {

	/**
	 * Serialisation ID.
	 */
	private static final long serialVersionUID = jhilbert.Main.VERSION;

	/**
	 * Original name.
	 */
	private final KindImpl originalName;

	/**
	 * Namespace.
	 */
	private NamespaceImpl<KindImpl> namespace;

	/**
	 * Default constructor, for serialisation use only!
	 */
	public KindImpl() {
		super();
		originalName = null;
		namespace = null;
	}

	/**
	 * Creates a new <code>KindImpl</code> with the specified name.
	 *
	 * @param name kind name.
	 */
	KindImpl(final String name) {
		this(name, null, -1);
	}

	/**
	 * Creates a new <code>KindImpl</code> with the specified name and
	 * original name.
	 *
	 * @param name kind name.
	 * @param orig original name, which may be <code>null</code> if this
	 * 	<code>KindImpl</code> is not derived from another one.
	 * @param parameterIndex index of parameter of <code>orig</code>.
	 */
	KindImpl(final String name, final KindImpl orig, final int parameterIndex) {
		super(name, parameterIndex);
		originalName = orig;
		namespace = null;
	}

	public final NamespaceImpl<KindImpl> getNamespace() {
		return namespace;
	}

	public KindImpl getOriginalName() {
		return originalName;
	}

	@Override void setNamespace(final NamespaceImpl<? extends AbstractName> namespace) {
		assert (namespace != null): "Supplied namespace is null";
		assert (this.namespace == null): "Attempt to alter namespace";
		try {
			this.namespace = (NamespaceImpl<KindImpl>) namespace;
		} catch (ClassCastException e) {
			throw new AssertionError("Wrong namespace");
		}
	}

}
