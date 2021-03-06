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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WikiInputStream extends InputStream {

	// This class contains only static methods
	private WikiInputStream() {
	}

	@Override
	public int read() throws IOException {
		throw new RuntimeException("This class contains only static methods");
	}

	public static InputStream create(String inputFileName) throws IOException {
		return create(new FileInputStream(inputFileName));
	}

	public static InputStream create(InputStream inputStream) throws IOException {
		return new ByteArrayInputStream(read(inputStream).getBytes("UTF-8"));
	}

	static String read(InputStream inputStream) throws IOException {
		Pattern PATTERN = Pattern.compile("<jh>.*?</jh>", Pattern.DOTALL);
		CharSequence contents = readFile(inputStream);
		final StringBuilder jhText = new StringBuilder();
		final Matcher matcher = PATTERN.matcher(contents);
		while(matcher.find()) {
			final String match = matcher.group();
			jhText.append('\n');
			jhText.append(match, 4, match.length() - 5); // strip tags
		}
		return jhText.toString();
	}

	private static CharSequence readFile(InputStream inputStream) throws IOException {
		final ByteArrayOutputStream contents = new ByteArrayOutputStream();
		int nread;
		byte[] buffer = new byte[32768];
		while ((nread = inputStream.read(buffer)) > 0) {
			contents.write(buffer, 0, nread);
		}
		return contents.toString("UTF-8");
	}

}
