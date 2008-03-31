package jhilbert.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import jhilbert.exceptions.InputException;
import jhilbert.util.StreamInputSource;

/**
 * A {@link StreamInputSource} associated to a file.
 */
public class FileInputSource extends StreamInputSource {

	/**
	 * Creates a new {@link FileInputStream} from a filename.
	 *
	 * @param filename the file name (must not be <code>null</code>).
	 *
	 * @return the new <code>FileInputStream</code>.
	 *
	 * @throws InputException if the file cannot be found.
	 */
	private static FileInputStream createFileInputStream(final String filename) throws InputException {
		assert (filename != null): "Supplied filename is null.";
		try {
			return new FileInputStream(filename);
		} catch (FileNotFoundException e) {
			throw new InputException("File not found", filename, e);
		}
	}

	/**
	 * Creates a new <code>FileInputSource</code> using the file with the specfied file name.
	 *
	 * @param filename the file name.
	 *
	 * @throws NullPointerException if filename is <code>null</code>.
	 * @throws InputException if the file cannot be found or an I/O problem occurs.
	 */
	public FileInputSource(final String filename) throws InputException {
		super(createFileInputStream(filename), filename);
	}

}
