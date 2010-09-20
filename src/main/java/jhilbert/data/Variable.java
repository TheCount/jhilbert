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
 * Interface for variables.
 */
public interface Variable extends Symbol, Term {

	/**
	 * Reimplemented from {@link Name#getOriginalName}, this method always
	 * returns <code>null</code> as variables are always local.
	 *
	 * @return <code>null</code>.
	 */
	public Name getOriginalName();

	/**
	 * Returns whether this <code>Variable</code> is a dummy variable.
	 *
	 * @return <code>true</code> if this <code>Variable</code> is a dummy
	 * 	variable, <code>false</code> otherwise.
	 */
	public boolean isDummy();

}
