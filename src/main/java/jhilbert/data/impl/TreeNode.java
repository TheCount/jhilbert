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
import java.util.List;

/**
 * A node in a tree data structure.
 * <p>
 * Each TreeNode has:
 * <ul>
 * <li>a value (may be <code>null</code>),
 * <li>a list of child nodes (may be empty if this node is a leaf).
 * </ul>
 *
 * @param E value type.
 * @param T child type.
 */
class TreeNode<E, T extends TreeNode<E,T>> {

	/**
	 * Value of this node.
	 */
	private E value;

	/**
	 * List of children.
	 */
	private final List<T> children;

	/**
	 * Creates a new TreeNode with the specified value and no children.
	 *
	 * @param value value of this node.
	 */
	protected TreeNode(final E value) {
		this.value = value;
		this.children = new ArrayList(); // we use ArrayList() because we don't expect expensive removal operations
	}

	/**
	 * Creates a new TreeNode with neither value nor children.
	 */
	protected TreeNode() {
		this(null);
	}

	/**
	 * Adds a child to this node.
	 *
	 * @param child child to be added.
	 */
	public void addChild(final T child) {
		assert (child != null): "Supplied child is null.";
		children.add(child);
	}

	/**
	 * Returns the number of children of this node.
	 *
	 * @return number of children of this node.
	 */
	public int childCount() {
		return children.size();
	}

	/**
	 * Returns the i-th child of this node.
	 *
	 * @param i index of child to be returned (counting from zero).
	 *
	 * @return i-th child of this node.
	 *
	 * @throws IndexOutOfBoundsException if i is negative or <code>&gt;=</code> {@link #childCount()}.
	 */
	public T getChild(final int i) {
		return children.get(i);
	}

	/**
	 * Returns the children of this node.
	 *
	 * @return children of this node.
	 */
	protected List<T> getChildren() {
		return children;
	}

	/**
	 * Returns whether this is a leaf node.
	 *
	 * @return <code>true</code> if this node has no children, <code>false</code> otherwise.
	 */
	public boolean isLeaf() {
		return children.isEmpty();
	}

	/**
	 * Returns the value of this node.
	 *
	 * @return value of this node. Can be <code>null</code> only if a value has never been set.
	 */
	public E getValue() {
		return value;
	}

	/**
	 * Sets the value of this node.
	 *
	 * @param value value to be set.
	 */
	public void setValue(final E value) {
		this.value = value;
	}

}
