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

package jhilbert.utils;

import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Automatic cache.
 * A hash map whose entries are removed on memory pressure if
 * the <em>values</em> are no longer strongly reachable.
 * This is in contrast to the {@link java.util.WeakHashMap} in which the keys
 * only have to be no longer strongly reachable to be removed.
 *
 * The cache structure is very simple. Basically, the JVM garbage collector
 * decides in which order entries are removed.
 */
public class AutoCache<K, V> extends AbstractMap<K, V> implements Map<K, V>, Cloneable, Serializable {

	/**
	 * Private set class for entry set view.
	 */
	private static class EntrySet<K, V> extends AbstractSet<Map.Entry<K, V>>
	implements Set<Map.Entry<K, V>> {

		/**
		 * Iterator class for this entry set.
		 */
		private static class EntrySetIterator<K, V> implements Iterator<Map.Entry<K, V>> {

			/**
			 * Backing iterator.
			 */
			private final Iterator<Map.Entry<K, SoftReference<V>>> backingIterator;

			/**
			 * Constructor.
			 *
			 * @param backingIterator backing iterator.
			 */
			EntrySetIterator(final Iterator<Map.Entry<K, SoftReference<V>>> backingIterator) {
				this.backingIterator = backingIterator;
			}

			public boolean hasNext() {
				return backingIterator.hasNext();
			}

			public Map.Entry<K, V> next() {
				final Map.Entry<K, SoftReference<V>> next = backingIterator.next();
				return new AbstractMap.SimpleEntry(next.getKey(), next.getValue().get());
			}

			public void remove() {
				backingIterator.remove();
			}

		}

		/**
		 * Backing entry set.
		 */
		private final Set<Map.Entry<K, SoftReference<V>>> backingSet;

		/**
		 * Constructor.
		 *
		 * @param backingSet backing set.
		 */
		EntrySet(final Set<Map.Entry<K, SoftReference<V>>> backingSet) {
			this.backingSet = backingSet;
		}

		public @Override Iterator<Map.Entry<K, V>> iterator() {
			return new EntrySetIterator(backingSet.iterator());
		}

		public @Override int size() {
			return backingSet.size();
		}

	}

	/**
	 * Serialization ID.
	 */
	private static final long serialVersionUID = jhilbert.Main.VERSION;

	/**
	 * Backing map.
	 */
	private final Map<K, SoftReference<V>> backingMap;

	/**
	 * Default initial capacity.
	 */
	private static final int DEFAULT_CAPACITY = 16;

	/**
	 * Constructs an empty <code>AutoCache</code> with the default initial
	 * capacity (16).
	 */
	public AutoCache() {
		this(DEFAULT_CAPACITY);
	}

	/**
	 * Constructs an empty <code>AutoCache</code> with the specified
	 * initial capacity.
	 *
	 * @param capacity initial capacity.
	 */
	public AutoCache(final int capacity) {
		backingMap = new HashMap(capacity);
	}

	/**
	 * Private copy constructor for cloning.
	 *
	 * @param backingMap backing map to be cloned.
	 */
	private AutoCache(final Map<K, SoftReference<V>> backingMap) {
		this.backingMap = new HashMap(backingMap);
	}

	/**
	 * Clean up collected entries.
	 */
	private void cleanup() {
		final Iterator<Map.Entry<K, SoftReference<V>>> i = backingMap.entrySet().iterator();
		while (i.hasNext()) {
			if (i.next().getValue().get() == null)
				i.remove();
		}
	}

	public @Override void clear() {
		backingMap.clear();
	}

	public @Override boolean containsKey(final Object key) {
		final SoftReference<V> ref = backingMap.get(key);
		if (ref == null)
			return false;
		if (ref.get() == null) {
			cleanup();
			return false;
		}
		return true;
	}

	public @Override Set<Map.Entry<K, V>> entrySet() {
		return new EntrySet(backingMap.entrySet());
	}

	public @Override V get(final Object key) {
		final SoftReference<V> ref = backingMap.get(key);
		if (ref == null)
			return null;
		final V value = ref.get();
		if (value == null)
			cleanup();
		return value;
	}

	public @Override boolean isEmpty() {
		return backingMap.isEmpty();
	}

	public @Override V put(final K key, final V value) {
		final SoftReference<V> result = backingMap.put(key, new SoftReference(value));
		if (result == null)
			return null;
		return result.get();
	}

	public @Override V remove(final Object key) {
		final V result = get(key);
		if (result != null)
			backingMap.remove(key);
		return result;
	}

	public @Override int size() {
		return backingMap.size();
	}

	public @Override AutoCache<K, V> clone() {
		return new AutoCache(backingMap);
	}

}
