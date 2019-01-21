function InsertAnchor(editor) {
  this.editor = editor;
  var cfg = editor.config;
  var self = this;
  
  this.placeholderImg = '<img class="IA_placeholder" src="'+Xinha.getPluginDir("InsertAnchor")+'/img/insert-anchor.gif" />';
  
  // register the toolbar buttons provided by this plugin
  cfg.registerButton({
  id       : "insert-anchor", 
  tooltip  : this._lc("Insert Anchor"), 
  image    : editor.imgURL("insert-anchor.gif", "InsertAnchor"),
  textMode : false,
  action   : function() {
               self.show();
             }
  });
  cfg.addToolbarElement("insert-anchor", "createlink", 1);
}

InsertAnchor._pluginInfo = {
  name          : "InsertAnchor",
  origin        : "version: 1.0, by Andre Rabold, MR Printware GmbH, http://www.mr-printware.de",
  version       : "2.0",
  developer     : "Udo Schmal",
  developer_url : "http://www.schaffrath-neuemedien.de",
  c_owner       : "Udo Schmal",
  sponsor       : "L.N.Schaffrath NeueMedien",
  sponsor_url   : "http://www.schaffrath-neuemedien.de",
  license       : "htmlArea"
};

InsertAnchor.prototype._lc = function(string) {
    return Xinha._lc(string, 'InsertAnchor');
};

InsertAnchor.prototype.onGenerate = function() {
  this.editor.addEditorStylesheet(Xinha.getPluginDir("InsertAnchor") + '/insert-anchor.css');
  
};

InsertAnchor.prototype.inwardHtml = function(html)
{
	html= html.replace(/(<a[^>]*class="anchor"[^>]*>)/g,"$1"+this.placeholderImg);
	return html;
}
InsertAnchor.prototype.outwardHtml = function(html)
{
	html= html.replace(/(<img[^>]*class="?IA_placeholder"?[^>]*>)/ig,"");
	return html;
}
InsertAnchor.prototype.onGenerateOnce = function()
{
	this._prepareDialog();
};
InsertAnchor.prototype._prepareDialog = function()
{
  var self = this;
  var editor = this.editor;

  if(!this.html)
  {
    Xinha._getback(Xinha.getPluginDir("InsertAnchor") + '/dialog.html', function(getback) { self.html = getback; self._prepareDialog(); });
    return;
  }
  
  // Now we have everything we need, so we can build the dialog.
  this.dialog = new Xinha.Dialog(editor, this.html, 'InsertAnchor',{width:400});
  
  this.dialog.getElementById('ok').onclick = function() {self.apply();}

	this.dialog.getElementById('cancel').onclick = function() { self.dialog.hide()};
	
  this.ready = true;
};

InsertAnchor.prototype.show = function()
{
	if(!this.ready) // if the user is too fast clicking the, we have to make them wait
	{
  var self = this;
		window.setTimeout(function() {self.show();},100);
		return;
  }
  
	var editor = this.editor;
	this.selectedHTML = editor.getSelectedHTML();
	var sel  = editor.getSelection();
  this.range  = editor.createRange(sel);
  this.a = editor.activeElement(sel);
  
  if(!(this.a != null && this.a.tagName.toLowerCase() == 'a'))
  {
    this.a = editor._getFirstAncestor(sel, 'a'); 
  }
  
  this.dialog.getElementById('warning').style.display = 'none';
  
  if (this.a != null && this.a.tagName.toLowerCase() == 'a')
  {
    inputs = { name : this.a.id };
  }
  else
  {
    if(!this.editor.selectionEmpty(sel))
    {
      this.dialog.getElementById('warning').style.display = '';
    }
    inputs = { name : '' };
  } 

	this.dialog.show(inputs);

	this.dialog.getElementById("name").focus();
};

InsertAnchor.prototype.apply = function ()
{
	var editor = this.editor;
	var param = this.dialog.hide();
	var anchor = param['name'];
	var a = this.a;
	var self = this;
	if (anchor == "" || anchor == null)
	{
		if (a) 
		{
          var child = self.outwardHtml(a.innerHTML);
          a.parentNode.removeChild(a);
          editor.insertHTML(child);
        }
        return;
	}
	try 
	{
        var doc = editor._doc;
		if (!a)
		{
          a = doc.createElement("a");
          a.id = anchor;
          a.name = anchor;
          a.title = anchor;
          a.className = "anchor";
          a.innerHTML = self.placeholderImg;
		//	var html = editor.getSelectedHTML();
		//  if (html) a.innerHTML += html;
			if (Xinha.is_ie) 
			{
            this.range.pasteHTML(a.outerHTML);
          	}
			else 
			{
            editor.insertNodeAtSelection(a);
          }
		}
		else 
		{
          a.id = anchor;
          a.name = anchor;
          a.title = anchor;
          a.className = "anchor";
        }
      }
      catch (e) { }

    }
