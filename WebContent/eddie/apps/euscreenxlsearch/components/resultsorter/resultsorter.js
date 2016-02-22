var Resultsorter = function(options){
	Component.apply(this, arguments);
	
	this.element = jQuery('#resultsorter');
	this.sortDescButton = this.element.find('.sort-desc');
	this.sortAscButton = this.element.find('.sort-asc');
};

Resultsorter.prototype = Object.create(Component.prototype);
Resultsorter.prototype.events = {
	"click #sortBy li a": function(event){
		var $src = jQuery(event.target);
		var field = $src.data('field');
		var object = {
			field: field,
			direction: "up",
			value: $src.data('value')
		};
		this.element.find('.dropdown span.field').text($src.text());
		var command = "setSorting(" + JSON.stringify(object) + ")";
		
		console.log("COMMAND = " + command);
		eddie.putLou("", command);
	},
	"click #resultsorter .sort-asc": function(event){
		event.preventDefault();
		this.sortDescButton.removeClass('hide');
		this.sortAscButton.addClass('hide');
		eddie.putLou("", "setSortDirection(down)");
	},
	"click #resultsorter .sort-desc": function(event){
		event.preventDefault();
		this.sortAscButton.removeClass('hide');
		this.sortDescButton.addClass('hide');
		eddie.putLou("", "setSortDirection(up)");
	}
}