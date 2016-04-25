package encodingwise;

import ir.ac.iust.htmlchardet.HTMLCharsetDetector;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.junit.Test;
import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

@SuppressWarnings("unused")
public class Evaluation {

	@Test
	public void htmlTest() throws Exception {
		// String charset = "UTF-8";
		// String charset = "Windows-1251";
		// String charset = "GBK";
		// String charset = "Windows-1256";
		String charset = "Shift_JIS";

		// String charset = "ISO-8859-1";
		// String charset = "Windows-1252";
		// String charset = "Big5";
		// String charset = "EUC-JP";
		// String charset = "GB2312";
		// String charset = "ISO-2022-JP";
		// String charset = "EUC-KR";

		File directory = new File("test-data/encoding-wise/corpus/" + charset);

		int counter = 0;
		Map<String, Integer> detectedCharsetStat = new HashMap<String, Integer>();
		for (File file : directory.listFiles()) {
			byte[] htmlByteSequence = FileUtils.readFileToByteArray(file);
			byte[] visibleTextByteSequence = Jsoup.parse(new String(htmlByteSequence, Charset.forName(charset))).text()
					.getBytes(Charset.forName(charset));

			// String detectedCharset = this.ibmICU4j(htmlByteSequence);
			// String detectedCharset = this.ibmICU4j(visibleTextByteSequence);

			// String detectedCharset = this.mozillaJCharDet(htmlByteSequence);
			String detectedCharset = this.mozillaJCharDet(visibleTextByteSequence);

            // String detectedCharset = HTMLCharsetDetector.detect(htmlByteSequence, false);

			if (detectedCharsetStat.containsKey(detectedCharset)) {
				detectedCharsetStat.put(detectedCharset, detectedCharsetStat.get(detectedCharset) + 1);
			} else {
				detectedCharsetStat.put(detectedCharset, 1);
			}

			System.out.println(++counter);
		}

		System.out.println("--------------------------------------");
		for (Entry<String, Integer> entry : detectedCharsetStat.entrySet()) {
			System.out.println(entry.getKey() + ":\t" + entry.getValue());
		}
	}

	private String ibmICU4j(byte[] bytes) {
		CharsetDetector charsetDetector = new CharsetDetector();
		charsetDetector.setText(bytes);
		CharsetMatch charsetMatch = charsetDetector.detect();
		String charset = charsetMatch.getName();
		return charset;
	}

	private String mozillaJCharDet(byte[] bytes) {
		nsDetector det = new nsDetector(nsDetector.ALL);
		det.DoIt(bytes, bytes.length, false);
		det.DataEnd();
		String[] charsets = det.getProbableCharsets();
		det.Reset();
		return charsets[0];
	}
}
