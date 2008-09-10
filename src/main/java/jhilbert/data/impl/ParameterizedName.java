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
import jhilbert.data.Name;
import jhilbert.data.impl.ParameterImpl;

/**
 * A name accompanied with parameter data.
 * Used for names imported through the param command.
 * This abstract class should be overridden whenever parameterization is needed for a class implementing {@link Name}.
 */
abstract class ParameterizedName implements Name {

	/**
	 * Serialization ID.
	 */
	// FIXME
	// private static final long serialVersionUID = jhilbert.Main.VERSION;

	/**
	 * Parameter.
	 */
	private ParameterImpl parameter;

	/**
	 * Creates a new parameterized name.
	 *
	 * @param parameter interface parameter providing the name (must not be <code>null</code>).
	 */
	protected ParameterizedName(final ParameterImpl parameter) {
		assert (parameter != null): "Supplied parameter is null.";
		this.parameter = parameter;
	}

	/**
	 * Creates an uninitalized parameterized name.
	 * Used by serialization.
	 */
	// FIXME
	//public ParameterizedName() {
	//	name = null;
	//	interfaceParameter = null;
	//	moduleParameter = null;
	//}

	/**
	 * Returns the full name.
	 *
	 * @return the full name.
	 */
	public final String getName() {
		return parameter.getPrefix() + this.getOriginalName();
	}

	/**
	 * Returns the original name.
	 */
	public abstract String getOriginalName();

	/**
	 * Returns the parameter.
	 *
	 * @return the parameter.
	 */
	public final ParameterImpl getParameter() {
		return parameter;
	}

	/**
	 * Returns the name of the interface parameter.
	 *
	 * @return name of the interface parameter.
	 */
	// FIXME
	// String getInterfaceName() {
	//	return interfaceParameter.getName();
	//}

	/**
	 * Returns the prefix of the interface parameter.
	 *
	 * @return prefix of the interface parameter.
	 */
	//String getInterfacePrefix() {
	//	return interfaceParameter.getPrefix();
	//}

	/**
	 * Returns the prefix of the module parameter.
	 * <strong>Warning:</strong>
	 * The module parameter must have been initialised with {@link #setModuleParameter()}.
	 *
	 * @return the prefix of the module parameter.
	 */
	// FIXME
	//String getModulePrefix() {
	//	assert (moduleParameter != null): "Request for module prefix before module parameter initialization.";
	//	return moduleParameter.getPrefix();
	//}

	/**
	 * Sets the module parameter.
	 *
	 * @param parameter the module parameter. A value of <code>null</code> unsets the module parameter.
	 */
	// FIXME
	//void setModuleParameter(final ParameterImpl parameter) {
	//	moduleParameter = parameter;
	//}

	/**
	 * Sets the module parameter to the interface parameter.
	 */
	// FIXME
	//void setModuleParameter() {
	//	moduleParameter = interfaceParameter;
	//}

	public @Override int hashCode() {
		return interfaceParameter.hashCode() ^ name.hashCode();
	}

	// FIXME
	//public @Override boolean equals(final Object o) {
	//	try {
	//		final ParameterizedName pn = (ParameterizedName) o;
	//		return name.equals(pn.name)
	//			&& interfaceParameter.getName().equals(pn.interfaceParameter.getName())
	//			&& interfaceParameter.getPrefix().equals(pn.interfaceParameter.getPrefix());
	//	} catch (ClassCastException e) {
	//		return false;
	//	}
	//}
	
	public int compareTo(final Name n) {
		return getName().compareTo(n.getName());
	}

	public @Override String toString() {
		return getOriginalName() + " parameterized by " + parameter;
	}

}
