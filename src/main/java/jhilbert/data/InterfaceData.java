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

package jhilbert.data;

import java.io.EOFException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jhilbert.data.AbstractComplexTerm;
import jhilbert.data.AbstractData;
import jhilbert.data.ComplexTerm;
import jhilbert.data.Kind;
import jhilbert.data.Parameter;
import jhilbert.data.Statement;
import jhilbert.data.Symbol;
import jhilbert.data.Variable;
import jhilbert.exceptions.DataException;
import jhilbert.exceptions.UnknownFormatException;
import jhilbert.util.DataInputStream;
import jhilbert.util.DataOutputStream;
import org.apache.log4j.Logger;

/**
 * Data collected in an {@link Interface}.
 * <p>
 * The parameters to the methods and constructors of this class must never be <code>null</code>.
 * Otherwise, the behavior of the whole class and its objects is undefined.
 * <p>
 * This class provides the {@link #store()} method to byte-compile the interface data.
 * <p>
 * This class uses permissive querying: unknown kinds and terms are created on demand and internally marked undefined.
 */
public final class InterfaceData extends AbstractData {

	/**
	 * Logger for this class.
	 */
	private final Logger logger = Logger.getLogger(InterfaceData.class);

	/**
	 * Undefined kinds.
	 */
	private final Set<String> undefinedKindNames;

	/**
	 * Kinds.
	 */
	private final Map<String, Kind> kinds;

	/**
	 * Undefined terms.
	 */
	private final Set<String> undefinedTermNames;

	/**
	 * Defined terms.
	 */
	private final Map<String, AbstractComplexTerm> terms;

	/**
	 * Creates new empty interface data.
	 */
	public InterfaceData() {
		super();
		undefinedKindNames = new HashSet();
		kinds = new HashMap();
		undefinedTermNames = new HashSet();
		terms = new LinkedHashMap(); // for sound insertion order
	}

