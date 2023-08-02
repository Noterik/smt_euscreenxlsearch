var Filter = function(options){
    	console.log("Filter()");
	var self = this;
	
	Component.apply(this, arguments);
	
	this.element = jQuery("#filter");
	this.loadingElement = this.element.find('#filter-accordion > .loading');
	this.countries = {};
	
	//The boxes which will be filled with options.
	this.fieldElements = {
		language: self.element.find("#filter-language ul"),
		decade: self.element.find("#filter-decade ul"),
		topic: self.element.find("#filter-topic ul"),
		provider: self.element.find("#filter-provider ul"),
		publisher: self.element.find("#filter-publisher ul"),
		genre: self.element.find("#filter-genre ul"),
		country: self.element.find("#filter-country-production ul")
	};
	
	//The template of the options for the select boxes
	this.optionTemplate = this.element.find('#filter-option-template').text();
	this.counterTemplate = this.element.find('#filter-counter-template').text();
	
	this.element.on('click', function(event){
		self.fieldClicked.apply(self, arguments);
	});
	
};

Filter.prototype = Object.create(Component.prototype);
Filter.prototype.events = {
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
	"click #search-parameters .done button": function(event){
		jQuery('#search-parameters').removeClass('optionOpened');
	},
	"show.bs.collapse .filtercontent": function(event) {
		console.log("SHOW");
    	$("#"+event.currentTarget.id).parent().find("i").removeClass("glyphicon-chevron-down").addClass("glyphicon-chevron-up");
    },
    "hide.bs.collapse .filtercontent": function(event) {
    	console.log("HIDE");
    	$("#"+event.currentTarget.id).parent().find("i").removeClass("glyphicon-chevron-up").addClass("glyphicon-chevron-down");
    }
};
Filter.prototype.loading = function(loading){
	console.log("Filter.prototype.loading()");
	if(loading == "true"){
		this.loadingElement.show();
	}else{
		this.loadingElement.hide();
	}
};
Filter.prototype.populateFields = function(data){
	console.log("Filter.populateFields()");
	var self = this;
	var fields = JSON.parse(data);
			
	for(var category in fields){
		var fieldsForCategory = fields[category];
		var templateParameters = {
			options: fieldsForCategory
		};
		
		if(category == "provider"){
			templateParameters.countries = self.countries;
		}
		self.fieldElements[category].html(_.template(self.optionTemplate, templateParameters));
	}
	
};
Filter.prototype.setCounts = function(data){
	console.log("Filter.prototype.setCounts()");
	var self = this;
	
	var fields = JSON.parse(data);
		
	for(var category in fields){
		var args = {category: category};
		var counts = fields[category];
		self.fieldElements[category].find('li').hide();
		var availableFields = _.filter(counts, function(value){
			return value > 0
		});
		if(availableFields.length > 1){
			this.activateCategory(JSON.stringify(args));
			for(var value in counts){
				self.fieldElements[category].find('a[data-value="' + value + '"]').parent().find('span').remove();
				if(!counts[value] == 0){
					self.fieldElements[category].find('a[data-value="' + value + '"]').append(_.template(self.counterTemplate, {counter: {amount: counts[value]}}));
					self.fieldElements[category].find('a[data-value="' + value + '"]').parent().show();
				}
			}
		}else{
			this.deactivateCategory(JSON.stringify(args));
		}
	}
};
Filter.prototype.fieldClicked = function(event){
	var category = $(event.target).parent().parent().data('category');
	var fieldVal = $(event.target).data('value');
	
	var objectToSend = {};
	
	if(fieldVal){
		objectToSend[category] = "" + fieldVal;
		
		eddie.putLou("", "setfield(" + JSON.stringify(objectToSend) + ")");
	}
	
	setTimeout(function(){
		eddie.putLou("template", "positionSidebar()");
	}, 500);
};
Filter.prototype.deactivateCategory = function(data){
	console.log("deactivateCategory(" + data + ")");
	var message = JSON.parse(data);
	
	var category = message.category;
	var filterElement = this.fieldElements[category].parent().parent();
	
	console.log(filterElement);
	var a = filterElement.find('> a');
	
	this.fieldElements[category].parent().removeClass('in').addClass('out');
	
	filterElement.addClass('inactive');
	console.log(filterElement[0]);
	a.on('click', function(){
		event.stopPropagation();
	});
	
	a.find('i').removeClass('glyphicon-chevron-up').addClass('glyphicon-chevron-down');
	a.find('i').hide();
	
	this.fieldElements[category].find('input:checkbox').prop('checked', false);
	
};
Filter.prototype.activateCategory = function(data){
	console.log("activateCategory(" + data + ")");
	var message = JSON.parse(data);
	
	var category = message.category;
	var filterElement = this.fieldElements[category].parent().parent();
	var a = filterElement.find('> a');
	
	filterElement.removeClass('inactive');
	a.off('click');
	
	a.find('i').show();
}
Filter.prototype.deactivate = function(){
	this.element.find(".filter").addClass('inactive');
};
Filter.prototype.setCountries = function(data){
	console.log("setCountries(" + data + ")");
	this.countries = JSON.parse(data);
};
