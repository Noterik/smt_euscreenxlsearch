package org.springfield.lou.application.types;

import java.util.HashMap;
import java.util.Map.Entry;

public class FieldMappings {
	
	private static final HashMap<String, String> mappings;
    static
    {
        mappings = new HashMap<String, String>();
        mappings.put("screenshot","screenshot");
        mappings.put("title", "TitleSet_TitleSetInEnglish_title");
        mappings.put("originalTitle", "TitleSet_TitleSetInOriginalLanguage_title");
        mappings.put("provider", "provider");
        mappings.put("year", "SpatioTemporalInformation_TemporalInformation_productionYear");
        mappings.put("language", "originallanguage");
        mappings.put("duration", "TechnicalInformation_itemDuration");
        mappings.put("topic", "topic");
        mappings.put("publisher", "publisherbroadcaster");
        mappings.put("genre", "genre");
        mappings.put("country", "SpatioTemporalInformation_SpatialInformation_CountryofProduction");
        mappings.put("sort_title", "sort_title");
    }
    
    private static final HashMap<String, String> interfaceMappings;
    static
    {
    	interfaceMappings = new HashMap<String, String>();
        interfaceMappings.put("SpatioTemporalInformation_TemporalInformation_productionYear", "YEAR");
        interfaceMappings.put("sort_title", "TITLE");
    }
    
    public static String getSystemFieldName(String readable){
    	return mappings.get(readable);
    }
    
    public static String getInterfaceFieldName(String readable){
    	return interfaceMappings.get(readable);
    }
    
    public static String getReadable(String systemName){
    	for (Entry<String, String> entry : mappings.entrySet()) {
            if (systemName.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
    	return null;
    }

}
