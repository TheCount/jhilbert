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
import jhilbert.data.Kind;
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
	 * Data.
	 */
	private final Data data;

	/**
	 * Kind of new variables.
	 */
	private final String kindName;

	/**
	 * List of new variable names.
	 */
	private final List<String> varNameList;

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
		assert (tokenScanner != null): "Supplied token scanner is null.";
		assert (data != null): "Supplied data are null.";
		this.data = data;
		try {
			kindName = tokenScanner.getAtom();
			varNameList = new ArrayList();
			Token token = tokenScanner.getToken();
			while (token.tokenClass == Token.TokenClass.ATOM) {
				varNameList.add(token.toString());
				token = tokenScanner.getToken();
			}
			if (token.tokenClass != Token.TokenClass.END_EXP) {
				logger.error("Syntax error: expected \")\" in " + tokenScanner.getContextString());
				throw new SyntaxException("Expected \")\"", tokenScanner.getContextString());
			}
			tokenScanner.putToken(token);
		} catch (NullPointerException e) {
			logger.error("Unexpected end of input while scanning var command");
			logger.error("Context: " + tokenScanner.getContextString());
			throw new SyntaxException("Unexpected end of input", tokenScanner.getContextString(), e);
		} catch (ScannerException e) {
			logger.error("Scanner error in context " + tokenScanner.getContextString());
			throw new SyntaxException("Scanner error", tokenScanner.getContextString(), e);
		}
	}

	public @Override void execute() throws VerifyException {
		try {
			final Kind kind = data.getKind(kindName);
			if (kind == null) {
				logger.error("Kind " + kindName + " not defined");
				logger.debug("Current data: " + data);
				throw new VerifyException("Kind not defined", kindName);
			}
			for (String varName: varNameList)
				data.defineVariable(varName, kind);
		} catch (DataException e) {
			logger.error("Error defining variable(s):" + varNameList);
			throw new VerifyException("var error", varNameList.toString(), e);
		}
	}

}
