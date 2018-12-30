package encodingwise.corpus.searchutil;

import java.util.List;

public interface SearchEngine {
	public List<SearchResult> search(String query, int numOfResults);
}
