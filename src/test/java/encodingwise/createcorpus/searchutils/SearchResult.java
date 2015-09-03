package encodingwise.createcorpus.searchutils;

public class SearchResult {

	private String url;
	private String title;
	private String snippet;

	public SearchResult(String url, String title, String snippet) {
		this.url = url;
		this.title = title;
		this.snippet = snippet;
	}

	public String getUrl() {
		return url;
	}

	public String getTitle() {
		return title;
	}

	public String getSnippet() {
		return snippet;
	}
}
