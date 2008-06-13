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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.SortedSet;
import jhilbert.data.Kind;
import jhilbert.data.Parameter;
import jhilbert.data.Statement;
import jhilbert.data.Symbol;
import jhilbert.data.Variable;
import jhilbert.exceptions.DataException;

/**
 * Interface for collecting data from JHilbert streams.
 * There are two implementations of this interface:
 * <ul>
 * <li>{@link InterfaceData} for interfaces.
 * <li>{@link ModuleData} for modules.
 * </ul>
 */
public interface Data {

	/**
	 * Obtains a kind.
	 *
	 * @param name name of the kind.
	 *
	 * @return kind with specified name, or <code>null</code> if no such kind exists.
	 *
	 * @throws DataException if a dependency could not be loaded.
	 */
	public Kind getKind(String name) throws DataException;

	/**
	 * Binds a previously defined kind to a new kind.
	 * This method merely creates an alias; no new kind will actually be created.
	 *
	 * @param oldKind previously defined kind.
	 * @param newKindName name of new kind to be bound to the old one.
	 *
	 * @throws DataException if the new kind already exists.
	 */
	public void bindKind(Kind oldKind, String newKindName) throws DataException;

	/**
	 * Defines a new term with the specified name.
	 * The specified variable list serves as the list of parameters for the term.
	 * In a {@link TermExpression}, the term so defined can be replaces by the specified definiens,
	 * with proper substitution of variables.
	 * The variables must have been previously defined by {@link #defineVariable()} and obtained by
	 * {@link #getVariable()}.
	 * The definiens must have been obtained by {@link DataFactory#scanTermExpression()} with this
	 * object as the data parameter.
	 *
	 * @param name name of the term.
	 * @param varList list of variables.
	 * @param definiens the definiens of this term.
	 *
	 * @throws DataException if a term with the same name as the specified term already exists.
	 */
	public void defineTerm(String name, LinkedHashSet<Variable> varList, TermExpression definiens)
	throws DataException;

	/**
	 * Obtains a symbol.
	 *
	 * @param name name of the symbol.
	 *
	 * @return {@link Symbol} with the specified name, or <code>null</code> if no such symbol exists.
	 */
	public Symbol getSymbol(String name);

	/**
	 * Obtains a local variable.
	 *
	 * @param name name of the variable.
	 *
	 * @return {@link Variable} with the specified name, or <code>null</code> if no such variable exists.
	 */
	public Variable getVariable(String name);
	
	/**
	 * Defines a new local variable.
	 *
	 * @param name name of the new variable (must not be <code>null</code>).
	 * @param kind Kind of the new variable (must not be <code>null</code>).
	 *
	 * @throws DataException if a symbol with the specified name already exists.
	 */
	public void defineVariable(String name, Kind kind) throws DataException;

	/**
	 * Obtains a statement.
	 *
	 * @param name name of the statement.
	 *
	 * @return {@link Statement} with the specified name, or <code>null</code> if no such statement exists.
	 */
	public Statement getStatement(String name);
	
	/**
	 * Defines a new Statement.
	 *
	 * @param name name of the statement.
	 * @param rawDV raw disjoint variable constraints.
	 * @param hypotheses List of hypotheses.
	 * @param consequent the consequent.
	 *
	 * @throws DataException if a symbol with the specified name already exists.
	 */
	public void defineStatement(String name, List<SortedSet<Variable>> rawDV, List<TermExpression> hypotheses,
		TermExpression consequent)
	throws DataException;

	/**
	 * Obtains a parameter.
	 *
	 * @param name name of the parameter.
	 *
	 * @return {@link Parameter} with the specified name, or <code>null</code> if no such parameter exists.
	 */
	public Parameter getParameter(String name);

	/**
	 * Defines a new parameter.
	 *
	 * @param name name of the parameter (must not be <code>null</code>).
	 * @param locator locator for the parameter (must not be <code>null</code>).
	 * @param paramList parameter list for the parameter (must not be <code>null</code>).
	 * @param prefix namespace prefix for the parameter (must not be <code>null</code>).
	 *
	 * @throws DataException if a parameter with the specified name already exists.
	 */
	public void defineParameter(String name, String locator, List<Parameter> paramList, String prefix)
	throws DataException;

}
