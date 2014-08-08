/* 
* EuscreenxlpreviewApplication.java
* 
* Copyright (c) 2012 Noterik B.V.
* 
* This file is part of Lou, related to the Noterik Springfield project.
*
* Lou is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Lou is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Lou.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.springfield.lou.application.types;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springfield.fs.FSList;
import org.springfield.fs.FSListManager;
import org.springfield.fs.Fs;
import org.springfield.fs.FsNode;
import org.springfield.lou.application.Html5Application;
import org.springfield.lou.application.types.conditions.AndCondition;
import org.springfield.lou.application.types.conditions.EqualsCondition;
import org.springfield.lou.application.types.conditions.FilterCondition;
import org.springfield.lou.application.types.conditions.OrCondition;
import org.springfield.lou.application.types.conditions.TimeRangeCondition;
import org.springfield.lou.application.types.conditions.TypeCondition;
import org.springfield.lou.homer.LazyHomer;
import org.springfield.lou.screen.Screen;


public class EuscreenxlsearchApplication extends Html5Application{
	
	/*
	 * Cached copy of all the nodes. Not sure if this is really neccesary. 
	 */
	private FSList allNodes;
	private HashMap<String, String> countriesForProviders;
	
	/*
	 * Arraylist containing all the categories of fields. This will be used to categorize the conditions, and to fill 
	 * the select boxes
	 */
	private ArrayList<String> availableConditionFieldCategories;
	private ArrayList<Integer> decades;
	private HashMap<String, HashMap<String, FilterCondition>> cachedCategorisedConditions;
	private Filter cachedCounterFilter;
	private boolean wantedna = true;
		
	/*
	 * Constructor for the preview application for EUScreen providers
	 * so they can check and debug their uploaded collections.
	 */
	public EuscreenxlsearchApplication(String id) {
		super(id); 
		
		this.countriesForProviders = new HashMap<String, String>();
		
		// allways 'loads' the full result set with all the items from the manager
		String uri = "/domain/euscreenxl/user/*/*"; // does this make sense, new way of mapping (daniel)
		allNodes = FSListManager.get(uri);
		
		this.availableConditionFieldCategories = new ArrayList<String>();
		this.availableConditionFieldCategories.add("language");
		this.availableConditionFieldCategories.add("decade");
		this.availableConditionFieldCategories.add("topic");
		this.availableConditionFieldCategories.add("provider");
		this.availableConditionFieldCategories.add("publisher");
		this.availableConditionFieldCategories.add("genre");
		this.availableConditionFieldCategories.add("country");
		
		this.decades = new ArrayList<Integer>();
		for(int i = 1900; i <= 2010; i += 10){
			this.decades.add(i);
		}
		
		this.cachedCategorisedConditions = this.createCategoryCountsConditions();
		this.cachedCounterFilter = this.createCounterFilter(this.cachedCategorisedConditions);
		
		List<FsNode> nodes = allNodes.getNodes();
		
		if(!this.inDevelMode()){
			Filter filter = new Filter();
			EqualsCondition condition = new EqualsCondition("public", "true");
			filter.addCondition(condition);
			nodes = filter.apply(nodes);
		}
		
		this.cachedCounterFilter.run(nodes);
				
		// default scoop is each screen is its own location, so no multiscreen effects
		setLocationScope("screen"); 
		
		//refer the header and footer elements from euscreenxl element application. 
		this.addReferid("mobilenav", "/euscreenxlelements/mobilenav");
		this.addReferid("header", "/euscreenxlelements/header");
		this.addReferid("footer", "/euscreenxlelements/footer");
		this.addReferid("linkinterceptor", "/euscreenxlelements/linkinterceptor");
	}
	
	public void init(Screen s){
		if(!this.inDevelMode()){
			s.putMsg("linkinterceptor", "", "interceptLinks()");
		}
	}
	
	private boolean inDevelMode() {
    	return LazyHomer.inDeveloperMode();
    }
	
	public void setInitialCounts(Screen s){
		s.putMsg("filter", "", "setCounts(" + this.getCounterObject(false) + ")");
	}
	
	public void setMobile(Screen s){
		System.out.println("setMobile()");
		s.setProperty("mobile", true);
	}
	
	public void parseURLParams(Screen s){
		JSONObject startupParameters = new JSONObject();
		if(s.getParameter("query") != null){
			String query = (String) s.getParameter("query");
			s.setProperty("searchQuery", query);
			System.out.println("MOBILE:");
			System.out.println(s.getProperty("mobile"));
			if(s.getProperty("mobile") != null){
				s.putMsg("mobilesearchinput", "", "setQuery(" + query + ")");
			}else{
				s.putMsg("searchinput", "", "setQuery(" + query + ")");
			}
			startupParameters.put("query", query);
		}
		
		if(s.getParameter("sortField") != null){
			String sortField = (String) s.getParameter("sortField");
			s.setProperty("sortField", (String) s.getParameter("sortField"));
			startupParameters.put("sortField", sortField);
		}	
		
		if(s.getParameter("sortDirection") != null){
			String sortDirection = (String) s.getParameter("sortDirection");
			s.setProperty("sortDirection", (String) s.getParameter("sortDirection"));
			startupParameters.put("sortDirection", sortDirection);
		}
		
		if(s.getParameter("activeType") != null){
			String activeType = (String) s.getParameter("activeType");
			s.setProperty("activeType", activeType);
			if(s.getProperty("mobile") == null)
				s.putMsg("tabs", "", "loadTab(" + activeType + ")");
			startupParameters.put("activeType", activeType);
		}
		
		if(s.getParameter("activeFields") != null){
			try {
				String encodedString = (String) s.getParameter("activeFields");
				startupParameters.put("activeFields", encodedString);
				String activeFields = java.net.URLDecoder.decode(encodedString, "UTF-8");
				this.setClientSelectedField(s, activeFields, false);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		s.putMsg("history", "", "setStartupParameters(" + startupParameters + ")");
		if(s.getProperty("searchQuery") != null){
			this.search(s);
		}
	};
	
	public void setSearchQuery(Screen s, String data){
		this.setSearchQuery(s, data, true);
	}
	
	public void clearFields(Screen s){
		s.setProperty("clientSelectedFields", new JSONObject());
		this.search(s);
	}
		
	/**
	 * Sets the search query on the screen, this will be used to search through the nodes. 
	 * 
	 * @param s The screen for which to set the search query.
	 * @param data The JSONObject containing the search query. 
	 */
	public void setSearchQuery(Screen s, String data, boolean refresh){
		JSONObject queryData = (JSONObject) JSONValue.parse(data);
		
		String query = (String) queryData.get("query");
		
		if(query.equals("")){
			query = "*";
		}
		
		query = query.toLowerCase();
		if(s.getParameter("query") == null){
			this.setHistoryParameter(s, "query", query);
		}
		
		s.setProperty("searchQuery", query);
		search(s);
	}
	
	/**
	 * Executes the search for the given screen. 
	 * 
	 * @param s The screen for which to execute the search
	 */
	public void search(Screen s){
		System.out.println("EuscreenxlsearchApplication.search()");
		String resultsElement;
		
		if(s.getProperty("mobile") == null){
			resultsElement = "results";
		}else{
			resultsElement = "mobileresults";
		}
		
		this.clearResults(s);
		s.putMsg(resultsElement, "", "loading(true)");
		
		// lets get the nodes from the fslist, depending on input we get them all or
		// filtered and sorted
		List<FsNode> nodes = null;
		
		//reset the counters before doing the search.
		this.resetCounters(s);
		
		//Get the search parameter from the Screen object
		String query = (String) s.getProperty("searchQuery");
		
		String sortDirection = (String) s.getProperty("sortDirection");
		String sortField = (String) s.getProperty("sortField");
				
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
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		//Get the filter from the screen object, this filter is created from the selection in the selectboxes on the page. 
		Filter filter = (Filter) s.getProperty("filter");
		nodes = filter.apply(nodes);
						
		s.setProperty("rawNodes", nodes);
				
		JSONObject results = createResultSet(nodes);
		s.setProperty("results", results);
		
		if(s.getProperty("mobile") == null){
			renderTabs(s);
		}
		
		s.putMsg("resulttopbar", "", "show()");
		this.setResultAmountOnClient(s);
		
		JSONObject activeFieldFilters = (JSONObject) s.getProperty("clientSelectedFields");
		String activeType = (String) s.getProperty("activeType");
		if(query == null && (activeType == null || activeType.equals("all")) && (activeFieldFilters == null || activeFieldFilters.isEmpty())){
			s.putMsg("filter", "", "setCounts(" + this.getCounterObject(false) + ")");
		}else{
			s.putMsg("filter", "", "setCounts(" + this.getCounterObject(true, s) + ")");
		}
		
		this.createTypeChunking(s);
		this.sendChunkToClient(s);
		s.putMsg(resultsElement, "", "loading(false)");
	}
	
	private void setHistoryParameter(Screen s, String key, String value){
		JSONObject historyObject = new JSONObject();
		historyObject.put(key, value);
		s.putMsg("history", "", "setParameter(" + historyObject + ")");
	}
	
	private void renderTabs(Screen s){
		JSONObject message = new JSONObject();
		JSONObject results = (JSONObject) s.getProperty("results");
		String activeType = (String) s.getProperty("activeType");
		for(Iterator<String> i = results.keySet().iterator(); i.hasNext();){
			String type = i.next();
			ArrayList resultsForType = (ArrayList) results.get(type);
			if(resultsForType.size() > 0){
				message.put(type, true);
			}else{
				if(type.equals(activeType)){
					s.putMsg("tabs", "", "loadTab(all)");
				}
				message.put(type, false);
			}
		}
		s.putMsg("tabs", "", "setActiveTabs(" + message + ")");
	}
	
	private void setResultAmountOnClient(Screen s){
		JSONObject results = (JSONObject) s.getProperty("results");
		String activeType = (String) s.getProperty("activeType");
		JSONArray resultsForType = (JSONArray) results.get(activeType);
		s.putMsg("resultcounter", "", "setAmount(" + resultsForType.size() + ")");
	}
	
	public void setDefaultType(Screen s){
		s.setProperty("activeType", "all");
	}
	
	private void clearResults(Screen s){
		if(s.getProperty("mobile") != null){
			s.putMsg("mobileresults", "", "clear()");
		}else{
			s.putMsg("results", "", "clear()");
		}
	}
	
	private void setResultsOnClient(Screen s, JSONObject results){
		System.out.println("setResultsOnClient()");
		if(s.getProperty("mobile") != null){
			s.putMsg("mobileresults", "", "setResults(" + results + ")");
		}else{
			s.putMsg("results", "", "setResults(" + results + ")");
		}
	}
	
	private JSONObject createResultSet(List<FsNode> nodes){
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
				
				if(!this.countriesForProviders.containsKey(provider)){
					FsNode providerNode = Fs.getNode("/domain/euscreenxl/user/" + provider + "/account/default");
					try{
						String fullProviderString = providerNode.getProperty("birthdata");
						this.countriesForProviders.put(provider, fullProviderString);
					}catch(NullPointerException npe){
						this.countriesForProviders.put(provider, node.getProperty(FieldMappings.getSystemFieldName("provider")));
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
				result.put("id", node.getId());
								
				resultsForType.add(result);
				all.add(result);
			}
			
			resultSet.put(type, resultsForType);
		} 
				
		return resultSet;
	};
	
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
	
	private Filter getTypeFilter(){
		ArrayList<FilterCondition> types = new ArrayList<FilterCondition>();
		
		types.add(new TypeCondition("video"));
		types.add(new TypeCondition("picture"));
		types.add(new TypeCondition("doc"));
		types.add(new TypeCondition("audio"));
		
		Filter filter = new Filter(types);
		
		return filter;
	};
	
	public void createTypeChunking(Screen s){
		HashMap<String, Integer> types = new HashMap<String, Integer>();
	 
		types.put("all", 1);
		types.put("video", 1);
		types.put("picture", 1);
		types.put("doc", 1);
		types.put("audio", 1);
		
		s.setProperty("chunkSize", 10);
		s.setProperty("typesChunks", types);
	}
	
	public void setActiveType(Screen s, String type){
		String resultsElement;
		
		if(s.getProperty("mobile") == null){
			resultsElement = "results";
		}else{
			resultsElement = "mobileresults";
		}
		s.putMsg(resultsElement, "", "loading(true)");
		this.clearResults(s);
		s.setProperty("activeType", type);	
		this.setHistoryParameter(s, "activeType", type);
		this.resetCounters(s);
		this.setResultAmountOnClient(s);
		s.putMsg("filter", "", "setCounts(" + this.getCounterObject(true, s) + ")");
		sendChunkToClient(s);
		s.putMsg(resultsElement, "", "loading(false)");
	};
	
	public void getNextChunk(Screen s){
		HashMap<String, Integer> types = (HashMap<String, Integer>) s.getProperty("typesChunks");
		String type = (String) s.getProperty("activeType");
		
		types.put(type, types.get(type) + 1);
		sendChunkToClient(s);
	};
	
	private void sendChunkToClient(Screen s){
		JSONObject chunk = new JSONObject();
		String activeType = (String) s.getProperty("activeType");
		JSONArray values = new JSONArray();
		chunk.put(activeType, values);
		JSONObject results = (JSONObject) s.getProperty("results");
		JSONArray resultsForType = (JSONArray) results.get(activeType);
		int itemsPerChunk = (Integer) s.getProperty("chunkSize");
		
		HashMap<String, Integer> chunksForType = (HashMap<String, Integer>) s.getProperty("typesChunks");
		int currentChunk = chunksForType.get(activeType);
				
		int start = (currentChunk - 1) * itemsPerChunk;
		int end = start + itemsPerChunk;
		
		String resultsComp;
		
		if(s.getProperty("mobile") != null){
			resultsComp = "mobileresults";
		}else{
			resultsComp = "results";
		}
		
		if((start + end) >= resultsForType.size()){
			end = resultsForType.size();
			s.putMsg(resultsComp, "", "hideLoadMore()");
		}else{
			s.putMsg(resultsComp, "", "showLoadMore()");
		}
		
		values.addAll(0, resultsForType.subList(start, end));
		
		String command = "setResults(" + chunk + ")";
		
		if(s.getProperty("mobile") != null){
			s.putMsg("mobileresults", "", command);
		}else{
			s.putMsg("results", "", command);
		}
	}
	
	public void setDefaultSorting(Screen s){
		s.setProperty("sortDirection", "up");
		s.setProperty("sortField", FieldMappings.getSystemFieldName("sort_title"));
	}
	
	public void setSorting(Screen s, String data){
		JSONObject message = (JSONObject) JSONValue.parse(data);
		String sortField = FieldMappings.getSystemFieldName((String) message.get("field"));
		s.setProperty("sortField", sortField);
		this.setHistoryParameter(s, "sortField", sortField);
		search(s);
	}
	
	public void setSortDirection(Screen s, String direction){
		s.setProperty("sortDirection", direction);
		this.setHistoryParameter(s, "sortDirection", direction);
		search(s);
	}
	
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
	
	private HashMap<String, HashMap<String, FilterCondition>> createCategoryCountsConditions(){
		HashMap<String, HashMap<String, FilterCondition>> categorisedConditionsToCount = new HashMap<String, HashMap<String, FilterCondition>>();
		ArrayList<FilterCondition> conditions = new ArrayList<FilterCondition>();
		
		List<FsNode> nodes = allNodes.getNodes();
		
		if(!this.inDevelMode()){
			Filter filter = new Filter();
			EqualsCondition condition = new EqualsCondition("public", "true");
			filter.addCondition(condition);
			nodes = filter.apply(nodes);
		}
		
		for(Iterator<FsNode> iter = nodes.iterator() ; iter.hasNext(); ) {
			// get the next node
			FsNode n = (FsNode)iter.next();	
			
			for(Iterator<String> categoriesIterator = this.availableConditionFieldCategories.iterator(); categoriesIterator.hasNext();){
				String category = categoriesIterator.next();
				String value = n.getProperty(FieldMappings.getSystemFieldName(category));
				if(value != null){
					HashMap<String, FilterCondition> catEntry = categorisedConditionsToCount.get(category);
					if(catEntry == null){
						catEntry = new HashMap<String, FilterCondition>();
						categorisedConditionsToCount.put(category, catEntry);
					}
					
					if(catEntry.get(value) == null && !value.contains(",")){
						FilterCondition condition = new EqualsCondition(FieldMappings.getSystemFieldName(category), value, ",");
						catEntry.put(value, condition);
						conditions.add(condition);
					}
				}
			}
		}
		
		int c = 0;
		HashMap<String, FilterCondition> decadeEntry = new HashMap<String, FilterCondition>();
		categorisedConditionsToCount.put("decade", decadeEntry);
		for(Iterator<Integer> i = this.decades.iterator(); i.hasNext();){
			int decade = i.next();
			
			FilterCondition condition = new TimeRangeCondition(decade, decade + 10, FieldMappings.getSystemFieldName("year"));
			decadeEntry.put("" + decade, condition);
			conditions.add(condition);
		}
		
		return categorisedConditionsToCount;
	};
	
	public void createCounterFilter(Screen s){		
		HashMap<String, HashMap<String, FilterCondition>> categorisedConditionsToCount = createCategoryCountsConditions();
		Filter counterFilter = createCounterFilter(categorisedConditionsToCount);
		
		s.setProperty("counterConditions", categorisedConditionsToCount);
		s.setProperty("counterFilter", counterFilter);
	}
	
	private void resetCounters(Screen s){
		HashMap<String, HashMap<String, FilterCondition>> counters = (HashMap<String, HashMap<String, FilterCondition>>) s.getProperty("counterConditions");
		
		for(Iterator<HashMap<String, FilterCondition>> i = counters.values().iterator(); i.hasNext();){
			for(Iterator<FilterCondition> it = i.next().values().iterator(); it.hasNext();){
				it.next().clearPassed();
			}
		}
	}
	
	private JSONObject getCounterObject(boolean refresh){
		List<FsNode> nodes = this.allNodes.getNodes();
		return getCounterObject(refresh, nodes, "all", this.cachedCategorisedConditions, this.cachedCounterFilter);
	}
	
	private JSONObject getCounterObject(boolean refresh, Screen s){
		List<FsNode> nodes = (List<FsNode>) s.getProperty("rawNodes");
		String type = (String) s.getProperty("activeType");
		HashMap<String, HashMap<String, FilterCondition>> categorisedConditions = (HashMap<String, HashMap<String, FilterCondition>>) s.getProperty("counterConditions");
		Filter counterFilter = (Filter) s.getProperty("counterFilter");
		return getCounterObject(refresh, nodes, type, categorisedConditions, counterFilter);
	}
	
	private JSONObject getCounterObject(boolean refresh, List<FsNode> nodes, String type, HashMap<String, HashMap<String, FilterCondition>> categorisedConditions, Filter counterFilter){
		if(!this.inDevelMode()){
			Filter filter = new Filter();
			filter.addCondition(new EqualsCondition("public", "true"));
			nodes = filter.apply(nodes);
		}
		
		if(!type.equals("all")){
			Filter typeFilter = new Filter();
			typeFilter.addCondition(new TypeCondition(type));
			
			nodes = typeFilter.apply(nodes);
		}
		
		if(refresh){
			counterFilter.run(nodes);
		}
		
		JSONObject countedResults = new JSONObject();
		for(Iterator<String> catIterator = categorisedConditions.keySet().iterator(); catIterator.hasNext();){
			String catName = catIterator.next();
			HashMap<String, FilterCondition> catEntry = categorisedConditions.get(catName);
			JSONObject categoryResults = new JSONObject();
			countedResults.put(catName, categoryResults);
			for(Iterator<String> fieldIterator = catEntry.keySet().iterator(); fieldIterator.hasNext();){
				String fieldName = fieldIterator.next();
				FilterCondition fieldCondition = catEntry.get(fieldName);
				categoryResults.put(fieldName, fieldCondition.getPassed().size());
			}
		}
		
		return countedResults;
	}
	
	/*
	private JSONObject getCounterClient(Screen s){
		List<FsNode> nodesToFilter = (List<FsNode>) s.getProperty("rawNodes");
		String type = (String) s.getProperty("activeType");
		
		List<FsNode> nodes;
		
		if(!type.equals("all")){
			Filter typeFilter = new Filter();
			typeFilter.addCondition(new TypeCondition(type));
			
			nodes = typeFilter.apply(nodesToFilter);
		}else{
			nodes = nodesToFilter;
		}
		
		HashMap<String, HashMap<String, FilterCondition>> counters = (HashMap<String, HashMap<String, FilterCondition>>) s.getProperty("counterConditions");
		Filter counterFilter = (Filter) s.getProperty("counterFilter");
		
		counterFilter.run(nodes);
		
		JSONObject countedResults = new JSONObject();
		
		for(Iterator<String> catIterator = counters.keySet().iterator(); catIterator.hasNext();){
			String catName = catIterator.next();
			HashMap<String, FilterCondition> catEntry = counters.get(catName);
			JSONObject categoryResults = new JSONObject();
			countedResults.put(catName, categoryResults);
			for(Iterator<String> fieldIterator = catEntry.keySet().iterator(); fieldIterator.hasNext();){
				String fieldName = fieldIterator.next();
				FilterCondition fieldCondition = catEntry.get(fieldName);
				categoryResults.put(fieldName, fieldCondition.getPassed().size());
			}
		}
		
		return countedResults;
	}
	*/
	
	private JSONObject createConditionFieldsForClient(Screen s){
		JSONObject fields = new JSONObject();
		
		for(Iterator<String> i = this.availableConditionFieldCategories.iterator(); i.hasNext();){
			String category = i.next();
			if(!category.equals("decade")){
				fields.put(category, createFieldsForCategoryForClient(s, FieldMappings.getSystemFieldName(category)));
			}else{
				fields.put(category, createDecadeFields(s));
			}
		}
				
		return fields;
	}
	
	private JSONArray createFieldsForCategoryForClient(Screen s, String category){
		String uri = "/domain/euscreenxl/user/*/*"; // does this make sense, new way of mapping (daniel)
		FSList fslist = FSListManager.get(uri);
		List<FsNode> nodes = fslist.getNodes();
		
		JSONArray availableOptions = new JSONArray();
		ArrayList<String> options = new ArrayList<String>();
		
		for(Iterator<FsNode> iter = nodes.iterator() ; iter.hasNext(); ) {
			// get the next node
			FsNode n = (FsNode)iter.next();	
			
			String optionsStr = n.getProperty(category);
			
			if(optionsStr != null){		
				String[] optionsArr = optionsStr.split(",");
				
				for(int i = 0; i < optionsArr.length; i++){
					String option = optionsArr[i].trim();
					if(!options.contains(option) && option != null){
						options.add(option);
					}
				}
			}
		}
		
		Collections.sort(options, new Comparator<String>() {
		    public int compare(String a, String b) {
		    	return a.compareTo(b);
		    }
		});
		
		for(Iterator<String> iter = options.iterator(); iter.hasNext();){
			String option = iter.next();
			JSONObject optionObject = new JSONObject();
			optionObject.put("label", option);
			optionObject.put("value", option);
			availableOptions.add(optionObject);
		}
		
		return availableOptions;
	}
	
	private JSONArray createDecadeFields(Screen s){
		JSONArray decades = new JSONArray();
		
		for(Iterator<Integer> i = this.decades.iterator(); i.hasNext();){
			int decade = i.next();
			JSONObject decadeObject = new JSONObject();
			decadeObject.put("label", decade + "s");
			decadeObject.put("value", decade);
			decades.add(decadeObject);
		}
		
		return decades;
	}
	
	public void populateFieldsClient(Screen s){
		JSONObject allFilters = this.createConditionFieldsForClient(s);
		s.setProperty("allFilters", allFilters);
		
		s.putMsg("filter", "", "populateFields(" + allFilters + ")");
	}
	
	public void createFilter(Screen s){
		System.out.println("createFilter()");
		Filter filter = new Filter();
		if(!this.inDevelMode()){
			filter.addCondition(new EqualsCondition("public", "true"));
		}
		s.setProperty("filter", filter);
	}
	
	public void setClientSelectedField(Screen s, String data){
		this.setClientSelectedField(s, data, true);
		JSONObject activeFields = (JSONObject) s.getProperty("clientSelectedFields");
		this.setHistoryParameter(s, "activeFields", activeFields.toJSONString());
	}
	
	/**
	 * Sets a filter on this screen based on the selection by the user.
	 * 
	 * @param s The screen for which the filter was selected
	 * @param data The data containing the userselection. 
	 */
	public void setClientSelectedField(Screen s, String data, boolean refresh){
		System.out.println("EuscreenxlsearchApplication().setClientSelectedField(" + data + ")");
		
		s.putMsg("activefields", "", "loading(true)");
		
		JSONObject activeFields = (JSONObject) s.getProperty("clientSelectedFields");
		if(activeFields == null){
			activeFields = new JSONObject();
			s.setProperty("clientSelectedFields", activeFields);
		}
	
		JSONObject message = (JSONObject) JSONValue.parse(data);
		for(Iterator<String> catIter = message.keySet().iterator(); catIter.hasNext();){
			String category = catIter.next();
			String value;
			try{
				value = (String) message.get(category);
			}catch(ClassCastException cce){
				JSONArray tmp = (JSONArray) message.get(category);
				value = (String) tmp.get(0);
			}
			
			JSONArray fieldsForCategory = (JSONArray) activeFields.get(category);
			
			if(fieldsForCategory == null){
				fieldsForCategory = new JSONArray();
				activeFields.put(category, fieldsForCategory);
			}
			
			fieldsForCategory.add(value);
			
			JSONObject deactivateMessage = new JSONObject();
			deactivateMessage.put("category", category);
			s.putMsg("filter", "", "deactivateCategory(" + deactivateMessage + ")");
		}
		
		s.putMsg("activefields", "", "setActiveFields(" + activeFields + ")");
		
		createFilterFromClientSelectionForScreen(s);
		
		//Execute the search again in order update the results based on the new filter. 
		if(refresh)
			this.search(s);
		
		s.putMsg("activefields", "", "loading(false)");
	}
	
	/**
	 * Removes a filter on this screen based on the selection by the user. 
	 * 
	 * @param s The screen for which the filter was removed
	 * @param data The data containing information as to what filter was removed. 
	 */
	public void removeClientSelectedField(Screen s, String data){
		s.putMsg("activefields", "", "loading(true)");
		
		JSONObject message = (JSONObject) JSONValue.parse(data);
		JSONObject activeFields = (JSONObject) s.getProperty("clientSelectedFields");
		
		String category = (String) message.keySet().iterator().next();
		String field = (String) message.values().iterator().next();
		
		JSONArray activeForCategory = (JSONArray) activeFields.get(category);
		activeForCategory.remove(activeForCategory.indexOf(field));
		if(activeForCategory.isEmpty()){
			activeFields.remove(category);
		}
		
		JSONObject activateMessage = new JSONObject();
		activateMessage.put("category", category);
		s.putMsg("filter", "", "activateCategory(" + activateMessage + ")");
		createFilterFromClientSelectionForScreen(s);
		this.search(s);
		
		this.setHistoryParameter(s, "activeFields", activeFields.toJSONString());
		
		s.putMsg("activefields", "", "loading(false)");
	}
	
	/**
	 * Used to create a Filter object from the clientSelectedFilters variable saved on the Screen object.
	 * 
	 * @param s The screen object on which the client selection is set. 
	 */
	private void createFilterFromClientSelectionForScreen(Screen s){
		JSONObject activeFields = (JSONObject) s.getProperty("clientSelectedFields");
		ArrayList<FilterCondition> conditions = new ArrayList<FilterCondition>();
		AndCondition andCondition = new AndCondition();
				
		for(Iterator<String> iter = activeFields.keySet().iterator(); iter.hasNext();){
			String key = iter.next();
			if(!key.equals("decade")){
				ArrayList<String> allowedValues = (ArrayList) activeFields.get(key);
				ArrayList<FilterCondition> equalsConditions = new ArrayList<FilterCondition>();
				equalsConditions = createEqualsConditionsForField(FieldMappings.getSystemFieldName(key), allowedValues);
				
				OrCondition orCondition = new OrCondition(equalsConditions);
				andCondition.add(orCondition);
			}else{
				ArrayList<String> allowedValues = (ArrayList) activeFields.get(key);
				ArrayList<FilterCondition> decadeConditions = new ArrayList<FilterCondition>();
				decadeConditions = createDecadeConditions(FieldMappings.getSystemFieldName("year"), allowedValues);
				
				OrCondition orCondition = new OrCondition(decadeConditions);
				andCondition.add(orCondition);
			}
		}
		
		if(!this.inDevelMode()){
			andCondition.add(new EqualsCondition("public", "true"));
		}
		
		conditions.add(andCondition);
		
		Filter filter = new Filter(conditions);
		
		
		s.setProperty("filter", filter);
	}
	
	private ArrayList<FilterCondition> createEqualsConditionsForField(String field, ArrayList<String> allowedValues){
		ArrayList<FilterCondition> conditions = new ArrayList<FilterCondition>();
		for(Iterator<String> i = allowedValues.iterator(); i.hasNext();){
			conditions.add(new EqualsCondition(field, i.next(), ","));
		}
		return conditions;
	}
	
	private ArrayList<FilterCondition> createDecadeConditions(String field, ArrayList<String> allowedValues){
		ArrayList<FilterCondition> conditions = new ArrayList<FilterCondition>();
		for(Iterator<String> i = allowedValues.iterator(); i.hasNext();){
			String startStr = i.next();
			int startYear = Integer.parseInt(startStr);
			int stopYear = startYear + 10;
			conditions.add(new TimeRangeCondition(startYear, stopYear, field));
		}
		return conditions;
	}

}
