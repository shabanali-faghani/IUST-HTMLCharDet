package encodingwise.corpus;

import encodingwise.corpus.searchutil.Google;
import encodingwise.corpus.searchutil.SearchResult;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * 
 * @author shabanali faghani
 * 
 */
public class Crawler {

	private static final Logger LOG = Logger.getLogger(Crawler.class);

	@BeforeClass
	public static void setUp() throws Exception {
		PropertyConfigurator.configure("log/log4j.properties");
	}

	@Test
	public void crawl() throws InterruptedException {
		final int numOfThreads = 150;
		String query = "site:.us";
		// String query = "site:.jp";
		// String query = "site:.sa";

		Google google = new Google();
		LOG.info("Getting primitive seeds from Google ...");
		List<SearchResult> results = google.search(query, numOfThreads);
		if (results == null) {
			LOG.error("Googling failed!");
			System.exit(0);
		}
		LOG.info("Googling done!");
		ConcurrentMap<String, Integer> charsetStat = new ConcurrentHashMap<String, Integer>();
		BlockingQueue<String> seedQueue = new ArrayBlockingQueue<String>(200);

		charsetStat.put("All fetched pages", 0);
		List<CrawlThread> crawlThreads = new ArrayList<CrawlThread>();
		int i = 0;
		Iterator<SearchResult> it = results.iterator();
		while (it.hasNext()) {
			CrawlThread thread = new CrawlThread("crawler" + i++, it.next().getUrl(), charsetStat, seedQueue);
			crawlThreads.add(thread);
			thread.start();
		}

		while (true) {
			Thread.sleep(5 * 1000);
			for (String charset : charsetStat.keySet()) {
				System.out.println(charset + ":\t" + charsetStat.get(charset));
			}
			System.out.println("------------------------------------");
		}
	}

}
