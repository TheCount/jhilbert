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

import jhilbert.expressions.Expression;

import jhilbert.scanners.TokenFeed;

/**
 * Data factory.
 */
public abstract class DataFactory {

	/**
	 * Instance.
	 */
	private static final DataFactory instance = new jhilbert.data.impl.DataFactory();

	/**
	 * Obtains a <code>DataFactory</code> instance.
	 *
	 * @return a <code>DataFactory</code> instance.
	 */
	public static DataFactory getInstance() {
		return instance;
	}

	/**
	 * Creates a new unversioned {@link Module} with the specified name.
	 *
	 * @param name module name.
	 *
	 * @return new unversioned module.
	 */
	public final Module createModule(String name) {
		try {
			return createModule(name, -1);
		} catch (DataException e) {
			assert false: "Revision -1 invalid: this should not happen";
			throw new IllegalArgumentException("Revision -1 invalid: this should not happen");
		}
	}

	/**
	 * Creates a new {@link Module} with the specified name and revision
	 * number.
	 * The revision number is a non-negative integer. A value of
	 * <code>-1</code> is also permitted, indicating an unversioned module.
	 *
	 * @param name module name.
	 * @param revision module revision number.
	 *
	 * @return new versioned module.
	 *
	 * @throws DataException if the <code>revision</code> is smaller than
	 * 	<code>-1</code>.
	 */
	public abstract Module createModule(String name, long revision) throws DataException;

	/**
	 * Creates a new {@link Kind} with the specified name in the specified
	 * kind {@link Namespace}.
	 *
	 * @param name name of the new kind.
	 * @param namespace namespace in which the new kind should be
	 * 	registered.
	 *
	 * @return the new kind.
	 *
	 * @throws DataException if a kind with the specified name already
	 * 	exists in the specified namespace.
	 */
	public final Kind createKind(String name, Namespace<? extends Kind> namespace) throws DataException {
		return createKind(name, null, -1, namespace);
	}

	/**
	 * Creates a new {@link Kind} derived from the specified old kind in
	 * the specified kind {@link Namespace}. A value of <code>null</code>
	 * is permissible for the old kind, indicating that the new kind is not
	 * derived from any other kind.
	 *
	 * @param name name of the new kind.
	 * @param oldkind old kind the new kind should be derived from.
	 * @param parameterIndex index of parameter of <code>oldkind</code>.
	 * @param namespace namespace in which the new kind should be
	 * 	registered.
	 *
	 * @return the new kind.
	 *
	 * @throws DataException if a kind with the specified name already
	 * 	exists in the specified namespace.
	 */
	public abstract Kind createKind(String name, Kind oldkind, int parameterIndex, Namespace<? extends Kind> namespace)
	throws DataException;

	/**
	 * Creates a new {@link Variable} of the specified {@link Kind} in the
	 * specified symbol {@link Namespace}.
	 *
	 * @param name name of the new variable.
	 * @param kind kind of the new variable.
	 * @param namespace namespace in which the new variable should be
	 * 	registered.
	 *
	 * @return the new variable.
	 *
	 * @throws DataException if a variable with the specified name already
	 * 	exists in the specified namespace.
	 */
	public abstract Variable createVariable(String name, Kind kind, Namespace<? extends Symbol> namespace) throws DataException;

	/**
	 * Creates a new unnamed {@link Variable} of the specified
	 * {@link Kind}.
	 * This is useful for anonymising expressions.
	 *
	 * @param kind kind of the new unnamed variable.
	 *
	 * @return the new unnamed variable.
	 */
	public abstract Variable createUnnamedVariable(Kind kind);

	/**
	 * Creates a new dummy {@link Variable} of the specified {@link Kind}.
	 * This method is similar to {@link #createUnnamedVariable} except
	 * that the new variable's {@link Variable#isDummy} method returns
	 * <code>true</code>.
	 *
	 * @param kind kind of the new unnamed variable.
	 *
	 * @return the new dummy variable.
	 */
	public abstract Variable createDummyVariable(Kind kind);

	/**
	 * Creates a new {@link Functor} with the specified name, kind and
	 * input kinds in the specified namespace.
	 *
	 * @param name name of new functor.
	 * @param kind kind of new functor.
	 * @param inputKinds List of input kinds.
	 * @param namespace namespace in which the new functor will be
	 * 	registered.
	 *
	 * @return the new functor.
	 *
	 * @throws DataException if a functor with the specified name already
	 * 	exists in the specified namespace.
	 */
	public final Functor createFunctor(final String name, final Kind kind, final List<Kind> inputKinds, final Namespace<? extends Functor> namespace)
	throws DataException {
		return createFunctor(name, null, -1, kind, inputKinds, namespace);
	}

	/**
	 * Creates a new {@link Functor} derived from the specified original
	 * functor with the specified name, kind and input kinds in the
	 * specified namespace.
	 *
	 * @param name name of the new functor.
	 * @param orig name this functor should be derived from.
	 * @param parameterIndex index of the parameter of <code>orig</code>.
	 * @param kind kind of new functor.
	 * @param inputKinds List of input kinds.
	 * @param namespace namespace in which the new functor will be
	 * 	registered.
	 *
	 * @return the new functor.
	 *
	 * @throws DataException if a functor with the specified name already
	 * 	exists in the specified namespace.
	 */
	public abstract Functor createFunctor(String name, Functor orig, int parameterIndex, Kind kind, List<Kind> inputKinds, Namespace<? extends Functor> namespace)
	throws DataException;

