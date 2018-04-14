// Context Menu Plugin for HTMLArea-3.0
// Sponsored by www.americanbible.org
// Implementation by Mihai Bazon, http://dynarch.com/mishoo/
//
// (c) dynarch.com 2003.
// Distributed under the same terms as HTMLArea itself.
// This notice MUST stay intact for use (see license.txt).
//
// $Id: ContextMenu.js 1402 2018-02-19 07:35:49Z gogo $

Xinha.loadStyle("menu.css", "ContextMenu");

function ContextMenu(editor) {
	this.editor = editor;
}

ContextMenu._pluginInfo = {
	name          : "ContextMenu",
	version       : "1.0",
	developer     : "Mihai Bazon",
	developer_url : "http://dynarch.com/mishoo/",
	c_owner       : "dynarch.com",
	sponsor       : "American Bible Society",
	sponsor_url   : "http://www.americanbible.org",
	license       : "htmlArea"
};

Xinha.Config.prototype.ContextMenu = {
	disableMozillaSpellCheck : false,
  customHooks : { } // 'a': [ ['Label', function() { alert('Action'); }, 'Tooltip', '/icon.jpg' ] ]
}

ContextMenu.prototype.onGenerate = function() {
	var self = this;
	var doc = this.editordoc = this.editor._iframe.contentWindow.document;
	Xinha._addEvents(doc, ["contextmenu"],
			    function (event) {
				    return self.popupMenu(Xinha.is_ie ? self.editor._iframe.contentWindow.event : event);
			    });
	this.currentMenu = null;
	
	if (this.editor.config.ContextMenu.disableMozillaSpellCheck) {
		this.editordoc.body.spellcheck = false; // Firefox spellchecking is quite confusing for the user when they don't get the browser context menu
	}
};

