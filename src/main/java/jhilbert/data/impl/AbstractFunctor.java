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

    You may contact the author on these Wiki pages:
    http://planetx.cc.vt.edu/AsteroidMeta//GrafZahl (preferred)
    http://en.wikisource.org/wiki/User_talk:GrafZahl
*/

package jhilbert.data.impl;

import java.io.Serializable;

import jhilbert.data.Functor;

/**
 * Skeletal implementation of the functor interface.
 */
abstract class AbstractFunctor extends AbstractName implements Functor, Serializable {

	/**
	 * Serialisation ID.
	 */
	private static final long serialVersionUID = jhilbert.Main.VERSION;

	/**
	 * Original name.
	 */
	private final AbstractFunctor originalName;

	/**
	 * Namespace.
	 */
	private NamespaceImpl<AbstractFunctor> namespace;

	/**
	 * Default constructor, for serialisation use only!
	 */
	public AbstractFunctor() {
		super();
		originalName = null;
		namespace = null;
	}

	/**
	 * Creates a new <code>AbstractFunctor</code> with the specified name.
	 *
	 * @param name name of new functor.
	 */
	AbstractFunctor(final String name) {
		this(name, null, -1);
	}

	/**
	 * Creates a new <code>AbstractFunctor</code> derived from the
	 * specified original name with the specified name.
	 *
	 * @param name name of new functor.
	 * @param orig original name, which may be <code>null</code> if this
	 * 	functor is not derived from another one.
	 * @param parameterIndex index of parameter of <code>orig</code>.
	 */
	AbstractFunctor(final String name, final AbstractFunctor orig, final int parameterIndex) {
		super(name, parameterIndex);
		originalName = orig;
		namespace = null;
	}

	public final NamespaceImpl<AbstractFunctor> getNamespace() {
		return namespace;
	}

	public final AbstractFunctor getOriginalName() {
		return originalName;
	}

	public final boolean isVariable() {
		return false;
	}

	@Override void setNamespace(final NamespaceImpl<? extends AbstractName> namespace) {
		assert (namespace != null): "Supplied namespace is null";
		assert (this.namespace == null): "Attemp to alter namespace";
		try {
			this.namespace = (NamespaceImpl<AbstractFunctor>) namespace;
		} catch (ClassCastException e) {
			throw new AssertionError("Wrong namespace");
		}
	}

}
