package jhilbert.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import jhilbert.exceptions.InputException;
import jhilbert.util.InputSource;

/**
 * An {@link InputSource} based on an {@link InputStream}.
 * This class expects the InputStream's data to be in <code>UTF-8</code> format.
 */
public abstract class StreamInputSource implements InputSource {

	/**
	 * character set used: UTF-8.
	 */
	private static final String CHARSET_NAME="UTF-8";

	/**
	 * input stream reader.
	 */
	private final InputStreamReader isr;

	/**
	 * context.
	 */
	private final String context;

	/**
	 * Creates a new stream input source from an {@link InputStream}.
	 *
	 * @param inputStream input stream (must not be <code>null</code>).
	 * @param context context String to inform the user of the source of the <code>inputStream</code>, such as a file
	 * 	name or a network host.
	 *
	 * @throws InputException if the <code>UTF-8</code> character set is not supported.
	 */
	protected StreamInputSource(final InputStream in, final String context) throws InputException {
		this.context = context;
		assert (in != null): "Supplied InputStream is null.";
		try {
			isr = new InputStreamReader(in, CHARSET_NAME);
		} catch (UnsupportedEncodingException e) {
			throw new InputException("UTF-8 encoding not supported", context, e);
		}
	}

	public int read() throws InputException {
		try {
			final int high = isr.read();
			if (Character.isDefined(high) && !Character.isHighSurrogate((char) high) && !Character.isLowSurrogate((char) high)) // highly likely
				return high;
			if (high == -1)
				return -1;
			if (Character.isLowSurrogate((char) high))
				throw new InputException("Unexpected low surrogate " + Integer.toHexString(high) + " (this should not happen)", context);
			final int low = isr.read();
			if (low == -1)
				throw new InputException("Incomplete unicode character with high surrogate " + Integer.toHexString(high), context);
			if (!Character.isSurrogatePair((char) high, (char) low))
				throw new InputException("Invalid surrogate pair: (" + Integer.toHexString(high) + ", " + Integer.toHexString(low) + ")", context);
			final int codepoint = Character.toCodePoint((char) high, (char) low);
			if (Character.isDefined(codepoint))
				return codepoint;
			throw new InputException("Invalid Unicode codepoint: U+" + Integer.toHexString(codepoint),
				context);
		} catch (IOException e) {
			throw new InputException("I/O error", context, e);
		}
	}

}
