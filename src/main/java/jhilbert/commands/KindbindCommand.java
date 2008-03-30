package jhilbert.commands;

import jhilbert.commands.Command;
import jhilbert.data.ModuleData;
import jhilbert.exceptions.DataException;
import jhilbert.exceptions.ScannerException;
import jhilbert.exceptions.SyntaxException;
import jhilbert.exceptions.VerifyException;
import jhilbert.util.TokenScanner;

/**
 * Command binding one kind to another.
 * <p>
 * The format of the command is:
 * <br>
 * oldKindName name
 */
public final class KindbindCommand extends Command {

	/**
	 * Old kind.
	 */
	private final String oldKind;

	/**
	 * Scans a new KindbindCommand from a TokenScanner.
	 * The parameters must not be <code>null</code>.
	 *
	 * @param tokenScanner TokenScanner to scan from.
	 * @param data ModuleData.
	 * 
	 * @throws SyntaxException if a syntax error occurs.
	 */
	public KindbindCommand(final TokenScanner tokenScanner, final ModuleData data) throws SyntaxException {
		super(data);
		assert (tokenScanner != null): "Supplied token scanner is null.";
		StringBuilder context = new StringBuilder("kindbind ");
		try {
			oldKind = tokenScanner.getAtom();
			context.append(oldKind).append(' ');
			name = tokenScanner.getAtom();
			context.append(name);
		} catch (ScannerException e) {
			throw new SyntaxException("Scanner error", context.toString(), e);
		}
	}

	public @Override void execute() throws VerifyException {
		try {
			data.bindKind(oldKind, name);
		} catch (DataException e) {
			throw new VerifyException("kindbind error", oldKind + "/" + name, e);
		}
	}

}
