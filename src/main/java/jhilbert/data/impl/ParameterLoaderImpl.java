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

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jhilbert.data.*;

import jhilbert.expressions.Expression;
import jhilbert.expressions.ExpressionException;
import jhilbert.expressions.ExpressionFactory;
import jhilbert.expressions.Matcher;
import jhilbert.expressions.Substituter;
import jhilbert.expressions.Translator;
import jhilbert.expressions.UnifyException;

import jhilbert.storage.Storage;
import jhilbert.storage.StorageException;

import org.apache.log4j.Logger;

/**
 * {@link ParameterLoader} implementation.
 */
final class ParameterLoaderImpl implements ParameterLoader {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(ParameterLoaderImpl.class);

	/**
	 * Data factory.
	 */
	private static final jhilbert.data.DataFactory dataFactory = jhilbert.data.DataFactory.getInstance();

	/**
	 * Parameter.
	 */
	private final Parameter parameter;

	/**
	 * Parameter list of <code>parameter</code>.
	 */
	private final List<Parameter> parameterList;

	/**
	 * Prefix.
	 */
	private final String prefix;

	/**
	 * Module.
	 */
	private final Module module;

	/**
	 * Index of parameter in current module.
	 */
	private final int parameterIndex;

	/**
	 * Kind namespace in current module.
	 */
	private final Namespace<? extends Kind> kindNamespace;

	/**
	 * Functor namespace in current module.
	 */
	private final Namespace<? extends Functor> functorNamespace;

	/**
	 * Symbol namespace in current module.
	 */
	private final Namespace<? extends Symbol> symbolNamespace;

	/**
	 * Parameter module.
	 */
	private final Module parameterModule;

	/**
	 * Kind namespace in parameter module.
	 */
	private final Namespace<? extends Kind> parameterKindNamespace;

	/**
	 * Functor namespace in parameter module.
	 */
	private final Namespace<? extends Functor> parameterFunctorNamespace;

	/**
	 * Symbol namespace in parameter module.
	 */
	private final Namespace<? extends Symbol> parameterSymbolNamespace;
	
	/**
	 * Map from parameter module kinds to kinds of current module.
	 */
	private final IdentityHashMap<Kind, Kind> kindMap;

	/**
	 * Map from parameter module functors to functors of current module.
	 */
	private final IdentityHashMap<Functor, Functor> functorMap;

	/**
	 * Map from parameter module statements to statements of current module.
	 */
	private final IdentityHashMap<Statement, Statement> statementMap;

	/**
	 * Expression factory.
	 */
	private final ExpressionFactory expressionFactory;

	/**
	 * Expression translator.
	 */
	private final Translator translator;

	/**
	 * Expression substituter.
	 */
	private final Substituter substituter;

	/**
	 * Creates a new <code>ParameterLoaderImpl</code> to load the specified
	 * parameter into the specified module.
	 *
	 * @param parameter parameter to load/import/export.
	 * @param module module to load/import/export parameter into.
	 *
	 * @throws DataException if the parameter module cannot be loaded.
	 */
	ParameterLoaderImpl(final Parameter parameter, final Module module) throws DataException {
		assert (parameter != null): "Supplied parameter is null";
		assert (module != null): "Supplied module is null";
		try {
			this.parameter = parameter;
			parameterList = parameter.getParameterList();
			prefix = parameter.getPrefix();
			this.module = module;
			parameterIndex = module.getParameters().size();
			kindNamespace = module.getKindNamespace();
			functorNamespace = module.getFunctorNamespace();
			symbolNamespace = module.getSymbolNamespace();
			parameterModule = Storage.getInstance().loadModule(parameter.getLocator(), parameter.getRevision());
			if (parameterList.size() != parameterModule.getParameters().size()) {
				logger.error("Wrong parameter count in parameter " + parameter.getName());
				logger.debug("Expected number of parameters: " + parameterModule.getParameters().size());
				logger.debug("Found number of parameters:    " + parameterList.size());
				throw new DataException("Wrong parameter count");
			}
			parameterKindNamespace = parameterModule.getKindNamespace();
			parameterFunctorNamespace = parameterModule.getFunctorNamespace();
			parameterSymbolNamespace = parameterModule.getSymbolNamespace();
			kindMap = new IdentityHashMap();
			functorMap = new IdentityHashMap();
			statementMap = new IdentityHashMap();
			expressionFactory = ExpressionFactory.getInstance();
			translator = expressionFactory.createTranslator(kindMap, functorMap);
			substituter = expressionFactory.createSubstituter(new IdentityHashMap());
		} catch (StorageException e) {
			logger.error("Unable to load module " + parameter.getLocator(), e);
			logger.debug("Requested revision: " + parameter.getRevision());
			throw new DataException("Unable to load module", e);
		}
	}

