package org.springfield.lou.application.types.conditions;

import org.springfield.lou.fs.FsNode;

public class EqualsCondition extends FilterCondition {
	
	private String field;
	private String allowedValue;
	private String fieldSeperator = ",";
	private boolean caseSensitive = false;
	
	public EqualsCondition(String field, String allowedValue) {
		// TODO Auto-generated constructor stub
		this.field = field;
		this.allowedValue = allowedValue;
	}
	
	public EqualsCondition(String field, String allowedValue, boolean caseSensitive){
		this(field, allowedValue);
		this.caseSensitive = caseSensitive;
	}
	
	public EqualsCondition(String field, String allowedValue, String fieldSeperator){
		this(field, allowedValue);
		this.fieldSeperator = fieldSeperator;
	}
	
	public EqualsCondition(String field, String allowedValue, boolean caseSensitive, String fieldSeperator){
		this(field, allowedValue, caseSensitive);
		this.fieldSeperator = fieldSeperator;
	}

	@Override
	public boolean allow(FsNode node) {
		String value = node.getProperty(field);
		if(value != null){
			
			if(caseSensitive){
				value = value.toLowerCase();
				allowedValue = allowedValue.toLowerCase();
			}
			
			String[] values;
			if(fieldSeperator != null){
				values = value.split(fieldSeperator);
			}else{
				values = new String[]{value};
			}
			
			for(int i = 0; i < values.length; i++){
				String singleValue = values[i];
								
				if(allowedValue.equals(singleValue)){
					this.getCounter().tick();
					return true;
				}
				
			}
		}
		return false;
	}
	
	public String getField(){
		return field;
	}
	
	public String getAllowedValue(){
		return this.allowedValue;
	}

}