	/**
	 * Loads previously stored interface data from the specified input stream.
	 *
	 * @param in input stream.
	 *
	 * @throws UnknownFormatException if the {@link Data#FORMAT_VERSION} is incorrect.
	 * @throws DataException if any other error occurs while loading the data.
	 *
	 * @see #store()
	 */
	public InterfaceData(final InputStream in) throws UnknownFormatException, DataException {
		assert (in != null): "Supplied input stream is null.";
		final DataInputStream ds = new DataInputStream(in);
		undefinedKindNames = new HashSet();
		kinds = new HashMap();
		undefinedTermNames = new HashSet();
		terms = new HashMap();
		try {
			final int format = ds.readInt();
			if (format != Data.FORMAT_VERSION) {
				logger.error("Unknown interface data file format version.");
				logger.error("Got version: " + format);
				logger.error("Expected version: " + Data.FORMAT_VERSION);
				throw new UnknownFormatException("Unknown interface data file format version",
					in.toString());
			}
			// read data sizes
			final int parameterSize = ds.readNonNegativeInt();
			final int undefinedKindsSize = ds.readNonNegativeInt();
			final int undefinedTermNamesSize = ds.readNonNegativeInt();
			final int definedKindsSize = ds.readNonNegativeInt();
			final int definedTermNamesSize = ds.readNonNegativeInt();
			final int statementsSize = ds.readNonNegativeInt();
			// calculate bounds
			final int parameterLowerBound = 1;
			final int parameterUpperBound = parameterLowerBound + parameterSize;
			final int kindsLowerBound = parameterUpperBound;
			final int undefinedKindsLowerBound = kindsLowerBound;
			final int undefinedKindsUpperBound = undefinedKindsLowerBound + undefinedKindsSize;
			final int definedKindsLowerBound = undefinedKindsUpperBound;
			final int definedKindsUpperBound = definedKindsLowerBound + definedKindsSize;
			final int kindsUpperBound = definedKindsUpperBound;
			final int termNamesLowerBound = definedKindsUpperBound;
			final int undefinedTermNamesLowerBound = termNamesLowerBound;
			final int undefinedTermNamesUpperBound = undefinedTermNamesLowerBound + undefinedTermNamesSize;
			final int definedTermNamesLowerBound = undefinedTermNamesUpperBound;
			final int definedTermNamesUpperBound = definedTermNamesLowerBound + definedTermNamesSize;
			final int termNamesUpperBound = definedTermNamesUpperBound;
			final int statementsLowerBound = definedTermNamesUpperBound;
			final int statementsUpperBound = statementsLowerBound + statementsSize;
			final int totalSize = statementsUpperBound;
			// read name list
			final List<String> nameList = new ArrayList(totalSize);
			nameList.add(null); // for unknown kinds, see ComplexTerm#store()
			for (int i = 1; i != totalSize; ++i)
				nameList.add(ds.readString());
			// load parameters
			for (int i = parameterLowerBound; i != parameterUpperBound; ++i)
				addParameter(new Parameter(nameList.get(i), ds, this, nameList, parameterLowerBound,
							parameterUpperBound));
			// load undefined kinds
			for (int i = undefinedKindsLowerBound; i != undefinedKindsUpperBound; ++i) {
				final String kindName = nameList.get(i);
				if (undefinedKindNames.contains(kindName)) {
					logger.error("Undefined kind " + kindName + " specified twice.");
					throw new DataException("Undefined kind specified twice", kindName);
				}
				undefinedKindNames.add(kindName);
				kindMap.put(kindName, new Kind(kindName));
			}
			// load defined kinds
			for (int i = definedKindsLowerBound; i != definedKindsUpperBound; ++i) {
				final String kindName = nameList.get(i);
				if (kinds.containsKey(kindName)) {
					logger.error("Kind " + kindName + " specified twice.");
					throw new DataException("Kind specified twice", kindName);
				}
				final String targetKindName = nameList.get(ds.readInt(kindsLowerBound, kindsUpperBound));
				final Kind newKind = new Kind(kindName);
				// make sure only one new kind is created for every kind name
				kinds.put(kindName, newKind);
				Kind targetKind = kinds.get(targetKindName);
				if (targetKind == null)
					targetKind = new Kind(targetKindName);
				kinds.put(kindName, targetKind);
			}
			// load undefined terms
			for (int i = undefinedTermNamesLowerBound; i != undefinedTermNamesUpperBound; ++i) {
				final String termName = nameList.get(i);
				if (undefinedTermNames.contains(termName)) {
					logger.error("Undefined term " + termName + " specified twice.");
					throw new DataException("Undefined term specified twice", termName);
				}
				undefinedTermNames.add(termName);
				terms.put(termName, AbstractComplexTerm.create(termName, ds, this, nameList,
					kindsLowerBound, kindsUpperBound));
			}
			// load defined terms
			for (int i = definedTermNamesLowerBound; i != definedTermNamesUpperBound; ++i) {
				final String termName = nameList.get(i);
				if (terms.containsKey(termName)) {
					logger.error("Defined term " + termName + " specified twice.");
					throw new DataException("Defined term specified twice", termName);
				}
				terms.put(termName, AbstractComplexTerm.create(termName, ds, this, nameList,
					kindsLowerBound, kindsUpperBound, termNamesLowerBound, i));
			}
			// load statements
			for (int i = statementsLowerBound; i != statementsUpperBound; ++i) {
				final String statementName = nameList.get(i);
				if (symbols.containsKey(statementName)) {
					logger.error("Statement " + statementName + " specified twice.");
					throw new DataException("Statement specified twice", statementName);
				}
				symbols.put(statementName, new Statement(statementName, ds, this, nameList, kindsLowerBound,
					kindsUpperBound, termNamesLowerBound, termNamesUpperBound));
			}
			if (ds.read() != -1) {
				logger.error("Interface completely loaded but input stream " + ds + "is not at EOF");
				throw new DataException("Interface completely loaded but input stream is not at EOF",
						ds.toString());
			}
		} catch (EOFException e) {
			logger.error("Unexpected end of input while loading interface.", e);
			throw new DataException("Unexpected end of input while loading interface.", ds.toString(), e);
		} catch (IOException e) {
			logger.error("I/O error while loading interface.", e);
			throw new DataException("I/O error while loading interface.", ds.toString(), e);
		}
	}

