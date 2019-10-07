/**
 EncodeOutput for Xinha
 ===============================================================================
     
 At the last moment before submitting, encode the textarea into one of various 
 encodings, also decode it if the textarea is loaded with encoded content.
 
 This is done to protect against mod_security problems, by encoding it before
 submitting then there is less chance that mod_security will detect your 
 html submission as something unpleasant.
 
 Encoding style is specified via a configuration option, for example
 
 {{{
   xinha_config.EncodeOutput.encoder = 'r13';
 }}}
 
 The available encodings are:
   
 * r13   - html is rot13 (alphanum only is rotated)      -- prefix :!r13!:
 * b64   - html is base64 encoded                        -- prefix :!b64!:
 * 64r   - html is base64 encoded and then that is rot13 -- prefix :!64r!:
 * r64   - html is rot13 and then that is base64         -- prefix :!r64!:
 * false - no encoding performed (decoding still will if prefixed)
  
 You will of course need to detect the prefix (eg :!r13!:) on fields in your 
 server side code and unencode them, for example in PHP you can do..
   
 if(preg_match('/^(:!r13!:)/', $_POST['myHtmlArea']))
 {
   // Remove the prefix
   $_POST['myHtmlArea'] = (string) substr($_POST['myHtmlArea'], 7);
   
   // rot13 it
   $_POST['myHtmlArea'] = str_rot13($_POST['myHtmlArea']);
 }
 elseif(preg_match('/^(:!b64!:)/', $_POST['myHtmlArea']))
 {
   // Remove the prefix
   $_POST['myHtmlArea'] = (string) substr($_POST['myHtmlArea'], 7);
   
   // b64 it
   $_POST['myHtmlArea'] = base64_decode($_POST['myHtmlArea']);
 }
 elseif(preg_match('/^(:!64r!:)/', $_POST['myHtmlArea']))
 {
   // Remove the prefix
   $_POST['myHtmlArea'] = (string) substr($_POST['myHtmlArea'], 7);
   
   // rot13 it
   $_POST['myHtmlArea'] = str_rot13($_POST['myHtmlArea']);
   
   // b64 it
   $_POST['myHtmlArea'] = base64_decode($_POST['myHtmlArea']);
 }
 elseif(preg_match('/^(:!64r!:)/', $_POST['myHtmlArea']))
 {
   // Remove the prefix
   $_POST['myHtmlArea'] = (string) substr($_POST['myHtmlArea'], 7);
   
   // b64 it
   $_POST['myHtmlArea'] = base64_decode($_POST['myHtmlArea']);
   
   // rot13 it
   $_POST['myHtmlArea'] = str_rot13($_POST['myHtmlArea']);
 }

*/

EncodeOutput._pluginInfo = {
  name          : "EncodeOutput",
  version       : "1.0",
  developer     : "Gogo Internet Services Limited",
  developer_url : "http://www.gogo.co.nz",
  sponsor       : "",
  sponsor_url   : "",
  license       : "htmlArea"
}

Xinha.Config.prototype.EncodeOutput = 
{
  // One of
  // 
  //   r13   - html is rot13 (alphanum only is rotated)      -- prefix :!r13!:
  //   b64   - html is base64 encoded                        -- prefix :!b64!:
  //   64r   - html is base64 encoded and then that is rot13 -- prefix :!64r!:
  //   r64   - html is rot13 and then that is base64         -- prefix :!r64!:
  //
  //   false - no encoding performed (decoding still will if prefixed)
  
  encoder:   'r13'
  
}

function EncodeOutput(editor)
{
  this.editor = editor;
}

EncodeOutput.prototype.rot13 = function (s)
{
  return (s ? s : this).split('').map(function(_)
  {
      if (!_.match(/[A-Za-z]/)) return _;
      c = Math.floor(_.charCodeAt(0) / 97);
      k = (_.toLowerCase().charCodeAt(0) - 83) % 26 || 26;
      return String.fromCharCode(k + ((c == 0) ? 64 : 96));
  }).join('');
}

EncodeOutput.prototype.unrot13 = function (s)
{
  return this.rot13(s);
}

EncodeOutput.prototype.b64 = function (s)
{
  return Xinha.base64_encode(s);
}

EncodeOutput.prototype.unb64 = function (s)
{
  return Xinha.base64_decode(s);
}

EncodeOutput.prototype.onGenerate = function ()
{

}
EncodeOutput.prototype.onGenerateOnce = function ()
{

}

/* If the inward html is r13, de-encode it.
 * note that we do not encode in outwardHtml because we don't want
 * to mess up the source code view, this de-code in inward is just
 * to catch any left-overs when you use the back button, or submit 
 * in code view mode.
 */

EncodeOutput.prototype.inwardHtml = function(html)
{
  if(html.match(/^:!r13!:/))
  {
    // Clean up a hanging rot13, this will happen if the form is submitted
    // while in text mode, and it's submitted to a new window/tab
    html = this.unrot13(html.substring(7));
  }    
  else if(html.match(/^:!b64:!/))
  {
    html = this.unb64(html.substring(7));
  }
  else if(html.match(/^:!64r!:/))
  {
    html = this.unb64(this.unrot13(html.substring(7)));
  }
  else if(html.match(/^:!r64!:/))
  {
    html = this.unrot13(this.unb64(html.substring(7)));
  }
  
  return html;
}

EncodeOutput.prototype.outwardHtml = function(html)
{
  return html;
}

EncodeOutput.prototype.onUpdateToolbar = function ()
{
  return false;
}

EncodeOutput.prototype.onExecCommand = function ( cmdID, UI, param )
{
  return false;
}

EncodeOutput.prototype.onKeyPress = function ( event )
{
  return false;
}

EncodeOutput.prototype.onMouseDown = function ( event )
{
  return false;
}

EncodeOutput.prototype.onBeforeSubmit = function ()
{
  return false;
}

EncodeOutput.prototype.onBeforeSubmitTextArea = function()
{    
  switch(this.editor.config.EncodeOutput.encoder)
  {
    case 'r64':
      this.editor._textArea.value = ':!r64!:' + this.b64(this.rot13(this.editor._textArea.value));
      break;
      
    case '64r':
      this.editor._textArea.value = ':!64r!:' + this.rot13(this.b64(this.editor._textArea.value));
      break;
      
    case 'b64':
      this.editor._textArea.value = ':!b64!:' + this.b64(this.editor._textArea.value);
      break;
          
    case 'r13':
      this.editor._textArea.value = ':!r13!:' + this.rot13(this.editor._textArea.value); 
      break;
  }
  
  var e = this;
  window.setTimeout(function(){ e.editor._textArea.value = e.inwardHtml(e.editor._textArea.value); }, 2000);
  
  return false;
}

EncodeOutput.prototype.onBeforeUnload = function ()
{
  return false;
}

EncodeOutput.prototype.onBeforeResize = function (width, height)
{
  return false;
}

EncodeOutput.prototype.onResize = function (width, height)
{
  return false;
}

/**
 * 
 * @param {String} action one of 'add', 'remove', 'hide', 'show', 'multi_hide', 'multi_show'
 * @param {DOMNode|Array} panel either the panel itself or an array like ['left','right','top','bottom']
 */
EncodeOutput.prototype.onPanelChange = function (action, panel)
{
  return false;
}
/**
 * 
 * @param {String} mode either 'textmode' or 'wysiwyg'
 */
EncodeOutput.prototype.onMode = function (mode)
{
  return false;
}

/**
 * 
 * @param {String} mode either 'textmode' or 'wysiwyg'
 */
EncodeOutput.prototype.onBeforeMode = function (mode)
{

  return false;
}
