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

import java.util.HashMap;
import java.util.Map;

import jhilbert.commands.CommandException;
import jhilbert.commands.SyntaxException;

import jhilbert.data.DataException;
import jhilbert.data.DataFactory;
import jhilbert.data.Module;
import jhilbert.data.Parameter;

import jhilbert.scanners.TokenScanner;

import org.apache.log4j.Logger;

/**
 * Command exporting a new {@link Parameter}.
 */
public final class ExportCommand extends AbstractCommand {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(ExportCommand.class);

	/**
	 * Parameter.
	 */
	private final Parameter parameter;

	/**
	 * Creates a new <code>ExportCommand</code>.
	 *
	 * @param module {@link Module} to load parameter into.
	 * @param tokenScanner {@link TokenScanner} to obtain parameter data.
	 *
	 * @throws SyntaxException if a syntax error occurs.
	 */
	public ExportCommand(final Module module, final TokenScanner tokenScanner) throws SyntaxException {
		super(module);
		try {
			parameter = DataFactory.getInstance().createParameter(module, tokenScanner);
		} catch (DataException e) {
			logger.error("Unable to scan parameter", e);
			throw new SyntaxException("Unable to scan parameter", e);
		}
	}

	public @Override void execute() throws CommandException {
		try {
			DataFactory.getInstance().createParameterLoader(parameter, getModule()).exportParameter();
		} catch (DataException e) {
			logger.error("Unable to export parameter " + parameter, e);
			throw new CommandException("Unable to export parameter", e);
		}
	}

}
