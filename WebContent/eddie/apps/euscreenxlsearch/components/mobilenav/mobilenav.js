var Mobilenav = function(options){
	var self = this;
	Component.apply(this, arguments);
	
	this.element = jQuery('#mobilenav');
	
	console.log(this.element[0]);
	
	console.log(this.element.slidePanelJS);
	
	setTimeout(function(){
		self.element.slidePanelJS({
	        openButton: '#menubutton',
	        pageSection:'#page',
	        navbarSection:'#navbar',
	        speed:200
	    });
	    
	}, 100);
	// nav panel
    
    console.log("SLIDE PANEL INITIALIZED!");
};

Mobilenav.prototype = Object.create(Component.prototype);