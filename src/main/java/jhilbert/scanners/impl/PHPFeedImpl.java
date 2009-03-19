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

import jhilbert.commands.CommandException;
import jhilbert.commands.CommandFactory;

import jhilbert.data.DataFactory;
import jhilbert.data.Module;

import jhilbert.scanners.PHPFeed;
import jhilbert.scanners.ScannerException;

import jhilbert.util.HTMLEscaper;
import jhilbert.util.TreeNode;

import org.apache.log4j.Logger;

/**
 * {@link PHPFeed} implementation.
 */
final class PHPFeedImpl implements PHPFeed {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(PHPFeedImpl.class);

	/**
	 * Is this a feed for proof modules?
	 */
	private final boolean isProofFeed;

	/**
	 * Data module.
	 */
	private final Module module;

	/**
	 * Command factory.
	 */
	private final CommandFactory commandFactory;

	/**
	 * Underlying LISP feed.
	 */
	private final LISPFeedImpl lispFeed;

	/**
	 * Current command atom.
	 */
	private String commandAtom;

	/**
	 * Current tokens.
	 */
	private final Deque<String> tokenQueue;

	/**
	 * Creates a new <code>PHPFeedImpl</code>.
	 *
	 * @param moduleName name of the module to which data is added.
	 * @param revision revision number of the module. The revision number
	 * 	must be a non-negative integer, or <code>-1</code> for
	 * 	unversioned modules.
	 */
	PHPFeedImpl(final String moduleName, final long revision) {
		assert (moduleName != null): "Supplied module name is null";
		assert (revision >= -1): "Supplied revision number is negative.
		module = DataFactory.getInstance().createModule(moduleName, revision);
		isProofFeed = "".equals(moduleName);
		commandFactory = CommandFactory.getInstance();
		lispFeed = new LISPFeedImpl();
		commandAtom = null;
		tokenQueue = new ArrayDeque();
		tokenQueue.addFirst("<code>");
	}

	public void feed(final String input) throws ScannerException {
		lispFeed.feed(input);
		replenishQueue();
	}

	public void finish() throws ScannerException {
		lispFeed.finish();
		replenishQueue();
		if (commandAtom != null) {
			logger.error("Lone command atom " + commandAtom);
			throw new ScannerException("Lone command atom");
		}
		tokenQueue.addLast("</code>");
	}

	public String getToken() {
		return tokenQueue.pollFirst();
	}

	public boolean hasToken() {
		return (tokenQueue.size() != 0);
	}

	// FIXME: Scanner methods

	/**
	 * Executes commands as scanned by the LISP feed. Adds return
	 * WikiML to the queue.
	 *
	 * @throws ScannerException if a syntax error occurs.
	 */
	private void replenishQueue() throws ScannerException {
		while (lispFeed.hasToken()) {
			final TreeNode<String> token = lispFeed.getToken();
			if (commandAtom == null) {
				commandAtom = token.getValue();
				if (commandAtom == null) {
					logger.error("Syntax error: expected command atom, got " + token);
					throw new ScannerException("Syntax error: expected command atom");
				}
			} else {
				final String command = commandAtom + " " + token;
				try {
					commandFactory.createCommand(commandAtom, module, token, isProofFeed).execute();
				} catch (CommandException e) {
					logger.error("Error executing command " + command);
					throw new ScannerException("Error executing command", e);
				}
				commandAtom = null;
				tokenQueue.addLast(HTMLEscaper.escape(command));
				tokenQueue.addLast("<br />\n");
			}
		}
	}

}
