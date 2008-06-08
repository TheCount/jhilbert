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

import java.util.ArrayList;
import java.util.List;
import jhilbert.commands.Command;
import jhilbert.data.Data;
import jhilbert.data.Parameter;
import jhilbert.data.Token;
import jhilbert.exceptions.DataException;
import jhilbert.exceptions.ScannerException;
import jhilbert.exceptions.SyntaxException;
import jhilbert.exceptions.VerifyException;
import jhilbert.util.TokenScanner;
import org.apache.log4j.Logger;

/**
 * Command template for loading/declaring interfaces.
 * <p>
 * The format of such commands is always the same:
 * <br>
 * name locator (param1 &hellip; paramN) prefix
 */
public abstract class InterfaceCommand extends Command {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(InterfaceCommand.class);

	/**
	 * Data.
	 */
	private final Data data;

	/**
	 * Parameter name.
	 */
	private String parameterName;

	/**
	 * Locator.
	 */
	private final String locator;

	/**
	 * List of parameter names.
	 */
	private final List<String> paramNameList;

	/**
	 * Prefix.
	 */
	private final String prefix;

	/**
	 * Scans a new InterfaceCommand from a TokenScanner.
	 * The parameters must not be <code>null</code>.
	 *
	 * @param commandName name of command.
	 * @param tokenScanner TokenScanner to scan from.
	 * @param data Data.
	 *
	 * @throws SyntaxException if a syntax error occurs.
	 */
	protected InterfaceCommand(final String commandName, final TokenScanner tokenScanner, final Data data) throws SyntaxException {
		assert (data != null): "Supplied data are null.";
		assert (tokenScanner != null): "Supplied token scanner is null.";
		assert (commandName != null): "Supplied commandName is null.";
		this.data = data;
		parameterName = null;
		try {
			parameterName = tokenScanner.getAtom();
			locator = tokenScanner.getAtom();
			paramNameList = new ArrayList();
			tokenScanner.beginExp();
			Token token = tokenScanner.getToken();
			while (token.tokenClass == Token.TokenClass.ATOM) {
				paramNameList.add(token.toString());
				token = tokenScanner.getToken();
			}
			if (token.tokenClass != Token.TokenClass.END_EXP) {
				logger.error("Expected \")\" after parameter list of parameter " + parameterName);
				throw new SyntaxException("Expected \")\"", tokenScanner.getContextString());
			}
			prefix = tokenScanner.getString();
		} catch (NullPointerException e) {
			logger.error("Unexpected end of input while scanning parameter " + parameterName + " in context "
				+ tokenScanner.getContextString());
			throw new SyntaxException("Unexpected end of input", tokenScanner.getContextString(), e);
		} catch (ScannerException e) {
			logger.error("Scanner error while scanning parameter " + parameterName + " in context "
				 + tokenScanner.getContextString());
			throw new SyntaxException("Scanner error", tokenScanner.getContextString(), e);
		}
	}

	/**
	 * Copy constructor.
	 * Data are copied deeply, everything else is copied shallowly.
	 * The String &quot;&nbsp;(copy)&quot; is appended to the interface name.
	 *
	 * @param interfaceCommand InterfaceCommand to be copied. Must not be <code>null</code>.
	 */
	// FIXME
	//protected InterfaceCommand(final InterfaceCommand interfaceCommand) {
	//	super(interfaceCommand.data.clone());
	//	name = interfaceCommand.name + " (copy)";
	//	locator = interfaceCommand.locator;
	//	paramNameList = interfaceCommand.paramNameList;
	//	prefix = interfaceCommand.prefix;
	//}

	public @Override void execute() throws VerifyException { // subclasses extend this method.
		List<Parameter> parameters = new ArrayList(paramNameList.size());
		for (final String paramName: paramNameList) {
			if (paramName.equals(parameterName)) {
				logger.error("Invalid self-reference in parameter " + parameterName);
				throw new VerifyException("Invalid parameter self-reference", parameterName);
			}
			final Parameter param = data.getParameter(paramName);
			if (param == null) {
				logger.error("Parameter " + paramName + " from parameter list of " + parameterName
					+ " does not exist.");
				throw new VerifyException("Parameter not found", paramName);
			}
			parameters.add(param);
		}
		try {
			data.defineParameter(parameterName, locator, parameters, prefix);
		} catch (DataException e) {
			logger.error("Unable to define parameter " + parameterName);
			throw new VerifyException("Unable to define parameter", parameterName, e);
		}
	}

	/**
	 * Returns the data.
	 *
	 * @return the data.
	 */
	public Data getData() {
		return data;
	}

	/**
	 * Returns the parameter name for this interface.
	 *
	 * @return the parameter name for this interface.
	 */
	public String getName() {
		return parameterName;
	}

}
