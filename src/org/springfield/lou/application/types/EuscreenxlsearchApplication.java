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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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
import org.springfield.lou.application.types.conditions.NotCondition;
import org.springfield.lou.application.types.conditions.OrCondition;
import org.springfield.lou.application.types.conditions.TimeRangeCondition;
import org.springfield.lou.application.types.conditions.TypeCondition;
import org.springfield.lou.homer.LazyHomer;
import org.springfield.lou.screen.Screen;


public class EuscreenxlsearchApplication extends Html5Application implements SearcherResultsHandler{
	
	/*
	 * Cached copy of all the nodes. Not sure if this is really neccesary. 
	 */
	private FSList allNodes;
	private JSONObject countriesForProviders;
	
	/*
	 * Arraylist containing all the categories of fields. This will be used to categorize the conditions, and to fill 
	 * the select boxes
	 */
	private ArrayList<String> availableConditionFieldCategories;
	private ArrayList<Integer> decades;
	private JSONObject cachedCounts = null;
	private boolean wantedna = true;
		
	/*
	 * Constructor for the preview application for EUScreen providers
	 * so they can check and debug their uploaded collections.
	 */
	public EuscreenxlsearchApplication(String id) {
		super(id); 
		
		this.countriesForProviders = new JSONObject();
		
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
				
		List<FsNode> nodes = allNodes.getNodes();
		
		Filter filter = new Filter();
		AndCondition andCondition = new AndCondition();
		
		if(!this.inDevelMode()){ // Production mode
			EqualsCondition condition = new EqualsCondition("public", "true");
			
			andCondition.add(condition);
		}
		
		NotCondition nCondition = new NotCondition("provider", "AGENCY");
		andCondition.add(nCondition);
		
		filter.addCondition(andCondition);
		nodes = filter.apply(nodes);
						
		// default scoop is each screen is its own location, so no multiscreen effects
		setLocationScope("screen"); 
		
		//refer the header and footer elements from euscreenxl element application. 
		this.addReferid("mobilenav", "/euscreenxlelements/mobilenav");
		this.addReferid("header", "/euscreenxlelements/header");
		this.addReferid("footer", "/euscreenxlelements/footer");
		this.addReferid("linkinterceptor", "/euscreenxlelements/linkinterceptor");
		this.addReferid("headerhider", "/euscreenxlelements/headerhider");
		this.addReferid("history", "/euscreenxlelements/history");
		
		this.addReferidCSS("fontawesome", "/euscreenxlelements/fontawesome");
		this.addReferidCSS("bootstrap", "/euscreenxlelements/bootstrap");
		this.addReferidCSS("theme", "/euscreenxlelements/theme");
		this.addReferidCSS("genericadditions", "/euscreenxlelements/generic");
		this.addReferidCSS("all", "/euscreenxlelements/all");
		this.addReferidCSS("terms", "/euscreenxlelements/terms");
	}
	
	public void init(Screen s){
		if(!this.inDevelMode()){
			s.putMsg("linkinterceptor", "", "interceptLinks()");
		}
		
		if(s.getProperty("mobile") != null){
			s.setProperty("resultsElement", "mobileresults");
		}else if(s.getProperty("tablet") != null){
			s.setProperty("resultsElement", "tabletresults");
		}else{
			s.setProperty("resultsElement", "results");
		}
	}
	
	public String getFavicon() {
        return "/eddie/apps/euscreenxlelements/img/favicon.png";
    }
	
	private boolean inDevelMode() {
    	return LazyHomer.inDeveloperMode();
    }
	
	public void setInitialCounts(Screen s){
		if(this.cachedCounts != null){
			s.putMsg("filter", "", "setCounts(" + cachedCounts + ")");
		}
		
	}
	
	public void setTablet(Screen s){
		s.setProperty("tablet", true);
	}
	
	public void setMobile(Screen s){
		s.setProperty("mobile", true);
	}
	
