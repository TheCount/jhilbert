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

package jhilbert.storage;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jhilbert.data.Module;
import jhilbert.utils.AutoCache;

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
		try {
			if (jhilbert.Main.getHashstorePath() != null) {
				instance = new jhilbert.storage.hashstore.Storage();
			} else if (jhilbert.Main.isDaemon()) {
				instance = new jhilbert.storage.mediawiki.Storage();
			} else if (jhilbert.Main.isWiki()) {
				instance = new jhilbert.storage.wiki.Storage();
			} else {
				instance = new jhilbert.storage.file.Storage();
			}
		} catch (StorageException e) {
			throw new RuntimeException("StorageException in static Storage initializer", e);
		}
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
	private final Map<ModuleID, Module> moduleCache;

	/**
	 * Set of currently loading modules.
	 * Used to track the loading of modules in order to avoid circularity
	 * errors.
	 */
	private final Set<ModuleID> loadingModules;

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
		moduleCache = Collections.synchronizedMap(new AutoCache(size));
		loadingModules = Collections.synchronizedSet(new HashSet());
	}
	
	/**
	 * Returns whether this <code>Storage</code> is a versioned storage.
	 *
	 * @return <code>true</code> if this is a versioned storage,
	 * 	<code>false</code> otherwise.
	 */
	public abstract boolean isVersioned();

	/**
	 * Obtains the canonical name for the specified locator.
	 * Some storages may restrict the size of the locator namespace and
	 * automatically transform non-matching names to an equivalent
	 * canonical form.
	 *
	 * @param locator locator.
	 *
	 * @return the canonical version of the specified locator.
	 *
	 * @throws StorageException if the canonical name cannot be derived.
	 */
	protected abstract String getCanonicalName(String locator) throws StorageException;

	/**
	 * Obtains the most recent revision of the module with the specified
	 * locator.
	 *
	 * @param locator canonical module name.
	 *
	 * @return current revision number, or <code>-1</code> if the storage
	 * 	is unversioned.
	 *
	 * @throws StorageException may be thrown if there is no such module.
	 * 	The implementation is not required to throw an exception.
	 * 	For example, unversioned storages may simply return
	 * 	<code>-1</code> without throwing an exception.
	 */
	protected abstract long getCurrentRevision(String locator) throws StorageException;

	/**
	 * Loads the specified module with the specified revision.
	 *
	 * @param locator canonical module name.
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
	public final Module loadModule(String locator, long version) throws StorageException {
		assert (locator != null): "Supplied locator is null";
		assert (!"".equals(locator)): "Proof modules cannot be loaded";
		assert (version >= -1): "Invalid version number supplied";
		locator = getCanonicalName(locator);
		if (version == -1)
			version = getCurrentRevision(locator);
		final ModuleID id = new ModuleID(locator, version);
		Module result = moduleCache.get(id);
		if (result != null)
			return result;
		synchronized (loadingModules) {
			if (loadingModules.contains(id))
				throw new StorageException("Requested module is currently being loaded. "
						+ "This usually indicates a circular parameter dependence.");
			loadingModules.add(id);
		}
		try {
			result = retrieveModule(locator, version);
			assert (result != null): "Implementation returned null module";
			moduleCache.put(id, result);
		} finally {
			loadingModules.remove(id);
		}
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

	/**
	 * Stores the specified module at the specified locator with the
	 * specified revision.
	 *
	 * @param module data module to store.
	 * @param locator canonical module name.
	 * @param version revision number, or <code>-1</code> if the module
	 * 	is unversioned.
	 *
	 * @throws StorageException if the module cannot be stored.
	 */
	protected abstract void storeModule(Module module, String locator, long version) throws StorageException;

	/**
	 * Saves the specified module at the specified locator with the
	 * specified revision, updating the cache.
	 *
	 * @param module data module to store.
	 * @param locator module name.
	 * @param version revision number, or <code>-1</code> if the module
	 * 	is unversioned.
	 *
	 * @throws StorageException if the module cannot be saved.
	 */
	public final void saveModule(final Module module, String locator, long version) throws StorageException {
		assert (module != null): "Supplied module is null";
		assert (locator != null): "Supplied locator is null";
		assert (!"".equals(locator)): "Proof modules cannot be saved";
		assert (version >= -1): "Invalid version number supplied";
		locator = getCanonicalName(locator);
		storeModule(module, locator, version);
		final ModuleID id = new ModuleID(module);
		synchronized (moduleCache) {
			// update only if already in cache
			if (moduleCache.containsKey(id))
				moduleCache.put(id, module);
		}
	}

	/**
	 * Erases the module at the specified locator with the specified
	 * revision from storage.
	 * Does nothing if the specified module is not in storage.
	 *
	 * @param locator canonical module name.
	 * @param version revision number, or <code>-1</code> if the module
	 * 	is unversioned.
	 *
	 * @throws StorageException if the module cannot be erased.
	 */
	protected abstract void eraseModule(String locator, long version) throws StorageException;

	/**
	 * Deletes the module at the specified locator with the specified
	 * revision from cache and possibly erases it from storage.
	 * Does nothing if the module is neither cached nor in storage.
	 *
	 * @param locator module name.
	 * @param version revision number, or <code>-1</code> if the module
	 * 	is unversioned.
	 *
	 * @throws StorageException if the module cannot be deleted.
	 */
	public final void deleteModule(String locator, final long version) throws StorageException {
		assert (locator != null): "Specified locator is null";
		assert (version >= -1): "Invalid version number supplied";
		locator = getCanonicalName(locator);
		eraseModule(locator, version);
		moduleCache.remove(new ModuleID(locator, version));
	}

}
