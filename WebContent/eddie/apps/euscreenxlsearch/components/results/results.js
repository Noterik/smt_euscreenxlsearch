var Results = function(options){	
	console.log("Results()");
	Component.apply(this, arguments);
	var self = this;
	
	this.element = jQuery("#results");
	
	this.pages = {
		"all": {
			currentChunk: 0,
			element: self.element.find('#results-all')
		},
		"video": {
			currentChunk: 0,
			element: self.element.find('#results-videos')
		},
		"picture": {
			currentChunk: 0,
			element: self.element.find('#results-images')
		},
		"audio": {
			currentChunk: 0,
			element: self.element.find('#results-audio')
		},
		"doc": {
			currentChunk: 0,
			element: self.element.find("#results-documents")
		}
	}
	
	this.template = this.element.find('.result-template').text();
	this.loadingTemplate = this.element.find('.loading-template').text();
	this.currentResults = [];
};

Results.prototype = Object.create(Component.prototype);
Results.prototype.chunkSize = 10;
Results.prototype.events = {
	"click .more a": function(event, element){
		event.preventDefault();
		var page = $(event.srcElement).data('page');
		this.renderChunk(this.pages[page].currentChunk + 1, page);
	}
};
Results.prototype.loading = function(loading){
	
};
Results.prototype.clear = function(){
	for(var page in this.pages){
		this.pages[page].currentChunk = 0;
		this.pages[page].element.find('.list').html('');
	}
};
Results.prototype.setResults = function(results){	
	this.clear();
	this.currentResults = JSON.parse(results);
	
	this.renderChunk(1);
	this.loading(false);
};
Results.prototype.renderChunk = function(chunkNo, page){
	console.log("CHUNKNO: " + chunkNo);
	var pagesToUpdate = [];
	
	if(page){
		pagesToUpdate.push(page);
	}else{
		for(var page in this.pages){
			pagesToUpdate.push(page);
		}
	}	
	
	var start = (chunkNo - 1) * this.chunkSize;	
	console.log("START: " + start);
	
	for(var i = 0; i < pagesToUpdate.length; i++){
		var page = pagesToUpdate[i];
				
		this.pages[page].currentChunk = chunkNo;
		
		var end = start + this.chunkSize;
		if(end > this.currentResults[page].length){
			end = this.currentResults[page].length;
			this.pages[page].element.find('.more').hide();
		}else{
			this.pages[page].element.find('.more').show();
		}
				
		for(var c = start; c < end; c++){
			var item = this.currentResults[page][c];
			if(item){
				var elementStr = _.template(this.template, {item: item});
				this.pages[page].element.find('.list').append(elementStr);
			}
		}
	}
};