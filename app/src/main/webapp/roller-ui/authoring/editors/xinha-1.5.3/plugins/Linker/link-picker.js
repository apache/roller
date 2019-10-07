
  /*--------------------------------------:noTabs=true:tabSize=2:indentSize=2:--
    --  Xinha (is not htmlArea) - http://xinha.gogo.co.nz/
    --
    --  Use of Xinha is granted by the terms of the htmlArea License (based on
    --  BSD license)  please read license.txt in this package for details.
    --
    --  Xinha was originally based on work by Mihai Bazon which is:
    --      Copyright (c) 2003-2004 dynarch.com.
    --      Copyright (c) 2002-2003 interactivetools.com, inc.
    --      This copyright notice MUST stay intact for use.
    --
    --  $HeadURL: http://svn.xinha.webfactional.com/trunk/modules/Dialogs/inline-dialog.js $
    --  $LastChangedDate: 2007-01-24 03:26:04 +1300 (Wed, 24 Jan 2007) $
    --  $LastChangedRevision: 694 $
    --  $LastChangedBy: gogo $
    --------------------------------------------------------------------------*/
 
    
/** The Link Picker is a semi-standalone instance of the Linker plugin which can be used
 *  for providing a Linker style browse dialog for selecting urls which are then 
 *  returned into a standard form field.
 *
 *  Usage: 
 *  --------------------------------------------------------------------------
 *  {{{
 
    <!-- If you are already using Xinha on the page you should already have this stuff... -->
    <script language="javascript">
      _editor_url = '/url/to/xinha';
      _editor_lang = "en"; 
    </script>
    <script type="text/javascript" src="/url/to/xinha/XinhaCore.js"></script>    
    
    <!-- There are four files required for link picker, in this order. -->
    <script type="text/javascript" src="/url/to/xinha/modules/Dialog/XinhaDialog.js"></script>        
    <script type="text/javascript" src="/url/to/xinha/modules/Dialog/DetachedDialog.js"></script>
    <script type="text/javascript" src="/url/to/xinha/plugins/Linker/Linker.js"></script>
    <script type="text/javascript" src="/url/to/xinha/plugins/Linker/link-picker.js"></script>
    
    <script language="javascript">
      with(LinkPicker.Config.prototype)
      {
         <?php
          // See Linker for more possible config items
          $myConfig = array
            (
              'dir'        => '/path/to/base' , 
              'url'        => '/url/to/base' ,              
            );
           xinha_pass_to_php_backend($myConfig);
          ?>
      }
    
      Xinha._addEvent(window, 'load', function()
        { 
          new LinkPicker(
           document.getElementById('url'),  // this is the field that you want to pick links for, it gets a Browse button
           new LinkPicker.Config()          
          );
        });
    </script>
    
    <form>
      <input type="text" id="url" />
      <div id="dialogHere" style="width:640px; height:480px;"></div>
    </form>
    
 *  }}}
 *   
 */
 
function LinkPicker(field, config)
{
  this.field = field;
  
  var linkPicker = this;
  
  // We use a tempoary anchor tag to pass to the Linker plugin
  this.tmpAnchor = document.createElement('a');  
  
  // We will use the detached dialog always
  config.dialog = LinkPicker.Dialog;
  config.canSetTarget = false; 
  config.canRemoveLink = false;
  
  // These methods are dummy versions of stuff that would normally be in a Xinha object
  this.selectionEmpty = function() { return true; };
  this.getSelection   = function() { return null; };  
  this.selectNodeContents = function() { return true; };  
  this.getHTML = function() { return ''; }
  this.disableToolbar = function() { return true; }
  this.enableToolbar  = function() { return true; }
  this._doc = {
    execCommand: function() { return false; },
    getElementsByTagName: function() { return [ ]; }
  }  
  this.config = { 
    Linker: config, 
    btnList: { }, 
    registerButton: function() { return true; }, 
    addToolbarElement: function() { }
  }
  
  // Add a button next to the field
  var button = document.createElement('input'); button.type='button';
  button.value = 'Browse';
  button.onclick = function() { linkPicker.editLink(); return false; } 
  field.parentNode.insertBefore(button,field.nextSibling);  
    
  // We co-opt updateToolbar as the point at which we copy the temporary anchor across to the field
  // Linker calls this as the last step, so perfect.
  this.updateToolbar  = function() { linkPicker.field.value = this.fixRelativeLinks(linkPicker.tmpAnchor.href); };
    
  this.linker = new Linker(this);  
  this.linker.onGenerateOnce();
}

LinkPicker.prototype.editLink = function()
{
  this.tmpAnchor.href = this.field.value;
  this.linker._createLink(this.tmpAnchor);
}

LinkPicker.prototype.fixRelativeLinks = function(href) 
{  
  return href.replace(document.location.href.replace( /^(https?:\/\/[^\/]*)(.*)$/i, '$1' ), '');
}

LinkPicker.Dialog = function(linkPicker, html, localizer, size, options) 
{ 
  LinkPicker.Dialog.parentConstructor.call(this, html, localizer, size, options); 
}

Xinha.extend(LinkPicker.Dialog, Xinha.DetachedDialog);

LinkPicker.Config = function() { }
LinkPicker.Config.prototype = Xinha.Config.prototype.Linker;