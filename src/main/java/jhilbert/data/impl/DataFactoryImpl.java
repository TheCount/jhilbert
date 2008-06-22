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

import jhilbert.data.Data;
import jhilbert.data.DataException;
import jhilbert.data.DataFactory;
import jhilbert.data.DVConstraints;
import jhilbert.data.InterfaceData;
import jhilbert.data.ModuleData;
import jhilbert.data.Parameter;
import jhilbert.data.TermExpression;
import jhilbert.data.Variable;
import jhilbert.data.impl.FileBasedDataFactory;
import jhilbert.data.impl.InterfaceDataImpl;
import jhilbert.data.impl.ModuleDataImpl;
import jhilbert.data.impl.ParameterImpl;
import jhilbert.data.impl.TermExpressionImpl;
import jhilbert.util.InputException;
import jhilbert.util.LRUCache;
import jhilbert.util.TokenScanner;

/**
 * Abstract implementation of {@link DataFactory}.
 * This implementation implements all methods except
 * {@link #loadInterfaceData}, whose implementation
 * depends on the underlying data organization
 * (files, SQL db, etc.).
 */
public abstract class DataFactoryImpl extends DataFactory {

	/**
	 * The global data factory instance.
	 * FIXME: This field should be set according to user input.
	 */
	private static final DataFactoryImpl instance = new FileBasedDataFactory();

	/**
	 * Interface cache.
	 * FIXME: This field should be set according to user input.
	 */
	private static final LRUCache<String, InterfaceDataImpl> interfaceCache = new LRUCache(10);

	public static final DataFactoryImpl getInstance() {
		return instance;
	}

	public final @Override ModuleDataImpl createModuleData() {
		return new ModuleDataImpl();
	}

	/**
	 * Implementation specific interface loader.
	 * This method is called by {@link #loadInterfaceData} and should be implemented by subclasses.
	 *
	 * @param locator locator for the interface.
	 *
	 * @return interface data according to the locator.
	 *
	 * @throws InputException if the interface data could not be loaded.
	 */
	protected abstract InterfaceDataImpl loadInterfaceData(String locator) throws InputException;

	public final @Override InterfaceDataImpl getInterfaceData(final String locator) throws InputException {
		if (!interfaceCache.containsKey(locator))
			interfaceCache.put(locator, loadInterfaceData(locator));
		return interfaceCache.get(locator);
	}

	public final @Override TermExpression createTermExpression(final Variable var) {
		return new TermExpressionImpl(var);
	}

	public final @Override TermExpressionImpl scanTermExpression(final TokenScanner scanner, final Data data)
	throws DataException {
		assert (data instanceof DataImpl): "Data not from this implementation.";
		return new TermExpressionImpl(scanner, (DataImpl) data);
	}

	public final @Override DVConstraintsImpl createDVConstraints() {
		return new DVConstraintsImpl();
	}

	public final @Override void importInterface(final ModuleData moduleData, final InterfaceData interfaceData,
		final Parameter parameter)
	throws DataException {
		assert (moduleData != null): "Supplied module data are null.";
		assert (interfaceData != null): "Supplied interface data are null.";
		assert (parameter != null): "Supplied parameter is null.";
		assert (moduleData instanceof ModuleDataImpl): "Module data not from this implementation.";
		assert (interfaceData instanceof InterfaceDataImpl): "Interface data not from this implementation.";
		assert (parameter instanceof ParameterImpl): "Parameter not from this implementation";
		((InterfaceDataImpl) interfaceData).importInto((ModuleDataImpl) moduleData, (ParameterImpl) parameter);
	}

	public final @Override void exportInterface(final ModuleData moduleData, final InterfaceData interfaceData,
		final Parameter parameter)
	throws DataException {
		assert (moduleData != null): "Supplied module data are null.";
		assert (interfaceData != null): "Supplied interface data are null.";
		assert (parameter != null): "Supplied parameter is null.";
		assert (moduleData instanceof ModuleDataImpl): "Module data not from this implementation.";
		assert (interfaceData instanceof InterfaceDataImpl): "Interface data not from this implementation.";
		assert (parameter instanceof ParameterImpl): "Parameter not from this implementation";
		((InterfaceDataImpl) interfaceData).exportFrom((ModuleDataImpl) moduleData, (ParameterImpl) parameter);
	}

}
