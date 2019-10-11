
  /*--------------------------------------:noTabs=true:tabSize=2:indentSize=2:--
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
    -- This is the WebKit (Safari) compatability plugin, part of the Xinha core.
    --
    --  The file is loaded as a special plugin by the Xinha Core when
    --  Xinha is being run under a Webkit based browser such as Safari
    --
    --  It provides implementation and specialisation for various methods
    --  in the core where different approaches per browser are required.
    --
    --  Design Notes::
    --   Most methods here will simply be overriding Xinha.prototype.<method>
    --   and should be called that, but methods specific to Webkit should 
    --   be a part of the WebKit.prototype, we won't trample on namespace
    --   that way.
    --
    --  $HeadURL: http://svn.xinha.org/trunk/modules/WebKit/WebKit.js $
    --  $LastChangedDate: 2018-02-19 20:35:49 +1300 (Mon, 19 Feb 2018) $
    --  $LastChangedRevision: 1402 $
    --  $LastChangedBy: gogo $
    --------------------------------------------------------------------------*/
                                                    
WebKit._pluginInfo = {
  name          : "WebKit",
  origin        : "Xinha Core",
  version       : "$LastChangedRevision: 1402 $".replace(/^[^:]*:\s*(.*)\s*\$$/, '$1'),
  developer     : "The Xinha Core Developer Team",
  developer_url : "$HeadURL: http://svn.xinha.org/trunk/modules/WebKit/WebKit.js $".replace(/^[^:]*:\s*(.*)\s*\$$/, '$1'),
  sponsor       : "",
  sponsor_url   : "",
  license       : "htmlArea"
};

function WebKit(editor) {
  this.editor = editor;  
  editor.WebKit = this;
}

/** Allow Webkit to handle some key events in a special way.
 */
  
WebKit.prototype.onKeyPress = function(ev)
{
  var editor = this.editor;
  var s = editor.getSelection();
  
  // Handle shortcuts
  if(editor.isShortCut(ev))
  {    
    switch(editor.getKey(ev).toLowerCase())
    {
      case 'z':
        if(editor._unLink && editor._unlinkOnUndo)
        {
          Xinha._stopEvent(ev);
          editor._unLink();
          editor.updateToolbar();
          return true;
        }
      break;
	  
	  case 'a':
        // ctrl-a selects all, but 
      break;
	  
      case 'v':
        // If we are not using htmlareaPaste, don't let Xinha try and be fancy but let the 
        // event be handled normally by the browser (don't stopEvent it)
        if(!editor.config.htmlareaPaste)
        {          
          return true;
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
          return true;
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
          return true;
        }
      }
    break;
  }
  
  // Handle special keys
  switch ( ev.keyCode )
  {    
    case 13: // ENTER
      if( ev.shiftKey  )
      {
        //TODO: here we need to add insert new line
      }
    break;

    case 27: // ESCAPE
      if ( editor._unLink )
      {
        editor._unLink();
        Xinha._stopEvent(ev);
      }
 
    break;
    
    case 9: // KEY tab
    {
      // Note that the ListOperations plugin will handle tabs in list items and indent/outdent those
      // at some point TableOperations might do also
      // so this only has to handle a tab/untab in text
      if(editor.config.tabSpanClass)
      {
        if(!ev.shiftKey)
        {                  //  v-- Avoid lc_parse_strings.php
          editor.insertHTML('<'+'span class="'+editor.config.tabSpanClass+'">'+editor.config.tabSpanContents+'</span>');
          var s = editor.getSelection().collapseToEnd();
        }
        else
        {
          var existingTab = editor.getParentElement();
          if(existingTab && existingTab.className.match(editor.config.tabSpanClass))
          {
            var s = editor.getSelection();
            s.removeAllRanges();
            var r = editor.createRange();
            r.selectNode(existingTab);
            s.addRange(r);
            s.deleteFromDocument();
          }
        }
      }
      
      Xinha._stopEvent(ev);
      
    }
    break;
    
    case 8: // KEY backspace
    case 46: // KEY delete
      // We handle the mozilla backspace directly??
      if ( !ev.shiftKey && this.handleBackspace() )
      {
        Xinha._stopEvent(ev);
      }
    break;
    default:
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
    break;
  }

  return false; // Let other plugins etc continue from here.
}

/** When backspace is hit, the Gecko onKeyPress will execute this method.
 *  I don't remember what the exact purpose of this is though :-(
 *  
 */
 
WebKit.prototype.handleBackspace = function()
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

WebKit.prototype.inwardHtml = function(html)
{
   return html;
}

WebKit.prototype.outwardHtml = function(html)
{
  return html;
}

