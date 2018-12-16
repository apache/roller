/**
 *  WYSIWYG Wrap Plugin
 * 
 *  The purpose of this plugin is to wrap the content being edited in Xinha
 *  with certain elements of given ID and/or class when in the WYSIWYG view.
 *
 *  The reason for this is to assist when styling content.
 *
 */

WysiwygWrap._pluginInfo = {
  name          : "WYSIWYG Wrap",
  version       : "1.0",
  developer     : "James Sleeman",
  developer_url : "http://www.gogo.co.nz/",
  c_owner       : "James Sleeman",
  license       : "htmlArea"
};

Xinha.Config.prototype.WysiwygWrap =
{
  'elements' : [ ]
  // 'elements' : [ 'div.class#id', 'ul', 'li' ]
};


function WysiwygWrap(editor) {
  this.editor = editor;    
}

/** Take HTML and wrap it with the elements. 
 *
 * @param string
 * @return string
 */
 
WysiwygWrap.prototype.inwardHtml = function(html)
{
  for(var x = this.editor.config.WysiwygWrap.elements.length - 1; x >= 0; x--)
  {
    var e = { tagName: this.editor.config.WysiwygWrap.elements[x], className: '', id: '' };    
    if(e.tagName.match(/#(.+)$/))
    {
      e.id        = RegExp.$1;
      e.tagName   = e.tagName.replace(/#(.+)$/,'');
    }
    
    if(e.tagName.match(/[^.]*\.(.+)$/))
    {
      e.className = RegExp.$1.replace('.', ' ');
      e.tagName   = e.tagName.replace(/\..+$/, '');
    }
    
    if(!e.tagName.length) 
    { 
      e.tagName = 'div'; 
    }
    
    html = '<'+e.tagName+' id="'+e.id+'" class="'+e.className+'">'+html+'</'+e.tagName+'>';
  }
  
  return html;
}

/** Take HTML and strip it from the elements. 
 *
 * @param string
 * @return string
 */
 
WysiwygWrap.prototype.outwardHtml = function(html)
{
  for(var x = 0; x < this.editor.config.WysiwygWrap.elements.length; x++)
  {
    var e = { tagName: this.editor.config.WysiwygWrap.elements[x], className: '', id: '' };    
    if(e.tagName.match(/#(.+)$/))
    {
      e.id        = RegExp.$1;
      e.tagName   = e.tagName.replace(/#(.+)$/,'');
    }
    
    if(e.tagName.match(/[^.]*\.(.+)$/))
    {
      e.className = RegExp.$1.replace('.', ' ');
      e.tagName   = e.tagName.replace(/\..+$/, '');
    }
    
    if(!e.tagName.length) 
    { 
      e.tagName = 'div'; 
    }

    var r1 = new RegExp('^(\\s|\\n|\\r)*<'+e.tagName+'[^>]*>(\\s|\\n|\\r)*', 'i');
    var r2 = new RegExp('(\\s|\\n|\\r)*</'+e.tagName+'[^>]*>(\\s|\\n|\\r)*$', 'i');
  
    html = html.replace(r1, '');
    html = html.replace(r2, '');
  }
  
  return html;
}
