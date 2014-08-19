/*
    stickySidebarJS
*/
(function($) {
	$.fn.stickySidebarJS = function(customSettings) {
		
		// settings
		var settings = $.extend({
            followParent : '#main',
        	device : 'desktop',
        	sidebarHeight : 400,
        	bottom : 0,
        	startClass : 'fix',
        	endClass : 'fix-bottom'             
        }, customSettings || {});
        var obj = this;
        
        // init
        var init = function() {
        	
        	// add class
        	addDefaultClass();
        	
        	// set height on desktop & mobile
        	setFloatingHeight();
        };
        
        
        // add default class
        var addDefaultClass = function(){
	        obj.addClass(settings.startClass);
        	$(settings.followParent).addClass(settings.startClass);
        };
        
        // setFloatingHeight
        var setFloatingHeight = function(){
	        if(settings.device != 'mobile') {
		        obj.css('height',settings.sidebarHeight+"px");
	        }
        }
        
        // scroll
        $(window).bind("scroll", function(event) {
        	if($(this).scrollTop() + $(this).height() > $(document).height() - settings.bottom) {
		    	if(!obj.hasClass('fix-bottom')) {
		    		//obj.addClass('fix-bottom');
		    	}
		    } else {
		    	if(obj.hasClass('fix-bottom')) {
				   //obj.removeClass('fix-bottom');
		    	}
		    }
        });
        
        // init
        init();
	};
})(jQuery);