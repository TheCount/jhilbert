/*
    JHilbert, a verifier for collaborative theorem proving
    Copyright © 2008, 2009 Alexander Klauer

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

import java.util.List;

import jhilbert.commands.CommandException;

import jhilbert.data.DataException;
import jhilbert.data.DataFactory;
import jhilbert.data.Kind;
import jhilbert.data.Module;
import jhilbert.data.Namespace;

import jhilbert.scanners.ScannerException;
import jhilbert.scanners.TokenFeed;

/**
 * Command to introduce a new kind.
 *
 * @see jhilbert.commands.Command.Class#KIND
 */
final class KindCommand extends AbstractCommand {

	/**
	 * Creates a new <code>KindCommand</code>.
	 *
	 * @param module {@link Module} to add kind to.
	 * @param tokenFeed {@link TokenFeed} to obtain kind data.
	 */
	public KindCommand(final Module module, final TokenFeed tokenFeed) {
		super(module, tokenFeed);
	}

	public @Override void execute() throws CommandException {
		final Namespace<? extends Kind> namespace = getModule().getKindNamespace();
		assert (namespace != null): "Module provided null namespace";
		final TokenFeed feed = getFeed();
		try {
			feed.beginExp();
			feed.confirmBeginExp();
			final String kindName = feed.getAtom();
			DataFactory.getInstance().createKind(kindName, namespace);
			feed.confirmKind();
			feed.endExp();
			feed.confirmEndCmd();
		} catch (DataException e) {
			try {
				feed.reject("Unable to create kind: " + e.getMessage());
			} catch (ScannerException ignored) {
				// ignored
			}
			throw new CommandException("Unable to create kind", e);
		} catch (ScannerException e) {
			throw new CommandException("Feed error", e);
		}
	}

}