ContextMenu.prototype.getContextMenu = function(target) {
	var self = this;
	var editor = this.editor;
	var config = editor.config;
	var menu = [];
	var tbo = this.editor.plugins.TableOperations;
	if (tbo) tbo = tbo.instance;
	var selection = editor.hasSelectedText();
	if (!Xinha.is_gecko) {
		if (selection) {
			menu.push([ Xinha._lc("Cut", "ContextMenu"), function() { editor.execCommand("cut"); }, null, config.btnList["cut"][1] ],
				  [ Xinha._lc("Copy", "ContextMenu"), function() { editor.execCommand("copy"); }, null, config.btnList["copy"][1] ]);
			menu.push([ Xinha._lc("Paste", "ContextMenu"), function() { editor.execCommand("paste"); }, null, config.btnList["paste"][1] ]);
		}
	}
	var currentTarget = target;
	var elmenus = [];

	var link = null;
	var table = null;
	var tr = null;
	var td = null;
	var img = null;

	function tableOperation(opcode) {
		tbo.buttonPress(editor, opcode);
	}

	function insertPara(after) {
		var el = currentTarget;
		var par = el.parentNode;
		var p = editor._doc.createElement("p");
		p.appendChild(editor._doc.createElement("br"));
		par.insertBefore(p, after ? el.nextSibling : el);
		var sel = editor._getSelection();
		var range = editor._createRange(sel);
		if (!Xinha.is_ie) {
			sel.removeAllRanges();
			range.selectNodeContents(p);
			range.collapse(true);
			sel.addRange(range);
		} else {
			range.moveToElementText(p);
			range.collapse(true);
			range.select();
		}
	}

	for (; target; target = target.parentNode) {
		var tag = target.tagName;
		if (!tag)
			continue;
		tag = tag.toLowerCase();
		switch (tag) {
		    case "img":
			img = target;
			elmenus.push(null,
				     [ Xinha._lc("_Image Properties...", "ContextMenu"),
				       function() {
					       editor._insertImage(img);
				       },
				       Xinha._lc("Show the image properties dialog", "ContextMenu"),
				       config.btnList["insertimage"][1] ]
				);
			break;
		    case "a":
			link = target;
			elmenus.push(null,
				     [ Xinha._lc("_Modify Link...", "ContextMenu"),
               function() { editor.config.btnList['createlink'][3](editor); },
				       Xinha._lc("Current URL is", "ContextMenu") + ': ' + link.href,
				       config.btnList["createlink"][1] ],

				     [ Xinha._lc("Chec_k Link...", "ContextMenu"),
				       function() { window.open(link.href); },
				       Xinha._lc("Opens this link in a new window", "ContextMenu") ],

				     [ Xinha._lc("_Remove Link...", "ContextMenu"),
				       function() {
					       if (confirm(Xinha._lc("Please confirm that you want to unlink this element.", "ContextMenu") + "\n" +
							   Xinha._lc("Link points to:", "ContextMenu") + " " + link.href)) {
						       while (link.firstChild)
							       link.parentNode.insertBefore(link.firstChild, link);
						       link.parentNode.removeChild(link);
					       }
				       },
				       Xinha._lc("Unlink the current element", "ContextMenu") ]
				);
			break;
        case "th":
		    case "td":
			td = target;
			if (!tbo) break;
			elmenus.push(null);
      if(typeof config.btnList["TO-cell-prop"] != 'undefined')
      {
				     elmenus.push([ Xinha._lc("C_ell Properties...", "ContextMenu"),
				       function() { tableOperation("TO-cell-prop"); },
				       Xinha._lc("Show the Table Cell Properties dialog", "ContextMenu"),
				       config.btnList["TO-cell-prop"][1] ]);
      }
      
      if(typeof config.btnList["TO-cell-insert-after"] != 'undefined')
      {
             elmenus.push(
             [ Xinha._lc("Insert Cell After", "ContextMenu"),
				       function() { tableOperation("TO-cell-insert-after"); },
				       Xinha._lc("Insert Cell After", "ContextMenu"),
				       config.btnList["TO-cell-insert-after"][1] ]);
      }
      
      if(typeof config.btnList["TO-cell-insert-before"] != 'undefined')
      {
             elmenus.push([ Xinha._lc("Insert Cell Before", "ContextMenu"),
				       function() { tableOperation("TO-cell-insert-before"); },
				       Xinha._lc("Insert Cell After", "ContextMenu"),
				       config.btnList["TO-cell-insert-before"][1] ]);
      }
      
      if(typeof config.btnList["TO-cell-delete"] != 'undefined')
      {
             elmenus.push([ Xinha._lc("Delete Cell", "ContextMenu"),
				       function() { tableOperation("TO-cell-delete"); },
				       Xinha._lc("Delete Cell", "ContextMenu"),
				       config.btnList["TO-cell-delete"][1] ]);
      }
      
      if(typeof config.btnList["TO-cell-merge"] != 'undefined')
      {
             elmenus.push([ Xinha._lc("Merge Cells", "ContextMenu"),
				       function() { tableOperation("TO-cell-merge"); },
				       Xinha._lc("Merge Cells", "ContextMenu"),
				       config.btnList["TO-cell-merge"][1] ]
				);
      }
			break;
		    case "tr":
			tr = target;
			if (!tbo) break;
			elmenus.push(null);
      
      if(typeof config.btnList["TO-row-prop"] != 'undefined')
      {
             elmenus.push([ Xinha._lc("Ro_w Properties...", "ContextMenu"),
				       function() { tableOperation("TO-row-prop"); },
				       Xinha._lc("Show the Table Row Properties dialog", "ContextMenu"),
				       config.btnList["TO-row-prop"][1] ]);
      }
      
      if(typeof config.btnList["TO-row-insert-above"] != 'undefined')
      {
             elmenus.push([ Xinha._lc("I_nsert Row Before", "ContextMenu"),
				       function() { tableOperation("TO-row-insert-above"); },
				       Xinha._lc("Insert a new row before the current one", "ContextMenu"),
				       config.btnList["TO-row-insert-above"][1] ]);
      }
      
      if(typeof config.btnList["TO-row-insert-under"] != 'undefined')
      {
             elmenus.push([ Xinha._lc("In_sert Row After", "ContextMenu"),
				       function() { tableOperation("TO-row-insert-under"); },
				       Xinha._lc("Insert a new row after the current one", "ContextMenu"),
				       config.btnList["TO-row-insert-under"][1] ]);
      }
      
      if(typeof config.btnList["TO-row-delete"] != 'undefined')
      {
             elmenus.push([ Xinha._lc("_Delete Row", "ContextMenu"),
				       function() { tableOperation("TO-row-delete"); },
				       Xinha._lc("Delete the current row", "ContextMenu"),
				       config.btnList["TO-row-delete"][1] ]
				);
      }
			break;
		    case "table":
			table = target;
			if (!tbo) break;
			elmenus.push(null);
      
      if(typeof config.btnList["TO-table-prop"] != 'undefined')
      {
             elmenus.push([ Xinha._lc("_Table Properties...", "ContextMenu"),
				       function() { tableOperation("TO-table-prop"); },
				       Xinha._lc("Show the Table Properties dialog", "ContextMenu"),
				       config.btnList["TO-table-prop"][1] ]);
      }
      
      if(typeof config.btnList["TO-col-insert-before"] != 'undefined')
      {
             elmenus.push([ Xinha._lc("Insert _Column Before", "ContextMenu"),
				       function() { tableOperation("TO-col-insert-before"); },
				       Xinha._lc("Insert a new column before the current one", "ContextMenu"),
				       config.btnList["TO-col-insert-before"][1] ]);
      }
      
      if(typeof config.btnList["TO-col-insert-after"] != 'undefined')
      {
             elmenus.push([ Xinha._lc("Insert C_olumn After", "ContextMenu"),
				       function() { tableOperation("TO-col-insert-after"); },
				       Xinha._lc("Insert a new column after the current one", "ContextMenu"),
				       config.btnList["TO-col-insert-after"][1] ]);
      }
      
      if(typeof config.btnList["TO-col-delete"] != 'undefined')
      {
             elmenus.push([ Xinha._lc("De_lete Column", "ContextMenu"),
				       function() { tableOperation("TO-col-delete"); },
				       Xinha._lc("Delete the current column", "ContextMenu"),
				       config.btnList["TO-col-delete"][1] ]
				);
      }
			break;
		    case "body":
			elmenus.push(null,
				     [ Xinha._lc("Justify Left", "ContextMenu"),
				       function() { editor.execCommand("justifyleft"); }, null,
				       config.btnList["justifyleft"][1] ],
				     [ Xinha._lc("Justify Center", "ContextMenu"),
				       function() { editor.execCommand("justifycenter"); }, null,
				       config.btnList["justifycenter"][1] ],
				     [ Xinha._lc("Justify Right", "ContextMenu"),
				       function() { editor.execCommand("justifyright"); }, null,
				       config.btnList["justifyright"][1] ],
				     [ Xinha._lc("Justify Full", "ContextMenu"),
				       function() { editor.execCommand("justifyfull"); }, null,
				       config.btnList["justifyfull"][1] ]
				);
			break;
		}
	}

	// If there is a selection, and not a link, image, or multiple words
	//  then this may be a misspelled word, cancel the context menu so that
	//  the browser's default will appear and offer suggestions
	if (selection && !link && !img && !editor.getSelectedHTML().replace(/<[^>]+>/, '').match(/\s/))
  {
    return false;
  }
	
	if (selection && !link)
		menu.push(null, [ Xinha._lc("Make lin_k...", "ContextMenu"),
           function() { editor.config.btnList['createlink'][3](editor); },
				  Xinha._lc("Create a link", "ContextMenu"),
				  config.btnList["createlink"][1] ]);

  if(editor.config.ContextMenu.customHooks[currentTarget.tagName.toLowerCase()]) 
  { 
    var items = editor.config.ContextMenu.customHooks[currentTarget.tagName.toLowerCase()]; 
    
    for (var i = 0; i < items.length; ++i)
    {
      menu.push(items[i]); 
    }
  }

	for (var i = 0; i < elmenus.length; ++i)
		menu.push(elmenus[i]);

	if (!/html|body/i.test(currentTarget.tagName))
		menu.push(null,
			  [ Xinha._lc("Remove the $elem Element...", 'ContextMenu', {elem: "&lt;" + currentTarget.tagName + "&gt;"}),
			    function() {
				    if (confirm(Xinha._lc("Please confirm that you want to remove this element:", "ContextMenu") + " " +
						currentTarget.tagName)) {
					    var el = currentTarget;
					    var p = el.parentNode;
					    p.removeChild(el);
					    if (Xinha.is_gecko) {
						    if (p.tagName.toLowerCase() == "td" && !p.hasChildNodes())
							    p.appendChild(editor._doc.createElement("br"));
						    editor.forceRedraw();
						    editor.focusEditor();
						    editor.updateToolbar();
						    if (table) {
							    var save_collapse = table.style.borderCollapse;
							    table.style.borderCollapse = "collapse";
							    table.style.borderCollapse = "separate";
							    table.style.borderCollapse = save_collapse;
						    }
					    }
				    }
			    },
			    Xinha._lc("Remove this node from the document", "ContextMenu") ],
			  [ Xinha._lc("Insert paragraph before", "ContextMenu"),
			    function() { insertPara(false); },
			    Xinha._lc("Insert a paragraph before the current node", "ContextMenu") ],
			  [ Xinha._lc("Insert paragraph after", "ContextMenu"),
			    function() { insertPara(true); },
			    Xinha._lc("Insert a paragraph after the current node", "ContextMenu") ]
			  );
	if (!menu[0]) menu.shift(); //If the menu begins with a separator, remove it for cosmetical reasons
	return menu;
};

