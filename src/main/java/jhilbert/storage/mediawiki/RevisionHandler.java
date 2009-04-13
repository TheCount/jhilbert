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

package jhilbert.storage.mediawiki;

import org.apache.log4j.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Class to extract page/revision data from MediaWiki API XML.
 */
final class RevisionHandler extends DefaultHandler {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(RevisionHandler.class);

	/**
	 * Page element name.
	 */
	private static final String PAGE_ELT = "page";

	/**
	 * Revision element name.
	 */
	private static final String REVISION_ELT = "rev";

	/**
	 * Revision ID attribute name.
	 */
	private static final String REVID_ATTR = "revid";

	/**
	 * Title attribute name.
	 */
	private static final String TITLE_ATTR = "title";

	/**
	 * Invalid attribute name.
	 */
	private static final String INVALID_ATTR = "invalid";

	/**
	 * Revision.
	 */
	private long revision;

	/**
	 * Page title.
	 */
	private String pageTitle;

	/**
	 * Wikitext.
	 */
	private final StringBuilder text;

	/**
	 * Current element.
	 */
	private String currentElement;

	/**
	 * Creates a new <code>RevisionHandler</code>.
	 */
	RevisionHandler() {
		revision = -1;
		pageTitle = null;
		text = new StringBuilder();
		currentElement = null;
	}

	/**
	 * Obtains the revision ID.
	 *
	 * @return revision ID, or <code>-1</code> if no revision ID was
	 * 	found.
	 */
	public long getRevision() {
		return revision;
	}

	/**
	 * Obtains the page title.
	 *
	 * @return page title, or <code>null</code> if no page title was
	 * 	found.
	 */
	public String getPageTitle() {
		return pageTitle;
	}

	/**
	 * Obtains the wiki text.
	 *
	 * @return wikitext, or the empty string, if no wikitext was found.
	 */
	public String getText() {
		return text.toString();
	}

	public @Override void characters(final char[] ch, int start, int length) {
		if (!REVISION_ELT.equals(currentElement))
			return;
		text.append(ch, start, length);
	}

	public @Override void endElement(final String uri, final String localName, final String qName) {
		currentElement = null;
	}

	public @Override void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
		if (logger.isTraceEnabled())
			logger.trace("API element uri=" + uri + " localName=" + localName + " qName=" + qName);
		currentElement = qName;
		if (PAGE_ELT.equals(qName)) {
			if (pageTitle != null)
				throw new SAXException("There should not be more than one page in the query result");
			final String title = attributes.getValue(TITLE_ATTR);
			if (title == null)
				throw new SAXException("Missing title attribute in page element");
			if (attributes.getValue(INVALID_ATTR) != null)
				throw new SAXException("Title is invalid");
			pageTitle = title;
		}
		if (REVISION_ELT.equals(qName)) {
			if (revision != -1)
				throw new SAXException("There should not be more than one revision in the query result");
			final String revisionString = attributes.getValue(REVID_ATTR);
			if (revisionString == null)
				return;
			try {
				revision = Long.parseLong(revisionString);
			} catch (NumberFormatException e) {
				throw new SAXException("Unable to parse revision number from " + revisionString, e);
			}
			if (revision < 0)
				throw new SAXException("Query returned negative revision number");
		}
	}

}
