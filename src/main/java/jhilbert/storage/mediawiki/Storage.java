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

package jhilbert.storage.mediawiki;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import jhilbert.commands.CommandException;
import jhilbert.commands.CommandFactory;
import jhilbert.data.DataException;
import jhilbert.data.DataFactory;
import jhilbert.data.Module;
import jhilbert.scanners.ScannerException;
import jhilbert.scanners.ScannerFactory;
import jhilbert.scanners.TokenFeed;
import jhilbert.storage.StorageException;

import org.xml.sax.SAXException;

/**
 * MediaWiki API based storage.
 */
public final class Storage extends jhilbert.storage.Storage {

	/**
	 * Encoding.
	 */
	private static final String ENCODING = "UTF-8";

	/**
	 * Parser factory.
	 */
	private static final SAXParserFactory parserFactory = SAXParserFactory.newInstance();

	/**
	 * Pattern to find JHilbert text.
	 */
	private static final Pattern PATTERN = Pattern.compile("<jh>.*?</jh>", Pattern.DOTALL);

	/**
	 * Handles a generic MediaWiki request.
	 *
	 * @param req request string (XML format and query action are implied).
	 *
	 * @return {@link RevisionHandler} with which the request was parsed.
	 *
	 * @throws StorageException on error.
	 */
	private RevisionHandler handleRequest(final String req) throws StorageException {
		assert (req != null): "Supplied request is null";
		try {
			final URL requestURL = new URL(jhilbert.Main.getMediaWikiApi() + "?format=xml&action=query&" + req);
			final URLConnection request = requestURL.openConnection();
			final SAXParser parser = parserFactory.newSAXParser();
			final RevisionHandler handler = new RevisionHandler();
			request.connect();
			parser.parse(request.getInputStream(), handler);
			return handler;
		} catch (MalformedURLException e) {
			throw new StorageException("Malformed request", e);
		} catch (SocketTimeoutException e) {
			throw new StorageException("Timeout while talking to MediaWiki API", e);
		} catch (IOException e) {
			throw new StorageException("I/O error", e);
		} catch (ParserConfigurationException e) {
			throw new StorageException("Unable to create parser", e);
		} catch (SAXException e) {
			throw new StorageException("Error parsing MediaWiki reply", e);
		}
	}

	public @Override boolean isVersioned() {
		return true;
	}

	protected @Override String getCanonicalName(final String locator) throws StorageException {
		assert (locator != null): "Supplied locator is null";
		try {
			final String urlEncodedLocator = URLEncoder.encode(locator, ENCODING);
			final RevisionHandler handler = handleRequest("titles=" + urlEncodedLocator);
			final String result = handler.getPageTitle();
			if (result == null)
				throw new StorageException("Invalid title: " + locator);
			return result;
		} catch (UnsupportedEncodingException e) {
			throw new StorageException("UTF-8 encoding not supported", e);
		}
	}

	protected @Override long getCurrentRevision(final String locator) throws StorageException {
		assert (locator != null): "Supplied locator is null";
		try {
			final String urlEncodedLocator = URLEncoder.encode(locator, ENCODING);
			final RevisionHandler handler = handleRequest("titles=" + urlEncodedLocator + "&prop=revisions&rvprop=ids&rvlimit=1");
			final long result = handler.getRevision();
			if (result == -1)
				throw new StorageException("Module " + locator + " does not exist");
			return result;
		} catch (UnsupportedEncodingException e) {
			throw new StorageException("UTF-8 encoding not supported", e);
		}
	}

	protected @Override Module retrieveModule(final String locator, final long revision) throws StorageException {
		assert (locator != null): "Supplied locator is null";
		assert (!"".equals(locator)): "Proof module supplied";
		assert (revision >= 0): "Invalid version number supplied";
		try {
			final String urlEncodedLocator = URLEncoder.encode(locator, ENCODING);
			final RevisionHandler handler = handleRequest("revids=" + revision + "&prop=revisions&rvprop=content");
			if (!locator.equals(handler.getPageTitle()))
				throw new StorageException("Supplied revision " + revision + " does not match supplied title " + locator);
			final String text = handler.getText();
			if ("".equals(text))
				throw new StorageException("Supplied revision " + revision + " does not contain any text");
			// extract JHilbert text
			final StringBuilder jhText = new StringBuilder();
			final Matcher matcher = PATTERN.matcher(text);
			while(matcher.find()) {
				final String match = matcher.group();
				jhText.append('\n');
				jhText.append(match, 4, match.length() - 5); // strip tags
			}
			// parse text
			final Module module = DataFactory.getInstance().createModule(locator, revision);
			final TokenFeed tokenFeed = ScannerFactory.getInstance().createTokenFeed(new ByteArrayInputStream(jhText.toString().getBytes(ENCODING)));
				// FIXME: This encodes UTF-8 just to decode it yet again.
				// Solution: change jhilbert.scanners.impl.{CharScanner,StreamTokenFeed} constructors to accept java.io.Reader.
				// Other solution: Use a FilterInputStream instead of regexes
			CommandFactory.getInstance().processCommands(module, tokenFeed);
			return module;
		} catch (UnsupportedEncodingException e) {
			throw new StorageException(ENCODING + " encoding not supported", e);
		} catch (DataException e) {
			throw new StorageException("Unable to create interface module", e);
		} catch (ScannerException e) {
			throw new StorageException("Unable to create token feed", e);
		} catch (CommandException e) {
			throw new StorageException("Unable to parse module", e);
		}
	}

	protected @Override void storeModule(final Module module, final String locator, final long version) {
		// nothing: MediaWiki must do that for us
	}

	protected @Override void eraseModule(final String locator, final long version) {
		// nothing: MediaWiki must do that for us
	}

}
