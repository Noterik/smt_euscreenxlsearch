var Template = function () {
	console.log("Template()");
    Component.apply(this, arguments);
    var self = this;

    this.extraFilters.hide(); //Bootstrap seems to override the display: none on mobile devices. Hide it again, by doing this.
    
 // sticky sidebar
    this.sidebar.stickySidebarJS({
     	followParent : '#right',
     	device : "desktop",
     	sidebarHeight : $(window).height() - $('.navbar').height(),
     	bottom : 120,
     	startClass : 'fix',
     	endClass : ''
     }); 
    
    var offset = 0;
    
    jQuery(window).resize(function(){
    	self.positionSidebar();
    });
    jQuery(window).on('scroll', function(){
    	self.positionSidebar();
    });
    
    setTimeout(function(){
    	self.positionSidebar();
    }, 250);
};

Template.prototype = Object.create(Component.prototype);
Template.prototype.name = "search-results";
Template.prototype.extraFilters = jQuery(".extra-option");
Template.prototype.sidebar = jQuery('#sidebar');
Template.prototype.showExtraFiltersButton = jQuery("#show-extra-filters");
Template.prototype.hideExtraFiltersButton = jQuery("#hide-extra-filters");
Template.prototype.positionSidebar = function(){
	console.log("positionSidebar()");
	var self = this;
	if(this.sidebar[0]){
		
		var scrollTop = jQuery(window).scrollTop();
		var documentHeight = jQuery(document).height();
		var windowHeight = jQuery(window).height();
		var moreButton = self.sidebar.find('.more');
		
		var offset = jQuery('#header .navbar').height() + moreButton.outerHeight(true) - 15;
		var menuBiggerThenWrapper = false;
		
		if(jQuery('#footer')[0]){
			var footerHeight = jQuery('#footer').height();
			var footerPosition = jQuery('#footer').offset();
						
			if(scrollTop == 0 && footerPosition.top < windowHeight){
				offset += footerHeight;
				self.sidebar.height(windowHeight - footerHeight)
			}else{
				var amountLeft = documentHeight - windowHeight - scrollTop;
				if(amountLeft < footerHeight){
					self.sidebar.css('margin-top', -(footerHeight - amountLeft));
				}else{
					self.sidebar.css('margin-top', 0);
				}
			}
		}
		
		self.sidebar.height(jQuery(window).height() - offset);
	}else{
		var timeout = setTimeout(function(){
			self.positionSidebar();
		}, 100);
	}
};
Template.prototype.events = {
    "click #show-extra-filters": function (event) {
        this.extraFilters.show();
        this.showExtraFiltersButton.hide();
        this.hideExtraFiltersButton.show();
    },
    "click #hide-extra-filters": function (event) {
        this.extraFilters.hide();
        this.hideExtraFiltersButton.hide();
        this.showExtraFiltersButton.show();
    }
};