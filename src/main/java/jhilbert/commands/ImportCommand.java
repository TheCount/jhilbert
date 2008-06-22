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
import jhilbert.commands.SyntaxException;
import jhilbert.commands.VerifyException;
import jhilbert.data.Data;
import jhilbert.data.DataException;
import jhilbert.data.DataFactory;
import jhilbert.data.InterfaceData;
import jhilbert.data.ModuleData;
import jhilbert.data.Parameter;
import jhilbert.util.InputException;
import jhilbert.util.TokenScanner;
import org.apache.log4j.Logger;

/**
 * Command importing a new Interface.
 *
 * @see InterfaceCommand
 */
public final class ImportCommand extends InterfaceCommand {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(ImportCommand.class);

	/**
	 * Scans a new ImportCommand from a TokenScanner.
	 * The parameters must not be <code>null</code>.
	 *
	 * @param tokenScanner TokenScanner to scan from.
	 * @param data ModuleData.
	 *
	 * @throws SyntaxException if a syntax error occurs.
	 *
	 * @see InterfaceCommand#InterfaceCommand
	 */
	public ImportCommand(final TokenScanner tokenScanner, final ModuleData data) throws SyntaxException {
		super("import", tokenScanner, data);
	}

	public @Override void execute() throws VerifyException {
		final DataFactory df = DataFactory.getInstance();
		super.execute();
		Parameter parameter = null;
		try {
			final ModuleData moduleData = (ModuleData) getData();
			parameter = moduleData.getParameter(getName());
			assert (parameter != null): "Newly defined parameter is null.";
			final InterfaceData interfaceData = df.getInterfaceData(parameter.getLocator());
			df.importInterface(moduleData, interfaceData, parameter);
		} catch (InputException e) {
			logger.error("Error loading interface " + parameter.getLocator());
			throw new VerifyException("Error loading interface", parameter.getLocator(), e);
		} catch (DataException e) {
			logger.error("Error importing interface " + parameter.toString());
			throw new VerifyException("Error importing interface", parameter.toString(), e);
		}
	}

}
