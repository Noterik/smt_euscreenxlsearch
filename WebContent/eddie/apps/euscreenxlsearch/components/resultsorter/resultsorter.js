var Resultsorter = function(options){
	Component.apply(this, arguments);
	
	this.element = jQuery('#resultsorter');
	this.sortDescButton = this.element.find('.sort-desc');
	this.sortAscButton = this.element.find('.sort-asc');
};

Resultsorter.prototype = Object.create(Component.prototype);
Resultsorter.prototype.events = {
	"click #sortBy li a": function(event){
		var field = jQuery(event.srcElement).data('field');
		var object = {
			field: field,
			direction: "up"
		};
		this.element.find('.dropdown span.field').text(jQuery(event.srcElement).text());
		eddie.putLou("", "setsorting(" + JSON.stringify(object) + ")");
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