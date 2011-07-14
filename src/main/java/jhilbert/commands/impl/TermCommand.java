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

import java.util.ArrayList;
import java.util.List;

import jhilbert.commands.CommandException;
import jhilbert.data.DataException;
import jhilbert.data.DataFactory;
import jhilbert.data.Functor;
import jhilbert.data.Kind;
import jhilbert.data.Module;
import jhilbert.data.Namespace;
import jhilbert.scanners.ScannerException;
import jhilbert.scanners.Token;
import jhilbert.scanners.TokenFeed;

/**
 * Command to introduce a new {@link jhilbert.data.Functor}.
 */
final class TermCommand extends AbstractCommand {

	/**
	 * Creates a new <code>TermCommand</code>.
	 *
	 * @param module {@link Module} to add functor to.
	 * @param tokenFeed {@link TokenFeed} to obtain functor data from.
	 */
	public TermCommand(final Module module, final TokenFeed tokenFeed) {
		super(module, tokenFeed);
	}

	public @Override void execute() throws CommandException {
		final Module module = getModule();
		final Namespace<? extends Kind> kindNamespace = module.getKindNamespace();
		assert (kindNamespace != null): "Module supplied null kind namespace";
		final Namespace<? extends Functor> functorNamespace = module.getFunctorNamespace();
		assert (functorNamespace != null): "Module supplied null functor namespace";
		final TokenFeed feed = getFeed();
		try {
			feed.beginExp();
			feed.confirmBeginExp();
			final Kind kind = kindNamespace.getObjectByString(feed.getAtom());
			if (kind == null) {
				feed.reject("Kind not found");
				throw new CommandException("Kind not found");
			}
			feed.confirmKind();
			feed.beginExp();
			feed.confirmBeginExp();
			final String name = feed.getAtom();
			feed.confirmTerm();
			final List<Kind> inputKindList = new ArrayList();
			Token token = feed.getToken();
			while (token.getTokenClass() == Token.Class.ATOM) {
				final Kind inputKind = kindNamespace.getObjectByString(token.getTokenString());
				if (inputKind == null) {
					feed.reject("Kind not found");
					throw new CommandException("Kind not found");
				}
				inputKindList.add(inputKind);
				feed.confirmKind();
				token = feed.getToken();
			}
			if (token.getTokenClass() != Token.Class.END_EXP) {
				feed.reject("Expected end of expression");
				throw new CommandException("Expected end of expression");
			}
			DataFactory.getInstance().createFunctor(name, kind, inputKindList, functorNamespace);
			feed.confirmEndExp();
			feed.endExp();
			feed.confirmEndCmd();
		} catch (DataException e) {
			try {
				feed.reject("Unable to create functor: " + e.getMessage());
			} catch (ScannerException ignored) {
				// ignored
			}
			throw new CommandException("Unable to create functor", e);
		} catch (ScannerException e) {
			throw new CommandException("Feed error", e);
		}
	}

}
