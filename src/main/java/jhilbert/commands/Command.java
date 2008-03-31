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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import jhilbert.data.ModuleData;
import jhilbert.exceptions.VerifyException;
import jhilbert.util.TokenScanner;

/**
 * Base class for all commands.
 */
public abstract class Command {

	/**
	 * Command classes.
	 */
	public static enum CommandClass {
		DEFINITION,
		EXPORT,
		IMPORT,
		KIND,
		KINDBIND,
		PARAMETER,
		STATEMENT,
		TERM,
		THEOREM,
		VARIABLE
	}

	/**
	 * Map mapping command atoms to {@link CommandClass}.
	 */
	private static final Map<String, CommandClass> commandClassMap;

	/**
	 * Set containing all commands admissible in proof modules.
	 */
	public static final Set<CommandClass> MODULE_COMMANDS;

	/**
	 * Set containing all commands admissible in interfaces.
	 */
	public static final Set<CommandClass> INTERFACE_COMMANDS;

	/**
	 * Initializes the static maps and sets of this class.
	 */
	static {
		// commandClassMap
		commandClassMap = new HashMap();
		commandClassMap.put("def", CommandClass.DEFINITION);
		commandClassMap.put("export", CommandClass.EXPORT);
		commandClassMap.put("import", CommandClass.IMPORT);
		commandClassMap.put("kind", CommandClass.KIND);
		commandClassMap.put("kindbind", CommandClass.KINDBIND);
		commandClassMap.put("param", CommandClass.PARAMETER);
		commandClassMap.put("stmt", CommandClass.STATEMENT);
		commandClassMap.put("term", CommandClass.TERM);
		commandClassMap.put("thm", CommandClass.THEOREM);
		commandClassMap.put("var", CommandClass.VARIABLE);
		// module commands
		MODULE_COMMANDS = EnumSet.of(CommandClass.DEFINITION,
			CommandClass.EXPORT,
			CommandClass.IMPORT,
			CommandClass.KINDBIND,
			CommandClass.THEOREM);
		MODULE_COMMANDS.add(CommandClass.VARIABLE);
		// interface commands
		INTERFACE_COMMANDS = EnumSet.of(CommandClass.KIND,
			CommandClass.KINDBIND,
			CommandClass.PARAMETER,
			CommandClass.STATEMENT,
			CommandClass.TERM);
		INTERFACE_COMMANDS.add(CommandClass.VARIABLE);
	}

	/**
	 * Maps a command string to the specified command class.
	 *
	 * @param commandString the command string (must not be <code>null</code>).
	 *
	 * @return the respective CommandClass, or <code>null</code> if the specified command does not exist.
	 */
	public static CommandClass getCommandClass(final String commandString) {
		assert (commandString != null): "Supplied command string is null.";
		return commandClassMap.get(commandString);
	}

	/**
	 * Name of newly defined object.
	 */
	protected String name;

	/**
	 * Data.
	 */
	protected final ModuleData data;

	/**
	 * Creates a new command.
	 *
	 * @param data ModuleData (must not be <code>null</code>).
	 */
	protected Command(final ModuleData data) {
		assert (data != null): "Supplied data are null.";
		this.data = data;
	}

	/**
	 * Returns the name of the newly defined object.
	 *
	 * @return name of the newly defined object.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Executes this command.
	 *
	 * @throws VerifyException if the command cannot be executed.
	 */
	public abstract void execute() throws VerifyException;

}
