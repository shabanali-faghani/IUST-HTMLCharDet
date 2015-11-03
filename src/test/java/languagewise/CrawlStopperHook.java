package languagewise;

import java.util.List;

import org.apache.log4j.Logger;

/**
 * 
 * @author shabanali faghani
 *
 */
public class CrawlStopperHook extends Thread {
	
	private static final Logger LOG = Logger.getLogger(CrawlStopperHook.class);
	private List<LangCrawlThread> crawlThreads;

	public CrawlStopperHook(List<LangCrawlThread> crawlThreads) {
		this.crawlThreads = crawlThreads;
	}

	@Override
	public void run() {
		LOG.info("Stopping crawl process, please wait...");
		for (LangCrawlThread crawlThread : crawlThreads) {
			crawlThread.stopCrawl();
		}
		for (LangCrawlThread crawlThread : crawlThreads) {
			try {
				crawlThread.join();
			} catch (InterruptedException e) {
				LOG.error("There is an isuue about joining thread: " + crawlThread.getName(), e);
			}
		}
	}
}
