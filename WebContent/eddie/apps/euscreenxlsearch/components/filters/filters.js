var Filters = function(options){
	console.log("Filters()");
	var self = this;
	
	Component.apply(this, arguments);
	
	this.element = jQuery("#filters");
	
	//The boxes which will be filled with options.
	this.filterElements = {
		language: self.element.find("#filter-language select"),
		decade: self.element.find("#filter-decade select"),
		topic: self.element.find("#filter-topic select"),
		provider: self.element.find("#filter-provider select"),
		publisher: self.element.find("#filter-publisher select"),
		genre: self.element.find("#filter-genre select"),
		country: self.element.find("#filter-country-production select")
	};
	
	//The template of the options for the select boxes
	this.optionTemplate = this.element.find('#filter-option-template').text();
};

Filters.prototype = Object.create(Component.prototype);
Filters.prototype.events = {
	"click #show-extra-filters": function(event){
		this.element.find('.extra-option').show();
		this.element.find('#show-extra-filters').hide();
		this.element.find('#hide-extra-filters').show();
	},
	"click #hide-extra-filters": function(event){
		this.element.find('.extra-option').hide();
		this.element.find('#show-extra-filters').show();
		this.element.find('#hide-extra-filters').hide();
	},
	"change select.filter": function(event){
		this.filterClicked(event);
	}
};
Filters.prototype.populateFilters = function(data){
	console.log("Filters.populateFilters()");
	var self = this;
	var filters = JSON.parse(data);
		
	for(var filter in filters){
		var options = filters[filter];
		this.filterElements[filter].html(_.template(this.optionTemplate, {options: options}));
	}
};
Filters.prototype.setCounts = function(data){
	console.log("Filters.prototype.setCounts()");
	var self = this;
	
	var counts = JSON.parse(data);
	for(var filter in counts){
		for(var field in counts[filter]){
			if(counts[filter][field] == 0){
				self.filterElements[filter].find('option[value="' + field + '"]').hide();
			}else{
				self.filterElements[filter].find('option[value="' + field + '"]').text(field + "    (" + counts[filter][field] + ")");
				self.filterElements[filter].find('option[value="' + field + '"]').show();
			}
			
		}
	}
};
Filters.prototype.filterClicked = function(event){
	var filterType = _.find(_.map(this.filterElements, function(value, key){
		if(value[0] == event.srcElement){
			return key;
		}
	}), function(value){
		return value;
	});
	
	var selectedValues = _.map(jQuery(event.srcElement).find(':selected'), function(val, key){
		return jQuery(val).val();
	});
	
	var objectToSend = {};
	objectToSend[filterType] = selectedValues;
	
	eddie.putLou("", "setfilter(" + JSON.stringify(objectToSend) + ")");
};
