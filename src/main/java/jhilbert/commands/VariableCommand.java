package jhilbert.commands;

import java.util.ArrayList;
import java.util.List;
import jhilbert.commands.Command;
import jhilbert.data.ModuleData;
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
	public VariableCommand(final TokenScanner tokenScanner, final ModuleData data) throws SyntaxException {
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
			if (token.tokenClass != Token.TokenClass.END_EXP)
				throw new SyntaxException("Expected \")\"", context.toString());
			name = context.substring(5);
			context.append(')');
			tokenScanner.putToken(token);
		} catch (NullPointerException e) {
			throw new SyntaxException("Unexpected end of input", context.toString(), e);
		} catch (ScannerException e) {
			throw new SyntaxException("Scanner error", context.toString(), e);
		}
	}

	public @Override void execute() throws VerifyException {
		String context = kind;
		try {
			if (!data.containsLocalKind(kind)) {
				logger.debug("Current data: " + data);
				throw new VerifyException("Kind not defined", kind);
			}
			final String definedKind = data.getLocalKind(kind);
			for (String varName: varList) {
				context = varName;
				data.defineVariable(new Variable(varName, definedKind));
			}
		} catch (DataException e) {
			throw new VerifyException("var error", context, e);
		}
	}

}
