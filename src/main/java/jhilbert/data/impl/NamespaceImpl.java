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

package jhilbert.data.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import jhilbert.data.DataException;
import jhilbert.data.Name;
import jhilbert.data.Namespace;
import jhilbert.utils.IdentityHashSet;

import org.apache.log4j.Logger;

/**
 * {@link Namespace} implementation.
 *
 * @param E type of stored names.
 */
final class NamespaceImpl<E extends AbstractName> implements Namespace<E>, Serializable {

	/**
	 * Serialisation ID.
	 */
	private static final long serialVersionUID = jhilbert.Main.VERSION;

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(NamespaceImpl.class);

	/**
	 * Module this <code>NamespaceImpl</code> belongs to.
	 */
	private final ModuleImpl module;

	/**
	 * Object registry.
	 * This map must be one-to-one.
	 */
	private final LinkedHashMap<String, E> registry;

	/**
	 * Aliases.
	 */
	private final Map<String, E> aliases;

	/**
	 * Identified objects structure.
	 */
	private final Map<String, Set<E>> idObjects;

	/**
	 * Default constructor, for serialisation use only!
	 */
	public NamespaceImpl() {
		module = null;
		registry = null;
		aliases = null;
		idObjects = null;
	}

	/**
	 * Creates a new <code>NamespaceImpl</code> belonging to the specified
	 * {@link ModuleImpl}.
	 *
	 * @param module {@link Module} this <code>NamespaceImpl</code> belongs
	 * 	to.
	 */
	NamespaceImpl(final ModuleImpl module) {
		this.module = module;
		registry = new LinkedHashMap();
		aliases = new HashMap();
		idObjects = new HashMap();
	}

	public ModuleImpl getModule() {
		return module;
	}

	public void registerObject(final Name o) throws DataException {
		assert (o != null): "Supplied object is null";
		E obj;
		try {
			obj = (E) o;
		} catch (ClassCastException e) {
			throw new AssertionError("Type covariance error");
		}
		assert (obj.getNamespace() == null): "Supplied object is already registered with a different namespace";
		final String name = obj.getNameString();
		if (registry.containsKey(name)) {
			logger.error("Name " + name + " already registered in this namespace");
			logger.debug("Previously registered object: " + registry.get(name));
			throw new DataException("Name " + name + " already registered");
		}
		registry.put(name, obj);
		obj.setNamespace(this);
	}

	public E getObjectByString(final String name) {
		assert (name != null): "Supplied name is null";
		final E result = registry.get(name);
		if (result == null)
			return aliases.get(name);
		return result;
	}

	public boolean checkEquality(final Name obj1, final Name obj2) throws DataException {
		assert (obj1 != null): "First supplied object is null";
		assert (obj2 != null): "Second supplied object is null";
		final String name1 = obj1.getNameString();
		final String name2 = obj2.getNameString();
		boolean obj1registered = false;
		boolean obj2registered = false;
		if (registry.containsKey(name1) && (registry.get(name1) == obj1))
			obj1registered = true;
		if (registry.containsKey(name2) && (registry.get(name2) == obj2))
			obj2registered = true;
		// both not registered: exception
		if (!(obj1registered || obj2registered)) {
			logger.error("Received equality check request for unregistered objects");
			logger.debug("First object:  " + obj1);
			logger.debug("Second object: " + obj2);
			throw new DataException("Objects unregistered");
		}
		// one not registered: return false
		if (!(obj1registered && obj2registered))
			return false;
		// both registered: perfom equality check
		if (obj1 == obj2)
			return true;
		if (!idObjects.containsKey(name1))
			return false;
		return idObjects.get(name1).contains(obj2);
	}

	public void createAlias(final Name o, final String name) throws DataException {
		assert (o != null): "Supplied object is null";
		assert (name != null): "Supplied name is null";
		E obj;
		try {
			obj = (E) o;
		} catch (ClassCastException e) {
			throw new AssertionError("Type covariance error");
		}
		final String oldname = obj.getNameString();
		if (!(registry.containsKey(oldname) && (registry.get(oldname) == obj))) {
			logger.error("Object " + obj + " not registered");
			throw new DataException("Object not registered");
		}
		if (registry.containsKey(name)) {
			logger.error("Name " + name + " has already been registered for object " + registry.get(name));
			throw new DataException("Name has already been registered");
		}
		if (aliases.containsKey(name)) {
			logger.error("Name " + name + " is already an alias for object " + aliases.get(name));
			throw new DataException("Alias already exists");
		}
		aliases.put(name, obj);
	}

	public void identify(final Name o1, final Name o2) throws DataException {
		assert (o1 != null): "First supplied object is null";
		assert (o2 != null): "Second supplied object is null";
		E obj1;
		E obj2;
		try {
			obj1 = (E) o1;
			obj2 = (E) o2;
		} catch (ClassCastException e) {
			throw new AssertionError("Type covariance error");
		}
		final String name1 = obj1.getNameString();
		if (idObjects.containsKey(name1))
			if (idObjects.get(name1).contains(obj2))
				return;
		final String name2 = obj2.getNameString();
		if (!(registry.containsKey(name1) && (registry.get(name1) == obj1))) {
			logger.error("Object " + obj1 + " not registered");
			throw new DataException("Object not registered");
		}
		if (obj1 == obj2)
			return;
		if (!(registry.containsKey(name2) && (registry.get(name2) == obj2))) {
			logger.error("Object " + obj2 + " not registered");
			throw new DataException("Object not registered");
		}
		// identify & build closure
		Set<E> idSet1 = idObjects.get(name1);
		if (idSet1 == null)
			idSet1 = Collections.singleton(obj1);
		Set<E> idSet2 = idObjects.get(name2);
		if (idSet2 == null)
			idSet2 = Collections.singleton(obj2);
		final Set<E> unionSet = new IdentityHashSet(idSet1);
		unionSet.addAll(idSet2);
		for (final Map.Entry<String, Set<E>> entry: idObjects.entrySet()) {
			final Set<E> value = entry.getValue();
			if ((value == idSet1) || (value == idSet2))
				entry.setValue(unionSet);
		}
		idObjects.put(name1, unionSet);
		idObjects.put(name2, unionSet);
	}

	public Collection<E> objects() {
		return Collections.unmodifiableCollection(registry.values()); // NB: registry is one-to-one
	}

	public Map<String, E> aliases() {
		return Collections.unmodifiableMap(aliases);
	}

	public Collection<Set<E>> equivalenceClasses() {
		return Collections.unmodifiableSet(new IdentityHashSet(idObjects.values()));
	}

}
