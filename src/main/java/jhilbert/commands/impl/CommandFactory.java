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

import java.util.HashMap;
import java.util.Map;

import jhilbert.commands.Command;
import jhilbert.commands.CommandException;
import jhilbert.data.Module;
import jhilbert.scanners.ScannerException;
import jhilbert.scanners.Token;
import jhilbert.scanners.TokenFeed;

/**
 * Command factory implementation.
 */
public final class CommandFactory extends jhilbert.commands.CommandFactory {

	// default constructed
	
	public @Override void processCommands(final Module module, final TokenFeed tokenFeed) throws CommandException {
		assert (module != null): "Supplied data module is null";
		assert (tokenFeed != null): "Supplied token feed is null";
		final Map<String, Command> commandMap = new HashMap();
		// init command map for module and feed
		commandMap.put("def", new DefinitionCommand(module, tokenFeed));
		commandMap.put("kindbind", new KindbindCommand(module, tokenFeed));
		commandMap.put("var", new VariableCommand(module, tokenFeed));
		if (module.isProofModule()) {
			// proof module only commands
			commandMap.put("export", new ExportCommand(module, tokenFeed));
			commandMap.put("import", new ImportCommand(module, tokenFeed));
			commandMap.put("thm", new TheoremCommand(module, tokenFeed));
		} else {
			// interface module only commands
			commandMap.put("kind", new KindCommand(module, tokenFeed));
			commandMap.put("param", new ParameterCommand(module, tokenFeed));
			commandMap.put("stmt", new StatementCommand(module, tokenFeed));
			commandMap.put("term", new TermCommand(module, tokenFeed));
		}
		// process commands
		try {
			for (;;) {
				final Token token = tokenFeed.getToken();
				if (token == null)
					return;
				final String command = token.getTokenString();
				if (!commandMap.containsKey(command)) {
					tokenFeed.reject("Command " + command + " unknown");
					throw new CommandException("Command unknown");
				}
				tokenFeed.confirmKeyword();
				commandMap.get(command).execute();
			}
		} catch (ScannerException e) {
			throw new CommandException(e.getScanner().getContextString() + "Feed failure: " + e.getMessage(), e);
		} catch (CommandException e) {
			final Throwable t = e.getCause();
			String erradd;
			if (t == null) {
				erradd = "(cause unknown)";
			} else {
				erradd = t.getMessage();
			}
			throw new CommandException(tokenFeed.getContextString() + " " + e.getMessage() + ": " + erradd, e);
		}
	}

}
