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

import jhilbert.data.Data;
import jhilbert.data.DVConstraints;
import jhilbert.data.InterfaceData;
import jhilbert.data.ModuleData;
import jhilbert.data.Parameter;
import jhilbert.data.TermExpression;
import jhilbert.data.Variable;
import jhilbert.data.impl.DataFactoryImpl;
import jhilbert.exceptions.DataException;
import jhilbert.exceptions.InputException;
import jhilbert.util.TokenScanner;

/**
 * Factory for creating various kinds of data.
 * Data handled are
 * <ul>
 * <li> {@link ModuleData}
 * <li> {@link InterfaceData}
 * <li> Two types of {@link Variable}s generated on an as-needed basis.
 * </ul>
 */
public abstract class DataFactory {

	/**
	 * Returns the global data factory instance.
	 */
	public static DataFactory getInstance() {
		return DataFactoryImpl.getInstance();
	}

	/**
	 * Creates an empty instance of ModuleData.
	 */
	public abstract ModuleData createModuleData();

	/**
	 * Loads interface data.
	 *
	 * @param locator to determine which data to load.
	 *
	 * @throws InputException if the interface could not be loaded.
	 */
	public abstract InterfaceData loadInterfaceData(String locator) throws InputException;

	/**
	 * Creates a TermExpression consisting of a single variable.
	 *
	 * @param var the variable that is to be converted to a TermExpression.
	 */
	public abstract TermExpression createTermExpression(Variable var);

	/**
	 * Scans a new TermExpression from the specified token scanner based on the specified data.
	 *
	 * @param scanner the TokenScanner to scan the LISP expression.
	 * @param data the data to obtain {@link Variable}s and {@link ComplexTerm}s.
	 *
	 * @throws DataException if a problem with the scanner occurs, or if the scanned expression is invalid.
	 */
	public abstract TermExpression scanTermExpression(TokenScanner scanner, Data data) throws DataException;

	/**
	 * Creates empty disjoint variable constraints.
	 *
	 * @return empty disjoint variable constraints.
	 */
	public abstract DVConstraints createDVConstraints();

	/**
	 * Imports an interface into a module.
	 *
	 * @param moduleData data for the module the interface should be imported into.
	 * @param interfaceData data for the interface that is to be imported.
	 * @param parameter import parameter.
	 *
	 * @throws DataException if the import fails.
	 */
	public abstract void importInterface(ModuleData moduleData, InterfaceData interfaceData, Parameter parameter)
	throws DataException;

	/**
	 * Exports an interface from a module.
	 *
	 * @param moduleData data for the module the interface should be imported from.
	 * @param interfaceData data for the interface that is to be exported.
	 * @param parameter export parameter.
	 *
	 * @throws DataException if the export fails.
	 */
	public abstract void exportInterface(ModuleData moduleData, InterfaceData interfaceData, Parameter parameter)
	throws DataException;

}
