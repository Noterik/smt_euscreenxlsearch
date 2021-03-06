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
		},
		"series": {
			element: self.element.find('#results-series')
		}
	}
	
	this.template = this.element.find('.result-template').text();
	this.loadingTemplate = this.element.find('.loading-template').text();
	this.tabContentElement = this.element.find('.tab-content');
	this.spinner = this.element.find('.spinner');
	this.noSearchElement = this.element.find('#no-search-term');
	this.noResultsElement = this.element.find('#no-results');
	
	//intercept item request to be able to scroll to the item in the results when going back
	this.element.on('click', '.media-item a', function() {
	    event.preventDefault();

	    var href = jQuery(this).attr("href"); 
	   
	    var lastItem = jQuery(this).data("params-id");

	    window.location = href;
	});	
};

Results.prototype = Object.create(Component.prototype);
Results.prototype.events = {
	"click #results .more a": function(event, element){
		event.preventDefault();
		eddie.putLou('', 'getNextChunk()');
	}
};
Results.prototype.loading = function(loading){
	if(loading == "true"){
		this.spinner.removeClass('hidden');
	}else{
		this.spinner.addClass('hidden');
	}
};
Results.prototype.startScreen = function(){
	this.spinner.addClass('hidden');
	this.clear();
	this.noSearchElement.removeClass('hidden');
	this.hideLoadMore();
};
Results.prototype.clear = function(){
	for(page in this.pages){
		this.pages[page].element.find('.list').html('');
	}
};
Results.prototype.setResults = function(results){	
	console.log("Results.prototype.setResults()");
	this.tabContentElement.removeClass('hidden');
	this.noSearchElement.addClass('hidden');
	this.spinner.addClass('hidden');
	var results = JSON.parse(results);
	console.log(results);
		
	for(var type in results){
		var resultsForType = results[type];
		
		if(resultsForType.length == 0){
			this.noResultsElement.removeClass('hidden');
		}else{
			this.noResultsElement.addClass('hidden');
			for(var i = 0; i < resultsForType.length; i++){
				var item = resultsForType[i];
				
				var elementStr = _.template(this.template, {item: item});
				this.pages[type].element.find('.list').append(elementStr);
			}
		}
		
	}
	eddie.getComponent('urltransformer').run();
	eddie.putLou('template', 'positionSidebar()');
};
Results.prototype.hideLoadMore = function(){
	this.element.find('a.show-more').addClass('hidden');
};
Results.prototype.showLoadMore = function(){
	this.element.find('a.show-more').removeClass('hidden');
};