	public void loadParameter() throws DataException {
		loadKinds();
		loadFunctors();
		checkFunctorMap();
		module.addParameter(parameter);
	}

	public void importParameter() throws DataException {
		assert ("".equals(module.getName())): "Attempt to import into interface module";
		loadKinds();
		loadFunctors();
		loadStatements();
		checkFunctorMap();
		checkStatementMap();
		module.addParameter(parameter);
	}

	public void exportParameter() throws DataException {
		assert ("".equals(module.getName())): "Attempt to export from interface module";
		exportKinds();
		exportFunctors();
		exportStatements();
		checkFunctorMap();
		checkStatementMap();
		module.addParameter(parameter);
	}

	private void loadKinds() throws DataException {
		for (final Kind parameterKind: parameterKindNamespace.objects())
			if (!loadAsNewKind(parameterKind))
				mapKnownName(parameterKind, kindMap, kindNamespace);
		// create aliases
		for (final Map.Entry<String, ? extends Kind> aliasEntry: parameterKindNamespace.aliases().entrySet()) {
			final Kind kindHere = kindMap.get(aliasEntry.getValue());
			final Kind newKind = dataFactory.createKind(prefix + aliasEntry.getKey(),
				kindHere.getOriginalName(), kindHere.getParameterIndex(), kindNamespace);
			kindNamespace.identify(newKind, kindHere);
		}
		// create additional equivalence classes
		for (final Set<? extends Kind> equivalenceClass: parameterKindNamespace.equivalenceClasses())
			for (final Kind i: equivalenceClass)
				for (final Kind j: equivalenceClass)
					kindNamespace.identify(kindMap.get(i), kindMap.get(j));
					// FIXME: This becomes inefficient quickly if kindbind is used heavily
	}

	private void loadFunctors() throws DataException {
		for (final Functor parameterFunctor: parameterFunctorNamespace.objects())
			if (!loadAsNewFunctor(parameterFunctor))
				mapKnownName(parameterFunctor, functorMap, functorNamespace);
	}

	private void loadStatements() throws DataException {
		for (final Symbol parameterSymbol: parameterSymbolNamespace.objects()) {
			if (parameterSymbol.isVariable())
				continue;
			final Statement parameterStatement = (Statement) parameterSymbol;
			if (!loadAsNewStatement(parameterStatement)) // should not happen
				mapKnownName(parameterStatement, statementMap, symbolNamespace);
		}
	}

	private void exportKinds() throws DataException {
		for (final Kind parameterKind: parameterKindNamespace.objects())
			if (!mapNewName(parameterKind, kindMap, kindNamespace))
				mapKnownName(parameterKind, kindMap, kindNamespace);
		// check aliases
		for (final Map.Entry<String, ? extends Kind> aliasEntry: parameterKindNamespace.aliases().entrySet()) {
			final Kind kind1 = kindMap.get(aliasEntry.getValue());
			final Kind kind2 = kindNamespace.getObjectByString(prefix + aliasEntry.getKey());
			if (!kind1.equals(kind2)) {
				logger.error("Kind export error: two aliased kinds in interface are inequivalent in "
					+ "proof module");
				logger.debug("Kind mapping:  " + aliasEntry.getValue() + " -> " + kind1);
				logger.debug("Alias mapping: " + aliasEntry.getKey() + " -> " + kind2);
				throw new DataException("Export kind alias equivalence error");
			}
		}
		// check equivalence classes
		for (final Set<? extends Kind> equivalenceClass: parameterKindNamespace.equivalenceClasses())
			for (final Kind i: equivalenceClass)
				for (final Kind j: equivalenceClass)
					if (!kindMap.get(i).equals(kindMap.get(j))) {
						logger.error("Kind export error: two equivalent kinds in interface "
							+ "are inequivalent in proof module");
						logger.debug("First kind mapping:  " + i + " -> " + kindMap.get(i));
						logger.debug("Second kind mapping: " + j + " -> " + kindMap.get(j));
						throw new DataException("Export kind equivalence class error");
					}
	}

