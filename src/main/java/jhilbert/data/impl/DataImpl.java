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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import jhilbert.data.Data;
import jhilbert.data.DataException;
import jhilbert.data.Kind;
import jhilbert.data.Parameter;
import jhilbert.data.Statement;
import jhilbert.data.Symbol;
import jhilbert.data.Term;
import jhilbert.data.TermExpression;
import jhilbert.data.Variable;
import jhilbert.util.GrowList;
import org.apache.log4j.Logger;

/**
 * Skeletal implementation of the {@link Data} interface.
 * This class implements those methods specified in {@link Data} which are common to {@link InterfaceData}
 * and {@link ModuleData}.
 */
abstract class DataImpl implements Data {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(DataImpl.class);

	/**
	 * Terms.
	 */
	protected Map<String, ComplexTerm> terms;

	/**
	 * Symbols.
	 */
	protected Map<String, Symbol> symbols;

	/**
	 * Parameters (insertion-ordered).
	 */
	protected GrowList<String, ParameterImpl> parameters;

	/**
	 * Initializes common data with empty collections.
	 */
	protected DataImpl() {
		symbols = new HashMap();
		parameters = new GrowList();
	}

	public abstract Kind getKind(String name) throws DataException;

	public abstract void bindKind(Kind oldKind, String newKindName) throws DataException;

	public abstract ComplexTerm getTerm(String name) throws DataException;

	public void defineTerm(final String name, final LinkedHashSet<Variable> varList,
		final TermExpression definiens)
	throws DataException {
		assert (name != null): "Supplied name is null.";
		assert (varList != null): "Supplied variable list is null.";
		assert (definiens != null): "Supplied definiens is null.";
		if (terms.containsKey(name)) {
			logger.error("Unable to define definition: a term with name " + name + " already exists.");
			throw new DataException("Term already exists", name);
		}
		terms.put(name, new Definition(name, varList, definiens));
	}

	public Symbol getSymbol(final String name) {
		return symbols.get(name);
	}

	public Variable getVariable(final String name) {
		try {
			return (Variable) symbols.get(name);
		} catch (ClassCastException e) {
			logger.warn("The name " + name + " of the requested variable is the name of a statement.", e);
			return null;
		}
	}

	public void defineVariable(final String name, final Kind kind) throws DataException {
		assert (name != null): "Supplied name is null.";
		assert (kind != null): "Supplied kind is null.";
		if (symbols.containsKey(name)) {
			logger.error("Unable to define variable; a symbol with name " + name + " already exists.");
			throw new DataException("Unable to define variable", name);
		}
		symbols.put(name, new VariableImpl(name, kind));
	}

	public StatementImpl getStatement(final String name) {
		try {
			return (StatementImpl) symbols.get(name);
		} catch (ClassCastException e) {
			logger.warn("The name " + name + " of the requested statement is the name of a variable.");
			return null;
		}
	}

	public void defineStatement(final String name, final List<SortedSet<Variable>> rawDV,
		final List<TermExpression> hypotheses, final TermExpression consequent)
	throws DataException {
		assert (name != null): "Supplied name is null.";
		assert (rawDV != null): "Supplied disjoint variable constraints are null.";
		assert (hypotheses != null): "Supplied list of hypotheses is null.";
		assert (consequent != null): "Supplied consequent is null.";
		if (symbols.containsKey(name)) {
			logger.error("Unable to define statement; a symbol with name " + name + " already exists.");
			throw new DataException("Unable to define statement", name);
		}
		symbols.put(name, new StatementImpl(name, rawDV, hypotheses, consequent));
	}

	public Parameter getParameter(final String name) {
		return parameters.get(name);
	}

	public void defineParameter(final String name, final String locator, final List<Parameter> paramList,
		final String prefix)
	throws DataException {
		assert (name != null): "Supplied name is null.";
		assert (locator != null): "Supplied locator is null.";
		assert (paramList != null): "Supplied parameter list is null.";
		assert (prefix != null): "Supplied prefix is null.";
		if (parameters.containsKey(name)) {
			logger.error("A parameter with name " + name + " already exists.");
			throw new DataException("Parameter already exists", name);
		}
		parameters.put(name, new ParameterImpl(name, locator, paramList, prefix));
	}

}
