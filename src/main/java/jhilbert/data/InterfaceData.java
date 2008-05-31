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

import java.util.List;
import jhilbert.data.Data;
import jhilbert.data.Kind;
import jhilbert.exceptions.DataException;

/**
 * Data to represent the content of an interface.
 */
public interface InterfaceData extends Data {

	/**
	 * Defines a new kind.
	 * @param kindName name of the kind to be defined (must not be <code>null</code>).
	 *
	 * @throws DataException if a kind with the specified name already exists.
	 */
	public void defineKind(String kindName) throws DataException;

	/**
	 * Defines a new term with the specified name, result kind and input kinds.
	 * The result kind and the input kinds must first have been defined with the
	 * {@link #defineKind()} method and then obtained with the {@link #getKind()}
	 * method. Otherwise, the behavior of this method and all subsequent operations
	 * involving this object are undefined.
	 *
	 * @param termName name of the term to be defined (must not be <code>null</code>).
	 * @param resultKind the resulting Kind of the term (must not be <code>null</code>).
	 * @param inputKindList list of input kinds of the term (must not be <code>null</code>).
	 *
	 * @throws DataException if a term with the specified name already exists.
	 */
	public void defineTerm(String termName, Kind resultKind, List<Kind> inputKindList) throws DataException;

}
