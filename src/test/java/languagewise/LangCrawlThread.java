package languagewise;

import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection.Response;
import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

import edu.iust.selab.htmlchardet.Charsets;
import edu.iust.selab.htmlchardet.HTMLCharsetDetector;

/**
 * 
 * @author shabanali faghani
 * 
 */
public class LangCrawlThread extends Thread {

	private static final Logger LOG = Logger.getLogger(LangCrawlThread.class);

	private BlockingQueue<String> langURLQueue = null;
	private ConcurrentMap<String, ConcurrentMap<String, Integer>> detectedCharsetStat = null;
	private AtomicInteger urlCounter = null;
	private AtomicInteger haveCharsetInHttpHeaderCounter = null;
	private AtomicInteger notExceptionedCounter = null;

	private boolean isStop = false;

	public LangCrawlThread(String threadName, BlockingQueue<String> langURLQueue,
			ConcurrentMap<String, ConcurrentMap<String, Integer>> detectedCharsetStat) {
		super(threadName);
		this.langURLQueue = langURLQueue;
		this.detectedCharsetStat = detectedCharsetStat;
	}

	@Override
	public void run() {
		while (langURLQueue.size() != 0 && !isStop) {
			try {
				String url = langURLQueue.take();
				urlCounter.incrementAndGet();
				url = "http://www." + url;
				Response response = (Response) Jsoup.connect(url).followRedirects(true).timeout(4 * 60 * 1000).execute();
				notExceptionedCounter.incrementAndGet();
				String charset = response.charset();
				if (Charsets.isValid(charset)) {
					charset = Charsets.normalize(charset);
					byte[] htmlByteSequence = IOUtils.toByteArray(new URL(url));
					String detectedCharset = null;
					haveCharsetInHttpHeaderCounter.getAndIncrement();

					detectedCharset = ibmICU4j(htmlByteSequence);
					updateDetectedCharsetStat(charset, detectedCharset, LangCrawler.ibmICUStat);

					detectedCharset = mozillaJCharDet(htmlByteSequence);
					updateDetectedCharsetStat(charset, detectedCharset, LangCrawler.mozillaCharDetStat);

					detectedCharset = HTMLCharsetDetector.detect(htmlByteSequence, false);
					updateDetectedCharsetStat(charset, detectedCharset, LangCrawler.hybridMechanismStat);

				} else if (charset != null && !charset.isEmpty()) {
					LOG.info("An Abnormal Charset: " + charset + "\tURL:" + url);
				}
			} catch (Throwable t) {
				LOG.error(this.getName() + " Exception Message: " + t.getMessage());
				continue;
			}
		}
	}

	private void updateDetectedCharsetStat(String charset, String detectedCharset, String detectorStat) {
		String pair = charset + "->" + detectedCharset;
		if (detectedCharsetStat.get(detectorStat).containsKey(pair)) {
			int preStat = detectedCharsetStat.get(detectorStat).get(pair);
			detectedCharsetStat.get(detectorStat).put(pair, preStat + 1);
		} else {
			detectedCharsetStat.get(detectorStat).put(pair, 1);
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

	public boolean stopCrawl() {
		isStop = true;
		return true;
	}

	public void setUrlCounter(AtomicInteger urlCounter) {
		this.urlCounter = urlCounter;
	}

	public void setHaveCharsetInHttpHeaderCounter(AtomicInteger haveCharsetInHttpHeaderCounter) {
		this.haveCharsetInHttpHeaderCounter = haveCharsetInHttpHeaderCounter;
	}

	public void setNotExceptionedCounter(AtomicInteger notExceptionedCounter) {
		this.notExceptionedCounter = notExceptionedCounter;
	}

}
