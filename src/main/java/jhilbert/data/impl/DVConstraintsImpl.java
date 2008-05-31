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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import jhilbert.data.DVConstraints;
import jhilbert.data.Variable;
import jhilbert.data.VariablePair;
import jhilbert.exceptions.DataException;
import org.apache.log4j.Logger;

/**
 * Class for managing distinct variable constraints.
 * This class performs no checks whatsoever that the specified VariablePairs are actually valid with respect to some {@link ModuleData}.
 * <p>
 * This set is symmetric: whenever (x, y) is in the set, so is (y, x), according to {@link #contains()}.
 * However, only (x, y) with x &lt; y is actually present in the set (and hence returned by an Iterator).
 * <p>
 * It is ensured that no elements of the form (x, x) can be added.
 */
class DVConstraintsImpl extends HashSet<VariablePair> implements DVConstraints {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(DVConstraintsImpl.class);

	/**
	 * Creates a new empty set of distinct variable constraints.
	 */
	public DVConstraintsImpl() {
		super();
	}

	/**
	 * Adds the specified element or its reverse.
	 *
	 * @param pair VariablePair to be added.
	 *
	 * @return <code>true</code> if this set has been changed, <code>false</code> otherwise.
	 *
	 * @throws NullPointerException if pair is <code>null</code>.
	 */
	public boolean add(final VariablePair pair) {
		assert (pair != null): "Supplied variable pair is null.";
		assert (pair instanceof VariablePairImpl): "variable pair not from this implementation.";
		if (pair.getFirst().equals(pair.getSecond()))
			return false;
		if (pair.getFirst().compareTo(pair.getSecond()) < 0)
			return super.add(pair);
		else
			return super.add(((VariablePairImpl) pair).reverse());
	}

	/**
	 * Adds a set of mutually distinct variables.
	 *
	 * @param varSet set of variables.
	 *
	 * @return <code>true</code> if this set has been changed, <code>false</code> otherwise.
	 */
	public boolean add(final SortedSet<Variable> varSet) {
		assert (varSet != null): "Supplied variable set is null.";
		boolean result = false;
		for (Variable x: varSet) {
			final Iterator<Variable> tail = varSet.tailSet(x).iterator();
			tail.next(); // good bye, x!
			while (tail.hasNext())
				result |= super.add(new VariablePairImpl(x, tail.next()));
		}
		return result;
	}

	public void addProduct(final SortedSet<Variable> varSet1, final SortedSet<Variable> varSet2) throws DataException {
		assert ((varSet1 != null) && (varSet2 != null)): "Supplied variable set is null.";
		if (varSet1.isEmpty() || varSet2.isEmpty())
			return;
		SortedSet<Variable> lowSet;
		SortedSet<Variable> highSet;
		if (varSet1.first().compareTo(varSet2.first()) < 0) {
			lowSet = varSet1;
			highSet = varSet2;
		} else {
			lowSet = varSet2;
			highSet = varSet1;
		}
		for (Variable x: lowSet) {
			if (highSet.contains(x)) {
				logger.error("Variable " + x + " is not distinct from itself.");
				throw new DataException("Variable not distinct from itself", x.toString());
			}
			for (Variable y: highSet.tailSet(x))
				super.add(new VariablePairImpl(x, y));
		}
	}

	public void restrict(final Collection<Variable> vars) {
		assert (vars != null): "Specified collection is null.";
		final Iterator<VariablePair> i = iterator();
		while (i.hasNext()) {
			final VariablePair p = i.next();
			if (!(vars.contains(p.getFirst()) && vars.contains(p.getSecond())))
				i.remove();
		}
	}

	public @Override boolean addAll(final Collection<? extends VariablePair> c) {
		boolean result = false;
		for (VariablePair p: c)
			result |= add(p);
		return result;
	}

	public @Override boolean contains(final Object o) {
		if (!(o instanceof VariablePair))
			return false;
		assert (o instanceof VariablePairImpl): "Variable pair not from this implementation.";
		VariablePairImpl p = (VariablePairImpl) o;
		return super.contains(p) || super.contains(p.reverse());
	}

	public @Override boolean containsAll(final Collection<?> c) {
		for (Object o: c)
			if (!contains(o))
				return false;
		return true;
	}

	public @Override boolean remove(final Object o) {
		if (!(o instanceof VariablePair))
			return false;
		assert (o instanceof VariablePairImpl): "Variable pair not from this implementation.";
		VariablePairImpl p = (VariablePairImpl) o;
		return super.remove(p) || super.remove(p.reverse());
	}

	public @Override boolean removeAll(final Collection<?> c) {
		boolean result = false;
		for (Object o: c)
			result |= remove(o);
		return result;
	}

}
