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

import jhilbert.data.Kind;
import jhilbert.data.impl.NameImpl;
import jhilbert.util.IdentityHashSet;

/**
 * Implementation of {@link Kind}.
 */
final class KindImpl extends NameImpl implements Kind {

	/**
	 * Serialization ID.
	 */
	private static final long serialVersionUID = jhilbert.Main.VERSION;

	/**
	 * Bound kind ID.
	 */
	private static int boundKindID = 0;

	/**
	 * Bound kinds set.
	 */
	private Set<Kind> boundKinds;

	/**
	 * Creates a new kind with the specified name.
	 *
	 * @param name name of the new kind.
	 */
	KindImpl(final String name) {
		super(name);
		boundKinds = new IdentityHashSet();
		boundKinds.add(this);
	}

	/**
	 * Creates a new bound kind.
	 * The two specified kinds become undistinguishable during an equals comparison.
	 * It is permissible, though pointless, that the two kinds are identical.
	 *
	 * @param kind1 first kind (must not be <code>null</code>).
	 * @param kind2 second kind (must not be <code>null</code>).
	 */
	KindImpl(final KindImpl kind1, final KindImpl kind2) {
		super("(bound kind " + (boundKindID++) + ")");
		assert (kind1 != null): "Supplied first kind is null.";
		assert (kind2 != null): "Supplied second kind is null.";
		boundKinds = new IdentityHashSet();
		if (kind1.boundKinds != null)
			boundKinds.addAll(kind1.boundKinds);
		if (kind2.boundKinds != null)
			boundKinds.addAll(kind2.boundKinds);
		kind1.boundKinds = boundKinds;
		kind2.boundKinds = boundKinds;
		boundKinds.add(kind1);
		boundKinds.add(kind2);
		boundKinds.add(this);
	}

	/**
	 * Creates an uninitalized kind.
	 * Used by serialization.
	 */
	public KindImpl() {
		super();
		boundKinds = null;
	}

	public @Override boolean equals(final Object o) {
		return boundKinds.contains(o);
	}

}
