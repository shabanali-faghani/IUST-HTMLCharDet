package encodingwise.corpus;

import ir.ac.iust.htmlchardet.Charsets;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 
 * @author shabanali faghani
 * 
 */
public class CrawlThread extends Thread {

	private static final Logger LOG = Logger.getLogger(CrawlThread.class);

	String threadSeed = null;
	ConcurrentMap<String, Integer> charsetStat = null;
	BlockingQueue<String> seedQueue = null;
	private boolean isStop = false;

	public CrawlThread(String threadName, String threadSeed, ConcurrentMap<String, Integer> charsetStat,
			BlockingQueue<String> seedQueue) {
		super(threadName);
		this.threadSeed = threadSeed;
		this.charsetStat = charsetStat;
		this.seedQueue = seedQueue;
	}

	@Override
	public void run() {
		while (!isStop) {
			Response response = null;
			try {
				response = (Response) Jsoup.connect(threadSeed).followRedirects(true).timeout(120 * 1000).execute();
				String charset = response.charset();
				if (Charsets.isValid(charset)) {
					charset = Charsets.normalize(charset);
					int stat = 1;
					if (charsetStat.containsKey(charset)) {
						stat = charsetStat.get(charset) + 1;
					}
					charsetStat.put(charset, stat);
					byte[] content = IOUtils.toByteArray(new URL(threadSeed));
					if (content.length < 7000) {
						continue;
					}
					File file = new File("test-data/encoding-wise/temp/" + charset + "/" + stat);
					FileUtils.writeByteArrayToFile(file, content);
					threadSeed = tunnel(Jsoup.parse(new String(content, Charset.forName(charset))));
				} else {
					threadSeed = tunnel(Jsoup.parse(new URL(threadSeed), 120 * 1000));
				}
				charsetStat.put("All fetched pages", charsetStat.get("All fetched pages") + 1);
			} catch (Throwable t) {
				LOG.debug(this.getName() + " Exception Message: " + t.getMessage());
				try {
					threadSeed = seedQueue.take();
				} catch (InterruptedException e) {
					continue;
				}
			}
		}
	}

	private String tunnel(Document domTree) throws InterruptedException {
		List<URL> urls = new ArrayList<URL>();
		Elements links = domTree.select("a");
		for (Element link : links) {
			URL url;
			try {
				url = new URL(link.attr("abs:href"));
			} catch (MalformedURLException e) {
				continue;
			}
			if (url.toString().contains("#")) {
				continue; // ignore relative URLs
			}
			urls.add(url);
		}
		if (urls.size() == 0) {
			return seedQueue.take();
		}
		Random randomGenerator = new Random();
		int randNum = randomGenerator.nextInt(urls.size());
		if (seedQueue.size() < 20 && urls.size() > 1) {
			int newRand = 0;
			do {
				newRand = randomGenerator.nextInt(urls.size());
			} while (newRand == randNum);
			seedQueue.put(urls.get(newRand).toString());
		}

		return urls.get(randNum).toString();
	}

	public boolean stopCrawl() {
		isStop = true;
		return true;
	}
}
