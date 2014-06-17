var Resulttopbar = function(options){
	this.element = jQuery("#resulttopbar");
};

Resulttopbar.prototype = Object.create(Component.prototype);
Resulttopbar.prototype.show = function(){
	this.element.show();
};
Resulttopbar.prototype.hide = function(){
	this.element.hide();
}