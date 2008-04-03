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

import jhilbert.data.Name;

/**
 * A Term.
 * A term can either be a {@link Variable} or an {@link AbstractComplexTerm}.
 */
public interface Term extends Name {

	/**
	 * Returns the kind of this term.
	 */
	public String getKind();

	/**
	 * Checks whether this term is a variable.
	 *
	 * @return <code>true</code> if this object is an instance of {@link Variable}, <code>false</code> otherwise.
	 */
	public boolean isVariable();

}
