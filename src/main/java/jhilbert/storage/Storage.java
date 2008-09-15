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

package jhilbert.storage;

import java.util.Collections;
import java.util.Map;

import jhilbert.data.Module;

import jhilbert.utils.LRUCache;

/**
 * Facility for {@link Module} data storage.
 */
public abstract class Storage {

	/**
	 * Instance.
	 */
	private static final Storage instance;

	/**
	 * Choose instance.
	 */
	static {
		// Right now there is only file based storage.
		// In the future, the storage method should be selectable by a command line switch
		instance = new jhilbert.storage.file.Storage();
	}

	/**
	 * Default cache size.
	 */
	private static final int DEFAULT_CACHE_SIZE = 50;

	/**
	 * Returns a <code>Storage</code> instance.
	 */
	public static Storage getInstance() {
		return instance;
	}

	/**
	 * Module cache.
	 */
	private final Map<String, Module> moduleCache;

	/**
	 * Creates a new <code>Storage</code> with a cache size of
	 * <code>50</code>.
	 */
	protected Storage() {
		this(DEFAULT_CACHE_SIZE);
	}

	/**
	 * Creates a new <code>Storage</code> with the specified cache size.
	 *
	 * @param size number of {@link Module} cache entries.
	 */
	protected Storage(final int size) {
		assert (size >= 0): "Supplied size is negative";
		moduleCache = Collections.synchronizedMap(new LRUCache(size));
	}
	
	/**
	 * Returns whether this <code>Storage</code> is a versioned storage.
	 *
	 * @return <code>true</code> if this is a versioned storage,
	 * 	<code>false</code> otherwise.
	 */
	public abstract boolean isVersioned();

	/**
	 * Loads the specified module with the specified revision.
	 *
	 * @param locator module name.
	 * @param version revision number, or <code>-1</code> for the most
	 * 	recent revision or if the module is unversioned.
	 *
	 * @return the module as specified.
	 *
	 * @throws StorageException if the module cannot be loaded, or a module
	 * 	with the specified revision number does not exist.
	 */
	protected abstract Module retrieveModule(final String locator, final long version) throws StorageException;

	/**
	 * Loads the specified module with the specified revision from cache.
	 * Falls back to storage if the module is not cached.
	 *
	 * @param locator module name.
	 * @param version revision number, or <code>-1</code>, if the module is
	 * 	unversioned.
	 *
	 * @return the module as specified.
	 *
	 * @throws StorageException if the module cannot be loaded, or a module
	 * 	with the specified revision number does not exist.
	 */
	public final Module loadModule(final String locator, final long version) throws StorageException {
		assert (locator != null): "Supplied locator is null";
		assert (!"".equals(locator)): "Proof modules cannot be loaded";
		assert (version >= -1): "Invalid version number supplied";
		Module result = moduleCache.get(locator);
		if ((result != null) && (result.getRevision() == version))
			return result;
		result = retrieveModule(locator, version);
		assert (result != null): "Implementation returned null module";
		moduleCache.put(locator, result);
		return result;
	}

	/**
	 * Loads the most recent (or only, if unversioned) revision of the
	 * specified module from cache.
	 * Falls back to storage if the module is not cached.
	 *
	 * @param locator module name.
	 *
	 * @return the module as specified.
	 *
	 * @throws StorageException if the module cannot be loaded.
	 */
	public final Module loadModule(final String locator) throws StorageException {
		return loadModule(locator, -1);
	}

}