	public @Override Kind getKind(final String name) {
		assert (name != null): "Supplied kind name is null.";
		Kind result = kinds.get(name);
		if (result == null) {
			undefinedKinds.add(name);
			result = new Kind(name);
			kindMap.put(name, result);
		}
		return result;
	}

	public @Override void defineKind(final Kind kind) throws DataException {
		assert (kind != null): "Supplied kind is null.";
		final String name = kind.getName();
		if (kinds.containsKey(name)) {
			logger.error("Kind " + name + " already defined or referenced.");
			throw new DataException("Kind already defined or referenced.", name);
		}
		kindMap.put(name, kind);
	}

	public @Override void bindKind(final Kind oldKind, final Kind newKind) throws DataException {
		assert (oldKind != null): "Supplied existing kind is null.";
		assert (newKind != null): "Supplied name of new kind is null.";
		final String newKindName = newKind.getName();
		if (oldKind.equals(newKind)) {
			logger.error("Binding kind " + newKind + ": old and new kind are identical.");
			throw new DataException("Old and new kind are identical while binding kind.", newKind);
		}
		if (kinds.containsKey(newKindName)) {
			logger.error("Binding kind: " + newKindName + " already defined or referenced.");
			throw new DataException("Kind already defined or referenced while binding kind.", newKindName);
		}
		kinds.put(newKindName, getKind(oldKind.getName()));
	}

	public @Override AbstractComplexTerm getTerm(final String name) {
		assert (name != null): "Supplied term name is null.";
		AbstractComplexTerm result = terms.get(name);
		if (result == null) {
			result = new ComplexTerm(name);
			undefinedTermNames.add(name);
			terms.put(name, result);
		}
		return result;
	}

	public @Override void defineTerm(AbstractComplexTerm term) throws DataException {
		assert (term != null): "Supplied term is null.";
		assert (term.getKind() != null): "Result kind of term to be defined is not known.";
		assert (term.placeCount() >= 0): "Place count of term to be defined is not known.";
		// FIXME: Also check input kinds
		final String name = term.getName();
		if (terms.containsKey(name)) {
			logger.error("Term " + name + " already defined or referenced.");
			throw new DataException("Term already defined or referenced.", name);
		}
		terms.put(name, term);
	}

	/**
	 * Defines a new parameter.
	 *
	 * @param parameter parameter to be defined (must not be <code>null</code>).
	 *
	 * @throws DataException if a parameter with the same name is already defined.
	 */
	public void addParameter(final Parameter parameter) throws DataException {
		assert (parameter != null): "Supplied parameter is null.";
		final String name = parameter.getName();
		if (parameters.containsKey(name)) {
			logger.error("Parameter " + name + " already defined.");
			throw new DataException("Parameter already defined", name);
		}
		parameters.put(name, parameter);
	}

	/**
	 * FIXME: Not implemented: not needed right now and I'm lazy
	 */
	public InterfaceData clone() {
		assert false: "Not implemented.";
		return new InterfaceData();
	}

