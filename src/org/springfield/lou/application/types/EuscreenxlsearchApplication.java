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

import java.io.BufferedReader;
import java.io.File;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.Namespace;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

import java.util.Map.Entry;

import org.springfield.lou.application.Html5Application;
import org.springfield.lou.application.Html5ApplicationInterface;
import org.springfield.lou.application.components.BasicComponent;
import org.springfield.lou.application.components.ComponentInterface;
import org.springfield.lou.application.types.conditions.*;
import org.springfield.lou.fs.*;
import org.springfield.lou.homer.LazyHomer;
import org.springfield.lou.screen.Screen;


public class EuscreenxlsearchApplication extends Html5Application{
	
	private FSList allNodes;
	private ArrayList<String> availableFilterCategories;
	
	/*
	 * Constructor for the preview application for EUScreen providers
	 * so they can check and debug their uploaded collections.
	 */
	public EuscreenxlsearchApplication(String id) {
		super(id); 
		
		// allways 'loads' the full result set with all the items from the manager
		String uri = "/domain/euscreenxl/user/*/*"; // does this make sense, new way of mapping (daniel)
		allNodes = FSListManager.get(uri);
		
		this.availableFilterCategories = new ArrayList<String>();
		this.availableFilterCategories.add("language");
		this.availableFilterCategories.add("decade");
		this.availableFilterCategories.add("topic");
		this.availableFilterCategories.add("provider");
		this.availableFilterCategories.add("publisher");
		this.availableFilterCategories.add("genre");
		this.availableFilterCategories.add("country");
				
		// default scoop is each screen is its own location, so no multiscreen effects
		setLocationScope("screen"); 
		this.addReferid("header", "/euscreenxlelements/header");
		this.addReferid("footer", "/euscreenxlelements/footer");
	}
	
	public void setSearchQuery(Screen s, String data){
		System.out.println("setSearchQuery(" + data + ")");
		JSONObject queryData = (JSONObject) JSONValue.parse(data);
		
		String query = (String) queryData.get("query");
		query = query.toLowerCase();
		if(query.equals("")){
			s.setProperty("searchQuery", "*");
		}else{
			s.setProperty("searchQuery", query);
		}
		
		s.setProperty("sortDirection", "up");
		s.setProperty("sortField", "id");
		s.setProperty("maxDisplay", 20);
		search(s);
	}
	
