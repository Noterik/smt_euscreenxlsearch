var Tabs = function(options){
	Component.apply(this, arguments);
	this.element = jQuery("#tabs");
	
	setTimeout(function(){
		jQuery('a[data-toggle="tab"]').on('shown.bs.tab', function(event, element){
			eddie.putLou('', 'setActiveType(' + jQuery(event.target).data('type') + ')');
		})
	}, 50);
	
	this.element.find('a').on('click', function(event){
		event.preventDefault();
		event.stopPropagation();
	});
};

Tabs.prototype = Object.create(Component.prototype);
Tabs.prototype.loadTab = function(type){
	this.element.find('a[data-type="' + type + '"]').tab('show');
};
Tabs.prototype.setActiveTabs = function(data){
	var message = JSON.parse(data);
	
	for(var type in message){
		var active = message[type];
		
		var tab = this.element.find('a[data-type="' + type + '"]')
		
		if(active){
			tab.removeClass('inactive');
			tab.attr('data-toggle', 'tab');
			tab.off('click');
		}else{
			tab.addClass('inactive');
			tab.attr('data-toggle', '');
			tab.on('click', function(event){
				event.preventDefault();
			});
		}
	}
};