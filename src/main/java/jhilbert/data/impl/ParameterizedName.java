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

import java.io.Serializable;
import jhilbert.data.impl.DataImpl;
import jhilbert.data.impl.ParameterImpl;

/**
 * A name accompanied with parameter data.
 */
class ParameterizedName implements Serializable {

	/**
	 * Serialization ID.
	 */
	private static final long serialVersionUID = DataImpl.FORMAT_VERSION;

	/**
	 * The name.
	 */
	private String name;

	/**
	 * Interface parameter first introducing the name.
	 */
	private ParameterImpl interfaceParameter;

	/**
	 * Module parameter provided during export or import of the interface.
	 */
	private transient ParameterImpl moduleParameter;

	/**
	 * Creates a new parameterized name.
	 *
	 * @param parameter interface parameter providing the name (must not be <code>null</code>).
	 * @param name the name (must not be <code>null</code>).
	 */
	ParameterizedName(final ParameterImpl parameter, final String name) {
		assert (parameter != null): "Supplied parameter is null.";
		assert (name != null): "Supplied name is null.";
		interfaceParameter = parameter;
		moduleParameter = null;
		this.name = name;
	}

	/**
	 * Creates an uninitalized parameterized name.
	 * Used by serialization.
	 */
	public ParameterizedName() {
		name = null;
		interfaceParameter = null;
		moduleParameter = null;
	}

	/**
	 * Returns the name.
	 *
	 * @return the name.
	 */
	String getName() {
		return name;
	}

	/**
	 * Returns the name of the interface parameter.
	 *
	 * @return name of the interface parameter.
	 */
	String getInterfaceName() {
		return interfaceParameter.toString();
	}

	/**
	 * Returns the prefix of the interface parameter.
	 *
	 * @return prefix of the interface parameter.
	 */
	String getInterfacePrefix() {
		return interfaceParameter.getPrefix();
	}

	/**
	 * Returns the prefix of the module parameter.
	 * <strong>Warning:</strong>
	 * The module parameter must have been initialised with {@link #setModuleParameter()}.
	 *
	 * @return the prefix of the module parameter.
	 */
	String getModulePrefix() {
		assert (moduleParameter != null): "Request for module prefix before module parameter initialization.";
		return moduleParameter.getPrefix();
	}

	/**
	 * Sets the module parameter.
	 *
	 * @param parameter the module parameter. A value of <code>null</code> unsets the module parameter.
	 */
	void setModuleParameter(final ParameterImpl parameter) {
		moduleParameter = parameter;
	}

	/**
	 * Sets the module parameter to the interface parameter.
	 */
	void setModuleParameter() {
		moduleParameter = interfaceParameter;
	}

	public @Override int hashCode() {
		return interfaceParameter.toString().hashCode() ^ name.hashCode();
	}

	public @Override boolean equals(final Object o) {
		try {
			final ParameterizedName pn = (ParameterizedName) o;
			return name.equals(pn.name)
				&& interfaceParameter.toString().equals(pn.interfaceParameter.toString())
				&& interfaceParameter.getPrefix().equals(pn.interfaceParameter.getPrefix());
		} catch (ClassCastException e) {
			return false;
		}
	}

}
