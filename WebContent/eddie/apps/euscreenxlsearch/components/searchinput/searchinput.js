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
Searchinput.prototype.events = {
	"submit #searchinput form": function(event){
		event.preventDefault();
		this.search();
		this.element.find('input').blur();
	}
};
Searchinput.prototype.setQuery = function(query){
	this.searchQueryInput.val(query);
};
Searchinput.prototype.search = function(){
	var objectToSend = {
    	query: this.searchQueryInput.val()
    };
    eddie.putLou('', 'query(' + JSON.stringify(objectToSend) + ')');
};