	private void exportFunctors() throws DataException {
		for (final Functor parameterFunctor: parameterFunctorNamespace.objects())
			if (!mapNewName(parameterFunctor, functorMap, functorNamespace))
				mapKnownName(parameterFunctor, functorMap, functorNamespace);
	}

	private void exportStatements() throws DataException {
		for (final Symbol parameterSymbol: parameterSymbolNamespace.objects()) {
			if (parameterSymbol.isVariable())
				continue;
			final Statement parameterStatement = (Statement) parameterSymbol;
			if (!mapNewName(parameterStatement, statementMap, symbolNamespace))
				mapKnownName(parameterStatement, statementMap, symbolNamespace);
		}
	}

	private boolean loadAsNewKind(final Kind parameterKind) throws DataException {
		if (!(parameterKind.getOriginalName() == null)) // not new
			return false;
		kindMap.put(parameterKind, dataFactory.createKind(prefix + parameterKind.getNameString(), parameterKind,
			parameterIndex, kindNamespace));
		return true;
	}

	private boolean loadAsNewFunctor(final Functor parameterFunctor) throws DataException {
		if (!(parameterFunctor.getOriginalName() == null)) // not new
			return false;
		if (parameterFunctor.definitionDepth() == 0) { // not a definition
			// translate kinds
			final List<? extends Kind> parameterFunctorInputKinds = parameterFunctor.getInputKinds();
			final List<Kind> inputKinds = new ArrayList(parameterFunctorInputKinds.size());
			for (final Kind parameterFunctorInputKind: parameterFunctorInputKinds)
				inputKinds.add(kindMap.get(parameterFunctorInputKind));
			functorMap.put(parameterFunctor,
				dataFactory.createFunctor(prefix + parameterFunctor.getNameString(), parameterFunctor,
					parameterIndex, kindMap.get(parameterFunctor.getKind()), inputKinds,
					functorNamespace));
			return true;
		}
		// functor is a definition
		final Definition parameterDefinition = (Definition) parameterFunctor;
		try {
			// translate arguments
			final List<Variable> argList = new ArrayList();
			for (final Variable parameterVariable: parameterDefinition.getArguments())
				argList.add(translator.translate(parameterVariable));
			functorMap.put(parameterFunctor,
				dataFactory.createDefinition(prefix + parameterFunctor.getNameString(),
					parameterDefinition, parameterIndex, argList,
					translator.translate(parameterDefinition.getDefiniens()), functorNamespace));
			return true;
		} catch (ExpressionException e) {
			logger.error("Unable to translate variable or expression", e);
			throw new DataException("Unable to translate variable or expression", e);
		}
	}

	private boolean loadAsNewStatement(final Statement parameterStatement) throws DataException {
		if (!(parameterStatement.getOriginalName() == null)) // not new (should not happen)
			return false;
		try {
			// translate DV constraints
			final DVConstraints dvConstraints = dataFactory.createDVConstraints();
			for (final Variable[] constraint: parameterStatement.getDVConstraints())
				dvConstraints.add(translator.translate(constraint[0]), translator.translate(constraint[1]));
			// translate hypotheses
			final List<Expression> parameterHypotheses = parameterStatement.getHypotheses();
			final List<Expression> hypotheses = new ArrayList(parameterHypotheses.size());
			for (final Expression parameterHypothesis: parameterHypotheses)
				hypotheses.add(translator.translate(parameterHypothesis));
			statementMap.put(parameterStatement,
				dataFactory.createStatement(prefix + parameterStatement.getNameString(),
				parameterStatement, parameterIndex, dvConstraints, hypotheses,
				translator.translate(parameterStatement.getConsequent()), symbolNamespace));
			return true;
		} catch (ExpressionException e) {
			logger.error("Unable to translate variable or expression", e);
			throw new DataException("Unable to translate variable or expression", e);
		}
	}

