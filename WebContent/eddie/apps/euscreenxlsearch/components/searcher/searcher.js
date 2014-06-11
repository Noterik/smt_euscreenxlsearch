var Searcher = function(options){
	Component.apply(this, [arguments]);
	
	this.component = jQuery('#searcher');	
	this.searchQueryInput = this.component.find('.search-query');
	this.resultsCounter = this.component.find('.results-counter');
	this.availableSorters = [];
	
};

Searcher.prototype = Object.create(Component.prototype);
Searcher.prototype.provider = '';
Searcher.prototype.mtype = 'all';
Searcher.prototype.datasource = 'all';
Searcher.prototype.decade = 'all';
Searcher.prototype.sortField = 'id';
Searcher.prototype.sortDirection = 'up';
Searcher.prototype.maxDisplay = 20;
Searcher.prototype.setCounter = function(amount){
	this.resultsCounter.text(amount + ' results');
};
Searcher.prototype.events = {
	"onkeyup .search-query": function(event){
		if (e.keyCode === 13) {
			this.search();
		}
	},
	"click button.search-go": function (event) {
        this.search();
    }
};
Searcher.prototype.search = function(){
	var objectToSend = {
    	query: this.searchQueryInput.val()
    };
    eddie.putLou('', 'query(' + JSON.stringify(objectToSend) + ')');
};
