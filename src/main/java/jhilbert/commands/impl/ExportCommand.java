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

import jhilbert.commands.CommandException;
import jhilbert.data.DataException;
import jhilbert.data.DataFactory;
import jhilbert.data.Module;
import jhilbert.data.Parameter;
import jhilbert.scanners.ScannerException;
import jhilbert.scanners.TokenFeed;

/**
 * Command exporting a new {@link Parameter}.
 */
final class ExportCommand extends AbstractCommand {

	/**
	 * Creates a new <code>ExportCommand</code>.
	 *
	 * @param module {@link Module} to load parameter into.
	 * @param tokenFeed {@link TokenFeed} to obtain parameter data from.
	 */
	public ExportCommand(final Module module, final TokenFeed tokenFeed) {
		super(module, tokenFeed);
	}

	public @Override void execute() throws CommandException {
		final Module module = getModule();
		final TokenFeed feed = getFeed();
		final DataFactory dataFactory = DataFactory.getInstance();
		try {
			feed.beginExp();
			feed.confirmBeginExp();
			final Parameter parameter = dataFactory.createParameter(module, feed);
			try {
				dataFactory.createParameterLoader(parameter, module).exportParameter();
			} catch (DataException e) {
				throw new CommandException("Unable to export parameter " + parameter, e);
			}
			feed.endExp();
			feed.confirmEndCmd();
		} catch (ScannerException e) {
			throw new CommandException("Feed error", e);
		} catch (DataException e) {
			throw new CommandException("Unable to create new parameter for module " + module, e);
		}
	}

}