	private <T extends Name> boolean mapNewName(final T name, final Map<T, T> nameMap, final Namespace<?> namespace)
	throws DataException {
		if (name.getOriginalName() != null) // not new
			return false;
		final String nameHere = prefix + name.getNameString();
		final Name objectHere = namespace.getObjectByString(nameHere);
		if (objectHere == null) {
			logger.error("Parameter mismatch: object " + nameHere + " not found");
			logger.debug("Object class: " + name.getClass());
			logger.debug("Should be defined in the current proof module");
			throw new DataException("Parameter mismatch: object not found");
		}
		try {
			nameMap.put(name, (T) objectHere);
		} catch (ClassCastException e) {
			logger.error("Object " + nameHere + " is of the wrong type", e);
			logger.error("Expected type: " + name.getClass());
			logger.error("Actual type:   " + objectHere.getClass());
			throw new DataException("Object is of the wrong type", e);
		}
		return true;
	}

	private <T extends Name> void mapKnownName(final T name, final Map<T, T> nameMap, final Namespace<?> namespace)
	throws DataException {
		final int nameIndex = name.getParameterIndex();
		assert ((0 <= nameIndex) && (nameIndex < parameterList.size())): "Invalid parameter index";
		final String nameHere = parameterList.get(nameIndex).getPrefix() + name.getOriginalName().getNameString();
		final Name objectHere = namespace.getObjectByString(nameHere);
		if (objectHere == null) {
			logger.error("Parameter mismatch: object " + nameHere + " not found");
			logger.debug("Object class:         " + name.getClass());
			logger.debug("Index:                " + nameIndex);
			logger.debug("Should be defined in: " + parameterList.get(nameIndex));
			throw new DataException("Parameter mismatch: object not found");
		}
		try {
			nameMap.put(name, (T) objectHere);
		} catch (ClassCastException e) {
			logger.error("Object " + nameHere + " is of the wrong type", e);
			logger.error("Expected type: " + name.getClass());
			logger.error("Actual type:   " + objectHere.getClass());
			throw new DataException("Object is of the wrong type", e);
		}
	}

	private void checkFunctorMap() throws DataException {
		for (final Map.Entry<Functor, Functor> functorEntry: functorMap.entrySet()) {
			final Functor parameterFunctor = functorEntry.getKey();
			final Functor functor = functorEntry.getValue();
			// check result kind
			if (!kindMap.get(parameterFunctor.getKind()).equals(functor.getKind())) {
				logger.error("Kind mismatch in functor " + functor);
				logger.debug("Expected kind: " + kindMap.get(parameterFunctor.getKind()));
				logger.debug("Actual kind:   " + functor.getKind());
				throw new DataException("Kind mismatch in functor");
			}
			// check input kinds
			final List<? extends Kind> parameterInputKinds = parameterFunctor.getInputKinds();
			final List<? extends Kind> inputKinds = functor.getInputKinds();
			final int size = inputKinds.size();
			if (size != parameterInputKinds.size()) {
				logger.error("Place count mismatch in functor " + functor);
				logger.debug("Expected place count: " + parameterInputKinds.size());
				logger.debug("Actual place count:   " + size);
				throw new DataException("Place count mismatch in functor");
			}
			for (int i = 0; i != size; ++i)
				if (!kindMap.get(parameterInputKinds.get(i)).equals(inputKinds.get(i))) {
					logger.error("Input kind mismatch in functor " + functor);
					logger.debug("Index:         " + (i+1));
					logger.debug("Expected kind: " + kindMap.get(parameterInputKinds.get(i)));
					logger.debug("Actual kind:   " + inputKinds.get(i));
				}
			// check definition compatibility
			if (parameterFunctor.definitionDepth() == 0)
				continue;
			if (functor.definitionDepth() == 0) {
				logger.error("Attempt to satisfy definition with functor");
				logger.debug("Definition: " + parameterFunctor);
				logger.debug("Functor:    " + functor);
				throw new DataException("Attempt to satisfy definition with functor");
			}
			final Definition parameterDefinition = (Definition) parameterFunctor;
			final Definition definition = (Definition) functor;
			final LinkedHashSet<Variable> parameterArguments = parameterDefinition.getArguments();
			final LinkedHashSet<Variable> arguments = definition.getArguments();
			final List<Expression> parameterChildren = new ArrayList(parameterArguments.size());
			final List<Expression> children = new ArrayList(arguments.size());
			for (final Variable parameterVariable: parameterArguments)
				parameterChildren.add(expressionFactory.createExpression(parameterVariable));
			for (final Variable variable: arguments)
				children.add(expressionFactory.createExpression(variable));
			try {
				final Expression parameterExpression
					= translator.translate(expressionFactory.createExpression(parameterDefinition,
						parameterChildren));
				final Expression expression = expressionFactory.createExpression(definition, children);
				substituter.unify(parameterExpression, expression);
			} catch (UnifyException e) {
				logger.error("Definition " + definition + " not unifiable with " + parameterDefinition, e);
				logger.debug("Source: " + e.getSource());
				logger.debug("Target: " + e.getTarget());
				throw new DataException("Definition not unifiable");
			} catch (ExpressionException e) {
				logger.error("Unable to translate definition " + parameterDefinition, e);
				throw new DataException("Unable to translate definition", e);
			}
		}
	}

