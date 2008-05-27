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

import jhilbert.data.AbstractComplexTerm;
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
public interface Data extends Cloneable {

	/**
	 * Data file format version.
	 */
	public static final int FORMAT_VERSION = 1;

	/**
	 * Obtains a kind.
	 *
	 * @param name name of the kind.
	 *
	 * @return kind with specified name, or <code>null</code> if no such kind exists.
	 */
	public Kind getKind(String name);

	/**
	 * Defines a new kind.
	 *
	 * @param kind kind to be defined.
	 *
	 * @throws DataException if the kind already exists.
	 */
	public void defineKind(Kind kind) throws DataException;

	/**
	 * Binds a previously defined kind to a new kind.
	 *
	 * @param oldKind previously defined kind.
	 * @param newKind new kind to be bound to the old one.
	 *
	 * @throws DataException if the new kind already exists.
	 */
	public void bindKind(Kind oldKind, Kind newKind) throws DataException;

	/**
	 * Obtains a term.
	 *
	 * @param name name of the term.
	 *
	 * @return {@link AbstractComplexTerm} with the specified name, or <code>null</code> if no such term exists.
	 */
	public AbstractComplexTerm getTerm(String name);

	/**
	 * Defines a new term.
	 *
	 * @param term term to be defined.
	 *
	 * @throws DataException if a term with the same name as the specified term already exists, or if the
	 * 	specified term is incomplete.
	 */
	public void defineTerm(AbstractComplexTerm term) throws DataException;

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
	 * Obtains a statement.
	 *
	 * @param name name of the statement.
	 *
	 * @return {@link Statement} with the specified name, or <code>null</code> if no such statement exists.
	 */
	public Statement getStatement(String name);

	/**
	 * Defines a new symbol.
	 *
	 * @param symbol symbol to be defined (must not be <code>null</code>).
	 *
	 * @throws DataException if a symbol with the same name as the specified symbol already exists.
	 */
	public void defineSymbol(Symbol symbol) throws DataException;

	/**
	 * Obtains a parameter.
	 * FIXME: This method might not belong here, but in subclasses instead.
	 *
	 * @param name name of the parameter.
	 *
	 * @return {@link Parameter} with the specified name, or <code>null</code> if no such parameter exists.
	 */
	public Parameter getParameter(String name);

	public Data clone();

}
