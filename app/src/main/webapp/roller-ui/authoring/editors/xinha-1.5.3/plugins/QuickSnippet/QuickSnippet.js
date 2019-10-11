/** QuickSnippet
 *  
 * Adds a drop-down selector to the toolbar with snippets
 * which can be inserted into the document at the current 
 * cursor position.
 *
 */

QuickSnippet._pluginInfo = {
  name          : "QuickSnippet",
  version       : "1.0",
  developer     : "James Sleeman",
  developer_url : "http://gogo.co.nz",
  sponsor       : "Gogo Internet Services Limited",
  sponsor_url   : "",
  license       : "htmlArea"
}

Xinha.Config.prototype.QuickSnippet = 
{
  snippetfile:         null,
  snippets:            
    [
      {
          'name'  : 'New Paragraph (Lorem Ipsum)',
          'parent': 'body,td,th,div',
          'html'  : '<p>Vivamus sed porta leo. Donec sed arcu ante. Morbi vel lectus sit amet ante faucibus varius. Donec sapien massa, mollis nec sem a, volutpat molestie ipsum. Sed blandit turpis sed orci gravida maximus. Donec fringilla luctus ornare. Cras vel urna vulputate, tristique nulla ac, finibus velit. Sed lobortis mollis hendrerit. Morbi vehicula blandit maximus. Curabitur turpis nunc, ornare quis risus sed, consectetur molestie quam.</p>',
      }
    ],
  default_snippetfile: Xinha.getPluginDir('QuickSnippet')+'/snippets.js'
};

/** BackCompat with InsertSnippet2 */
Xinha.Config.prototype.InsertSnippet2 =
{
  'snippets' : null
};

// We want to be able to use full selectors for the "parent" element
// of snippets, so we'll just cheap-out and use jQuery
Xinha.loadLibrary('jQuery');

function QuickSnippet(editor)
{
	this.editor = editor;
  this.tbo    = null;
  
  var self    = this;
  
  if(editor.config.QuickSnippet.snippetfile)
  {
    Xinha._getback(editor.config.QuickSnippet.snippetfile, function (t,r){
      self.addSnippets(JSON.parse(t));
    });
  }
  else if(editor.config.InsertSnippet2.snippets)
  {
    // We are able to load an InsertSnippet2 xml configuration file
    Xinha._getback(editor.config.InsertSnippet2.snippets, function (t,r){
      var xml = r.responseXML;
      var s   = xml.getElementsByTagName('s');
   
      var snippets = [ ];
      for(var i = 0; i<s.length;i++)
      {
        var snippet = { 
          'name': s[i].getAttribute('n')
        };
        
        if(s[i].hasAttribute('v') &&  s[i].getAttribute('v').length )
        {
          snippet.html = s[i].getAttribute('v');
        }
        else
        {
          snippet.html = s[i].text || s[i].textContent;
        }
        
        if(s[i].hasAttribute('parent') &&  s[i].getAttribute('parent').length )
        {
          snippet.parent = s[i].getAttribute('parent');
        }

        snippets[i] = snippet;
      }

      self.addSnippets(snippets);
    });
  }
  else if(editor.config.QuickSnippet.default_snippetfile)
  {
    Xinha._getback(editor.config.QuickSnippet.default_snippetfile, function (t,r){
      self.addSnippets(JSON.parse(t));
    });
  }
    
  this.dropDown = {
    id: 'QuickSnippet',
    options: {   },
    action:  function(editor){ self.doInsert(); },
    refresh: function(editor){ /* NOP */        },
    context: null
  };
  
  if(typeof editor.config.FancySelects != 'undefined')
  {
    editor.config.FancySelects.widths['QuickSnippet'] = '130px';
  }
  
  editor.config.registerDropdown(this.dropDown);
  editor.config.addToolbarElement(['QuickSnippet', 'separator'] , "formatblock", -1);
  
}

QuickSnippet.prototype.doInsert = function()
{
  var options = this.tbo.element.options;
  
  var selected = options[options.selectedIndex].value;
  if(!selected) return false;
  
  selected = this.editor.config.QuickSnippet.snippets[selected];
  
  if(selected.parent)
  {
    // We first need to convert the HTML string into an element
    var frag = this.editor._doc.createElement('div');
    frag.innerHTML=selected.html;

    // Now we need to find the parent element which matches
    var parent = jQuery(this.editor.getParentElement()).closest(selected.parent).first();
    
    if(!parent.length)
    {
      // Not applicble here
      console.log("Not Applicable");
    }
    else if(parent.children().length)
    {
      // Now we need to find where to insert it in that lot, we are looking for the 
      // direct child of the parent which contains the selection point
      var self = this;
      var found = false;
      parent.children().each(function(i,v){
        if(jQuery(v).find(self.editor.getParentElement()).addBack(self.editor.getParentElement()).length)
        {
          found = true;
          // Found the child of the "parent" which contains the selection point
          // insert this snippet before/after here, show a dialog to allow 
          // choosing, here we setup handlers for the after and before buttons
          // in the dialog
          
          self.dialog.getElementById('after').onclick = function(){
            jQuery(frag).children().insertAfter(v);
            self.dialog.hide();
          };
          
          self.dialog.getElementById('before').onclick = function(){
            jQuery(frag).children().insertBefore(v);
            self.dialog.hide();
          };
          
          // and then show it
          self.dialog.show();
          
        }
      });
      
      // Something went wrong, couldn't find where to insert, drop it at the 
      // end of the parent
      if(!found)
      {
        jQuery(frag).children().appendTo(parent);
      }
      
    }
    else
    {
      // There are no children, so just insert into the parent
      jQuery(frag).children().appendTo(parent);
    }
    
    //console.log(this.editor.getParentElement());
  }
  else
  {
    this.editor.insertHTML(selected.html);
  }
  
  options.selectedIndex = 0;
}

