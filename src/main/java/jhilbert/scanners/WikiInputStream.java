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
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WikiInputStream extends InputStream {

	private InputStream delegate;
	private Writer buffer;
	private String contents;
	private ArrayList<String> expectedErrors;

	private WikiInputStream() {
		this.buffer = new StringWriter();
		expectedErrors = new ArrayList();
	}

	private void finishWriting() throws IOException {
		buffer.flush();
		contents = buffer.toString();
		delegate = new ByteArrayInputStream(contents.getBytes("UTF-8"));
		buffer = null;
	}

	private String getContents() {
		return contents;
	}

	@Override
	public int read() throws IOException {
		return delegate.read();
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}

	@Override
	public int read(byte[] arg0, int arg1, int arg2) throws IOException {
		return delegate.read(arg0, arg1, arg2);
	}

	@Override
	public int read(byte[] b) throws IOException {
		return delegate.read(b);
	}

	public static WikiInputStream create(String inputFileName) throws IOException {
		return create(new FileInputStream(inputFileName));
	}

	public static WikiInputStream create(InputStream inputStream) throws IOException {
		WikiInputStream wiki = new WikiInputStream();
		wiki.readWikiText(inputStream);
		wiki.finishWriting();
		return wiki;
	}

	private void readWikiText(InputStream input) throws IOException {
		Pattern START_OR_END = Pattern.compile("(<jh>|</jh>)");
		CharSequence contents = readFile(input);
		findExpectedErrors(contents);
		final Matcher matcher = START_OR_END.matcher(contents);
		int startTag = -1;
		while(matcher.find()) {
			final int matchStart = matcher.start();
			final int matchEnd = matcher.end();
			final CharSequence matched = contents.subSequence(matchStart, matchEnd);
			if ("<jh>".equals(matched)) {
				startTag = matchStart + matched.length();
			}
			else {
				if (startTag == -1) {
					throw new RuntimeException(
						"Found </jh> tag without matching <jh> tag");
				}
				buffer.append('\n');
				buffer.append(contents.subSequence(startTag, matchStart));
				startTag = -1;
			}
		}
	}

	static String read(String input) throws IOException {
		return create(new ByteArrayInputStream(input.getBytes("UTF-8"))).getContents();
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

	private void findExpectedErrors(CharSequence wikiText) {
		Pattern EXPECTED_ERRORS = Pattern.compile(
			"\\{\\{\\s*error expected\\s*[|]\\s*([^|}]+)\\s*\\}\\}");
		Matcher matcher = EXPECTED_ERRORS.matcher(wikiText);
		while (matcher.find()) {
			expectedErrors.add(matcher.group(1));
		}
	}

	public List<String> expectedErrors() {
		return expectedErrors;
	}

}
