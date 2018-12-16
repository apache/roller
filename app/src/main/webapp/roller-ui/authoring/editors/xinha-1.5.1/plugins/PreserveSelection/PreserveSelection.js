/**
 PreserveSelection for Xinha
 ===============================================================================
 
 Originally developed by AdamJ in ticket #1544 as a Gecko patch.  Pluginised by 
 James Sleeman.
 
 This plugin preserves the selected text when switching between wysiwyg and 
 source modes, eg, select hello world in the wysiwyg mode, and switch to
 source mode and hello world will be selected there also.
 
 Note that this plugin works a bit differently as it over-rides existing 
 features of the Gecko/WebKit module loaded in the browser.
*/

PreserveSelection._pluginInfo = {
  name          : "PreserveSelection",
  version       : "1.0",
  developer     : "AdamJ, James Sleeman",
  developer_url : "http://trac.xinha.org/ticket/1544",
  sponsor       : "",
  sponsor_url   : "",
  license       : "htmlArea"
}

function PreserveSelection(editor)
{
  this.editor = editor;
};

Xinha.ccStart = String.fromCharCode(8286); 
Xinha.ccEnd   = String.fromCharCode(8285); 

PreserveSelection.prototype.onGenerateOnce = function()
{
  var editor = this.editor;
  
  editor.setCC =  function ( target )
  {
    var ccStart = Xinha.ccStart;
    var ccEnd = Xinha.ccEnd;
    try
    {
      if ( target == "textarea" )
      {
        var ta = this._textArea;
        var startIndex = ta.selectionStart;
        var endIndex = ta.selectionEnd;
      
      var after=ta.value.substring( startIndex, ta.value.length );
      if ( after.match(/^[^<]*>/) ) // make sure cursor is in an editable area (outside tags, script blocks, entities, and inside the body)
        {
          var tagEnd = after.indexOf(">") + 1;
          startIndex+=tagEnd;
        }
      
      var after=ta.value.substring( endIndex, ta.value.length );
      if ( after.match(/^[^<]*>/) ) // make sure cursor is in an editable area (outside tags, script blocks, entities, and inside the body)
        {
          var tagEnd = after.indexOf(">") + 1;
          endIndex+=tagEnd;
        }
      
        var before = ta.value.substring( 0, startIndex )
        var middle = ta.value.substring( startIndex, endIndex )
        var after = ta.value.substring( endIndex, ta.value.length );

        ta.value = before + ccStart + middle + ccEnd + after;
        ta.value = ta.value.replace(new RegExp ('(&[^'+ccStart+';]*?)('+ccStart+')([^'+ccStart+']*?;)'), "$1$3$2");
        ta.value = ta.value.replace(new RegExp ('(<script[^>]*>[^'+ccStart+']*?)('+ccStart+')([^'+ccStart+']*?<\/script>)'), "$1$3$2");
        ta.value = ta.value.replace(new RegExp ('^([^'+ccStart+']*)('+ccStart+')([^'+ccStart+']*<body[^>]*>)(.*?)'), "$1$3$2$4");
      }
      else
      {
        var sel = this.getSelection();
      
      var range=sel.getRangeAt(0);
      var collapsed=range.collapsed;
      

      if(range.startContainer.nodeType==3){
        range.startContainer.insertData(range.startOffset,ccStart);
      }else if(range.startContainer.nodeType==1){
        if( range.startOffset ){
          var startTextNode=range.startContainer.insertBefore( this._doc.createTextNode(ccStart), range.startContainer.childNodes[range.startOffset] );
        }else{
          var startTextNode=range.startContainer.appendChild( this._doc.createTextNode(ccStart) );
        }
      }else{
        alert(range.startContainer.nodeType);
      }
      
      
      if( collapsed==false ){
        if(range.endContainer.nodeType==3){
          range.endContainer.insertData(range.endOffset,ccEnd);
        }else if(range.endContainer.nodeType==1){
          if( range.endOffset ){
            var endTextNode=range.endContainer.insertBefore( this._doc.createTextNode(ccEnd), range.endContainer.childNodes[range.endOffset] );
          }else{
            var endTextNode=range.endContainer.appendChild( this._doc.createTextNode(ccEnd) );
          }
        }else{
          alert(range.endContainer.nodeType);
        }
      }
      }
    } catch (e) {}
  };
  
  editor.findCC = function ( target )
  {
    var ccStart = Xinha.ccStart;
    var ccEnd = Xinha.ccEnd;
    
    if ( target == 'textarea' )
    {
      var ta = this._textArea;
      
      var startPos = ta.value.indexOf( ccStart );
      if ( startPos == -1 ) return;
      
      var endPos = ta.value.indexOf( ccEnd );
      
      if ( endPos != -1 ){ 

        if( startPos<endPos ){
          pos1=startPos;
          pos2=endPos;
        }else{
          pos1=endPos;
          pos2=startPos;
        }
      
        var middle = pos1 + ccStart.length;
        var before =  ta.value.substring( 0, pos1 );
        var middle = ta.value.substring( middle, pos2 );
        var after = ta.value.substring(pos2 + ccEnd.length);
        ta.value = before;
      
        ta.scrollTop = ta.scrollHeight;
        var scrollPos = ta.scrollTop;
        
        ta.value += middle;
        ta.value += after;
        ta.setSelectionRange(pos1,pos2-1);
      }else{
        
        var end = startPos + ccStart.length;
        var before =  ta.value.substring( 0, startPos );
        var after = ta.value.substring( end, ta.value.length );
        ta.value = before;
      
        ta.scrollTop = ta.scrollHeight;
        var scrollPos = ta.scrollTop;
        
        ta.value += after;
        ta.setSelectionRange(startPos,startPos);
      }
    
      ta.focus();
      
      ta.scrollTop = scrollPos;

    }
    else
    {
      try
      {
        var doc = this._doc; 
      doc.body.innerHTML = doc.body.innerHTML.replace(new RegExp(ccStart),'<span id="XinhaEditingStart"></span>');
      doc.body.innerHTML = doc.body.innerHTML.replace(new RegExp(ccEnd),'<span id="XinhaEditingEnd"></span>');
      var startEl = doc.getElementById('XinhaEditingStart');
      var endEl = doc.getElementById('XinhaEditingEnd');
      
      this.forceRedraw();
      var range;
      var collapsed = typeof collapseToStart == "undefined" ? true : false;
      var sel = this.getSelection();
      range = this._doc.createRange();
      if ( !startEl ){
        sel.removeAllRanges();
        return;
      }
      // Tables and Images get selected as "objects" rather than the text contents
      if ( !endEl && startEl.tagName && startEl.tagName.toLowerCase().match(/table|img|input|textarea|select/) ){
        range.selectNode(startEl);
      }else{
        range.selectNodeContents(startEl);
      }
      
      if( endEl ){
        range.setStart(startEl,0);
        range.setEnd(endEl,0);
      }

      sel.removeAllRanges();
      sel.addRange(range);
      
      this.scrollToElement(startEl);
      startEl.parentNode.removeChild(startEl);          
      endEl.parentNode.removeChild(endEl);
      
      this._iframe.contentWindow.focus();
      } catch (e) {}
    }
  };

};
