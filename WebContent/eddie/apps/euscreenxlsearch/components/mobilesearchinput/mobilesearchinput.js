var Mobilesearchinput = function(options){
	console.log("Mobilesearchinput()");
	Component.apply(this, arguments);
	var self = this;
	
	this.element = jQuery("#mobilesearchinput");
	this.searchQueryInput = this.element.find('#searchkeyword');
	this.searchParametersElement = jQuery('#search-parameters');
	this.toggleButton = jQuery("#mobilesearchinput #optionbutton");
	
	this.searchQueryInput.keyup(function(event){
		if(event.keyCode === 13){
			self.search();
		}
	});
		
};
Mobilesearchinput.prototype = Object.create(Component.prototype);
Mobilesearchinput.prototype.events = {
	"onkeyup #mobilesearchinput #searchkeyword": function(event){
		if (e.keyCode === 13) {
			this.search();
		}
	},
	"focusout #mobilesearchinput #searchkeyword": function(event){
		this.search();
	},
	"click #mobilesearchinput #optionbutton": function(event){
		var button = jQuery(event.target);
		console.log(button[0]);
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
		var objectToSend = {
	    	query: this.searchQueryInput.val()
	    };
	    eddie.putLou('', 'query(' + JSON.stringify(objectToSend) + ')');
	}
};