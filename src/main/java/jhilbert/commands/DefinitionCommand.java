package jhilbert.commands;

import java.util.ArrayList;
import java.util.List;
import jhilbert.commands.Command;
import jhilbert.data.Definition;
import jhilbert.data.ModuleData;
import jhilbert.data.TermExpression;
import jhilbert.data.Token;
import jhilbert.data.Variable;
import jhilbert.exceptions.DataException;
import jhilbert.exceptions.ScannerException;
import jhilbert.exceptions.SyntaxException;
import jhilbert.exceptions.VerifyException;
import jhilbert.util.TokenScanner;

/**
 * Command introducing a new {@link jhilbert.data.Definition}.
 * <p>
 * The format of this command is:
 * <br>
 * (name var1 &hellip; varN) {@link jhilbert.data.TermExpression}
 */
public final class DefinitionCommand extends Command {

	/**
	 * List of variables serving as parameters.
	 */
	final List<String> varNameList;

	/**
	 * The definiens.
	 */
	final TermExpression definiens;

	/**
	 * Scans a new DefinitionCommand from a TokenScanner.
	 * The parameters must not be <code>null</code>.
	 *
	 * @param tokenScanner TokenScanner to scan from.
	 * @param data ModuleData.
	 *
	 * @throws SyntaxException if a syntax error occurs.
	 */
	public DefinitionCommand(final TokenScanner tokenScanner, final ModuleData data) throws SyntaxException {
		super(data);
		assert (tokenScanner != null): "Supplied token scanner is null.";
		final StringBuilder context = new StringBuilder("def ");
		varNameList = new ArrayList();
		try {
			tokenScanner.beginExp();
			name = tokenScanner.getAtom();
			context.append(name);
			context.append('(');
			Token token = tokenScanner.getToken();
			while (token.tokenClass == Token.TokenClass.ATOM) {
				final String tokenString =token.toString();
				context.append(tokenString).append(", ");
				varNameList.add(tokenString);
				token = tokenScanner.getToken();
			}
			final int length = context.length();
			context.delete(length - 2, length);
			context.append("): ");
			if (token.tokenClass != Token.TokenClass.END_EXP)
				throw new SyntaxException("Expected \")\"", context.toString());
			definiens = new TermExpression(tokenScanner, data);
		} catch (NullPointerException e) {
			throw new SyntaxException("Unexpected end of input", context.toString(), e);
		} catch (DataException e) {
			throw new SyntaxException("Error scanning definiens", context.toString(), e);
		} catch (ScannerException e) {
			throw new SyntaxException("Scanner error", context.toString(), e);
		}
	}

	public @Override void execute() throws VerifyException {
		final String context = "def " + name;
		try {
			if (data.containsTerm(name))
				throw new VerifyException("Term already defined", context);
			List<Variable> varList = new ArrayList();
			for (String varName: varNameList) {
				if (!data.containsVariable(varName))
					throw new VerifyException("Variable " + varName + " not defined", context);
				varList.add((Variable) data.getLocalSymbol(varName));
			}
			data.defineTerm(new Definition(name, varList, definiens));
		} catch (DataException e) {
			throw new VerifyException("Data error (this should not happen)", context, e);
		}
	}

}
