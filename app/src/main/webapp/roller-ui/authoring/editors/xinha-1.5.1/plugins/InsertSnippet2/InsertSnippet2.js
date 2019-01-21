/*------------------------------------------*\
 InsertSnippet2 for Xinha
 _______________________
 
 Insert HTML fragments or template variables
 
\*------------------------------------------*/

function InsertSnippet2(editor) {
  this.editor = editor;

  var cfg = editor.config;
  var self = this;
  

  cfg.registerButton({
  id       : "InsertSnippet2",
  tooltip  : this._lc("Insert Snippet"),
  image    : editor.imgURL("ed_snippet.gif", "InsertSnippet2"),
  textMode : false,
  action   : function(editor) {
          self.buttonPress(editor);
      }
  });
  cfg.addToolbarElement("InsertSnippet2", "insertimage", -1);
  this.snippets = null;
  this.categories = null;
  this.html =null;
      
  Xinha._getback(cfg.InsertSnippet2.snippets,function (txt,req) {
                  var xml=req.responseXML;
                  var c = xml.getElementsByTagName('c');
                  self.categories = [];
                  for (var i=0;i<c.length;i++)
                  {
                      self.categories.push(c[i].getAttribute('n'));	
                  }
                  var s = xml.getElementsByTagName('s');
                  self.snippets = [];
                  var textContent,item;
                  
                  for (var i=0;i<s.length;i++)
                  {
                      item = {};
                      for (var j=0;j<s[i].attributes.length;j++)
                      {
                          item[s[i].attributes[j].nodeName] = s[i].attributes[j].nodeValue;
                      }
                      item.html = s[i].text || s[i].textContent;
                      self.snippets.push(item);
                  }
  });
  Xinha.loadStyle('InsertSnippet.css','InsertSnippet2','IScss');
}

InsertSnippet2.prototype.onUpdateToolbar = function() 
{
  if (!this.snippets)
  {
      this.editor._toolbarObjects.InsertSnippet2.state("enabled", false);
  }
  else InsertSnippet2.prototype.onUpdateToolbar = null;
}

InsertSnippet2._pluginInfo = {
  name          : "InsertSnippet2",
  version       : "1.2",
  developer     : "Raimund Meyer",
  developer_url : "http://x-webservice.net",
  c_owner       : "Raimund Meyer",
  sponsor       : "",
  sponsor_url   : "",
  license       : "htmlArea"
};

Xinha.Config.prototype.InsertSnippet2 =
{
  'snippets' : Xinha.getPluginDir('InsertSnippet2')+'/snippets.xml'
};
    
InsertSnippet2.prototype._lc = function(string) {
    return Xinha._lc(string, 'InsertSnippet2');
};

InsertSnippet2.prototype.onGenerateOnce = function()
{
    this._prepareDialog();
};
InsertSnippet2.prototype._prepareDialog = function()
{
  var self = this;
  if(!this.html) // retrieve the raw dialog contents
  {
    Xinha._getback(Xinha.getPluginDir('InsertSnippet2')+'/dialog.html', function(getback) { self.html = getback; self._prepareDialog(); });
    return;
  }
  if (!this.snippets)
  {
    setTimeout(function(){self._prepareDialog()},50);
    return;
  }
  var editor = this.editor;
  var InsertSnippet2 = this;
  
  this.dialog = new Xinha.Dialog(editor, this.html, 'InsertSnippet2',{width:800,height:400},{modal:true});
  Xinha._addClass( this.dialog.rootElem, 'InsertSnippet2' );
  
  var dialog = this.dialog;
  var main = this.dialog.main;
  var caption = this.dialog.captionBar;
  
  this.snippetTable = dialog.getElementById('snippettable');
  
  this.drawCatTabs();
  this.drawSnippetTable();
  this.preparePreview();
  
  dialog.onresize = function() {self.resize(); } 
  
  this.dialog.getElementById('search').onkeyup = function() { self.search ()};
  this.dialog.getElementById('wordbegin').onclick = function() { self.search ()};
  this.dialog.getElementById('cancel').onclick = function() { self.dialog.hide ()};
  
}

