package org.springfield.lou.application.types;

import org.json.simple.JSONObject;
import org.springfield.lou.screen.Screen;

public interface SearcherResultsHandler {
	public void handleResults(Searcher searcher, JSONObject results);
	public void handleCounts(JSONObject counts);
	public void handleResults(Searcher searcher, Screen s, JSONObject results);
	public void handleCounts(Screen s, JSONObject counts);
}
