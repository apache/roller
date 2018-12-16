/**
  = Mootools File Manager =
  == File Manager ==
  
  The functions in this file extend the MootoolsFileManager plugin with support
  for managing files (inserting a link to a file).  This file is loaded automatically.
     
 * @author $Author$
 * @version $Id$
 * @package MootoolsFileManager
 */


// Open a "files" mode of the plugin to allow to select a file to
// create a link to.
MootoolsFileManager.prototype.OpenFileManager = function(link) 
{
    var editor = this.editor;
    var outparam = null;
    var self = this;
    
    if (typeof link == "undefined") 
    {
      link = this.editor.getParentElement();
      if (link) 
      {
        if (/^img$/i.test(link.tagName))
            link = link.parentNode;
        if (!/^a$/i.test(link.tagName))
            link = null;
      }
    }
    
    // If the link wasn't provided, and no link is currently in focus,
    // make one from the selection.
    if (!link) 
    {
      var sel = editor.getSelection();
      var range = editor.createRange(sel);
      var compare = 0;
      
      if (Xinha.is_ie) 
      {
        if ( sel.type == "Control" )
        {
          compare = range.length;
        }
        else
        {
          compare = range.compareEndPoints("StartToEnd", range);
        }
      } 
      else 
      {
        compare = range.compareBoundaryPoints(range.START_TO_END, range);
      }
      
      if (compare == 0) 
      {
        alert(Xinha._lc("You must select some text before making a new link.", 'MootoolsFileManager'));
        return;
      }
      outparam = {
          f_href : '',
          f_title : '',
          f_target : '',          
          f_type: '',
          baseHref: editor.config.baseHref
      };
    }
    else
    {
      outparam = {
          f_href   : Xinha.is_ie ? link.href : link.getAttribute("href"),
          f_title  : link.title,
          f_target : link.target,          
          f_type   : link.type ? link.type : '',
          baseHref: editor.config.baseHref
      };
    }
    
    this.current_link = link;
    this.current_attributes = outparam;
    
    if(!this.FileManagerWidget)
    {    
      this.FileManagerWidget = new FileManager({
        url:            this.editor.config.MootoolsFileManager.backend,
        assetBasePath:  Xinha.getPluginDir('MootoolsFileManager')+'/mootools-filemanager/Assets',
        language:       _editor_lang,
        selectable:     true,
        upload:         this.phpcfg.files_allow_upload,
        destroy:        this.phpcfg.files_allow_delete,
        createFolders:  this.phpcfg.files_allow_create_dir,   
        rename:         this.phpcfg.files_allow_move,
        move_or_copy:   this.phpcfg.files_allow_move,
        download:       this.phpcfg.files_allow_download,   
                                                     
        propagateData:  Object.merge({'__function': 'file-manager'}, this.editor.config.MootoolsFileManager.backend_data),
        propagateType:  'POST',
      
        uploadAuthData: Object.merge({'__function': 'file-manager'}, this.editor.config.MootoolsFileManager.backend_data),
                                               
        onComplete:     function(path, file, mgr) { self.FileManagerReturn(path,file); },
        onHide:         function() { if(this.swf && this.swf.box) this.swf.box.style.display = 'none'; },
        onShow:         function() { if(this.swf && this.swf.box) this.swf.box.style.display = ''; },
        onDetails:      function(details) 
                        {                                                 
                          this.info.adopt(self.FileManagerAttributes(details)); 
                          return true;
                        },
        onHidePreview:  function()
                        {                        
                          document.id(self.FileManagerAttributes().table).dispose();
                          return true;
                        },
                        
        showDirGallery: false,
        keyboardNavigation: false,
        listType:           this.phpcfg.files_list_type,
        listPaginationSize: this.phpcfg.files_pagination_size,
        listMaxSuggestedDirSizeForThumbnails: this.phpcfg.files_list_mode_over,
        directory:          this.phpcfg.files_list_start_in
      });       
    }

    // IE11 which pretends it is "gecko" is particularly finicky 
    //  about losing selection other browsers not so much, but they
    //   don't seem to mind saving and restoring it anyway, so we 
    //   will do that for everybody
    if(1||Xinha.is_ie) this.current_selection = this.editor.saveSelection();
    if(link)
    {      
        var src  = Xinha.is_ie ? link.href : link.getAttribute("href");
        if(!src.match(/^(([a-z]+:)|\/)/i))
        {
            src = self.editor.config.baseHref.replace(/\/[^\/]*$/, '') + '/' + src;
            if(src.match(/^[a-z]+:/i) && !self.phpcfg.files_url.match(/^[a-z]:/i))
            {
              src = src.replace(/^[a-z]+:(\/\/?)[^/]*/i, '');
            }
        }
        
        // Get exploded path without the base url
        var path = src.replace(self.phpcfg.files_url+'/', '').split('/');
        
        // Pull off the file
        var base = path.pop();      
        
        // Join the path back togethor (no base url, trailing slash if the path has any length)
        path = path.length ? (path.join('/') + '/') : '';
        
        // feed to widget
        this.FileManagerWidget.show(null, path, base);          
    }
    else
    {
      this.FileManagerWidget.show();    
    }    
};

