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

package jhilbert.commands.impl;

import java.util.ArrayList;
import java.util.List;

import jhilbert.commands.CommandException;
import jhilbert.data.ConstraintException;
import jhilbert.data.DVConstraints;
import jhilbert.data.DataException;
import jhilbert.data.DataFactory;
import jhilbert.data.Module;
import jhilbert.data.Namespace;
import jhilbert.data.Symbol;
import jhilbert.expressions.Expression;
import jhilbert.expressions.ExpressionException;
import jhilbert.expressions.ExpressionFactory;
import jhilbert.scanners.ScannerException;
import jhilbert.scanners.Token;
import jhilbert.scanners.TokenFeed;

import org.apache.log4j.Logger;

/**
 * Command introducing a new {@link jhilbert.data.Statement}.
 */
final class StatementCommand extends AbstractCommand {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(StatementCommand.class);

	/**
	 * Creates a new <code>StatementCommand</code>.
	 *
	 * @param module {@link Module} to add statement to.
	 * @param tokenFeed {@link TokenFeed} to obtain statement data.
	 */
	public StatementCommand(final Module module, final TokenFeed tokenFeed) {
		super(module, tokenFeed);
	}

	public @Override void execute() throws CommandException {
		final Module module = getModule();
		final Namespace<? extends Symbol> symbolNamespace = module.getSymbolNamespace();
		assert (symbolNamespace != null): "Module provided null namespace";
		final TokenFeed feed = getFeed();
		final DataFactory dataFactory = DataFactory.getInstance();
		final ExpressionFactory expressionFactory = ExpressionFactory.getInstance();
		try {
			feed.beginExp();
			feed.confirmBeginExp();
			final String name = feed.getAtom();
			feed.confirmStatement();
			final DVConstraints dvConstraints = dataFactory.createDVConstraints(symbolNamespace, feed);
			feed.beginExp();
			feed.confirmBeginExp();
			final List<Expression> hypotheses = new ArrayList();
			Token token = feed.getToken();
			while (token.getTokenClass() != Token.Class.END_EXP) {
				feed.putToken(token);
				hypotheses.add(expressionFactory.createExpression(module, feed));
				token = feed.getToken();
			}
			feed.confirmEndExp();
			final Expression consequent = expressionFactory.createExpression(module, feed);
			feed.endExp();
			dataFactory.createStatement(name, dvConstraints, hypotheses, consequent, symbolNamespace);
			feed.confirmEndCmd();
		} catch (ScannerException e) {
			throw new CommandException("Feed error", e);
		} catch (ConstraintException e) {
			throw new CommandException("Error scanning DV constraints", e);
		} catch (ExpressionException e) {
			throw new CommandException("Error scanning expression", e);
		} catch (NullPointerException e) {
			logger.error("Unexpected end of input");
			throw new CommandException("Unexpected end of input", e);
		} catch (DataException e) {
			try {
				feed.reject("Unable to create statement: " + e.getMessage());
			} catch (ScannerException ignored) {
				logger.error("Unable to create statement: " + e.getMessage());
			}
			throw new CommandException("Unable to create statement", e);
		}
	}

}
