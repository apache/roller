/**
  = WebKit Image Resizer =
  
  The WebKit based (or similar) browsers, including Chrome (and Edge)
  don't support drag-to-size images or tables, which is a pain.
  
  This plugin implements the EditorBoost jQuery resizing plugin
    http://www.editorboost.net/Webkitresize/Index
  jQuery is required, naturally, if it's not already loaded 
  then we will load our own version, so ensure you load your version
  first if you need a newer one (then this might break, but oh well).
  
    
  == Usage ==._
  Instruct Xinha to load the WebKitImageResuze plugin (follow the NewbieGuide),
  
  You can also specify to enable the TD and TABLE resizing for Gecko 
  (Firefox, and IE11 predominatly) as these also don't have support for
  sizing those (but can size images)., this is enabled by default, you
  can disable with...
  
  xinha_config.WebKitResize.enableTDForGeckoAlso = false;
  xinha_config.WebKitResize.enableTABLEForGeckoAlso = false;
  
  == Caution ==
  This only works acceptably in either:
    Standards Mode  (eg Doctype <!DOCTYPE html> )
    Quirks Mode     (eg no doctype              )
    
  it does not work great in "Almost Standards Mode" because scrolling throws
  the resize border out of place, ,that's ok if you don't need to scroll, 
  either because your text is small, or you are in FullScreen mode or 
  something, but anyway.
  
 
 * @author $Author$
 * @version $Id$
 * @package WebKitResize
 */


WebKitResize._pluginInfo = {
  name          : "WebKit Image Resize",
  version       : "1.0",
  developer     : "EditorBoost, James Sleeman (Xinha)",
  developer_url : "http://www.editorboost.net/Webkitresize/Index",
  license       : "GPLv3"
};

Xinha.Config.prototype.WebKitResize = {
  enableTDForGeckoAlso:     true,
  enableTABLEForGeckoAlso:  true
};

function WebKitResize(editor)
{
    this.editor = editor;
    
    if(Xinha.is_webkit || this.editor.config.WebKitResize.enableTDForGeckoAlso || this.editor.config.WebKitResize.enableTABLEForGeckoAlso)
    {
        Xinha.loadLibrary('jQuery')
        .loadScript('jquery.mb.browser.min.js', 'WebKitResize')
        .loadScript('jquery.webkitresize.js',   'WebKitResize');
    }
}


WebKitResize.prototype.onGenerateOnce = function()
{
  // jQuery not loaded yet?
  if(!(typeof jQuery != 'undefined' && jQuery.fn && jQuery.fn.webkitimageresize))
  {
    var self = this;
    window.setTimeout(function(){self.onGenerateOnce()}, 500);
    return;
  }
  
  try
  {
    if(Xinha.is_webkit)
    {
      jQuery(this.editor._iframe).webkitimageresize();
    }
    
    if(Xinha.is_webkit || Xinha.is_gecko && this.editor.config.WebKitResize.enableTABLEForGeckoAlso) 
    {
      jQuery(this.editor._iframe).webkittableresize();
    }
    
    if(Xinha.is_webkit || Xinha.is_gecko && this.editor.config.WebKitResize.enableTDForGeckoAlso) 
    {
      jQuery(this.editor._iframe).webkittdresize();
    }
  }
  catch(e) { }
}

// When changing modes, make sure that we stop displaying the handles
// if they were displaying, otherwise they create duplicates (because
// the images are recreated).
WebKitResize.prototype.onBeforeMode = function(mode)
{
  if(mode == 'textmode')
  {
    if(typeof this.editor._iframe._WebKitImageResizeEnd == 'function')
      this.editor._iframe._WebKitImageResizeEnd();
    
    if(typeof this.editor._iframe._WebKitTableResizeEnd == 'function')
      this.editor._iframe._WebKitTableResizeEnd();
    
    if(typeof this.editor._iframe._WebKitTdResizeEnd == 'function')
      this.editor._iframe._WebKitTdResizeEnd();
  }
}

WebKitResize.prototype.onMode = function(mode)
{
  if(mode == 'textmode')
  {
    
  }
  else
  {
    if(typeof this.editor._iframe._WebKitImageResizeStart == 'function')
      this.editor._iframe._WebKitImageResizeStart();
  }
}