	/**
	 * Creates a new {@link Definition} with the specified name,
	 * argument list and definiens.
	 *
	 * @param name name of new definition.
	 * @param argList argument list.
	 * @param definiens definiens of the new definition.
	 * @param namespace namespace in which the new definition will be
	 * 	registered.
	 *
	 * @return the new definition.
	 *
	 * @throws DataException if <code>argList</code> contains the same
	 * 	entry more than once.
	 */
	public final Definition createDefinition(final String name, final List<Variable> argList, final Expression definiens,
		final Namespace<? extends Functor> namespace)
	throws DataException {
		return createDefinition(name, null, -1, argList, definiens, namespace);
	}

	/**
	 * Creates a new {@link Definition} derived from the specified original
	 * definition with the specified name, kind and input kinds in the
	 * specified namespace.
	 *
	 * @param name name of new definition.
	 * @param orig name this definition should be derived from.
	 * @param parameterIndex index of the parameter of <code>orig</code>.
	 * @param argList argument list.
	 * @param definiens definiens of the new definition.
	 * @param namespace namespace in which the new definition will be
	 * 	registered.
	 *
	 * @return the new definition.
	 *
	 * @throws DataException if <code>argList</code> contains the same
	 * 	entry more than once.
	 */
	public abstract Definition createDefinition(String name, Definition orig, int parameterIndex, List<Variable> argList, Expression definiens,
		Namespace<? extends Functor> namespace)
	throws DataException;

	/**
	 * Creates new, empty {@link DVConstraints}.
	 *
	 * @return new, empty disjoint variable constraints.
	 */
	public abstract DVConstraints createDVConstraints();

	/**
	 * Scans new {@link DVConstraints} from the specified
	 * {@link TokenFeed} containing variables from the specified symbol
	 * namespace.
	 *
	 * @param namespace namespace to obtain variables from.
	 * @param tokenFeed token feed to scan constraints from.
	 *
	 * @return the new constraints.
	 *
	 * @throws ConstraintException if a scanner error occurs or if a variable
	 * 	could not be found.
	 */
	public abstract DVConstraints createDVConstraints(Namespace<? extends Symbol> namespace, TokenFeed tokenFeed) throws ConstraintException;

	/**
	 * Creates a new {@link Statement} with the specified name, disjoint
	 * variable constraints, hypotheses and consequent in the specified
	 * namespace.
	 *
	 * @param name name of new statement.
	 * @param dv disjoint variable constraints.
	 * @param hypotheses {@link List} of hypotheses.
	 * @param consequent consequent of new statement.
	 * @param namespace namespace in which the new statement will be
	 * 	created.
	 *
	 * @return the new statement.
	 *
	 * @throws DataException if a statement with the specified name has
	 * 	already been registered in the specified namespace.
	 */
	public final Statement createStatement(final String name, final DVConstraints dv, final List<Expression> hypotheses, final Expression consequent,
		final Namespace<? extends Symbol> namespace)
	throws DataException {
		return createStatement(name, null, -1, dv, hypotheses, consequent, namespace);
	}

	/**
	 * Creates a new {@link Statement} derived from the specified
	 * original statement with the specified name, disjoint variable
	 * constraints, hypotheses and consequent.
	 *
	 * @param name name of new statement.
	 * @param orig statement this statement is derived from.
	 * @param parameterIndex index of the parameter of <code>orig</code>.
	 * @param dv disjoint variable constraints.
	 * @param hypotheses {@link List} of hypotheses.
	 * @param consequent consequent of new statement.
	 * @param namespace namespace in which the new statement will be created.
	 *
	 * @return the new statement.
	 *
	 * @throws DataException if a statement with the specified name has
	 * 	already been registered in the specified namespace.
	 */
	public abstract Statement createStatement(String name, Statement orig, int parameterIndex, DVConstraints dv, List<Expression> hypotheses, Expression consequent,
		Namespace<? extends Symbol> namespace)
	throws DataException;

	/**
	 * Creates a new {@link Parameter} with the specified name, locator,
	 * parameter list and prefix.
	 *
	 * @param name name of new parameter.
	 * @param locator module locator of new parameter.
	 * @param parameterList parameter list.
	 * @param prefix namespace prefix.
	 *
	 * @return new parameter.
	 */
	public abstract Parameter createParameter(String name, String locator, List<Parameter> parameterList, String prefix);

	/**
	 * Scans a new {@link Parameter} from the specified token feed using
	 * data from the specified module.
	 *
	 * @param module data module.
	 * @param tokenFeed {@link TokenFeed} to obtain parameter data.
	 *
	 * @return the new parameter.
	 *
	 * @throws DataException if a syntax error occurs.
	 */
	public abstract Parameter createParameter(Module module, TokenFeed tokenFeed) throws DataException;

	/**
	 * Creates a new {@link ParameterLoader} to load the specified
	 * {@link Parameter} into the specified {@link Module}.
	 *
	 * @param parameter parameter to load/import/export.
	 * @param module module to load/import/export parameter into.
	 *
	 * @return the new parameter loader.
	 *
	 * @throws DataException if the parameter module cannot be loaded.
	 */
	public abstract ParameterLoader createParameterLoader(final Parameter parameter, final Module module)
	throws DataException;

}
