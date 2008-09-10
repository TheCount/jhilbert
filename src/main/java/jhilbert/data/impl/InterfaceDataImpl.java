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

import java.io.EOFException;
import java.io.Externalizable;
import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import jhilbert.data.Data;
import jhilbert.data.DataException;
import jhilbert.data.Kind;
import jhilbert.data.InterfaceData;
import jhilbert.data.Parameter;
import jhilbert.data.Statement;
import jhilbert.data.Symbol;
import jhilbert.data.TermExpression;
import jhilbert.data.Variable;
import jhilbert.data.VariablePair;
import jhilbert.data.impl.DataFactoryImpl;
import jhilbert.data.impl.DataImpl;
import jhilbert.data.impl.IComplexTerm;
import jhilbert.data.impl.KindImpl;
import jhilbert.data.impl.ParameterizedKind;
import jhilbert.data.impl.ParameterizedTerm;
import jhilbert.util.InputException;
import jhilbert.util.Pair;
import org.apache.log4j.Logger;

/**
 * Data collected in an {@link Interface}.
 * <p>
 * The parameters to the methods and constructors of this class must never be <code>null</code>.
 * Otherwise, the behavior of the whole class and its objects is undefined.
 * <p>
 * This class uses permissive querying: unknown kinds and terms are created on demand and internally marked undefined.
 */
final class InterfaceDataImpl extends DataImpl implements InterfaceData, Externalizable {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(InterfaceDataImpl.class);

	/**
	 * Undefined kinds.
	 */
	private transient Map<String, ParameterizedKind> undefinedKinds;

	/**
	 * Kind bindings.
	 */
	private Set<Pair<String, String>> kindBindings;

	/**
	 * Kinds.
	 */
	private LinkedHashMap<String, KindImpl> kinds;

	/**
	 * Undefined term names.
	 */
	private transient Map<String, ParameterizedTerm> undefinedTerms;

	/**
	 * Creates new empty interface data.
	 */
	public InterfaceDataImpl() {
		super();
		undefinedKindNames = new HashMap();
		kinds = new LinkedHashMap(); // for sound insertion order
		kindBindings = new HashMap();
		undefinedTerms = new HashMap();
		terms = new LinkedHashMap(); // for sound insertion order
	}

	public @Override Kind getKind(final String name) throws DataException {
		assert (name != null): "Supplied kind name is null.";
		final Kind result = undefinedKinds.get(name);
		if (result == null)
			return kinds.get(name);
		return result;
	}

	public void defineKind(final String name) throws DataException {
		assert (name != null): "Supplied kind name is null.";
		if (undefinedKinds.containsKey(name)) {
			logger.error("Definition of kind " + name + " clobbers kind " + undefinedKinds.get(name).getOriginalName() + " from parameter "
				+ undefinedKinds.get(name).getParameter());
			throw new DataException("Kind already exists", name);
		}
		if (kinds.containsKey(name)) {
			logger.error("Kind " + name + " already defined.");
			throw new DataException("Kind already defined", name);
		}
		kinds.put(name, new KindImpl(name));
	}

	public @Override void bindKind(final Kind oldKind, final String newKindName) throws DataException {
		assert (oldKind != null): "Supplied existing kind is null.";
		assert (newKindName != null): "Supplied name of new kind is null.";
		final String oldKindName = oldKind.getName();
		if (oldKindName.equals(newKindName)) {
			logger.error("Binding kind " + newKindName + ": old and new kind are identical.");
			throw new DataException("Old and new kind are identical while binding kind.", newKindName);
		}
		if (!(undefinedKinds.containsKey(oldKindName) && undefinedKinds.containsKey(newKindName))) {
			logger.error("Kinds " + oldKindName + " and " + newKindName + " must both come from a parameter.");
			throw new DataException("Kind not found in parameters", oldkindName + " or " + newKindName);
		}
		kindBindings.add(new Pair(oldKindName, newKindName));
	}

	public @Override IComplexTerm getTerm(final String name) throws DataException {
		assert (name != null): "Supplied term name is null.";
		final IComplexTerm result = undefinedTerms.get(name);
		if (result == null)
			return terms.get(name);
		return result;
	}

