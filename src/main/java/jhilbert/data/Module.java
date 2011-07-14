/*
    JHilbert, a verifier for collaborative theorem proving
    Copyright © 2008, 2009 Alexander Klauer

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

    You may contact the author on this Wiki page:
    http://www.wikiproofs.de/w/index.php?title=User_talk:GrafZahl
*/

package jhilbert.data;

import java.io.Serializable;
import java.util.List;

/**
 * A Module.
 * Modules are data structures which contain the namespaces for all
 * {@link Kind}, {@link Functor}, {@link Symbol} and {@link Parameter}
 * data defined in one single module.
 */
public interface Module extends Serializable {

	/**
	 * Obtains the name of the module.
	 * The name of the module is an identifier for the interface in which
	 * the data are defined. If this module contains the data of the
	 * current main proof module, the empty string is returned.
	 *
	 * @return module name, or the empty string, if this is the main proof
	 * 	module.
	 */
	public String getName();

	/**
	 * Obtains the revision number of the current module.
	 * Revision numbers allow for simple versioning of interfaces. They are
	 * useful for a storage backend to decide where the data goes.
	 *
	 * @return the revision number of this module, which must be a
	 * 	non-negative integer, or <code>-1</code> if this module is not
	 * 	versioned.
	 */
	public long getRevision();

	/**
	 * Obtains the parameters of this module in proper order.
	 *
	 * @return parameters of this module.
	 */
	public List<Parameter> getParameters();

	/**
	 * Obtains the parameter by the specified name.
	 *
	 * @param name name of parameter.
	 *
	 * @return parameter with name <code>name</code>, or <code>null</code>
	 * 	if no such parameter exists in this module.
	 */
	public Parameter getParameter(String name);

	/**
	 * Adds the specified {@link Parameter} to this <code>Module</code>.
	 *
	 * @param parameter parameter to be added.
	 *
	 * @throws DataException if a parameter with the same name as
	 * 	<code>parameter</code> has previously been added.
	 */
	public void addParameter(Parameter parameter) throws DataException;

	/**
	 * Obtains the {@link Namespace} containing the {@link Kind}s defined
	 * in this module.
	 *
	 * @return the kind namespace of this module.
	 */
	public Namespace<? extends Kind> getKindNamespace();

	/**
	 * Obtains the {@link Namespace} containing the {@link Symbol}s defined
	 * in this module.
	 *
	 * @return the symbol namespace of this module.
	 */
	public Namespace<? extends Symbol> getSymbolNamespace();

	/**
	 * Obtains the {@link Namespace} containing the {@link Functor}s
	 * defined in this module.
	 *
	 * @return the functor namespace of this module.
	 */
	public Namespace<? extends Functor> getFunctorNamespace();

	public boolean isProofModule();

}
