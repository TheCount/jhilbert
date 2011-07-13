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
		Pattern PATTERN = Pattern.compile("<jh>.*?</jh>", Pattern.DOTALL);
		CharSequence contents = readFile(new FileInputStream(inputFileName));
		final StringBuilder jhText = new StringBuilder();
		final Matcher matcher = PATTERN.matcher(contents);
		while(matcher.find()) {
			final String match = matcher.group();
			jhText.append('\n');
			jhText.append(match, 4, match.length() - 5); // strip tags
		}
		return new ByteArrayInputStream(jhText.toString().getBytes("UTF-8"));
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
