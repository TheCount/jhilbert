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

package jhilbert.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * An interface for storing {@link Name}s.
 *
 * @param E type of stored names.
 */
public interface Namespace<E extends Name> extends Serializable {

	/**
	 * Obtains the {@link Module} this <code>Namespace</code> belongs to.
	 *
	 * @return {@link Module} this <code>Namespace</code> belongs to.
	 */
	public Module getModule();

	/**
	 * Registers the specified {@link Name}-derived object with this
	 * namespace.
	 * If the same name has already been registered, a
	 * {@link DataException} is thrown. &quot;Same&quot; here means
	 * that the {@link Name#getNameString} method returns a string which
	 * is equal to the name string of a previously registered name.
	 *
	 * @param o object to register. Must be convertible to <code>E</code>.
	 * 
	 * @throws DataException if the same name has already been registered.
	 */
	public void registerObject(Name o) throws DataException;

	/**
	 * Obtains a previously registered object by its name string.
	 *
	 * @param name name string.
	 *
	 * @return the object which was previously registered under the
	 * 	specified name, or <code>null</code> if no object with the
	 * 	specified name has been registered with this namespace.
	 *
	 * @see #registerObject
	 */
	public E getObjectByString(String name);

	/**
	 * Checks the two specified {@link Name}-derived objects for equality.
	 * <p>
	 * <strong>Warning:</strong> This is <em>not</em> the same as calling
	 * {@link java.lang.Object#equals} on one of the objects with the other
	 * as parameter.
	 * <p>
	 * If both objects are registered with this namespace, they are
	 * considered equal if they are the same, or if they have been
	 * identified in this namespace.
	 * <p>
	 * If only one of the objects is registered with this namespace, they
	 * are not considered equal.
	 * <p>
	 * If <em>none</em> of the objects are registered with this namespace,
	 * an exception is thrown.
	 *
	 * @param obj1 first object to be checked for equality.
	 * @param obj2 second object to be checked for equality.
	 *
	 * @return <code>true</code> if <code>obj1</code> and <code>obj2</code>
	 * 	are equal in the sense described above, <code>false</code>
	 * 	otherwise.
	 *
	 * @throws DataException if none of the two parameters is registered
	 * 	with this namespace.
	 *
	 * @see #identify
	 */
	public boolean checkEquality(Name obj1, Name obj2) throws DataException;

	/**
	 * Creates an alias for a previously registered object. The object
	 * can then be retrieved by {@link #getObjectByString} using the
	 * alias.
	 *
	 * @param obj registered object.
	 * @param name new name for the object <code>obj</code>.
	 *
	 * @throws DataException if <code>obj</code> has not been registered
	 * 	with this <code>Namespace</code>, or if <code>name</code> has
	 * 	already been created as an alias, or is the name of another
	 * 	registered object.
	 *
	 * @see #registerObject
	 * @see #getObjectByString
	 */
	public void createAlias(Name obj, String name) throws DataException;

	/**
	 * Identifies two previously registered objects with each other.
	 * A call of {@link #checkEquality} on the specified objects will
	 * subsequently return <code>true</code>.
	 *
	 * @param obj1 first object
	 * @param obj2 second object
	 *
	 * @throws DataException if one of the objects has not been registered
	 * 	with this namespace.
	 *
	 * @see #checkEquality
	 */
	public void identify(Name obj1, Name obj2) throws DataException;

	/**
	 * Returns a view of the objects in this <code>Namespace</code>.
	 * This includes identified, but not aliased entries. Use
	 * {@link #equivalenceClasses} to obtain equivalence classes.
	 *
	 * @return view of the objects in this namespace. The returned
	 * 	collection does not contain duplicate entries. The collection's
	 * 	iteration order corresponds to the order in which the objects
	 * 	were registered.
	 *
	 * @see #equivalenceClasses
	 * @see #createAlias
	 * @see #identify
	 */
	public Collection<E> objects();

	/**
	 * Returns a view of the aliases in this <code>Namespace</code>.
	 *
	 * @return view of the aliases in this namespace.
	 */
	public Map<String, E> aliases();

	/**
	 * Returns a view of all equivalence classes in this
	 * <code>Namespace</code> which contain more than one element.
	 *
	 * @return view of all equivalence classes in this namespace which
	 * 	contain more than element. The returned collection does not
	 * 	contain duplicate entries.
	 */
	public Collection<Set<E>> equivalenceClasses();

}
