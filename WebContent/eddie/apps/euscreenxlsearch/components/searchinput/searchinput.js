var Searchinput = function(options){
	Component.apply(this, arguments);
	var self = this;
	
	this.element = jQuery("#searchinput");
	this.searchQueryInput = this.element.find('#searchkeyword');
	
	this.searchQueryInput.keyup(function(event){
		if(event.keyCode === 13){
			self.search();
		}
	})
	console.log(this);
};
Searchinput.prototype = Object.create(Component.prototype);
Searchinput.prototype.events = {
	"onkeyup #searchinput #searchkeyword": function(event){
		if (e.keyCode === 13) {
			this.search();
		}
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