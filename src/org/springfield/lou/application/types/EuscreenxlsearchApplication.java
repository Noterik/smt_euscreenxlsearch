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
	
	/*
	 * Cached copy of all the nodes. Not sure if this is really neccesary. 
	 */
	private FSList allNodes;
	
	/*
	 * Arraylist containing all the categories of fields. This will be used to categorize the conditions, and to fill 
	 * the select boxes
	 */
	private ArrayList<String> availableConditionFieldCategories;
	
	/*
	 * Constructor for the preview application for EUScreen providers
	 * so they can check and debug their uploaded collections.
	 */
	public EuscreenxlsearchApplication(String id) {
		super(id); 
		
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
		
				
		// default scoop is each screen is its own location, so no multiscreen effects
		setLocationScope("screen"); 
		
		//refer the header and footer elements from euscreenxl element application. 
		this.addReferid("header", "/euscreenxlelements/header");
		this.addReferid("footer", "/euscreenxlelements/footer");
	}
	
	/**
	 * Sets the search query on the screen, this will be used to search through the nodes. 
	 * 
	 * @param s The screen for which to set the search query.
	 * @param data The JSONObject containing the search query. 
	 */
	public void setSearchQuery(Screen s, String data){
		System.out.println("setSearchQuery(" + data + ")");
		JSONObject queryData = (JSONObject) JSONValue.parse(data);
		
		String query = (String) queryData.get("query");
		query = query.toLowerCase();
		
		//If the query is empty, set the query to * to return all
		if(query.equals("")){
			s.setProperty("searchQuery", "*");
		}else{
			s.setProperty("searchQuery", query);
		}
		
		s.setProperty("maxDisplay", 20);
		search(s);
	}
	
	/**
	 * Executes the search for the given screen. 
	 * 
	 * @param s The screen for which to execute the search
	 */
	public void search(Screen s){
		System.out.println("search()");
		
		// lets get the nodes from the fslist, depending on input we get them all or
		// filtered and sorted
		List<FsNode> nodes = null;
		
		//reset the counters before doing the search.
		this.resetCounters(s);
		
		//Get the search parameter from the Screen object
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
		
		//Get the filter from the screen object, this filter is created from the selection in the selectboxes on the page. 
		Filter filter = (Filter) s.getProperty("filter");
		nodes = filter.apply(nodes);
						
		s.setProperty("results", nodes);
		
		JSONObject results = createResultSet(nodes);
		
		s.putMsg("resultcounter", "", "setAmount(" + nodes.size() + ")");
		s.putMsg("resulttopbar", "", "show()");
		s.putMsg("results", "", "setResults(" + results + ")");
		s.putMsg("filter", "", "setCounts(" + this.getCounterClient(s, nodes) + ")");
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
				
				JSONObject result = new JSONObject();
				result.put("type", node.getName());
				result.put("screenshot", node.getProperty(FieldMappings.getSystemFieldName("screenshot")));
				result.put("title", node.getProperty(FieldMappings.getSystemFieldName("title")));
				result.put("originalTitle", node.getProperty(FieldMappings.getSystemFieldName("originalTitle")));
				result.put("provider", node.getProperty(FieldMappings.getSystemFieldName("provider")));
				result.put("year", node.getProperty(FieldMappings.getSystemFieldName("year")));
				result.put("language", node.getProperty(FieldMappings.getSystemFieldName("language")));
				result.put("duration", node.getProperty(FieldMappings.getSystemFieldName("duration")));
				resultsForType.add(result);
				all.add(result);
			}
			
			resultSet.put(type, resultsForType);
		} 
				
		return resultSet;
	};
	
	private Filter getTypeFilter(){
		ArrayList<FilterCondition> types = new ArrayList<FilterCondition>();
		
		types.add(new TypeCondition("video"));
		types.add(new TypeCondition("picture"));
		types.add(new TypeCondition("doc"));
		types.add(new TypeCondition("audio"));
		
		Filter filter = new Filter(types);
		return filter;
	};
	
	public void setDefaultSorting(Screen s){
		s.setProperty("sortDirection", "up");
		s.setProperty("sortField", FieldMappings.getSystemFieldName("title"));
	}
	
	public void setSorting(Screen s, String data){
		JSONObject message = (JSONObject) JSONValue.parse(data);
		s.setProperty("sortDirection", message.get("direction"));
		s.setProperty("sortField", FieldMappings.getSystemFieldName((String) message.get("field")));
		search(s);
	}
	
	public void createCounterFilter(Screen s){
		System.out.println("createCounterFilter()");
		
		HashMap<String, HashMap<String, EqualsCondition>> categorisedConditionsToCount = new HashMap<String, HashMap<String, EqualsCondition>>();
		ArrayList<FilterCondition> conditions = new ArrayList<FilterCondition>();
		
		for(Iterator<FsNode> iter = allNodes.getNodes().iterator() ; iter.hasNext(); ) {
			// get the next node
			FsNode n = (FsNode)iter.next();	
			
			for(Iterator<String> categoriesIterator = this.availableConditionFieldCategories.iterator(); categoriesIterator.hasNext();){
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
				it.next().clearPassed();
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
				categoryResults.put(fieldName, fieldCondition.getPassed().size());
			}
		}
		
		return countedResults;
	}
	
	private JSONObject createConditionFieldsForClient(Screen s){
		System.out.println("createConditionFieldsForClient()");
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
	
	public void populateFieldsClient(Screen s){
		System.out.println("populateConditionsFieldsClient()");
		JSONObject allFilters = this.createConditionFieldsForClient(s);
		s.setProperty("allFilters", allFilters);
		
		s.putMsg("filter", "", "populateFields(" + allFilters + ")");
	}
	
	public void createFilter(Screen s){
		s.setProperty("filter", new Filter());
	}
	
	/**
	 * Sets a filter on this screen based on the selection by the user.
	 * 
	 * @param s The screen for which the filter was selected
	 * @param data The data containing the userselection. 
	 */
	public void setClientSelectedField(Screen s, String data){
		System.out.println("setClientSelectedCondition()");
		JSONObject activeFields = (JSONObject) s.getProperty("clientSelectedFields");
		if(activeFields == null){
			activeFields = new JSONObject();
			s.setProperty("clientSelectedFields", activeFields);
		}

		JSONObject message = (JSONObject) JSONValue.parse(data);
		System.out.println("MESSAGE: " + message);
		String category = (String) message.keySet().iterator().next();
		System.out.println("Category: " + category);
		String value = (String) message.values().iterator().next();
		System.out.println("value:" + value);
		JSONArray fieldsForCategory = (JSONArray) activeFields.get(category);
		
		if(fieldsForCategory == null){
			fieldsForCategory = new JSONArray();
			activeFields.put(category, fieldsForCategory);
		}
		
		fieldsForCategory.add(value);
		
		System.out.println(activeFields);
		
		s.putMsg("activefields", "", "setActiveFields(" + activeFields + ")");
		
		createFilterFromClientSelectionForScreen(s);
		
		//Execute the search again in order update the results based on the new filter. 
		this.search(s);
	}
	
	/**
	 * Removes a filter on this screen based on the selection by the user. 
	 * 
	 * @param s The screen for which the filter was removed
	 * @param data The data containing information as to what filter was removed. 
	 */
	public void removeClientSelectedField(Screen s, String data){
		JSONObject message = (JSONObject) JSONValue.parse(data);
		JSONObject activeFields = (JSONObject) s.getProperty("clientSelectedFields");
		
		String category = (String) message.keySet().iterator().next();
		String field = (String) message.values().iterator().next();
		
		JSONArray activeForCategory = (JSONArray) activeFields.get(category);
		activeForCategory.remove(activeForCategory.indexOf(field));
		if(activeForCategory.isEmpty()){
			activeFields.remove(category);
		}
		createFilterFromClientSelectionForScreen(s);
		this.search(s);
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
		
		System.out.println(activeFields);
		
		for(Iterator<String> iter = activeFields.keySet().iterator(); iter.hasNext();){
			String key = iter.next();
			ArrayList<String> allowedValues = (ArrayList) activeFields.get(key);
			ArrayList<FilterCondition> equalsConditions = new ArrayList<FilterCondition>();
			equalsConditions = createEqualsConditionsForField(FieldMappings.getSystemFieldName(key), allowedValues);
			
			OrCondition orCondition = new OrCondition(equalsConditions);
			andCondition.add(orCondition);
		}
		
		conditions.add(andCondition);
		
		s.setProperty("filter", new Filter(conditions));
	}
	
	private ArrayList<FilterCondition> createEqualsConditionsForField(String field, ArrayList<String> allowedValues){
		ArrayList<FilterCondition> conditions = new ArrayList<FilterCondition>();
		for(Iterator<String> i = allowedValues.iterator(); i.hasNext();){
			conditions.add(new EqualsCondition(field, i.next(), ","));
		}
		return conditions;
	}

}
