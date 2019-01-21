/*------------------------------------------*\
 SmartReplace for Xinha
 _______________________
	 
\*------------------------------------------*/

function SmartReplace(editor) {
	this.editor = editor;
	this._wasShowing = false; // Set true when switching to textmode if the dialog is visible, so it can be restored
	var cfg = editor.config;
	var self = this;
	
	cfg.registerButton
	({
		id       : "smartreplace",
		tooltip  : this._lc("SmartReplace"),
		image    : [_editor_url +'iconsets/Tango/ed_buttons_main.png',3,8],
		textMode : false,
		action   : function(e, objname, obj) { self.buttonPress(null, obj); }
	});
	cfg.addToolbarElement("smartreplace", "htmlmode", 1);
}

SmartReplace._pluginInfo = {
  name          : "SmartReplace",
  version       : "1.0",
  developer     : "Raimund Meyer",
  developer_url : "http://x-webservice.net",
  c_owner       : "Raimund Meyer",
  sponsor       : "",
  sponsor_url   : "",
  license       : "htmlArea"
};

SmartReplace.prototype._lc = function(string) {
	return Xinha._lc(string, 'SmartReplace');
};

Xinha.Config.prototype.SmartReplace =
{
	'defaultActive' : true,
	'quotes' : null//[String.fromCharCode(187),String.fromCharCode(171),String.fromCharCode(8250),String.fromCharCode(8249)]
}
SmartReplace.prototype.toggleActivity = function(newState) 
{
	if (typeof newState != 'undefined')
	{
		this.active = newState;
	}
	else
	{
		this.active = this.active ? false : true;		
	}
	this.editor._toolbarObjects.smartreplace.state("active", this.active);
}

SmartReplace.prototype.onUpdateToolbar = function() {
	this.editor._toolbarObjects.smartreplace.state("active", this.active);
}

SmartReplace.prototype.onGenerate = function() {
	this.active = this.editor.config.SmartReplace.defaultActive;
	this.editor._toolbarObjects.smartreplace.state("active", this.active);
	
	var self = this;
	Xinha._addEvent(
		self.editor._doc,
		 "keypress",
		function (event)
		{
		  return self.keyEvent(Xinha.is_ie ? self.editor._iframe.contentWindow.event : event);
		});
	
	var quotes = this.editor.config.SmartReplace.quotes;
   
	if (quotes && typeof quotes == 'object')
	{
		this.openingQuotes = quotes[0];
		this.closingQuotes = quotes[1];
		this.openingQuote  = quotes[2];
		this.closingQuote  = quotes[3];
	}
	else
	{
		this.openingQuotes = this._lc("OpeningDoubleQuotes");
		this.closingQuote  = this._lc("ClosingSingleQuote");
		this.closingQuotes = this._lc("ClosingDoubleQuotes");
		this.openingQuote  = this._lc("OpeningSingleQuote");
	}
	
	if (this.openingQuotes == 'OpeningDoubleQuotes') //If nothing else is defined, English style as default
	{
		this.openingQuotes = String.fromCharCode(8220);
		this.closingQuotes = String.fromCharCode(8221);
		this.openingQuote = String.fromCharCode(8216);
		this.closingQuote = String.fromCharCode(8217);
	}
};

SmartReplace.prototype.keyEvent = function(ev)
{
	if ( !this.active) return true;
	var editor = this.editor;
	var charCode =  Xinha.is_ie ? ev.keyCode : ev.which;

	var key = String.fromCharCode(charCode);

	if ( key == '"' || key == "'")
	{
		Xinha._stopEvent(ev);
		return this.smartQuotes(key);
	}
	if (charCode == 32) //space bar
	{
		return this.smartReplace(ev, 2, /^\s-/, ' '+String.fromCharCode(8211), false); // space-space -> dash 
	}
	if ( key == '.' ) // ... -> ellipsis
	{
		return this.smartReplace(ev, 2, /\.\./, String.fromCharCode(8230), true);
	}
	return true;
}

SmartReplace.prototype.smartQuotes = function(kind)
{
	if (kind == "'")
	{
		var opening = this.openingQuote;
		var closing = this.closingQuote;
	}
	else
	{
		var opening = this.openingQuotes;
		var closing = this.closingQuotes;
	}
	
	var editor = this.editor;
		
	var sel = editor.getSelection();

	if (Xinha.is_ie)
	{
		var r = editor.createRange(sel);
		if (r.text !== '')
		{
			r.text = '';
		}
		r.moveStart('character', -1);
		
		if(r.text.match(/\S/))
		{
			r.moveStart('character', +1);
			r.text = closing;
		}
		else
		{
			r.moveStart('character', +1);
			r.text = opening;
		}
	}
	else
	{
		var r = editor.createRange(sel);

		if (!r.collapsed)
		{
			editor.insertNodeAtSelection(document.createTextNode(''));
		}
		if (r.startOffset > 0) r.setStart(r.startContainer, r.startOffset -1);

		
		if(r.toString().match(/[^\s\xA0]/))
		{;
			r.collapse(false);
			editor.insertNodeAtSelection(document.createTextNode(closing));
		}
		else
		{
			editor.insertNodeAtSelection(document.createTextNode(r.toString()+opening));			
		}
		editor.getSelection().collapseToEnd();
	}
	return false;
}

