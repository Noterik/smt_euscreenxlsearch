package org.springfield.lou.application.types;

import java.util.Comparator;
import java.util.HashMap;

import org.springfield.fs.FsNode;

public class Sorters {
	private static final HashMap<String, Comparator<FsNode>> sorters;
    static
    {
        sorters = new HashMap<String, Comparator<FsNode>>();
        sorters.put("TitleSet_TitleSetInEnglish_title", new Comparator<FsNode>(){
			public int compare(FsNode n1, FsNode n2){
				String title1 = n1.getProperty(FieldMappings.getSystemFieldName("sort_title")).trim().toLowerCase().replaceAll("\\W", ""); ;
				String title2 = n2.getProperty(FieldMappings.getSystemFieldName("sort_title")).trim().toLowerCase().replaceAll("\\W", "");
				
				return title2.compareTo(title1);
			}
		});
        
        sorters.put("SpatioTemporalInformation_TemporalInformation_productionYear", new Comparator<FsNode>(){
        	public int compare(FsNode n1, FsNode n2){
        		String n1Year = n1.getProperty(FieldMappings.getSystemFieldName("year"));
				String n2Year = n2.getProperty(FieldMappings.getSystemFieldName("year"));
				
				Integer n1YearInt;
				Integer n2YearInt;
				try{
					n1YearInt = Integer.parseInt(n1Year);
				}catch(NumberFormatException nfe){
					n1YearInt = 0;
				}
				
				try{
					n2YearInt = Integer.parseInt(n2Year);
				}catch(NumberFormatException nfe){
					n2YearInt = 0;
				}
				
				return n2YearInt.compareTo(n1YearInt);
        	}
        });
    }
    
    public static Comparator<FsNode> getSorter(String field){
    	return sorters.get(field);
    }
}
