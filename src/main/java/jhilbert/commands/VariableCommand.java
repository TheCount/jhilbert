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
import java.util.List;
import jhilbert.commands.Command;
import jhilbert.data.Data;
import jhilbert.data.Token;
import jhilbert.data.Variable;
import jhilbert.exceptions.DataException;
import jhilbert.exceptions.ScannerException;
import jhilbert.exceptions.SyntaxException;
import jhilbert.exceptions.VerifyException;
import jhilbert.util.TokenScanner;
import org.apache.log4j.Logger;

/**
 * Creates a new command to introduce variables.
 * <p>
 * The format of this command is:
 * <br>
 * kind var1 &hellip; varN
 */
public final class VariableCommand extends Command {

	/**
	 * Logger.
	 */
	private final static Logger logger = Logger.getLogger(VariableCommand.class);;

	/**
	 * Kind of new variables.
	 */
	private final String kind;

	/**
	 * List of new variables.
	 */
	private final List<String> varList;

	/**
	 * Scans a new VariableCommand from a TokenScanner.
	 * The parameters must not be <code>null</code>.
	 *
	 * @param tokenScanner TokenScanner to scan from.
	 * @param data ModuleData.
	 *
	 * @throws SyntaxException if a syntax error occurs.
	 */
	public VariableCommand(final TokenScanner tokenScanner, final Data data) throws SyntaxException {
		super(data);
		assert (tokenScanner != null): "Supplied token scanner is null.";
		StringBuilder context = new StringBuilder("var (");
		try {
			kind = tokenScanner.getAtom();
			context.append(kind).append(' ');
			varList = new ArrayList();
			Token token = tokenScanner.getToken();
			while (token.tokenClass == Token.TokenClass.ATOM) {
				String varName = token.toString();
				context.append(varName).append(' ');
				varList.add(varName);
				token = tokenScanner.getToken();
			}
			final int length = context.length();
			context.delete(length - 1, length);
			if (token.tokenClass != Token.TokenClass.END_EXP) {
				logger.error("Syntax error: expected \")\" in " + context);
				throw new SyntaxException("Expected \")\"", context.toString());
			}
			name = context.substring(5);
			context.append(')');
			tokenScanner.putToken(token);
		} catch (NullPointerException e) {
			logger.error("Unexpected end of input");
			throw new SyntaxException("Unexpected end of input", context.toString(), e);
		} catch (ScannerException e) {
			logger.error("Scanner error in context " + context);
			throw new SyntaxException("Scanner error", context.toString(), e);
		}
	}

	public @Override void execute() throws VerifyException {
		String context = kind;
		try {
			final String definedKind = data.getKind(kind);
			if (definedKind == null) {
				logger.error("Kind " + kind + " not defined");
				logger.debug("Current data: " + data);
				throw new VerifyException("Kind not defined", kind);
			}
			for (String varName: varList) {
				context = varName;
				data.defineSymbol(new Variable(varName, definedKind));
			}
		} catch (DataException e) {
			logger.error("Error defining variable(s)");
			throw new VerifyException("var error", context, e);
		}
	}

}
