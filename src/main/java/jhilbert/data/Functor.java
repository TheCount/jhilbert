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

import java.util.List;

/**
 * A <code>Functor</code> can be applied to
 * {@link jhilbert.expressions.Expression}s of prescribed
 * {@link Kind}s to yield a {@link Term}.
 */
public interface Functor extends Term {

	/**
	 * Returns an unmodifiable {@link List} of input {@link Kind}s.
	 *
	 * @return unmodifiable list of input kinds.
	 */
	public List<? extends Kind> getInputKinds();

	/**
	 * Returns the definition depth of this <code>Functor</code>.
	 * This functor is convertible to a {@link Definition} if and only if
	 * the returned depth is greater than zero.
	 *
	 * @return the definition depth, a non-negative integer.
	 */
	public int definitionDepth();

	public Namespace<? extends Functor> getNamespace();

	public Functor getOriginalName();

}
