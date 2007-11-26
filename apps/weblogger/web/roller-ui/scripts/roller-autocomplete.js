RollerTagsAutoCompleter = Class.create();
Object.extend(Object.extend(RollerTagsAutoCompleter.prototype, Ajax.Autocompleter.prototype), {

	initialize: function(element, update, url, options) {
		this.baseInitialize(element, update, options);
	    this.options.asynchronous  = true;
	    this.options.onComplete    = this.onComplete.bind(this)
	    this.options.method   = 'get';
	    this.options.defaultParams = 'page=0';
	    this.url    = url;
	    this.cache = {};
	    this.options.mine = options.mine || [];
	  },
	
  getUpdatedChoices: function() {
  	
  	var t = this.getToken();
  	if (this.cache[t])
  	{
  		this.updateChoices(this.cache[t]);
  		return;
  	}
  	
	this.options.parameters = this.options.callback ?
	  this.options.callback(this.element, entry) : "";

	//if(this.options.defaultParams) 
	  this.options.parameters += '&' + this.options.defaultParams;
	  this.options.parameters += "&format=json";
	  this.options.parameters += "&prefix=" + encodeURIComponent(t);

	new Ajax.Request(this.url, this.options);
  },

	onComplete: function(request) {
		eval("var results = " + request.responseText + ";");
		var buf = "<ul>\n";
		for (var i = 0; i < results.tagcounts.length; i++)
		{
			var tc = results.tagcounts[i];						
			buf += "<li>" + tc.tag + "</li>\n";
		}
		buf += "</ul>";
		this.cache[results.prefix] = buf;
		this.updateChoices(buf);
	}

});