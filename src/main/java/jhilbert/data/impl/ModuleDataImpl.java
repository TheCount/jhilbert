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

package jhilbert.data.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jhilbert.data.Kind;
import jhilbert.data.ModuleData;
import jhilbert.data.Parameter;
import jhilbert.data.Symbol;
import jhilbert.data.Variable;
import jhilbert.exceptions.DataException;
import jhilbert.util.Collections;
import org.apache.log4j.Logger;

/**
 * Data collected in a proof module.
 */
final class ModuleDataImpl extends DataImpl implements ModuleData {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(ModuleDataImpl.class);

	/**
	 * Kinds.
	 */
	private final Map<String, Kind> kinds;

	/**
	 * Creates a new instance of module data.
	 */
	// FIXME
	public ModuleDataImpl() {
		super();
		kinds = new HashMap();
		terms = new HashMap();
	}

	/**
	 * Copy constructor.
	 * Creates a shallow copy.
	 * FIXME: Do we need this method?
	 *
	 * @param moduleData module data to be copied (must not be <code>null</code>).
	 */
	// FIXME
	//protected ModuleDataImpl(final ModuleDataImpl moduleData) {
	//	super(moduleData);
	//	kinds = moduleData.kinds;
	//	terms = moduleData.terms;
	//}

	/**
	 * Creates a deep copy of this data collection.
	 * The contents is copied shallowly.
	 *
	 * @return a deep copy of this data collection.
	 */
	// FIXME: Do we need this methods?
	//public ModuleDataImpl clone() {
	//	final ModuleDataImpl result = new ModuleDataImpl();
	//	result.parameters.putAll(parameters);
	//	result.kinds.putAll(kinds);
	//	result.terms.putAll(terms);
	//	result.symbols.putAll(symbols);
	//}

	/**
	 * Defines a new kind.
	 *
	 * @param name name of the kind to be defined (must not be <code>null</code>).
	 *
	 * @throws DataException if a kind with the specified name already exists.
	 */
	void defineKind(final String name) throws DataException {
		assert (name != null): "Supplied kind name is null.";
		if (kinds.containsKey(name)) {
			logger.error("Kind " + name + " already defined.");
			throw new DataException("Kind already defined", name);
		}
		kinds.put(name, new KindImpl(name));
	}

	public Kind getKind(final String kind) {
		return kinds.get(kind);
	}

	public void bindKind(final Kind oldKind, final String newKindName) throws DataException {
		assert (oldKind != null): "Supplied old kind is null.";
		assert (newKindName != null): "Supplied new kind name is null.";
		final String oldKindName = oldKind.toString();
		if (!kinds.containsKey(oldKindName)) {
			logger.error("Old kind not found: " + oldKindName);
			throw new DataException("Kind not found", oldKindName);
		}
		if (kinds.containsKey(newKindName)) {
			logger.error("New kind already exists: " + newKindName);
			throw new DataException("Kind already exists", newKindName);
		}
		kinds.put(newKindName, kinds.get(oldKindName));
	}

	// FIXME
	//public void defineTerm(final ComplexTerm term) throws DataException {
	//	assert (term != null): "Supplied term is null.";
	//	final String name = term.getName();
	//	if (terms.containsKey(name)) {
	//		logger.error("A term with name " + name + " is already defined.");
	//		throw new DataException("Term already defined", name);
	//	}
	//	terms.put(name, term);
	//}

	/**
	 * Defines a new {@link Functor}.
	 *
	 * @param name name of the new Functor (must not be <code>null</code>).
	 * @param resultKind resulting kind of the Functor (must not be <code>null</code>).
	 * @param inputKindList list of input kinds for the new Functor (must not be <code>null</code>).
	 *
	 * @throws DataException if a functor with the specified name already exists.
	 */
	void defineTerm(final String name, final Kind resultKind, final List<Kind> inputKindList) throws DataException {
		assert (name != null): "Supplied name is null.";
		assert (resultKind != null): "Supplied result kind is null.";
		assert (inputKindList != null): "Supplied input kind list is null.";
		if (terms.containsKey(name)) {
			logger.error("A term with name " + name + " already exists.");
			throw new DataException("Term already exists", name);
		}
		terms.put(name, new Functor(name, resultKind, inputKindList));
	}

	public ComplexTerm getTerm(final String name) {
		return terms.get(name);
	}

	/**
	 * Defines a new Parameter.
	 *
	 * @param param Parameter to define (must not be <code>null</code>).
	 *
	 * @throws DataException if the parameter is already defined.
	 */
	// FIXME
	//public void defineParameter(final Parameter param) throws DataException {
	//	assert (param != null): "Supplied parameter is null.";
	//	final String name = param.toString();
	//	if (parameters.containsKey(name)) {
	//		logger.error("Parameter with name " + name + " already defined.");
	//		throw new DataException("Parameter already defined", name);
	//	}
	//	parameters.put(name, param);
	//}

	// FIXME
	public @Override String toString() {
		return	  "Kinds: " + kinds.toString() + "; "
			+ "Terms: " + terms.toString() + "; "
			+ "Symbols: " + symbols.toString() + "; "
			+ "Parameters: " + parameters.toString();
	}

}
