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
import java.util.SortedSet;
import java.util.TreeSet;
import jhilbert.commands.Command;
import jhilbert.data.Data;
import jhilbert.data.DataFactory;
import jhilbert.data.DVConstraints;
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
 * Command introducing a new theorem or statement.
 * <p>
 * The format of the command is:
 * <br>
 * statementName ((var11 &hellip; var1N) &hellip; (varM1 &hellip; varMN)) (hypotheses) {@link TermExpression} [proof]
 * <p>
 * The format of the hypotheses depends on the command type. A proof is only present in {@link TheoremCommand}s.
 *
 * @see StatementCommand
 * @see TheoremCommand
 */
public abstract class AbstractStatementCommand extends Command {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(AbstractStatementCommand.class);

	/**
	 * Data.
	 */
	final Data data;

	/**
	 * Statement name.
	 */
	private final String statementName;

	/**
	 * Distinct variable constraints (as a list of a list of strings).
	 */
	private final List<List<String>> rawDVList;

	/**
	 * DV constraints.
	 * These are the <em>unanonymized </em> DV constraints of the statement.
	 */
	private final DVConstraints dvConstraints;

	/**
	 * Hypotheses.
	 */
	protected final List<TermExpression> hypotheses;

	/**
	 * Consequent.
	 */
	private final TermExpression consequent;

	/**
	 * Scans the hypotheses from a TokenScanner.
	 * This method must be overridden by subclasses of AbstractStatementCommand.
	 * This method must fill the {@link #hypotheses}.
	 *
	 * @param tokenScanner TokenScanner to scan from.
	 * @param data Data.
	 *
	 * @throws SyntaxException if a syntax error occurs.
	 * @throws ScannerException if a problem scanning the hypotheses occurs.
	 * @throws DataException if the hypotheses fail to be valid {@link TermExpression}s.
	 */
	protected abstract void scanHypotheses(final TokenScanner tokenScanner, final Data data) throws SyntaxException, ScannerException, DataException;

	/**
	 * Scans a new statement from a TokenScanner.
	 * Does not scan proof if present.
	 * The parameters must not be <code>null</code>.
	 *
	 * @param tokenScanner TokenScanner to scan from.
	 * @param data ModuleData.
	 *
	 * @throws SyntaxException if a syntax error occurs.
	 */
	protected AbstractStatementCommand(final TokenScanner tokenScanner, final Data data) throws SyntaxException {
		assert (tokenScanner != null): "Supplied token scanner is null.";
		assert (data != null): "Supplied data are null.";
		this.data = data;
		final DataFactory df = DataFactory.getInstance();
		dvConstraints = df.createDVConstraints();
		try {
			statementName = tokenScanner.getAtom();
			tokenScanner.beginExp();
			rawDVList = new ArrayList();
			Token outer = tokenScanner.getToken();
			while (outer.tokenClass == Token.TokenClass.BEGIN_EXP) {
				List<String> dvList = new ArrayList();
				Token inner = tokenScanner.getToken();
				while (inner.tokenClass == Token.TokenClass.ATOM) {
					dvList.add(inner.toString());
					inner = tokenScanner.getToken();
				}
				if (inner.tokenClass != Token.TokenClass.END_EXP) {
					logger.error("Expected \")\" at end of DV constraint in statement "
						+ statementName);
					throw new SyntaxException("Expected \")\"", tokenScanner.getContextString());
				}
				rawDVList.add(dvList);
				outer = tokenScanner.getToken();
			}
			if (outer.tokenClass != Token.TokenClass.END_EXP) {
				logger.error("Expected \")\" after list of DV constraints in statement " + statementName);
				throw new SyntaxException("Expected \")\"", tokenScanner.getContextString());
			}
			tokenScanner.beginExp();
			hypotheses = new ArrayList();
			scanHypotheses(tokenScanner, data);
			tokenScanner.endExp();
			consequent = df.scanTermExpression(tokenScanner, data);
		} catch (NullPointerException e) {
			logger.error("Unexpected end of input in context " + tokenScanner.getContextString());
			throw new SyntaxException("Unexpected end of input", tokenScanner.getContextString(), e);
		} catch (DataException e) {
			logger.error("Error scanning term in context " + tokenScanner.getContextString());
			throw new SyntaxException("Error scanning term", tokenScanner.getContextString(), e);
		} catch (ScannerException e) {
			logger.error("Scanner error in context " + tokenScanner.getContextString());
			throw new SyntaxException("Scanner error", tokenScanner.getContextString(), e);
		}
	}

	/**
	 * Partially executes this command.
	 * Subclasses of AbstractStatementCommand must override this method.
	 *
	 * @throws VerifyException if the command cannot be executed.
	 */
	public @Override void execute() throws VerifyException {
		final List<SortedSet<Variable>> cookedDVList = new ArrayList();
		for (final List<String> rawDV: rawDVList) {
			final SortedSet<Variable> cookedDV = new TreeSet();
			for (final String varName: rawDV) {
				final Variable var = data.getVariable(varName);
				if (var == null) {
					logger.error("Constraint variable " + varName + " not found in statement "
						+ statementName);
					throw new VerifyException("Constraint variable not defined", varName);
				}
				if (!cookedDV.add(var)) {
					logger.error("Constraint variable " + varName + " occurs twice.");
					logger.debug("Constraint list: " + rawDV);
					throw new VerifyException("Constraint variable occurring twice", varName);
				}
			}
			cookedDVList.add(cookedDV);
		}
		try {
			data.defineStatement(statementName, cookedDVList, hypotheses, consequent);
		} catch (DataException e) {
			logger.error("Error defining statement " + statementName);
			throw new VerifyException("Error defining statement", statementName, e);
		}
		for (final SortedSet<Variable> cookedDV: cookedDVList)
			dvConstraints.add(cookedDV);
	}

	/**
	 * Returns the name of this statement.
	 *
	 * @return name of this statement.
	 */
	protected String getName() {
		return statementName;
	}

	/**
	 * Returns the data of this statement.
	 *
	 * @return data of this statement.
	 */
	protected Data getData() {
		return data;
	}

	/**
	 * Returns the unanonymized DV constraints of this statement.
	 *
	 * @return unanonymized DV constraints of this statement.
	 */
	protected DVConstraints getUnanonymizedDVConstraints() {
		return dvConstraints;
	}

}
