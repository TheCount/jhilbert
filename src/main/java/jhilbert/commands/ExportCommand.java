package jhilbert.commands;

import java.util.Iterator;
import java.util.List;
import jhilbert.commands.InterfaceCommand;
import jhilbert.commands.ParamCommand;
import jhilbert.data.ExportData;
import jhilbert.data.Interface;
import jhilbert.data.InterfaceData;
import jhilbert.data.ModuleData;
import jhilbert.exceptions.SyntaxException;
import jhilbert.util.TokenScanner;

/**
 * Command exporting a new {@link jhilbert.data.Interface}
 *
 * @see InterfaceCommand
 */
public final class ExportCommand extends InterfaceCommand {

	/**
	 * Scans a new ExportCommand from a TokenScanner.
	 * The parameters must not be <code>null</code>.
	 *
	 * @param tokenScanner TokenScanner to scan from.
	 * @param data ModuleData.
	 *
	 * @throws SyntaxException if a syntax error occurs.
	 *
	 * @see InterfaceCommand#InterfaceCommand(TokenScanner, ModuleData)
	 */
	public ExportCommand(final TokenScanner tokenScanner, final ModuleData data) throws SyntaxException {
		super("export", tokenScanner, data);
	}

	/**
	 * Create a new ExportCommand from a {@link ParamCommand}.
	 * This is used to create a phony export command for parameter checking.
	 * Package access only, used by ParamCommand.
	 *
	 * @param paramCommand ParamCommand to be copied.
	 */
	ExportCommand(final ParamCommand paramCommand) {
		super(paramCommand);
	}

	protected @Override InterfaceData createInterfaceData(final List<Interface> parameters) {
		return new ExportData(prefix, parameters.iterator(), data);
	}

}
