package jhilbert.scanners;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

public class WikiInputStreamTest extends TestCase {

	public void testPageWithoutJh() throws Exception {
		assertEquals("", WikiInputStream.read(new ByteArrayInputStream(
			"This is some [[wikitext]] {{and a template}}.".getBytes("UTF-8"))));
	}

	public void testFindJhSections() throws Exception {
		assertEquals("\nvar (\nformula p)", WikiInputStream.read(new ByteArrayInputStream(
			"Non-jhilbert <jh>var (</jh> and then <jh>formula p)</jh>".getBytes("UTF-8"))));
	}

}