// Take the values from the file selection and make it (or update) a link
MootoolsFileManager.prototype.FileManagerReturn = function(path, file)
{
  var editor = this.editor;
  var a      = this.current_link;
  
  var param = this.FileManagerAttributes();  
  param.f_href = path;

  // IE11 which pretends it is "gecko" is particularly finicky 
  //  about losing selection other browsers not so much, but they
  //   don't seem to mind saving and restoring it anyway, so we 
  //   will do that for everybody
  if(1||Xinha.is_ie) this.editor.restoreSelection(this.current_selection);  
  if (!a)
  {
    try 
    {
      editor._doc.execCommand("createlink", false, param.f_href);
      a = editor.getParentElement();
      var sel = editor.getSelection();
      var range = editor.createRange(sel);
      if (!Xinha.is_ie) 
      {
        a = range.startContainer;
        if (!/^a$/i.test(a.tagName)) 
        {
          a = a.nextSibling;
          if (a == null)
          {
            a = range.startContainer.parentNode;
          }
        }
      }
    } catch(e) {}
  }
  else 
  {
    var href = param.f_href.trim();
    editor.selectNodeContents(a);
    if (href == "") 
    {
      editor._doc.execCommand("unlink", false, null);
      editor.updateToolbar();
      return false;
    }
    else 
    {
      a.href = href;
    }
  }
  
  if (!(a && /^a$/i.test(a.tagName)))
  {
    return false;
  }
  
  a.type = param.f_type.trim();
  a.target = param.f_target.trim();
  a.title = param.f_title.trim();
  editor.selectNodeContents(a);
  editor.updateToolbar();
};

/** Return a DOM fragment which has all the fields needed to set the
 *  attributes for a link given a structure of initial values.
 * 
 *  OR return a structure of values taken from the currently table.
 */
 
