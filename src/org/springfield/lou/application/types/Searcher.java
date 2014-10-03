package org.springfield.lou.application.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springfield.fs.FSList;
import org.springfield.fs.Fs;
import org.springfield.fs.FsNode;
import org.springfield.lou.application.types.conditions.EqualsCondition;
import org.springfield.lou.application.types.conditions.FilterCondition;
import org.springfield.lou.application.types.conditions.TypeCondition;
import org.springfield.lou.screen.Screen;

public class Searcher implements Runnable{
	
	private FSList allNodes;
	private Screen screen;
	private String query;
	private String sortDirection;
	private String sortField;
	private String type = "all";
	private SearcherResultsHandler handler;
	private Filter filter;
	private Filter counterFilter;
	private List<FsNode> nodes;
	private JSONObject countriesForProviders;
	private boolean devel = false;
	private boolean wantedna = true;
	private HashMap<String, HashMap<String, FilterCondition>> counterConditions;
	
	public Searcher(SearcherResultsHandler handler, FSList allNodes, String query, String type, String sortDirection, String sortField, Filter filter, HashMap<String, HashMap<String, FilterCondition>> counterConditions, boolean devel){
		this.allNodes = allNodes;
		this.handler = handler;
		this.query = query;
		this.sortDirection = sortDirection;
		this.sortField = sortField;
		this.filter = filter;
		this.devel = devel;
		this.screen = null;
		this.countriesForProviders = new JSONObject();
		this.counterConditions = counterConditions;
		this.counterFilter = this.createCounterFilter(counterConditions);
	}
	
	public Searcher(SearcherResultsHandler handler, Screen screen, FSList allNodes, String query, String type, String sortDirection, String sortField, Filter filter, HashMap<String, HashMap<String, FilterCondition>> counterConditions, boolean devel){
		this.allNodes = allNodes;
		this.screen = screen;
		this.handler = handler;
		this.query = query;
		this.sortDirection = sortDirection;
		this.sortField = sortField;
		this.filter = filter;
		this.devel = devel;
		this.countriesForProviders = new JSONObject();
		this.counterConditions = counterConditions;
		this.counterFilter = this.createCounterFilter(counterConditions);
	}
		
	private JSONObject createResultsSet(){
		JSONObject resultSet = new JSONObject();
		JSONArray all = new JSONArray();
		resultSet.put("all", all);
		
		Filter typeFilter = getTypeFilter();
		typeFilter.run(nodes);
		
		ArrayList<FilterCondition> types = typeFilter.getConditions();
		for(Iterator<FilterCondition> typeIterator = types.iterator(); typeIterator.hasNext();){
			TypeCondition typeCondition = (TypeCondition) typeIterator.next();
			String type = typeCondition.getAllowedValue();
			
			JSONArray resultsForType = new JSONArray();
			
			for(Iterator<FsNode> nodeIterator = typeCondition.getPassed().iterator(); nodeIterator.hasNext();){
				FsNode node = nodeIterator.next();
				
				String path = node.getPath();
				String[] splits = path.split("/");
				String provider = splits[4];
				String providerNodeValue = node.getProperty(FieldMappings.getSystemFieldName("provider"));
				
				if(!this.countriesForProviders.containsKey(provider)){
					FsNode providerNode = Fs.getNode("/domain/euscreenxl/user/" + provider + "/account/default");
					try{
						String fullProviderString = providerNode.getProperty("birthdata");
						this.countriesForProviders.put(provider, fullProviderString);
					}catch(NullPointerException npe){
						this.countriesForProviders.put(provider, node.getProperty(FieldMappings.getSystemFieldName("provider")));
					}
				}
				
				if(!this.countriesForProviders.containsKey(providerNodeValue)){
					FsNode providerNode = Fs.getNode("/domain/euscreenxl/user/" + provider + "/account/default");
					try{
						String fullProviderString = providerNode.getProperty("birthdata");
						this.countriesForProviders.put(providerNodeValue, fullProviderString);
					}catch(NullPointerException npe){
						this.countriesForProviders.put(providerNodeValue, node.getProperty(FieldMappings.getSystemFieldName("provider")));
					}
				}
				
				JSONObject result = new JSONObject();
				result.put("type", node.getName());
				result.put("screenshot", this.setEdnaMapping(node.getProperty(FieldMappings.getSystemFieldName("screenshot"))));
				result.put("title", node.getProperty(FieldMappings.getSystemFieldName("title")));
				result.put("originalTitle", node.getProperty(FieldMappings.getSystemFieldName("originalTitle")));
				result.put("provider", this.countriesForProviders.get(provider));
				result.put("year", node.getProperty(FieldMappings.getSystemFieldName("year")));
				result.put("language", node.getProperty(FieldMappings.getSystemFieldName("language")));
				result.put("duration", node.getProperty(FieldMappings.getSystemFieldName("duration")));
				result.put("seriesEnglish", node.getProperty(FieldMappings.getSystemFieldName("seriesEnglish")));
				result.put("series", node.getProperty(FieldMappings.getSystemFieldName("series")));
				result.put("id", node.getId());
								
				resultsForType.add(result);
				all.add(result);
			}
			
			resultSet.put(type, resultsForType);
		} 
				
		
		return resultSet;
	}
	
