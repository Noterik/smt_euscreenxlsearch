var Results = function(options){	
	Component.apply(this, arguments);
	
	this.element = jQuery("#results");
	this.currentChunk = 0;
	this.template = this.element.find('.result-template').text();
	this.loadingTemplate = this.element.find('.loading-template').text();
	this.list = this.element.find('.result-list');
	this.currentResults = [];
};

Results.prototype = Object.create(Component.prototype);
Results.prototype.chunkSize = 10;
Results.prototype.events = {
	"click button.load-more": function(event){
		this.renderChunk(this.currentChunk+1);
	}
};
Results.prototype.loading = function(loading){
	if(loading && loading != 'false'){
		this.list.html('');
		this.list.append(this.loadingTemplate);
	}else{
		this.list.find('.loading').remove();
	}
};
Results.prototype.setResults = function(results){
	this.currentResults = JSON.parse(results);
		
	this.list.html('');
	
	this.renderChunk(1);
	this.loading(false);
};
Results.prototype.renderChunk = function(chunkNo){
	this.currentChunk = chunkNo;
	var start = (chunkNo - 1) * this.chunkSize;
	var end = start + this.chunkSize;
	for(var i = start; i < end; i++){
		var item = this.currentResults[i];
		if(item){
			var elementStr = _.template(this.template, {item: item});
			this.list.append(elementStr);
		}
	}
};