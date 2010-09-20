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

package jhilbert.data.impl;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import jhilbert.data.ConstraintException;
import jhilbert.data.DVConstraints;
import jhilbert.data.Namespace;
import jhilbert.data.Symbol;
import jhilbert.data.Variable;

import jhilbert.scanners.ScannerException;
import jhilbert.scanners.Token;
import jhilbert.scanners.TokenFeed;

import org.apache.log4j.Logger;

/**
 * {@link DVConstraints} implementation.
 */
final class DVConstraintsImpl implements DVConstraints, Serializable {

	/**
	 * Serialisation ID.
	 */
	private static final long serialVersionUID = jhilbert.Main.VERSION;

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(DVConstraintsImpl.class);

	/**
	 * Empty variable array for type selection.
	 */
	private static final Variable[] EMPTY_VAR_ARRAY = new Variable[0];

	/**
	 * Underlying iterator.
	 */
	private static final class DVIterator implements Iterator<Variable[]> {

		/**
		 * Base iterator.
		 */
		private final Iterator<ArrayList<Variable>> baseIterator;

		/**
		 * Creates a new <code>DVIterator</code>.
		 *
		 * @param baseIterator constraint set iterator.
		 */
		DVIterator(final Iterator<ArrayList<Variable>> baseIterator) {
			assert (baseIterator != null): "Supplied base iterator is null";
			this.baseIterator = baseIterator;
		}

		public boolean hasNext() {
			return baseIterator.hasNext();
		}

		public Variable[] next() {
			return baseIterator.next().toArray(EMPTY_VAR_ARRAY);
		}

		public void remove() {
			logger.error("Remove operation not supported, use the restrict() method to restrict constraints");
			throw new UnsupportedOperationException("Removing elements from DV constraints is not supported");
		}

	}

	/**
	 * Underlying constraints set.
	 *
	 * FIXME: We have two choices here:
	 * <ul>
	 * <li>Use HashSet<ArrayList>: fast addition and retrieval, slow DV pair creation, lots of overhead
	 * <li>Use TreeSet<Variable[]> with a suitable comparator: slower addition and retrieval, faster pair creation
	 * </ul>
	 * I'm implementing the first method now, but I'd say the second method warrants some testing...
	 */
	private final Set<ArrayList<Variable>> constraintSet;

	/**
	 * Creates new, empty <code>DVConstraintsImpl</code>.
	 * <p>
	 * This constructor is public as it may be used by serialisation.
	 */
	public DVConstraintsImpl() {
		constraintSet = new HashSet();
	}

	/**
	 * Scans new <code>DVConstraintsImpl</code> from the specified
	 * {@link TokenFeed} containing variables from the specified symbol
	 * namespace.
	 *
	 * @param namespace namespace to obtain variables from.
	 * @param tokenFeed token feed to scan constraints from.
	 *
	 * @throws ConstraintException if a feed error occurs or if a variable
	 * 	could not be found.
	 */
	DVConstraintsImpl(final Namespace<? extends Symbol> namespace, final TokenFeed tokenFeed)
	throws ConstraintException {
		constraintSet = new HashSet();
		assert (namespace != null): "Supplied namespace is null";
		assert (tokenFeed != null): "Supplied token scanner is null";
		try {
			tokenFeed.beginExp();
			tokenFeed.confirmBeginExp();
			Token outer = tokenFeed.getToken();
			while (outer.getTokenClass() == Token.Class.BEGIN_EXP) {
				tokenFeed.confirmBeginExp();
				final List<Variable> varList = new ArrayList();
				Token inner = tokenFeed.getToken();
				while (inner.getTokenClass() == Token.Class.ATOM) {
					final String varName = inner.getTokenString();
					final Symbol symbol = namespace.getObjectByString(varName);
					if (symbol == null) {
						tokenFeed.reject("Variable not found");
						throw new ConstraintException("Variable not found");
					}
					varList.add((Variable) symbol);
					tokenFeed.confirmVar();
					inner = tokenFeed.getToken();
				}
				if (inner.getTokenClass() != Token.Class.END_EXP) {
					tokenFeed.reject("Expected end of expression");
					throw new ConstraintException("Expected end of expression");
				}
				add(varList.toArray(EMPTY_VAR_ARRAY));
				tokenFeed.confirmEndExp();
				outer = tokenFeed.getToken();
			}
			tokenFeed.putToken(outer);
			tokenFeed.endExp();
			tokenFeed.confirmEndExp();
		} catch (ClassCastException e) {
			try {
				tokenFeed.reject("Symbol is not a variable");
			} catch (ScannerException ignored) {
				logger.error("Symbol is not a variable");
			}
			throw new ConstraintException("Symbol is not a variable", e);
		} catch (NullPointerException e) {
			logger.error("Unexpected end of input while scanning DV constraints", e);
			throw new ConstraintException("Unexpected end of input", e);
		} catch (ScannerException e) {
			throw new ConstraintException("Feed error", e);
		}
	}

	public void add(final Variable... vars) throws ConstraintException {
		assert (vars != null): "Supplied variables are null";
		ArrayList<Variable> element;
		for (int i = 0; i != vars.length; ++i) {
			assert (vars[i] != null): "Variable is null";
			for (int j = i + 1; j != vars.length; ++j) {
				if (vars[i].equals(vars[j])) {
					logger.error("Same variable appearing twice in DV list: " + vars[i]);
					throw new ConstraintException("Same variable appearing twice in DV list");
				}
				element = new ArrayList(2);
				element.add(vars[i]);
				element.add(vars[j]);
				constraintSet.add(element);
				element = new ArrayList(2);
				element.add(vars[j]);
				element.add(vars[i]);
				constraintSet.add(element);
			}
		}
	}

	public void addProduct(final Set<Variable> varSet1, final Set<Variable> varSet2) throws ConstraintException {
		assert (varSet1 != null): "First supplied set of variables is null";
		assert (varSet2 != null): "Second supplied set of variables is null";
		ArrayList<Variable> element;
		for (final Variable var1: varSet1) {
			assert (var1 != null): "Variable is null";
			for (final Variable var2: varSet2) {
				assert (var2 != null): "Variable is null";
				if (var1.equals(var2)) {
					logger.error("Intersection of cartesian product factors is not empty");
					logger.debug("Common element: " + var1);
					throw new ConstraintException("Intersection of cartesian product factors is not empty");
				}
				element = new ArrayList(2);
				element.add(var1);
				element.add(var2);
				constraintSet.add(element);
				element = new ArrayList(2);
				element.add(var2);
				element.add(var1);
				constraintSet.add(element);
			}
		}
	}

	public boolean contains(final Variable var1, final Variable var2) {
		final ArrayList<Variable> element = new ArrayList(2);
		element.add(var1);
		element.add(var2);
		return constraintSet.contains(element);
	}

	 public boolean contains(final DVConstraints dv) {
	 	assert (dv instanceof DVConstraintsImpl): "Implementation type error";
		return constraintSet.containsAll(((DVConstraintsImpl) dv).constraintSet);
	 }

	public void restrict(final Set<Variable> varSet) {
		assert (varSet != null): "Supplied set of variables is null";
		for (final Iterator<ArrayList<Variable>> i = constraintSet.iterator(); i.hasNext();) {
			final ArrayList<Variable> element = i.next();
			if (!(varSet.contains(element.get(0)) && varSet.contains(element.get(1))))
				i.remove();
		}
	}

	public Iterator<Variable[]> iterator() {
		return new DVIterator(constraintSet.iterator());
	}

	public @Override String toString() {
		return constraintSet.toString();
	}

}