	public void defineTerm(final String name, final Kind resultKind, final List<Kind> inputKindList)
	throws DataException {
		assert (name != null): "Supplied term name is null.";
		assert (resultKind != null): "Supplied result kind is null.";
		assert (inputKindList != null): "Supplied list of input kinds is null.";
		if (undefinedTerms.containsKey(name)) {
			logger.error("Definition of term " + name + " clobbers term " + undefinedTerms.get(name).getOriginalName() + " from parameter "
				+ undefinedTerms.get(name).getParameter().getName());
			throw new DataException("Term already exists", name);
		}
		if (terms.containsKey(name)) {
			logger.error("Term " + name + " already defined.");
			throw new DataException("Term already defined.", name);
		}
		terms.put(name, new Functor(name, resultKind, inputKindList));
	}

	/**
	 * Obtains the index of the specified parameter.
	 *
	 * @param name parameter name.
	 *
	 * @return index of the specified parameter, or <code>-1</code> if no such parameter exists.
	 */
	public int getParameterIndex(final String name) {
		return parameters.indexOfKey(name);
	}

	/**
	 * Imports these interface data into the specified module data, defining the specified parameter.
	 *
	 * @param moduleData module data to import these data into (must not be <code>null</code>).
	 * @param parameter parameter to define (must not be <code>null</code>).
	 *
	 * @throws DataException if the import fails.
	 */
	void importInto(final ModuleDataImpl moduleData, final ParameterImpl parameter) throws DataException {
		assert (moduleData != null): "Supplied module data are null.";
		assert (parameter != null): "Supplied parameter is null.";
		logger.info("Importing " + parameter);
		final Map<Variable, Variable> varMap = new HashMap();
		// parameters
		final Map<String, ParameterImpl> parameterMap = checkParameters(moduleData, parameter);
		setParameters(parameterMap);
		final String prefix = parameter.getPrefix();
		final Map<String, String> kindNameMap = new HashMap();
		final Map<String, String> termNameMap = new HashMap();
		// kinds
		final Map<String, String> aliasMap = new LinkedHashMap();
		for (final Map.Entry<String, KindImpl> kindMapping: kinds.entrySet()) {
			final String key = kindMapping.getKey();
			final String value = kindMapping.getValue().getName();
			if (undefinedKindNames.containsKey(key)) {
				final ParameterizedName pn = undefinedKindNames.get(key);
				final String fullName = pn.getModulePrefix() + pn.getName();
				if (moduleData.getKind(fullName) == null) {
					logger.error("Import: unable to link kind " + key + " to " + fullName);
					throw new DataException("Unable to link kind", key);
				}
				kindNameMap.put(key, fullName);
			} else if (key.equals(value)) {
				final String fullName = prefix + key;
				moduleData.defineKind(fullName);
				kindNameMap.put(key, fullName);
			} else
				aliasMap.put(key, value);
		}
		for (final Map.Entry<String, String> aliasMapping: aliasMap.entrySet()) {
			final String key = aliasMapping.getKey();
			final String fullName = prefix + key;
			moduleData.bindKind(moduleData.getKind(kindNameMap.get(aliasMapping.getValue())),
				fullName);
			kindNameMap.put(key, fullName);
		}
		// terms
		for (final Map.Entry<String, ComplexTerm> termMapping: terms.entrySet()) {
			final String key = termMapping.getKey();
			final ComplexTerm value = termMapping.getValue();
			if (undefinedTermNames.containsKey(key)) {
				final ParameterizedName pn = undefinedTermNames.get(key);
				final String fullName = pn.getModulePrefix() + pn.getName();
				if (moduleData.getTerm(fullName) == null) {
					logger.error("Import: unable to link term " + key + " to " + fullName);
					throw new DataException("Unable to link term", key);
				}
				termNameMap.put(key, fullName);
			} else if (value instanceof Functor) {
				final Functor functor = (Functor) value;
				final Kind resultKind = moduleData.getKind(kindNameMap.get(functor.getKind().getName()));
				assert (resultKind != null): "Result kind null during Functor import.";
				final int placeCount = functor.placeCount();
				assert (placeCount >= 0): "Place count negative during Functor import.";
				final List<Kind> inputKinds = new ArrayList(placeCount);
				for (int i = 0; i != placeCount; ++i) {
					final Kind inputKind = moduleData.getKind(kindNameMap
						.get(functor.getInputKind(i).getName()));
					assert (inputKind != null): "Input kind null during Functor import.";
					inputKinds.add(inputKind);
				}
				final String fullName = prefix + key;
				moduleData.defineTerm(fullName, resultKind, inputKinds);
				termNameMap.put(key, fullName);
			} else if (value instanceof Definition) {
				final Definition definition = (Definition) value;
				final TermExpressionImpl definiens
					= definition.getDefiniens().adapt(moduleData, kindNameMap, termNameMap, varMap);
				final LinkedHashSet<Variable> varList = definiens.variables();
				assert (varList.size() == definition.placeCount()):
					"Place count mismatch during Definition import.";
				final String fullName = prefix + key;
				moduleData.defineTerm(fullName, varList, definiens);
				termNameMap.put(key, fullName);
			} else
				assert false: "Term not from this implementation.";
		}
		// symbols
		for (final Symbol symbol: symbols.values()) {
			if (symbol.isVariable())
				continue;
			assert (symbol instanceof StatementImpl): "Symbol not from this implementation.";
			final StatementImpl statement = (StatementImpl) symbol;
			final List<SortedSet<Variable>> rawDV
				= statement.getDVConstraints().adapt(moduleData, kindNameMap, varMap);
			final List<TermExpression> hypotheses = new ArrayList();
			for (final TermExpression hypothesis: statement.getHypotheses()) {
				assert (hypothesis instanceof TermExpressionImpl):
					"Hypothesis not from this implementation.";
				hypotheses.add(((TermExpressionImpl) hypothesis)
					.adapt(moduleData, kindNameMap, termNameMap, varMap));
			}
			final TermExpressionImpl consequent = statement.getConsequent()
				.adapt(moduleData, kindNameMap, termNameMap, varMap);
			moduleData.defineStatement(prefix + statement.getName(), rawDV, hypotheses, consequent);
		}
		unsetParameters();
	}

