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
import jhilbert.data.Data;
import jhilbert.data.DataFactory;
import jhilbert.data.InterfaceData;
import jhilbert.data.ModuleData;
import jhilbert.data.Parameter;
import jhilbert.exceptions.DataException;
import jhilbert.exceptions.InputException;
import jhilbert.exceptions.SyntaxException;
import jhilbert.exceptions.VerifyException;
import jhilbert.util.TokenScanner;
import org.apache.log4j.Logger;

/**
 * Command exporting a new {@link jhilbert.data.Interface}
 *
 * @see InterfaceCommand
 */
public final class ExportCommand extends InterfaceCommand {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(ExportCommand.class);

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
	public ExportCommand(final TokenScanner tokenScanner, final Data data) throws SyntaxException {
		super("export", tokenScanner, data);
	}

	public @Override void execute() throws VerifyException {
		final DataFactory df = DataFactory.getInstance();
		Parameter parameter = null;
		super.execute();
		try {
			final ModuleData moduleData = (ModuleData) getData();
			parameter = moduleData.getParameter(getName());
			assert (parameter != null): "Newly defined parameter is null.";
			final InterfaceData interfaceData = df.getInterfaceData(parameter.getLocator());
			df.exportInterface(moduleData, interfaceData, parameter);
		} catch (InputException e) {
			logger.error("Error obtaining interface " + parameter.getLocator(), e);
			throw new VerifyException("Error loading interface", parameter.getLocator(), e);
		} catch (DataException e) {
			logger.error("Error exporting interface " + getName());
			throw new VerifyException("Error exporting interface", getName(), e);
		}
	}

}
