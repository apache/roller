
  /*--------------------------------------:noTabs=true:tabSize=2:indentSize=2:--
    --
    --
    --  NOTICE Modern Opera does not use this engine any more, it identifies as
    --           and uses WebKit (Opera is these days based on Blink).
    --
    --   People using older versions of Opera that need this engine should
    --   upgrade to a WebKit or Gecko based browser.
    --
    --  This engine is no longer officially supported or tested.
    --
    -----------------------------------------------------------------------------
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
    -- This is the Opera compatability plugin, part of the Xinha core.
    --
    --  The file is loaded as a special plugin by the Xinha Core when
    --  Xinha is being run under an Opera based browser with the Midas
    --  editing API.
    --
    --  It provides implementation and specialisation for various methods
    --  in the core where different approaches per browser are required.
    --
    --  Design Notes::
    --   Most methods here will simply be overriding Xinha.prototype.<method>
    --   and should be called that, but methods specific to Opera should 
    --   be a part of the Opera.prototype, we won't trample on namespace
    --   that way.
    --
    --  $HeadURL: http://svn.xinha.org/trunk/modules/Opera/Opera.js $
    --  $LastChangedDate: 2018-02-19 20:35:49 +1300 (Mon, 19 Feb 2018) $
    --  $LastChangedRevision: 1402 $
    --  $LastChangedBy: gogo $
    --------------------------------------------------------------------------*/
                                                    
Opera._pluginInfo = {
  name          : "Opera",
  origin        : "Xinha Core",
  version       : "$LastChangedRevision: 1402 $".replace(/^[^:]*:\s*(.*)\s*\$$/, '$1'),
  developer     : "The Xinha Core Developer Team",
  developer_url : "$HeadURL: http://svn.xinha.org/trunk/modules/Opera/Opera.js $".replace(/^[^:]*:\s*(.*)\s*\$$/, '$1'),
  sponsor       : "Gogo Internet Services Limited",
  sponsor_url   : "http://www.gogo.co.nz/",
  license       : "htmlArea"
};

function Opera(editor) {
  this.editor = editor;  
  editor.Opera = this;
}

/** Allow Opera to handle some key events in a special way.
 */
