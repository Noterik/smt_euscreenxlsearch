var Mobileresults = function(options){
	Component.apply(this, arguments);
	
	this.element = jQuery("#mobileresults");
	this.list = this.element.find('.list');
	this.moreButton = this.element.find('.more');
	this.template = this.element.find('.result-template').text();
};

Mobileresults.prototype = Object.create(Component.prototype);
Mobileresults.prototype.visibleType = "all";
Mobileresults.prototype.events = {
	"click #mobileresults .more a": function(event, element){
		event.preventDefault();
		this.renderChunk(this.currentChunk + 1);
	}
};
Mobileresults.prototype.chunkSize = 10;
Mobileresults.prototype.currentChunk = 0;
Mobileresults.prototype.currentResults = null;
Mobileresults.prototype.setVisibleType = function(visibleType){
	console.log("Mobileresults.setVisibleType(" + visibleType + ")");
	this.visibleType = visibleType;
	this.currentChunk = 0;
	this.clear();
	this.renderChunk(1);
}
Mobileresults.prototype.setResults = function(results){	
	console.log("Mobileresults.setResults()");
	var results = JSON.parse(results);
		
	this.clear();
	this.currentResults = results;
	
	this.renderChunk(1);
};
Mobileresults.prototype.clear = function(){
	this.list.html('');
};
Mobileresults.prototype.renderChunk = function(chunkNo){
	this.currentChunk = chunkNo;
	var start = (this.currentChunk - 1) * this.chunkSize;	
	
	var end = start + this.chunkSize;
	if(end > this.currentResults[this.visibleType].length){
		end = this.currentResults[this.visibleType].length;
		this.moreButton.hide();
	}else{
		this.moreButton.show();
	}
	
	console.log("start:" + start);
	console.log("end:" + end);
			
	for(var c = start; c < end; c++){
		var item = this.currentResults[this.visibleType][c];
		console.log(item);
		if(item){
			var elementStr = _.template(this.template, {item: item});
			var itemElement = jQuery(elementStr);
			
			this.list.append(itemElement);
		}
	}
};