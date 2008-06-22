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

package jhilbert.util;

import java.util.Set;
import jhilbert.commands.Command;
import jhilbert.commands.DefinitionCommand;
import jhilbert.commands.ExportCommand;
import jhilbert.commands.ImportCommand;
import jhilbert.commands.KindCommand;
import jhilbert.commands.KindbindCommand;
import jhilbert.commands.ParamCommand;
import jhilbert.commands.StatementCommand;
import jhilbert.commands.SyntaxException;
import jhilbert.commands.TermCommand;
import jhilbert.commands.TheoremCommand;
import jhilbert.commands.VariableCommand;
import jhilbert.data.Data;
import jhilbert.data.InterfaceData;
import jhilbert.data.ModuleData;
import jhilbert.util.InputSource;
import jhilbert.util.Scanner;
import jhilbert.util.ScannerException;
import jhilbert.util.Token;
import jhilbert.util.TokenScanner;

/**
 * Scanner for commands encoded in LISP symbolic expressions.
 */
public final class CommandScanner extends Scanner<Command> {

	/**
	 * Token scanner.
	 */
	private final TokenScanner tokenScanner;

	/**
	 * Data.
	 */
	private final Data data;

	/**
	 * admissible commands
	 */
	private final Set<Command.CommandClass> admissibleCommands;

	/**
	 * Creates a new command scanner.
	 * <p>
	 * If one of the parameters is <code>null</code>, undefined behavior occurs.
	 *
	 * @param inputSource input source for this scanner.
	 * @param data module data, used for parsing.
	 * @param admissibleCommands Set of admissible commands.
	 */
	public CommandScanner(final InputSource inputSource, final Data data, final Set<Command.CommandClass> admissibleCommands) {
		assert (data != null): "Supplied data are null.";
		assert (admissibleCommands != null): "Supplied set of admissible commands is null.";
		this.tokenScanner = new TokenScanner(inputSource);
		this.data = data;
		this.admissibleCommands = admissibleCommands;
	}

	/**
	 * Creates a new Command token.
	 *
	 * @return new Command token.
	 *
	 * @throws ScannerException if a problem scanning the command occurs.
	 */
	protected @Override Command getNewToken() throws ScannerException {
		tokenScanner.resetContext();
		setContext(tokenScanner.getContext());
		String commandString;
		try {
			Token token = tokenScanner.getToken();
			commandString = token.toString();
			if (token.tokenClass != Token.TokenClass.ATOM)
				throw new ScannerException("Expected command", this);
		} catch (NullPointerException e) { // EOF
			return null;
		} catch (ScannerException e) {
			throw new ScannerException("Error scanning command string", this, e);
		}
		Command.CommandClass commandClass = Command.getCommandClass(commandString);
		if (commandClass == null)
			throw new ScannerException("Invalid command", this);
		if (!admissibleCommands.contains(commandClass))
			throw new ScannerException("Command not admissible here", this);
		Command c;
		try {
			tokenScanner.beginExp();
			switch (commandClass) {
				case DEFINITION:
				c = new DefinitionCommand(tokenScanner, data);
				break;

				case EXPORT:
				c = new ExportCommand(tokenScanner, data);
				break;

				case IMPORT:
				c = new ImportCommand(tokenScanner, (ModuleData) data);
				break;

				case KIND:
				c = new KindCommand(tokenScanner, (InterfaceData) data);
				break;

				case KINDBIND:
				c = new KindbindCommand(tokenScanner, data);
				break;

				case PARAMETER:
				c = new ParamCommand(tokenScanner, (InterfaceData) data);
				break;

				case STATEMENT:
				c = new StatementCommand(tokenScanner, (InterfaceData) data);
				break;

				case TERM:
				c = new TermCommand(tokenScanner, (InterfaceData) data);
				break;

				case THEOREM:
				c = new TheoremCommand(tokenScanner, (ModuleData) data);
				break;

				case VARIABLE:
				c = new VariableCommand(tokenScanner, data);
				break;

				default:
				assert false: "This cannot happen.";
				c = null;
			}
			tokenScanner.endExp();
		} catch (SyntaxException e) {
			throw new ScannerException("Error creating command", this, e);
		} catch (ScannerException e) {
			throw new ScannerException("Scanner error", this, e);
		}
		return c;
	}

}
