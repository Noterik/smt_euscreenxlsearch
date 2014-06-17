var Searchinput = function(options){
	var self = this;
	
	this.element = jQuery("#searchinput");
	this.searchQueryInput = this.element.find('.search-input-form-element');
	
	this.searchQueryInput.keyup(function(event){
		if(event.keyCode === 13){
			self.search();
		}
	})
};
Searchinput.prototype = Object.create(Component);
Searchinput.prototype.events = {
	"onkeyup #searchinput .search-input-form-element": function(event){
		if (e.keyCode === 13) {
			this.search();
		}
	}
};
Searchinput.prototype.search = function(){
	var objectToSend = {
    	query: this.searchQueryInput.val()
    };
    eddie.putLou('', 'query(' + JSON.stringify(objectToSend) + ')');
};