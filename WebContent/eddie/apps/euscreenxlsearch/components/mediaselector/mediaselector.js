var Mediaselector = function(options){
	Component.apply(this, arguments);
	
	this.element = jQuery('#mediaselector');
	this.selectBox = this.element.find('select');
};

Mediaselector.prototype = Object.create(Component.prototype);
Mediaselector.prototype.events = {
	"change #mediaselector select": function(event){
		eddie.putLou('', 'setActiveType(' + this.selectBox.find(':selected').val() + ')');
		jQuery('#search-parameters').removeClass('optionOpened');
		jQuery("#mobilesearchinput #optionbutton").removeClass('active');
	}
};