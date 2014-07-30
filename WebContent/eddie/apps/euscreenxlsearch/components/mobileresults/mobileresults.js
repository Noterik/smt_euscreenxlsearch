var Mobileresults = function(options){
	Component.apply(this, arguments);
	
	this.element = jQuery("#mobileresults");
	this.list = this.element.find('.list');
	this.moreButton = this.element.find('.more');
	this.template = this.element.find('.result-template').text();
};

Mobileresults.prototype = Object.create(Component.prototype);
Mobileresults.prototype.visibleType = "all";
Mobileresults.prototype.events = {
	"click #mobileresults .more a": function(event, element){
		event.preventDefault();
		eddie.putLou('', 'getNextChunk()');
	}
};
Mobileresults.prototype.chunkSize = 10;
Mobileresults.prototype.currentChunk = 0;
Mobileresults.prototype.currentResults = null;
Mobileresults.prototype.setResults = function(results){	
	console.log("Mobileresults.setResults()");
	var results = JSON.parse(results);
	this.currentResults = results;
	
	for(var type in results){
		var resultsForType = results[type];
		for(var i = 0; i < resultsForType.length; i++){
			var item = resultsForType[i];
			
			var elementStr = _.template(this.template, {item: item});			
			this.list.append(elementStr);
		}
	}
	
};
Mobileresults.prototype.clear = function(){
	this.list.html('');
};
Mobileresults.prototype.hideLoadMore = function(){
	this.element.find(".more a").hide();
};
Mobileresults.prototype.showLoadMore = function(){
	this.element.find(".more a").show();
};