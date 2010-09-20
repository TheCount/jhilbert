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

/**
 * Provides methods for loading, importing and exporting {@link Parameter}s.
 * Use the {@link DataFactory} to obtain an instance.
 * <p>
 * Implementations must provide a constructor accepting a parameter and a
 * module to perform loading, importing and exporting on.
 */
public interface ParameterLoader {

	/**
	 * Loads the parameter into the module in
	 * accordance with the semantics of the JHilbert &quot;param&quot;
	 * command. See the JHilbert specification for details.
	 *
	 * @throws DataException if loading the parameter fails.
	 */
	public void loadParameter() throws DataException;

	/**
	 * Imports the parameter into the proof module
	 * in accordance with the semantics of the JHilbert &quot;import&quot;
	 * command. See the JHilbert specification for details.
	 *
	 * @throws DataException if importing the parameter fails.
	 */
	public void importParameter() throws DataException;

	/**
	 * Exports the specified parameter from the specified proof module in
	 * accordance with the semantics of the JHilbert &quot;export&quot;
	 * command. See the JHilbert specification for details.
	 *
	 * @throws DataException if exporting the parameter fails.
	 */
	public void exportParameter() throws DataException;

}
