SuperClean.prototype._superClean = function(opts, obj)
{
  if(this.editor.config.SuperClean.show_dialog && !this._dialog)
  {
    this._dialog = new SuperClean.Dialog(this);
  }
  var superclean = this;

  // Do the clean if we got options
  var doOK = function()
  {
    superclean._dialog.dialog.getElementById("main").style.display = "none";
    superclean._dialog.dialog.getElementById("waiting").style.display = "";
    superclean._dialog.dialog.getElementById("buttons").style.display = "none";
    
  	var opts = superclean._dialog.dialog.getValues();
    var editor = superclean.editor;

    if(opts.word_clean) editor._wordClean();
    
    var D;
    
    if(opts['scope'].value == 'selection')
    {
      D = editor.getSelectedHTML();
    }
    else
    {
      D = editor.getEditorContent();
    }

    for(var filter in editor.config.SuperClean.filters)
    {
      if(filter=='tidy' || filter=='word_clean') continue;
      
      // Any full only ones need to happen later
      if(typeof filter == 'object' && filter.fullonly) continue;
      
      if(opts[filter])
      {
        D = SuperClean.filterFunctions[filter](D, editor);
      }
    }

    D = D.replace(/(style|class)="\s*"/gi, '');
    D = D.replace(/<(font|span)\s*>/gi, '');

    if(opts['scope'].value == 'selection')
    {
      editor.insertHTML(D);
    }
    else
    {
      editor.setEditorContent(D);
    }

    // Do the full only ones
    D = editor.getEditorContent();
    for(var filter in editor.config.SuperClean.filters)
    {
      // Skip tidy we do that last, and word_clean was done
      // first
      if(filter=='tidy' || filter=='word_clean') continue;
      
      // Any full only ones need to happen last
      if(typeof filter == 'object' && filter.fullonly && opts[filter])
      {
        D = SuperClean.filterFunctions[filter](D, editor);
      }
    }
    
    if(opts.tidy)
    {
      var callback = function(javascriptResponse) 
      { 
        eval("var response = " + javascriptResponse);
        switch (response.action)
        {
          case 'setHTML':
            editor.setEditorContent(response.value);
            superclean._dialog.hide();
          break;
          case 'alert':
            superclean._dialog.dialog.getElementById("buttons").style.display = "";
            superclean._dialog.dialog.getElementById("ok").style.display = "none";
            superclean._dialog.dialog.getElementById("waiting").style.display = "none"; 
            superclean._dialog.dialog.getElementById("alert").style.display = ""; 
            superclean._dialog.dialog.getElementById("alert").innerHTML = superclean._lc(response.value);
          break;
          default: // make the dialog go away if sth goes wrong, who knows...
           superclean._dialog.hide();
          break;
        }
      }
      
      var args = editor.config.SuperClean.tidy_args; 
      if ( typeof args != 'object' ) 
      { 
        args = {}; 
      } 
      args['content'] =  editor.getInnerHTML(); 
     
      Xinha._postback(editor.config.SuperClean.tidy_handler, args, callback);       
    }
    else
    {
      superclean._dialog.hide();
    }
    return true;
  }

  if(this.editor.config.SuperClean.show_dialog)
  {
    var inputs = {};
    this._dialog.show(inputs, doOK);
  }
  else
  {
    var editor = this.editor;
    var html = editor.getEditorContent();
    for(var filter in editor.config.SuperClean.filters)
    {
      if(filter=='tidy') continue; //call tidy last
      html = SuperClean.filterFunctions[filter](html, editor);
    }

    html = html.replace(/(style|class)="\s*"/gi, '');
    html = html.replace(/<(font|span)\s*>/gi, '');

    editor.setEditorContent(html);

    if(editor.config.SuperClean.filters.tidy)
    {
      SuperClean.filterFunctions.tidy(html, editor);
    }
  }
};

