/** Save the content of the editor to the text area when blurring (losing focus).
 *
 * @author Peter Siewert 
 * @see Ticket #1433
 */
 
SaveOnBlur._pluginInfo = {
  name:"SaveOnBlur",
  version:"1.0",
  developer:"Peter Siewert",
  developer_url:"http://xinha.org",
  sponsor:"",
  sponsor_url:"",
  license:"htmlArea"
};

function SaveOnBlur(editor){
  this.editor=editor;
}

SaveOnBlur.prototype.onKeyPress=function(){
  this.queue_xinha_update_textarea();
  return false;
};

SaveOnBlur.prototype.onMouseDown=function(){
  this.queue_xinha_update_textarea();
  return false;
};

SaveOnBlur.prototype.queue_xinha_update_textarea = function()
{
  var blurry = this;
  
  if(!this._attach_blur)
  {
  
    Xinha._addEvent(this.editor._iframe, 'blur', function(){ blurry.xinha_update_textarea(); }); // IE
    Xinha._addEvent(this.editor._doc, 'blur',    function(){ blurry.xinha_update_textarea(); }); // GECKO
    this._attach_blur = 1;
  }
}

SaveOnBlur.prototype.xinha_update_textarea = function()
{
  this.editor._textArea.value=this.editor.outwardHtml(this.editor.getHTML());
}
