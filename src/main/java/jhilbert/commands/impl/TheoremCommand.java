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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jhilbert.commands.CommandException;
import jhilbert.commands.SyntaxException;

import jhilbert.data.DataException;
import jhilbert.data.DataFactory;
import jhilbert.data.DVConstraints;
import jhilbert.data.Module;
import jhilbert.data.Namespace;
import jhilbert.data.Statement;
import jhilbert.data.Symbol;

import jhilbert.expressions.Expression;
import jhilbert.expressions.ExpressionException;
import jhilbert.expressions.ExpressionFactory;

import jhilbert.scanners.ScannerException;
import jhilbert.scanners.Token;
import jhilbert.scanners.TokenScanner;

import jhilbert.verifier.Verifier;
import jhilbert.verifier.VerifierFactory;
import jhilbert.verifier.VerifyException;

import org.apache.log4j.Logger;

/**
 * Command introducing a new {@link Statement} and checking its proof.
 */
public final class TheoremCommand extends AbstractCommand {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(TheoremCommand.class);

	/**
	 * Name of theorem.
	 */
	private final String name;

	/**
	 * DV constraints.
	 */
	private final DVConstraints dvConstraints;

	/**
	 * Hypotheses.
	 */
	private final Map<String, Expression> hypotheses;

	/**
	 * Consequent.
	 */
	private final Expression consequent;

	/**
	 * Proof.
	 */
	private final List<Object> proof;

	/**
	 * Creates a new <code>TheoremCommand</code>.
	 *
	 * @param module {@link Module} to add statement to.
	 * @param tokenScanner {@link TokenScanner} to obtain statement data.
	 *
	 * @throws SyntaxException if a syntax error occurs.
	 */
	public TheoremCommand(final Module module, final TokenScanner tokenScanner) throws SyntaxException {
		super(module, tokenScanner);
		final Namespace<? extends Symbol> symbolNamespace = module.getSymbolNamespace();
		assert (symbolNamespace != null): "Module provided null namespace";
		final ExpressionFactory expressionFactory = ExpressionFactory.getInstance();
		try {
			name = tokenScanner.getAtom();
			// DV constraints
			tokenScanner.beginExp();
			dvConstraints = DataFactory.getInstance().createDVConstraints(symbolNamespace, tokenScanner);
			tokenScanner.endExp();
			// hypotheses
			hypotheses = new LinkedHashMap();
			tokenScanner.beginExp();
			Token token = tokenScanner.getToken();
			while (token.getTokenClass() == Token.Class.BEGIN_EXP) {
				final String label = tokenScanner.getAtom();
				if (hypotheses.containsKey(label)) {
					logger.error("Hypothesis label " + label + " already in use");
					logger.debug("Expression this label was previously used on: " + hypotheses.get(label));
					logger.debug("Current scanner context: " + tokenScanner.getContextString());
					throw new SyntaxException("Hypothesis label already in use");
				}
				hypotheses.put(label, expressionFactory.createExpression(module, tokenScanner));
				tokenScanner.endExp();
				token = tokenScanner.getToken();
			}
			if (token.getTokenClass() != Token.Class.END_EXP) {
				logger.error("Expected end of hypotheses");
				logger.debug("Current scanner context: " + tokenScanner.getContextString());
				throw new SyntaxException("Expected end of hypotheses");
			}
			// consequent
			consequent = expressionFactory.createExpression(module, tokenScanner);
			// proof
			proof = new ArrayList(64);
			tokenScanner.beginExp();
			token = tokenScanner.getToken();
			label:
			while (true) {
				switch (token.getTokenClass()) {
					case ATOM:
					proof.add(token.getTokenString());
					break;

					case BEGIN_EXP:
					tokenScanner.putToken(token);
					proof.add(expressionFactory.createExpression(module, tokenScanner));
					break;

					case END_EXP:
					break label;
				}
				token = tokenScanner.getToken();
			}
		} catch (NullPointerException e) {
			logger.error("Unexpected end of input while scanning thm command", e);
			throw new SyntaxException("Unexpected end of input", e);
		} catch (ScannerException e) {
			logger.error("Scanner error while scanning thm command", e);
			logger.debug("Scanner context: " + e.getScanner().getContextString());
			throw new SyntaxException("Scanner error", e);
		} catch (ExpressionException e) {
			logger.error("Unable to scan expression", e);
			throw new SyntaxException("Unable to scan expression", e);
		} catch (DataException e) {
			logger.error("Unable to scan DV constraints", e);
			throw new SyntaxException("Unable to scan DV constraints", e);
		}
	}

	public @Override void execute() throws CommandException {
		final Module module = getModule();
		final Namespace<? extends Symbol> symbolNamespace = module.getSymbolNamespace();
		assert (symbolNamespace != null): "Module provided null namespace";
		final Verifier verifier = VerifierFactory.getInstance().createVerifier(proof);
		logger.info("Starting verification of theorem " + name);
		try {
			verifier.verify(module, dvConstraints, hypotheses, consequent);
			final List<Expression> hypList = new ArrayList(hypotheses.size());
			for (final Map.Entry<String, Expression> entry: hypotheses.entrySet())
				hypList.add(entry.getValue());
			DataFactory.getInstance().createStatement(name, dvConstraints, hypList, consequent, symbolNamespace);
		} catch (VerifyException e) {
			logger.error("Unable to verify proof", e);
			logger.debug("Proof: " + proof);
			throw new CommandException("Unable to verify proof", e);
		} catch (DataException e) {
			logger.error("Unable to define statement " + name, e);
			throw new CommandException("Unable to define statement", e);
		}
	}

}
