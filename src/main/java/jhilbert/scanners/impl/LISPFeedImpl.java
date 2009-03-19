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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Stack;

import jhilbert.scanners.LISPFeed;
import jhilbert.scanners.ScannerException;

import jhilbert.utils.ArrayTreeNode;
import jhilbert.utils.TreeNode;

import org.apache.log4j.Logger;

/**
 * {@link LISPFeed} implementation.
 */
final class LISPFeedImpl implements LISPFeed {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(LISPFeedImpl.class);

	/**
	 * Expression stack.
	 */
	private final Stack<TreeNode<String>> expressionStack;

	/**
	 * Parsed expressions.
	 */
	private final Deque<TreeNode<String>> parsedExpressions;

	private static enum State {

		/**
		 * Currently within a comment.
		 */
		COMMENT,

		/**
		 * Looking for start of atom, expression or comment.
		 */
		START,

		/**
		 * Reading an atom.
		 */
		ATOM

	}

	/**
	 * Current parser state.
	 */
	private State state;

	/**
	 * Current ATOM.
	 */
	private final StringBuilder currentAtom;

	/**
	 * Feed context.
	 */
	private final StringBuilder context;

	/**
	 * Creates a new <code>LISPFeedImpl</code>.
	 */
	LISPFeedImpl() {
		expressionStack = new Stack();
		parsedExpressions = new ArrayDeque();
		state = State.START;
		currentAtom = new StringBuilder();
		context = new StringBuilder();
	}

	public void feed(final String input) throws ScannerException {
		assert (input != null): "Supplied input string is null";
		context.append(input);
		final int length = input.length();
		for (int i = 0; i != length; ++i) {
			final char codepoint = input.charAt(i);
			final Char.Class charClass = (new Char(codepoint)).getCharClass();
			// balk at invalid characters
			if (charClass == Char.Class.INVALID) {
				logger.error("Invalid character in input string " + input + " detected");
				throw new ScannerException("Invalid character in input string", this);
			}
			// read atom
			if (state == State.ATOM) {
				if (charClass == Char.Class.ATOM) {
					currentAtom.append(codepoint);
					continue;
				}
				state = State.START;
				if (expressionStack.isEmpty())
					parsedExpressions.push(new ArrayTreeNode<String>(currentAtom.toString()));
				else
					expressionStack.peek().addChild(new ArrayTreeNode<String>(currentAtom.toString()));
				currentAtom.setLength(0);
			}
			// ignore comments
			if (state == State.COMMENT) {
				if (charClass == Char.Class.NEWLINE)
					state = State.START;
				continue;
			}
			// state is now START; act according to character class
			switch (charClass) {
				case SPACE:
				case NEWLINE:
				continue;

				case OPEN_PAREN:
				expressionStack.push(new ArrayTreeNode<String>());
				continue;

				case CLOSE_PAREN:
				if (expressionStack.isEmpty()) {
					logger.error("Unmatched closing parenthesis in " + input);
					logger.debug("Character: " + i);
					throw new ScannerException("Unmatched closing parenthesis", this);
				}
				final TreeNode<String> finishedExpression = expressionStack.pop();
				if (expressionStack.isEmpty())
					parsedExpressions.push(finishedExpression);
				else
					expressionStack.peek().addChild(finishedExpression);
				continue;

				case HASHMARK:
				state = State.COMMENT;
				continue;

				default:
				throw new AssertionError("Character class " + charClass
					+ " encountered. This cannot happen");
			}
		}
	}

	public void finish() throws ScannerException {
		feed("\n"); // in case input is not newline-terminated
		if (!expressionStack.isEmpty()) {
			logger.error("End of input, but there are still expressions pending");
			logger.debug("Current expression stack: " + expressionStack);
			throw new ScannerException("End of input, but there are still expressions pending", this);
		}
	}

	public TreeNode<String> getToken() {
		return parsedExpressions.pollFirst();
	}

	public boolean hasToken() {
		return (!parsedExpressions.isEmpty());
	}

	public void resetContext() {
		context.setLength(0);
	}

	public String getContextString() {
		return context.toString();
	}

	public void putToken(TreeNode<String> token) {
		parsedExpressions.addFirst(token);
	}

}
