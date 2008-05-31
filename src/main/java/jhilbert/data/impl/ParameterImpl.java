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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import jhilbert.data.Data;
import jhilbert.data.Parameter;
import jhilbert.data.impl.NameImpl;
import jhilbert.exceptions.DataException;
import jhilbert.util.DataInputStream;
import jhilbert.util.DataOutputStream;
import org.apache.log4j.Logger;

/**
 * Parameter data to denote an interface and the namespace prefix with which its data should be loaded.
 */
class ParameterImpl extends NameImpl implements Parameter {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(ParameterImpl.class);

	/**
	 * Locator.
	 */
	private final String locator;

	/**
	 * List of parameters to this parameter.
	 */
	private final List<Parameter> parameterList;

	/**
	 * Namespace prefix.
	 */
	private final String prefix;

	/**
	 * Creates a new parameter.
	 *
	 * @param name name of this parameter.
	 * @param locator locator for the interface this parameter denotes.
	 * @param parameterList list of parameters to be passed when loading the interface.
	 * @param prefix namespace prefix for this interface parameter.
	 */
	public ParameterImpl(final String name, final String locator, final List<Parameter> parameterList,
			final String prefix) {
		super(name);
		assert (locator != null): "Supplied locator is null.";
		assert (parameterList != null): "Supplied parameter list is null.";
		assert (prefix != null): "Supplied prefix is null.";
		this.locator = locator;
		this.parameterList = Collections.unmodifiableList(parameterList);
		this.prefix = prefix;
	}

	/**
	 * Loads a parameter from a data input stream.
	 *
	 * @param name name of this parameter.
	 * @param in data input stream to load parameter from.
	 * @param data context data.
	 * @param nameList list of names.
	 * @param lower lower bound for parameter name indices
	 * @param upper upper bound for parameter name indices
	 *
	 * @throws DataException if the input stream is inconsistent.
	 * @throws IOException if an I/O error occurs.
	 * @throws EOFException upon unexpected end of stream.
	 */
	// FIXME
	ParameterImpl(final String name, final DataInputStream in, final Data data, final List<String> nameList,
		final int lower, final int upper)
	throws DataException, IOException, EOFException {
		super(name);
		assert (in != null): "Supplied data input stream is null.";
		assert (data != null): "Supplied data are null.";
		locator = in.readString();
		final int parameterListSize = in.readNonNegativeInt();
		parameterList = new ArrayList(parameterListSize);
		for (int i = 0; i != parameterListSize; ++i) {
			final int nameIndex = in.readInt(lower, upper);
			final Parameter parameter = data.getParameter(nameList.get(nameIndex));
			if (parameter == null) {
				logger.error("Invalid parameter " + nameList.get(nameIndex) + " requested.");
				throw new DataException("Invalid parameter requested", name);
			}
			parameterList.add(parameter);
		}
		prefix = in.readString();
	}

	public String getLocator() {
		return locator;
	}

	/**
	 * Obtains the parameter list to this parameter.
	 *
	 * @return unmodifiable parameter list to this parameter.
	 */
	// FIXME
	public List<Parameter> getParameterList() {
		return parameterList;
	}

	/**
	 * Obtains the namespace prefix for this interface parameter.
	 *
	 * @return namespace prefix for this interface parameter.
	 */
	// FIXME
	public String getPrefix() {
		return prefix;
	}

	/**
	 * Stores this parameter in the specified data output stream.
	 *
	 * @param out output stream.
	 * @param parameterNameTable name to ID table for storing parameter names.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	// FIXME
	void store(final DataOutputStream out, final Map<String, Integer> parameterNameTable) throws IOException {
		out.writeString(locator);
		out.writeInt(parameterList.size());
		for (Parameter parameter: parameterList)
			out.writeInt(parameterNameTable.get(parameter.toString()));
		out.writeString(prefix);
	}

}