Opera.prototype.onKeyPress = function(ev)
{
  var editor = this.editor;
  var s = editor.getSelection();
  
  // Handle shortcuts
  if(editor.isShortCut(ev))
  {
    switch(editor.getKey(ev).toLowerCase())
    {
      case 'z':
      {
        if(editor._unLink && editor._unlinkOnUndo)
        {
          Xinha._stopEvent(ev);
          editor._unLink();
          editor.updateToolbar();
          return true;
        }
      }
      break;
      
      case 'a':
      {
        // KEY select all
        sel = editor.getSelection();
        sel.removeAllRanges();
        range = editor.createRange();
        range.selectNodeContents(editor._doc.body);
        sel.addRange(range);
        Xinha._stopEvent(ev);
        return true;
      }
      break;
      
      case 'v':
      {
        // If we are not using htmlareaPaste, don't let Xinha try and be fancy but let the 
        // event be handled normally by the browser (don't stopEvent it)
        if(!editor.config.htmlareaPaste)
        {          
          return true;
        }
      }
      break;
    }
  }
  
  // Handle normal characters
  switch(editor.getKey(ev))
  {
    // Space, see if the text just typed looks like a URL, or email address
    // and link it appropriatly
    case ' ':
    {      
      var autoWrap = function (textNode, tag)
      {
        var rightText = textNode.nextSibling;
        if ( typeof tag == 'string')
        {
          tag = editor._doc.createElement(tag);
        }
        var a = textNode.parentNode.insertBefore(tag, rightText);
        Xinha.removeFromParent(textNode);
        a.appendChild(textNode);
        rightText.data = ' ' + rightText.data;
    
        s.collapse(rightText, 1);
    
        editor._unLink = function()
        {
          var t = a.firstChild;
          a.removeChild(t);
          a.parentNode.insertBefore(t, a);
          Xinha.removeFromParent(a);
          editor._unLink = null;
          editor._unlinkOnUndo = false;
        };
        editor._unlinkOnUndo = true;
    
        return a;
      };
  
      if ( editor.config.convertUrlsToLinks && s && s.isCollapsed && s.anchorNode.nodeType == 3 && s.anchorNode.data.length > 3 && s.anchorNode.data.indexOf('.') >= 0 )
      {
        var midStart = s.anchorNode.data.substring(0,s.anchorOffset).search(/\S{4,}$/);
        if ( midStart == -1 )
        {
          break;
        }

        if ( editor._getFirstAncestor(s, 'a') )
        {
          break; // already in an anchor
        }

        var matchData = s.anchorNode.data.substring(0,s.anchorOffset).replace(/^.*?(\S*)$/, '$1');

        var mEmail = matchData.match(Xinha.RE_email);
        if ( mEmail )
        {
          var leftTextEmail  = s.anchorNode;
          var rightTextEmail = leftTextEmail.splitText(s.anchorOffset);
          var midTextEmail   = leftTextEmail.splitText(midStart);

          autoWrap(midTextEmail, 'a').href = 'mailto:' + mEmail[0];
          break;
        }

        RE_date = /([0-9]+\.)+/; //could be date or ip or something else ...
        RE_ip = /(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)/;
        var mUrl = matchData.match(Xinha.RE_url);
        if ( mUrl )
        {
          if (RE_date.test(matchData))
          {
            break; //ray: disabling linking of IP numbers because of general bugginess (see Ticket #1085)
            /*if (!RE_ip.test(matchData)) 
            {
              break;
            }*/
          } 
          var leftTextUrl  = s.anchorNode;
          var rightTextUrl = leftTextUrl.splitText(s.anchorOffset);
          var midTextUrl   = leftTextUrl.splitText(midStart);
          autoWrap(midTextUrl, 'a').href = (mUrl[1] ? mUrl[1] : 'http://') + mUrl[2];
          break;
        }
      }
    }
    break;    
  }
  
  // Handle special keys
  switch ( ev.keyCode )
  {    
/*  This is now handled by a plugin  
    case 13: // ENTER

    break;*/

    case 27: // ESCAPE
    {
      if ( editor._unLink )
      {
        editor._unLink();
        Xinha._stopEvent(ev);
      }
      break;
    }
    break;
    
    case 8: // KEY backspace
    case 46: // KEY delete
    {
      // We handle the mozilla backspace directly??
      if ( !ev.shiftKey && this.handleBackspace() )
      {
        Xinha._stopEvent(ev);
      }
    }
    
    default:
    {
        editor._unlinkOnUndo = false;

        // Handle the "auto-linking", specifically this bit of code sets up a handler on
        // an self-titled anchor (eg <a href="http://www.gogo.co.nz/">www.gogo.co.nz</a>)
        // when the text content is edited, such that it will update the href on the anchor
        
        if ( s.anchorNode && s.anchorNode.nodeType == 3 )
        {
          // See if we might be changing a link
          var a = editor._getFirstAncestor(s, 'a');
          // @todo: we probably need here to inform the setTimeout below that we not changing a link and not start another setTimeout
          if ( !a )
          {
            break; // not an anchor
          } 
          
          if ( !a._updateAnchTimeout )
          {
            if ( s.anchorNode.data.match(Xinha.RE_email) && a.href.match('mailto:' + s.anchorNode.data.trim()) )
            {
              var textNode = s.anchorNode;
              var fnAnchor = function()
              {
                a.href = 'mailto:' + textNode.data.trim();
                // @fixme: why the hell do another timeout is started ?
                //         This lead to never ending timer if we dont remove this line
                //         But when removed, the email is not correctly updated
                //
                // - to fix this we should make fnAnchor check to see if textNode.data has
                //   stopped changing for say 5 seconds and if so we do not make this setTimeout 
                a._updateAnchTimeout = setTimeout(fnAnchor, 250);
              };
              a._updateAnchTimeout = setTimeout(fnAnchor, 1000);
              break;
            }

            var m = s.anchorNode.data.match(Xinha.RE_url);

            if ( m && a.href.match(new RegExp( 'http(s)?://' + Xinha.escapeStringForRegExp( s.anchorNode.data.trim() ) ) ) )
            {
              var txtNode = s.anchorNode;
              var fnUrl = function()
              {
                // Sometimes m is undefined becase the url is not an url anymore (was www.url.com and become for example www.url)
                // ray: shouldn't the link be un-linked then?
                m = txtNode.data.match(Xinha.RE_url);
                if(m)
                {
                  a.href = (m[1] ? m[1] : 'http://') + m[2];
                }
                
                // @fixme: why the hell do another timeout is started ?
                //         This lead to never ending timer if we dont remove this line
                //         But when removed, the url is not correctly updated
                //
                // - to fix this we should make fnUrl check to see if textNode.data has
                //   stopped changing for say 5 seconds and if so we do not make this setTimeout
                a._updateAnchTimeout = setTimeout(fnUrl, 250);
              };
              a._updateAnchTimeout = setTimeout(fnUrl, 1000);
            }
          }        
        }                
    }
    break;
  }

  return false; // Let other plugins etc continue from here.
}

