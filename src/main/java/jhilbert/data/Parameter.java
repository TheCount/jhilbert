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
 * An interface parameter.
 */
public interface Parameter extends Serializable {

	/**
	 * Returns the name of this parameter.
	 *
	 * @return name of this parameter.
	 */
	public String getName();

	/**
	 * Returns the locator for this parameter.
	 * The locator can be used to obtain the corresponding module.
	 *
	 * @return locator of this parameter.
	 */
	public String getLocator();

	/**
	 * Returns the revision of this parameter.
	 *
	 * @return revision of this parameter.
	 */
	public long getRevision();

	/**
	 * Returns the parameter list of this parameter.
	 *
	 * @return parameter list of this parameter.
	 */
	public List<Parameter> getParameterList();

	/**
	 * Returns the namespace prefix for this parameter.
	 *
	 * @return namespace prefix for this parameter.
	 */
	public String getPrefix();

}
