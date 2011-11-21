/*
    JHilbert, a verifier for collaborative theorem proving

    Copyright © 2008, 2009, 2011 The JHilbert Authors
      See the AUTHORS file for the list of JHilbert authors.
      See the commit logs ("git log") for a list of individual contributions.

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

package jhilbert.utils;

import java.util.List;

/**
 * A node in a tree-like data structure.
 * Each <code>TreeNode</code> has a value and zero or more children, which
 * are themselves compatible tree nodes.
 *
 * @param E value type.
 */
public interface TreeNode<E> {

	/**
	 * Returns the value of this <code>TreeNode</code>.
	 *
	 * @return value of this tree node.
	 */
	public E getValue();

	/**
	 * Returns an unmodifiable {@link List} of children of this tree
	 * node.
	 *
	 * @return list of children of this tree node.
	 */
	public List<? extends TreeNode<E>> getChildren();

	/**
	 * Adds a child to this <code>TreeNode</code>.
	 *
	 * @param child child to add.
	 *
	 * @throws ClassCastException if <code>child</code> cannot be cast to
	 * 	the type of the implementing class.
	 */
	public void addChild(TreeNode<E> child) throws ClassCastException;

	/**
	 * Checks whether this <code>TreeNode</code> is a leaf node.
	 *
	 * @return <code>true</code> if this tree node has exactly zero
	 * 	children, <code>false</code> otherwise.
	 */
	public boolean isLeaf();

}
