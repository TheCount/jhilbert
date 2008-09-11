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

package jhilbert.scanners.impl;

import java.io.InputStream;

import jhilbert.commands.Command;
import jhilbert.commands.SyntaxException;

import jhilbert.data.Module;

import jhilbert.scanners.CommandScanner;
import jhilbert.scanners.ScannerException;
import jhilbert.scanners.Token;

import org.apache.log4j.Logger;

/**
 * {@link CommandScanner} implementation.
 */
final class CommandScannerImpl extends AbstractScanner<Command> implements CommandScanner {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(CommandScannerImpl.class);

	/**
	 * Token scanner.
	 */
	private final TokenScannerImpl tokenScanner;

	/**
	 * Module.
	 */
	private final Module module;

	/**
	 * Interface?
	 */
	private final boolean scanInterface;

	/**
	 * Creates a new <code>CommandScannerImpl</code> from the specified
	 * input stream for the specified module.
	 *
	 * @param in input stream.
	 * @param module data module.
	 *
	 * @throws ScannerException if the underlying token scanner cannot
	 * 	be set up.
	 */
	CommandScannerImpl(final InputStream in, final Module module) throws ScannerException {
		assert (in != null): "Supplied input stream is null";
		assert (module != null): "Supplied module is null";
		try {
			tokenScanner = new TokenScannerImpl(in);
			this.module = module;
			if ("".equals(module.getName()))
				scanInterface = false;
			else
				scanInterface = true;
		} catch (ScannerException e) {
			logger.error("Unable to set up token scanner", e);
			throw new ScannerException("Unable to set up token scanner", this, e);
		}
	}

	protected @Override Command getNewToken() throws ScannerException {
		tokenScanner.resetContext();
		try {
			final Token token = tokenScanner.getToken();
			if (token == null)
				return null;
			final String commandString = token.getTokenString();
			appendToContext(commandString);
			tokenScanner.beginExp();
			final Command.Class commandClass = Command.Class.get(commandString);
			if (commandClass == null) {
				logger.error("Unknown command: " + commandString);
				throw new ScannerException("Unknown command", this);
			}
			if (!((!scanInterface || commandClass.isInterfacePermissible()) && (scanInterface || commandClass.isProofPermissible()))) {
				logger.error("Command class " + commandClass + " is not admissible in this context");
				logger.debug("Context: " + (scanInterface ? "Interface module" : "Proof module"));
				throw new ScannerException("Command class not admissible in this context", this);
			}
			final Command result = commandClass.createCommand(module, tokenScanner);
			tokenScanner.endExp();
			appendToContext(' ');
			return result;
		} catch (ScannerException e) {
			logger.error("Error in underlying token scanner", e);
			logger.debug("Context of underlying scanner: " + e.getScanner().getContextString());
			throw new ScannerException("Error in underlying token scanner", this, e);
		} catch (SyntaxException e) {
			logger.error("Syntax error in command", e);
			throw new ScannerException("Syntax error in command", this, e);
		}
	}

}
