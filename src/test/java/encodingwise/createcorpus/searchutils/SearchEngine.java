package encodingwise.createcorpus.searchutils;

import java.util.List;

public interface SearchEngine {
	public List<SearchResult> search(String query, int numOfResults);
}
