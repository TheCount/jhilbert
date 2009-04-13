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

package jhilbert.data.impl;

import java.io.Serializable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jhilbert.data.Functor;

/**
 * Basic implementation of the {@link Functor} interface.
 */
final class FunctorImpl extends AbstractFunctor implements Serializable {

	/**
	 * Serialisation ID.
	 */
	private static final long serialVersionUID = jhilbert.Main.VERSION;

	/**
	 * Kind.
	 */
	private final KindImpl kind;

	/**
	 * List of input kinds.
	 */
	private final List<KindImpl> inputKindList;

	/**
	 * Default constructor, for serialisation use only!
	 */
	public FunctorImpl() {
		super();
		kind = null;
		inputKindList = null;
	}

	/**
	 * Creates a new <code>FunctorImpl</code> with the specified name, kind
	 * and input kinds.
	 *
	 * @param name name of new functor.
	 * @param kind result kind of new functor.
	 * @param inputKinds List of input kinds.
	 */
	FunctorImpl(final String name, final KindImpl kind, final List<KindImpl> inputKinds) {
		this(name, null, -1, kind, inputKinds);
	}

	/**
	 * Creates a new <code>FunctorImpl</code> derived from the specified
	 * original functor with the specified name, kind, and input kinds.
	 *
	 * @param name name of new functor.
	 * @param orig original name, which may be <code>null</code> if this
	 * 	<code>FunctorImpl</code> is not derived from another one.
	 * @param parameterIndex index of parameter of <code>orig</code>.
	 * @param kind result kind of new functor.
	 * @param inputKinds List of input kinds.
	 */
	FunctorImpl(final String name, final FunctorImpl orig, final int parameterIndex, final KindImpl kind,
			final List<KindImpl> inputKinds) {
		super(name, orig, parameterIndex);
		assert (kind != null): "Supplied kind is null";
		this.kind = kind;
		assert (inputKinds != null): "Supplied input kinds are null";
		inputKindList = Collections.unmodifiableList(inputKinds);
	}

	public KindImpl getKind() {
		return kind;
	}

	public List<KindImpl> getInputKinds() {
		return inputKindList;
	}

	public int definitionDepth() {
		return 0;
	}

}
