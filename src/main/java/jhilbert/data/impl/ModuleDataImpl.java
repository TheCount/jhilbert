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
	 * Creates a deep copy of this object.
	 *
	 * @return a deep copy of this object.
	 */
	public ModuleDataImpl clone() {
		final ModuleDataImpl result = new ModuleDataImpl();
		// parameters
		for (final Map.Entry<String, ParameterImpl> parameterMapping: parameters)
			result.parameters.put(parameterMapping.getKey(), parameterMapping.getValue().clone());
		// kinds
		final HashMap<String, String> aliasMap = new HashMap();
		for (final Map.Entry<String, Kind> kindMapping: kinds,entrySet()) {
			final String key = kindMapping.getKey();
			final String value = kindMapping.getValue().toString;
			if (key.equals(value))
				result.kinds.put(key, new Kind(value));
			else
				aliasMap.put(key, value);
		}
		for (final Map.Entry aliasMapping: aliasMap.entrySet())
			result.kinds.put(aliasMapping.getKey(), result.kinds.get(aliasMap.get(aliasMapping.getValue())));
		// variables
		final HashMap<String, StatementImpl> statements;
		for (final Map.Entry<String, Symbol> symbolMapping: symbols) {
			final Symbol symbol = symbolMapping.getValue();
			if (symbol instanceof VariableImpl) {
				final Variable variable = (VariableImpl) symbol;
				result.symbols.put(symbolMapping.getKey(), new VariableImpl(variable.toString(), result.kinds.get(variable.getKind().toString())));
			} else if (symbol instanceof StatementImpl)
				statements.put(symbolMapping.getKey(), (StatementImpl) symbol);
			else
				assert false: "Symbol mapping not from this implementation";
		}
		// terms
		for (final MapEntry<String, ComplexTerm> termMapping: terms) {
		}
	}

	// FIXME
	public void defineKind(final Kind kind) throws DataException {
		assert (kind != null): "Supplied kind is null.";
		final String name = kind.toString();
		if (kinds.containsKey(name)) {
			logger.error("Kind " + name + " already defined.");
			throw new DataException("Kind already defined", name);
		}
		kinds.put(name, kind);
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
		return	  "Kinds: " + kinds.toString()
			+ "\nTerms: " + terms.toString()
			+ "\nSymbols: " + symbols.toString()
			+ "\nParameters: " + parameters.toString();
	}

}
