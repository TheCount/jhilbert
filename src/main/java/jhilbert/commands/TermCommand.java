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
import jhilbert.data.Kind;
import jhilbert.data.InterfaceData;
import jhilbert.data.Token;
import jhilbert.exceptions.DataException;
import jhilbert.exceptions.ScannerException;
import jhilbert.exceptions.SyntaxException;
import jhilbert.exceptions.VerifyException;
import jhilbert.util.TokenScanner;
import org.apache.log4j.Logger;

/**
 * Command introducing a new term.
 * <p>
 * The format of this command is:
 * <br>
 * kindName (name kindName1 &hellip; kindNameN)
 */
public final class TermCommand extends Command {

	/**
	 * Logger for this class.
	 */
	private final static Logger logger = Logger.getLogger(TermCommand.class);

	/**
	 * Interface data.
	 */
	private final InterfaceData data;

	/**
	 * Term name.
	 */
	private String termName;

	/**
	 * Result kind name.
	 */
	private final String kindName;

	/**
	 * List of input kind names.
	 */
	private final List<String> inputKindNameList;

	/**
	 * Scans a new TermCommand from a TokenScanner.
	 * The parameters must not be <code>null</code>.
	 *
	 * @param tokenScanner TokenScanner to scan from.
	 * @param data InterfaceData.
	 *
	 * @throws SyntaxException if a syntax error occurs.
	 */
	public TermCommand(final TokenScanner tokenScanner, final InterfaceData data) throws SyntaxException {
		assert (tokenScanner != null): "Supplied token scanner is null.";
		assert (data != null): "Supplied data are null.";
		this.data = data;
		termName = null;
		try {
			kindName = tokenScanner.getAtom();
			tokenScanner.beginExp();
			termName = tokenScanner.getAtom();
			inputKindNameList = new ArrayList();
			Token token = tokenScanner.getToken();
			while (token.tokenClass == Token.TokenClass.ATOM) {
				inputKindNameList.add(token.toString());
				token = tokenScanner.getToken();
			}
			if (token.tokenClass != Token.TokenClass.END_EXP) {
				logger.error("Syntax error: expected \")\" in context " + tokenScanner.getContextString());
				throw new SyntaxException("Expected \")\"", tokenScanner.getContextString());
			}
		} catch (NullPointerException e) {
			logger.error("Unexpected end of input while scanning term " + termName);
			throw new SyntaxException("Unexpected end of input", tokenScanner.getContextString(), e);
		} catch (ScannerException e) {
			logger.error("Scanner error in context " + tokenScanner.getContextString());
			throw new SyntaxException("Scanner error", tokenScanner.getContextString(), e);
		}
	}

	public @Override void execute() throws VerifyException {
		Kind resultKind;
		List<Kind> inputKindList = new ArrayList(inputKindNameList.size());
		try {
			resultKind = data.getKind(kindName);
			if (resultKind == null) {
				logger.error("Result kind does not exist: " + kindName);
				logger.error("Cannot define term " + termName);
				throw new VerifyException("Result kind does not exist", kindName);
			}
			for (final String inputKindName: inputKindNameList) {
				final Kind inputKind = data.getKind(inputKindName);
				if (inputKind == null) {
					logger.error("Input kind does not exist: " + inputKindName);
					logger.error("Cannot define term " + termName);
					throw new VerifyException("Input kind does not exist", inputKindName);
				}
				inputKindList.add(inputKind);
			}
		} catch (DataException e) {
			logger.error("Unable to obtain kind while trying define term " + termName);
			throw new VerifyException("Unable to obtain kind while trying to define term", termName, e);
		}
		try {
			data.defineTerm(termName, resultKind, inputKindList);
		} catch (DataException e) {
			logger.error("A term named " + termName + " already exists.");
			throw new VerifyException("Term already exists", termName, e);
		}
	}

}
