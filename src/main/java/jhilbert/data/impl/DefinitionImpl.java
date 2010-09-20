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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jhilbert.data.DataException;
import jhilbert.data.Definition;
import jhilbert.data.Functor;
import jhilbert.data.Kind;
import jhilbert.data.Term;
import jhilbert.data.Variable;

import jhilbert.expressions.Anonymiser;
import jhilbert.expressions.Expression;
import jhilbert.expressions.ExpressionFactory;

import org.apache.log4j.Logger;

/**
 * {@link Definition} implementation.
 */
final class DefinitionImpl extends AbstractFunctor implements Definition, Serializable {

	/**
	 * Serialisation ID.
	 */
	private static final long serialVersionUID = jhilbert.Main.VERSION;

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(DefinitionImpl.class);

	/**
	 * Definition args.
	 */
	private final LinkedHashSet<Variable> arguments;

	/**
	 * List of input kinds.
	 */
	private final List<Kind> inputKindList;

	/**
	 * Definiens.
	 */
	private final Expression definiens;

	/**
	 * Definition depth.
	 */
	private final int definitionDepth;

	/**
	 * Default constructor, for serialisation use only!
	 */
	public DefinitionImpl() {
		super();
		arguments = null;
		inputKindList = null;
		definiens = null;
		definitionDepth = -1;
	}

	/**
	 * Creates a new <code>DefinitionImpl</code> with the specified name,
	 * argument list and definiens.
	 *
	 * @param name name of new definition.
	 * @param argList argument list.
	 * @param definiens definiens of the new definition.
	 *
	 * @throws DataException if <code>argList</code> contains the same
	 * 	entry more than once.
	 */
	DefinitionImpl(final String name, final List<Variable> argList, final Expression definiens) throws DataException {
		this(name, null, -1, argList, definiens);
	}

	/**
	 * Creates a new <code>DefinitionImpl</code> derived from the specified
	 * original name with the specified name, argument list and definiens.
	 *
	 * @param name name of new definition.
	 * @param orig original definition.
	 * @param parameterIndex index of parameter of <code>orig</code>.
	 * @param argList argument list.
	 * @param definiens definiens of the new definition.
	 *
	 * @throws DataException if <code>argList</code> contains the same
	 * 	entry more than once.
	 */
	DefinitionImpl(final String name, final DefinitionImpl orig, final int parameterIndex, final List<Variable> argList, final Expression definiens) throws DataException {
		super(name, orig, parameterIndex);
		assert (argList != null): "Supplied argument list is null";
		assert (definiens != null): "Supplied definiens is null";
		final Set<Variable> namedSet = new HashSet(argList);
		if (namedSet.size() != argList.size()) {
			logger.error("Argument list contains the same entry twice or more");
			logger.debug("Argument list:            " + argList);
			logger.debug("Normalised argument list: " + namedSet);
			throw new DataException("Ambiguous entry in argument list");
		}
		final Anonymiser anonymiser = ExpressionFactory.getInstance().createAnonymiser(namedSet);
		// calculate arguments and input kinds
		// FIXME: arguments should result in an unmodifiable set... but I'm too lazy ti implement unmodifiedLinkedHashSet()
		arguments = new LinkedHashSet();
		final List<Kind> tempList = new ArrayList(argList.size());
		for (final Variable var: argList) {
			tempList.add(var.getKind());
			arguments.add(anonymiser.anonymise(var));
		}
		inputKindList = Collections.unmodifiableList(tempList);
		// calculate definiens
		this.definiens = anonymiser.anonymise(definiens);
		// calculate definition depth
		final Term term = definiens.getValue();
		if (term.isVariable())
			definitionDepth = 1;
		else
			definitionDepth = ((Functor) term).definitionDepth() + 1;
	}

	public LinkedHashSet<Variable> getArguments() {
		return arguments;
	}

	public Expression getDefiniens() {
		return definiens;
	}

	public Expression unfold(final List<Expression> exprList) {
		assert (exprList != null): "Supplied expression list is null";
		assert (exprList.size() == arguments.size()): "Wrong number of arguments while unfolding";
		final Map<Variable, Expression> substMap = new HashMap();
		int i = 0;
		for (final Variable arg: arguments) {
			final Expression expr = exprList.get(i);
			assert (arg.getKind().equals(expr.getKind())): "Kind mismatch while unfolding";
			substMap.put(arg, expr);
			++i;
		}
		return ExpressionFactory.getInstance().createSubstituter(substMap).substitute(definiens);
	}

	public int definitionDepth() {
		return definitionDepth;
	}

	public Kind getKind() {
		return definiens.getValue().getKind();
	}

	public List<Kind> getInputKinds() {
		return inputKindList;
	}

}
