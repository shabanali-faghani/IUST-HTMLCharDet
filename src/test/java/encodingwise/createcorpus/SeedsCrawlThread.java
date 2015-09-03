package encodingwise.createcorpus;

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

import edu.iust.selab.htmlchardet.Charsets;

/**
 * 
 * @author shabanali faghani
 * 
 */
public class SeedsCrawlThread extends Thread {

	private static final Logger LOG = Logger.getLogger(SeedsCrawlThread.class);

	ConcurrentMap<String, Integer> fetchedURLs = null;
	ConcurrentMap<String, Integer> charsetStat = null;
	BlockingQueue<String> seedQueue = null;
	String SpecialChrset = null;
	private boolean isStop = false;

	public SeedsCrawlThread(String threadName, BlockingQueue<String> seedQueue,
			ConcurrentMap<String, Integer> charsetStat, ConcurrentMap<String, Integer> fetchedURLs, String SpecialChrset) {
		super(threadName);
		this.seedQueue = seedQueue;
		this.charsetStat = charsetStat;
		this.fetchedURLs = fetchedURLs;
		this.SpecialChrset = SpecialChrset;
	}

	@Override
	public void run() {
		while (!isStop) {
			String threadSeed = null;
			try {
				threadSeed = seedQueue.take();
				if (fetchedURLs.containsKey(threadSeed)) {
					continue;
				}
				Response response = (Response) Jsoup.connect(threadSeed).followRedirects(true).timeout(120 * 1000).execute();
				String charset = response.charset();
//				charset = "EUC-KR";
				charsetStat.put("All fetched URL", charsetStat.get("All fetched URL") + 1);
				if (Charsets.isValid(charset)) {
					charset = Charsets.normalize(charset);
					int stat = 1;
					if (charsetStat.containsKey(charset)) {
						stat = charsetStat.get(charset) + 1;
					}
					charsetStat.put(charset, stat);
					if (charset.equalsIgnoreCase("UTF-8")) {
						continue;
					}
					byte[] content = IOUtils.toByteArray(new URL(threadSeed));
					if (content.length < 10000) {
						continue;
					}
					for (String fetchedUrl : fetchedURLs.keySet()) {
						if (content.length < fetchedURLs.get(fetchedUrl) + 100
								&& content.length > fetchedURLs.get(fetchedUrl) - 100) {
							continue;
						}
					}
					File file = new File("test-data/encoding-wise/" + charset + "/" + stat);
					FileUtils.writeByteArrayToFile(file, content);
					fetchedURLs.put(threadSeed, content.length);
					if (charset.equalsIgnoreCase(SpecialChrset)) {
						getAllLink(content, charset, threadSeed);
					} else {
						threadSeed = tunnel(Jsoup.parse(new String(content, Charset.forName(charset))));
					}
				} else {
					threadSeed = tunnel(Jsoup.parse(new URL(threadSeed), 120 * 1000));
				}
			} catch (Throwable t) {
				LOG.debug(this.getName() + " Exception Message: " + t.getMessage());
				// LOG.debug(this.getName(), t);
				try {
					threadSeed = seedQueue.take();
				} catch (InterruptedException e) {
					continue;
				}
			}
		}
	}

	private void getAllLink(byte[] content, String charset, String threadSeed) throws Exception {
		Document domTree = Jsoup.parse(new String(content, charset));
		Elements links = domTree.select("a");
		String HomePageHost = new URL(threadSeed).getHost();
		for (Element link : links) {
			URL url;
			try {
				url = new URL(link.attr("abs:href"));
			} catch (MalformedURLException e) {
				continue; // Ignor invalid URLs
			}
			if (url.toString().contains("#")) {
				continue; // Ignore relative URLs
			}
			if (!url.toString().contains(".htm")) {
				continue; // sure to be a valid html page, not pdf, mp3, ...
			}
			if (!url.getHost().equalsIgnoreCase(HomePageHost)) {
				continue; // only special pages should be crawled
			}
			seedQueue.add(url.toString());
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
				continue; // Ignore relative URLs
			}
			urls.add(url);
		}
		if (urls.size() == 0) {
			return seedQueue.take();
		}
		Random randomGenerator = new Random();
		int randNum = randomGenerator.nextInt(urls.size());
		if (seedQueue.size() == 0 && urls.size() > 1) {
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
