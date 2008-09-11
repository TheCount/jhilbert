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

/**
 * Thrown when a {@link Command} is used in the wrong context, such as a proof
 * command in an interface file.
 */
public class InvalidCommandException extends CommandException {

	/**
	 * Class of the invalid command.
	 */
	private final Command.Class commandClass;

	/**
	 * Creates a new <code>InvalidCommandException</code> with the
	 * specified class of the invalid command and detail message.
	 *
	 * @param commandClass class of the invalid command.
	 * @param message detail message.
	 */
	public InvalidCommandException(final Command.Class commandClass, final String message) {
		this(commandClass, message, null);
	}

	/**
	 * Creates a new <code>InvalidCommandException</code> with the
	 * specified class of the invalid command, detail message and cause.
	 *
	 * @param commandClass class of the invalid command.
	 * @param message detail message.
	 * @param cause the cause.
	 */
	public InvalidCommandException(final Command.Class commandClass, final String message, final Throwable cause) {
		super(message, cause);
		assert (commandClass != null): "Supplied command class is null";
		this.commandClass = commandClass;
	}

	/**
	 * Obtains the class of the command which caused this exception.
	 *
	 * @return class of the command which caused this exception.
	 */
	public Command.Class getCommandClass() {
		return commandClass;
	}

}