/** When backspace is hit, the Opera onKeyPress will execute this method.
 *  I don't remember what the exact purpose of this is though :-(
 */
 
Opera.prototype.handleBackspace = function()
{
  var editor = this.editor;
  setTimeout(
    function()
    {
      var sel   = editor.getSelection();
      var range = editor.createRange(sel);
      var SC = range.startContainer;
      var SO = range.startOffset;
      var EC = range.endContainer;
      var EO = range.endOffset;
      var newr = SC.nextSibling;
      if ( SC.nodeType == 3 )
      {
        SC = SC.parentNode;
      }
      if ( ! ( /\S/.test(SC.tagName) ) )
      {
        var p = document.createElement("p");
        while ( SC.firstChild )
        {
          p.appendChild(SC.firstChild);
        }
        SC.parentNode.insertBefore(p, SC);
        Xinha.removeFromParent(SC);
        var r = range.cloneRange();
        r.setStartBefore(newr);
        r.setEndAfter(newr);
        r.extractContents();
        sel.removeAllRanges();
        sel.addRange(r);
      }
    },
    10);
};

Opera.prototype.inwardHtml = function(html)
{
   // Both IE and Opera use strike internally instead of del (#523)
   // Xinha will present del externally (see Xinha.prototype.outwardHtml
   html = html.replace(/<(\/?)del(\s|>|\/)/ig, "<$1strike$2");
   
   return html;
}

Opera.prototype.outwardHtml = function(html)
{

  return html;
}

Opera.prototype.onExecCommand = function(cmdID, UI, param)
{   
    
  switch(cmdID)
  {
    
    /*case 'paste':
    {
      alert(Xinha._lc("The Paste button does not work in Mozilla based web browsers (technical security reasons). Press CTRL-V on your keyboard to paste directly.", "Opera"));
      return true; // Indicate paste is done, stop command being issued to browser by Xinha.prototype.execCommand
    }
    break;*/
    
    case 'removeformat':
      var editor = this.editor;
      var sel = editor.getSelection();
      var selSave = editor.saveSelection(sel);
      var range = editor.createRange(sel);

      var els = editor._doc.body.getElementsByTagName('*');

      var start = ( range.startContainer.nodeType == 1 ) ? range.startContainer : range.startContainer.parentNode;
      var i, el;
      if (sel.isCollapsed) range.selectNodeContents(editor._doc.body);
      
      for (i=0; i<els.length;i++)
      {
        el = els[i];
        if ( range.isPointInRange(el, 0) || (els[i] == start && range.startOffset == 0))
        {
          el.removeAttribute('style');
        }
      }
      this.editor._doc.execCommand(cmdID, UI, param);
      editor.restoreSelection(selSave);
      return true;
    break;
    
  }
  
  return false;
}

Opera.prototype.onMouseDown = function(ev)
{   
  
}


/*--------------------------------------------------------------------------*/
/*------- IMPLEMENTATION OF THE ABSTRACT "Xinha.prototype" METHODS ---------*/
/*--------------------------------------------------------------------------*/



