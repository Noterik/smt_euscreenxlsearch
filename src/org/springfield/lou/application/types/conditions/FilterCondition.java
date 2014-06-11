package org.springfield.lou.application.types.conditions;

import org.springfield.lou.fs.FsNode;

public abstract class FilterCondition implements IFilterCondition {
	
	private ConditionCounter counter;

	public FilterCondition() {
		// TODO Auto-generated constructor stub
		this.counter = new ConditionCounter();
	}
	
	public ConditionCounter getCounter(){
		return this.counter;
	}

}
