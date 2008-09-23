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

package jhilbert.commands.impl;

import jhilbert.commands.Command;
import jhilbert.commands.CommandException;

import jhilbert.data.Module;

/**
 * Basic {@link Command} implementation.
 */
abstract class AbstractCommand implements Command {

	/**
	 * Data module.
	 */
	private final Module module;

	/**
	 * Creates a new <code>AbstractCommand</code> which will use the
	 * specified {@link Module}.
	 *
	 * @param module module for data handling.
	 * @param tokenScanner the token scanner.
	 */
	AbstractCommand(final Module module) {
		assert (module != null): "Supplied module is null";
		this.module = module;
	}

	/**
	 * Obtains the {@link Module} used by this command.
	 *
	 * @return module used by this command.
	 */
	protected Module getModule() {
		return module;
	}

	public abstract void execute() throws CommandException;

}