ContextMenu.prototype.popupMenu = function(ev) {
	var self = this;
	if (this.currentMenu)
	{
		this.closeMenu();
	}
	function getPos(el) {
		var r = { x: el.offsetLeft, y: el.offsetTop };
		if (el.offsetParent) {
			var tmp = getPos(el.offsetParent);
			r.x += tmp.x;
			r.y += tmp.y;
		}
		return r;
	}
	function documentClick(ev) {
		ev || (ev = window.event);
		if (!self.currentMenu) {
			alert(Xinha._lc("How did you get here? (Please report!)", "ContextMenu"));
			return false;
		}
		var el = Xinha.is_ie ? ev.srcElement : ev.target;
		for (; el != null && el != self.currentMenu; el = el.parentNode);
		if (el == null)
			self.closeMenu();
		//Xinha._stopEvent(ev);
		//return false;
	}
	var keys = [];
	function keyPress(ev) {
		ev || (ev = window.event);
		Xinha._stopEvent(ev);
		if (ev.keyCode == 27) {
			self.closeMenu();
			return false;
		}
		var key = String.fromCharCode(Xinha.is_ie ? ev.keyCode : ev.charCode).toLowerCase();
		for (var i = keys.length; --i >= 0;) {
			var k = keys[i];
			if (k[0].toLowerCase() == key)
				k[1].__msh.activate();
		}
	}
	self.closeMenu = function() {
		self.currentMenu.parentNode.removeChild(self.currentMenu);
		self.currentMenu = null;
		Xinha._removeEvent(document, "mousedown", documentClick);
		Xinha._removeEvent(self.editordoc, "mousedown", documentClick);
		if (keys.length > 0)
			Xinha._removeEvent(self.editordoc, "keypress", keyPress);
		if (Xinha.is_ie)
			self.iePopup.hide();
	}
	var target = Xinha.is_ie ? ev.srcElement : ev.target;
     var ifpos = getPos(self.editor._htmlArea);//_iframe);
	var x = ev.clientX + ifpos.x;
	var y = ev.clientY + ifpos.y;

	var div;
	var doc;
	if (!Xinha.is_ie) {
		doc = document;
	} else {
		// IE stinks
		var popup = this.iePopup = window.createPopup();
		doc = popup.document;
		doc.open();
		doc.write("<html><head><style type='text/css'>@import url(" + Xinha.getPluginDir('ContextMenu') + "/menu.css); html, body { padding: 0px; margin: 0px; overflow: hidden; border: 0px; }</style></head><body unselectable='yes'></body></html>");
		doc.close();
	}
	div = doc.createElement("div");
	if (Xinha.is_ie)
		div.unselectable = "on";
	div.oncontextmenu = function() { return false; };
	div.className = "htmlarea-context-menu";
	if (!Xinha.is_ie) {
	    div.style.visibility = "hidden";
	    div.style.left = div.style.top = "-200px";
	}
	doc.body.appendChild(div);

	var table = doc.createElement("table");
	div.appendChild(table);
	table.cellSpacing = 0;
	table.cellPadding = 0;
	var parent = doc.createElement("tbody");
	table.appendChild(parent);

	var options = this.getContextMenu(target);
  if(options === false) return false; // No context menu
  
	for (var i = 0; i < options.length; ++i) {
		var option = options[i];
		var item = doc.createElement("tr");
		parent.appendChild(item);
		if (Xinha.is_ie)
			item.unselectable = "on";
		else item.onmousedown = function(ev) {
			Xinha._stopEvent(ev);
			return false;
		};
		if (!option) {
			item.className = "separator";
			var td = doc.createElement("td");
			td.className = "icon";
			var IE_IS_A_FUCKING_SHIT = '>';
			if (Xinha.is_ie) {
				td.unselectable = "on";
				IE_IS_A_FUCKING_SHIT = " unselectable='on' style='height=1px'>&nbsp;";
			}
			td.innerHTML = "<div" + IE_IS_A_FUCKING_SHIT + "</div>";
			var td1 = td.cloneNode(true);
			td1.className = "label";
			item.appendChild(td);
			item.appendChild(td1);
		} else {
			var label = option[0];
			item.className = "item";
			item.__msh = {
				item: item,
				label: label,
				action: option[1],
				tooltip: option[2] || null,
				icon: option[3] || null,
				activate: function() {
					self.closeMenu();
					self.editor.focusEditor();
					this.action();
				}
			};
			label = label.replace(/_([a-zA-Z0-9])/, "<u>$1</u>");
			if (label != option[0])
				keys.push([ RegExp.$1, item ]);
			label = label.replace(/__/, "_");
			var td1 = doc.createElement("td");
			if (Xinha.is_ie)
				td1.unselectable = "on";
			item.appendChild(td1);
			td1.className = "icon";
			if (item.__msh.icon)
      {
        var t = Xinha.makeBtnImg(item.__msh.icon, doc);
        td1.appendChild(t);
      }
      var td2 = doc.createElement("td");
			if (Xinha.is_ie)
				td2.unselectable = "on";
			item.appendChild(td2);
			td2.className = "label";
			td2.innerHTML = label;
			item.onmouseover = function() {
				this.className += " hover";
				self.editor._statusBarTree.innerHTML = this.__msh.tooltip || '&nbsp;';
			};
			item.onmouseout = function() { this.className = "item"; };
			item.oncontextmenu = function(ev) {
				this.__msh.activate();
				if (!Xinha.is_ie)
					Xinha._stopEvent(ev);
				return false;
			};
			item.onmouseup = function(ev) {
				var timeStamp = (new Date()).getTime();
				if (timeStamp - self.timeStamp > 500)
					this.__msh.activate();
				if (!Xinha.is_ie)
					Xinha._stopEvent(ev);
				return false;
			};
			//if (typeof option[2] == "string")
			//item.title = option[2];
		}
	}

	if (!Xinha.is_ie) {
	    /* keep then menu from overflowing the client window boundaries */ 
	
	    /*	provide a virtual margin to leave a swoosh of air between the
		meny and the window edge. This should probably go into the menu
		container css as margin 10px instead...
	     */
	    var margin = 10;
	    
	    if (y + div.offsetHeight + margin > window.innerHeight)
		y = window.innerHeight - div.offsetHeight - margin;
	    if (x + div.offsetWidth + margin > window.innerWidth)
		x = window.innerWidth - div.offsetWidth - margin;
	    
	    div.style.left = x + "px";
	    div.style.top = y + "px";
	    div.style.visibility = "visible";

	} else {
    // To get the size we need to display the popup with some width/height
    // then we can get the actual size of the div and redisplay the popup at the
    // correct dimensions.
    this.iePopup.show(ev.screenX, ev.screenY, 300,50);
		var w = div.offsetWidth;
		var h = div.offsetHeight;
		this.iePopup.show(ev.screenX, ev.screenY, w, h);
	}

	this.currentMenu = div;
	this.timeStamp = (new Date()).getTime();

	Xinha._addEvent(document, "mousedown", documentClick);
	Xinha._addEvent(this.editordoc, "mousedown", documentClick);
	if (keys.length > 0)
		Xinha._addEvent(this.editordoc, "keypress", keyPress);

	Xinha._stopEvent(ev);
	return false;
};