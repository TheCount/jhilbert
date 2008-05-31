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
import jhilbert.commands.AbstractStatementCommand;
import jhilbert.data.Data;
import jhilbert.data.DataFactory;
import jhilbert.data.InterfaceData;
import jhilbert.data.TermExpression;
import jhilbert.data.Token;
import jhilbert.exceptions.DataException;
import jhilbert.exceptions.ScannerException;
import jhilbert.exceptions.SyntaxException;
import jhilbert.exceptions.VerifyException;
import jhilbert.util.TokenScanner;
import org.apache.log4j.Logger;

/**
 * Command introducing a new statement.
 * <p>
 * The hypotheses for a statement have the following form:
 * <br>
 * {@link TermExpression}1 &hellip; TermExpressionN
 *
 * @see AbstractStatementCommand
 */
public final class StatementCommand extends AbstractStatementCommand {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(StatementCommand.class);

	protected @Override void scanHypotheses(final TokenScanner tokenScanner, final Data data) throws SyntaxException, ScannerException, DataException {
		final DataFactory df = DataFactory.getInstance();
		try {
			Token token = tokenScanner.getToken();
			while (token.tokenClass != Token.TokenClass.END_EXP) {
				tokenScanner.putToken(token);
				hypotheses.add(df.scanTermExpression(tokenScanner, data));
				token = tokenScanner.getToken();
			}
			tokenScanner.putToken(token);
		} catch (NullPointerException e) {
			logger.error("Unexpected end of input while scanning hypotheses in statement " + getName());
			throw new SyntaxException("Unexpected end of input", tokenScanner.getContextString(), e);
		}
	}

	/**
	 * Scans a new StatementCommand from a TokenScanner.
	 * The parameters must not be <code>null</code>.
	 *
	 * @param tokenScanner TokenScanner to scan from.
	 * @param data InterfaceData.
	 *
	 * @throws SyntaxException if a syntax error occurs.
	 */
	public StatementCommand(final TokenScanner tokenScanner, final InterfaceData data) throws SyntaxException {
		super(tokenScanner, data);
	}

}
