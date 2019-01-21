
  /*--------------------------------------:noTabs=true:tabSize=2:indentSize=2:--
    --
    --  NOTICE Modern IE does not use this engine any more
    --
    --          IE 11 identifies as Gecko
    --          Edge  identifies as WebKit
    --
    --  The last IE version to use this engine is probably IE10, people should
    --   not use such an old version of IE and should upgrade or use a WebKit
    --   or Gecko based browser.
    --
    --  This engine is no longer officially supported or tested.
    --
    -----------------------------------------------------------------------------
    --
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
    -- This is the Internet Explorer compatability plugin, part of the 
    -- Xinha core.
    --
    --  The file is loaded as a special plugin by the Xinha Core when
    --  Xinha is being run under an Internet Explorer based browser.
    --
    --  It provides implementation and specialisation for various methods
    --  in the core where different approaches per browser are required.
    --
    --  Design Notes::
    --   Most methods here will simply be overriding Xinha.prototype.<method>
    --   and should be called that, but methods specific to IE should 
    --   be a part of the InternetExplorer.prototype, we won't trample on 
    --   namespace that way.
    --
    --  $HeadURL: http://svn.xinha.org/trunk/modules/InternetExplorer/InternetExplorer.js $
    --  $LastChangedDate: 2018-02-19 20:35:49 +1300 (Mon, 19 Feb 2018) $
    --  $LastChangedRevision: 1402 $
    --  $LastChangedBy: gogo $
    --------------------------------------------------------------------------*/
                                                    
InternetExplorer._pluginInfo = {
  name          : "Internet Explorer",
  origin        : "Xinha Core",
  version       : "$LastChangedRevision: 1402 $".replace(/^[^:]*:\s*(.*)\s*\$$/, '$1'),
  developer     : "The Xinha Core Developer Team",
  developer_url : "$HeadURL: http://svn.xinha.org/trunk/modules/InternetExplorer/InternetExplorer.js $".replace(/^[^:]*:\s*(.*)\s*\$$/, '$1'),
  sponsor       : "",
  sponsor_url   : "",
  license       : "htmlArea"
};

function InternetExplorer(editor) {
  this.editor = editor;  
  editor.InternetExplorer = this; // So we can do my_editor.InternetExplorer.doSomethingIESpecific();
}

/** Allow Internet Explorer to handle some key events in a special way.
 */
InternetExplorer.prototype.onKeyPress = function(ev)
{
  var editor = this.editor;
  
  // Shortcuts
  if(this.editor.isShortCut(ev))
  {
    switch(this.editor.getKey(ev).toLowerCase())
    {
      case 'n':
      {
        this.editor.execCommand('formatblock', false, '<p>');        
        Xinha._stopEvent(ev);
        return true;
      }
      break;
      
      case '1':
      case '2':
      case '3':
      case '4':
      case '5':
      case '6':
      {
        this.editor.execCommand('formatblock', false, '<h'+this.editor.getKey(ev).toLowerCase()+'>');
        Xinha._stopEvent(ev);
        return true;
      }
      break;
    }
  }
  
  switch(ev.keyCode) 
  {
    case 8: // KEY backspace
    case 46: // KEY delete
    {
      if(this.handleBackspace())
      {
        Xinha._stopEvent(ev);
        return true;
      }
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
          var s = editor.getSelection();
          var r = editor.createRange(s);                   
          r.collapse(true);
          r.select();
        }
        else
        {
          // Shift tab is not trivial to fix in old IE
          // and I don't care enough about it to try hard
        }
      }
      
      Xinha._stopEvent(ev);
      return true;
    }
    break;

  }
  
  return false;
}

/** When backspace is hit, the IE onKeyPress will execute this method.
 *  It preserves links when you backspace over them and apparently 
 *  deletes control elements (tables, images, form fields) in a better
 *  way.
 *
 *  @returns true|false True when backspace has been handled specially
 *   false otherwise (should pass through). 
 */

