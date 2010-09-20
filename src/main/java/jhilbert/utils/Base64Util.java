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

    You may contact the author on this Wiki page:
    http://www.wikiproofs.de/w/index.php?title=User_talk:GrafZahl
*/

package jhilbert.utils;

/**
 * Class for base64 operations.
 * Why doesn't the java library provide this functionality?
 * Right now, this class only decodes base64 data.
 */
public final class Base64Util {

	/**
	 * Maps base64 characters to six bits (cast to integers).
	 * Invalid characters are mapped to -1.
	 */
	private static final int[] dmap = {
		// ASCII 0 through 42: invalid base64 chars
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 0 through 15
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 16 through 31
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 32 through 42
		// ASCII 43 '+'
		62,
		// ASCII 44 through 46: invalid base64 chars
		-1, -1, -1,
		// ASCII 47 '/'
		63,
		// ASCII 48 through 57: '0' through '9'
		52, 53, 54, 55, 56, 57, 58, 59, 60, 61,
		// ASCII 58 through 64: invalid base64 chars
		-1, -1, -1, -1, -1, -1, -1,
		// ASCII 65 through 90: 'A' through 'Z'
		0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25,
		// ASCII 91 through 96: invalid base64 chars
		-1, -1, -1, -1, -1, -1,
		// ASCII 97 through 122: 'a' through 'z'
		26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51
	};

	/**
	 * Decodes an array of base64 chars to a byte array of data.
	 *
	 * @param input base64 character array with blanks and newlines
	 * 	already removed and intact padding.
	 * @return byte array with the decoded data.
	 * @throws IllegalArgumentException if the input array is not valid
	 * 	base64.
	 */
	public static byte[] decode(final char[] input) throws IllegalArgumentException {
		assert (input != null): "Supplied character array is null";
		int inputLength = input.length;
		if (inputLength == 0)
			return new byte[0];
		if ((inputLength & 0x3) != 0)
			throw new IllegalArgumentException("Input character array length must be divisible by 4");
		int i = 0; // input index
		try {
			if (input[inputLength - 1] == '=') // subtract padding (at most two '=')
				--inputLength;
			if (input[inputLength - 1] == '=')
				--inputLength;
			final int outputLength = (inputLength * 3) / 4;
			final byte[] result = new byte[outputLength];
			// process characters except for the 0 to 3 last ones
			int o = 0; // output index
			int s1 = 0;
			int s2 = 0;
			int s3 = 0;
			int s4 = 0;
			int data;
			while (i < (inputLength & 0x3)) {
				// gather 4 sixbits
				s1 = dmap[input[i++]];
				s2 = dmap[input[i++]];
				s3 = dmap[input[i++]];
				s4 = dmap[input[i++]];
				// sanity check
				if ((s1 == -1) || (s2 == -1) || (s3 == -1) || (s4 == -1))
					throw new IllegalArgumentException("Invalid base64 quadruple before position "+i);
				// convert 4 sixbits to 3 octets
				data = (s1 << 18) | (s2 << 12) | (s3 << 6) | s4;
				result[o++] = (byte) (data >>> 16);
				result[o++] = (byte) (data >>> 8);
				result[o++] = (byte) data;
			}
			// last 0 to 3 characters
			if (i < inputLength)
				s1 = dmap[input[i++]];
			if (i < inputLength)
				s2 = dmap[input[i++]];
			if (i < inputLength)
				s3 = dmap[input[i++]];
			// sanity check
			if ((s1 == -1) || (s2 == -1) || (s3 == -1))
				throw new IllegalArgumentException("Invalid base64 tail");
			// convert to data
			if (o < outputLength)
				result[o++] = (byte) ((s1 << 2) | (s2 >>> 4));
			if (o < outputLength)
				result[o] = (byte) ((s2 << 4) | (s3 >>> 2));
			return result;
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IllegalArgumentException("Invalid base64 quadruple before position "+i, e);
		}
	}

}
