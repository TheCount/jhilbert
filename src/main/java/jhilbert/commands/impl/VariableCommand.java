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

import java.util.ArrayList;
import java.util.List;

import jhilbert.commands.CommandException;

import jhilbert.data.DataException;
import jhilbert.data.DataFactory;
import jhilbert.data.Kind;
import jhilbert.data.Module;
import jhilbert.data.Namespace;
import jhilbert.data.Symbol;

import jhilbert.scanners.ScannerException;
import jhilbert.scanners.Token;
import jhilbert.scanners.TokenFeed;

import org.apache.log4j.Logger;

/**
 * Command to introduce an new {@link jhilbert.data.Variable}.
 */
final class VariableCommand extends AbstractCommand {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(VariableCommand.class);

	/**
	 * Creates a new <code>VariableCommand</code>.
	 *
	 * @param module {@link Module} to add variable to.
	 * @param tokenFeed {@link TokenFeed} to obtain variable data from.
	 */
	public VariableCommand(final Module module, final TokenFeed tokenFeed) {
		super(module, tokenFeed);
	}

	public @Override void execute() throws CommandException {
		final Module module = getModule();
		final Namespace<? extends Kind> kindNamespace = module.getKindNamespace();
		final Namespace<? extends Symbol> symbolNamespace = module.getSymbolNamespace();
		assert (kindNamespace != null): "Module provided null namespace";
		assert (symbolNamespace != null): "Module provided null namespace";
		final TokenFeed feed = getFeed();
		final DataFactory dataFactory = DataFactory.getInstance();
		try {
			feed.beginExp();
			feed.confirmBeginExp();
			final Kind kind = kindNamespace.getObjectByString(feed.getAtom());
			feed.confirmKind();
			Token token = feed.getToken();
			while (token.getTokenClass() == Token.Class.ATOM) {
				dataFactory.createVariable(token.getTokenString(), kind, symbolNamespace);
				feed.confirmVar();
				token = feed.getToken();
			}
			if (token.getTokenClass() != Token.Class.END_EXP) {
				feed.reject("Expected end of expression");
				throw new CommandException("Expected end of expression");
			}
			feed.confirmEndCmd();
		} catch (NullPointerException e) {
			logger.error("Unexpected end of input while scanning var command", e);
			throw new CommandException("Unexpected end of input", e);
		} catch (DataException e) {
			try {
				feed.reject("Data error: " + e.getMessage());
			} catch (ScannerException ignored) {
				// ignore
			}
			throw new CommandException("Data error", e);
		} catch (ScannerException e) {
			throw new CommandException("Feed error", e);
		}
	}

}
