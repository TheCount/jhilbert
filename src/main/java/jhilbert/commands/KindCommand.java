package jhilbert.commands;

import jhilbert.commands.Command;
import jhilbert.data.InterfaceData;
import jhilbert.exceptions.DataException;
import jhilbert.exceptions.ScannerException;
import jhilbert.exceptions.SyntaxException;
import jhilbert.exceptions.VerifyException;
import jhilbert.util.TokenScanner;

/**
 * Command introducing a new kind.
 * <p>
 * The format of this command is:
 * <br>
 * name
 */
public final class KindCommand extends Command {

	/**
	 * Scans a new KindCommand from a TokenScanner.
	 * The parameters must not be <code>null</code>.
	 *
	 * @param tokenScanner TokenScanner to scan from.
	 * @param data InterfaceData.
	 *
	 * @throws SyntaxException if a syntax error occurs.
	 */
	public KindCommand(final TokenScanner tokenScanner, final InterfaceData data) throws SyntaxException {
		super(data);
		assert (tokenScanner != null): "Supplied token scanner is null.";
		StringBuilder context = new StringBuilder("kind ");
		try {
			name = tokenScanner.getAtom();
			context.append(name);
		} catch (ScannerException e) {
			throw new SyntaxException("Scanner error", context.toString(), e);
		}
	}

	public @Override void execute() throws VerifyException {
		try {
			((InterfaceData) data).defineKind(name);
		} catch (DataException e) {
			throw new VerifyException("Kind error", name, e);
		}
	}

}
