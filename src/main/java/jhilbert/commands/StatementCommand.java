package jhilbert.commands;

import java.util.ArrayList;
import java.util.List;
import jhilbert.commands.AbstractStatementCommand;
import jhilbert.data.InterfaceData;
import jhilbert.data.ModuleData;
import jhilbert.data.TermExpression;
import jhilbert.data.Token;
import jhilbert.exceptions.DataException;
import jhilbert.exceptions.ScannerException;
import jhilbert.exceptions.SyntaxException;
import jhilbert.exceptions.VerifyException;
import jhilbert.util.TokenScanner;

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

	protected @Override void scanHypotheses(final TokenScanner tokenScanner, final ModuleData data) throws SyntaxException, ScannerException, DataException {
		StringBuilder context = new StringBuilder("hypotheses: ");
		try {
			Token token = tokenScanner.getToken();
			while (token.tokenClass != Token.TokenClass.END_EXP) {
				tokenScanner.putToken(token);
				TermExpression expr = new TermExpression(tokenScanner, data);
				context.append(expr.toString()).append(' ');
				hypotheses.add(expr);
				token = tokenScanner.getToken();
			}
			tokenScanner.putToken(token);
		} catch (NullPointerException e) {
			throw new SyntaxException("Unexpected end of input", context.toString(), e);
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
		super("statement", tokenScanner, data);
	}

	public @Override void execute() throws VerifyException {
		super.execute();
		InterfaceData data = (InterfaceData) this.data;
		try {
			data.defineStatement(statement);
		} catch (DataException e) {
			throw new VerifyException("Data error while defining statement", statement.getName(), e);
		}
	}

}
