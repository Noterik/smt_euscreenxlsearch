var Mobileresults = function(options){
	Component.apply(this, arguments);
	
	this.element = jQuery("#mobileresults");
	this.list = this.element.find('.list');
	this.moreButton = jQuery("#showmoreresults");
	this.template = this.element.find('.result-template').text();
	
	this.tabContentElement = this.element.find('.tab-content');
	this.spinner = this.element.find('.spinner');
	this.noSearchElement = this.element.find('#no-search-term');
	this.noResultsElement = this.element.find('#no-results');
};

Mobileresults.prototype = Object.create(Component.prototype);
Mobileresults.prototype.visibleType = "all";
Mobileresults.prototype.events = {
	"click #showmoreresults a": function(event, element){
		event.preventDefault();
		eddie.putLou('', 'getNextChunk()');
	}
};
Mobileresults.prototype.chunkSize = 10;
Mobileresults.prototype.currentChunk = 0;
Mobileresults.prototype.currentResults = null;
Mobileresults.prototype.setResults = function(results){	
	console.log("Mobileresults.setResults()");
	this.noSearchElement.addClass('hidden');
	var results = JSON.parse(results);
	this.currentResults = results;
	var hasResults = false;
		
	for(var type in results){
		var resultsForType = results[type];
		if(resultsForType.length > 0){
			hasResults = true;
			for(var i = 0; i < resultsForType.length; i++){
				var item = resultsForType[i];
				
				var elementStr = _.template(this.template, {item: item});			
				this.list.append(elementStr);
			}	
		}
		
	}
	
	if(hasResults){
		this.noResultsElement.addClass('hidden');
	}else{
		this.noResultsElement.removeClass('hidden');
	}
	
};
Mobileresults.prototype.clear = function(){
	console.log("Mobileresults.prototype.clear()");
	this.list.html('');
};
Mobileresults.prototype.loading = function(loading){
	if(loading == "true"){
		this.spinner.removeClass('hidden');
	}else{
		this.spinner.addClass('hidden');
	}
};
Mobileresults.prototype.hideLoadMore = function(){
	this.moreButton.addClass('hidden');
};
Mobileresults.prototype.showLoadMore = function(){
	this.moreButton.removeClass('hidden');
};