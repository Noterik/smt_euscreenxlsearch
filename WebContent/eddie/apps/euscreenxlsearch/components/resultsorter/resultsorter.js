var Resultsorter = function(options){
	Component.apply(this, arguments);
};

Resultsorter.prototype = Object.create(Component.prototype);
Resultsorter.prototype.events = {
	"click #sortBy li a": function(event){
		var object = {
			field: jQuery(event.srcElement).data('field'),
			direction: "up"
		};
		eddie.putLou("", "setsorting(" + JSON.stringify(object) + ")");
	}
}