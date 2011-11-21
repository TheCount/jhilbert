/*
    JHilbert, a verifier for collaborative theorem proving

    Copyright © 2008, 2009, 2011 The JHilbert Authors
      See the AUTHORS file for the list of JHilbert authors.
      See the commit logs ("git log") for a list of individual contributions.

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

    You may contact the author on this Wiki page:
    http://www.wikiproofs.de/w/index.php?title=User_talk:GrafZahl
*/

package jhilbert.commands.impl;

import jhilbert.commands.Command;
import jhilbert.commands.CommandException;
import jhilbert.data.Module;
import jhilbert.scanners.TokenFeed;

/**
 * Basic {@link Command} implementation.
 */
abstract class AbstractCommand implements Command {

	/**
	 * Data module.
	 */
	private final Module module;

	/**
	 * Token feed.
	 */
	private final TokenFeed tokenFeed;

	/**
	 * Creates a new <code>AbstractCommand</code> which will use the
	 * specified {@link Module} and {@link TokenFeed}.
	 *
	 * @param module module for data handling.
	 * @param tokenFeed the token feed.
	 */
	AbstractCommand(final Module module, final TokenFeed tokenFeed) {
		assert (module != null): "Supplied module is null";
		assert (tokenFeed != null): "Supplied token feed is null";
		this.module = module;
		this.tokenFeed = tokenFeed;
	}

	/**
	 * Obtains the {@link Module} used by this command.
	 *
	 * @return module used by this command.
	 */
	protected final Module getModule() {
		return module;
	}

	/**
	 * Obtains the {@link TokenFeed} used by this command.
	 *
	 * @return token feed used by this command.
	 */
	protected final TokenFeed getFeed() {
		return tokenFeed;
	}

	public abstract void execute() throws CommandException;

}
