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

import jhilbert.commands.Command;
import jhilbert.commands.CommandException;

import jhilbert.data.Module;

import jhilbert.scanners.ScannerException;
import jhilbert.scanners.Token;
import jhilbert.scanners.TokenFeed;

import java.util.HashMap;
import java.util.Map;

/**
 * Command factory implementation.
 */
public final class CommandFactory extends jhilbert.commands.CommandFactory {

	/**
	 * Finish command.
	 */
	private static final String FINISH_CMD = "FINI";

	// default constructed
	
	public @Override void processCommands(final Module module, final TokenFeed tokenFeed) throws CommandException {
		assert (module != null): "Supplied data module is null";
		assert (tokenFeed != null): "Supplied token feed is null";
		final Map<String, Command> commandMap = new HashMap();
		// init command map for module and feed
		commandMap.put("def", new DefinitionCommand(module, tokenFeed));
		commandMap.put("kindbind", new KindbindCommand(module, tokenFeed));
		commandMap.put("var", new VariableCommand(module, tokenFeed));
		if ("".equals(module.getName())) {
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
				if (FINISH_CMD.equals(command))
					return;
				if (!commandMap.containsKey(command)) {
					tokenFeed.reject("Command " + command + " unknown");
					throw new CommandException("Command unknown");
				}
				tokenFeed.confirmKeyword();
				commandMap.get(command).execute();
			}
		} catch (ScannerException e) {
			throw new CommandException("Feed error", e);
		}
	}

}
