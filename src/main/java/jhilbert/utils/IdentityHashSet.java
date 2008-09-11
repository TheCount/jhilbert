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

package jhilbert.utils;

import java.io.Serializable;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;

/**
 * This class implements the {@link java.util.Set} interface using reference
 * equality when comparing elements. It complements (and is actually backed
 * by) the {@link IdentityHashMap} class. In particular, it intentionally
 * violates {@link java.util.Set}'s general contract in the same way as
 * {@link IdentityHashMap} violates {@link java.util.Map}'s.
 */
public class IdentityHashSet<E> extends AbstractSet<E> implements Serializable, Cloneable {

	/**
	 * Serialisation ID.
	 */
	private static final long serialVersionUID = jhilbert.Main.VERSION;

	/**
	 * Backing map.
	 */
	private final IdentityHashMap<E, Void> backingMap;

	/**
	 * Creates a new, empty <code>IdentityHashSet</code>.
	 */
	public IdentityHashSet() {
		super();
		backingMap = new IdentityHashMap();
	}

	/**
	 * Creates a new <code>IdentityHashSet</code> containing all elements
	 * of the specified {@link Collection}.
	 *
	 * @param c the collection whose elements are to be placed into this
	 * 	set.
	 *
	 * @throws NullPointerException if the specified collection is
	 * 	<code>null</code>.
	 */
	public IdentityHashSet(final Collection<? extends E> c) throws NullPointerException {
		super();
		backingMap = new IdentityHashMap();
		addAll(c);
	}

	public @Override int hashCode() {
		return backingMap.keySet().hashCode();
	}

	public @Override boolean add(final E e) {
		if (backingMap.containsKey(e))
			return false;
		backingMap.put(e, null);
		return true;
	}

	public @Override void clear() {
		backingMap.clear();
	}

	public @Override boolean contains(final Object o) {
		return backingMap.containsKey(o);
	}

	public @Override Iterator<E> iterator() {
		return backingMap.keySet().iterator();
	}
	
	public @Override boolean remove(final Object o) {
		final boolean result = backingMap.containsKey(o);
		backingMap.remove(o);
		return result;
	}

	public @Override int size() {
		return backingMap.size();
	}

	public @Override String toString() {
		return backingMap.keySet().toString();
	}

	public @Override IdentityHashSet<E> clone() {
		return new IdentityHashSet<E>(this);
	}

}
