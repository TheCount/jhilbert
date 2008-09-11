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
 * Thrown when a correct {@link Command.Class} to execute a command could not
 * be found.
 */
public class CommandNotFoundException extends CommandException {

	/**
	 * Invalid command atom.
	 */
	private final String atom;

	/**
	 * Creates a new <code>CommandNotFoundException</code> with the
	 * specified invalid command atom and detail message.
	 *
	 * @param atom invalid command atom.
	 * @param message detail message.
	 */
	public CommandNotFoundException(final String atom, final String message) {
		this(atom, message, null);
	}

	/**
	 * Creates a new <code>CommandNotFoundException</code> with the
	 * specified invalid command atom, detail message and cause.
	 *
	 * @param atom invalid command atom.
	 * @param message detail message.
	 * @param cause the cause.
	 */
	public CommandNotFoundException(final String atom, final String message, final Throwable cause) {
		super(message, cause);
		assert (atom != null): "Supplied atom is null";
		this.atom = atom;
	}

	/**
	 * Obtains the invalid atom which caused this exception.
	 *
	 * @return invalid atom.
	 */
	public String getAtom() {
		return atom;
	}

}