	/**
	 * Stores interface data in the specified output stream.
	 * <p>
	 * FIXME: output format specified in JHilbert spec???
	 *
	 * @param out OutputStream.
	 *
	 * @throws DataException if an I/O error occurs.
	 */
	public void store(final OutputStream out) throws DataException {
		final Map<String, Integer> parameterNameTable = new HashMap();
		final Map<String, Integer> kindNameTable = new HashMap();
		final Map<String, Integer> termNameTable = new HashMap();
		int currentID = 1;
		final Set<String> definedKinds = new HashSet(kinds.keySet());
		definedKinds.removeAll(undefinedKinds);
		final Set<String> definedTermNames = new HashSet(terms.keySet());
		definedTermNames.removeAll(undefinedTermNames);
		final Map<String, Statement> statements = new HashMap();
		for (Symbol symbol: symbols.values())
			if (!symbol.isVariable())
				statements.put(symbol.getName(), (Statement) symbol);
		try {
			final DataOutputStream ds = new DataOutputStream(out);
			// structure sizes
			ds.writeInt(Data.FORMAT_VERSION);	// 0: version
			ds.writeInt(parameters.size());		// 4: number of parameters
			ds.writeInt(undefinedKinds.size());	// 8: # of undefined kinds
			ds.writeInt(undefinedTermNames.size());	// 12: # of undefined terms
			ds.writeInt(definedKinds.size());	// 16: # of defined kinds
			ds.writeInt(definedTermNames.size());	// 20: # of defined terms
			ds.writeInt(statements.size());		// 24: # of statements
			// name list
			for (String parameterName: parameters.keySet()) {
				parameterNameTable.put(parameterName, currentID++);
				ds.writeString(parameterName);
			}
			for (String undefinedKind: undefinedKinds) {
				kindNameTable.put(undefinedKind, currentID++);
				ds.writeString(undefinedKind);
			}
			for (String undefinedTermName: undefinedTermNames) {
				termNameTable.put(undefinedTermName, currentID++);
				ds.writeString(undefinedTermName);
			}
			for (String definedKind: definedKinds) {
				kindNameTable.put(definedKind, currentID++);
				ds.writeString(definedKind);
			}
			for (String definedTermName: definedTermNames) {
				termNameTable.put(definedTermName, currentID++);
				ds.writeString(definedTermName);
			}
			for (String statementName: statements.keySet())
				ds.writeString(statementName);
			// parameters
			for (String parameterName: parameters.keySet())
				parameters.get(parameterName).store(ds, parameterNameTable);
			// kind mappings (defined kinds only)
			for (String definedKind: definedKinds)
				ds.writeInt(kindNameTable.get(kinds.get(definedKind).getName()));
			// undefined terms (partial info)
			for (String undefinedTermName: undefinedTermNames)
				terms.get(undefinedTermName).store(ds, kindNameTable, termNameTable);
			// defined terms
			for (String definedTermName: definedTermNames)
				terms.get(definedTermName).store(ds, kindNameTable, termNameTable);
			// statements
			for (String statementName: statements.keySet())
				statements.get(statementName).store(ds, kindNameTable, termNameTable);
			ds.flush();
		} catch (IOException e) {
			logger.error("I/O error while writing interface data to " + out, e);
			throw new DataException("I/O error while writing interface data", out.toString(), e);
		}
	}

