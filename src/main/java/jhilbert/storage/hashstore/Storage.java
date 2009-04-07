/*
    JHilbert, a verifier for collaborative theorem proving
    Copyright © 2008, 2009 Alexander Klauer

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

package jhilbert.storage.hashstore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.RandomAccessFile;
import java.io.StreamCorruptedException;

import java.nio.charset.Charset;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import jhilbert.data.Module;

import jhilbert.storage.StorageException;

import jhilbert.utils.FileAccessManager;

import org.apache.log4j.Logger;

/**
 * Hashstore storage.
 * Module names are hashed securely and then stored in the filesystem
 * with the hashed names.
 */
public final class Storage extends jhilbert.storage.Storage {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(Storage.class);

	/**
	 * Hex digits.
	 */
	private static final char[] HEXDIGITS = { '0', '1', '2', '3', '4', '5', '6', '7',
		'8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	/**
	 * Digest.
	 */
	private static final MessageDigest HASHER;

	/**
	 * Digest charset.
	 */
	private static final Charset HASHER_CHARSET = Charset.forName("UTF-8");

	/**
	 * Static initializer.
	 */
	static {
		try {
			HASHER = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("NoSuchAlgorithmException in static hashstore Storage initializer", e);
		}
	}

	/**
	 * Converts a file name to a hashstore pathname.
	 *
	 * @param locator file name.
	 *
	 * @return hashstore pathname.
	 */
	private static String n2p(final String name) {
		assert (name != null): "Supplied pathname is null";
		assert (name.length() >= 2): "Supplied pathname is too short";
		return jhilbert.Main.getHashstorePath() + '/' + name.charAt(0) + '/' + name.substring(0,2) + '/' + name;
	}

	/**
	 * Converts a byte array to a file name in a unique way.
	 *
	 * @param a byte array.
	 *
	 * @return file name.
	 */
	private static String b2n(final byte... a) {
		assert (a != null): "Supplied byte array is null";
		final StringBuilder result = new StringBuilder();
		for (final byte b: a) {
			final int i = b + 128;
			result.append(HEXDIGITS[i >>> 4])
				.append(HEXDIGITS[i & 0x0F]);
		}
		return result.toString();
	}

	/**
	 * Converts a locator to a hash byte array.
	 *
	 * @param l locator.
	 *
	 * @return hash byte array.
	 */
	private static byte[] l2b(final String l) {
		assert (l != null): "Supplied locator is null";
		synchronized (HASHER) {
			// FIXME: this may be an congestion point; maybe use a separate hasher for each call...
			HASHER.update(l.getBytes(HASHER_CHARSET));
			return HASHER.digest();
		}
	}

	/**
	 * Converts a locator to a hashstore pathname.
	 *
	 * @param l locator
	 *
	 * @return hashstore pathname.
	 */
	private static String l2p(final String l) {
		assert (l != null): "Supplied locator is null";
		return n2p(b2n(l2b(l)));
	}

	/**
	 * Creates a new hashstore storage.
	 * There should not be more than one instance of this class.
	 *
	 * @throws StorageException if directory hierarchy cannot be created.
	 */
	public Storage() throws StorageException {
		// create hash directory structure
		final String basePath = jhilbert.Main.getHashstorePath();
		for (int i = 0; i != 16; ++i)
			for (int j = 0; j != 16; ++j) {
				final File dir = new File(basePath + '/' + HEXDIGITS[i] + '/'
						+ HEXDIGITS[i] + HEXDIGITS[j] + '/');
				if (!(dir.isDirectory() || dir.mkdirs())) {
					logger.error("Unable to create hashstore directory hierarchy at " + basePath);
					throw new StorageException("Unable to create hashstore directory hierarchy");
				}
			}
	}

	public @Override boolean isVersioned() {
		return false; // FIXME
	}

	protected @Override Module retrieveModule(final String locator, final long version) throws StorageException {
		assert (locator != null): "Supplied locator is null";
		assert (version >= -1): "Invalid revision number supplied";
		try {
			final RandomAccessFile file = FileAccessManager.getFile(l2p(locator));
			try {
				synchronized (file) {
					final ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file.getFD()));
					return (Module) ois.readObject();
				}
			} finally {
				FileAccessManager.putFile(file);
			}
		} catch (FileNotFoundException e) {
			throw new StorageException("File not found (should not normally happen)", e);
		} catch (ClassNotFoundException e) {
			throw new StorageException("File does not contain module data", e);
		} catch (ClassCastException e) {
			throw new StorageException("File does not contain module data", e);
		} catch (OptionalDataException e) {
			throw new StorageException("File does not contain module data", e);
		} catch (InvalidClassException e) {
			throw new StorageException("File contains obsolete data", e);
		} catch (StreamCorruptedException e) {
			throw new StorageException("File contains inconsistent data", e);
		} catch (IOException e) {
			throw new StorageException("I/O error while loading module", e);
		}
	}

	protected @Override void storeModule(final Module module, final String locator, final long version) throws StorageException {
		assert (module != null): "Supplied module is null";
		assert (locator != null): "Supplied locator is null";
		assert (version >= -1): "Invalid revision number supplied";
		try {
			final RandomAccessFile file = FileAccessManager.getFile(l2p(locator));
			try {
				synchronized (file) {
					file.getChannel().truncate(0);
					final ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file.getFD()));
					oos.writeObject(module);
				}
			} finally {
				FileAccessManager.putFile(file);
			}
		} catch (InvalidClassException e) {
			final AssertionError err = new AssertionError("Invalid seralization class. This should not happen");
			err.initCause(e);
			throw err;
		} catch (NotSerializableException e) {
			final AssertionError err = new AssertionError("Unseralizable class found. This should not happen");
			err.initCause(e);
			throw err;
		} catch (IOException e) {
			throw new StorageException("I/O error while storing module", e);
		}
	}

}
