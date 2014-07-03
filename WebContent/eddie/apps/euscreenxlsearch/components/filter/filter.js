var Filter = function(options){
	console.log("Filter()");
	var self = this;
	
	Component.apply(this, arguments);
	
	this.element = jQuery("#filter");
	
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
	})
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
	"show.bs.collapse .filtercontent": function(event) {
    	$("#"+event.currentTarget.id).parent().find("i").removeClass("glyphicon-chevron-down").addClass("glyphicon-chevron-up");
    },
    "hide.bs.collapse .filtercontent": function(event) {
    	$("#"+event.currentTarget.id).parent().find("i").removeClass("glyphicon-chevron-up").addClass("glyphicon-chevron-down");
    }
};
Filter.prototype.populateFields = function(data){
	console.log("Filter.populateFields()");
	var self = this;
	var fields = JSON.parse(data);
			
	for(var category in fields){
		var fieldsForCategory = fields[category];
		self.fieldElements[category].html(_.template(self.optionTemplate, {options: fieldsForCategory}));
	}
	
};
Filter.prototype.setCounts = function(data){
	console.log("Filter.prototype.setCounts()");
	var self = this;
	
	var fields = JSON.parse(data);
	
	console.log(fields);
	
	for(var category in fields){
		var counts = fields[category];
		self.fieldElements[category].find('li').hide();
		for(var value in counts){
			self.fieldElements[category].find('a[data-value="' + value + '"]').parent().find('span.badge').remove();
			if(!counts[value] == 0){
				self.fieldElements[category].find('a[data-value="' + value + '"]').parent().append(_.template(self.counterTemplate, {counter: {amount: counts[value]}}));
				self.fieldElements[category].find('a[data-value="' + value + '"]').parent().show();
			}
		}
	}
};
Filter.prototype.fieldClicked = function(event){
	console.log(event.srcElement);
	
	var category = $(event.srcElement).parent().parent().data('category');
	var fieldVal = $(event.srcElement).data('value');
	
	var objectToSend = {};
	
	if(fieldVal){
		objectToSend[category] = "" + fieldVal;
		
		eddie.putLou("", "setfield(" + JSON.stringify(objectToSend) + ")");
	}
};
Filter.prototype.deactivateCategory = function(data){
	console.log("deactivateCategory()");
	var message = JSON.parse(data);
	
	var category = message.category;
	var a = this.fieldElements[category].parent().parent().find('> a');
	
	this.fieldElements[category].parent().removeClass('in').addClass('out');
	
	
	a.addClass('inactive');
	a.on('click', function(){
		event.stopPropagation();
	});
	
	a.find('i').removeClass('glyphicon-chevron-up').addClass('glyphicon-chevron-down');
	a.find('i').hide();
};
Filter.prototype.activateCategory = function(data){
	console.log("activateCategory()");
	var message = JSON.parse(data);
	
	var category = message.category;
	var a = this.fieldElements[category].parent().parent().find('> a');
	
	a.removeClass('inactive');
	a.off('click');
	
	a.find('i').show();
}
