package org.springfield.lou.application.types.conditions;

import org.springfield.fs.FsNode;

public class NotCondition extends FilterCondition {
	
	private String field;
	private String nAllowedValue;
	private String fieldSeperator = null;
	private boolean trim = true;
	private boolean caseSensitive = false;
	
	public NotCondition(String field, String nAllowedValue) {
		System.out.println("nAllowedValue: " + nAllowedValue);
		// TODO Auto-generated constructor stub
		this.field = field;
		this.nAllowedValue = nAllowedValue;
	}
	
	public NotCondition(String field, String nAllowedValue, boolean caseSensitive){
		this(field, nAllowedValue);
		this.caseSensitive = caseSensitive;
	}
	
	public NotCondition(String field, String nAllowedValue, String fieldSeperator){
		this(field, nAllowedValue);
		this.fieldSeperator = fieldSeperator;
	}
	
	public NotCondition(String field, String nAllowedValue, boolean caseSensitive, String fieldSeperator){
		this(field, nAllowedValue, caseSensitive);
		this.fieldSeperator = fieldSeperator;
	}

	@Override
	public boolean allow(FsNode node) {
		String value = node.getProperty(field);
		
		if(value != null){
			
			if(caseSensitive){
				value = value.toLowerCase();
				nAllowedValue = nAllowedValue.toLowerCase();
			}
			
			if(trim){
				value = value.trim();
			}
			
			String[] values;
			if(fieldSeperator != null){
				values = value.split(fieldSeperator);
			}else{
				values = new String[]{value};
			}
			
			for(int i = 0; i < values.length; i++){
				String singleValue = values[i];
								
				if(!nAllowedValue.equals(singleValue)){
					
					this.getPassed().add(node);
					return true;
				}else{
					System.out.println("HIDE YOUR WIFES, HIDE YOUR DAUGHTERS!!");
				}
				
			}
		}
		return false;
	}
	
	public String getField(){
		return field;
	}
	
	public String getAllowedValue(){
		return this.nAllowedValue;
	}

}