/** Insert a node at the current selection point. 
 * @param toBeInserted DomNode
 */

Xinha.prototype.insertNodeAtSelection = function(toBeInserted)
{
  if ( toBeInserted.ownerDocument != this._doc )
  {
    try 
    {
      toBeInserted = this._doc.adoptNode( toBeInserted );
    } catch (e) {}
  }
  
  this.focusEditor();
  
  var sel = this.getSelection();      
  var range = this.createRange(sel);
    
  range.deleteContents();
  
  var node = range.startContainer;
  var pos = range.startOffset;
  var selnode = toBeInserted;
  
  sel.removeAllRanges();
  
  switch ( node.nodeType )
  {
    case 3: // Node.TEXT_NODE
      // we have to split it at the caret position.
      if ( toBeInserted.nodeType == 3 )
      {
        // do optimized insertion
        node.insertData(pos, toBeInserted.data);
        range = this.createRange();
        range.setEnd(node, pos + toBeInserted.length);
        range.setStart(node, pos + toBeInserted.length);
        sel.addRange(range);
      }
      else
      {
        node = node.splitText(pos);
        if ( toBeInserted.nodeType == 11 /* Node.DOCUMENT_FRAGMENT_NODE */ )
        {
          selnode = selnode.firstChild;
        }
        node.parentNode.insertBefore(toBeInserted, node);
        this.selectNodeContents(selnode);
        this.updateToolbar();
      }
    break;
    
    case 1: // Node.ELEMENT_NODE
      if ( toBeInserted.nodeType == 11 /* Node.DOCUMENT_FRAGMENT_NODE */ )
      {
        selnode = selnode.firstChild;
      }
      node.insertBefore(toBeInserted, node.childNodes[pos]);
      this.selectNodeContents(selnode);
      this.updateToolbar();
    break;
  }
};
  
/** Get the parent element of the supplied or current selection. 
 *  @param   sel optional selection as returned by getSelection
 *  @returns DomNode
 */
 
Xinha.prototype.getParentElement = function(sel)
{
  if ( typeof sel == 'undefined' )
  {
    sel = this.getSelection();
  }
  var range = this.createRange(sel);
  try
  {
    var p = range.commonAncestorContainer;
    if ( !range.collapsed && range.startContainer == range.endContainer &&
        range.startOffset - range.endOffset <= 1 && range.startContainer.hasChildNodes() )
    {
      p = range.startContainer.childNodes[range.startOffset];
    }

    while ( p.nodeType == 3 )
    {
      p = p.parentNode;
    }
    return p;
  }
  catch (ex)
  {
    return null;
  }
};

/**
 * Returns the selected element, if any.  That is,
 * the element that you have last selected in the "path"
 * at the bottom of the editor, or a "control" (eg image)
 *
 * @returns null | DomNode
 */

Xinha.prototype.activeElement = function(sel)
{
  if ( ( sel === null ) || this.selectionEmpty(sel) )
  {
    return null;
  }

  // For Mozilla we just see if the selection is not collapsed (something is selected)
  // and that the anchor (start of selection) is an element.  This might not be totally
  // correct, we possibly should do a simlar check to IE?
  if ( !sel.isCollapsed )
  {      
    if ( sel.anchorNode.childNodes.length > sel.anchorOffset && sel.anchorNode.childNodes[sel.anchorOffset].nodeType == 1 )
    {
      return sel.anchorNode.childNodes[sel.anchorOffset];
    }
    else if ( sel.anchorNode.nodeType == 1 )
    {
      return sel.anchorNode;
    }
    else
    {
      return null; // return sel.anchorNode.parentNode;
    }
  }
  return null;
};

/** 
 * Determines if the given selection is empty (collapsed).
 * @param selection Selection object as returned by getSelection
 * @returns true|false
 */
 
Xinha.prototype.selectionEmpty = function(sel)
{
  if ( !sel )
  {
    return true;
  }

  if ( typeof sel.isCollapsed != 'undefined' )
  {      
    return sel.isCollapsed;
  }

  return true;
};

