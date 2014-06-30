var Tabs = function(options){
	this.element = jQuery("#tabs");
	
	setTimeout(function(){
		jQuery('a[data-toggle="tab"]').on('shown.bs.tab', function(event, element){
			eddie.putLou('', 'setActiveType(' + jQuery(event.target).data('type') + ')');
		})
	}, 50);	
}

Tabs.prototype = Object.create(Component.prototype);