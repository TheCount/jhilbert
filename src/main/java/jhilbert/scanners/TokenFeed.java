/*
    JHilbert, a verifier for collaborative theorem proving

    Copyright © 2008, 2009, 2011 The JHilbert Authors
      See the AUTHORS file for the list of JHilbert authors.
      See the commit logs ("git log") for a list of individual contributions.

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

package jhilbert.scanners;

import jhilbert.data.Functor;

/**
 * Token feed.
 * A token feed provides tokens. Each token must be confirmed or rejected.
 * Such confirmation or rejection may be passed on if the token provider is an
 * external entity.
 * <br />
 * Note that once a token is confirmed, it cannot be put back.
 */
public interface TokenFeed extends Scanner<Token> {
	// FIXME: it's not a good idea to extend Scanner<Token> but I'm lazy...

	/**
	 * Keyword confirmation message.
	 */
	public static final String KEYWORD = "keyword";

	/**
	 * Expression start confirmation message.
	 */
	public static final String BEGIN_EXP = "beginexp";

	/**
	 * Expression end confirmation message.
	 */
	public static final String END_EXP = "endexp";

	/**
	 * Kind confirmation message.
	 */
	public static final String KIND = "kind";

	/**
	 * Variable confirmation message.
	 */
	public static final String VARIABLE = "var";

	/**
	 * Term confirmation message.
	 */
	public static final String TERM = "term";

	/**
	 * Definition confirmation message.
	 */
	public static final String DEFINITION = "def";

	/**
	 * Statement confirmation message.
	 */
	public static final String STATEMENT = "stat";

	/**
	 * Label confirmation message.
	 */
	public static final String LABEL = "label";

	/**
	 * Parameter confirmation message.
	 */
	public static final String PARAMETER = "param";

	/**
	 * Locator confirmation message
	 */
	public static final String LOCATOR = "locator";

	/**
	 * String confirmation message.
	 */
	public static final String STRING = "string";

	/**
	 * Reads a LISP atom.
	 * This convenience method reads a token and returns it if it is an
	 * atom, otherwise the token will be rejected and a
	 * {@link ScannerException} is thrown.
	 *
	 * @return a new {@link Token} whose {@link Token.Class} is
	 * 	{@link Token.Class#ATOM}.
	 *
	 * @throws ScannerException if the next token is not an atom, or if
	 * 	reading the token fails.
	 */
	public String getAtom() throws ScannerException;

	/**
	 * Reads the beginning of a LISP s-expression.
	 * This convenience method reads a token and returns normally it if it
	 * is the beginning of a LISP s-expression (that is, an opening
	 * parenthesis), otherwise the token will be rejected and a
	 * {@link ScannerException} is thrown.
	 *
	 * @throws ScannerException if the next token is not of class
	 * 	{@link Token.Class#BEGIN_EXP}, or if reading the token fails.
	 */
	public void beginExp() throws ScannerException;

	/**
	 * Reads the end of a LISP s-expression.
	 * This convenience method reads a token and returns normally it if it
	 * is the end of a LISP s-expression (that is, a closing
	 * parenthesis), otherwise the token will be rejected and a
	 * {@link ScannerException} is thrown.
	 *
	 * @throws ScannerException if the next token is not of class
	 * 	{@link Token.Class#END_EXP}, or if reading the token fails.
	 */
	public void endExp() throws ScannerException;

	/**
	 * Reads a string.
	 * This convenience method reads a string, that is either an atom or
	 * a {@link Token} of class {@link Token.Class#BEGIN_EXP} immediately
	 * followed by a <code>Token</code> of class
	 * {@link Token.Class#END_EXP}, in which case the string is empty.
	 * In case of an empty string, the opening parenthesis is confirmed.
	 * If the input is not a string, the infringing token is rejected and
	 * a {@link ScannerException} os thrown.
	 *
	 * @return a string as specified.
	 *
	 * @throws ScannerException if a string as specified could not be
	 * 	found, or if reading the token(s) fails.
	 */
	public String getString() throws ScannerException;

	/**
	 * Confirms the last token.
	 * Once a token has been confirmed, it cannot be put back.
	 *
	 * @param msg confirmation message.
	 *
	 * @throws ScannerException if confirmation fails.
	 *
	 * @see #putToken
	 */
	public void confirm(String msg) throws ScannerException;

	/**
	 * Rejects the last token.
	 * Once a token has been rejected, scanning should cease.
	 *
	 * @param msg rejection message.
	 *
	 * @throws ScannerException if rejection fails.
	 */
	public void reject(String msg) throws ScannerException;

	/**
	 * Convenience method to confirm a keyword.
	 *
	 * @throws ScannerException if confirmation fails.
	 */
	public void confirmKeyword() throws ScannerException;

	/**
	 * Convenience method to confirm an expression start.
	 *
	 * @throws ScannerException if confirmation fails.
	 */
	public void confirmBeginExp() throws ScannerException;

	/**
	 * Convenience method to confirm an expression end.
	 *
	 * @throws ScannerException if confirmation fails.
	 */
	public void confirmEndExp() throws ScannerException;

	/**
	 * Confirm end of command.
	 *
	 * @throws ScannerException if confirmation fails.
	 */
	public void confirmEndCmd() throws ScannerException;

	/**
	 * Convenience method to confirm a kind.
	 *
	 * @throws ScannerException if confirmation fails.
	 */
	public void confirmKind() throws ScannerException;

	/**
	 * Convenience method to confirm a variable.
	 *
	 * @throws ScannerException if confirmation fails.
	 */
	public void confirmVar() throws ScannerException;

	/**
	 * Convenience method to confirm a term.
	 *
	 * @throws ScannerException if confirmation fails.
	 */
	public void confirmTerm() throws ScannerException;

	/**
	 * Convenience method to confirm a definition.
	 *
	 * @throws ScannerException if confirmation fails.
	 */
	public void confirmDef() throws ScannerException;

	/**
	 * Convenience method to confirm a {@link Functor}.
	 * Will call {@link #confirmTerm} or {@link #confirmDef} depending on
	 * whether the provided functor is a term or a definition.
	 *
	 * @param functor functor to confirm.
	 *
	 * @throws ScannerException if confirmation fails.
	 */
	public void confirmFunctor(Functor functor) throws ScannerException;

	/**
	 * Convenience method to confirm a {@link jhilbert.data.Statement}.
	 *
	 * @throws ScannerException if confirmation fails.
	 */
	public void confirmStatement() throws ScannerException;

	/**
	 * Convenience method to confirm a label.
	 *
	 * @throws ScannerException if confirmation fails.
	 */
	public void confirmLabel() throws ScannerException;

	/**
	 * Convenience method to confirm a parameter.
	 *
	 * @throws ScannerException if confirmation fails.
	 */
	public void confirmParameter() throws ScannerException;

	/**
	 * Convenience method to confirm a locator.
	 *
	 * @throws ScannerException if confirmation fails.
	 */
	public void confirmLocator() throws ScannerException;

	/**
	 * Convenience method to confirm a string.
	 *
	 * @throws ScannerException if confirmation fails.
	 */
	public void confirmString() throws ScannerException;

}
