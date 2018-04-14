/*------------------------------------------*\
 AsciiMathML Formula Editor for Xinha
 _______________________
 
 Based on AsciiMathML by Peter Jipsen http://www.chapman.edu/~jipsen
 
 Including a table with math symbols for easy input modified from CharacterMap for ASCIIMathML by Peter Jipsen
 HTMLSource based on HTMLArea XTD 1.5 (http://mosforge.net/projects/htmlarea3xtd/) modified by Holger Hees
 Original Author - Bernhard Pfeifer novocaine@gmx.net
 
 See readme.txt
 
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation; either version 2.1 of the License, or (at
 your option) any later version.

 This program is distributed in the hope that it will be useful, 
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License (at http://www.gnu.org/licenses/lgpl.html) 
 for more details.

 Raimund Meyer  11/23/2006
     
\*------------------------------------------*/
function Equation(editor) {
	this.editor = editor;

	var cfg = editor.config;
	var self = this;
	

	// register the toolbar buttons provided by this plugin
	cfg.registerButton({
	id       : "equation",
	tooltip  : this._lc("Formula Editor"),
	image    : editor.imgURL("equation.gif", "Equation"),
	textMode : false,
	action   : function(editor, id) {
			self.buttonPress(editor, id);
		}
	});
	cfg.addToolbarElement("equation", "inserthorizontalrule", -1);
	
	mathcolor = cfg.Equation.mathcolor;       // change it to "" (to inherit) or any other color
	mathfontfamily = cfg.Equation.mathfontfamily;
	
	this.enabled = !Xinha.is_ie;
	
	if (this.enabled)
	{	
		this.onBeforeSubmit = this.onBeforeUnload = function () {self.unParse();};
	}
	
	if (typeof  AMprocessNode != "function")
	{
		Xinha._loadback(Xinha.getPluginDir('Equation') + "/ASCIIMathML.js", function () { translate(); });
	}
}

Xinha.Config.prototype.Equation =
{
	"mathcolor" : "black",       // change it to "" (to inherit) or any other color
	"mathfontfamily" : "serif" // change to "" to inherit (works in IE) 
                               // or another family (e.g. "arial")
}

Equation._pluginInfo = {
	name          : "ASCIIMathML Formula Editor",
	version       : "2.3 (2008-01-26)",
	developer     : "Raimund Meyer",
	developer_url : "http://x-webservice.net",
	c_owner       : "",
	sponsor       : "",
	sponsor_url   : "",
	license       : "GNU/LGPL"
};

Equation.prototype._lc = function(string) 
{
    return Xinha._lc(string, 'Equation');
};
Equation.prototype.onGenerate = function() 
{
	this.parse();
};

// avoid changing the formula in the editor
Equation.prototype.onKeyPress = function(ev)
{
	if (this.enabled)
	{
		e = this.editor;
		var span = e._getFirstAncestor(e.getSelection(),['span']);
		if ( span && span.className == "AM" )
		{
			if (
				ev.keyCode == 8 || // delete
				ev.keyCode == 46 ||// backspace
				ev.charCode	       // all character keys
			) 
			{ // stop event
				Xinha._stopEvent(ev);
				return true; 
			}
		}
	}
	return false;
}
Equation.prototype.onBeforeMode = function( mode )
{
	if (this.enabled && mode == 'textmode')
	{
		this.unParse();
	}
}
Equation.prototype.onMode = function( mode )
{
	if (this.enabled && mode == 'wysiwyg')
	{
		this.parse();
	}
}

Equation.prototype.parse = function ()
{
	if (this.enabled)
	{
		var doc = this.editor._doc;
		var spans = doc.getElementsByTagName("span");
		for (var i = 0;i<spans.length;i++)
		{
			var node = spans[i];
			if (node.className != 'AM') continue;
			if (node.innerHTML.indexOf(this.editor.cc) != -1) // avoid problems with source code position auxiliary character
			{
				node.innerHTML = node.innerHTML.replace(this.editor.cc,'');
				node.parentNode.insertBefore(doc.createTextNode(this.editor.cc), node);
			}
			node.title = node.innerHTML;
			// FF3 strict source document policy: 
			// the span is taken from the editor document, processed in the plugin document, and put back in the editor
			var clone = node.cloneNode(true);
			try {
				document.adoptNode(clone);
			} catch (e) {}
			AMprocessNode(clone, false);
			try {
				doc.adoptNode(clone);
			} catch (e) {}
			node.parentNode.replaceChild(clone, node);
			// insert space before and after the protected node, otherwide one could get stuck
			clone.parentNode.insertBefore(doc.createTextNode(String.fromCharCode(32)),clone);
			clone.parentNode.insertBefore(doc.createTextNode(String.fromCharCode(32)),clone.nextSibling);
		}
	}
}

Equation.prototype.unParse = function ()
{
	var doc = this.editor._doc;
	var spans = doc.getElementsByTagName("span");
	for (var i = 0;i<spans.length;i++)
	{
		var node = spans[i];
		if (node.className.indexOf ("AM") == -1 || node.getElementsByTagName("math").length == 0) continue;
		var formula = '`' + node.getElementsByTagName('math')[0].getAttribute('title') + '`';
		node.innerHTML = formula;
		node.setAttribute("title", null);
	}
}

Equation.prototype.buttonPress = function() 
{
	var self = this;
	var editor = this.editor;
	var args = {};
	
	args['editor'] = editor;
	
	var parent = editor._getFirstAncestor(editor.getSelection(),['span']);
	if (parent)
	{
		args["editedNode"] = parent;
	}
	Dialog(Xinha.getPluginDir('Equation') + "/popups/dialog.html", function(params) {
				self.insert(params);
			}, args);
};

Equation.prototype.insert = function (param)
{
	if (typeof param == 'undefined' || param == null) return;

	if (typeof param["formula"] != "undefined")
	{
		var formula = (param["formula"] != '') ? param["formula"].replace(/^`?(.*)`?$/m,"`$1`") : '';

		if (param["editedNode"] && (param["editedNode"].tagName.toLowerCase() == 'span')) 
		{
			var span = param["editedNode"]; 
			if (formula != '')
			{
				span.innerHTML = formula;
				if (this.enabled) span.title = formula;
			}
			else
			{
				span.parentNode.removeChild(span);
			}
			
		}
		else if (!param["editedNode"] && formula != '')
		{
			if (this.enabled)
			{			
				var span = document.createElement('span');
				span.className = 'AM';
				this.editor.insertNodeAtSelection(span);
				span.innerHTML = formula;
				span.title = formula;
			}
			else
			{
				this.editor.insertHTML('<span class="AM">'+formula+'</span>');
			}
		}

		if (this.enabled) this.parse();//AMprocessNode(this.editor._doc.body, false);
	}
}
