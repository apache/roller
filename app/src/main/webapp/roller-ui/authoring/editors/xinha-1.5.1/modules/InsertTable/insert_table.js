
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
    --  This is the Xinha standard implementation of a table insertion plugin
    --
    --  The file is loaded by the Xinha Core when no alternative method (plugin) is loaded.
    --
    --
    --  $HeadURL: http://svn.xinha.org/trunk/modules/InsertTable/insert_table.js $
    --  $LastChangedDate: 2018-02-19 20:35:49 +1300 (Mon, 19 Feb 2018) $
    --  $LastChangedRevision: 1402 $
    --  $LastChangedBy: gogo $
    --------------------------------------------------------------------------*/
InsertTable._pluginInfo = {
  name          : "InsertTable",
  origin        : "Xinha Core",
  version       : "$LastChangedRevision: 1402 $".replace(/^[^:]*:\s*(.*)\s*\$$/, '$1'),
  developer     : "The Xinha Core Developer Team",
  developer_url : "$HeadURL: http://svn.xinha.org/trunk/modules/InsertTable/insert_table.js $".replace(/^[^:]*:\s*(.*)\s*\$$/, '$1'),
  sponsor       : "",
  sponsor_url   : "",
  license       : "htmlArea"
};

function InsertTable(editor) {
	this.editor = editor;
	var cfg = editor.config;
	var self = this;

	if(typeof editor._insertTable == 'undefined') {
	    editor._insertTable = function() {
		self.show();
	    }
	}
}

InsertTable.prototype._lc = function(string) {
	return Xinha._lc(string, 'InsertTable');
};


InsertTable.prototype.onGenerateOnce = function()
{
	InsertTable.loadAssets();
};
InsertTable.loadAssets = function()
{
	var self = InsertTable;
	if (self.loading) return;
	self.loading = true;
	Xinha._getback(_editor_url + 'modules/InsertTable/dialog.html', function(getback) { self.html = getback; self.dialogReady = true; });
	Xinha._getback(_editor_url + 'modules/InsertTable/pluginMethods.js', function(getback) { eval(getback); self.methodsReady = true; });
};

InsertTable.prototype.onUpdateToolbar = function()
{ 
  if (!(InsertTable.dialogReady && InsertTable.methodsReady))
	{
	  this.editor._toolbarObjects.inserttable.state("enabled", false);
	}
	else this.onUpdateToolbar = null;
};

InsertTable.prototype.prepareDialog = function()
{
	var self = this;
	var editor = this.editor;

	var dialog = this.dialog = new Xinha.Dialog(editor, InsertTable.html, 'Xinha',{width:400})
	// Connect the OK and Cancel buttons
	dialog.getElementById('ok').onclick = function() {self.apply();}
	dialog.getElementById('cancel').onclick = function() { self.dialog.hide()};
  
	this.borderColorPicker = new Xinha.colorPicker.InputBinding(dialog.getElementById('border_color'));

	this.dialog.onresize = function ()
	{
		this.getElementById("layout_fieldset").style.width =(this.width / 2) + 50 + 'px';
    this.getElementById("spacing_fieldset").style.width =(this.width / 2) - 120 + 'px'; 
	}

	this.dialogReady = true;
};