/** 
 * Returns a range object to be stored 
 * and later restored with Xinha.prototype.restoreSelection()
 * 
 * @returns Range
 */
Xinha.prototype.saveSelection = function()
{
  return this.createRange(this.getSelection()).cloneRange();
}
/** 
 * Restores a selection previously stored
 * @param savedSelection Range object as returned by Xinha.prototype.restoreSelection()
 */
Xinha.prototype.restoreSelection = function(savedSelection)
{
  var sel = this.getSelection();
  sel.removeAllRanges();
  sel.addRange(savedSelection);
}
/**
 * Selects the contents of the given node.  If the node is a "control" type element, (image, form input, table)
 * the node itself is selected for manipulation.
 *
 * @param node DomNode 
 * @param collapseToStart A boolean that, when supplied, says to collapse the selection. True collapses to the start, and false to the end.
 */
 
Xinha.prototype.selectNodeContents = function(node, collapseToStart)
{
  this.focusEditor();
  this.forceRedraw();
  var range;
  var collapsed = typeof collapseToStart == "undefined" ? true : false;
  var sel = this.getSelection();
  range = this._doc.createRange();
  // Tables and Images get selected as "objects" rather than the text contents
  if ( collapsed && node.tagName && node.tagName.toLowerCase().match(/table|img|input|textarea|select/) )
  {
    range.selectNode(node);
  }
  else
  {
    range.selectNodeContents(node);
  }
  sel.removeAllRanges();
  sel.addRange(range);
  if (typeof collapseToStart != "undefined")
  {
    if (collapseToStart)
    {
      sel.collapse(range.startContainer, range.startOffset);
    } else
    {
      sel.collapse(range.endContainer, range.endOffset);
    }
  }
};
  
/** Insert HTML at the current position, deleting the selection if any. 
 *  
 *  @param html string
 */
 
Xinha.prototype.insertHTML = function(html)
{
  var sel = this.getSelection();
  var range = this.createRange(sel);
  this.focusEditor();
  // construct a new document fragment with the given HTML
  var fragment = this._doc.createDocumentFragment();
  var div = this._doc.createElement("div");
  div.innerHTML = html;
  while ( div.firstChild )
  {
    // the following call also removes the node from div
    fragment.appendChild(div.firstChild);
  }
  // this also removes the selection
  var node = this.insertNodeAtSelection(fragment);
};

/** Get the HTML of the current selection.  HTML returned has not been passed through outwardHTML.
 *
 * @returns string
 */
 
Xinha.prototype.getSelectedHTML = function()
{
  var sel = this.getSelection();
  if (sel.isCollapsed) return '';
  var range = this.createRange(sel);
  return Xinha.getHTML(range.cloneContents(), false, this);
};
  

/** Get a Selection object of the current selection.  Note that selection objects are browser specific.
 *
 * @returns Selection
 */
 
Xinha.prototype.getSelection = function()
{
  var sel = this._iframe.contentWindow.getSelection();
  if(sel && sel.focusNode && sel.focusNode.tagName && sel.focusNode.tagName == 'HTML')
  { // Bad news batman, this selection is wonky  
    var bod = this._doc.getElementsByTagName('body')[0];
    var rng = this.createRange();
    rng.selectNodeContents(bod);
    sel.removeAllRanges();
    sel.addRange(rng);
    sel.collapseToEnd();    
  }
  return sel; 
};
  
/** Create a Range object from the given selection.  Note that range objects are browser specific.
 *
 *  @param sel Selection object (see getSelection)
 *  @returns Range
 */
 
Xinha.prototype.createRange = function(sel)
{
  this.activateEditor();
  if ( typeof sel != "undefined" )
  {
    try
    {
      return sel.getRangeAt(0);
    }
    catch(ex)
    {
      return this._doc.createRange();
    }
  }
  else
  {
    return this._doc.createRange();
  }
};

