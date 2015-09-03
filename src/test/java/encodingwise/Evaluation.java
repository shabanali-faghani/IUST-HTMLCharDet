package encodingwise;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.junit.Test;
import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

import edu.iust.selab.htmlchardet.HTMLCharsetDetector;

@SuppressWarnings("unused")
public class Evaluation {

	@Test
	public void htmlTest() throws Exception {
//		 File directory = new File("old_corpus/GBK/");
//		 File directory = new File("old_corpus/ISO-8859-1/");
//		 File directory = new File("old_corpus/UTF-8/");
//		 File directory = new File("old_corpus/Windows-1252/");
//		 File directory = new File("old_corpus/Windows-1256/");
		 
		 
		 File directory = new File("new_corpus/Shift_JIS/");
//		 File directory = new File("new_corpus/Big5/");
//		File directory = new File("new_corpus/EUC-JP/");
//		File directory = new File("new_corpus/Windows-1251/");
//		File directory = new File("new_corpus/GB2312/");
//		File directory = new File("new_corpus/ISO-2022-JP/");
//		File directory = new File("new_corpus/EUC-KR/");
//		File directory = new File("new_corpus/Windows-1252/");
//		File directory = new File("new_corpus/UTF-8/");

		int counter = 0;
		Map<String, Integer> detectedCharsetStat = new HashMap<String, Integer>();
		for (File file : directory.listFiles()) {
			byte[] htmlByteSequence = FileUtils.readFileToByteArray(file);
			byte[] visibleTextByteSequence = Jsoup.parse(new String(htmlByteSequence, Charset.forName("Shift_JIS")))
					.text().getBytes(Charset.forName("Shift_JIS"));
			
//			 String charset = ibmICU4j(htmlByteSequence);
//			 String charset = ibmICU4j(visibleTextByteSequence);
			
//			String charset = mozillaJCharDet(htmlByteSequence);
			String charset = mozillaJCharDet(visibleTextByteSequence);
			
//			 String charset = HTMLCharsetDetector.detect(htmlByteSequence, false);
			 
			if (detectedCharsetStat.containsKey(charset)) {
				detectedCharsetStat.put(charset, detectedCharsetStat.get(charset) + 1);
			} else {
				detectedCharsetStat.put(charset, 1);
			}

			System.out.println(++counter);
		}

		for (String detectedCharset : detectedCharsetStat.keySet()) {
			System.out.println(detectedCharset + ":\t" + detectedCharsetStat.get(detectedCharset));
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
		int lang = nsDetector.ALL;
		nsDetector det = new nsDetector(lang);
		det.Init(new nsICharsetDetectionObserver() {
			// @Override
			public void Notify(String charset) {
				// HtmlCharsetDetector.found = true;
			}
		});
		det.DoIt(bytes, bytes.length, false);
		det.DataEnd();
		String[] charsets = det.getProbableCharsets();
		det.Reset();
		return charsets[0];
	}
}
