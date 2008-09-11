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

import jhilbert.data.Module;

import jhilbert.scanners.TokenScanner;

import org.apache.log4j.Logger;

/**
 * {@link Command} factory.
 */
public abstract class CommandFactory {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(CommandFactory.class);

	/**
	 * Instance.
	 */
	private final static CommandFactory instance = new jhilbert.commands.impl.CommandFactory();

	/**
	 * Obtains an instance of a command factory.
	 *
	 * @return command factory.
	 */
	public static CommandFactory getInstance() {
		return instance;
	}

	/**
	 * Creates a new command.
	 * The correct {@link Command.Class} is chosen from the specified atom.
	 * Then a command from this class is created using the specified
	 * {@link Module} and {@link TokenScanner}.
	 * <p>
	 * <strong>Warning:</strong> It is imperative that the implementation
	 * correctly initialises the command class's command constructors.
	 *
	 * @param atom command atom,
	 * @param module data module the command should work with,
	 * @param tokenScanner token scanner.
	 * @param proofCommand whether the command should be permissible in
	 * 	proof modules. <code>true</code> if it should be permissible
	 * 	in proof modules, <code>false</code> if it should be
	 * 	permissible in interface modules.
	 *
	 * @return the newly created command.
	 *
	 * @throws SyntaxException if a syntax error occurs.
	 * @throws CommandNotFoundException if <code>atom</code> does not refer
	 * 	to a valid command class.
	 * @throws InvalidCommandException if <code>atom</code> <em>does</em>
	 * 	refer to a valid command class, but is not permissible for the
	 * 	requested interface type.
	 */
	public final Command createCommand(final String atom, final Module module, final TokenScanner tokenScanner,
		final boolean proofCommand)
	throws SyntaxException, CommandNotFoundException, InvalidCommandException {
		assert (atom != null): "Supplied atom is null";
		assert (module != null): "Supplied module is null";
		assert (tokenScanner != null): "Supplied token scanner is null";
		final Command.Class commandClass = Command.Class.get(atom);
		if (commandClass == null) {
			logger.error("No such command class: " + atom);
			throw new CommandNotFoundException(atom, "No such command class");
		}
		if ((proofCommand && !commandClass.isProofPermissible()) ||
				!(proofCommand || commandClass.isInterfacePermissible())) {
			logger.error("Command class " + commandClass + " may not be used in "
				+ (proofCommand ? "proof modules" : "interface modules"));
			throw new InvalidCommandException(commandClass, "Command invalid here");
		}
		return commandClass.createCommand(module, tokenScanner);
	}

}
