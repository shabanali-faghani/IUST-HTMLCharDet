package encodingwise.corpus.searchutil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 
 * @author shaabanali faghani
 * 
 */
public class Google implements SearchEngine {

	private static final Logger LOG = Logger.getLogger(Google.class);

	@Override
	public List<SearchResult> search(String query, int numOfResults) {
		query = query.trim().replaceAll("\\s", "+");
		String queryUrl = "http://www.google.com/search?q=" + query + "&num=" + numOfResults;

		Document resultsPage = null;
		try {
			resultsPage = Jsoup.connect(queryUrl).timeout(60 * 1000).userAgent("Mozilla").get();
		} catch (IOException e) {
			LOG.error("Could not search through Google.", e);
			return null;
		}
		List<SearchResult> googleSearchResults = new ArrayList<SearchResult>();
		Elements gs = resultsPage.select("li.g");
		for (Element g : gs) {
			Element h3 = g.select("h3").first();
			String url = h3.select("a").first().attr("href").substring(7);
			url = url.substring(0, url.indexOf("&sa="));
			String title = h3.text();
			String snippet = g.select("span.st").text();
			googleSearchResults.add(new SearchResult(url, title, snippet));
		}
		return googleSearchResults;
	}

	public SearchResult imFeelingLucky(String query) {
		// TODO: something here, a thing like the following code ...
		List<SearchResult> results = this.search(query, 20);
		return results.get(new Random().nextInt(results.size()));
	}
}