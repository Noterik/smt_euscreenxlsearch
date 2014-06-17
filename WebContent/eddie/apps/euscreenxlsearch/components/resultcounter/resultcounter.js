var Resultcounter = function(options){
	console.log("Resultcounter()");
	this.element = jQuery("#resultcounter");
	this.amountElement = this.element.find(".number");
};
Resultcounter.prototype = Object.create(Component.prototype);
Resultcounter.prototype.setAmount = function(amount){
	this.amountElement.text(amount);
};
