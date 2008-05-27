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
import jhilbert.data.Data;
import jhilbert.data.Definition;
import jhilbert.data.TermExpression;
import jhilbert.data.Token;
import jhilbert.data.Variable;
import jhilbert.exceptions.DataException;
import jhilbert.exceptions.ScannerException;
import jhilbert.exceptions.SyntaxException;
import jhilbert.exceptions.VerifyException;
import jhilbert.util.TokenScanner;
import org.apache.log4j.Logger;

/**
 * Command introducing a new {@link jhilbert.data.Definition}.
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
	 * List of variables serving as parameters.
	 */
	final List<String> varNameList;

	/**
	 * The definiens.
	 */
	final TermExpression definiens;

	/**
	 * Scans a new DefinitionCommand from a TokenScanner.
	 * The parameters must not be <code>null</code>.
	 *
	 * @param tokenScanner TokenScanner to scan from.
	 * @param data Data.
	 *
	 * @throws SyntaxException if a syntax error occurs.
	 */
	public DefinitionCommand(final TokenScanner tokenScanner, final Data data) throws SyntaxException {
		super(data);
		assert (tokenScanner != null): "Supplied token scanner is null.";
		final StringBuilder context = new StringBuilder("def ");
		varNameList = new ArrayList();
		try {
			tokenScanner.beginExp();
			name = tokenScanner.getAtom();
			context.append(name);
			context.append('(');
			Token token = tokenScanner.getToken();
			while (token.tokenClass == Token.TokenClass.ATOM) {
				final String tokenString =token.toString();
				context.append(tokenString).append(", ");
				varNameList.add(tokenString);
				token = tokenScanner.getToken();
			}
			final int length = context.length();
			context.delete(length - 2, length);
			context.append("): ");
			if (token.tokenClass != Token.TokenClass.END_EXP) {
				logger.error("Syntax error: expected \")\" in context " + context);
				throw new SyntaxException("Expected \")\"", context.toString());
			}
			definiens = new TermExpression(tokenScanner, data);
		} catch (NullPointerException e) {
			logger.error("Unexpected end of input while scanning definition " + name, e);
			throw new SyntaxException("Unexpected end of input", context.toString(), e);
		} catch (DataException e) {
			logger.error("Error scanning definiens in context " + context, e);
			throw new SyntaxException("Error scanning definiens", context.toString(), e);
		} catch (ScannerException e) {
			logger.error("Scanner error in context " + context, e);
			throw new SyntaxException("Scanner error", context.toString(), e);
		}
	}

	public @Override void execute() throws VerifyException {
		final LinkedHashSet<Variable> varList = new LinkedHashSet();
		for (final String varName: varNameList) {
			final Variable var = data.getVariable(varName);
			if (var == null) {
				logger.error("Variable " + varName + " does not exist.");
				logger.error("Location: def " + name);
				throw new VerifyException("Variable does not exist", varName);
			}
			if (!varList.add(var)) {
				logger.error("Duplicate entry in variable list.");
				logger.error("Offending variable name:" + varName);
				logger.error("Location: def " + name);
				throw new VerifyException("Duplicte entry in variable list", name);
			}
		}
		try {
			data.defineTerm(new Definition(name, varList, definiens));
		} catch (DataException e) {
			logger.error("A term with name " + name + " does already exist.", e);
			throw new VerifyException("Term already exists", name, e);
		}
	}

}
