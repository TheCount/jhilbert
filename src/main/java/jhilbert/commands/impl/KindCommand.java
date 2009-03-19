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

import java.util.List;

import jhilbert.commands.CommandException;
import jhilbert.commands.SyntaxException;

import jhilbert.data.DataException;
import jhilbert.data.DataFactory;
import jhilbert.data.Kind;
import jhilbert.data.Module;
import jhilbert.data.Namespace;

import jhilbert.scanners.ScannerException;
import jhilbert.scanners.TokenScanner;

import jhilbert.utils.TreeNode;

import org.apache.log4j.Logger;

/**
 * Command to introduce a new kind.
 *
 * @see jhilbert.commands.Command.Class#KIND
 */
public final class KindCommand extends AbstractCommand {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(KindCommand.class);

	/**
	 * Name of the kind to be introduced.
	 */
	private final String kindName;

	/**
	 * Creates a new <code>KindCommand</code>.
	 *
	 * @param module {@link Module} to add kind to.
	 * @param tokenScanner {@link TokenScanner} to obtain kind data.
	 *
	 * @throws SyntaxException if a syntax error occurs.
	 */
	public KindCommand(final Module module, final TokenScanner tokenScanner) throws SyntaxException {
		super(module);
		try {
			kindName = tokenScanner.getAtom();
		} catch (ScannerException e) {
			logger.error("Error scanning kind", e);
			logger.debug("Scanner context: " + e.getScanner().getContextString());
			throw new SyntaxException("Error scanning kind", e);
		}
	}

	/**
	 * Creates a new <code>KindCommand</code>.
	 *
	 * @param module {@link Module} to add kind to.
	 * @param tree syntax tree to obtain kind data from.
	 *
	 * @throws SyntaxException if a syntax error occurs.
	 */
	public KindCommand(final Module module, final TreeNode<String> tree) throws SyntaxException {
		super(module);
		assert (tree != null): "Supplied syntax tree is null";
		final List<? extends TreeNode<String>> children = tree.getChildren();
		if (children.size() != 1) {
			logger.error("Expected one kind name, got " + children);
			throw new SyntaxException("Too many elements in LISP list");
		}
		kindName = children.get(0).getValue();
		if (kindName == null) {
			logger.error("Expected kind name, got " + children);
			throw new SyntaxException("No kind name found");
		}
	}

	public @Override void execute() throws CommandException {
		final Namespace<? extends Kind> namespace = getModule().getKindNamespace();
		assert (namespace != null): "Module provided null namespace";
		try {
			DataFactory.getInstance().createKind(kindName, namespace);
		} catch (DataException e) {
			logger.error("Unable to create kind " + kindName, e);
			throw new CommandException("Unable to create kind", e);
		}
	}

}
