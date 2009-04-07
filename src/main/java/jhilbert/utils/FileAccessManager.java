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

package jhilbert.utils;

import java.io.IOException;
import java.io.RandomAccessFile;

import java.nio.channels.FileLock;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Static file access manager for exclusive file access.
 * Provides system wide, thread-safe file access, provided that all exclusive
 * files are accessed through this facility and the usage rules are adhered
 * to.
 * <br />
 * <b>Usage rules:</b>
 * <ol>
 * <li>Get file via {@link FileAccessManager#getFile}.</li>
 * <li>Synchronize on that file object whenever you use its methods.</li>
 * <li>When done, release file via {@link FileAccessManager#putFile}.
 * 	You might want to use a <code>try/finally</code> construct to ensure
 * 	proper release. <em>Do not close the file exlicitly!</em>
 * </li>
 * </ol>
 */
public final class FileAccessManager {

	/**
	 * File info structure.
	 */
	private static final class FileInfo {

		/**
		 * File.
		 */
		public RandomAccessFile file;

		/**
		 * File lock.
		 */
		public FileLock lock;

		/**
		 * Reference count.
		 */
		public int refCount;

	}

	/**
	 * Name to file info map.
	 */
	private static final Map<String, FileInfo> nameMap;

	/**
	 * File to name map.
	 */
	private static final Map<RandomAccessFile, String> fileMap;

	/**
	 * Initializer.
	 */
	static {
		nameMap = new HashMap();
		fileMap = new IdentityHashMap();
	}

	/**
	 * Get a file by name.
	 *
	 * @param name file name.
	 *
	 * @return file object.
	 *
	 * @throws IOException if an I/O-Error occurs.
	 */
	public static RandomAccessFile getFile(final String name) throws IOException {
		assert (name != null): "Supplied file name is null";
		synchronized (nameMap) {
			final FileInfo info = nameMap.get(name);
			if (info != null) {
				++info.refCount;
				return info.file;
			}
		}
		final RandomAccessFile result = new RandomAccessFile(name, "rw"); // do not sync as opening the file might take time
		synchronized (nameMap) {
			// try again in case we lost a race
			FileInfo info = nameMap.get(name);
			if (info != null) {
				++info.refCount;
				return info.file;
			}
			// add new file info
			info = new FileInfo();
			info.file = result;
			info.lock = result.getChannel().tryLock();
			if (info.lock == null)
				throw new IOException("Unable to lock file " + name);
			info.refCount = 1;
			nameMap.put(name, info);
			fileMap.put(result, name);
			return result;
		}
	}

	/**
	 * Put a file.
	 *
	 * @param file file to put.
	 *
	 * @throws IOException on error.
	 */
	public static void putFile(final RandomAccessFile file) throws IOException {
		assert (file != null): "Supplied file is null";
		synchronized (nameMap) {
			final String name = fileMap.get(file);
			if (name == null)
				throw new IOException("Invalid file: " + file);
			final FileInfo info = nameMap.get(name);
			assert (info != null): "No info for valid name. This cannot happen.";
			--info.refCount;
			if (info.refCount > 0)
				return;
			nameMap.remove(name);
			fileMap.remove(file);
			info.lock.release();
		}
		file.close();
	}

}
