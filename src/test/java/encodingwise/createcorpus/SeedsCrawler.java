package encodingwise.createcorpus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection.Response;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author shabanali faghani
 * 
 */
@SuppressWarnings("unused")
public class SeedsCrawler {

	private static final Logger LOG = Logger.getLogger(SeedsCrawler.class);

	@Before
	public void setUp() throws Exception {
		PropertyConfigurator.configure("log/log4j.properties");
	}

	@Test
	public void crawl() throws InterruptedException {
		final int numOfThreads = 100;
//		String specialChrset = "Shift_JIS";
//		String specialChrset = "BIG5";
//		String specialChrset = "EUC-JP";
//		String specialChrset = "Windows-1251";
//		String specialChrset = "GB2312";
//		String specialChrset = "ISO-2022-JP";
//		String specialChrset = "EUC-KR";
		String specialChrset = "Windows-1252";
		

//		BlockingQueue<String> seedQueue = shiftJISSeeds();
//		BlockingQueue<String> seedQueue = big5Seeds();
//		BlockingQueue<String> seedQueue = euc_JPSeeds();
//		BlockingQueue<String> seedQueue = windows_1251Seeds();
//		BlockingQueue<String> seedQueue = gb2312Seeds();
//		BlockingQueue<String> seedQueue = iso_2022_jpSeeds();
//		BlockingQueue<String> seedQueue = euc_krSeeds();
		BlockingQueue<String> seedQueue = windows_1252Seeds();
		
		ConcurrentMap<String, Integer> charsetStat = new ConcurrentHashMap<String, Integer>();
		ConcurrentMap<String, Integer> fetchedURLs = new ConcurrentHashMap<String, Integer>();

		charsetStat.put("All fetched URL", 0);
		List<SeedsCrawlThread> crawlThreads = new ArrayList<SeedsCrawlThread>();
		for (int i = 0; i < numOfThreads; i++) {
			SeedsCrawlThread thread = new SeedsCrawlThread("crawlerThread" + i, seedQueue, charsetStat,
					fetchedURLs, specialChrset);
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

	/**
	 * The following methods puts proper URLs into seeds
	 * 
	 * These URLs taken from: 
	 * http://w3techs.com/technologies/overview/character_encoding/all 
	 * 
	 * for example for Shift_JIS encoding, the URLs are taken from:
	 * http://w3techs.com/technologies/details/en-shiftjis/all/all
	 */
	
	
	private BlockingQueue<String> shiftJISSeeds() {
		BlockingQueue<String> seeds = new ArrayBlockingQueue<String>(50000);
		seeds.add("http://www.Sponichi.co.jp");	// http header:Shift_JIS
		seeds.add("http://www.Jalan.net");	// http header:Shift_JIS
		
//		seeds.add("http://www.Kakaku.com");
//		seeds.add("http://www.Itmedia.co.jp");
//		seeds.add("http://www.Kuronekoyamato.co.jp");
//		seeds.add("http://www.Blood-examination.com");
//		seeds.add("http://www.Koolweb2.com");
//		seeds.add("http://www.Dfpolygraph.com");
//		seeds.add("http://www.Astyle.jp");
//		seeds.add("http://www.Kawasaki-disease.gr.jp");
//		seeds.add("http://www.Nifty.com");
//		seeds.add("http://www.2ch.net");
//		seeds.add("http://www.Carview.co.jp");
//		seeds.add("http://www.Production-ir.net");
//		seeds.add("http://www.Miracle1000.in");
//		seeds.add("http://www.Yaunix.com");
//		seeds.add("http://www.Narita-airport.jp");
//		seeds.add("http://www.Tokyuhotels.co.jp");
		
		return seeds;
	}
	
	private BlockingQueue<String> big5Seeds() {
		BlockingQueue<String> seeds = new ArrayBlockingQueue<String>(50000);
		
		seeds.add("http://www.Ruten.com.tw"); // http header:BIG5
		seeds.add("http://www.Cyccea.org.tw"); // http header:BIG5
		seeds.add("http://www.2-hand.info"); // http header:BIG5
		
//		seeds.add("http://www.Udn.com");
//		seeds.add("http://www.Ptt.cc");	
//		seeds.add("http://www.Myfreshnet.com");
//		seeds.add("http://www.Nextmedia.com");
//		seeds.add("http://www.Books.com.tw");
//		seeds.add("http://www.Discuss.com.hk");
//		seeds.add("http://www.I-gamer.net");
//		seeds.add("http://www.Uwants.com");
//		seeds.add("http://www.Pcstore.com.tw");
//		seeds.add("http://www.Colourlessdesign.com");
//		seeds.add("http://www.Adfree.tw");
//		seeds.add("http://www.Fiat.club.tw");
//		seeds.add("http://www.Peacefulmindclinic.com");
//		seeds.add("http://www.Okfun.org"); // http header:UTF-8
//		seeds.add("http://www.Myp2p-pe.net");
//		seeds.add("http://www.Surmiya.com.tw");
//		seeds.add("http://www.Lunsing.net");

		return seeds;
	}	
	
	private BlockingQueue<String> euc_JPSeeds() {
		BlockingQueue<String> seeds = new ArrayBlockingQueue<String>(50000);
		
		seeds.add("http://www.Ldblog.jp"); // http header:EUC-JP
		seeds.add("http://www.Shop-pro.jp");// http header:EUC-JP
		seeds.add("http://www.Cube38.com"); // http header:EUC-JP
		seeds.add("http://www.Eiyaaa.com");// http header:EUC-JP
		
//		seeds.add("http://www.Rakuten.co.jp");
//		seeds.add("http://www.Mixi.jp");
//		seeds.add("http://www.Nikkeibp.co.jp"); 
//		seeds.add("http://www.Blogmura.com");	
//		seeds.add("http://www.Jugem.jp");
//		seeds.add("http://www.A8.net");
//		seeds.add("http://www.Jiji.com");
//		seeds.add("http://www.Aucfan.com");
//		seeds.add("http://www.Kbm.cc");
//		seeds.add("http://www.Cad-data.com");
//		seeds.add("http://www.Onjuku-kankou.com");
//		seeds.add("http://www.A-hikkoshi.com");
//		seeds.add("http://www.Systemax.jp");
//		seeds.add("http://www.Yoshiwarachagall.com");
//		seeds.add("http://www.Strato.co.jp");

		return seeds;
	}
	
	private BlockingQueue<String> windows_1251Seeds() {
		BlockingQueue<String> seeds = new ArrayBlockingQueue<String>(50000);
		
		seeds.add("http://www.Sape.ru");// http header:WINDOWS-1251
		seeds.add("http://www.Wmmail.ru");// http header:WINDOWS-1251
		seeds.add("http://www.Gogetlinks.net"); // http header:WINDOWS-1251
		seeds.add("http://www.Vesti.ru");	// http header:WINDOWS-1251
		seeds.add("http://www.Astrogalaxy.ru");// http header:WINDOWS-1251
		seeds.add("http://www.Gongfu.ru");// http header:WINDOWS-1251
		
//		seeds.add("http://www.Vk.com"); 
//		seeds.add("http://www.Rutracker.org");
//		seeds.add("http://www.Kinopoisk.ru"); 
//		seeds.add("http://www.Sberbank.ru");
//		seeds.add("http://www.Gazeta.ru");
//		seeds.add("http://www.Fotostrana.ru");
//		seeds.add("http://www.Benatton.com.ua");
//		seeds.add("http://www.Eurobeton.ru");
//		seeds.add("http://www.Cweekly.ru");
//		seeds.add("http://www.Borda.ru");
//		seeds.add("http://www.35wenxue.com");
//		seeds.add("http://www.Xiper.net");
//		seeds.add("http://www.Newstime.az");
//		seeds.add("http://www.Trendsoundpromoter.com");

		return seeds;
	}


	private BlockingQueue<String> gb2312Seeds() {
		BlockingQueue<String> seeds = new ArrayBlockingQueue<String>(50000);
		
		seeds.add("http://www.Qq.com"); // http header:GB2312
		seeds.add("http://www.People.com.cn"); 
		seeds.add("http://www.Whyl.net");
		seeds.add("http://www.Homevv.com");
		seeds.add("http://www.tripvv.com");
		
//		seeds.add("http://www.Taobao.com");
//		seeds.add("http://www.Sina.com.cn"); 
//		seeds.add("http://www.Weibo.com");
//		seeds.add("http://www.163.com");
//		seeds.add("http://www.Tmall.com");
//		seeds.add("http://www.39.net");	
//		seeds.add("http://www.Aili.com");
//		seeds.add("http://www.Arnaw.com");
//		seeds.add("http://www.Cqjw.gov.cn");
//		seeds.add("http://www.333309.com");
//		seeds.add("http://www.Absolute-trade.jp");
//		seeds.add("http://www.9281.net"); // UTF-8
//		seeds.add("http://www.Ivc.cn");
//		seeds.add("http://www.93959.com");

		return seeds;
	}
	
	private BlockingQueue<String> iso_2022_jpSeeds() {
		BlockingQueue<String> seeds = new ArrayBlockingQueue<String>(50000);
		seeds.add("http://www.Acnenga.com");
		seeds.add("http://www.a-coco.net");
		seeds.add("http://www.p-coco.com");
		
//		seeds.add("http://www.Wda.jp"); 
//		seeds.add("http://www.Kensuke.bizn"); 
//		seeds.add("http://www.Kabocha.to");
//		seeds.add("http://www.N705.net"); //utf-8
//		seeds.add("http://www.Namazu.org");
//		seeds.add("http://www.E-koyo.com"); 

		return seeds;
	}
	
	private BlockingQueue<String> euc_krSeeds() {
		BlockingQueue<String> seeds = new ArrayBlockingQueue<String>(50000);
		
		seeds.add("http://www.Gmarket.co.kr"); // http header:EUC-KR
		seeds.add("http://www.Incruit.com");// http header:EUC-KR
		seeds.add("http://www.Ppomppu.co.kr"); // http header:EUC-KR
		seeds.add("http://www.A1capital.co.kr");  // http header:EUC-KR
		seeds.add("http://www.Nopp.co.kr");  // http header:EUC-KR
		seeds.add("http://www.Sac.or.kr");  // http header:EUC-KR
		seeds.add("http://www.Cyworld.com");
		seeds.add("http://www.Mk.co.kr");
		seeds.add("http://www.Mt.co.kr");
		seeds.add("http://www.Nongmin.com");	
		seeds.add("http://www.Wolyo.co.kr");
		seeds.add("http://www.Cyworld.com");
		seeds.add("http://www.Mediapen.com");
		seeds.add("http://www.Koreapolyschool.com");
		
//		seeds.add("http://www.Afreeca.com"); 
//		seeds.add("http://www.11st.co.kr");
//		seeds.add("http://www.Inven.co.kr"); 
//		seeds.add("http://www.Hankyung.com");
//		seeds.add("http://www.Speconomy.com");
//		seeds.add("http://www.Icarmax.co.kr");

		return seeds;
	}	
	
	private BlockingQueue<String> windows_1252Seeds() {
		BlockingQueue<String> seeds = new ArrayBlockingQueue<String>(50000);
		
		seeds.add("http://www.Leboncoin.fr"); // http header:WINDOWS-1252
		seeds.add("http://www.T411.me");  // http header:WINDOWS-1252
		seeds.add("http://www.Pokefarm.org"); // http header:WINDOWS-1252
		seeds.add("http://www.Literato.es"); // http header:WINDOWS-1252
		
//		seeds.add("http://www.Elance.com");
//		seeds.add("http://www.Billdesk.com"); 
//		seeds.add("http://www.Ccavenue.com"); 
//		seeds.add("http://www.Ip-adress.com"); 
//		seeds.add("http://www.Crictime.com");
//		seeds.add("http://www.Forumfree.it");
//		seeds.add("http://www.Godlikeproductions.com");
//		seeds.add("http://www.Forumcommunity.net");	
//		seeds.add("http://www.Newsnarayanganj24.net");
//		seeds.add("http://www.Matkaviikko.fi");
//		seeds.add("http://www.Bestsourceautoparts.com");
//		seeds.add("http://www.Coreynahman.com");
//		seeds.add("http://www.Calabriaworldnews.info"); 
//		seeds.add("http://www.Vertelenovelas.net"); 
//		seeds.add("http://www.Elanceonline.com");
//		seeds.add("http://www.Tgareed.com");

		return seeds;
	}	
	
	
	@Test
	public void seedsTest() throws Exception {
//		BlockingQueue<String> seeds = shiftJISSeeds();
//		BlockingQueue<String> seeds = big5Seeds();
//		BlockingQueue<String> seeds = euc_JPSeeds();
//		BlockingQueue<String> seeds = windows_1251Seeds();
//		BlockingQueue<String> seeds = gb2312Seeds();
//		BlockingQueue<String> seeds = iso_2022_jpSeeds();
//		BlockingQueue<String> seeds = euc_krSeeds();
		BlockingQueue<String> seeds = windows_1252Seeds();
		for (String seed : seeds) {
			try {
				Response response = (Response) Jsoup.connect(seed).followRedirects(true).timeout(120 * 1000).execute();
				System.out.println(response.charset() + ":\t" + seed);
			} catch (Exception e) {
				System.out.println("Exeption:\t" + seed);
			}
		}
	}
}
