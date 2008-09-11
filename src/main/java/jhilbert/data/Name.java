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

import java.io.Serializable;

/**
 * A named piece of data.
 */
public interface Name extends Serializable {

	/**
	 * Obtains the <code>Name</code> as a {@link java.lang.String}.
	 *
	 * @return this <code>Name</code> as a {@link java.lang.String}.
	 */
	public String getNameString();

	/**
	 * Obtains the {@link Namespace} with which this <code>Name</code>
	 * is registered.
	 *
	 * @return {@link Namespace} with which this name is
	 * 	registered, or <code>null</code> if this name has not yet been
	 * 	registered with a namespace.
	 */
	public Namespace<? extends Name> getNamespace();

	/**
	 * Obtains the original <code>Name</code> this name is derived from.
	 *
	 * @return the original <code>Name</code> this name is derived from,
	 * 	or <code>null</code> if this name is not derived from any
	 * 	name.
	 */
	public Name getOriginalName();

	/**
	 * Obtains the index of the {@link Parameter} of the {@link Module} of
	 * the {@link Namespace} of this <code>Name</code>.
	 * 
	 * @return parameter index of module of namespace of this name, or
	 * 	<code>-1</code> if this name is not derived from any name.
	 */
	public int getParameterIndex();

}