WebKit.prototype.onExecCommand = function(cmdID, UI, param)
{   
  this.editor._doc.execCommand('styleWithCSS', false, false); //switch styleWithCSS off; seems to make no difference though 
   
  switch(cmdID)
  {
    case 'paste':
      alert(Xinha._lc("The Paste button does not work in this browser for security reasons. Press CTRL-V on your keyboard to paste directly.", "WebKit"));
      return true; // Indicate paste is done, stop command being issued to browser by Xinha.prototype.execCommand
    break;
    case 'removeformat':
      var editor = this.editor;
      var sel = editor.getSelection();
      var selSave = editor.saveSelection(sel);
      var range = editor.createRange(sel);

      var els = editor._doc.getElementsByTagName('*');
      els = Xinha.collectionToArray(els);
      var start = ( range.startContainer.nodeType == 1 ) ? range.startContainer : range.startContainer.parentNode;
      var i,el,newNode, fragment, child,r2 = editor._doc.createRange();

      function clean (el)
      {
        if (el.nodeType != 1) return;
        el.removeAttribute('style');
        for (var j=0; j<el.childNodes.length;j++)
        {
          clean(el.childNodes[j]);
        }
        if ( (el.tagName.toLowerCase() == 'span' && !el.attributes.length ) || el.tagName.toLowerCase() == 'font')
        {
          r2.selectNodeContents(el);
          fragment = r2.extractContents();
          while (fragment.firstChild)
          {
            child = fragment.removeChild(fragment.firstChild);
            el.parentNode.insertBefore(child, el);
          }
          el.parentNode.removeChild(el);
        }
      }
      if (sel.isCollapsed)
      {
        els = editor._doc.body.childNodes;
        for (i = 0; i < els.length; i++) 
        {
          el = els[i];
          if (el.nodeType != 1) continue;
          if (el.tagName.toLowerCase() == 'span')
          {
            newNode = editor.convertNode(el, 'div');
            el.parentNode.replaceChild(newNode, el);
            el = newNode;
          }
          clean(el);
        }
      } 
      else
      {
        for (i=0; i<els.length;i++)
        {
          el = els[i];
          if ( range.isPointInRange(el, 0) || (els[i] == start && range.startOffset == 0))
          {
            clean(el);
          }
        }
      }

      r2.detach();
      editor.restoreSelection(selSave);
      return true;
    break;
  }

  return false;
}
WebKit.prototype.onMouseDown = function(ev)
{ 
  // selection of hr in Safari seems utterly impossible :(
  if (ev.target.tagName.toLowerCase() == "hr" || ev.target.tagName.toLowerCase() == "img")
  {
    this.editor.selectNodeContents(ev.target);
  }
}


/*--------------------------------------------------------------------------*/
/*------- IMPLEMENTATION OF THE ABSTRACT "Xinha.prototype" METHODS ---------*/
/*--------------------------------------------------------------------------*/

/** Insert a node at the current selection point. 
 * @param toBeInserted DomNode
 */

Xinha.prototype.insertNodeAtSelection = function(toBeInserted)
{
  var sel = this.getSelection();
  var range = this.createRange(sel);
  // remove the current selection
  sel.removeAllRanges();
  range.deleteContents();
  var node = range.startContainer;
  var pos = range.startOffset;
  var selnode = toBeInserted;
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
  if(!node) return; // I've seen this once
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

  if ( range )
  {
    return Xinha.getHTML(range.cloneContents(), false, this);
  }
  else return '';
};
  

/** Get a Selection object of the current selection.  Note that selection objects are browser specific.
 *
 * @returns Selection
 */
 
