package languagewise;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author shabanali faghani
 * 
 */
public class LangCrawler {

	private static final Logger LOG = Logger.getLogger(LangCrawler.class);

	private ConcurrentMap<String, ConcurrentMap<String, Integer>> detectedCharsetStat = null;
	private AtomicInteger urlCounter = new AtomicInteger(0);
	private AtomicInteger haveCharsetInHttpHeaderCounter = new AtomicInteger(0);
	private AtomicInteger notExceptionedCounter = new AtomicInteger(0);

	public static final String ibmICUStat = "Statistics of IBM ICU";
	public static final String mozillaCharDetStat = "Statistics of Mozilla CharDet";
	public static final String hybridMechanismStat = "Statistics of Hybrid Mechanism";

	@BeforeClass
	public void setUp() throws Exception {
		PropertyConfigurator.configure("log/log4j.properties");

		detectedCharsetStat = new ConcurrentHashMap<String, ConcurrentMap<String, Integer>>();
		detectedCharsetStat.put(ibmICUStat, new ConcurrentHashMap<String, Integer>());
		detectedCharsetStat.put(mozillaCharDetStat, new ConcurrentHashMap<String, Integer>());
		detectedCharsetStat.put(hybridMechanismStat, new ConcurrentHashMap<String, Integer>());
	}

	@Test
	public void crawl() throws InterruptedException, IOException {
		final int numOfThreads = 100;

		String specLangFile = "Alexa-Persian-Urls";
		// String specLangFile = "Alexa-Arabic-Urls";
		// String specLangFile = "Alexa-English-Urls";
		// String specLangFile = "Alexa-Japanese-Urls";
		// String specLangFile = "Alexa-Russian-Urls";
		// String specLangFile = "Alexa-Chinese-Urls";
		// String specLangFile = "Alexa-Germany-Urls";
		// String specLangFile = "Alexa-Indian-Urls";

		BlockingQueue<String> langURLQueue = new ArrayBlockingQueue<String>(50000);
		langURLQueue.addAll(FileUtils.readLines(new File("test-data/language-wise/" + specLangFile)));

		List<LangCrawlThread> crawlThreads = new ArrayList<LangCrawlThread>();
		for (int i = 0; i < numOfThreads; i++) {
			LangCrawlThread thread = new LangCrawlThread("crawler" + i, langURLQueue, detectedCharsetStat);
			thread.setUrlCounter(urlCounter);
			thread.setHaveCharsetInHttpHeaderCounter(haveCharsetInHttpHeaderCounter);
			thread.setNotExceptionedCounter(notExceptionedCounter);
			crawlThreads.add(thread);
			thread.start();
			LOG.info("Thread " + thread.getName() + " got launched!");
		}

		Runtime.getRuntime().addShutdownHook(new CrawlStopperHook(crawlThreads));

		while (true) {
			Thread.sleep(2 * 1000);
			System.out.println("Number of processed URLs:\t" + urlCounter.get());
			System.out.println("Number of pages with valid charset in http header:\t"
					+ haveCharsetInHttpHeaderCounter.get());
			System.out.println("Number of not exception:\t" + notExceptionedCounter.get());
			for (String detector : detectedCharsetStat.keySet()) {
				System.out.println(detector + ":");
				for (String pair : detectedCharsetStat.get(detector).keySet()) {
					System.out.println(pair + ":\t" + detectedCharsetStat.get(detector).get(pair));
				}
				System.out.println("--------------------------------------------------");
			}
			System.out.println("**************************************************************");
		}
	}

}