	/**
	 * Exports these interface data from the specified module data using the specified parameter.
	 *
	 * @param moduleData module data to export these data from (must not be <code>null</code>).
	 * @param parameter parameter to use (must not be <code>null</code>).
	 *
	 * @throws DataException if the export fails.
	 */
	void exportFrom(final ModuleDataImpl moduleData, final ParameterImpl parameter) throws DataException {
		assert (moduleData != null): "Supplied module dara are null.";
		assert (parameter != null): "Supplied parameter is null.";
		logger.info("Exporting " + parameter);
		final Map<String, ParameterImpl> parameterMap = checkParameters(moduleData, parameter);
		setParameters(parameterMap);
		final String prefix = parameter.getPrefix();
		final Map<String, String> kindNameMap = new HashMap();
		final Map<String, String> termNameMap = new HashMap();
		// kinds
		for (final String kindName: kinds.keySet()) {
			String moduleName;
			if (undefinedKindNames.containsKey(kindName)) {
				final ParameterizedName pn = undefinedKindNames.get(kindName);
				moduleName = pn.getModulePrefix() + pn.getName();
			} else
				moduleName = prefix + kindName;
			if (moduleData.getKind(moduleName) == null) {
				logger.error("Kind " + kindName + " unknown during export.");
				throw new DataException("Kind unknown during export", kindName);
			}
			kindNameMap.put(kindName, moduleName);
		}
		// terms
		for (final ComplexTerm term: terms.values()) {
			final String key = term.getName();
			String moduleName = key;
			if (undefinedTermNames.containsKey(moduleName)) {
				final ParameterizedName pn = undefinedTermNames.get(moduleName);
				moduleName = pn.getModulePrefix() + pn.getName();
			} else
				moduleName = prefix + moduleName;
			final ComplexTerm moduleTerm = moduleData.getTerm(moduleName);
			if (moduleTerm == null) {
				logger.error("Term " + term + " unknown during export.");
				throw new DataException("Term unknown during export", term.toString());
			}
			// result kind
			final Kind resultKind = term.getKind();
			if (resultKind != null) {
				if (!moduleTerm.getKind()
						.equals(moduleData.getKind(kindNameMap.get(resultKind.getName())))) {
					logger.error("Result kind mismatch during export of term " + term);
					logger.error("Needed result kind: "
						+ moduleData.getKind(kindNameMap.get(resultKind.getName())));
					logger.error("Found result kind:  " + moduleTerm.getKind());
					throw new DataException("Result kind mismatch during term export", term.toString());
				}
			} else
				assert (undefinedTermNames.containsKey(term.getName())): "Unknown result kind for known term.";
			// place count
			final int placeCount = term.placeCount();
			assert (placeCount >= 0): "Negative place count in term during export.";
			if (placeCount != moduleTerm.placeCount()) {
				logger.error("Place count mismatch during export of term " + term);
				logger.error("Needed place count: " + placeCount);
				logger.error("Found place count:  " + moduleTerm.placeCount());
				throw new DataException("Place count mismatch during term export", term.toString());
			}
			// input kinds
			for (int i = 0; i != placeCount; ++i) {
				final Kind inputKind = term.getInputKind(i);
				if (inputKind != null) {
					if (!moduleTerm.getInputKind(i)
							.equals(moduleData
							.getKind(kindNameMap.get(inputKind.getName())))) {
						logger.error("Input kind mismatch during export of term " + term);
						logger.error("Position: " + (i+1));
						logger.error("Needed input kind: "
							+ moduleData.getKind(kindNameMap.get(inputKind.getName())));
						logger.error("Found input kind:  " + moduleTerm.getInputKind(i));
						throw new DataException("Input kind mismatch during term export", term.toString());
					}
				} else
					assert (undefinedTermNames.containsKey(key)): "Unknown input kind for known term.";
			}
			termNameMap.put(key, moduleName);
		}
		// symbols
		for (final Symbol symbol: symbols.values()) {
			if (symbol.isVariable())
				continue;
			assert (symbol instanceof StatementImpl): "Statement not from this implementation.";
			final StatementImpl statement = (StatementImpl) symbol;
			final StatementImpl moduleStatement = moduleData.getStatement(prefix + statement.getName());
			if (moduleStatement == null) {
				logger.error("Statement " + statement + " unknown during export.");
				throw new DataException("Statement unknown during export", statement.toString());
			}
			// adapt expressions
			final Map<Variable, Variable> varMap = new HashMap();
			final List<TermExpressionImpl> hypotheses = new ArrayList();
			for (final TermExpression hypothesis: statement.getHypotheses()) {
				assert (hypothesis instanceof TermExpressionImpl): "hypothesis not from this implementation.";
				hypotheses.add(((TermExpressionImpl) hypothesis)
					.adapt(moduleData, kindNameMap, termNameMap, varMap));
			}
			final TermExpressionImpl consequent = statement.getConsequent()
				.adapt(moduleData, kindNameMap, termNameMap, varMap);
			// compare expressions
			final Map<Variable, Variable> translationMap = new HashMap();
			final List<TermExpression> moduleHypotheses = moduleStatement.getHypotheses();
			final int numHyps = hypotheses.size();
			if (numHyps != moduleHypotheses.size()) {
				logger.error("Hypothesis count mismatch in statement " + statement + " during export.");
				logger.error("Expected # of hypotheses: " + moduleHypotheses.size());
				logger.error("Received # of hypotheses: " + numHyps);
				throw new DataException("Hypothesis count mismatch during export", statement.toString());
			}
			for (int i = 0; i != numHyps; ++i) {
				final TermExpression moduleHypothesis = moduleHypotheses.get(i);
				assert (moduleHypothesis instanceof TermExpressionImpl): "Module hypothesis not from this implementation.";
				hypotheses.get(i).equalityMap((TermExpressionImpl) moduleHypothesis, translationMap);
			}
			consequent.equalityMap(moduleStatement.getConsequent(), translationMap);
			// check DV constraints
			final DVConstraintsImpl moduleDV = new DVConstraintsImpl(moduleStatement.getDVConstraints());
			final List<SortedSet<Variable>> rawDVConstraints = statement.getDVConstraints().adapt(moduleData, kindNameMap, varMap);
			final DVConstraintsImpl dvConstraints = new DVConstraintsImpl();
			for (final SortedSet<Variable> rawDVConstraint: rawDVConstraints)
				dvConstraints.add(rawDVConstraint);
			for (final VariablePair p: dvConstraints) {
				if (!moduleDV.remove(new VariablePairImpl(translationMap.get(p.getFirst()), translationMap.get(p.getSecond())))) {
					logger.error("Missing DV constraints in target of " + statement + " during export.");
					logger.error("Offending constraint: " + p);
					throw new DataException("Missing DV constraints in export target", statement.toString());
				}
			}
			if (!moduleDV.isEmpty()) {
				logger.error("Superfluous DV constraints in target of " + statement + " during export.");
				logger.error("Superfluous constraints: " + moduleDV);
				throw new DataException("Superfluous DV constraints during export", statement.toString());
			}
		}
		unsetParameters();
	}