SuperClean.filterFunctions.remove_colors = function(D)
{
  D = D.replace(/color="?[^" >]*"?/gi, '');
  // { (stops jedit's fold breaking)
  D = D.replace(/([^-])color:[^;}"']+;?/gi, '$1');
  return(D);
};
SuperClean.filterFunctions.remove_sizes = function(D)
{
  D = D.replace(/size="?[^" >]*"?/gi, '');
  // { (stops jedit's fold breaking)
  D = D.replace(/font-size:[^;}"']+;?/gi, '');
  return(D);
};
SuperClean.filterFunctions.remove_faces = function(D)
{
  D = D.replace(/face="?[^" >]*"?/gi, '');
  // { (stops jedit's fold breaking)
  D = D.replace(/font-family:[^;}"']+;?/gi, '');
  return(D);
};
SuperClean.filterFunctions.remove_lang = function(D)
{
  D = D.replace(/lang="?[^" >]*"?/gi, '');
  return(D);
};
SuperClean.filterFunctions.word_clean = function(html, editor)
{
  editor.setHTML(html);
  editor._wordClean();
  return editor.getInnerHTML();
};

SuperClean.filterFunctions.remove_fancy_quotes = function(D)
{
  D = D.replace(new RegExp(String.fromCharCode(8216),"g"),"'");
  D = D.replace(new RegExp(String.fromCharCode(8217),"g"),"'");
  D = D.replace(new RegExp(String.fromCharCode(8218),"g"),"'");
  D = D.replace(new RegExp(String.fromCharCode(8219),"g"),"'");
  D = D.replace(new RegExp(String.fromCharCode(8220),"g"),"\"");
  D = D.replace(new RegExp(String.fromCharCode(8221),"g"),"\"");
  D = D.replace(new RegExp(String.fromCharCode(8222),"g"),"\"");
  D = D.replace(new RegExp(String.fromCharCode(8223),"g"),"\"");
  return D;
};

SuperClean.filterFunctions.tidy = function(html, editor)
{
  var callback = function(javascriptResponse) 
  {
    eval("var response = " + javascriptResponse);
    switch (response.action)
    {
      case 'setHTML':
        editor.setEditorContent(response.value);
      break;
      case 'alert':
        alert(Xinha._lc(response.value, 'SuperClean'));
      break;
    }
  }
  
  var args = editor.config.SuperClean.tidy_args; 
  if ( typeof args != 'object' ) 
  { 
    args = {}; 
  }
  args['content'] =  html; 
  
  Xinha._postback(editor.config.SuperClean.tidy_handler, args, callback);         
};

SuperClean.filterFunctions.remove_emphasis = function(html)
{  
  html = SuperClean.stripTags(html, [ 'b', 'strong', 'i', 'em', 'u', 'mark', 'ins','abbr', 'acronym', 'kbd', 'samp', 'strike', 's'  ]);
  
  // for del we delete it entirely or it would be confising (<del>old thing</del> <ins>new thing</ins> would become old thing new thing)  
  html = SuperClean.stripTags(html, [ 'del' ], true);
  
  html = html.replace(/font-weight:[^;}"']+;?/gi, '');
  html = html.replace(/font-style:[^;}"']+;?/gi, '');
  return html;
};

SuperClean.filterFunctions.remove_sup_sub = function(html)
{
  return SuperClean.stripTags(html, ['sup', 'sub']).replace(/vertical-align:\s*(sub|super)[^;}"']+;?/gi);
};

SuperClean.filterFunctions.remove_alignment = function(html)
{
  return html.replace(/ align=[^\s|>]*/gi,'').replace(/(text-align|vertical-align|float):[^;}"']+;?/gi, '');
};

SuperClean.filterFunctions.remove_all_css_classes = function(html)
{
  return SuperClean.stripAttributes(html, 'class');
};

SuperClean.filterFunctions.remove_all_css_styles = function(html)
{
  return SuperClean.stripAttributes(html, 'style');
};

SuperClean.filterFunctions.remove_all_tags = function(html)
{
  return  html.replace(/<[\!]*?[^<>]*?>/g, "");
};

SuperClean.Dialog = function (SuperClean)
{
  var  lDialog = this;
  this.Dialog_nxtid = 0;
  this.SuperClean = SuperClean;
  this.id = { }; // This will be filled below with a replace, nifty

  this.ready = false;
  this.dialog = false;

  // load the dTree script
  this._prepareDialog();

};

SuperClean.Dialog.prototype._prepareDialog = function()
{
  var lDialog = this;
  var SuperClean = this.SuperClean;

  var html = window.SuperClean.html;

  var htmlFilters = "";
  for(var filter in this.SuperClean.editor.config.SuperClean.filters)
  {
    htmlFilters += "    <div>\n";
    var filtDetail = this.SuperClean.editor.config.SuperClean.filters[filter];
    if(typeof filtDetail.label == 'undefined')
    {
      htmlFilters += "        <input type=\"checkbox\" name=\"["+filter+"]\" id=\"["+filter+"]\" checked value=\"on\" />\n";
      htmlFilters += "        <label for=\"["+filter+"]\">"+this.SuperClean.editor.config.SuperClean.filters[filter]+"</label>\n";
    }
    else
    {
      htmlFilters += "        <input type=\"checkbox\" name=\"["+filter+"]\" id=\"["+filter+"]\" value=\"on\"" + (filtDetail.checked ? "checked" : "") + " />\n";
      htmlFilters += "        <label for=\"["+filter+"]\">"+filtDetail.label+(filtDetail.fullonly?'^ ':'')+"</label>\n";
    }
    htmlFilters += "    </div>\n";
  }
  html = html.replace('<!--filters-->', htmlFilters);


  // Now we have everything we need, so we can build the dialog.
  var dialog = this.dialog = new Xinha.Dialog(SuperClean.editor, html, 'SuperClean',{width:400});

  this.ready = true;
};

SuperClean.Dialog.prototype._lc = SuperClean.prototype._lc;

SuperClean.Dialog.prototype.show = function(inputs, ok, cancel)
{
  if(!this.ready)
  {
    var lDialog = this;
    window.setTimeout(function() {lDialog.show(inputs,ok,cancel);},100);
    return;
  }

  // Connect the OK and Cancel buttons
  var dialog = this.dialog;
  var lDialog = this;
  if(ok)
  {
    this.dialog.getElementById('ok').onclick = ok;
  }
  else
  {
    this.dialog.getElementById('ok').onclick = function() {lDialog.hide();};
  }

  if(cancel)
  {
    this.dialog.getElementById('cancel').onclick = cancel;
  }
  else
  {
    this.dialog.getElementById('cancel').onclick = function() { lDialog.hide()};
  }

  // If there is selected text, set that the default scope
  if(this.SuperClean.editor.hasSelectedText())
  {
    this.dialog.getElementsByName('scope')[0].selectedIndex = 1;
  }
  else
  {
    this.dialog.getElementsByName('scope')[0].selectedIndex = 0;
  }
  
  // Show the dialog
  this.SuperClean.editor.disableToolbar(['fullscreen','SuperClean']);

  this.dialog.show(inputs);

  // Init the sizes
  this.dialog.onresize();
};

SuperClean.Dialog.prototype.hide = function()
{
  var ret = this.dialog.hide();
  this.SuperClean.editor.enableToolbar();
  this.dialog.getElementById("main").style.display = "";
  this.dialog.getElementById("buttons").style.display = "";
  this.dialog.getElementById("waiting").style.display = "none";
  this.dialog.getElementById("alert").style.display = "none";
  this.dialog.getElementById("ok").style.display = "";
  return ret;
};