QuickSnippet.prototype.addSnippets = function(snippetsArray)
{
  for(var i = 0; i < snippetsArray.length; i++)
  {
    this.editor.config.QuickSnippet.snippets.push(snippetsArray[i]);
  }
  
  // Force the update to rebuild snippet options
  this.updateSnippets(true);
}

QuickSnippet.prototype.updateSnippets = function()
{
  if(!this.tbo) return;
  
  var select  = this.tbo.element;
  var options = this.tbo.element.options;
    
  if(!options.length)
  {
    options[options.length] = new Option('Insert Snippet');
    options[options.length-1].disabled = true;
  }
  
  var focusElement = jQuery(this.editor.getParentElement());
  var optionElements = jQuery(select).children('option');
  
  // The first option element is the "Insert Snippet"
  var lastOption = optionElements[0];
  var somethingChanged = 0;
  
  for(var i = 0; i < this.editor.config.QuickSnippet.snippets.length; i++)
  {
    var snippet = this.editor.config.QuickSnippet.snippets[i];
    
    // Snippets with parents can only be inserted in that context of one of those parents
    if(snippet.parent)
    {
      if(!focusElement.closest(snippet.parent).length)
      {
        // This is out of context, so remove the option (disabling doesn't work so well
        //  with FancySelects).
        optionElements.closest('option[value='+i+']').remove();
        somethingChanged++;
      }
      else
      {
        if(!optionElements.closest('option[value='+i+']').length)
        {
          var newOption = jQuery('<option value="'+i+'">'+snippet.name+'</option>').first();
          newOption.insertAfter(lastOption);          
          lastOption = newOption;
          somethingChanged++;
        }
        else
        {
          lastOption = optionElements.closest('option[value='+i+']').first();
        }
      }
    }
    
    // Snippets without parents can be inserted anywhere
    else
    {
      if(!optionElements.closest('option[value='+i+']').length)
      {
        var newOption = jQuery('<option value="'+i+'">'+snippet.name+'</option>').first();
        newOption.insertAfter(lastOption);          
        lastOption = newOption;
        somethingChanged++;
      }
      else
      {
        lastOption = optionElements.closest('option[value='+i+']').first();
      }
    }
  }
  
  if(somethingChanged)
  {
    // If FancySelect was open it might be confusing, close and reopen it
    if(typeof jQuery(select).select2 != 'undefined' && jQuery(select).data('select2').isOpen())
    {
      jQuery(select).select2('close');
      jQuery(select).select2('open');
    }
  }
  
};

QuickSnippet.prototype.onGenerate = function ()
{
  
}

QuickSnippet.prototype.onGenerateOnce = function ()
{
  this.tbo = this.editor._toolbarObjects['QuickSnippet'];
  this._prepareDialog();
}

QuickSnippet.prototype._prepareDialog = function()
{
  var self = this;
  var editor = this.editor;

  if(!this.html)
  {
    Xinha._getback(Xinha.getPluginDir("QuickSnippet") + '/dialog.html', function(getback) { self.html = getback; self._prepareDialog(); });
    return;
  }
  
  // Now we have everything we need, so we can build the dialog.
  this.dialog = new Xinha.Dialog(editor, this.html, 'QuickSnippet',{width:400});

  this.dialog.getElementById('cancel').onclick = function() { self.dialog.hide()};
  
  this.ready = true;
};


QuickSnippet.prototype.inwardHtml = function(html)
{
	return html;
}
QuickSnippet.prototype.outwardHtml = function(html)
{
	return html;
}
QuickSnippet.prototype.onUpdateToolbar = function ()
{
  this.updateSnippets();
	return false;
}

QuickSnippet.prototype.onExecCommand = function ( cmdID, UI, param )
{
	return false;
}

QuickSnippet.prototype.onKeyDown = function ( event )
{
  return false;
}

QuickSnippet.prototype.onKeyPress = function ( event )
{
	return false;
}

QuickSnippet.prototype.onOnShortCut = function ( event , shortCut )
{
  // Where shortCut is a single character, eg if you press ctrl-a, then
  //  shortCut == 'a'
  return false;
}

QuickSnippet.prototype.onKeyUp = function ( event )
{
  return false;
}

QuickSnippet.prototype.onMouseDown = function ( event )
{
	return false;
}

QuickSnippet.prototype.onBeforeSubmit = function ()
{
	return false;
}

QuickSnippet.prototype.onBeforeUnload = function ()
{
	return false;
}

QuickSnippet.prototype.onBeforeResize = function (width, height)
{
	return false;
}
QuickSnippet.prototype.onResize = function (width, height)
{
	return false;
}
/**
 * 
 * @param {String} action one of 'add', 'remove', 'hide', 'show', 'multi_hide', 'multi_show'
 * @param {DOMNode|Array} panel either the panel itself or an array like ['left','right','top','bottom']
 */
QuickSnippet.prototype.onPanelChange = function (action, panel)
{
	return false;
}
/**
 * 
 * @param {String} mode either 'textmode' or 'wysiwyg'
 */
QuickSnippet.prototype.onMode = function (mode)
{
	return false;
}
/**
 * 
 * @param {String} mode either 'textmode' or 'wysiwyg'
 */
QuickSnippet.prototype.onBeforeMode = function (mode)
{
	return false;
}