	/**
	 * Imports these interface data into the specified module data, defining the specified parameter.
	 *
	 * @param moduleData module data to import these data into (must not be <code>null</code>).
	 * @param parameter parameter to define (must not be <code>null</code>).
	 *
	 * @throws DataException if the import fails.
	 */
	public void importInto(final ModuleData moduleData, final Parameter parameter) throws DataException {
		// FIXME: Redo from start 8-0
		assert (moduleData != null): "Supplied module data are null.";
		assert (parameter != null): "Supplied parameter is null.";
		// check parameters
		final List<Parameter> parameterList = parameter.getParameterList();
		final int paramCount = parameterList.size();
		if (paramCount != parameters.size()) {
			logger.error("Wrong number of parameters while importing interface " + parameter.getName());
			logger.error("Expected number of parameters:  " + parameters.size());
			logger.error("Received number of parameters: " + paramCount);
			throw new DataException("Wrong number of parameters", parameter.getName());
		}
		int i = 0;
		for (final Parameter thisParameter: parameters) {
			final Parameter thatParameter = parameterList.get(i);
			if (!(thisParameter.getLocator().equals(thatParameter.getLocator()) && thisParameter.getPrefix().equals(thatParameter.getPrefix()))) {
				logger.info("Checking whether parameter " + thatParameter.getName() + " satisfies " + thisParameter.getName());
				try {
					exportFrom(moduleData.clone(), thisParameter);
				} catch (DataException e) {
					logger.error("Parameter " + thatParameter.getName() + " does not satisfy " + thisParameter.getName(), e);
					throw new DataException("Parameter satisfaction error", thatParameter.getName(), e);
				}
			}
			++i;
		}
		final String prefix = parameter.getPrefix();
		// check kinds
		for (final Map.Entry<String, String> kindMapEntry: kindMap) {
			final String kind = kindMapEntry.getKey();
			final String moduleKind = prefix + kind;
			final String targetKind = kindMapEntry.getValue();
			final String moduleTargetKind = prefix + targetKind;
			if (undefinedKinds.contains(kind)) { // FIXME: loops can be split if necessary
				if (moduleData.getKind(moduleKind) == null) {
					logger.error("Unresolved kind " + moduleKind + " during import.");
					throw new DataException("Unresolved kind", moduleKind);
				}
				// undefined kinds are always fixed points of kindMap, so no bindKind is needed.
			} else
				try {
					if (kind.equals(targetKind))
						moduleData.defineKind(moduleKind);
					else
						moduleData.bindKind(moduleTargetKind, moduleKind);
				} catch (DataException e) {
					logger.error("Unable to import kind " + moduleKind, e);
					logger.error("Target: " + moduleTargetKind, e);
					throw new DataException("Unable to import kind", moduleKind, e);
				}
		}
		// check terms
		// FIXME
	}

	/**
	 * Checks whether these interface data satisfy the specified interface data.
	 * That is, check whether all kinds, terms, and statements of these data
	 * are defined in the specified data.
	 *
	 * @param data interface data to check against (must not be <code>null</code>).
	 *
	 * @return <code>true</code> if these data satisfy the specified <code>data</code>, as explained above; <code>false</code> otherwise.
	 *
	 * FIXME: broken, don't use
	 */
	private boolean satisfies(final InterfaceData data) {
		// FIXME: we probably need a prefix
		assert (data != null): "Supplied data are null.";
		// build collections of defined names
		final HashSet<String> definedKinds = new HashSet(kindMap.keySet());
		definedKinds.removeAll(undefinedKinds);
		final HashSet<String> dataDefinedKinds = new HashSet(data.kindMap.keySet());
		dataDefinedKinds.removeAll(data.undefinedKinds);
		final HashSet<String> definedTermNames = new HashSet(terms.keySet());
		definedTermNames.removeAll(undefinedTermNames);
		final HashSet<String> dataDefinedTermNames = new HashSet(data.terms.keySet());
		dataDefinedTermNames.removeAll(data.undefinedTermNames);
		final HashSet<String> statementNames = new HashSet();
		for (final Symbol symbol: symbols.values())
			if (symbol instanceof Statement)
				statementNames.add(symbol.getName());
		final HashSet<String> dataStatementNames = new HashSet();
		for (final Symbol symbol: data.symbols.values())
			if (symbol instanceof Statement)
				dataStatementNames.add(symbol.getName());
		// check kinds
		for (final String kind: definedKinds)
			if (!dataDefinedKinds.contains(kind))
				return false;
		// check terms
		for (final String termName: definedTermNames) {
			if (!dataDefinedTermNames.contains(termName))
				return false;
			if (!terms.get(termName).equalsSuperficially(data.terms.get(termName), this, data)) {
				logger.error("Kind mismatch in term " + termName);
				return false;
			}
		}
		// check statements
		for (final String statementName: statementNames) {
			if (!dataStatementNames.contains(statementName))
				return false;
			if (!((Statement) symbols.get(statementName)).equalsSuperficially((Statement) data.symbols.get(statementName))) {
				logger.error("Mismatch in statement " + statementName);
				return false;
			}
		}
		return true;
	}

}