	private Filter getTypeFilter(){
		ArrayList<FilterCondition> types = new ArrayList<FilterCondition>();
		
		types.add(new TypeCondition("video"));
		types.add(new TypeCondition("picture"));
		types.add(new TypeCondition("doc"));
		types.add(new TypeCondition("audio"));
		types.add(new TypeCondition("series"));
		
		Filter filter = new Filter(types);
		
		return filter;
	};
	
	private Filter createCounterFilter(HashMap<String, HashMap<String, FilterCondition>> categorisedConditions){
		ArrayList<FilterCondition> conditions = new ArrayList<FilterCondition>();
		
		for(Iterator<String> categoryIter = categorisedConditions.keySet().iterator(); categoryIter.hasNext();){
			HashMap<String, FilterCondition> category = categorisedConditions.get(categoryIter.next());
			for(Iterator<FilterCondition> conditionIter = category.values().iterator(); conditionIter.hasNext();){
				conditions.add(conditionIter.next());
			}
		}
		
		return new Filter(conditions);
	}
	
	private String setEdnaMapping(String screenshot) {
		if(screenshot != null){
			if (!wantedna) {
				screenshot = screenshot.replace("edna/", "");
			} else {
				int pos = screenshot.indexOf("edna/");
				if 	(pos!=-1) {
					screenshot = "http://images.euscreenxl.eu/"+screenshot.substring(pos+5);
				}
			}
		}
		return screenshot;
	}
	
	private void createCounts(){
		System.out.println("createCounts()");
		System.out.println(counterFilter);
		Counter c = new Counter(handler, screen, type, nodes, counterFilter, counterConditions, devel);
		Thread t = new Thread(c);
		t.start();
	}
	
	public void setType(String type){
		this.type = type;
		this.createCounts();
	}
	
	@Override
	public void run() {
		System.out.println("Starting search thread!");
		// TODO Auto-generated method stub
		
		try{
			if (query == null || query.equals("*")) { 
				if (sortField.equals("id")) {
					nodes = allNodes.getNodes(); // get them all unsorted
				} else {
					nodes = allNodes.getNodesSorted(sortField, sortDirection); // get them all sorted
				}
			} else {
				if (sortField.equals("id")) {
					nodes = allNodes.getNodesFiltered(query); // filter them but not sorted
				} else {
					nodes = allNodes.getNodesFilteredAndSorted(query, sortField, sortDirection); // and sorted
				}
			}
			
			nodes = filter.apply(nodes);
			JSONObject results = createResultsSet();
			
			if(screen != null){
				handler.handleResults(this, screen, results);
			}else{
				handler.handleResults(this, results);
			}

			this.createCounts();
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public JSONObject getCountriesForProviders(){
		return this.countriesForProviders;
	}
}
