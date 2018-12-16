  /*--------------------------------------:noTabs=true:tabSize=2:indentSize=2:--
    --  Xinha (is not htmlArea) - http://xinha.org
    --
    --  Use of Xinha is granted by the terms of the htmlArea License (based on
    --  BSD license)  please read license.txt in this package for details.
    --
    --  Copyright (c) 2005-2008 Xinha Developer Team and contributors
    --
    --  Xinha was originally based on work by Mihai Bazon which is:
    --      Copyright (c) 2003-2004 dynarch.com.
    --      Copyright (c) 2002-2003 interactivetools.com, inc.
    --      This copyright notice MUST stay intact for use.
    --
    --  This is the standard implementation of the Xinha.prototype._createLink method,
    --  which provides the functionality to insert a hyperlink in the editor.
    --
    --  The file is loaded as a special plugin by the Xinha Core when no alternative method (plugin) is loaded.
    --
    --
    --  $HeadURL: http://svn.xinha.org/trunk/modules/CreateLink/link.js $
    --  $LastChangedDate: 2018-02-19 20:35:49 +1300 (Mon, 19 Feb 2018) $
    --  $LastChangedRevision: 1402 $
    --  $LastChangedBy: gogo $
    --------------------------------------------------------------------------*/

function CreateLink(editor) {
	this.editor = editor;
	var cfg = editor.config;
	var self = this;

	if(typeof editor._createLink == 'undefined') {
	    editor._createLink = function(target) {
		if(!target) target = self._getSelectedAnchor();
		self.show(target);
	    }
	}
}

CreateLink._pluginInfo = {
  name          : "CreateLink",
  origin        : "Xinha Core",
  version       : "$LastChangedRevision: 1402 $".replace(/^[^:]*:\s*(.*)\s*\$$/, '$1'),
  developer     : "The Xinha Core Developer Team",
  developer_url : "$HeadURL: http://svn.xinha.org/trunk/modules/CreateLink/link.js $".replace(/^[^:]*:\s*(.*)\s*\$$/, '$1'),
  sponsor       : "",
  sponsor_url   : "",
  license       : "htmlArea"
};

CreateLink.prototype._lc = function(string) {
	return Xinha._lc(string, 'CreateLink');
};


CreateLink.prototype.onGenerateOnce = function()
{
  CreateLink.loadAssets();
};

CreateLink.loadAssets = function()
{
	var self = CreateLink;
	if (self.loading) return;
	self.loading = true;
	Xinha._getback(_editor_url + 'modules/CreateLink/dialog.html', function(getback) { self.html = getback; self.dialogReady = true; });
	Xinha._getback(_editor_url + 'modules/CreateLink/pluginMethods.js', function(getback) { eval(getback); self.methodsReady = true; });
}

CreateLink.prototype.onUpdateToolbar = function()
{ 
	if (!(CreateLink.dialogReady && CreateLink.methodsReady))
	{
		this.editor._toolbarObjects.createlink.state("enabled", false);
	}
	else this.onUpdateToolbar = null;
};

CreateLink.prototype.prepareDialog = function()
{
	var self = this;
	var editor = this.editor;

	var dialog = this.dialog = new Xinha.Dialog(editor, CreateLink.html, 'Xinha',{width:400})
	// Connect the OK and Cancel buttons
	dialog.getElementById('ok').onclick = function() {self.apply();}

	dialog.getElementById('cancel').onclick = function() { self.dialog.hide()};

	if (!editor.config.makeLinkShowsTarget)
	{
		dialog.getElementById("f_target_label").style.visibility = "hidden";
		dialog.getElementById("f_target").style.visibility = "hidden";
		dialog.getElementById("f_other_target").style.visibility = "hidden";
	}

	dialog.getElementById('f_target').onchange= function() 
	{
		var f = dialog.getElementById("f_other_target");
		if (this.value == "_other") {
			f.style.visibility = "visible";
			f.select();
			f.focus();
		} else f.style.visibility = "hidden";
	};

	
	this.dialogReady = true;
};
