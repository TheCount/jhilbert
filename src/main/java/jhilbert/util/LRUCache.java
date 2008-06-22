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

package jhilbert.util;

import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Least Recently Used (LRU) Cache.
 * Basically a hash map, but the least recently used entry is removed from the map
 * whenever a new entry is added and the maximum number of entries has been reached.
 *
 * @param K map key type.
 * @param V map value type.
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {

	/**
	 * Maximal number of entries.
	 */
	private final int maxSize;

	/**
	 * Creates a new cache with the number of entries.
	 *
	 * @param numEntries number of cache entries.
	 *
	 * @throws IllegalArgumentException if number of entries is negative.
	 */
	public LRUCache(final int numEntries) throws IllegalArgumentException {
		super(4 * (numEntries + 1) / 3 + 1, 0.75f, true);
		if (numEntries < 0)
			throw new IllegalArgumentException("Negative number of entries.");
		maxSize = numEntries;
	}

	protected @Override boolean removeEldestEntry(final Map.Entry<K,V> eldest) {
		return size() > maxSize;
	}

}