	public void search(Screen s){
		System.out.println("search()");
		
		// lets get the nodes from the fslist, depending on input we get them all or
		// filtered and sorted
		List<FsNode> nodes = null;
		
		this.resetCounters(s);
		
		String query = (String) s.getProperty("searchQuery");
		if(query == null){
			query = "*";
		}
		
		String sortDirection = (String) s.getProperty("sortDirection");
		String sortField = (String) s.getProperty("sortField");
		Integer maxDisplay = (Integer) s.getProperty("maxDisplay");
		
		try{
			if (query.equals("*")) { 
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
		
		Filter filter = (Filter) s.getProperty("filter");
		nodes = filter.apply(nodes);
				
		s.setProperty("results", nodes);
		
		JSONArray results = new JSONArray();
		
		for(Iterator<FsNode> iter = nodes.iterator() ; iter.hasNext(); ) {
			// get the next node
			FsNode n = (FsNode)iter.next();	
			
			JSONObject object = new JSONObject();
			object.put("type", n.getName());
			object.put("screenshot", n.getProperty(FieldMappings.getSystemFieldName("screenshot")));
			object.put("title", n.getProperty(FieldMappings.getSystemFieldName("title")));
			object.put("originalTitle", n.getProperty(FieldMappings.getSystemFieldName("originalTitle")));
			object.put("provider", n.getProperty(FieldMappings.getSystemFieldName("provider")));
			object.put("year", n.getProperty(FieldMappings.getSystemFieldName("year")));
			object.put("language", n.getProperty(FieldMappings.getSystemFieldName("language")));
			object.put("duration", n.getProperty(FieldMappings.getSystemFieldName("duration")));
			
			results.add(object);
		}
		this.componentmanager.getComponent("results").put("", "setResults(" + results + ")");
		this.componentmanager.getComponent("searcher").put("", "setCounter(" + nodes.size() + ")");	
		this.componentmanager.getComponent("filters").put("", "setCounts(" + this.getCounterClient(s, nodes) + ")");
	}
	
	public void createCounterFilter(Screen s){
		System.out.println("createCounterFilter()");
		
		HashMap<String, HashMap<String, EqualsCondition>> categorisedConditionsToCount = new HashMap<String, HashMap<String, EqualsCondition>>();
		ArrayList<FilterCondition> conditions = new ArrayList<FilterCondition>();
		
		for(Iterator<FsNode> iter = allNodes.getNodes().iterator() ; iter.hasNext(); ) {
			// get the next node
			FsNode n = (FsNode)iter.next();	
			
			for(Iterator<String> categoriesIterator = this.availableFilterCategories.iterator(); categoriesIterator.hasNext();){
				String category = categoriesIterator.next();
				String value = n.getProperty(FieldMappings.getSystemFieldName(category));
				if(value != null){
					HashMap<String, EqualsCondition> catEntry = categorisedConditionsToCount.get(category);
					if(catEntry == null){
						catEntry = new HashMap<String, EqualsCondition>();
						categorisedConditionsToCount.put(category, catEntry);
					}
					
					if(catEntry.get(value) == null && !value.contains(",")){
						EqualsCondition condition = new EqualsCondition(FieldMappings.getSystemFieldName(category), value, ",");
						catEntry.put(value, condition);
						conditions.add(condition);
					}
				}
			}
		}
		
		Filter counterFilter = new Filter(conditions);
		
		s.setProperty("counterConditions", categorisedConditionsToCount);
		s.setProperty("counterFilter", counterFilter);
	}
	
	private void resetCounters(Screen s){
		HashMap<String, HashMap<String, EqualsCondition>> counters = (HashMap<String, HashMap<String, EqualsCondition>>) s.getProperty("counterConditions");
		
		for(Iterator<HashMap<String, EqualsCondition>> i = counters.values().iterator(); i.hasNext();){
			for(Iterator<EqualsCondition> it = i.next().values().iterator(); it.hasNext();){
				it.next().getCounter().reset();
			}
		}
	}
	
	private JSONObject getCounterClient(Screen s, List<FsNode> nodes){
		HashMap<String, HashMap<String, EqualsCondition>> counters = (HashMap<String, HashMap<String, EqualsCondition>>) s.getProperty("counterConditions");
		Filter counterFilter = (Filter) s.getProperty("counterFilter");
		counterFilter.run(nodes);
		
		JSONObject countedResults = new JSONObject();
		
		for(Iterator<String> catIterator = counters.keySet().iterator(); catIterator.hasNext();){
			String catName = catIterator.next();
			HashMap<String, EqualsCondition> catEntry = counters.get(catName);
			JSONObject categoryResults = new JSONObject();
			countedResults.put(catName, categoryResults);
			for(Iterator<String> fieldIterator = catEntry.keySet().iterator(); fieldIterator.hasNext();){
				String fieldName = fieldIterator.next();
				EqualsCondition fieldCondition = catEntry.get(fieldName);
				categoryResults.put(fieldName, fieldCondition.getCounter().getTicks());
			}
		}
		
		return countedResults;
	}
	
	private JSONObject createFiltersForClient(Screen s){
		System.out.println("createFilters()");
		JSONObject filters = new JSONObject();
		
		for(Iterator<String> i = this.availableFilterCategories.iterator(); i.hasNext();){
			String category = i.next();
			if(!category.equals("decade")){
				filters.put(category, createFilterOptionsArray(s, FieldMappings.getSystemFieldName(category)));
			}else{
				filters.put(category, createDecades(s));
			}
		}
				
		return filters;
	}
	
	private JSONArray createFilterOptionsArray(Screen s, String field){
		List<FsNode> nodes;
		
		if(s.getProperty("results") == null){
			String uri = "/domain/euscreenxl/user/*/*"; // does this make sense, new way of mapping (daniel)
			FSList fslist = FSListManager.get(uri);
			nodes = fslist.getNodes();
		}else{
			nodes = (List<FsNode>) s.getProperty("results");
		}
		
		JSONArray availableOptions = new JSONArray();
		ArrayList<String> options = new ArrayList<String>();
		
		for(Iterator<FsNode> iter = nodes.iterator() ; iter.hasNext(); ) {
			// get the next node
			FsNode n = (FsNode)iter.next();	
			
			String optionsStr = n.getProperty(field);
			
			if(optionsStr != null){		
				String[] optionsArr = optionsStr.split(",");
				
				for(int i = 0; i < optionsArr.length; i++){
					String option = optionsArr[i];
					
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
	
	private JSONArray createDecades(Screen s){
		JSONArray decades = new JSONArray();
		
		int startDecade = 1900;
		int endDecade = 2010;
		
		for(int decade = startDecade; decade <= endDecade; decade += 10){
			JSONObject decadeObject = new JSONObject();
			decadeObject.put("label", decade + "\'s");
			decadeObject.put("value", decade);
			decades.add(decadeObject);
		}
		
		return decades;
	}
	
	public void populateFiltersClient(Screen s){
		System.out.println("populateFiltersClient()");
		JSONObject allFilters = this.createFiltersForClient(s);
		s.setProperty("allFilters", allFilters);
				
		this.componentmanager.getComponent("filters").put("", "populateFilters(" + allFilters + ")");
	}
	
	public void createFilter(Screen s){
		s.setProperty("filter", new Filter());
	}
	
	public void setFilter(Screen s, String data){
		System.out.println("setFilter()");
		JSONObject activeFilters = (JSONObject) s.getProperty("activeFilters");
		if(activeFilters == null)
			activeFilters = new JSONObject();

		JSONObject filter = (JSONObject) JSONValue.parse(data);
		String key = (String) filter.keySet().iterator().next();
		JSONArray value = (JSONArray) filter.values().iterator().next();
		activeFilters.put(key, value);
		s.setProperty("activeFilters", activeFilters);
		this.componentmanager.getComponent("activefilters").put("", "setActiveFilters(" + activeFilters + ")");
		createFilterFromSettingsForScreen(s);
		this.search(s);
	}
	
	public void removeFilter(Screen s, String data){
		JSONObject filterToRemove = (JSONObject) JSONValue.parse(data);
		JSONObject activeFilters = (JSONObject) s.getProperty("activeFilters");
		
		String type = (String) filterToRemove.keySet().iterator().next();
		String toRemove = (String) filterToRemove.values().iterator().next();
		
		JSONArray activeForType = (JSONArray) activeFilters.get(type);
		activeForType.remove(activeForType.indexOf(toRemove));
		if(activeForType.isEmpty()){
			activeFilters.remove(type);
		}
		createFilterFromSettingsForScreen(s);
		this.search(s);
	}
	
	private ArrayList<FilterCondition> createEqualsConditionsForField(String field, ArrayList<String> allowedValues){
		ArrayList<FilterCondition> conditions = new ArrayList<FilterCondition>();
		for(Iterator<String> i = allowedValues.iterator(); i.hasNext();){
			conditions.add(new EqualsCondition(field, i.next(), ","));
		}
		return conditions;
	}
	
	private void createFilterFromSettingsForScreen(Screen s){
		JSONObject activeFilters = (JSONObject) s.getProperty("activeFilters");
		ArrayList<FilterCondition> conditions = new ArrayList<FilterCondition>();
		AndCondition andCondition = new AndCondition();
		
		for(Iterator<String> iter = activeFilters.keySet().iterator(); iter.hasNext();){
			String key = iter.next();
			ArrayList<String> allowedValues = (ArrayList) activeFilters.get(key);
			ArrayList<FilterCondition> equalsConditions = new ArrayList<FilterCondition>();
			equalsConditions = createEqualsConditionsForField(FieldMappings.getSystemFieldName(key), allowedValues);
			
			OrCondition orCondition = new OrCondition(equalsConditions);
			andCondition.add(orCondition);
		}
		
		conditions.add(andCondition);
		
		s.setProperty("filter", new Filter(conditions));
	}

}
