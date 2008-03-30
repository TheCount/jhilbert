package jhilbert.data;

/**
 * A token as they are encountered while scanning a LISP symbolic expression.
 */
public class Token implements Cloneable {

	/**
	 * Token classes.
	 */
	public static enum TokenClass {
		/**
		 * Beginning of a list.
		 * Typically denoted by a single opening parenthesis.
		 */
		BEGIN_EXP,

		/**
		 * End of a list.
		 * Typically denoted by a single closing parenthesis.
		 */
		END_EXP,

		/**
		 * Atomic symbolic expression.
		 * A concatenation of {@link Char.CharClass#ATOM_CHAR} characters.
		 */
		ATOM
	}

	/**
	 * String representation of this token.
	 */
	public final String repr;

	/**
	 * Token class of this token.
	 */
	public final TokenClass tokenClass;

	/**
	 * Creates a new token with the specified String representation and token class.
	 *
	 * @param repr String representation of this token.
	 * @param tokenClass token class of this token.
	 *
	 * @throws NullPointerException if repr is <code>null</code>.
	 */
	public Token(final String repr, final TokenClass tokenClass) {
		if (repr == null)
			throw new NullPointerException("Specified representation is null.");
		this.repr = repr;
		this.tokenClass = tokenClass;
	}

	/**
	 * Copy constructor.
	 *
	 * @param token the token to be copied.
	 */
	protected Token(final Token token) {
		this(token.repr, token.tokenClass);
	}

	public Token clone() {
		return new Token(this);
	}

	public int hashCode() {
		return repr.hashCode();
	}

	/**
	 * Equality comparison.
	 * Two non-null tokens are equal if and only if they are equal as Strings and they are in the same class.
	 *
	 * @param o Object to be compared for equality.
	 */
	public boolean equals(final Object o) {
		try {
			final Token t = (Token) o;
			return (repr.equals(t.repr) && (t.tokenClass == tokenClass));
		} catch (NullPointerException e) {
			return false;
		} catch (ClassCastException e) {
			return false;
		}
	}

	/**
	 * Return the string representation of this Token.
	 *
	 * @return String representation of this Token.
	 */
	public String toString() {
		return repr;
	}

}