/** 
 * @NOTE I don't have a way to test this any more (don't have old opera version
 *   and don't care enough to find one), I'm assuming it will probably be close
 *   to Gecko so I have just copied it directly.
 * 
 * Due to browser differences, some keys which Xinha prefers to call a keyPress
 *   do not get an actual keypress event.  This browser specific function 
 *   overridden in the browser's engine (eg modules/WebKit/WebKit.js) as required
 *   takes a keydown event type and tells us if we should treat it as a 
 *   keypress event type.
 *
 *  To be clear, the keys we are interested here are
 *        Escape, Tab, Backspace, Delete, Enter
 *   these are "non printable" characters which we still want to regard generally
 *   as a keypress.  
 * 
 *  If the browser does not report these as a keypress
 *   ( https://dvcs.w3.org/hg/d4e/raw-file/tip/key-event-test.html )
 *   then this function must return true for such keydown events as it is
 *   given.
 * 
 * @param KeyboardEvent with keyEvent.type == keydown
 * @return boolean
 */

Xinha.prototype.isKeyDownThatShouldGetButDoesNotGetAKeyPressEvent = function(keyEvent)
{
  // Dom 3
  if(typeof keyEvent.key != 'undefined')
  {
    // Found using IE11 (which uses Gecko)
    //   this seems to be a reasonable way to distinguish
    //   between IE11 and other Gecko browsers which do 
    //   not provide the "Old DOM3" .char property
    if(typeof keyEvent.char != 'undefined')
    {
      if(keyEvent.key.match(/^(Tab|Backspace|Del)/))
      {
        return true;
      }
    }
    
    // Firefox reports everything we need as a keypress
    // correctly (in terms of Xinha)
  }
  // Legacy
  else
  {
    // Even very old firefox reports everything we need as a keypress
    // correctly (in terms of Xinha)
  }
};

/** Return the character (as a string) of a keyEvent  - ie, press the 'a' key and
 *  this method will return 'a', press SHIFT-a and it will return 'A'.
 * 
 *  @param   keyEvent
 *  @returns string
 */
                                   
Xinha.prototype.getKey = function(keyEvent)
{
  return String.fromCharCode(keyEvent.charCode);
}

/** Return the HTML string of the given Element, including the Element.
 * 
 * @param element HTML Element DomNode
 * @returns string
 */
 
Xinha.getOuterHTML = function(element)
{
  return (new XMLSerializer()).serializeToString(element);
};


/* Caret position remembering when switch between text and html mode.
 * Largely this is the same as for Gecko except:
 *
 * Opera does not have window.find() so we use instead an element with known
 * id (<span id="XinhaOperaCaretMarker">MARK</span>) so that we can 
 * do _doc.getElementById() on it.
 * 
 * Opera for some reason will not set the insert point (flashing caret)
 * anyway though, in theory, the iframe is focused (in findCC below) and then
 * the selection (containing the span above) is collapsed, it should show
 * caret.  I don't know why not.  Seems to require user to actually
 * click to get the caret to show (or type anything without it acting wierd)? 
 * Very annoying.
 *
 */ 
Xinha.cc = String.fromCharCode(8286);
Xinha.prototype.setCC = function ( target )
{
  // Do a two step caret insertion, first a single char, then we'll replace that with the 
  // id'd span.
  var cc = Xinha.cc;
  
  try
  {
    if ( target == "textarea" )
    {
      var ta = this._textArea;
      var index = ta.selectionStart;
      var before = ta.value.substring( 0, index )
      var after = ta.value.substring( index, ta.value.length );

      if ( after.match(/^[^<]*>/) ) // make sure cursor is in an editable area (outside tags, script blocks, entities, and inside the body)
      {
        var tagEnd = after.indexOf(">") + 1;
        ta.value = before + after.substring( 0, tagEnd ) + cc + after.substring( tagEnd, after.length );
      }
      else ta.value = before + cc + after;
      ta.value = ta.value.replace(new RegExp ('(&[^'+cc+';]*?)('+cc+')([^'+cc+']*?;)'), "$1$3$2");
      ta.value = ta.value.replace(new RegExp ('(<script[^>]*>[^'+cc+']*?)('+cc+')([^'+cc+']*?<\/script>)'), "$1$3$2");
      ta.value = ta.value.replace(new RegExp ('^([^'+cc+']*)('+cc+')([^'+cc+']*<body[^>]*>)(.*?)'), "$1$3$2$4");
      
      ta.value = ta.value.replace(cc, '<span id="XinhaOperaCaretMarker">MARK</span>');
    }
    else
    {
      var sel = this.getSelection();
      
      var mark =  this._doc.createElement('span');
      mark.id  = 'XinhaOperaCaretMarker';
      sel.getRangeAt(0).insertNode( mark );
      
    }
  } catch (e) {}
};

