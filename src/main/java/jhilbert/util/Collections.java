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
import java.util.Map;

/**
 * Class with static methods operating on collections.
 */
public class Collections {

	protected Collections() {} // no instantiation

	/**
	 * Clones elements from the source collection into the destination collection.
	 * The generic type <code>T</code> must declare a public clone method which
	 * returns an object whose type extends <code>T</code>.
	 *
	 * @param dest destination collection.
	 * @param src source collection.
	 */
	// FIXME
	//public static <T extends Cloneable> void clone(final Collection<? super T> dest,
	//		final Collection<? extends T> src) {
	//	assert (dest != null): "Supplied destination is null.";
	//	assert (src != null): "Supplied source is null.";
	//	for (final T e: src)
	//		dest.add((T) e.getClass().getMethod("clone").invoke(e));
	//}

	/**
	 * Clones mappings from the source mapping into the destination mapping.
	 * Only values are cloned. Existing mappings in the destination map are overwritten.
	 * The value type must implement a public clone method which returns a type
	 * which extends the value type.
	 *
	 * @param dest destination map.
	 * @param src source map.
	 */
	// FIXME
	//public static <K, V extends Cloneable> void clone(final Map<? super K, ? super V> dest,
	//		final Map<? extends K, ? extends V> src) {
	//	assert (dest != null): "Supplied destination is null.";
	//	assert (src != null): "Supplied source is null.";
	//	for (final Map.Entry<? extends K, ? extends V> entry: src.entrySet()) {
	//		final V value = entry.getValue();
	//		dest.put(entry.getKey(), (V) value.getClass().getMethod("clone").invoke(value));
	//	}
	//}

}
