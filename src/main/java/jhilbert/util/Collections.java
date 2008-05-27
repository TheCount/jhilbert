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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Class with static methods extending the functionality of {@link java.util.Collections}.
 */
public class Collections extends java.util.Collections {

	/**
	 * Clones elements from the source collection into the destination collection.
	 *
	 * @param dest destination collection.
	 * @param src source collection.
	 */
	public static <T extends Cloneable> void clone(final Collection<? super T> dest,
			final Collection<? extends T> src) {
		assert (dest != null): "Supplied destination is null.";
		assert (src != null): "Supplied source is null.";
		for (final T e: src)
			dest.add(e.clone());
	}

	/**
	 * Clones mappings from the source mapping into the destination mapping.
	 * Only values are cloned. Existing mappings in the destination map are overwritten.
	 *
	 * @param dest destination map.
	 * @param src source map.
	 */
	public static <K, V extends Cloneable> void clone(final Map<? super K, ? super V> dest,
			final Map<? extends K, ? extends V> src) {
		assert (dest != null): "Supplied destination is null.";
		assert (src != null): "Supplied source is null.";
		for (final Map.Entry<K, V> entry: src.entrySet())
			dest.put(entry.getKey(), entry.getValue().clone());
	}

}
