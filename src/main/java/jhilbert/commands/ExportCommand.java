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

import java.util.Iterator;
import java.util.List;
import jhilbert.commands.InterfaceCommand;
import jhilbert.commands.ParamCommand;
import jhilbert.data.ExportData;
import jhilbert.data.Interface;
import jhilbert.data.InterfaceData;
import jhilbert.data.ModuleData;
import jhilbert.exceptions.SyntaxException;
import jhilbert.util.TokenScanner;

/**
 * Command exporting a new {@link jhilbert.data.Interface}
 *
 * @see InterfaceCommand
 */
public final class ExportCommand extends InterfaceCommand {

	/**
	 * Scans a new ExportCommand from a TokenScanner.
	 * The parameters must not be <code>null</code>.
	 *
	 * @param tokenScanner TokenScanner to scan from.
	 * @param data ModuleData.
	 *
	 * @throws SyntaxException if a syntax error occurs.
	 *
	 * @see InterfaceCommand#InterfaceCommand(TokenScanner, ModuleData)
	 */
	public ExportCommand(final TokenScanner tokenScanner, final ModuleData data) throws SyntaxException {
		super("export", tokenScanner, data);
	}

	/**
	 * Create a new ExportCommand from a {@link ParamCommand}.
	 * This is used to create a phony export command for parameter checking.
	 * Package access only, used by ParamCommand.
	 *
	 * @param paramCommand ParamCommand to be copied.
	 */
	ExportCommand(final ParamCommand paramCommand) {
		super(paramCommand);
	}

	protected @Override InterfaceData createInterfaceData(final List<Interface> parameters) {
		return new ExportData(prefix, parameters.iterator(), data);
	}

}
