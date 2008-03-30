package jhilbert.data;

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
public class TreeNode<E, T extends TreeNode<E,T>> {

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
