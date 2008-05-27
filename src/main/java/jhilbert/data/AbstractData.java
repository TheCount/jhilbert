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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import jhilbert.data.AbstractComplexTerm;
import jhilbert.data.Data;
import jhilbert.data.Kind;
import jhilbert.data.Parameter;
import jhilbert.data.Statement;
import jhilbert.data.Symbol;
import jhilbert.data.Variable;
import jhilbert.exceptions.DataException;
import org.apache.log4j.Logger;

/**
 * Skeletal implementation of the {@link Data} interface.
 * This class implements those methods specified in {@link Data} which are common to {@link InterfaceData}
 * and {@link ModuleData}.
 */
public abstract class AbstractData implements Data {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(AbstractData.class);

	/**
	 * Symbols.
	 */
	protected Map<String, Symbol> symbols;

	/**
	 * Parameters (insertion-ordered).
	 */
	protected final LinkedHashMap<String, Parameter> parameters;

	/**
	 * Initializes common data with empty collections.
	 */
	protected AbstractData() {
		symbols = new HashMap();
		parameters = new LinkedHashMap();
	}

	/**
	 * Copy constructor.
	 * Creates a shallow copy.
	 *
	 * @param data AbstractData to be copied (must not be <code>null</code>).
	 */
	protected AbstractData(final AbstractData data) {
		assert (data != null): "Supplied data are null.";
		symbols = data.symbols;
		parameters = data.parameters;
	}

	public abstract Kind getKind(String name);
	
	public abstract void defineKind(Kind kind) throws DataException;

	public abstract void bindKind(Kind oldKind, Kind newKind) throws DataException;

	public abstract AbstractComplexTerm getTerm(String name);

	public abstract void defineTerm(AbstractComplexTerm term) throws DataException;

	public Symbol getSymbol(final String name) {
		return symbols.get(name);
	}

	public Variable getVariable(final String name) {
		try {
			return (Variable) symbols.get(name);
		} catch (ClassCastException e) {
			logger.warn("The name " + name + " of the requested variable is the name of a statement.");
			return null;
		}
	}

	public Statement getStatement(final String name) {
		try {
			return (Statement) symbols.get(name);
		} catch (ClassCastException e) {
			logger.warn("The name " + name + " of the requested statement is the name of a variable.");
			return null;
		}
	}

	public void defineSymbol(final Symbol symbol) throws DataException {
		assert (symbol != null): "Supplied symbol is null.";
		final String name = symbol.getName();
		if (symbols.containsKey(name)) {
			logger.error("Symbol " + name + " is already defined.");
			throw new DataException("Attempted to define an already existing symbol", name);
		}
		symbols.put(name, symbol);
	}

	public Parameter getParameter(final String name) {
		return parameters.get(name);
	}

	public abstract AbstractData clone();

}
