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
import jhilbert.data.Definition;
import jhilbert.data.Functor;
import jhilbert.data.Module;
import jhilbert.data.Namespace;
import jhilbert.data.Symbol;
import jhilbert.data.Variable;

import jhilbert.expressions.Expression;
import jhilbert.expressions.ExpressionException;
import jhilbert.expressions.ExpressionFactory;

import jhilbert.scanners.ScannerException;
import jhilbert.scanners.Token;
import jhilbert.scanners.TokenScanner;

import org.apache.log4j.Logger;

/**
 * Command introducing a new {@link Definition}
 */
public final class DefinitionCommand extends AbstractCommand {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(DefinitionCommand.class);

	/**
	 * Name of definition.
	 */
	private final String name;

	/**
	 * List of argument names.
	 */
	private final List<String> varNameList;

	/**
	 * Definiens.
	 */
	private final Expression definiens;

	/**
	 * Creates a new <code>DefinitionCommand</code>.
	 *
	 * @param module {@link Module} to add definition to.
	 * @param tokenScanner {@link TokenScanner} to obtain definition data.
	 *
	 * @throws SyntaxException if a syntax error occurs.
	 */
	public DefinitionCommand(final Module module, final TokenScanner tokenScanner) throws SyntaxException {
		super(module, tokenScanner);
		try {
			tokenScanner.beginExp();
			name = tokenScanner.getAtom();
			varNameList = new ArrayList();
			Token token = tokenScanner.getToken();
			while (token.getTokenClass() == Token.Class.ATOM) {
				varNameList.add(token.getTokenString());
				token = tokenScanner.getToken();
			}
			if (token.getTokenClass() != Token.Class.END_EXP) {
				logger.error("Expected end of LISP s-expression");
				logger.debug("Current scanner context: " + tokenScanner.getContextString());
				throw new SyntaxException("Expected end of expression");
			}
			definiens = ExpressionFactory.getInstance().createExpression(module, tokenScanner);
		} catch (NullPointerException e) {
			logger.error("Unexpected end of input while scanning def command", e);
			throw new SyntaxException("Unexpected end of input", e);
		} catch (ScannerException e) {
			logger.error("Scanner error while scanning def command", e);
			logger.debug("Scanner context: " + e.getScanner().getContextString());
			throw new SyntaxException("Scanner error", e);
		} catch (ExpressionException e) {
			logger.error("Unable to scan expression", e);
			throw new SyntaxException("Unable to scan expression", e);
		}
	}

	public @Override void execute() throws CommandException {
		final Module module = getModule();
		final Namespace<? extends Symbol> symbolNamespace = module.getSymbolNamespace();
		assert (symbolNamespace != null): "Module provided null symbol namespace";
		final Namespace<? extends Functor> functorNamespace = module.getFunctorNamespace();
		assert (functorNamespace != null): "Module provided null functor namespace";
		try {
			final List<Variable> arguments = new ArrayList(varNameList.size());
			for (final String varName: varNameList) {
				final Symbol symbol = symbolNamespace.getObjectByString(varName);
				if (symbol == null) {
					logger.error("Variable " + varName + " not found");
					throw new CommandException("Variable not found");
				}
				if (!symbol.isVariable()) {
					logger.error("Symbol " + varName + " is not a variable");
					throw new CommandException("Symbol is not a variable");
				}
				arguments.add((Variable) symbol);
			}
			DataFactory.getInstance().createDefinition(name, arguments, definiens, functorNamespace);
		} catch (DataException e) {
			logger.error("Unable to define definition " + name, e);
			throw new CommandException("Unable to define definition", e);
		}
	}

}
