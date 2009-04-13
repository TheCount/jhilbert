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

    You may contact the author on these Wiki pages:
    http://planetx.cc.vt.edu/AsteroidMeta//GrafZahl (preferred)
    http://en.wikisource.org/wiki/User_talk:GrafZahl
*/

package jhilbert.utils;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A {@link TreeNode} implementation using {@link ArrayList}s.
 * This means in particular that the iteration order over the list of children
 * matches the order in which the children were added to the node.
 *
 * @param E value type.
 */
public class ArrayTreeNode<E> implements TreeNode<E>, Serializable {

	/**
	 * Serialisation ID.
	 */
	private static final long serialVersionUID = jhilbert.Main.VERSION;

	/**
	 * Value.
	 */
	private E value;

	/**
	 * Children.
	 */
	private List<ArrayTreeNode<E>> children;

	/**
	 * Default constructor.
	 */
	public ArrayTreeNode() {
		super();
		value = null;
		children = new ArrayList(2); // HUGE memory eater if left at 10
	}

	/**
	 * Creates a new <code>ArrayTreeNode</code> with the specified value
	 * and no children.
	 *
	 * @param value value; <code>null</code> values are <em>not</em>
	 * 	permitted. Use the default constructor to create a null-valued
	 * 	node.
	 */
	public ArrayTreeNode(final E value) {
		assert (value != null): "Supplied value is null";
		this.value = value;
		children = new ArrayList(2); // HUGE memory eater if left at 10
	}

	public E getValue() {
		return value;
	}

	/**
	 * Sets the value of this <code>ArrayTreeNode</code>.
	 *
	 * @param value new value of this <code>ArrayTreeNode</code>.
	 */
	protected void setValue(final E value) {
		assert (value != null): "Supplied value is null";
		this.value = value;
	}

	public List<? extends TreeNode<E>> getChildren() {
		return Collections.unmodifiableList(children);
	}

	/**
	 * Sets the children of this <code>ArrayTreeNode</code>.
	 *
	 * @param children List of new children. The list must be modifiable.
	 */
	protected void setChildren(ArrayTreeNode<E>... children) {
		assert (children != null): "Supplied children are null";
		this.children = Arrays.asList(children);
	}

	public void addChild(final TreeNode<E> child) throws ClassCastException {
		children.add((ArrayTreeNode<E>) child);
	}

	public boolean isLeaf() {
		return (children.size() == 0);
	}

}
