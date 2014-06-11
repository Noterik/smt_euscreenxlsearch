var Activefilters = function(options){
	console.log("Activefilters()");
	Component.apply(this, arguments);
	this.element = jQuery("#activefilters");
	this.activeFiltersElement = this.element.find('.active-filters');
	this.filterTemplate = this.element.find("#active-filter-template").text();
};

Activefilters.prototype = Object.create(Component.prototype);
Activefilters.prototype.setActiveFilters = function(data){
	var filters = JSON.parse(data);
	var templateObject = {
		'activeFilters': []
	};
	
	for(var type in filters){
		var filter = filters[type];
		for(var i = 0; i < filter.length; i++){
			templateObject.activeFilters.push({
				type: type,
				value: filter[i]
			});
		}
	};
	
	this.activeFiltersElement.html(_.template(this.filterTemplate, templateObject));
	this.activeFiltersElement.find(".filter button").off("click").click(function(event){
		var parent = jQuery(this).parent();
		var type = parent.data('type');
		var value = ""+parent.data('value');
		
		var objectToSend = {};
		objectToSend[type] = value;
		parent.remove();
		eddie.putLou('', 'removefilter(' + JSON.stringify(objectToSend) + ')');
	});
};
