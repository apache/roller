/**
  = Mootools File Manager =
  The MootoolsFileManager plugin allows for the management, selection and 
  insertion of links to files and inline images to the edited HTML.
   
  It requires the use of Mootools.  If you do not use mootools plugins,
  then mootools is not loaded.  If mootools is already loaded, it will
  not be reloaded (so if you want to use your own version of Mootools 
  then load it first).
 
  == Usage ==
  Instruct Xinha to load the MootoolsFileManager plugin (follow the NewbieGuide).
 
  Configure the plugin as per the directions in the config.php file.
   
 * @author $Author$
 * @version $Id$
 * @package MootoolsFileManager
 */


MootoolsFileManager._pluginInfo = {
  name          : "Mootols File Manager",
  version       : "1.5",
  developer     : "James Sleeman (Xinha), Christoph Pojer (FileManager)",  
  license       : "MIT"
};

// All the configuration is done through PHP, please read config.php to learn how (or if you're in a real 
// hurry, just edit config.php)
Xinha.Config.prototype.MootoolsFileManager =
{
  'backend'      : Xinha.getPluginDir("MootoolsFileManager") + '/backend.php?__plugin=MootoolsFileManager&',
  'backend_data' : { }
};

// In case you want to use your own version of Mootools, you can load it first.
MootoolsFileManager.AssetLoader = Xinha.loadLibrary('MooTools');

// In case you want to use your own version of FileManager, you can load it first.
// You better look at the changes we had to do to the standard one though.
if(typeof FileManager == 'undefined')
{
  if(typeof __MFM_ASSETS_DIR__ == 'undefined')
  {
    __MFM_ASSETS_DIR__ = Xinha.getPluginDir('MootoolsFileManager')+'/mootools-filemanager/Assets'
  }
  
  if(typeof __MFM_USE_FLASH__ == 'undefined')
  {
    // Flash is now disabled by default, there is no good reason to use it
    // as the NoFlash uploader works exactly the same on modern browsers
    // (allows multiple file uploads, progress bars and so forth)
    __MFM_USE_FLASH__ = false;
    
    /*
      __MFM_USE_FLASH__ = Browser.Plugins.Flash.version ? true : false;
      if(Browser.Platform.mac && Browser.firefox3) __MFM_USE_FLASH__ = false;
    */
  }
  
  if(typeof __MFM_USE_BACK_BUTTON_NAVIGATION__ == 'undefined')
  {
    __MFM_USE_BACK_BUTTON_NAVIGATION__ = false;
  }
  
  // Because dom is probably already ready for us, we can't use the autoinit, we do it manually
  __MILKBOX_NO_AUTOINIT__ = true;
  
  MootoolsFileManager.AssetLoader
    // These are now loaded by FileManager.js automatically
    //.loadStyle('mootools-filemanager/Assets/Css/FileManager.css', 'MootoolsFileManager')
    //.loadStyle('mootools-filemanager/Assets/Css/Additions.css', 'MootoolsFileManager')
    //.loadScript('mootools-filemanager/Source/Additions.js', 'MootoolsFileManager')        
    .loadScript('mootools-filemanager/Source/FileManager.js', 'MootoolsFileManager')
    .loadScript('mootools-filemanager/Language/Language.en.js', 'MootoolsFileManager')
    .whenReady(function() {
      window.milkbox =  new Milkbox({
        centered:false
      });
    });
    
 if(__MFM_USE_FLASH__)
 {
   MootoolsFileManager.AssetLoader
     .loadScript('mootools-filemanager/Source/Uploader/Swiff.Uploader.js', 'MootoolsFileManager')
     .loadScript('mootools-filemanager/Source/Uploader/Fx.ProgressBar.js', 'MootoolsFileManager')
     .loadScript('mootools-filemanager/Source/Uploader.js', 'MootoolsFileManager');
 }
 else
 {
   MootoolsFileManager.AssetLoader
      .loadScript('mootools-filemanager/Source/NoFlash.Uploader.js', 'MootoolsFileManager');
 }    
}
MootoolsFileManager.AssetLoader.loadStyle('MootoolsFileManager.css', 'MootoolsFileManager');

function MootoolsFileManager(editor)
{  
  this.editor = editor;
  var self = this;
  var cfg = editor.config;
    
  // Do a callback to the PHP backend and get it to "decode" the configuration for us into a 
  // javascript object.  
  
  // IMPORTANT: we need to do this synchronously to ensure that the buttons are added to the toolbar
  //  before the toolbar is drawn.
  
  var phpcfg = Xinha._posturlcontent(editor.config.MootoolsFileManager.backend+'__function=read-config', editor.config.MootoolsFileManager.backend_data);

  eval ('var f = '+phpcfg+';'); 
  self.phpcfg = f; 
  self.hookUpButtons(); 
    
  return;  
};

