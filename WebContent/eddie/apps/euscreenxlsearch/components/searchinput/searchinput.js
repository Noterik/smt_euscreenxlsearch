var Searchinput = function(options){
	Component.apply(this, arguments);
	var self = this;
	
	this.element = jQuery("#searchinput");
	this.searchQueryInput = this.element.find('#searchkeyword');
	
	this.element.find('form').on('submit', function(event){
		event.preventDefault();
		self.search();
		self.element.find('input').blur();
	})
};
Searchinput.prototype = Object.create(Component.prototype);
Searchinput.prototype.events = {};
Searchinput.prototype.setQuery = function(query){
	this.searchQueryInput.val(query);
};
Searchinput.prototype.search = function(){
	var query = this.searchQueryInput.val();
	if(!(query == "" || query == " " || query == null)){
		var objectToSend = {
	    	query: query
	    };
	    eddie.putLou('', 'query(' + JSON.stringify(objectToSend) + ')');
	}
};