	/**
	 * Sets the module parameters of the undefined names.
	 */
	private void setParameters(final Map<String, ParameterImpl> parameterMap) {
		for (final ParameterizedName pn: undefinedKindNames.values()) {
			final String name = pn.getInterfaceName();
			if (parameterMap.containsKey(name))
				pn.setModuleParameter(parameterMap.get(name));
			else
				pn.setModuleParameter(); // FIXME: for null parameters (currently not implemented)
		}
		for (final ParameterizedName pn: undefinedTermNames.values()) {
			final String name = pn.getInterfaceName();
			if (parameterMap.containsKey(name))
				pn.setModuleParameter(parameterMap.get(name));
			else
				pn.setModuleParameter(); // for MAIN parameters
		}
	}

	/**
	 * Unsets the module parameters of the undefined names.
	 */
	private void unsetParameters() {
		for (final ParameterizedName pn: undefinedKindNames.values())
			pn.setModuleParameter(null);
		for (final ParameterizedName pn: undefinedTermNames.values())
			pn.setModuleParameter(null);
	}

	/**
	 * Checks whether the parameters provided by the module's import or export command
	 * satisfy the interface parameters specified by the param commands.
	 *
	 * @param moduleData the module data
	 * @param parameter the parameter these interface data should define, provided the import/export succeeds.
	 *
	 * @return a map mapping interface parameter names to their module parameters.
	 *
	 * @throws DataException if an argument does not verify, or if an interface cannot be loaded.
	 */
	private Map<String, ParameterImpl> checkParameters(final ModuleDataImpl moduleData,
		final ParameterImpl parameter)
	throws DataException {
		final List<Parameter> parameterList = parameter.getParameterList();
		final int paramCount = parameterList.size();
		final DataFactoryImpl df = DataFactoryImpl.getInstance();
		if (paramCount != parameters.size()) {
			logger.error("Wrong number of parameters while in interface " + parameter.toString());
			logger.error("Expected number of parameters: " + parameters.size() + " (" + parameters.keySet() + ")");
			logger.error("Received number of parameters: " + paramCount + " (" + parameterList + ")");
			throw new DataException("Wrong number of parameters", parameter.toString());
		}
		final Map<String, ParameterImpl> result = new HashMap();
		int i = 0;
		for (final Map.Entry<String,ParameterImpl> ifParamMapping: parameters.entrySet()) {
			final ParameterImpl ifParam = ifParamMapping.getValue();
			final ParameterImpl argument = (ParameterImpl) parameterList.get(i);
			result.put(ifParamMapping.getKey(), argument);
			if (!(ifParam.getLocator().equals(argument.getLocator()) && ifParam.getPrefix().equals(argument.getPrefix()))) {
				logger.info("Checking whether parameter " + argument + " satisfies " + parameter);
				try {
					final InterfaceDataImpl ifdata = df.getInterfaceData(ifParam.getLocator());
					ifdata.exportFrom(moduleData, ifParam);
				} catch (DataException e) {
					logger.error("Parameter " + argument + " does not satisfy " + ifParam);
					throw new DataException("Parameter satisfaction error", argument.toString(), e);
				} catch (InputException e) {
					logger.error("Unable to load interface " + ifParam);
					throw new DataException("Unable to load interface", ifParam.toString(), e);
				}
			}
			++i;
		}
		return result;
	}

	public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
		final int version = in.readInt();
		if (version != jhilbert.Main.VERSION)
			throw new ClassNotFoundException("Obsolete library format version " + version);
		try {
			undefinedKindNames = (Map<String, ParameterizedName>) in.readObject();
			kinds = (LinkedHashMap<String, KindImpl>) in.readObject();
			undefinedTermNames = (Map<String, ParameterizedName>) in.readObject();
			terms = (Map<String, ComplexTerm>) in.readObject();
			symbols = (Map<String, Symbol>) in.readObject();
			parameters = (LinkedHashMap<String, ParameterImpl>) in.readObject();
		} catch (ClassCastException e) {
			logger.error("Wrong class during deserialization of interface data.");
			throw new ClassNotFoundException("Wrong class during deserialization of interface data.", e);
		}
	}

	public void writeExternal(final ObjectOutput out) throws IOException {
		out.writeInt(jhilbert.Main.VERSION);
		out.writeObject(undefinedKindNames);
		out.writeObject(kinds);
		out.writeObject(undefinedTermNames);
		out.writeObject(terms);
		out.writeObject(symbols);
		out.writeObject(parameters);
	}

}
