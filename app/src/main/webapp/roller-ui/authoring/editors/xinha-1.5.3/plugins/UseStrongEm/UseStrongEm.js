/*------------------------------------------*\
 UseStrongEm for Xinha
 _______________________
     
 When using the execCommand "bold" or "italic" ensure that what gets inserted is an <em> or <strong>
 instead of an <i> or <b>.
 
 This plugin uses jQuery, because I am very lazy.
 
\*------------------------------------------*/

UseStrongEm._pluginInfo = {
  name          : "UseStrongEm",
  version       : "1.0",
  developer     : "James Sleeman",
  developer_url : "http://gogo.co.nz",
  sponsor       : "Gogo Internet Services Limited",
  sponsor_url   : "http://gogo.co.nz",
  license       : "htmlArea"
}

Xinha.Config.prototype.UseStrongEm = 
{
  useEm         : true,
  useStrong     : true,
  twoStageStrong: true
}

Xinha.loadLibrary('jQuery');

function UseStrongEm(editor)
{
	this.editor = editor;
  
  if(editor.config.preserveI == null)
  {
    editor.config.preserveI = true;
  }
  
  if(editor.config.preserveB == null)
  {
    editor.config.preserveB = true;
  }
}

// Adapted from https://stackoverflow.com/questions/918792/use-jquery-to-change-an-html-tag
UseStrongEm.prototype.replaceTag = function(currentElem, newTagName) {
    var currentElem = jQuery(currentElem);
    
    currentElem.wrapAll('<'+newTagName+'>');
    
    var newTag = currentElem.parent()[0];    
    currentElem.contents().unwrap();
    
    return newTag;
}

UseStrongEm.prototype.onGenerate = function ()
{

}
UseStrongEm.prototype.onGenerateOnce = function ()
{

}
UseStrongEm.prototype.inwardHtml = function(html)
{
	return html;
}
UseStrongEm.prototype.outwardHtml = function(html)
{
	return html;
}
UseStrongEm.prototype.onUpdateToolbar = function ()
{
	return false;
}

UseStrongEm.prototype.onExecCommand = function ( cmdID, UI, param )
{
  var self = this;
  
  if(this.editor.config.UseStrongEm.useEm && cmdID == 'italic')
  {
    // Flag all existing <i> with a special class name
    jQuery(this.editor._doc).find('i').addClass('usestrongem');
    
    try
    {
      this.editor._doc.execCommand('styleWithCSS', false, false);
      this.editor._doc.execCommand(cmdID, UI, param);
    } catch(ex) { }
    
    // We want to preserve anything that is already an "i"
    jQuery(this.editor._doc).find('i').each(function(i,e){
      if(jQuery(e).hasClass('usestrongem'))
      {
        jQuery(e).removeClass('usestrongem');
      }
      else
      {
        // This must be a newly added i, so change it to an em
        self.editor.selectNodeContents(self.replaceTag(e, 'em'));
      }
    });
    
    return true;
  }
  else if(this.editor.config.UseStrongEm.useStrong && cmdID == 'bold')
  {
    if(this.editor.config.UseStrongEm.twoStageStrong)
    {
      // Two Stage Strong works something like how Firefox seems to do it
      // (it's possible FF's default bevhaviour is CSS dependant)
      // 
      // Webkit has some issues with bolding and unbolding regardless
      // if we use b or strong so twoStageStrong helps here as well.
      
      // Look to see if the current selection includes
      // one strong, if so, add another, this works more like firefox
      // two strongs, if so remove the inner and change the outer to a span
      // no strongs, fall through to add an initial strong
      
      
      var ancestors = this.editor.getAllAncestors();
      var strongs = [ ];
      for(var i = 0; i < ancestors.length; i++)
      {
        if(ancestors[i].tagName.toLowerCase() == 'strong')
        {
          strongs.push(ancestors[i]);
        }
      }
      
      if(strongs.length == 1)
      {
        jQuery(strongs[0]).wrapAll('<strong>');
        // Select the new inner strong
        this.editor.selectNodeContents(strongs[0]);
        return true;
      }
      else if(strongs.length >= 2)
      {
        // Remove the inner
        jQuery(strongs[0]).contents().unwrap();
        this.editor.selectNodeContents(this.replaceTag(strongs[1], 'span'));
        
        return true;
      }
      
    }
    
    // Flag all existing <b> with a special class name
    jQuery(this.editor._doc).find('b').addClass('usestrongem');
    
    try
    {
      this.editor._doc.execCommand('styleWithCSS', false, false);
      this.editor._doc.execCommand(cmdID, UI, param);
    } catch(ex) { }
        
    // Find any newly added b and turn it into a strong, and 
    // remove the special class from any existing b
    jQuery(self.editor._doc).find('b').each(function(i,e){
      if(jQuery(e).hasClass('usestrongem'))
      {
        jQuery(e).removeClass('usestrongem');
      }
      else
      {
        // This must be a newly added b, so change it to an strong
        var z = self.replaceTag(e, 'strong')
        self.editor.selectNodeContents(z);
      }
    });
    
    
    return true;
  }
  
	return false;
}

UseStrongEm.prototype.onKeyDown = function ( event )
{
  return false;
}

UseStrongEm.prototype.onKeyPress = function ( event )
{
	return false;
}

UseStrongEm.prototype.onOnShortCut = function ( event , shortCut )
{
  // Where shortCut is a single character, eg if you press ctrl-a, then
  //  shortCut == 'a'
  return false;
}

UseStrongEm.prototype.onKeyUp = function ( event )
{
  return false;
}

UseStrongEm.prototype.onMouseDown = function ( event )
{
	return false;
}

UseStrongEm.prototype.onBeforeSubmit = function ()
{
	return false;
}

UseStrongEm.prototype.onBeforeUnload = function ()
{
	return false;
}

UseStrongEm.prototype.onBeforeResize = function (width, height)
{
	return false;
}
UseStrongEm.prototype.onResize = function (width, height)
{
	return false;
}
/**
 * 
 * @param {String} action one of 'add', 'remove', 'hide', 'show', 'multi_hide', 'multi_show'
 * @param {DOMNode|Array} panel either the panel itself or an array like ['left','right','top','bottom']
 */
UseStrongEm.prototype.onPanelChange = function (action, panel)
{
	return false;
}
/**
 * 
 * @param {String} mode either 'textmode' or 'wysiwyg'
 */
UseStrongEm.prototype.onMode = function (mode)
{
	return false;
}
/**
 * 
 * @param {String} mode either 'textmode' or 'wysiwyg'
 */
UseStrongEm.prototype.onBeforeMode = function (mode)
{
	return false;
}
