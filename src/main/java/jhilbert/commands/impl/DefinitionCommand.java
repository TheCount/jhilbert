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

import java.util.ArrayList;
import java.util.List;

import jhilbert.commands.CommandException;

import jhilbert.data.DataException;
import jhilbert.data.DataFactory;
import jhilbert.data.Definition;
import jhilbert.data.DVConstraints;
import jhilbert.data.Functor;
import jhilbert.data.Module;
import jhilbert.data.Namespace;
import jhilbert.data.Symbol;
import jhilbert.data.Variable;

import jhilbert.expressions.Expression;
import jhilbert.expressions.ExpressionException;
import jhilbert.expressions.ExpressionFactory;

import jhilbert.scanners.ScannerException;
import jhilbert.scanners.Token;
import jhilbert.scanners.TokenFeed;

import org.apache.log4j.Logger;

/**
 * Command introducing a new {@link Definition}
 */
final class DefinitionCommand extends AbstractCommand {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(DefinitionCommand.class);

	/**
	 * Creates a new <code>DefinitionCommand</code>.
	 *
	 * @param module {@link Module} to add definition to.
	 * @param tokenFeed {@link TokenFeed} to obtain definition data from.
	 */
	public DefinitionCommand(final Module module, final TokenFeed tokenFeed) {
		super(module, tokenFeed);
	}

	public @Override void execute() throws CommandException {
		final Module module = getModule();
		final Namespace<? extends Symbol> symbolNamespace = module.getSymbolNamespace();
		assert (symbolNamespace != null): "Module provided null symbol namespace";
		final Namespace<? extends Functor> functorNamespace = module.getFunctorNamespace();
		assert (functorNamespace != null): "Module provided null functor namespace";
		final DataFactory dataFactory = DataFactory.getInstance();
		assert (dataFactory != null): "Null data factory";
		final TokenFeed feed = getFeed();
		DVConstraints dvConstraints = null;
		try {
			feed.beginExp();
			feed.confirmBeginExp();
			// FIXME: Optional DV constraints. This is really ugly.
			// FIXME FIXME: Doesn't work with the current feed implementation
			//Token beginExp = feed.getToken();
			//if (beginExp.getTokenClass() != Token.Class.BEGIN_EXP) {
			//	feed.putToken(beginExp);
			//	feed.beginExp(); // throws exception
			//}
			//Token token = feed.getToken();
			//feed.putToken(token);
			//feed.putToken(beginExp);
			//if (token.getTokenClass() != Token.Class.ATOM) {
				// DV constraints follow
			//	dvConstraints = dataFactory.createDVConstraints(symbolNamespace, feed);
			//}
			// End FIXME FIXME
			// End FIXME
			feed.beginExp();
			feed.confirmBeginExp();
			final String name = feed.getAtom();
			feed.confirmDef();
			final List<Variable> arguments = new ArrayList();
			/**/ Token /**/ token = feed.getToken();
			while (token.getTokenClass() == Token.Class.ATOM) {
				final Symbol symbol = symbolNamespace.getObjectByString(token.getTokenString());
				if (symbol == null) {
					feed.reject("Variable not found");
					throw new CommandException("Variable not found");
				}
				arguments.add((Variable) symbol);
				feed.confirmVar();
				token = feed.getToken();
			}
			if (token.getTokenClass() != Token.Class.END_EXP) {
				feed.reject("Expected end of expression");
				throw new CommandException("Expected end of expression");
			}
			feed.confirmEndExp();
			final Expression definiens = ExpressionFactory.getInstance().createExpression(module, feed);
			feed.endExp();
			dataFactory.createDefinition(name, dvConstraints, arguments, definiens, functorNamespace);
			feed.confirmEndCmd();
		} catch (ClassCastException e) {
			try {
				feed.reject("This symbol is not a variable");
			} catch (ScannerException ignored) {
				logger.error("Symbol is not a variable");
			}
			throw new CommandException("Symbol is not a variable", e);
		} catch (NullPointerException e) {
			logger.error("Unexpected end of input while scanning definition");
			throw new CommandException("Unexpected end of input", e);
		} catch (ScannerException e) {
			throw new CommandException("Feed error", e);
		} catch (ExpressionException e) {
			throw new CommandException("Unable to scan expression", e);
		} catch (DataException e) {
			try {
				feed.reject("Unable to create definition: " + e.getMessage());
			} catch (ScannerException ignored) {
				logger.error("Unable to create definition: " + e.getMessage());
			}
			throw new CommandException("Unable to create definiton", e);
		}
	}

}
