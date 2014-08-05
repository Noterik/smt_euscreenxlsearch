var Activefields = function(options){
	console.log("Activefields()");
	Component.apply(this, arguments);
	this.element = jQuery("#activefields");
	this.activeFieldsElement = this.element.find('#tags');
	this.fieldTemplate = this.element.find("#active-field-template").text();
};

Activefields.prototype = Object.create(Component.prototype);
Activefields.prototype.loading = function(loading){
	if(loading == "true"){
		this.element.find('.selectedfilter').addClass('loading')
	}else{
		this.element.find('.selectedfilter').removeClass('loading');
	}
};
Activefields.prototype.setActiveFields = function(data){
	var fields = JSON.parse(data);
	var templateObject = {
		'activeFields': []
	};
	
	for(var categoryName in fields){
		var category = fields[categoryName];
		for(var i = 0; i < category.length; i++){
			templateObject.activeFields.push({
				category: categoryName,
				value: category[i]
			});
		}
	};
	
	this.activeFieldsElement.html(_.template(this.fieldTemplate, templateObject));
	this.activeFieldsElement.find("ul.tag li.action a").off("click").click(function(event){
		var parent = jQuery(this).parent().parent();
		var type = jQuery(this).data('category');
		var value = ""+jQuery(this).data('value');
		
		var objectToSend = {};
		objectToSend[type] = value;
		
		parent.remove();
		
		console.log(objectToSend);
		
		eddie.putLou('', 'removefield(' + JSON.stringify(objectToSend) + ')');
	});
};
