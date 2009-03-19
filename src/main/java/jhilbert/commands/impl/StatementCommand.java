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

package jhilbert.commands.impl;

import java.util.ArrayList;
import java.util.List;

import jhilbert.commands.CommandException;
import jhilbert.commands.SyntaxException;

import jhilbert.data.DataException;
import jhilbert.data.DataFactory;
import jhilbert.data.DVConstraints;
import jhilbert.data.Module;
import jhilbert.data.Namespace;
import jhilbert.data.Symbol;

import jhilbert.expressions.Expression;
import jhilbert.expressions.ExpressionException;
import jhilbert.expressions.ExpressionFactory;

import jhilbert.scanners.ScannerException;
import jhilbert.scanners.Token;
import jhilbert.scanners.TokenScanner;

import jhilbert.utils.TreeNode;

import org.apache.log4j.Logger;

/**
 * Command introducing a new {@link jhilbert.data.Statement}.
 */
public final class StatementCommand extends AbstractCommand {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(StatementCommand.class);

	/**
	 * Name of statement.
	 */
	private final String name;

	/**
	 * DV constraints.
	 */
	private final DVConstraints dvConstraints;

	/**
	 * Hypotheses.
	 */
	private final List<Expression> hypotheses;

	/**
	 * Consequent.
	 */
	private final Expression consequent;

	/**
	 * Creates a new <code>StatementCommand</code>.
	 *
	 * @param module {@link Module} to add statement to.
	 * @param tokenScanner {@link TokenScanner} to obtain statement data.
	 *
	 * @throws SyntaxException if a syntax error occurs.
	 */
	public StatementCommand(final Module module, final TokenScanner tokenScanner) throws SyntaxException {
		super(module);
		final Namespace<? extends Symbol> symbolNamespace = module.getSymbolNamespace();
		assert (symbolNamespace != null): "Module provided null namespace";
		final ExpressionFactory expressionFactory = ExpressionFactory.getInstance();
		try {
			name = tokenScanner.getAtom();
			// DV constraints
			tokenScanner.beginExp();
			dvConstraints = DataFactory.getInstance().createDVConstraints(symbolNamespace, tokenScanner);
			tokenScanner.endExp();
			// hypotheses
			hypotheses = new ArrayList();
			tokenScanner.beginExp();
			Token token = tokenScanner.getToken();
			while (token.getTokenClass() != Token.Class.END_EXP) {
				tokenScanner.putToken(token);
				hypotheses.add(expressionFactory.createExpression(module, tokenScanner));
				token = tokenScanner.getToken();
			}
			// consequent
			consequent = expressionFactory.createExpression(module, tokenScanner);
		} catch (NullPointerException e) {
			logger.error("Unexpected end of input while scanning stmt command");
			throw new SyntaxException("Unexpected end of input", e);
		} catch (ScannerException e) {
			logger.error("Scanner error while scanning stmt command");
			logger.debug("Scanner context: " + e.getScanner().getContextString());
			throw new SyntaxException("Scanner error", e);
		} catch (ExpressionException e) {
			logger.error("Unable to scan expression");
			throw new SyntaxException("Unable to scan expression", e);
		} catch (DataException e) {
			logger.error("Unable to scan DV constraints");
			throw new SyntaxException("Unable to scan DV constraints", e);
		}
	}

	/**
	 * Creates a new <code>StatementCommand</code>.
	 *
	 * @param module {@link Module} to add statement to.
	 * @param tree syntax tree to obtain statement data from.
	 *
	 * @throws SyntaxException if a syntax error occurs.
	 */
	public StatementCommand(final Module module, final TreeNode<String> tree) throws SyntaxException {
		super(module);
		assert (tree != null): "Specified LISP tree is null";
		final Namespace<? extends Symbol> symbolNamespace = module.getSymbolNamespace();
		final ExpressionFactory expressionFactory = ExpressionFactory.getInstance();
		try {
			final List<? extends TreeNode<String>> children = tree.getChildren();
			if (children.size() != 4)
				throw new IndexOutOfBoundsException();
			// name
			name = children.get(0).getValue();
			if (name == null)
				throw new NullPointerException();
			// DV constraints
			dvConstraints = DataFactory.getInstance().createDVConstraints(symbolNamespace, children.get(1));
			// hypotheses
			final TreeNode<String> hypTree = children.get(2);
			if (hypTree.getValue() != null)
				throw new NullPointerException();
			final List<? extends TreeNode<String>> hypList = hypTree.getChildren();
			hypotheses = new ArrayList(hypList.size());
			for (final TreeNode<String> hyp: hypList)
				hypotheses.add(expressionFactory.createExpression(module, hyp));
			// consequent
			consequent = expressionFactory.createExpression(module, children.get(3));
		} catch (NullPointerException e) {
			logger.error("Syntax error, expected stmt (newstmt ([ dvc ]) ([ hypotheses ]) stmtexpr), got " + tree);
			throw new SyntaxException("Syntax error in statement", e);
		} catch (DataException e) {
			logger.error("Unable to scan DV constraints");
			throw new SyntaxException("Unable to scan DV constraints", e);
		} catch (ExpressionException e) {
			logger.error("Unable to scan expression");
			throw new SyntaxException("Unable to scan expression", e);
		}
	}

	public @Override void execute() throws CommandException {
		final Namespace<? extends Symbol> symbolNamespace = getModule().getSymbolNamespace();
		assert (symbolNamespace != null): "Module provided null namespace";
		try {
			DataFactory.getInstance().createStatement(name, dvConstraints, hypotheses, consequent, symbolNamespace);
		} catch (DataException e) {
			logger.error("Unable to define statement " + name, e);
			throw new CommandException("Unable to define statement", e);
		}
	}

}