	public void parseURLParams(Screen s){
		JSONObject startupParameters = new JSONObject();
		boolean search = false;
		if(s.getParameter("query") != null){
			search = true;
			String query = (String) s.getParameter("query");
			s.setProperty("searchQuery", query);
			if(s.getProperty("mobile") != null){
				s.putMsg("mobilesearchinput", "", "setQuery(" + query + ")");
			}else{
				s.putMsg("searchinput", "", "setQuery(" + query + ")");
			}
			startupParameters.put("query", query);
		}
		
		if(s.getParameter("sortField") != null){
			String sortField = (String) s.getParameter("sortField");
			s.setProperty("sortField", sortField);
			startupParameters.put("sortField", sortField);
			
			String body = "<span class='colorgray'>SORT BY</span> "+FieldMappings.getInterfaceFieldName(sortField)+" <span class='caret'></span>";
			setContentOnScope(s,"sortHeader",body);
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
			search = true;
			try {
				String encodedString = (String) s.getParameter("activeFields");
				startupParameters.put("activeFields", encodedString);
				String activeFields = org.springfield.fs.FsEncoding.decode(java.net.URLDecoder.decode(encodedString, "UTF-8"));
				this.setClientSelectedField(s, activeFields, false);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		s.putMsg("history", "", "setStartupParameters(" + startupParameters + ")");
		if(search)
			this.search(s);
	};
	
	public void setSearchQuery(Screen s, String data){
		this.setSearchQuery(s, data, true);
	}
	
	public void clearFields(Screen s){
		JSONObject emptyFields = new JSONObject();
		s.setProperty("clientSelectedFields", emptyFields);
		this.createFilterFromClientSelectionForScreen(s);
		this.search(s);
		s.putMsg("activefields", "", "setActiveFields(" + emptyFields + ")");
		this.setHistoryParameter(s, "activeFields", emptyFields.toJSONString());
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
		String resultsElement = (String) s.getProperty("resultsElement");
		
		if(s.getProperty("searchQueue") == null){
			s.setProperty("searchQueue", Executors.newSingleThreadExecutor());
		}
		
		ExecutorService searchQueue = (ExecutorService) s.getProperty("searchQueue");
		
		this.clearResults(s);
		s.putMsg(resultsElement, "", "loading(true)");
		
		// lets get the nodes from the fslist, depending on input we get them all or
		// filtered and sorted
		List<FsNode> nodes = null;
		
		//reset the counters before doing the search.
		this.resetCounters(s);
		
		//Get the search parameter from the Screen object
		String query = (String) s.getProperty("searchQuery");
		JSONObject activeFields = (JSONObject) s.getProperty("clientSelectedFields");
				
		if(query == null && activeFields.size() > 0){
			query = "*";
		}
		
		if(query != null && query.trim().length() > 0){
			String sortDirection = (String) s.getProperty("sortDirection");
			String sortField = (String) s.getProperty("sortField");
			String activeType = (String) s.getProperty("activeType");
			Filter filter = (Filter) s.getProperty("filter");
			HashMap<String, HashMap<String, FilterCondition>> counterConditions = (HashMap<String, HashMap<String, FilterCondition>>) s.getProperty("counterConditions");
					
			Searcher searcher = new Searcher(this, s, this.allNodes, query, activeType, sortDirection, sortField, filter, counterConditions, this.inDevelMode());
			searchQueue.execute(searcher);
			s.setProperty("searcher", searcher);
			s.putMsg("filter", "", "loading(true)");
		}else{
			s.putMsg("filter", "", "deactivate()");
			s.putMsg(resultsElement, "", "startScreen()");
			s.putMsg("resultcounter", "", "setAmount()");
			s.setProperty("results", null);
			this.renderTabs(s);
		}
	}
	
	private void setHistoryParameter(Screen s, String key, String value){
		JSONObject historyObject = new JSONObject();
		historyObject.put(key, value);
		s.putMsg("history", "", "setParameter(" + historyObject + ")");
	}
	
	private void renderTabs(Screen s){
		JSONObject message = new JSONObject();
		JSONObject results = (JSONObject) s.getProperty("results");
		if(results != null){
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
			
		}else{
			message.put("all", true);
			message.put("video", false);
			message.put("audio", false);
			message.put("picture", false);
			message.put("series", false);
			message.put("doc", false);
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
		String resultsElement = (String) s.getProperty("resultsElement");
		s.putMsg(resultsElement, "", "clear()");
	}
	
	public void createTypeChunking(Screen s){
		HashMap<String, Integer> types = new HashMap<String, Integer>();
	 
		types.put("all", 1);
		types.put("video", 1);
		types.put("picture", 1);
		types.put("doc", 1);
		types.put("audio", 1);
		types.put("series", 1);
		
		s.setProperty("chunkSize", 10);
		s.setProperty("typesChunks", types);
	}
	
	public void setActiveType(Screen s, String type){
		Searcher searcher = (Searcher) s.getProperty("searcher");
		String resultsElement = (String) s.getProperty("resultsElement");
		s.putMsg(resultsElement, "", "loading(true)");
		this.clearResults(s);
		s.setProperty("activeType", type);	
		this.setHistoryParameter(s, "activeType", type);
		this.resetCounters(s);
		this.setResultAmountOnClient(s);
		HashMap<String, Integer> types = (HashMap<String, Integer>) s.getProperty("typesChunks");
		types.put(type, 1);
		sendChunkToClient(s);
		s.putMsg(resultsElement, "", "loading(false)");
		searcher.setType(type);
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
		
		String resultsElement = (String) s.getProperty("resultsElement");
		
		if((start + end) >= resultsForType.size()){
			end = resultsForType.size();
			s.putMsg(resultsElement, "", "hideLoadMore()");
		}else{
			s.putMsg(resultsElement, "", "showLoadMore()");
		}
		
		values.addAll(0, resultsForType.subList(start, end));
		
		s.putMsg(resultsElement, "", "setResults(" + chunk + ")");
	}
	
	public void setDefaultSorting(Screen s){
		s.setProperty("sortDirection", "up");
		s.setProperty("sortField", FieldMappings.getSystemFieldName("sort_title"));
	}
	
	public void setSorting(Screen s, String data){
		String resultsElement = (String) s.getProperty("resultsElement");
		s.putMsg(resultsElement, "", "hideLoadMore()");
		JSONObject message = (JSONObject) JSONValue.parse(data);
		String sortField = FieldMappings.getSystemFieldName((String) message.get("field"));
		s.setProperty("sortField", sortField);
		this.setHistoryParameter(s, "sortField", sortField);
		
		String body = "<span class='colorgray'>SORT BY</span> "+(String) message.get("value")+" <span class='caret'></span>";
		setContentOnScope(s,"sortHeader",body);
		search(s);
	}
	
	public void setSortDirection(Screen s, String direction){
		String resultsElement = (String) s.getProperty("resultsElement");
		s.putMsg(resultsElement, "", "hideLoadMore()");
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
					}else if(category.equals("topic") || category.equals("language")){
						FilterCondition condition = new EqualsCondition(FieldMappings.getSystemFieldName(category), value);
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
				
				if(!FieldMappings.getReadable(category).equals("topic") && !FieldMappings.getReadable(category).equals("language")){

					String[] optionsArr = optionsStr.split(",");
					
					for(int i = 0; i < optionsArr.length; i++){
						String option = optionsArr[i].trim();
						if(!options.contains(option) && option != null){
							options.add(option);
						}
					}
				}else{
					if(!options.contains(optionsStr)){
						options.add(optionsStr);
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
		s.putMsg("filter", "", "setCountries(" + this.countriesForProviders + ")");
		s.putMsg("filter", "", "populateFields(" + allFilters + ")");
	}
	
	public void createFilter(Screen s){
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
			if(!key.equals("decade") && !key.equals("topic") && !key.equals("language")){
				ArrayList<String> allowedValues = (ArrayList) activeFields.get(key);
				ArrayList<FilterCondition> equalsConditions = new ArrayList<FilterCondition>();
				equalsConditions = createEqualsConditionsForField(FieldMappings.getSystemFieldName(key), allowedValues);
				
				OrCondition orCondition = new OrCondition(equalsConditions);
				andCondition.add(orCondition);
			}else if(key.equals("decade")){
				ArrayList<String> allowedValues = (ArrayList) activeFields.get(key);
				ArrayList<FilterCondition> decadeConditions = new ArrayList<FilterCondition>();
				decadeConditions = createDecadeConditions(FieldMappings.getSystemFieldName("year"), allowedValues);
				
				OrCondition orCondition = new OrCondition(decadeConditions);
				andCondition.add(orCondition);
			}else if(key.equals("topic") || key.equals("language")){
				ArrayList<String> allowedValues = (ArrayList) activeFields.get(key);
				ArrayList<FilterCondition> decadeConditions = new ArrayList<FilterCondition>();
				decadeConditions = createNotCommaSeperatedConditions(FieldMappings.getSystemFieldName(key), allowedValues);
				
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
	
	private ArrayList<FilterCondition> createNotCommaSeperatedConditions(String field, ArrayList<String> allowedValues){
		ArrayList<FilterCondition> conditions = new ArrayList<FilterCondition>();
		for(Iterator<String> i = allowedValues.iterator(); i.hasNext();){
			String value = i.next();
			conditions.add(new EqualsCondition(field, value));
		}
		return conditions;
	}

	@Override
	public void handleResults(Searcher searcher, Screen s, JSONObject results) {
		// TODO Auto-generated method stub
		String resultsElement = (String) s.getProperty("resultsElement");
		s.setProperty("results", results);
		
		if(s.getProperty("mobile") == null){
			renderTabs(s);
		}
		
		s.putMsg("resulttopbar", "", "show()");
		this.setResultAmountOnClient(s);
		
		this.createTypeChunking(s);
		this.sendChunkToClient(s);
		s.putMsg(resultsElement, "", "loading(false)");
	}

	@Override
	public void handleCounts(Screen s, JSONObject counts) {
		// TODO Auto-generated method stub
		JSONObject activeFields = (JSONObject) s.getProperty("clientSelectedFields");
		//If there are still multiple counts for a category which is already active, make it inactive
		for(Iterator<String> i = counts.keySet().iterator(); i.hasNext();){
			String category = i.next();
			if(activeFields != null && activeFields.containsKey(category)){
				counts.put(category, new JSONArray());
			}
		}
		s.putMsg("filter", "", "setCounts(" + counts + ")");
		s.putMsg("filter", "", "loading(false)");
	}

	@Override
	public void handleResults(Searcher searcher, JSONObject results) {
		// TODO Auto-generated method stub
		this.countriesForProviders = searcher.getCountriesForProviders();
	}

	@Override
	public void handleCounts(JSONObject counts) {
		// TODO Auto-generated method stub
		this.cachedCounts = counts;
	}

}
