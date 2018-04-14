/**
 * An input field Image Picker utilising the Xinha ImageManager.
 *
 * Hijack the Xinha ImageManager plugin to provide an image URL picker
 * for any form input field in the form of a "Browse" button to the
 * right of the field, in the same manner as a file type input field
 * except it opens the familiar ImageManager dialog to upload/select/edit
 * an image and returns the URL of the image to the field's value.
 *
 * Example Usage:
 *
 *  <script type="text/javascript">_editor_url = '/url/to/xinha';</script>
 *  <script type="text/javascript" src="image-picker.js" />
 *  <script type="text/javascript">
 *   <?php require_once('/path/to/xinha/contrib/php-xinha.php'); ?>
 *   with(ImagePicker.prototype)
 *   {
 *     <?php 
 *      $Conf = array
 *       (
 *         'images_dir' => '/path/to/images', 
 *         'images_url' => '/url/to/images', 
 *         'show_full_options' => false, // Full options are not useful as a URL picker
 *         // See ImageManager for more configuration options !           
 *       );
 *      xinha_pass_to_php_backend($Conf);
 *     ?>
 *   }
 *
 *   window.onload = function() { new ImagePicker(document.getElementById('idOfTheInputField')); }
 *  </script>
 *
 *
 * @author $Author$
 * @version $Id$
 * @package ImageManager
 */


function ImagePicker(field)
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

ImagePicker.prototype.backend             = Xinha.getPluginDir('ImageManager') + '/backend.php?__plugin=ImageManager&';
ImagePicker.prototype.backend_data        = null;

ImagePicker.prototype.append_query_string = true;

ImagePicker.prototype.popup_picker = function()
{
  var picker = this; // closure for later  
  var outparam = null;
  if(picker.field.value)
  {
    outparam =
		{      
			f_url    : picker.field.value,			
			f_width  : null,
			f_height  : null,
     
      // None of this stuff is useful to us, we return only a URL.
      f_alt    : picker.field.value,
			f_border : null,
			f_align  : null,
			f_padding: null,
			f_margin : null,
      f_backgroundColor: null,
      f_borderColor: null,
      f_border : null,
      f_padding: null,
      f_margin: null
    };
    
    while(outparam.f_url.match(/[?&]((f_[a-z0-9]+)=([^&#]+))/i))
    {
      outparam[RegExp.$2] = decodeURIComponent(RegExp.$3);
      outparam.f_url = outparam.f_url.replace(RegExp.$1, '');
    }
    
    outparam.f_url = outparam.f_url.replace(/&{2,}/g, '&');
    outparam.f_url = outparam.f_url.replace(/\?&*(#.*)?$/, ''); 
  }

  var manager = this.backend + '__function=manager';
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
    
    picker.field.value = param.f_url;
    if(picker.append_query_string)
    {
      if(picker.field.value.match(/[?&](.*)$/))
      {
        if(RegExp.$1.length)
        {
          picker.field.value += '&';
        }
      }
      else
      {
        picker.field.value += '?';
      }
      
      for(var i in param)
      {        
        if(i == 'f_url' || param[i] == null || param[i] == 'null' || param[i] == '') continue;                
        if(typeof param[i] == 'function') continue;
        if(param[i].length = 0) continue;
        
        picker.field.value += i + '=' + encodeURIComponent(param[i]) + '&';
      }
    }
    
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
ImagePicker.prototype.backend_config      = null;
ImagePicker.prototype.backend_config_hash = null;
ImagePicker.prototype.backend_config_secret_key_location = 'Xinha:ImageManager';
//---------------------------------------------------------
