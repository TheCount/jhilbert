package jhilbert.commands;

import java.util.Iterator;
import java.util.List;
import jhilbert.commands.InterfaceCommand;
import jhilbert.data.ImportData;
import jhilbert.data.Interface;
import jhilbert.data.InterfaceData;
import jhilbert.data.ModuleData;
import jhilbert.exceptions.SyntaxException;
import jhilbert.util.TokenScanner;

/**
 * Command importing a new {@link jhilbert.data.Interface}
 *
 * @see InterfaceCommand
 */
public final class ImportCommand extends InterfaceCommand {

	/**
	 * Scans a new ImportCommand from a TokenScanner.
	 * The parameters must not be <code>null</code>.
	 *
	 * @param tokenScanner TokenScanner to scan from.
	 * @param data ModuleData.
	 *
	 * @throws SyntaxException if a syntax error occurs.
	 *
	 * @see InterfaceCommand#InterfaceCommand(TokenScanner, ModuleData)
	 */
	public ImportCommand(final TokenScanner tokenScanner, final ModuleData data) throws SyntaxException {
		super("import", tokenScanner, data);
	}

	protected @Override InterfaceData createInterfaceData(final List<Interface> parameters) {
		return new ImportData(prefix, parameters.iterator(), data);
	}

}