SmartReplace.prototype.smartReplace = function(ev, lookback, re, replace, stopEvent)
{
	var editor = this.editor;
	var sel = this.editor.getSelection();
	var r = this.editor.createRange(sel);
	
	if (Xinha.is_ie)
	{
		r.moveStart('character', -lookback);
		
		if(r.text.match(re))
		{
			r.text = replace;
			if (stopEvent) 
			{
				Xinha._stopEvent(ev);
				return false
			}
		}
	}
	else
	{
		if (r.startOffset > 1) r.setStart(r.startContainer, r.startOffset -lookback);

		if(r.toString().match(re))
		{
			this.editor.insertNodeAtSelection(document.createTextNode(replace));
			r.deleteContents();
			r.collapse(true);
		  if (stopEvent) 
		  {
				Xinha._stopEvent(ev);
				return false
		  }
		}
		editor.getSelection().collapseToEnd();
	}
	return true;
}

SmartReplace.prototype.replaceAll = function()
{
	var doubleQuotes = [
							'&quot;',
							String.fromCharCode(8220),
							String.fromCharCode(8221),
							String.fromCharCode(8222),
							String.fromCharCode(187),
							String.fromCharCode(171)
							
						];
	var singleQuotes = [
							"'",
							String.fromCharCode(8216),
							String.fromCharCode(8217),
							String.fromCharCode(8218),
							String.fromCharCode(8250),
							String.fromCharCode(8249)
						];
	
	var html = this.editor.getHTML();
	var reOpeningDouble = new RegExp ('(\\s|^|>)('+doubleQuotes.join('|')+')(\\S)','g');
	html = html.replace(reOpeningDouble,'$1'+this.openingQuotes+'$3');
	
	var reOpeningSingle = new RegExp ('(\\s|^|>)('+singleQuotes.join('|')+')(\\S)','g');
	html = html.replace(reOpeningSingle,'$1'+this.openingQuote+'$3');
	
	var reClosingDouble = new RegExp ('(\\S)('+doubleQuotes.join('|')+')','g');
	html = html.replace(reClosingDouble,'$1'+this.closingQuotes);
	
	var reClosingSingle = new RegExp ('(\\S)('+singleQuotes.join('|')+')','g');
	html = html.replace(reClosingSingle,'$1'+this.closingQuote);
	
	var reDash    = new RegExp ('( |&nbsp;)(-)( |&nbsp;)','g');
	html = html.replace(reDash,' '+String.fromCharCode(8211)+' ');
	
	this.editor.setHTML(html);
}

SmartReplace.prototype.buttonPress = function(opts, obj)
{
	var self = this;

	if ( this.dialog.rootElem.style.display != 'none')
	{
		return this.dialog.hide();
	}
	var doOK = function()
	{
		var opts = self.dialog.hide();
		self.toggleActivity((opts.enable) ? true : false); 
		if (opts.convert)
		{
			self.replaceAll();
			self.dialog.getElementById("convert").checked = false;
		}
	}
	var inputs = 
	{
		enable : self.active ? "on" : '',
		convert: ''
	};
	this.show(inputs, doOK);
};

SmartReplace.prototype.onGenerateOnce = function()
{
  this._prepareDialog();
};

SmartReplace.prototype._prepareDialog = function()
{
  var self = this;
  var editor = this.editor;

  if(!this.html)
  {
    Xinha._getback( Xinha.getPluginDir("SmartReplace") + '/dialog.html', function(getback) { self.html = getback; self._prepareDialog(); });
	return;
  }
  
  // Now we have everything we need, so we can build the dialog.
  this.dialog = new Xinha.Dialog(editor, this.html, 'SmartReplace',{},{modal:false});
  this.dialog.attachToPanel('top');

  this.dialog.getElementById('enable').onchange = function ()
  {
  	self.toggleActivity(this.checked); 
  }
  this.dialog.getElementById('convert').onchange = function ()
  {
  	self.dialog.getElementById('ok').style.display = ( this.checked ) ? '' : 'none'; 
  }
  this.dialog.getElementById('ok').onclick = function ()
  {
  	self.replaceAll();
  	self.dialog.getElementById('convert').checked = false; 
  	this.style.display =  'none'; 
  }
  this.ready = true;
};

SmartReplace.prototype.show = function(inputs)
{
  if(!this.ready)
  {
    var self = this;
    window.setTimeout(function() {self.show(inputs,ok,cancel);},100);
	return;
  }

  // Connect the OK and Cancel buttons
  var self = this;

  this.dialog.show(inputs);

  // Init the sizes
  this.dialog.onresize();
};

SmartReplace.prototype.onBeforeMode = function(mode)
{
  switch(mode)
  {
    case 'textmode':      
      if ( this.dialog.rootElem.style.display != 'none')
      {
        this._wasShowing = true;
        this.dialog.hide();
      }
      else
      {
        this._wasShowing = false;
      }
      break;
      
    case 'wysiwyg':
      if(this._wasShowing)
      {
        this.buttonPress();
      }
      break;
  }
}