MootoolsFileManager.prototype.FileManagerAttributes = function (details)
{

  var self = this;
  self._LastFileDetails = details;
  
  function f(name)
  {
    var e = self._FileManagerAttributesTable.getElementsByTagName('input');
    for(var i = 0; i < e.length; i++)
    {
      if(e[i].name == name) return e[i];
    }
    
    var e = self._FileManagerAttributesTable.getElementsByTagName('select');
    for(var i = 0; i < e.length; i++)
    {
      if(e[i].name == name) return e[i];
    }
    
    return null;    
  }
  
  function s(name, value)
  {
    for(var i = 0; i < f(name).options.length; i++)
    {
      if(f(name).options[i].value == value) 
      {
       // f(name).options[i].selected = true;
        f(name).selectedIndex = i;
        return true;
      }
    }
    return false;
  }
  
  if(!this._FileManagerAttributesTable)
  {
    this._FileManagerAttributesTable = (function() {
      var div     = document.createElement('div');
      
      var h2  = document.createElement('h2');
      h2.appendChild(document.createTextNode('Link Attributes'));            
      div.appendChild(h2);
            
      var table = document.createElement('table');
      div.appendChild(table);
      
      table.className = 'filemanager-extended-options';
      var tbody = table.appendChild(document.createElement('tbody'));
      
      { // Title
        var tr    = tbody.appendChild(document.createElement('tr'));
        var th    = tr.appendChild(document.createElement('th'));
        var label = th.appendChild(document.createTextNode('Title:'));
        
        var td    = tr.appendChild(document.createElement('td'));
        var input = td.appendChild(document.createElement('input'));
        
        td.colSpan   = 6;
        input.name   = 'f_title';
        input.type = 'text';
        th.className = td.className = 'filemanager-f_title';      
      }
      
      { // Content Type
        var tr    = tbody.appendChild(document.createElement('tr'));
        var th    = tr.appendChild(document.createElement('th'));
        var label = th.appendChild(document.createTextNode('Type:'));
        
        var td    = tr.appendChild(document.createElement('td'));
        var input = td.appendChild(document.createElement('input'));
        
        td.colSpan   = 6;
        input.name   = 'f_type';
        input.type = 'text';
        th.className = td.className = 'filemanager-f_type';      
      }
           
      { // Target
        var tr    = tbody.appendChild(document.createElement('tr'));
        
        { // Target
          var th    = tr.appendChild(document.createElement('th'));
          var label = th.appendChild(document.createTextNode('Open In:'));
          
          var td    = tr.appendChild(document.createElement('td'));
          td.colSpan = 2;
          var input = td.appendChild(document.createElement('select'));
          
          input.name   = 'f_target';          
          input.options[0] = new Option('');
          input.options[1] = new Option('New Window', '_blank');
          input.options[2] = new Option('Top Frame', '_top');
          input.options[3] = new Option('Other Frame:', '');
          
          Xinha._addEvent(input, 'change', function()
          {                    
            if(f('f_target').selectedIndex == 3)
            {
              f('f_otherTarget').style.visibility = 'visible';
            }
            else
            {
              f('f_otherTarget').style.visibility = 'hidden';
            }
          });
          
          var input = td.appendChild(document.createElement('input'));
          input.name   = 'f_otherTarget';
          input.size = 7;    
          input.type = 'text';     
          input.style.visibility = 'hidden';
          
          th.className = td.className = 'filemanager-f_target';              
        }                      
      }
            
      return div;
    })();        
  }
  
  if(this.current_attributes)
  {
    f('f_title').value    = this.current_attributes.f_title;
    f('f_type').value     = this.current_attributes.f_type;
    
    if(this.current_attributes.f_target)
    {
      if(!s('f_target', this.current_attributes.f_target))
      {
        f('f_target').selectedIndex = 3;
        f('f_otherTarget').value = this.current_attributes.f_target;
      }     
      else
      {
        f('f_otherTarget').value = '';
      }
    }
    
    this.current_attributes = null;
  }
  
  // If no details were supplied, we return the current ones
  if(!details) 
  {
    var details = {
      f_title:  f('f_title').value,
      f_type:  f('f_type').value,
      f_target: f('f_target').selectedIndex < 3 ? f('f_target').options[f('f_target').selectedIndex].value : f('f_otherTarget').value,
      
      table: this._FileManagerAttributesTable
    }
    
    return details;
  }
  
  // If details were supplied, we set the appropriate ones.    
  if(details.mime) f('f_type').value = details.mime;
  
  f('f_target').style.visibility = ''; // Ensure that the select hasn't been hidden by an overlay and not put back
  
  if(f('f_target').selectedIndex == 3)
  {
    f('f_otherTarget').style.visibility = 'visible';
  }
  else
  {
    f('f_otherTarget').style.visibility = 'hidden';
  }
  
  return this._FileManagerAttributesTable;
};
