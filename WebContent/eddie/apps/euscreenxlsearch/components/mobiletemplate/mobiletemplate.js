var Mobiletemplate = function(){
	Page.apply(this, arguments);
	console.log("Mobiletemplate()");
	
	this.loadStylesheet("/eddie/apps/euscreenxlelements/css/bootstrap.css");
	this.loadStylesheet("/eddie/apps/euscreenxlelements/css/theme.css");
	this.loadStylesheet("/eddie/apps/euscreenxlelements/css/all.css");
	this.loadStylesheet("/eddie/apps/euscreenxlelements/css/terms.css");
};

Mobiletemplate.prototype = Object.create(Page.prototype);