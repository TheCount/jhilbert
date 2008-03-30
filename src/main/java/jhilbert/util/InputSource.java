package jhilbert.util;

import jhilbert.exceptions.InputException;

/**
 * An input source, such as one provided by an InputStream.
 */
public interface InputSource {

	/**
	 * Returns Unicode codepoint of the next character.
	 * If there are no more characters, <code>-1</code> will be returned.
	 * Once this happens, all subsequent calls will also return <code>-1</code>.
	 *
	 * @return codepoint of the next character, or -1 if there are no more characters.
	 *
	 * @throws InputException if a problem, such as an I/O error, occurs.
	 */
	public int read() throws InputException;

}