/** Connect up/insert the appropriate buttons and load in the auxillary files.
 *
 *  The different "modes" of this plugin have been split into several auxilliary files
 *  as it's likely you may not want them all (esp if we add more modes later).
 *
 *  Each mode's "include" is loaded as soon as we know it could be needed by the
 *  editor, we don't wait until the button is pressed as that would be slow for
 *  the user to respond.
 * 
 */
 
MootoolsFileManager.prototype.hookUpButtons = function() 
{
  var self = this;  
  var phpcfg = self.phpcfg;    
  
  if (phpcfg.files_dir) 
  {
    MootoolsFileManager.AssetLoader.loadScript('MootoolsFileManager.FileManager.js', 'MootoolsFileManager');
    
    this.editor.config.registerButton({
        id        : "linkfile",
        tooltip   : Xinha._lc("Insert File Link",'MootoolsFileManager'),
        image     : Xinha.getPluginDir('MootoolsFileManager') + '/img/ed_linkfile.gif',
        textMode  : false,
        action    : function(editor) { MootoolsFileManager.AssetLoader.whenReady(function() { self.OpenFileManager(); }); }
        });
        
    this.editor.config.addToolbarElement("linkfile", "createlink", 1);            
  };
  
  if(phpcfg.images_dir)
  {     
    MootoolsFileManager.AssetLoader.loadScript('MootoolsFileManager.ImageManager.js', 'MootoolsFileManager');
    
    // Override our Editors insert image button action.  
    self.editor._insertImage = function(image)
    {
      MootoolsFileManager.AssetLoader.whenReady(function() { self.OpenImageManager(image); });
    }              
  } 
};

/** Helper method to scale (an image typically) to a new constraint.
 *
 * @param origdim object { width: 123,height: 456 } The original dimensions.
 * @param newdim  object { width: 456, height: 123 } The new (maximum) dimensions.
 * @param flexside 'width'|'height' (optional) the side which can be "flexible" 
 *   Defaults to the "short" side.
 * @return { width: 789, height: 987 } The scaled dimensions which should be used. 
 */
 
MootoolsFileManager.prototype.ScaleImage = function( origdim, newdim, flexside )
{
  if(!origdim.height || !origdim.width) return newdim; // No old size, stays new.
  if(!newdim.height && !newdim.width) return origdim; // No new size, stays the same.
    
  if(!flexside)
  {
    if(origdim.width > origdim.height)
    {
      flexside = 'height'; // Landscape image, allow the height to flex.
    }
    else
    {
      flexside = 'width'; // Portrait image, allow the width to flex
    }
  }
  
  var knownside = null;
  switch(flexside)
  {
    case 'height': knownside = 'width'; break;
    case 'width' : knownside = 'height'; break;
  }
  
  // If we DON'T know the known side, we need to flip it.
  if(!newdim[knownside])
  {
    var t = knownside;
    knownside = flexside;
    flexside = t;
  }
  
  var ratio = 0;
  switch(flexside)
  {
    case 'width':  ratio = origdim.width / origdim.height; break;
    case 'height': ratio = origdim.height / origdim.width; break;
  }
  
  var rdim = {};
  rdim[knownside] = newdim[knownside];
  rdim[flexside]  = Math.floor(newdim[knownside] * ratio);
  if(isNaN(rdim[knownside])) rdim[knownside] = null;
  if(isNaN(rdim[flexside]))  rdim[flexside]  = null;
  return rdim;
}

/** Take a multi-part CSS size specification (eg sizes for 4 borders) and 
 * shrink it into one if possible.
 */
 
MootoolsFileManager.prototype.shortSize = function(cssSize)
{
  if(/ /.test(cssSize))
  {
    var sizes = cssSize.split(' ');
    var useFirstSize = true;
    for(var i = 1; i < sizes.length; i++)
    {
      if(sizes[0] != sizes[i])
      {
        useFirstSize = false;
        break;
      }
    }
    if(useFirstSize) cssSize = sizes[0];
  }
  return cssSize;
};

/** Take a colour in rgb(a,b) format and convert to HEX
 * handles multiple colours in same string as well.
 */
 
MootoolsFileManager.prototype.convertToHex = function(color) 
{
  if (typeof color == "string" && /, /.test.color)
  color = color.replace(/, /, ','); // rgb(a, b) => rgb(a,b)

  if (typeof color == "string" && / /.test.color) { // multiple values
    var colors = color.split(' ');
    var colorstring = '';
    for (var i = 0; i < colors.length; i++) {
      colorstring += Xinha._colorToRgb(colors[i]);
      if (i + 1 < colors.length)
      colorstring += " ";
    }
    return colorstring;
  }

  return Xinha._colorToRgb(color);
}