InsertSnippet2.prototype.drawSnippetTable = function()
{
  if (!this.snippets.length) return;
  var self = this;
  var tbody = this.snippetTable;
  var snippets = this.snippets;

  while (tbody.hasChildNodes())
  {
    tbody.removeChild(tbody.lastChild);
  }
  var id,snippet_name, snippet_html, snippet_cat, snippet_varname, trow, newCell, cellNo, btn;

  for(var i = 0,trowNo=0; i < snippets.length; i++) 
  {
    id = i;
    snippet_name = snippets[i]['n'];
    snippet_cat = snippets[i]['c'];
    snippet_varname = snippets[i]['v']
    snippet_html = snippets[i]['html'];
    
    if (this.categoryFilter && snippet_cat != this.categoryFilter && this.categoryFilter != 'all') continue;

    trow = tbody.insertRow(trowNo);
    trowNo++;

    cellNo = 0;
    newCell = trow.insertCell(cellNo);

    newCell.onmouseover = function(event) {return self.preview(event || window.event)};
    newCell.onmouseout  = function() {return self.preview()};
    newCell.appendChild(document.createTextNode(snippet_name));
    newCell.snID = id;
    
    newCell.id = 'cell' + id;
    cellNo++;

    newCell = trow.insertCell(cellNo);
    
    newCell.style.whiteSpace = 'nowrap';
    btn = document.createElement('button');
    btn.snID = id;
    btn._insAs = 'html';
    btn.onclick = function(event) {self.doInsert(event || window.event); return false};
    btn.appendChild(document.createTextNode(this._lc("HTML")));
    btn.title = this._lc("Insert as HTML");
    newCell.appendChild(btn);
    
    if (snippet_varname)
    {
      newCell.appendChild(document.createTextNode(' '));
      var btn = document.createElement('button');
          btn.snID = id;
          btn._insAs = 'variable';
          btn.onclick = function(event) {self.doInsert(event || window.event); return false};
          btn.appendChild(document.createTextNode(this._lc("Variable")));
          btn.title = this._lc("Insert as template variable");

      newCell.appendChild(btn);
    }

    cellNo++;
  }
}
InsertSnippet2.prototype.drawCatTabs = function()
{
  if (!this.categories.length) return;
  var self = this;
  var tabsdiv = this.dialog.getElementById("cattabs");
  
  while (tabsdiv.hasChildNodes())
  {
    tabsdiv.removeChild(tabsdiv.lastChild);
  }
  var tabs_i = 1;
  var tab = document.createElement('a');
      tab.href = "javascript:void(0);";
      tab.appendChild(document.createTextNode(this._lc("All Categories")));
      tab.cat = 'all';
      tab.className = "tab"+tabs_i;
      tab.onclick = function() {self.categoryFilter = self.cat; self.drawCatTabs();self.drawSnippetTable(); self.search ()} 
      if (!this.categoryFilter || this.categoryFilter == 'all')
      {
        Xinha._addClass(tab,'active');
        tab.onclick = null;
      }
  tabsdiv.appendChild(tab);
  tabs_i++;

  for (var i = 0;i < this.categories.length;i++)
  {
    var name = this.categories[i];
    var tab = document.createElement('a');
        tab.href = "javascript:void(0);";
        tab.appendChild(document.createTextNode(name));
        tab.cat = name;
        tab.className = "tab"+tabs_i;
        tab.onclick = function() {self.categoryFilter = this.cat; self.drawCatTabs();self.drawSnippetTable(); self.search ()} 
        if (name == this.categoryFilter)
        {
            Xinha._addClass(tab,'active');
            tab.onclick = null;
        }
    tabsdiv.appendChild(tab);
    if (Xinha.is_gecko) tabsdiv.appendChild(document.createTextNode(String.fromCharCode(8203)));
    tabs_i = (tabs_i<16) ? tabs_i +1 : 1;
  }

  if (!this.catTabsH)
  {
    this.catTabsH = tabsdiv.offsetHeight;
  }
}
InsertSnippet2.prototype.search = function ()
{
  var tbody = this.dialog.getElementById("snippettable");
  var searchField =this.dialog.getElementById('search');
  if (searchField.value)
  {
    var val =  searchField.value;

        val = val.replace(/\.?([*?+])/g,'.$1');

    var wordstart = (this.dialog.getElementById('wordbegin').checked) ? '^' : '';
    try { var re = new RegExp (wordstart+val,'i'); } catch (e) {var re = null};
  }
  else var re = null;

  for (var i=0;i<tbody.childNodes.length;i++)
  {
    var tr = tbody.childNodes[i]; 
    var name = tr.firstChild.firstChild.data;
    if (re && !name.match(re))
    {
      tr.style.display = 'none';
    }
    else 
    {
      tr.style.display = '';
    }
  }
}
InsertSnippet2.prototype.preview = function(event)
{
  if (!event)
  {
    this.previewBody.innerHTML = '';
    return;
  }
  var target = event.target || event.srcElement;
  var snID = target.snID;
  if (!this.previewBody)
  {
    this.preparePreview();
    return;
  }
  if (this.previewIframe.style.display == 'none')
  {
    this.previewIframe.style.display = 'block';
  }
  this.previewBody.innerHTML = this.snippets[snID].html;
}
InsertSnippet2.prototype.preparePreview = function()
{
  var editor = this.editor;
  var self = this;
  var preview_iframe = this.previewIframe = this.dialog.getElementById('preview_iframe');

  var doc = null;
  
  try
  {
    if ( preview_iframe.contentDocument )
    {
      doc = preview_iframe.contentDocument;        
    }
    else
    {
      doc = preview_iframe.contentWindow.document;
    }
    // try later
    if ( !doc )
    {
      if ( Xinha.is_gecko )
      {
        setTimeout(function() { self.preparePreview(snID); }, 50);
        return false;
      }
      else
      {
        throw("ERROR: IFRAME can't be initialized.");
      }
    }
  }
  catch(ex)
  { // try later
    setTimeout(function() { self.preparePreview(snID);  }, 50);
  }
  doc.open("text/html","replace");

  var html = '<html><head><title></title>';
  html += "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=" + editor.config.charSet + "\">\n";
  html += '<style type="text/css">body {background-color:#fff} </style>';
  if ( typeof editor.config.baseHref != 'undefined' && editor.config.baseHref !== null )
  {
    html += "<base href=\"" + editor.config.baseHref + "\"/>\n";
  }
  
  if ( editor.config.pageStyle )
  {
    html += "<style type=\"text/css\">\n" + editor.config.pageStyle + "\n</style>";
  }

  if ( typeof editor.config.pageStyleSheets !== 'undefined' )
  {
    for ( var i = 0; i < editor.config.pageStyleSheets.length; i++ )
    {
      if ( editor.config.pageStyleSheets[i].length > 0 )
      {
        html += "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + editor.config.pageStyleSheets[i] + "\">";
        //html += "<style> @import url('" + editor.config.pageStyleSheets[i] + "'); </style>\n";
      }
    }
  }
  html += "</head>\n";
  html += "<body>\n";
  html += "</body>\n";
  html += "</html>";

  doc.write(html);
  doc.close();
  setTimeout(function() {
    self.previewBody = doc.getElementsByTagName('body')[0];
  },100);
}

InsertSnippet2.prototype.buttonPress = function(editor)
{
  this.dialog.toggle();
}

InsertSnippet2.prototype.doInsert = function(event)
{
  var target = event.target || event.srcElement;
  var sn  = this.snippets[target.snID];
  this.dialog.hide();
  var cfg = this.editor.config.InsertSnippet2;
  if (target._insAs == 'variable') 
  {
      this.editor.insertHTML(sn.v);
  } 
  else 
  {
      this.editor.insertHTML(sn.html);
  }	
}

InsertSnippet2.prototype.resize = function ()
{
  var insertDiv = this.dialog.getElementById('insert_div');
  var preview_iframe = this.dialog.getElementById('preview_iframe');
  var win = {h:this.dialog.height,w:this.dialog.width};

  var h = win.h - 90;
  if (this.categories.length) h -= this.catTabsH;
  insertDiv.style.height = preview_iframe.style.height =  h + 'px';
  
  //insertDiv.style.width =  win.w + 'px';

  return true;
}