InternetExplorer.prototype.handleBackspace = function()
{
  var editor = this.editor;
  var sel = editor.getSelection();
  if ( sel.type == 'Control' )
  {
    var elm = editor.activeElement(sel);
    Xinha.removeFromParent(elm);
    return true;
  }

  // This bit of code preseves links when you backspace over the
  // endpoint of the link in IE.  Without it, if you have something like
  //    link_here |
  // where | is the cursor, and backspace over the last e, then the link
  // will de-link, which is a bit tedious
  var range = editor.createRange(sel);
  var r2 = range.duplicate();
  r2.moveStart("character", -1);
  var a = r2.parentElement();
  // @fixme: why using again a regex to test a single string ???
  if ( a != range.parentElement() && ( /^a$/i.test(a.tagName) ) )
  {
    r2.collapse(true);
    r2.moveEnd("character", 1);
    r2.pasteHTML('');
    r2.select();
    return true;
  }
};

InternetExplorer.prototype.inwardHtml = function(html)
{
   // Both IE and Gecko use strike internally instead of del (#523)
   // Xinha will present del externally (see Xinha.prototype.outwardHtml
   html = html.replace(/<(\/?)del(\s|>|\/)/ig, "<$1strike$2");
   // ie eats scripts and comments at beginning of page, so
   // make sure there is something before the first script on the page
   html = html.replace(/(<script|<!--)/i,"&nbsp;$1");
   
   // We've got a workaround for certain issues with saving and restoring
   // selections that may cause us to fill in junk span tags.  We'll clean
   // those here
   html = html.replace(/<span[^>]+id="__InsertSpan_Workaround_[a-z]+".*?>([\s\S]*?)<\/span>/i,"$1");
   
   return html;
}

InternetExplorer.prototype.outwardHtml = function(html)
{
   // remove space added before first script on the page
   html = html.replace(/&nbsp;(\s*)(<script|<!--)/i,"$1$2");

   // We've got a workaround for certain issues with saving and restoring
   // selections that may cause us to fill in junk span tags.  We'll clean
   // those here
   html = html.replace(/<span[^>]+id="__InsertSpan_Workaround_[a-z]+".*?>([\s\S]*?)<\/span>/i,"$1");
   
   return html;
}

InternetExplorer.prototype.onExecCommand = function(cmdID, UI, param)
{   
  switch(cmdID)
  {
    // #645 IE only saves the initial content of the iframe, so we create a temporary iframe with the current editor contents
    case 'saveas':
        var doc = null;
        var editor = this.editor;
        var iframe = document.createElement("iframe");
        iframe.src = "about:blank";
        iframe.style.display = 'none';
        document.body.appendChild(iframe);
        try
        {
          if ( iframe.contentDocument )
          {
            doc = iframe.contentDocument;        
          }
          else
          {
            doc = iframe.contentWindow.document;
          }
        }
        catch(ex)
        { 
          //hope there's no exception
        }
        
        doc.open("text/html","replace");
        var html = '';
        if ( editor.config.browserQuirksMode === false )
        {
          var doctype = '<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">';
        }
        else if ( editor.config.browserQuirksMode === true )
        {
           var doctype = '';
        }
        else
        {
           var doctype = Xinha.getDoctype(document);
        }
        if ( !editor.config.fullPage )
        {
          html += doctype + "\n";
          html += "<html>\n";
          html += "<head>\n";
          html += "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=" + editor.config.charSet + "\">\n";
          if ( typeof editor.config.baseHref != 'undefined' && editor.config.baseHref !== null )
          {
            html += "<base href=\"" + editor.config.baseHref + "\"/>\n";
          }
          
          if ( typeof editor.config.pageStyleSheets !== 'undefined' )
          {
            for ( var i = 0; i < editor.config.pageStyleSheets.length; i++ )
            {
              if ( editor.config.pageStyleSheets[i].length > 0 )
              {
                html += "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + editor.config.pageStyleSheets[i] + "\">";
                //html += "<style> @import url('" + editor.config.pageStyleSheets[i] + "'); </style>\n";
              }
            }
          }
          
          if ( editor.config.pageStyle )
          {
            html += "<style type=\"text/css\">\n" + editor.config.pageStyle + "\n</style>";
          }
          
          html += "</head>\n";
          html += "<body>\n";
          html += editor.getEditorContent();
          html += "</body>\n";
          html += "</html>";
        }
        else
        {
          html = editor.getEditorContent();
          if ( html.match(Xinha.RE_doctype) )
          {
            editor.setDoctype(RegExp.$1);
          }
        }
        doc.write(html);
        doc.close();
        doc.execCommand(cmdID, UI, param);
        document.body.removeChild(iframe);
      return true;
    break;
    case 'removeformat':
      var editor = this.editor;
      var sel = editor.getSelection();
      var selSave = editor.saveSelection(sel);

      var i, el, els;

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
          el.outerHTML = el.innerHTML;
        }
      }
      if ( editor.selectionEmpty(sel) )
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
      editor._doc.execCommand(cmdID, UI, param);

      editor.restoreSelection(selSave);
      return true;
    break;
  }
  
  return false;
};
/*--------------------------------------------------------------------------*/
/*------- IMPLEMENTATION OF THE ABSTRACT "Xinha.prototype" METHODS ---------*/
/*--------------------------------------------------------------------------*/