Xinha.prototype.getSelection = function()
{
  return this._iframe.contentWindow.getSelection();
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


/** Due to browser differences, some keys which Xinha prefers to call a keyPress
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
    if(typeof Xinha.DOM3_WebKit_KeyDownKeyPress_RE == 'undefined')
    {
      // I don't know if pre-defining this is really faster in the modern world of
      //  Javascript JIT compiling, but it does no harm
      Xinha.DOM3_WebKit_KeyDownKeyPress_RE = /^(Escape|Esc|Tab|Backspace|Delete|Del)/;
    }
    
    // Found using Chrome 65 and Opera 50, Edge
    //  Note that Edge calls Escape Esc, and Delete Del
    if(Xinha.DOM3_WebKit_KeyDownKeyPress_RE.test(keyEvent.key))
    {
      return true;
    }
  }
  // Old Safari and Chrome
  else if(typeof keyEvent.keyIdentifier != 'undefined' && keyEvent.keyIdentifier.length)
  {
    var kI = parseInt(keyEvent.keyIdentifier.replace(/^U\+/,''),16);
    // Found using Safari 9.1.1 - safari seems to pass ESC ok as a keyPress
    if(kI == 9 /* Tab */ || kI == 8 /* Backspace */ || kI == 127 /* Delete */ ) return true;
  }
  // Legacy
  else
  {
    // Also Chrome 65, I'm assuming perhaps dangerously, that it's about the same as 
    // older pre-KeyboardEvent.key but that's been around a few years now so
    // people will mostly be on supporting browsers anyway so this probably
    // won't be hit by that many people
    if(keyEvent.charCode == 0)
    {
      if( keyEvent.keyCode == 27  // ESC
       || keyEvent.keyCode == 9   // Tab
       || keyEvent.keyCode == 8   // Backspace
       || keyEvent.keyCode == 46  // Del
      ) return true;
    }
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
  // DOM3 Key is easy (ish)
  if(typeof keyEvent.key != 'undefined' && keyEvent.key.length > 0)
  {
    return keyEvent.key;
  }
  // Old DOM3 used by (Old?) Safari SOMETIMES (but not ALWAYS!) and old Chrome
  else if(typeof keyEvent.keyIdentifier != 'undefined' && keyEvent.keyIdentifier.length > 0 && keyEvent.keyIdentifier.match(/^U\+/))
  {
      var key = String.fromCharCode(parseInt(keyEvent.keyIdentifier.replace(/^U\+/,''),16));
      if (keyEvent.shiftKey) return key;
      else return key.toLowerCase();
  }
  // If charCode is specified, that's what we want
  else if(keyEvent.charCode)
  {
    return String.fromCharCode(keyEvent.charCode);
  }
  // Safari does not set charCode if CTRL is pressed
  //  but does set keyCode to the key, it also sets keyCode
  //  for the actual pressing of ctrl, skip that
  //  the keyCode in Safari si the uppercase character's code 
  //  for that key, so if shift is not pressed, lowercase it
  else if(keyEvent.ctrlKey && keyEvent.keyCode != 17)
  {
    if(keyEvent.shiftKey)
    {
      return String.fromCharCode(keyEvent.keyCode);
    }
    else
    {
      return String.fromCharCode(keyEvent.keyCode).toLowerCase();
    }
  }
  
  // Ok, give up, no idea!
  return '';
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

Xinha.cc = String.fromCharCode(8286); 

Xinha.prototype.setCC = function ( target )
{
  var cc = Xinha.cc;
  try
  {
    if ( target == "textarea" )
    {
      var ta = this._textArea;
      var index = ta.selectionStart;
      var before = ta.value.substring( 0, index )
      var after = ta.value.substring( index, ta.value.length );

      if ( after.match(/^[^<]*>/) ) // make sure cursor is in an editable area (outside tags, script blocks, enities and inside the body)
      {
        var tagEnd = after.indexOf(">") + 1;
        ta.value = before + after.substring( 0, tagEnd ) + cc + after.substring( tagEnd, after.length );
      }
      else ta.value = before + cc + after;
      ta.value = ta.value.replace(new RegExp ('(&[^'+cc+';]*?)('+cc+')([^'+cc+']*?;)'), "$1$3$2");
      ta.value = ta.value.replace(new RegExp ('(<script[^>]*>[^'+cc+']*?)('+cc+')([^'+cc+']*?<\/script>)'), "$1$3$2");
      ta.value = ta.value.replace(new RegExp ('^([^'+cc+']*)('+cc+')([^'+cc+']*<body[^>]*>)(.*?)'), "$1$3$2$4");
    }
    else
    {
      var sel = this.getSelection();
      sel.getRangeAt(0).insertNode( this._doc.createTextNode( cc ) );
    }
  } catch (e) {}
};

Xinha.prototype.findCC = function ( target )
{
  var cc = Xinha.cc;
  
  if ( target == 'textarea' )
  {
  var ta = this._textArea;
  var pos = ta.value.indexOf( cc );
  if ( pos == -1 ) return;
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
    var self = this;
    try
    {
      var doc = this._doc; 
      doc.body.innerHTML = doc.body.innerHTML.replace(new RegExp(cc),'<span id="XinhaEditingPostion"></span>');
      var posEl = doc.getElementById('XinhaEditingPostion');
      this.selectNodeContents(posEl);
      this.scrollToElement(posEl);
      posEl.parentNode.removeChild(posEl);

      this._iframe.contentWindow.focus();
    } catch (e) {}
  }
};
/*--------------------------------------------------------------------------*/
/*------------ EXTEND SOME STANDARD "Xinha.prototype" METHODS --------------*/
/*--------------------------------------------------------------------------*/

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
