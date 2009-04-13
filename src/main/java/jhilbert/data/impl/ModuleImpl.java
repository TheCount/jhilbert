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

    You may contact the author on these Wiki pages:
    http://planetx.cc.vt.edu/AsteroidMeta//GrafZahl (preferred)
    http://en.wikisource.org/wiki/User_talk:GrafZahl
*/

package jhilbert.data.impl;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jhilbert.data.DataException;
import jhilbert.data.Module;
import jhilbert.data.Parameter;

import org.apache.log4j.Logger;

/**
 * {@link Module} implementation.
 */
final class ModuleImpl implements Module, Serializable {

	/**
	 * Serialisation ID.
	 */
	private static final long serialVersionUID = jhilbert.Main.VERSION;

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(ModuleImpl.class);

	/**
	 * Module name.
	 */
	private final String name;

	/**
	 * Module revision.
	 */
	private final long revision;

	/**
	 * Parameter list.
	 */
	private final List<Parameter> parameterList;

	/**
	 * Parameter lookup map.
	 */
	private final Map<String, Integer> parameterLookupMap;

	/**
	 * Kind namespace.
	 */
	private final NamespaceImpl<KindImpl> kindNamespace;

	/**
	 * Symbol namespace.
	 */
	private final NamespaceImpl<SymbolImpl> symbolNamespace;

	/**
	 * Functor namespace.
	 */
	private final NamespaceImpl<AbstractFunctor> functorNamespace;

	/**
	 * Default constructor, for serialisation only!
	 */
	public ModuleImpl() {
		name = null;
		revision = -1;
		parameterList = null;
		parameterLookupMap = null;
		kindNamespace = null;
		symbolNamespace = null;
		functorNamespace = null;
	}

	/**
	 * Creates a new unversioned <code>ModuleImpl</code> with the specified
	 * name.
	 *
	 * @param name module name.
	 */
	ModuleImpl(final String name) throws DataException /* not really */ {
		this(name, -1);
	}

	/**
	 * Creates a new <code>ModuleImpl</code> with the specified name and
	 * revision number.
	 * The revision number is a non-negative integer. A value of
	 * <code>-1</code> is also permitted, indicating an unversioned module.
	 *
	 * @param name module name.
	 * @param revision revision number.
	 *
	 * @throws DataException if the revision number is smaller than
	 * 	<code>-1</code>.
	 */
	ModuleImpl(final String name, final long revision) throws DataException {
		assert (name != null): "Supplied name is null";
		if (revision < -1) {
			logger.error("Negative revision for module " + name + " requested");
			logger.debug("Requested revision: " + revision);
			throw new DataException("Negative revision requested");
		}
		this.name = name;
		this.revision = revision;
		parameterList = new ArrayList();
		parameterLookupMap = new HashMap();
		kindNamespace = new NamespaceImpl(this);
		symbolNamespace = new NamespaceImpl(this);
		functorNamespace = new NamespaceImpl(this);
	}

	public String getName() {
		return name;
	}

	public long getRevision() {
		return revision;
	}

	public List<Parameter> getParameters() {
		return Collections.unmodifiableList(parameterList);
	}

	public Parameter getParameter(final String name) {
		assert (name != null): "Supplied name is null";
		if (!parameterLookupMap.containsKey(name))
			return null;
		assert (parameterLookupMap.get(name) != null): "internal parameter lookup table contains null value";
		assert ((0 <= parameterLookupMap.get(name)) && (parameterLookupMap.get(name) < parameterList.size())): "Internal parameter size error";
		return parameterList.get(parameterLookupMap.get(name));
	}

	public void addParameter(final Parameter parameter) throws DataException {
		assert (parameter != null): "Supplied parameter is null";
		final String parameterName = parameter.getName();
		if (parameterLookupMap.containsKey(parameterName)) {
			logger.error("Parameter " + parameterName + " already exists");
			logger.debug("Previous parameter: " + parameterList.get(parameterLookupMap.get(parameterName)));
			logger.debug("Found parameter:    " + parameter);
			throw new DataException("Parameter already exists");
		}
		parameterLookupMap.put(parameterName, parameterList.size());
		parameterList.add(parameter);
	}

	public NamespaceImpl<KindImpl> getKindNamespace() {
		return kindNamespace;
	}

	public NamespaceImpl<SymbolImpl> getSymbolNamespace() {
		return symbolNamespace;
	}

	public NamespaceImpl<AbstractFunctor> getFunctorNamespace() {
		return functorNamespace;
	}

}
