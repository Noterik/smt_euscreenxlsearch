package org.springfield.lou.application.types;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONObject;
import org.springfield.fs.FsNode;
import org.springfield.lou.application.types.conditions.AndCondition;
import org.springfield.lou.application.types.conditions.EqualsCondition;
import org.springfield.lou.application.types.conditions.FilterCondition;
import org.springfield.lou.application.types.conditions.NotCondition;
import org.springfield.lou.application.types.conditions.TypeCondition;
import org.springfield.lou.screen.Screen;

public class Counter implements Runnable {

	private SearcherResultsHandler handler;
	private Screen screen = null;
	private String type;
	private List<FsNode> nodes;
	private Filter counterFilter;
	private HashMap<String, HashMap<String, FilterCondition>> counterConditions;
	private boolean devel;
	
	public Counter(SearcherResultsHandler handler, String type, List<FsNode> nodes, Filter counterFilter, HashMap<String, HashMap<String, FilterCondition>> counterConditions, boolean devel) {
		this.handler = handler;
		this.type = type;
		this.nodes = nodes;
		this.counterFilter = counterFilter;
		this.counterConditions = counterConditions;
		this.devel = devel;
	}
	
	public Counter(SearcherResultsHandler handler, Screen s, String type, List<FsNode> nodes, Filter counterFilter, HashMap<String, HashMap<String, FilterCondition>> counterConditions, boolean devel) {
		this(handler, type, nodes, counterFilter, counterConditions, devel);
		this.screen = s;
	}

	@Override
	public void run() {
		
		Filter filter = new Filter();
		AndCondition andCondition = new AndCondition();
		
		if(!this.devel){ // Production mode
			EqualsCondition condition = new EqualsCondition("public", "true");
			
			andCondition.add(condition);
		}
		
		NotCondition nCondition = new NotCondition("provider", "AGENCY");
		andCondition.add(nCondition);
		
		filter.addCondition(andCondition);
		nodes = filter.apply(nodes);
		
		if(!type.equals("all")){
			Filter typeFilter = new Filter();
			typeFilter.addCondition(new TypeCondition(type));
			
			nodes = typeFilter.apply(nodes);
		}
		
		counterFilter.run(nodes);
		
		JSONObject countedResults = new JSONObject();
		for(Iterator<String> catIterator = counterConditions.keySet().iterator(); catIterator.hasNext();){
			String catName = catIterator.next();
			HashMap<String, FilterCondition> catEntry = counterConditions.get(catName);
			JSONObject categoryResults = new JSONObject();
			countedResults.put(catName, categoryResults);
			for(Iterator<String> fieldIterator = catEntry.keySet().iterator(); fieldIterator.hasNext();){
				String fieldName = fieldIterator.next();
				FilterCondition fieldCondition = catEntry.get(fieldName);
				categoryResults.put(fieldName, fieldCondition.getPassed().size());
			}
		}
		if(screen != null){
			handler.handleCounts(screen, countedResults);
		}else{
			handler.handleCounts(countedResults);
		}
	}

}
