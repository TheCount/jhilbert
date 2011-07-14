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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jhilbert.commands.CommandException;
import jhilbert.data.ConstraintException;
import jhilbert.data.DVConstraints;
import jhilbert.data.DataException;
import jhilbert.data.DataFactory;
import jhilbert.data.Module;
import jhilbert.data.Namespace;
import jhilbert.data.Statement;
import jhilbert.data.Symbol;
import jhilbert.expressions.Expression;
import jhilbert.expressions.ExpressionException;
import jhilbert.expressions.ExpressionFactory;
import jhilbert.scanners.ScannerException;
import jhilbert.scanners.Token;
import jhilbert.scanners.TokenFeed;
import jhilbert.verifier.Verifier;
import jhilbert.verifier.VerifierFactory;
import jhilbert.verifier.VerifyException;

import org.apache.log4j.Logger;

/**
 * Command introducing a new {@link Statement} and checking its proof.
 */
final class TheoremCommand extends AbstractCommand {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(TheoremCommand.class);

	/**
	 * Creates a new <code>TheoremCommand</code>.
	 *
	 * @param module {@link Module} to add statement to.
	 * @param tokenFeed {@link TokenFeed} to obtain statement data.
	 */
	public TheoremCommand(final Module module, final TokenFeed tokenFeed) {
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
			final Map<String, Expression> hypotheses = new LinkedHashMap();
			Token token = feed.getToken();
			while (token.getTokenClass() == Token.Class.BEGIN_EXP) {
				feed.confirmBeginExp();
				final String label = feed.getAtom();
				if (hypotheses.containsKey(label)) {
					feed.reject("Label already in use");
					throw new CommandException("Label already in use");
				}
				feed.confirmLabel();
				final Expression hypothesis = expressionFactory.createExpression(module, feed);
				feed.endExp();
				hypotheses.put(label, hypothesis);
				feed.confirmEndExp();
				token = feed.getToken();
			}
			if (token.getTokenClass() != Token.Class.END_EXP) {
				feed.reject("Expected end of expression");
				throw new CommandException("Expected end of expression");
			}
			feed.confirmEndExp();
			final Expression consequent = expressionFactory.createExpression(module, feed);
			final Verifier verifier = VerifierFactory.getInstance().createVerifier(module, feed);
			verifier.verify(dvConstraints, hypotheses, consequent);
			final List<Expression> hypList = new ArrayList(hypotheses.size());
			for (final Map.Entry<String, Expression> entry: hypotheses.entrySet())
				hypList.add(entry.getValue());
			feed.endExp();
			dataFactory.createStatement(name, dvConstraints, hypList, consequent, symbolNamespace);
			feed.confirmEndCmd();
		} catch (NullPointerException e) {
			logger.error("Unexpected end of input while scanning theorem");
			throw new CommandException("Unexpected end of input", e);
		} catch (ScannerException e) {
			throw new CommandException("Feed error", e);
		} catch (ConstraintException e) {
			throw new CommandException("Unable to scan DV constraints", e);
		} catch (ExpressionException e) {
			throw new CommandException("Unable to scan expression", e);
		} catch (VerifyException e) {
			throw new CommandException("Proof does not verify", e);
		} catch (DataException e) {
			try {
				feed.reject("Unable to create statement: " + e.getMessage());
			} catch (ScannerException ignored) {
				// ignore
			}
			throw new CommandException("Unable to create statement", e);
		}
	}

}
