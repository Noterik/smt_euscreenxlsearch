var History = function(){
	Component.apply(this, arguments);
	
	this.parameters = {};
};

History.prototype = Object.create(Component.prototype);
History.prototype.setStartupParameters = function(parameters){
	this.parameters = JSON.parse(parameters);
	console.log(this.parameters);
}
History.prototype.setParameter = function(parameter){
	parameter = JSON.parse(parameter);
	for(var name in parameter){
		this.parameters[name] = parameter[name];
	}
	this.createURL();
};
History.prototype.createURL = function(){
	console.log("createURL");
	if(window.history){
		var queryStr = "";
		var i = 0;
		for(var name in this.parameters){
			if(i == 0){
				queryStr += "?";
			}else{
				queryStr += "&";
			}
			queryStr += encodeURIComponent(name) + "=" + encodeURI(this.parameters[name]);
			i++;
		}
		history.pushState(null, null, queryStr);
	}
};