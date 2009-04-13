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

import jhilbert.data.Name;
import jhilbert.data.Namespace;

/**
 * Basic implementation of {@link Name}.
 */
abstract class AbstractName implements Name, Serializable {

	/**
	 * Serialisation ID.
	 */
	private static final long serialVersionUID = jhilbert.Main.VERSION;

	/**
	 * Name.
	 */
	private final String name;

	/**
	 * Parameter index.
	 */
	private final int parameterIndex;

	/**
	 * Default constructor, for serialisation use only!
	 */
	public AbstractName() {
		name = null;
		parameterIndex = -1;
	}

	/**
	 * Creates a new name with default parameter index.
	 *
	 * @param name the name.
	 */
	AbstractName(final String name) {
		this(name, -1);
	}

	/**
	 * Creates a new name.
	 *
	 * @param name the name.
	 * @param parameterIndex the parameter index
	 * 	(which may be <code>-1</code>).
	 */
	AbstractName(final String name, final int parameterIndex) {
		assert (name != null): "Supplied name is null";
		assert (parameterIndex >= -1): "Invalid parameter index";
		this.name = name;
		this.parameterIndex = parameterIndex;
	}

	public String getNameString() {
		return name;
	}

	public abstract Namespace<? extends Name> getNamespace();

	public abstract AbstractName getOriginalName();

	public int getParameterIndex() {
		return parameterIndex;
	}

	public @Override int hashCode() {
		return name.hashCode();
	}

	/**
	 * Two <code>Name</code>s are equal, if they are the same, or if they
	 * are registered in a {@link Namespace} and compare equal acording to
	 * the namespace.
	 * In particular, this method is <em>not</em> quite the same as
	 * {@link Namespace#checkEquality}.
	 * Strictly speaking, this method intentionally violates the
	 * {@link Object#equals}/{@link Object.hashMap} contract. See the
	 * package description for details.
	 *
	 * @param o object to compare this name with.
	 */
	public @Override boolean equals(final Object o) {
		if (this == o)
			return true;
		try {
			return getNamespace().checkEquality(this, (Name) o);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Sets the namespace this <code>AbstractName</code> belongs to.
	 * This should be called by a <code>NamespaceImpl</code>.
	 *
	 * @param namespace the namespace.
	 */
	abstract void setNamespace(NamespaceImpl<? extends AbstractName> namespace);

	public @Override String toString() {
		return getNameString();
	}

}
