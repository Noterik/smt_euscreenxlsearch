var Activefields = function(options){
	console.log("Activefields()");
	Component.apply(this, arguments);
	
	var self = this;
	this.element = jQuery("#activefields");
	this.activeFieldsElement = this.element.find('#tags');
	this.fieldTemplate = this.element.find("#active-field-template").text();
	this.clearButton = this.element.find('.clear');
	
	this.clearButton.on('click', function(){
		self.clear();
	});
};

Activefields.prototype = Object.create(Component.prototype);
Activefields.prototype.loading = function(loading){
	if(loading == "true"){
		this.element.find('.selectedfilter').addClass('loading')
	}else{
		this.element.find('.selectedfilter').removeClass('loading');
	}
};
Activefields.prototype.clear = function(){
	this.activeFieldsElement.html('');
	this.element.addClass('hidden');
	eddie.putLou('', 'clearFields()');
};
Activefields.prototype.setActiveFields = function(data){
	console.log("Activefields.prototype.setActiveFields(" + data + ")");
	var self = this;
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
	
	if(templateObject.activeFields.length > 0){
		this.clearButton.removeClass('hidden');
		this.element.removeClass('hidden');
		this.activeFieldsElement.html(_.template(this.fieldTemplate, templateObject));
		this.activeFieldsElement.find("ul.tag li.action a").off("click").click(function(event){
			var parent = jQuery(this).parent().parent();
			var type = jQuery(this).data('category');
			var value = ""+jQuery(this).data('value');
			
			var objectToSend = {};
			objectToSend[type] = value;
			
			parent.remove();
			
			if(self.activeFieldsElement.find("ul.tag li.action a").length == 0){
				self.element.addClass('hidden');
			}
			
			eddie.putLou('', 'removefield(' + JSON.stringify(objectToSend) + ')');
		});
	}
	
};