/** Insert a node at the current selection point. 
 * @param toBeInserted DomNode
 */

Xinha.prototype.insertNodeAtSelection = function(toBeInserted)
{
  this.insertHTML(toBeInserted.outerHTML);
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
  switch ( sel.type )
  {
    case "Text":
      // try to circumvent a bug in IE:
      // the parent returned is not always the real parent element
      var parent = range.parentElement();
      while ( true )
      {
        var TestRange = range.duplicate();
        TestRange.moveToElementText(parent);
        if ( TestRange.inRange(range) )
        {
          break;
        }
        if ( ( parent.nodeType != 1 ) || ( parent.tagName.toLowerCase() == 'body' ) )
        {
          break;
        }
        parent = parent.parentElement;
      }
      return parent;
    case "None":
      // It seems that even for selection of type "None",
      // there _is_ a parent element and it's value is not
      // only correct, but very important to us.  MSIE is
      // certainly the buggiest browser in the world and I
      // wonder, God, how can Earth stand it?
      try
      {
        return range.parentElement();
      }
      catch(e)
      {
        return this._doc.body; // ??
      }
      
    case "Control":
      return range.item(0);
    default:
      return this._doc.body;
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

  if ( sel.type.toLowerCase() == "control" )
  {
    return sel.createRange().item(0);
  }
  else
  {
    // If it's not a control, then we need to see if
    // the selection is the _entire_ text of a parent node
    // (this happens when a node is clicked in the tree)
    var range = sel.createRange();
    var p_elm = this.getParentElement(sel);
    if ( p_elm.innerHTML == range.htmlText )
    {
      return p_elm;
    }
    /*
    if ( p_elm )
    {
      var p_rng = this._doc.body.createTextRange();
      p_rng.moveToElementText(p_elm);
      if ( p_rng.isEqual(range) )
      {
        return p_elm;
      }
    }

    if ( range.parentElement() )
    {
      var prnt_range = this._doc.body.createTextRange();
      prnt_range.moveToElementText(range.parentElement());
      if ( prnt_range.isEqual(range) )
      {
        return range.parentElement();
      }
    }
    */
    return null;
  }
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

  return this.createRange(sel).htmlText === '';
};

/** 
 * Returns a range object to be stored 
 * and later restored with Xinha.prototype.restoreSelection()
 * 
 * @returns Range
 */
Xinha.prototype.saveSelection = function(sel)
{
  return this.createRange(sel ? sel : this.getSelection())
}
/** 
 * Restores a selection previously stored
 * @param savedSelection Range object as returned by Xinha.prototype.restoreSelection()
 */
Xinha.prototype.restoreSelection = function(savedSelection)
{
  if (!savedSelection) return;
  
  // Ticket #1387
  // avoid problem where savedSelection does not implement parentElement(). 
  // This condition occurs if there was no text selection at the time saveSelection() was called.  In the case 
  // an image selection, the situation is confusing... the image may be selected in two different ways:  1) by 
  // simply clicking the image it will appear to be selected by a box with sizing handles; or 2) by clicking and 
  // dragging over the image as you might click and drag over text.  In the first case, the resulting selection 
  // object does not implement parentElement(), leading to a crash later on in the code below.  The following 
  // hack avoids that problem. 
  
  // Ticket #1488
  // fix control selection in IE8
  
  var savedParentElement = null;
  if (savedSelection.parentElement)
  {
    savedParentElement =  savedSelection.parentElement();
  }
  else
  {
    savedParentElement = savedSelection.item(0);
  }
  
  // In order to prevent triggering the IE bug mentioned below, we will try to
  // optimize by not restoring the selection if it happens to match the current
  // selection.
  var range = this.createRange(this.getSelection());

  var rangeParentElement =  null;
  if (range.parentElement)
  {
    rangeParentElement =  range.parentElement();
  }
  else
  {
    rangeParentElement = range.item(0);
  }

  // We can't compare two selections that come from different documents, so we
  // must make sure they're from the same document.
  var findDoc = function(el)
  {
    for (var root=el; root; root=root.parentNode)
    {
      if (root.tagName.toLowerCase() == 'html')
      {
        return root.parentNode;
      }
    }
    return null;
  }

  if (savedSelection.parentElement && findDoc(savedParentElement) == findDoc(rangeParentElement))
  {
    if (range.isEqual(savedSelection))
    {
      // The selection hasn't moved, no need to restore.
      return;
    }
  }

  try { savedSelection.select() } catch (e) {};
  range = this.createRange(this.getSelection());
  
  if (range.parentElement)
  {
    rangeParentElement =  range.parentElement();
  }
  else
  {
    rangeParentElement = range.item(0);
  }
  
  if (rangeParentElement != savedParentElement)
  {
    // IE has a problem with selections at the end of text nodes that
    // immediately precede block nodes. Example markup:
    // <div>Text Node<p>Text in Block</p></div>
    //               ^
    // The problem occurs when the cursor is after the 'e' in Node.

    var solution = this.config.selectWorkaround || 'VisibleCue';
    switch (solution)
    {
      case 'SimulateClick':
        // Try to get the bounding box of the selection and then simulate a
        // mouse click in the upper right corner to return the cursor to the
        // correct location.

        // No code yet, fall through to InsertSpan
      case 'InsertSpan':
        // This workaround inserts an empty span element so that we are no
        // longer trying to select a text node,
        var parentDoc = findDoc(savedParentElement);

        // A function used to generate a unique ID for our temporary span.
        var randLetters = function(count)
        {
          // Build a list of 26 letters.
          var Letters = '';
          for (var index = 0; index<26; ++index)
          {
            Letters += String.fromCharCode('a'.charCodeAt(0) + index);
          }

          var result = '';
          for (var index=0; index<count; ++index)
          {
            result += Letters.substr(Math.floor(Math.random()*Letters.length + 1), 1);
          }
          return result;
        }

        // We'll try to find a unique ID to use for finding our element.
        var keyLength = 1;
        var tempId = '__InsertSpan_Workaround_' + randLetters(keyLength);
        while (parentDoc.getElementById(tempId))
        {
          // Each time there's a collision, we'll increase our key length by
          // one, making the chances of a collision exponentially more rare.
          keyLength += 1;
          tempId = '__InsertSpan_Workaround_' + randLetters(keyLength);
        }

        // Now that we have a uniquely identifiable element, we'll stick it and
        // and use it to orient our selection.
        savedSelection.pasteHTML('<span id="' + tempId + '"></span>');
        var tempSpan = parentDoc.getElementById(tempId);
        savedSelection.moveToElementText(tempSpan);
        savedSelection.select();
        break;
      case 'JustificationHack':
        // Setting the justification on an element causes IE to alter the
        // markup so that the selection we want to make is possible.
        // Unfortunately, this can force block elements to be kicked out of
        // their containing element, so it is not recommended.

        // Set a non-valid character and use it to anchor our selection.
        var magicString = String.fromCharCode(1);
        savedSelection.pasteHTML(magicString);
        savedSelection.findText(magicString,-1);
        savedSelection.select();

        // I don't know how to find out if there's an existing justification on
        // this element.  Hopefully, you're doing all of your styling outside,
        // so I'll just clear.  I already told you this was a hack.
        savedSelection.execCommand('JustifyNone');
        savedSelection.pasteHTML('');
        break;
      case 'VisibleCue':
      default:
        // This method will insert a little box character to hold our selection
        // in the desired spot.  We're depending on the user to see this ugly
        // box and delete it themselves.
        var magicString = String.fromCharCode(1);
        savedSelection.pasteHTML(magicString);
        savedSelection.findText(magicString,-1);
        savedSelection.select();
    }
  }
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
  // Tables and Images get selected as "objects" rather than the text contents
  if ( collapsed && node.tagName && node.tagName.toLowerCase().match(/table|img|input|select|textarea/) )
  {
    range = this._doc.body.createControlRange();
    range.add(node);
  }
  else
  {
    range = this._doc.body.createTextRange();
    if (3 == node.nodeType)
    {
      // Special handling for text nodes, since moveToElementText fails when
      // attempting to select a text node

      // Since the TextRange has a quite limited API, our strategy here is to
      // select (where possible) neighboring nodes, and then move our ranges
      // endpoints to be just inside of neighboring selections.
      if (node.parentNode)
      {
        range.moveToElementText(node.parentNode);
      } else
      {
        range.moveToElementText(this._doc.body);
      }
      var trimmingRange = this._doc.body.createTextRange();

      // In rare situations (mostly html that's been monkeyed about with by
      // javascript, but that's what we're doing) there can be two adjacent
      // text nodes.  Since we won't be able to handle these, we'll have to
      // hack an offset by 'move'ing the number of characters they contain.
      var texthackOffset = 0;
      var borderElement=node.previousSibling;
      for (; borderElement && (1 != borderElement.nodeType); borderElement = borderElement.previousSibling)
      {
        if (3 == borderElement.nodeType)
        {
          // IE doesn't count '\r' as a character, so we have to adjust the offset.
          texthackOffset += borderElement.nodeValue.length-borderElement.nodeValue.split('\r').length-1;
        }
      }
      if (borderElement && (1 == borderElement.nodeType))
      {
        trimmingRange.moveToElementText(borderElement);
        range.setEndPoint('StartToEnd', trimmingRange);
      }
      if (texthackOffset)
      {
        // We now need to move the selection forward the number of characters
        // in all text nodes in between our text node and our ranges starting
        // border.
        range.moveStart('character',texthackOffset);
      }

      // Youpi!  Now we get to repeat this trimming on the right side.
      texthackOffset = 0;
      borderElement=node.nextSibling;
      for (; borderElement && (1 != borderElement.nodeType); borderElement = borderElement.nextSibling)
      {
        if (3 == borderElement.nodeType)
        {
          // IE doesn't count '\r' as a character, so we have to adjust the offset.
          texthackOffset += borderElement.nodeValue.length-borderElement.nodeValue.split('\r').length-1;
          if (!borderElement.nextSibling)
          {
            // When a text node is the last child, IE adds an extra selection
            // "placeholder" for the newline character.  We need to adjust for
            // this character as well.
            texthackOffset += 1;
          }
        }
      }
      if (borderElement && (1 == borderElement.nodeType))
      {
        trimmingRange.moveToElementText(borderElement);
        range.setEndPoint('EndToStart', trimmingRange);
      }
      if (texthackOffset)
      {
        // We now need to move the selection backward the number of characters
        // in all text nodes in between our text node and our ranges ending
        // border.
        range.moveEnd('character',-texthackOffset);
      }
      if (!node.nextSibling)
      {
        // Above we performed a slight adjustment to the offset if the text
        // node contains a selectable "newline".  We need to do the same if the
        // node we are trying to select contains a newline.
        range.moveEnd('character',-1);
      }
    }
    else
    {
    range.moveToElementText(node);
    }
  }
  if (typeof collapseToStart != "undefined")
  {
    range.collapse(collapseToStart);
    if (!collapseToStart)
    {
      range.moveStart('character',-1);
      range.moveEnd('character',-1);
    }
  }
  range.select();
};
  
/** Insert HTML at the current position, deleting the selection if any. 
 *  
 *  @param html string
 */
 
Xinha.prototype.insertHTML = function(html)
{
  this.focusEditor();
  var sel = this.getSelection();
  var range = this.createRange(sel);
  range.pasteHTML(html);
};


/** Get the HTML of the current selection.  HTML returned has not been passed through outwardHTML.
 *
 * @returns string
 */
 
Xinha.prototype.getSelectedHTML = function()
{
  var sel = this.getSelection();
  if (this.selectionEmpty(sel)) return '';
  var range = this.createRange(sel);
  
  // Need to be careful of control ranges which won't have htmlText
  if( range.htmlText )
  {
    return range.htmlText;
  }
  else if(range.length >= 1)
  {
    return range.item(0).outerHTML;
  }
  
  return '';
};
  
/** Get a Selection object of the current selection.  Note that selection objects are browser specific.
 *
 * @returns Selection
 */
 
Xinha.prototype.getSelection = function()
{
  return this._doc.selection;
};

/** Create a Range object from the given selection.  Note that range objects are browser specific.
 *
 *  @param sel Selection object (see getSelection)
 *  @returns Range
 */
 
Xinha.prototype.createRange = function(sel)
{
  if (!sel) sel = this.getSelection();
  
  // ticket:1508 - when you do a key event within a 
  // absolute position div, in IE, the toolbar update
  // for formatblock etc causes a getParentElement() (above)
  // which produces a "None" select, then if we focusEditor() it
  // defocuses the absolute div and focuses into the iframe outside of the
  // div somewhere.  
  //
  // Removing this is probably a workaround and maybe it breaks something else
  // focusEditor is used in a number of spots, I woudl have thought it should
  // do nothing if the editor is already focused.
  //
  // if(sel.type == 'None') this.focusEditor();
  
  return sel.createRange();
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
    // Found using IE11 in 9-10 modes
    if(keyEvent.key.match(/^(Tab|Backspace|Del)/))
    {
      return true;
    }
  }
  // Legacy
  else
  {
    // Found using IE11 in 5-8 modes
    if(keyEvent.keyCode == 9   // Tab
    || keyEvent.keyCode == 8   // Backspace
    || keyEvent.keyCode == 46  // Del
    ) return true;
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
  return String.fromCharCode(keyEvent.keyCode);
}


/** Return the HTML string of the given Element, including the Element.
 * 
 * @param element HTML Element DomNode
 * @returns string
 */
 
Xinha.getOuterHTML = function(element)
{
  return element.outerHTML;
};

// Control character for retaining edit location when switching modes
Xinha.cc = String.fromCharCode(0x2009);

Xinha.prototype.setCC = function ( target )
{
  var cc = Xinha.cc;
  if ( target == "textarea" )
  {
    var ta = this._textArea;
    var pos = document.selection.createRange();
    pos.collapse();
    pos.text = cc;
    var index = ta.value.indexOf( cc );
    var before = ta.value.substring( 0, index );
    var after  = ta.value.substring( index + cc.length , ta.value.length );
    
    if ( after.match(/^[^<]*>/) ) // make sure cursor is in an editable area (outside tags, script blocks, entities, and inside the body)
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
    var r = sel.createRange(); 
    if ( sel.type == 'Control' )
    {
      var control = r.item(0);
      control.outerHTML += cc;
    }
    else
    {
      r.collapse();
      r.text = cc;
    }
  }
};

Xinha.prototype.findCC = function ( target )
{
  var findIn = ( target == 'textarea' ) ? this._textArea : this._doc.body;
  range = findIn.createTextRange();
  // in case the cursor is inside a link automatically created from a url
  // the cc also appears in the url and we have to strip it out additionally 
  if( range.findText( escape(Xinha.cc) ) )
  {
    range.select();
    range.text = '';
    range.select();
  }
  if( range.findText( Xinha.cc ) )
  {
    range.select();
    range.text = '';
    range.select();
  }
  if ( target == 'textarea' ) this._textArea.focus();
};

/** Return a doctype or empty string depending on whether the document is in Qirksmode or Standards Compliant Mode
 *  It's hardly possible to detect the actual doctype without unreasonable effort, so we set HTML 4.01 just to trigger the rendering mode
 * 
 * @param doc DOM element document
 * @returns string doctype || empty
 */
Xinha.getDoctype = function (doc)
{
  return (doc.compatMode == "CSS1Compat" && Xinha.ie_version < 8 ) ? '<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">' : '';
};
