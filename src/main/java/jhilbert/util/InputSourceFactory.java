package jhilbert.util;

import jhilbert.exceptions.InputException;
import jhilbert.util.FileInputSource;
import jhilbert.util.InputSource;

/**
 * Factory class for {@link InputSource}s.
 *
 * FIXME: Right now, only files are supported
 */
public abstract class InputSourceFactory {

	/**
	 * Create an interface input source from a locator String.
	 *
	 * @param locator interface specifier.
	 *
	 * @return an InputSource for the specified interface.
	 *
	 * @throws InputException if the input source cannot be created.
	 */
	public static InputSource createInterfaceInputSource(final String locator) throws InputException {
		return new FileInputSource(locator + ".ghi");
	}

}
