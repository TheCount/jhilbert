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
 * A symbol ({@link Statement} or {@link Variable}).
 */
public interface Symbol extends Name {

	public Namespace<? extends Symbol> getNamespace();

	/**
	 * Returns whether this <code>Symbol</code> is a {@link Variable}.
	 *
	 * @return <code>true</code> if this <code>Symbol</code> is a
	 * 	{@link Variable}, <code>false</code> otherwise.
	 */
	public boolean isVariable();

}
