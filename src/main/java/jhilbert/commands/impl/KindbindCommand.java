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

    You may contact the author on these Wiki pages:
    http://planetx.cc.vt.edu/AsteroidMeta//GrafZahl (preferred)
    http://en.wikisource.org/wiki/User_talk:GrafZahl
*/

package jhilbert.commands.impl;

import java.util.List;

import jhilbert.commands.CommandException;

import jhilbert.data.DataException;
import jhilbert.data.Kind;
import jhilbert.data.Module;
import jhilbert.data.Namespace;

import jhilbert.scanners.ScannerException;
import jhilbert.scanners.TokenFeed;

/**
 * Command to bind two kinds together.
 */
final class KindbindCommand extends AbstractCommand {

	/**
	 * Creates a new <code>KindbindCommand</code>.
	 *
	 * @param module {@link Module} in which to bind kinds.
	 * @param tokenFeed {@link TokenFeed} to obtain kindbind data from.
	 */
	public KindbindCommand(final Module module, final TokenFeed tokenFeed) {
		super(module, tokenFeed);
	}

	public @Override void execute() throws CommandException {
		final Namespace<? extends Kind> namespace = getModule().getKindNamespace();
		assert (namespace != null): "Module provided null namespace";
		final TokenFeed feed = getFeed();
		try {
			feed.beginExp();
			feed.confirmBeginExp();
			final Kind oldKind = namespace.getObjectByString(feed.getAtom());
			if (oldKind == null) {
				feed.reject("Kind not found");
				throw new CommandException("Kind not found");
			}
			feed.confirmKind();
			final String newKindName = feed.getAtom();
			final Kind newKind = namespace.getObjectByString(newKindName);
			if (newKind == null) {
				namespace.createAlias(oldKind, newKindName);
			} else {
				namespace.identify(oldKind, newKind);
			}
			feed.confirmKind();
			feed.endExp();
			feed.confirmEndCmd();
		} catch (ScannerException e) {
			throw new CommandException("Feed error", e);
		} catch (DataException e) {
			try {
				feed.reject("Error creating alias/identifying; this should not happen");
			} catch (ScannerException ignored) {
				// ignored
			}
			throw new AssertionError("Error creating alias/identifying; this should not happen");
		}
	}

}
