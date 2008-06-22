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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import jhilbert.data.Data;
import jhilbert.data.Parameter;
import jhilbert.data.impl.NameImpl;

/**
 * Parameter data to denote an interface and the namespace prefix with which its data should be loaded.
 */
class ParameterImpl extends NameImpl implements Parameter {

	/**
	 * Serialization ID.
	 */
	private static final long serialVersionUID = jhilbert.Main.VERSION;

	/**
	 * A special parameter for the "main" module.
	 */
	public static final ParameterImpl MAIN_PARAMETER
		= new ParameterImpl("(main)", "(none)", Collections.<Parameter>emptyList(), "");

	/**
	 * Locator.
	 */
	private String locator;

	/**
	 * List of parameters to this parameter.
	 */
	private List<Parameter> parameterList;

	/**
	 * Namespace prefix.
	 */
	private String prefix;

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
		this.parameterList = parameterList;
		this.prefix = prefix;
	}

	/**
	 * Creates an uninitialized Parameter.
	 * Used by serialization.
	 */
	public ParameterImpl() {
		super();
		locator = null;
		parameterList = null;
		prefix = null;
	}

	public String getLocator() {
		return locator;
	}

	/**
	 * Obtains the parameter list to this parameter.
	 *
	 * @return unmodifiable parameter list to this parameter.
	 */
	public List<Parameter> getParameterList() {
		return parameterList;
	}

	/**
	 * Obtains the namespace prefix for this interface parameter.
	 *
	 * @return namespace prefix for this interface parameter.
	 */
	public String getPrefix() {
		return prefix;
	}

}