	private void checkStatementMap() throws DataException {
		for (final Map.Entry<Statement, Statement> statementEntry: statementMap.entrySet()) {
			final Statement parameterStatement = statementEntry.getKey();
			final Statement statement = statementEntry.getValue();
			final Matcher matcher = expressionFactory.createMatcher();
			// check hypotheses
			final List<Expression> parameterHypotheses = parameterStatement.getHypotheses();
			final List<Expression> hypotheses = statement.getHypotheses();
			final int size = hypotheses.size();
			if (parameterHypotheses.size() != size) {
				logger.error("Statement " + statement + " has wrong number of hypotheses");
				logger.debug("Expected number of hypotheses: " + parameterHypotheses.size());
				logger.debug("Actual number of hypotheses:   " + size);
				throw new DataException("Statement has wrong number of hypotheses");
			}
			try {
				for (int i = 0; i != size; ++i)
					if (!matcher.checkVEquality(translator.translate(parameterHypotheses.get(i)),
							hypotheses.get(i))) {
					logger.error("Hypothesis in " + statement + " does not match");
					logger.debug("Expected hypothesis:    "
						+ translator.translate(parameterHypotheses.get(i)));
					logger.debug("Actual hypothesis:      " + hypotheses.get(i));
					logger.debug("Current assignment map: " + matcher.getAssignmentMap());
					throw new DataException("Hypothesis does not match");
				}
			// check consequent
				if (!matcher.checkVEquality(translator.translate(parameterStatement.getConsequent()),
						statement.getConsequent())) {
					logger.error("Consequent of " + statement + " does not match");
					logger.debug("Expected consequent:    "
						+ translator.translate(parameterStatement.getConsequent()));
					logger.debug("Actual consequent:      " + statement.getConsequent());
					logger.debug("Current assignment map: " + matcher.getAssignmentMap());
					throw new DataException("Consequent does not match");
				}
			// check DV constraints
				final Map<Variable, Variable> assignmentMap = matcher.getAssignmentMap();
				final DVConstraints dvConstraints = dataFactory.createDVConstraints();
				for (final Variable[] constraint: parameterStatement.getDVConstraints()) {
					assert (constraint.length == 2): "Invalid constraint length";
					dvConstraints.add(assignmentMap.get(translator.translate(constraint[0])),
						assignmentMap.get(translator.translate(constraint[1])));
				}
				if (!dvConstraints.contains(statement.getDVConstraints())) {
					logger.error("Statement " + statement + " has more DV constraints than "
						+ parameterStatement);
					throw new DataException("Statement has too many DV constraints");
				}
			} catch (ExpressionException e) {
				logger.error("Unable to translate statement " + parameterStatement, e);
				throw new DataException("Unable to translate statement", e);
			} catch (NullPointerException e) {
				throw new AssertionError("Unrestricted DV constraints");
			}
		}
	}

}
