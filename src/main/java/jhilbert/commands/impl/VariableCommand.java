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
import jhilbert.data.Kind;
import jhilbert.data.Module;
import jhilbert.data.Namespace;
import jhilbert.data.Symbol;

import jhilbert.scanners.ScannerException;
import jhilbert.scanners.Token;
import jhilbert.scanners.TokenScanner;

import org.apache.log4j.Logger;

/**
 * Command to introduce an new {@link jhilbert.data.Variable}.
 */
public final class VariableCommand extends AbstractCommand {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(VariableCommand.class);

	/**
	 * Name of kind for the new variables.
	 */
	private final String kindName;

	/**
	 * Names of new variables.
	 */
	private final List<String> variableNameList;

	/**
	 * Creates a new <code>VariableCommand</code>.
	 *
	 * @param module {@link Module} to add kind to.
	 * @param tokenScanner {@link TokenScanner} to obtain variable data.
	 *
	 * @throws SyntaxException if a syntax error occurs.
	 */
	public VariableCommand(final Module module, final TokenScanner tokenScanner) throws SyntaxException {
		super(module, tokenScanner);
		try {
			kindName = tokenScanner.getAtom();
			variableNameList = new ArrayList();
			Token token = tokenScanner.getToken();
			while (token.getTokenClass() == Token.Class.ATOM) {
				variableNameList.add(token.getTokenString());
				token = tokenScanner.getToken();
			}
			if (token.getTokenClass() != Token.Class.END_EXP) {
				logger.error("Expected end of LISP s-expression");
				logger.debug("Current scanner context: " + tokenScanner.getContextString());
				throw new SyntaxException("Expected end of expression");
			}
			tokenScanner.putToken(token);
		} catch (NullPointerException e) {
			logger.error("Unexpected end of input while scanning var command", e);
			logger.debug("Scanner context: " + tokenScanner.getContextString());
			throw new SyntaxException("Unexpected end of input", e);
		} catch (ScannerException e) {
			logger.error("Scanner error while scanning var command", e);
			logger.debug("Current scanner context: " + e.getScanner().getContextString());
			throw new SyntaxException("Scanner error", e);
		}
	}

	public @Override void execute() throws CommandException {
		final Module module = getModule();
		final Namespace<? extends Kind> kindNamespace = module.getKindNamespace();
		final Namespace<? extends Symbol> symbolNamespace = getModule().getSymbolNamespace();
		assert (kindNamespace != null): "Module provided null namespace";
		assert (symbolNamespace != null): "Module provided null namespace";
		try {
			final Kind kind = kindNamespace.getObjectByString(kindName);
			if (kind == null) {
				logger.error("Kind " + kindName + " not defined");
				throw new CommandException("Kind not defined");
			}
			final DataFactory dataFactory = DataFactory.getInstance();
			for (final String varName: variableNameList)
				dataFactory.createVariable(varName, kind, symbolNamespace);
		} catch (DataException e) {
			logger.error("Unable to define variable", e);
			throw new CommandException("Unable to define variable", e);
		}
	}

}
