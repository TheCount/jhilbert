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

package jhilbert.commands;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import jhilbert.commands.Command;
import jhilbert.commands.SyntaxException;
import jhilbert.commands.VerifyException;
import jhilbert.data.Data;
import jhilbert.data.DataException;
import jhilbert.data.DataFactory;
import jhilbert.data.TermExpression;
import jhilbert.data.Variable;
import jhilbert.util.ScannerException;
import jhilbert.util.Token;
import jhilbert.util.TokenScanner;
import org.apache.log4j.Logger;

/**
 * Command introducing a new Definition.
 * <p>
 * The format of this command is:
 * <br>
 * (name var1 &hellip; varN) {@link jhilbert.data.TermExpression}
 */
public final class DefinitionCommand extends Command {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(DefinitionCommand.class);

	/**
	 * Data.
	 */
	private final Data data;

	/**
	 * Name of the definition.
	 */
	private String defName;

	/**
	 * List of variables serving as parameters.
	 */
	private final List<String> varNameList;

	/**
	 * The definiens.
	 */
	private final TermExpression definiens;

	/**
	 * Scans a new DefinitionCommand from a TokenScanner.
	 *
	 * @param tokenScanner TokenScanner to scan from (must not be <code>null</code>).
	 * @param data Data (must not be <code>null</code>).
	 *
	 * @throws SyntaxException if a syntax error occurs.
	 */
	public DefinitionCommand(final TokenScanner tokenScanner, final Data data) throws SyntaxException {
		assert (tokenScanner != null): "Supplied token scanner is null.";
		assert (data != null): "Supplied data are null.";
		this.data = data;
		defName = null;
		try {
			tokenScanner.beginExp();
			defName = tokenScanner.getAtom();
			varNameList = new ArrayList();
			Token token = tokenScanner.getToken();
			while (token.tokenClass == Token.TokenClass.ATOM) {
				varNameList.add(token.toString());
				token = tokenScanner.getToken();
			}
			if (token.tokenClass != Token.TokenClass.END_EXP) {
				logger.error("Syntax error: expected \")\" in context " + tokenScanner.getContextString());
				throw new SyntaxException("Expected \")\"", tokenScanner.getContextString());
			}
			definiens = DataFactory.getInstance().scanTermExpression(tokenScanner, data);
		} catch (NullPointerException e) {
			logger.error("Unexpected end of input while scanning definition " + defName);
			throw new SyntaxException("Unexpected end of input", tokenScanner.getContextString(), e);
		} catch (DataException e) {
			logger.error("Error scanning definiens in context " + tokenScanner.getContextString());
			throw new SyntaxException("Error scanning definiens", tokenScanner.getContextString(), e);
		} catch (ScannerException e) {
			logger.error("Scanner error in context " + tokenScanner.getContextString());
			throw new SyntaxException("Scanner error", tokenScanner.getContextString(), e);
		}
	}

	public @Override void execute() throws VerifyException {
		final LinkedHashSet<Variable> varList = new LinkedHashSet();
		for (final String varName: varNameList) {
			final Variable var = data.getVariable(varName);
			if (var == null) {
				logger.error("Variable " + varName + " does not exist.");
				logger.error("Location: def " + defName);
				throw new VerifyException("Variable does not exist", varName);
			}
			if (!varList.add(var)) {
				logger.error("Duplicate entry in variable list.");
				logger.error("Offending variable name: " + varName);
				logger.error("Location: def " + defName);
				throw new VerifyException("Duplicte entry in variable list", defName);
			}
		}
		try {
			data.defineTerm(defName, varList, definiens);
		} catch (DataException e) {
			logger.error("A term with name " + defName + " already exists.");
			throw new VerifyException("Term already exists", defName, e);
		}
	}

}
