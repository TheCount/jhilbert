package jhilbert.utils;

import java.io.IOException;
import java.io.InputStream;

public class Io {

	/**
	 * Read from input stream until end of file or buffer.length bytes have been
	 * read.
	 * @return Number of bytes read. This will be buffer.length unless end of file
	 * was reached. If end of file is reached immediately, return zero.
	 */
	public static int read(InputStream input, byte[] buffer) throws IOException {
		int offset = 0;
		int nread;
		int toread = buffer.length;
		while ((nread = input.read(buffer, offset, toread)) > 0) {
			offset += nread;
			toread -= nread;
		}
		return offset;
	}

}
