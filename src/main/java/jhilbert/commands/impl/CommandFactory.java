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

import jhilbert.data.Module;

import jhilbert.scanners.TokenScanner;

/**
 * Command factory implementation.
 */
public class CommandFactory extends jhilbert.commands.CommandFactory {

	/**
	 * Creates the new command factory.
	 */
	public CommandFactory() {
		try {
			Command.Class.KIND.setConstructor(KindCommand.class.getConstructor(Module.class,
				TokenScanner.class));
			Command.Class.KINDBIND.setConstructor(KindbindCommand.class.getConstructor(Module.class,
				TokenScanner.class));
			Command.Class.VARIABLE.setConstructor(VariableCommand.class.getConstructor(Module.class,
				TokenScanner.class));
			Command.Class.TERM.setConstructor(TermCommand.class.getConstructor(Module.class,
				TokenScanner.class));
			Command.Class.DEFINITION.setConstructor(DefinitionCommand.class.getConstructor(Module.class,
				TokenScanner.class));
			Command.Class.STATEMENT.setConstructor(StatementCommand.class.getConstructor(Module.class,
				TokenScanner.class));
			Command.Class.THEOREM.setConstructor(TheoremCommand.class.getConstructor(Module.class,
				TokenScanner.class));
			Command.Class.PARAMETER.setConstructor(ParameterCommand.class.getConstructor(Module.class,
				TokenScanner.class));
			Command.Class.IMPORT.setConstructor(ImportCommand.class.getConstructor(Module.class,
				TokenScanner.class));
			Command.Class.EXPORT.setConstructor(ExportCommand.class.getConstructor(Module.class,
				TokenScanner.class));
		} catch (NoSuchMethodException e) {
			final Error err = new AssertionError("Constructor missing");
			err.initCause(e);
			throw err;
		}
	}

}