Xinha.prototype.findCC = function ( target )
{
  if ( target == 'textarea' )
  {  
    var ta = this._textArea;
    var pos = ta.value.search( /(<span\s+id="XinhaOperaCaretMarker"\s*\/?>((\s|(MARK))*<\/span>)?)/ );    
    if ( pos == -1 ) return;
    var cc = RegExp.$1;
    var end = pos + cc.length;
    var before =  ta.value.substring( 0, pos );
    var after = ta.value.substring( end, ta.value.length );
    ta.value = before ;
  
    ta.scrollTop = ta.scrollHeight;
    var scrollPos = ta.scrollTop;
    
    ta.value += after;
    ta.setSelectionRange(pos,pos);
  
    ta.focus();
    
    ta.scrollTop = scrollPos;

  }
  else
  {    
    var marker = this._doc.getElementById('XinhaOperaCaretMarker');
    if(marker) 
    {
      this.focusEditor();// this._iframe.contentWindow.focus();
      
      var rng = this.createRange();
      rng.selectNode(marker);
      
      var sel = this.getSelection();
      sel.addRange(rng); 
      sel.collapseToStart();
      
      this.scrollToElement(marker);
      
      marker.parentNode.removeChild(marker);
      return;
    }
  }
};

/*--------------------------------------------------------------------------*/
/*------------ EXTEND SOME STANDARD "Xinha.prototype" METHODS --------------*/
/*--------------------------------------------------------------------------*/

/*
Xinha.prototype._standardToggleBorders = Xinha.prototype._toggleBorders;
Xinha.prototype._toggleBorders = function()
{
  var result = this._standardToggleBorders();
    
  // flashing the display forces moz to listen (JB:18-04-2005) - #102
  var tables = this._doc.getElementsByTagName('TABLE');
  for(var i = 0; i < tables.length; i++)
  {
    tables[i].style.display="none";
    tables[i].style.display="table";
  }
  
  return result;
};
*/

/** Return the doctype of a document, if set
 * 
 * @param doc DOM element document
 * @returns string the actual doctype
 */
Xinha.getDoctype = function (doc)
{
  var d = '';
  if (doc.doctype)
  {
    d += '<!DOCTYPE ' + doc.doctype.name + " PUBLIC ";
    d +=  doc.doctype.publicId ? '"' + doc.doctype.publicId + '"' : '';  
    d +=  doc.doctype.systemId ? ' "'+ doc.doctype.systemId + '"' : ''; 
    d += ">";
  }
  return d;
};

Xinha.prototype._standardInitIframe = Xinha.prototype.initIframe;
Xinha.prototype.initIframe = function()
{
  if(!this._iframeLoadDone) 
  {    
    if(this._iframe.contentWindow && this._iframe.contentWindow.xinhaReadyToRoll)
    {
      this._iframeLoadDone = true;
      this._standardInitIframe();
    }
    else
    {
      // Timeout a little (Opera is a bit dodgy about using an event)
      var editor = this;
      setTimeout( function() { editor.initIframe() }, 5);
    }
  }
}

// For some reason, Opera doesn't listen to addEventListener for at least select elements with event = change
// so we will have to intercept those and use a Dom0 event, these are used eg for the fontname/size/format
// dropdowns in the toolbar
Xinha._addEventOperaOrig = Xinha._addEvent;
Xinha._addEvent = function(el, evname, func)
{
  if(el.tagName && el.tagName.toLowerCase() == 'select' && evname == 'change')
  {
    return Xinha.addDom0Event(el,evname,func);
  }
  
  return Xinha._addEventOperaOrig(el,evname,func);
}
