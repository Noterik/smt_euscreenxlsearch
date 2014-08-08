var Mobilesearchinput = function(options){
	console.log("Mobilesearchinput()");
	Component.apply(this, arguments);
	var self = this;
	
	this.element = jQuery("#mobilesearchinput");
	this.searchQueryInput = this.element.find('#searchkeyword');
	this.searchParametersElement = jQuery('#search-parameters');
	this.toggleButton = jQuery("#mobilesearchinput #optionbutton");
	
	this.element.find('form').submit(function(event){
		event.preventDefault();
		self.search();
		self.element.find('input').blur();
	});
		
};
Mobilesearchinput.prototype = Object.create(Component.prototype);
Mobilesearchinput.prototype.events = {
	"submit #mobilesearchinput form": function(event){
		event.preventDefault();
		this.search();
		this.element.find('input').blur();
	},
	"click #mobilesearchinput #optionbutton": function(event){
		var button = jQuery(event.target);
		if(this.searchParametersElement.hasClass('optionOpened')){
			this.searchParametersElement.removeClass('optionOpened');
			this.toggleButton.removeClass('active');
		}else{
			this.searchParametersElement.addClass('optionOpened');
			this.toggleButton.addClass('active');
		}
		
	}
};
Mobilesearchinput.prototype.search = function(){
	if(this.searchQueryInput.val().length > 0){
		console.log("Mobilesearchinput.search()");
		console.log(this.searchQueryInput.val());
		var objectToSend = {
	    	query: this.searchQueryInput.val()
	    };
	    eddie.putLou('', 'query(' + JSON.stringify(objectToSend) + ')');
	}
};
Mobilesearchinput.prototype.setQuery = function(query){
	console.log("mobilesearchinput.prototype.setquery(" + query + ")");
	this.searchQueryInput.val(query);
};