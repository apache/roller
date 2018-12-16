/**
 * An input field File Picker utilising the Xinha ExtendedFileManager.
 *
 * Hijack the Xinha ExtendedFileManager plugin to provide a file URL picker
 * for any form input field in the form of a "Browse" button to the
 * right of the field, in the same manner as a file type input field
 * except it opens the familiar ExtendedFileManager dialog to upload/select
 * a file and returns the URL of the file to the field's value.
 *
 * Example Usage:
 *
 *  <script type="text/javascript">_editor_url = '/url/to/xinha';</script>
 *  <script type="text/javascript" src="file-picker.js" />
 *  <script type="text/javascript">
 *   <?php require_once('/path/to/xinha/contrib/php-xinha.php'); ?>
 *   with(FilePicker.prototype)
 *   {
 *     <?php 
 *      $Conf = array
 *       (
 *         'files_dir' => '/path/to/downloads', 
 *         'files_url' => '/url/to/downloads', 
 *         'show_full_options' => false, // Full options are not useful as a URL picker
 *         // See ExtendedFileManager for more configuration options !           
 *       );
 *      xinha_pass_to_php_backend($Conf);
 *     ?>
 *   }
 *
 *   window.onload = function() { new FilePicker(document.getElementById('idOfTheInputField')); }
 *  </script>
 *
 *
 * @author $Author$
 * @version $Id$
 * @package ImageManager
 */


function FilePicker(field)
{
  this.field = field;
  var picker = this;
  
  var but = document.createElement('input');   
  but.type = 'button';
  but.value = 'Browse'; 
  but.onclick = function() { picker.popup_picker(); }
     
  field.parentNode.insertBefore(but,field.nextSibling);
  field.size = '20';
  field.style.textAlign = 'right';
};

FilePicker.prototype.backend             = _editor_url + 'plugins/ExtendedFileManager/backend.php?__plugin=ExtendedFileManager&';
FilePicker.prototype.backend_data        = null;

FilePicker.prototype.append_query_string = true;

FilePicker.prototype.popup_picker = function()
{
  var picker = this; // closure for later  
  var outparam = null;
  if(picker.field.value)
  {
    outparam =
		{      
			f_href    : picker.field.value,
      f_title   : '',
      f_target  : '',
      f_usetarget : false,
      baseHref: null
    };
     
  }

  var manager = this.backend + '__function=manager&mode=link';
  if(this.backend_config != null)
  {
    manager += '&backend_config='
      + encodeURIComponent(this.backend_config);
    manager += '&backend_config_hash='
      + encodeURIComponent(this.backend_config_hash);
    manager += '&backend_config_secret_key_location='
      + encodeURIComponent(this.backend_config_secret_key_location);
  }
  
  if(this.backend_data != null)
  {
    for ( var i in this.backend_data )
    {
      manager += '&' + i + '=' + encodeURIComponent(this.backend_data[i]);
    }
  }

  Dialog(manager, function(param) {
		if (!param) {	// user must have pressed Cancel
			return false;
		}
    
    picker.field.value = param.f_href;
    
		}, outparam);
}

// Dialog is part of Xinha, but we'll provide it here incase Xinha's not being
// loaded.
if(typeof Dialog == 'undefined')
{
  // htmlArea v3.0 - Copyright (c) 2003-2004 interactivetools.com, inc.
  // This copyright notice MUST stay intact for use (see license.txt).
  //
  // Portions (c) dynarch.com, 2003-2004
  //
  // A free WYSIWYG editor replacement for <textarea> fields.
  // For full source code and docs, visit http://www.interactivetools.com/
  //
  // Version 3.0 developed by Mihai Bazon.
  //   http://dynarch.com/mishoo
  //
  // $Id: dialog.js 183 2005-05-20 06:11:44Z gogo $
  
  // Though "Dialog" looks like an object, it isn't really an object.  Instead
  // it's just namespace for protecting global symbols.
  
  function Dialog(url, action, init) {
    if (typeof init == "undefined") {
      init = window;	// pass this window object by default
    }
    var dlg = window.open(url, "hadialog",
              "toolbar=no,menubar=no,personalbar=no,width=10,height=10," +
              "scrollbars=yes,resizable=yes,modal=yes,dependable=yes");
    Dialog._modal = dlg;
    Dialog._arguments = init;

    // make up a function to be called when the Dialog ends.
    Dialog._return = function (val) 
    {
      if (val && action) {
        action(val);
      }

      Dialog._modal = null;
    };
    Dialog._modal.focus();
  };
     
  // should be a function, the return handler of the currently opened dialog.
  Dialog._return = null;
  
  // constant, the currently opened dialog
  Dialog._modal = null;
  
  // the dialog will read it's args from this variable
  Dialog._arguments = null;
 
}

// Deprecated method for passing config, use above instead!
//---------------------------------------------------------
FilePicker.prototype.backend_config      = null;
FilePicker.prototype.backend_config_hash = null;
FilePicker.prototype.backend_config_secret_key_location = 'Xinha:ExtendedFileManager';
//---------------------------------------------------------
