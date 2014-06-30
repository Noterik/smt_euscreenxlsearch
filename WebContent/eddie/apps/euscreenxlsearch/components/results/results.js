var Results = function(options){	
	console.log("Results()");
	Component.apply(this, arguments);
	var self = this;
	
	this.element = jQuery("#results");
	
	this.pages = {
		"all": {
			element: self.element.find('#results-all')
		},
		"video": {
			element: self.element.find('#results-videos')
		},
		"picture": {
			element: self.element.find('#results-images')
		},
		"audio": {
			element: self.element.find('#results-audio')
		},
		"doc": {
			element: self.element.find("#results-documents")
		}
	}
	
	this.template = this.element.find('.result-template').text();
	this.loadingTemplate = this.element.find('.loading-template').text();
};

Results.prototype = Object.create(Component.prototype);
Results.prototype.events = {
	"click #results .more a": function(event, element){
		event.preventDefault();
		eddie.putLou('', 'getNextChunk()');
	}
};
Results.prototype.loading = function(loading){
	
};
Results.prototype.clear = function(){
	for(page in this.pages){
		this.pages[page].element.find('.list').html('');
	}
};
Results.prototype.setResults = function(results){	
	console.log("Results.setResults()");
	console.log(JSON.parse(results));
	//this.clear();
	var results = JSON.parse(results);
	
	for(var type in results){
		var resultsForType = results[type];
		for(var i = 0; i < resultsForType.length; i++){
			var item = resultsForType[i];
			
			var elementStr = _.template(this.template, {item: item});		
			this.pages[type].element.find('.list').append(elementStr);
		}
	}
};