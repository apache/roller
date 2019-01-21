  /*--------------------------------------:noTabs=true:tabSize=2:indentSize=2:--
    --  Xinha (is not htmlArea) - http://xinha.org
    --
    --  Use of Xinha is granted by the terms of the htmlArea License (based on
    --  BSD license)  please read license.txt in this package for details.
    --
    --  Copyright (c) 2005-2008 Xinha Developer Team and contributors
    --
    --  Xinha was originally based on work by Mihai Bazon which is:
    --      Copyright (c) 2003-2004 dynarch.com.
    --      Copyright (c) 2002-2003 interactivetools.com, inc.
    --      This copyright notice MUST stay intact for use.
    --
    --  This is the new all-in-one implementation of dialogs for Xinha
    --
    --
    --  $HeadURL: http://svn.xinha.org/trunk/modules/Dialogs/XinhaDialog.js $
    --  $LastChangedDate: 2018-02-07 14:14:28 +1300 (Wed, 07 Feb 2018) $
    --  $LastChangedRevision: 1379 $
    --  $LastChangedBy: gogo $
    --------------------------------------------------------------------------*/
/*jslint regexp: false, rhino: false, browser: true, bitwise: false, forin: false, adsafe: false, evil: true, nomen: false, 
glovar: false, debug: false, eqeqeq: false, passfail: false, sidebar: false, laxbreak: false, on: false, cap: true, 
white: false, widget: false, undef: true, plusplus: false*/
/*global  Xinha */

/** Xinha Dialog
 *  
 * @constructor
 * @version $LastChangedRevision: 1379 $ $LastChangedDate: 2018-02-07 14:14:28 +1300 (Wed, 07 Feb 2018) $
 * @param {Xinha} editor Xinha object    
 * @param {String} html string The HTML for the dialog's UI
 * @param {String} localizer string the "context" parameter for Xinha._lc(), typically the name of the plugin
 * @param {Object} size object with two possible properties of the size: width & height as int, where height is optional
 * @param {Object} options dictionary with optional boolean attributes 'modal', 'closable', 'resizable', and 'centered', as well as integer attribute 'layer'

 */
Xinha.Dialog = function(editor, html, localizer, size, options)
{
  var dialog = this;
  
  /** Used for dialog.getElementById()
   * @type Object
   * @private
   */
  this.id    = { };
  /** Used for dialog.getElementById()
   * @type Object
   * @private
   */
  this.r_id  = { }; // reverse lookup id
  /** The calling Xinha instance
   * @type Xinha
   * @private
   */
  this.editor   = editor;
  /** 
   * @private
   * @type Document
   */
  this.document = document;
  /** Object with width, height as numbers
   * @type Object
   */
  
  this.size = size;
  /** 
   * @type Boolean
   * @private
   */
  this.modal = (options && options.modal === false) ? false : true;
  /** 
   * @type Boolean
   * @private
   */
  this.closable = (options && options.closable === false) ? false : true;
  /** 
   * @type Boolean
   * @private
   */
  this.resizable = (options && options.resizable === false) ? false : true;
  /** 
   * @type Number
   * @private
   */
  this.layer = (options && options.layer) ? options.layer : 0;
  /** 
   * @type Boolean
   * @private
   */
  this.centered = (options && options.centered === true) ? true : false;
  /** 
   * @type Boolean
   * @private
   */
  this.closeOnEscape = (options && options.closeOnEscape === true) ? true : false;
  
  /** The div that is the actual dialog
   *  @type DomNode
   */
  this.rootElem = null;
  
  /** The caption at the top of the dialog that is used to dragged the dialog. It is automatically created from the first h1 in the dialog's HTML
   *  @type DomNode
   */
  this.captionBar = null;
  /** This div contains the content
   *  @type DomNode
   */
  this.main = null;
  
  /** Each dialog has a background
   *  @type DomNode
   *  @private
   */
  this.background = null;
  /** 
   * @type Boolean
   * @private
   */
  this.centered = null;
  /** 
   * @type Boolean
   * @private
   */
  this.greyout = null;
  
  /** 
   * @type DomNode
   * @private
   */
  this.buttons = null;
  /** 
   * @type DomNode
   * @private
   */
  this.closer = null;
  /** 
   * @type DomNode
   * @private
   */
  this.icon = null;
  /** 
   * @type DomNode
   * @private
   */
  this.resizer = null;
  /** 
   * @type Number
   * @private
   */
  this.initialZ = null;
  
  /* Check global config to see if we should override any of the above options
    If a global option is set, it will apply to all dialogs, regardless of their
    individual settings (i.e., it will override them). If the global option is
    undefined, the options passed in above will be used.
  */
  var globalOptions = editor.config.dialogOptions;
  if (globalOptions) 
  {
    if (typeof globalOptions.centered != 'undefined') 
    {
      this.centered = globalOptions.centered;
    }
    if (typeof globalOptions.resizable != 'undefined') 
    {
      this.resizable = globalOptions.resizable;
    }
    if (typeof globalOptions.closable != 'undefined') 
    {
      this.closable = globalOptions.closable;
    }
    if (typeof globalOptions.greyout != 'undefined') 
    {
      this.greyout = globalOptions.greyout;
    }
    if (typeof globalOptions.closeOnEscape != 'undefined') 
    {
      this.closeOnEscape = globalOptions.closeOnEscape;
    }
  }
  var backG;
  if (Xinha.is_ie)
  { // IE6 needs the iframe to hide select boxes
    backG = document.createElement("iframe");
    backG.src = "about:blank";
    backG.onreadystatechange = function () 
    {
      var doc = window.event.srcElement.contentWindow.document;
      if (this.readyState == 'complete' && doc && doc.body)
      {
        var div = doc.createElement('div');
        //insert styles to make background color skinable
        var styles, stylesheets = document.styleSheets;
        
        for (var i=0;i<stylesheets.length;i++)
        {
          if (stylesheets[i].id.indexOf('Xinha') != -1 && stylesheets[i].cssText)
          {
            styles += stylesheets[i].cssText;
          }
        }
        div.innerHTML = '<br><style type="text/css">\n'+styles+'\n</style>'; // strange way, but didn't work otherwise
        doc.getElementsByTagName('body')[0].appendChild(div);
        doc.body.className = 'xinha_dialog_background';
        if (dialog.modal) 
        {
          doc.body.className += '_modal';
        }
        if (dialog.greyout) 
        {
          doc.body.className += '_greyout';
        }
      }
    };
  }
  else
  { // Mozilla (<FF3) can't have the iframe, because it hides the caret in text fields
    // see https://bugzilla.mozilla.org/show_bug.cgi?id=226933
    backG = document.createElement("div");
  }
  backG.className = "xinha_dialog_background";
  if (this.modal) 
  {
    backG.className += '_modal';
  }
  if (this.greyout) 
  {
    backG.className += '_greyout';
  }
  var z = 1000;
  if (!Xinha.Dialog.initialZ)
  {
    var p = editor._htmlArea;
    while (p)
    {
      if (p.style && parseInt(p.style.zIndex, 10) > z) 
      {
        z = parseInt(p.style.zIndex, 10);
      }
      p = p.parentNode;
    }
    Xinha.Dialog.initialZ = z;
  }
  z = Xinha.Dialog.initialZ;
  var s = backG.style;
  s.position = "absolute";
  s.top = 0;
  s.left = 0;
  s.border = 'none';
  s.overflow = "hidden";
  s.display = "none";
  s.zIndex = (this.modal ? z + 25 : z +1 ) + this.layer;

  document.body.appendChild(backG);

  this.background = backG;

  backG = null;
  Xinha.freeLater(this, "background");

  var rootElem = document.createElement('div');
  //I've got the feeling dragging is much slower in IE7 w/ pos:fixed, besides the strange fact that it only works in Strict mode 
  //rootElem.style.position = (Xinha.ie_version < 7 ||(Xinha.is_ie && document.compatMode == "BackCompat") || !this.modal) ? "absolute" : "fixed";
  rootElem.style.position = (Xinha.is_ie || !this.modal) ? "absolute" : "fixed";
  rootElem.style.zIndex = (this.modal ? z + 27 : z + 3 ) + this.layer;
  rootElem.style.display  = 'none';
  
  if (!this.modal)
  {
    Xinha._addEvent(rootElem,'mousedown', function () { Xinha.Dialog.activateModeless(dialog);});
  }
  
  // FIXME: This is nice, but I don't manage to get it switched off on text inputs :(
  // rootElem.style.MozUserSelect = "none";
  
  rootElem.className = 'dialog' + (this.modal ? '' : ' modeless');
  if (Xinha.is_chrome) rootElem.className += ' chrome'; // Hack because border-radius & box-shadow don't go well together in chrome
  
 // this.background[1].appendChild(rootElem);
  document.body.appendChild(rootElem);

  rootElem.style.paddingBottom = "10px";
  rootElem.style.width = ( size && size.width )  ? size.width + 'px' : '';

  if (size && size.height)
  {
    if (Xinha.ie_version < 7)
    {
      rootElem.style.height = size.height + 'px';
    }
    else
    {
      rootElem.style.minHeight =  size.height + 'px';
    }
  }

  html = this.translateHtml(html,localizer);

  var main = document.createElement('div');
  rootElem.appendChild(main);
  main.innerHTML = html;

  // If the localizer is a string containing a plugin name, it can be used to
  // lookup the plugin.
  this.fixupDOM(main, localizer);
  
  //make the first h1 to drag&drop the rootElem
  var captionBar = main.removeChild( main.getElementsByTagName("h1")[0]);
  rootElem.insertBefore(captionBar,main);
  Xinha._addEvent(captionBar, 'mousedown',function(ev) { dialog.dragStart(ev); });
  
  captionBar.style.MozUserSelect = "none";
  captionBar.style.WebkitUserSelect = "none"; //seems to have no effect
  captionBar.unselectable = "on";
  captionBar.onselectstart = function() {return false;};

  this.buttons = document.createElement('div');
  s = this.buttons.style;
  s.position = "absolute";
  s.top = "0";
  s.right = "2px";

  rootElem.appendChild(this.buttons);

  if (this.closable && this.closeOnEscape)
  {
    Xinha._addEvent(document, 'keypress', function(ev) {
      if (ev.keyCode == 27) // ESC key
      {
        if (Xinha.Dialog.activeModeless == dialog || dialog.modal)
        {
          dialog.hide();
          return true;
        }
      }
    });
  }

  this.closer = null;
  if ( this.closable )
  {
    this.closer = document.createElement('div');
    this.closer.className= 'closeButton'; 
      
    this.closer.onmousedown = function(ev) { this.className = "closeButton buttonClick"; Xinha._stopEvent(Xinha.getEvent(ev)); return false;};
    this.closer.onmouseout = function(ev) { this.className = "closeButton"; Xinha._stopEvent(Xinha.getEvent(ev)); return false;};
    this.closer.onmouseup = function() { this.className = "closeButton"; dialog.hide(); return false;};
  
    this.buttons.appendChild(this.closer);
  
    var butX = document.createElement('span');
    butX.className = 'innerX';
    butX.style.position = 'relative';
    butX.style.top = '-3px';
  
    butX.appendChild(document.createTextNode('\u00D7')); // cross
    //below different symbols for future use
    //butX.appendChild(document.createTextNode('\u25AC')); //bar
    //butX.appendChild(document.createTextNode('\u25BA')); //triangle right
    //butX.appendChild(document.createTextNode('\u25B2')); //triangle up
    //butX.appendChild(document.createTextNode('\u25BC')); //triangle down
    this.closer.appendChild(butX);
    butX = null;
  }
  
  this.icon = document.createElement('img');
  var icon = this.icon;
  icon.className = 'icon';
  icon.src = editor.config.iconList.dialogCaption;
  icon.style.position = 'absolute';
  icon.style.top = '3px';
  icon.style.left = '2px';
  icon.ondrag = function () {return false;};

  //captionBar.style.paddingLeft = '22px';
  rootElem.appendChild(this.icon);
  
  var all = rootElem.getElementsByTagName("*");

  for (var i=0; i<all.length;i++)
  {
    var el = all[i]; 
    if (el.tagName.toLowerCase() == 'textarea' || el.tagName.toLowerCase() == 'input')
    {
      // FIXME: this doesn't work
      //el.style.MozUserSelect = "text";
    }
    else
    {
      el.unselectable = "on";
    }
  }

  this.resizer = null;
  if (this.resizable)
  {
    this.resizer = document.createElement('div');
    this.resizer.className = "resizeHandle";
    s = this.resizer.style;
    s.position = "absolute";
    s.bottom = "0px";
    s.right= "0px";
    s.MozUserSelect = 'none';

    Xinha._addEvent(this.resizer, 'mousedown', function(ev) { dialog.resizeStart(ev); });
    rootElem.appendChild(this.resizer);
  }

  this.rootElem = rootElem;
  this.captionBar = captionBar;
  this.main = main;
  
  captionBar = null;
  rootElem = null;
  main = null;
  
  Xinha.freeLater(this,"rootElem");
  Xinha.freeLater(this,"captionBar");
  Xinha.freeLater(this,"main");
  Xinha.freeLater(this, "buttons");
  Xinha.freeLater(this, "closer");
  Xinha.freeLater(this, "icon");
  Xinha.freeLater(this, "resizer");
  Xinha.freeLater(this, "document");
  
  // for caching size & position after dragging & resizing
  this.size = {};

};
/** This function is called when the dialog is resized. 
 *  By default it does nothing, but you can override it in your Xinha.Dialog object e.g. to resize elements within you Dialog.
 *  Example:<br />
 *  <code>
 *  var dialog = this.dialog; //The plugin's dialog instance;
 *  dialog.onresize = function() 
 *  {
 *    var el = dialog.getElementById('foo');
 *    el.style.width = dialog.width;
 *  }
 *  </code>
 */
Xinha.Dialog.prototype.onresize = function()
{
  return true;
};
/** This function shows the dialog and populates form elements with values.
 * Example:<br />
 * Given your dialog contains an input element like <code>&lt;input name="[myInput]" type="text" /&gt;</code>
 * <code>
 *  var dialog = this.dialog; //The plugin's dialog instance;
 *  var values = {myInput : 'My input value'}
 *  dialog.show(values);
 *  </code>
 *  @see #setValues
 *  @param {Object} values Object indexed by names of input elements
 */
Xinha.Dialog.prototype.show = function(values)
{
  var rootElem = this.rootElem;
  var rootElemStyle = rootElem.style;
  var modal = this.modal;
  var scrollPos = this.editor.scrollPos();
  this.scrollPos = scrollPos;
  var dialog = this;
  //dialog.main.style.height = '';
  if ( this.attached ) 
  {
    this.editor.showPanel(rootElem);
  }
    
  // We need to preserve the selection
  // if this is called before some editor has been activated, it activates the editor
  if (Xinha._someEditorHasBeenActivated)
  {
    this._lastRange = this.editor.saveSelection();
     
    if (Xinha.is_ie && !modal)
    {
      dialog.saveSelection = function() { dialog._lastRange = dialog.editor.saveSelection();};
      Xinha._addEvent(this.editor._doc,'mouseup', dialog.saveSelection);
    }
  }
 
  if ( modal )
  {
    this.editor.deactivateEditor();
    this.editor.suspendUpdateToolbar = true;
    this.editor.currentModal = dialog;
  }

  // unfortunately we have to hide the editor (iframe/caret bug)
  if (Xinha.is_ff2 && modal)
  {
    this._restoreTo = [this.editor._textArea.style.display, this.editor._iframe.style.visibility, this.editor.hidePanels()];
    this.editor._textArea.style.display = 'none';
    this.editor._iframe.style.visibility   = 'hidden';
  }
  
  if ( !this.attached )
  {
    if (modal) 
    {
      this.showBackground();
      this.posBackground({
        top: 0,
        left: 0
      });
      this.resizeBackground(Xinha.Dialog.calcFullBgSize());
    }
    else 
    {
      this.background.style.display = '';
    }

    //this.onResizeWin = function () {dialog.sizeBackground()};
    //Xinha._addEvent(window, 'resize', this.onResizeWin );

    //rootElemStyle.display   = '';
    Xinha.Dialog.fadeIn(this.rootElem, 100,function() {
      //this is primarily to work around a bug in IE where absolutely positioned elements have a frame that renders above all #1268
      //but could also be seen as a feature ;)
      if (modal)
      {
        var input = dialog.rootElem.getElementsByTagName('input');
        for (var i=0;i<input.length;i++)
        {
          if (input[i].type == 'text')
          {
            try {
              input[i].focus();
              break;
            }
            catch (e) {}
          }
        }
      }
    });
    //hide object & embed tags in document so they won't show through
    if (this.editor.config.hideObjectsBehindDialogs)
    {
      this.objTags = this.editor._doc.getElementsByTagName('object');
      this.embedTags = this.editor._doc.getElementsByTagName('embed');
      for (var j=0; j<this.objTags.length; j++)
      {
        this.objTags[j].__object_hidden = this.objTags[j].style.visibility;
        this.objTags[j].style.visibility = 'hidden';
      }
      for (j=0; j<this.embedTags.length; j++)
      {
        this.embedTags[j].__embed_hidden = this.embedTags[j].style.visibility;
        this.embedTags[j].style.visibility = 'hidden';
      }
    }

    var dialogHeight = rootElem.offsetHeight;
    var dialogWidth = rootElem.offsetWidth;
    var viewport = Xinha.viewportSize();
    var viewportHeight = viewport.y;
    var viewportWidth = viewport.x;
    
    if (dialogHeight >  viewportHeight)
    {
      rootElemStyle.height =  viewportHeight + "px";
      if (rootElem.scrollHeight > dialogHeight)
      {
        dialog.main.style.overflowY = "auto";
      }
    }

    if(this.size.top && this.size.left)
    {
      rootElemStyle.top =  parseInt(this.size.top,10) + 'px';
      rootElemStyle.left = parseInt(this.size.left,10) + 'px';
    }
    else if (this.editor.btnClickEvent && !this.centered)
    {
      var btnClickEvent = this.editor.btnClickEvent;
      if (rootElemStyle.position == 'absolute')
      {
        rootElemStyle.top =  btnClickEvent.clientY + this.scrollPos.y +'px';
      }
      else
      {
        rootElemStyle.top =  btnClickEvent.clientY +'px';
      }

      if (dialogHeight + rootElem.offsetTop >  viewportHeight)
      {
        rootElemStyle.top = (rootElemStyle.position == 'absolute' ? this.scrollPos.y : 0 ) + "px" ;
      }

      if (rootElemStyle.position == 'absolute')
      {
        rootElemStyle.left = btnClickEvent.clientX +  this.scrollPos.x +'px';
      }
      else
      {
        rootElemStyle.left =  btnClickEvent.clientX +'px';
      }

      if (dialogWidth + rootElem.offsetLeft >  viewportWidth)
      {
        rootElemStyle.left =  btnClickEvent.clientX - dialogWidth   + 'px';
        if (rootElem.offsetLeft < 0)
        {
          rootElemStyle.left = 0;
        }
      }
      this.editor.btnClickEvent = null;
    }
    else
    {
      var top =  ( viewportHeight - dialogHeight) / 2;
      var left = ( viewportWidth - dialogWidth) / 2;
      rootElemStyle.top =  ((top > 0) ? top : 0) +'px';
      rootElemStyle.left = ((left > 0) ? left : 0)+'px';
    }
  }
  this.width = dialogWidth;
  this.height = dialogHeight;

  if (!modal)
  {
    this.resizeBackground({width: dialogWidth + 'px', height: dialogHeight + 'px' });
    this.posBackground({top:  rootElemStyle.top, left: rootElemStyle.left});
  }
 
  if(typeof values != 'undefined')
  {
    this.setValues(values);
  }
  this.dialogShown = true;
};
/** Hides the dialog and returns an object with the valuse of form elements
 * @see #getValues
 * @type Object
 */
Xinha.Dialog.prototype.hide = function()
{
  if ( this.attached )
  {
    this.editor.hidePanel(this.rootElem);
  }
  else
  {
    //this.rootElem.style.display = 'none';
    Xinha.Dialog.fadeOut(this.rootElem);
    this.hideBackground();
    var dialog = this;

    if (Xinha.is_ff2 && this.modal)
    {
      this.editor._textArea.style.display = this._restoreTo[0];
      this.editor._iframe.style.visibility   = this._restoreTo[1];
      this.editor.showPanels(this._restoreTo[2]);
    }

    //restore visibility of object & embed tags in document
    if (this.editor.config.hideObjectsBehindDialogs)
    {
      for (var j=0; j<this.objTags.length; j++)
      {
        this.objTags[j].style.visibility = this.objTags[j].__object_hidden;
      }
      for (j=0; j<this.embedTags.length; j++)
      {
        this.embedTags[j].style.visibility = this.embedTags[j].__embed_hidden;
      }
    }

    if (!this.editor._isFullScreen && this.modal)
    {
      window.scroll(this.scrollPos.x, this.scrollPos.y);
    }

    if (Xinha.is_ie && !this.modal)
    {
      Xinha._removeEvent(this.editor._doc,'mouseup', dialog.saveSelection);
    }

    if (this.modal)
    {
      this.editor.suspendUpdateToolbar = false;
      this.editor.currentModal = null;
      this.editor.activateEditor();
    }
  }

  if (this.modal)
  {
    this.editor.restoreSelection(this._lastRange);
  }
  
  this.dialogShown = false;
  this.editor.updateToolbar();
  this.editor.focusEditor();
  return this.getValues();
};
/** Shows/hides the dialog
 * 
 */
Xinha.Dialog.prototype.toggle = function()
{
  if(this.rootElem.style.display == 'none')
  {
    this.show();
  }
  else
  {
    this.hide();
  }
};
/** Reduces the dialog to the size of the caption bar
 * 
 */
Xinha.Dialog.prototype.collapse = function()
{
  if(this.collapsed)
  {
    this.collapsed = false;
    this.show();
  }
  else
  {
    this.main.style.height = 0;
    this.collapsed = true;
  }
};
/** Equivalent to document.getElementById. You can't use document.getElementById because id's are dynamic to avoid id clashes between plugins
 * @type DomNode
 * @param {String} id
 */
Xinha.Dialog.prototype.getElementById = function(id)
{
  if(!this.rootElem.parentNode)
  {     
    this.document.body.appendChild(this.rootElem);
  }
  
  return this.document.getElementById(this.id[id] ? this.id[id] : id);
};
/** Equivalent to document.getElementByName. You can't use document.getElementByName because names are dynamic to avoid name clashes between plugins
 * @type Array
 * @param {String} name
 */
Xinha.Dialog.prototype.getElementsByName = function(name)
{
  if(!this.rootElem.parentNode)
  {     
    this.document.body.appendChild(this.rootElem);
  }
    
  var els = this.document.getElementsByName(this.id[name] ? this.id[name] : name); 
  return Xinha.collectionToArray(els);
};
/** Return all elements in the dialog that have the given class
 * @type Array 
 * @param {String} className
 */
Xinha.Dialog.prototype.getElementsByClassName = function(className)
{
  return Xinha.getElementsByClassName(this.rootElem,className);
};

/** Return all elements in the dialog that have the given class
 * @type Array 
 * @param {String} className
 */
Xinha.Dialog.prototype.getElementsByTagName = function(className)
{
  return Xinha.getElementsByTagName(this.rootElem,className);
};

/** Creates an elementin the dialog, with the given id if provided
 *   (note that the id is transfomed into a unique id)
 */

Xinha.Dialog.prototype.createElement = function(tagName, id)
{
  var newElement = this.document.createElement(tagName);
  if(typeof id == 'string')
  {
    newElement.id = this.createId(id);
  }
  return newElement;
};

/** Initiates dragging
 * @private
 * @param {Object} ev Mousedown event
 */
Xinha.Dialog.prototype.dragStart = function (ev) 
{
  if ( this.attached || this.dragging) 
  {
    return;
  }
  if (!this.modal)
  {
    this.posBackground({top:0, left:0}); 
    this.resizeBackground(Xinha.Dialog.calcFullBgSize());
    this.editor.suspendUpdateToolbar = true;
  }
  ev = Xinha.getEvent(ev);
  
  var dialog = this;

  dialog.dragging = true;

  dialog.scrollPos = dialog.editor.scrollPos();
   
  var st = dialog.rootElem.style;

  dialog.xOffs =  ev.offsetX || ev.layerX; //first value for IE/Opera/Safari, second value for Gecko (or should I say "netscape";))
  dialog.yOffs =  ev.offsetY || ev.layerY;

  dialog.mouseMove = function(ev) { dialog.dragIt(ev); };
  Xinha._addEvent(document,"mousemove", dialog.mouseMove );
  if (Xinha.is_ie) 
  {
    Xinha._addEvent(this.background.contentWindow.document, "mousemove", dialog.mouseMove);
  }
  
  dialog.mouseUp = function (ev) { dialog.dragEnd(ev); };
  Xinha._addEvent(document,"mouseup",  dialog.mouseUp);
  if (Xinha.is_ie) 
  {
    Xinha._addEvent(this.background.contentWindow.document, "mouseup", dialog.mouseUp);
  }
};
/** Sets the position while dragging
 * @private
 * @param {Object} ev Mousemove event
 */
Xinha.Dialog.prototype.dragIt = function(ev)
{
  var dialog = this;

  if (!dialog.dragging) 
  {
    return false;
  }
  var posY, posX, newPos;
  if (dialog.rootElem.style.position == 'absolute')
  {
    posY = (ev.clientY + this.scrollPos.y) - dialog.yOffs + "px";
    posX = (ev.clientX + this.scrollPos.x) - dialog.xOffs + "px";

    newPos = {top: posY,left: posX};
  }
  else if (dialog.rootElem.style.position == 'fixed')
  {
    posY = ev.clientY  - dialog.yOffs + "px";
    posX = ev.clientX - dialog.xOffs + "px";

    newPos = {top: posY,left: posX};
  }
  
  dialog.posDialog(newPos);
};
/** Ends dragging
 * @private
 * @param {Object} ev Mouseup event
 */
Xinha.Dialog.prototype.dragEnd = function(ev)
{
  var dialog = this;
  
  if (!this.modal)
  {
     this.editor.suspendUpdateToolbar = false; 
  }

  if (!dialog.dragging) 
  {
    return false;
  }
  dialog.dragging = false;

  Xinha._removeEvent(document, "mousemove", dialog.mouseMove );
  if (Xinha.is_ie) 
  {
    Xinha._removeEvent(this.background.contentWindow.document, "mousemove", dialog.mouseMove);
  }
  Xinha._removeEvent(document, "mouseup", dialog.mouseUp);
  if (Xinha.is_ie) 
  {
    Xinha._removeEvent(this.background.contentWindow.document, "mouseup", dialog.mouseUp);
  }

  var rootElemStyle = dialog.rootElem.style;
  
  dialog.size.top  = rootElemStyle.top;
  dialog.size.left = rootElemStyle.left;
  
  if (!this.modal)
  {
    this.sizeBgToDialog();
  }

};
/** Initiates resizing
 * @private
 * @param {Object} ev Mousedown event
 */
Xinha.Dialog.prototype.resizeStart = function (ev) {
  var dialog = this;
  if (dialog.resizing)
  {
    return;
  }
  dialog.resizing = true;
  if (!this.modal)
  {
    this.editor.suspendUpdateToolbar = true;
    this.posBackground({top:0, left:0}); 
    this.resizeBackground(Xinha.Dialog.calcFullBgSize());
  }
  dialog.scrollPos = dialog.editor.scrollPos();
  
  var st = dialog.rootElem.style;
  st.minHeight = '';
  st.overflow  =  'hidden';
  dialog.xOffs = parseInt(st.left,10);
  dialog.yOffs = parseInt(st.top,10);

  dialog.mouseMove = function(ev) { dialog.resizeIt(ev); };
  Xinha._addEvent(document,"mousemove", dialog.mouseMove );
  if (Xinha.is_ie) 
  {
    Xinha._addEvent(this.background.contentWindow.document, "mousemove", dialog.mouseMove);
  }
  dialog.mouseUp = function (ev) { dialog.resizeEnd(ev); };
  Xinha._addEvent(document,"mouseup",  dialog.mouseUp); 
  if (Xinha.is_ie) 
  {
    Xinha._addEvent(this.background.contentWindow.document, "mouseup", dialog.mouseUp);
  }
};
/** Sets the size while resiziong
 * @private
 * @param {Object} ev Mousemove event
 */
Xinha.Dialog.prototype.resizeIt = function(ev)
{
  var dialog = this;

  if (!dialog.resizing) {
    return false;
  }
  var posY, posX;
  if (dialog.rootElem.style.position == 'absolute')
  {
    posY = ev.clientY + dialog.scrollPos.y;
    posX = ev.clientX + dialog.scrollPos.x;
  }
  else
  {
    posY = ev.clientY;
    posX = ev.clientX;
  }

  posX -=  dialog.xOffs;
  posY -=  dialog.yOffs;

  var newSize = {};
  newSize.width  = (( posX > 10) ? posX : 10) + 8 + "px";
  newSize.height = (( posY > 10) ? posY : 10) + "px";

  dialog.sizeDialog(newSize);
  
  
  dialog.width = dialog.rootElem.offsetWidth;
  dialog.height = dialog.rootElem.offsetHeight;

  dialog.onresize();
};
/** Ends resizing
 * @private
 * @param {Object} ev Mouseup event
 */
Xinha.Dialog.prototype.resizeEnd = function(ev)
{
  var dialog = this;
  dialog.resizing = false;

  if (!this.modal)
  {
    this.editor.suspendUpdateToolbar = false;
  }

  Xinha._removeEvent(document, "mousemove", dialog.mouseMove );
  if (Xinha.is_ie) 
  {
    Xinha._removeEvent(this.background.contentWindow.document, "mouseup", dialog.mouseUp);
  }
  Xinha._removeEvent(document, "mouseup",  dialog.mouseUp);
  if (Xinha.is_ie) 
  {
    Xinha._removeEvent(this.background.contentWindow.document, "mouseup", dialog.mouseUp);
  }
  
  dialog.size.width  = dialog.rootElem.offsetWidth;
  dialog.size.height = dialog.rootElem.offsetHeight;

  if (!this.modal) 
  {
    this.sizeBgToDialog();
  }  
};
/** Attaches a modeless dialog to a panel on the given side
 *  Triggers a notifyOf panel_change event
 *  @param {String} side one of 'left', 'right', 'top', 'bottom'
 */
Xinha.Dialog.prototype.attachToPanel = function(side)
{
  var dialog = this;
  var rootElem = this.rootElem;
  var editor = this.editor;
  
  this.attached = true;
  this.rootElem.side = side;
  this.captionBar.ondblclick = function(ev) { dialog.detachFromPanel(Xinha.getEvent(ev)); };
  
  rootElem.style.position = "static";
  rootElem.parentNode.removeChild(rootElem);
  
  this.background.style.display = 'none';
  
  this.captionBar.style.paddingLeft = "3px";
  this.resizer.style.display = 'none';
  if (this.closable) 
  {
    this.closer.style.display = 'none';
  }
  this.icon.style.display = 'none';
  
  if ( side == 'left' || side == 'right' )
  {
    rootElem.style.width  = editor.config.panel_dimensions[side];
  }
  else
  {
    rootElem.style.width = '';
  }
  Xinha.addClasses(rootElem, 'panel');
  editor._panels[side].panels.push(rootElem);
  editor._panels[side].div.appendChild(rootElem);

  editor.notifyOf('panel_change', {'action':'add','panel':rootElem});
};
/** Removes a panel dialog from its panel and makes it float
 * 
 */
Xinha.Dialog.prototype.detachFromPanel = function()
{
  var dialog = this;
  var rootElem = dialog.rootElem;
  var rootElemStyle = rootElem.style;
  var editor = dialog.editor;
  
  dialog.attached = false;
  
  var pos = Xinha.getElementTopLeft(rootElem);
  rootElemStyle.position = "absolute";
  rootElemStyle.top = pos.top + "px";
  rootElemStyle.left = pos.left + "px";
  
  //dialog.captionBar.style.paddingLeft = "22px";
  dialog.resizer.style.display = '';
  if (dialog.closable) 
  {
    dialog.closer.style.display = '';
  }
  dialog.icon.style.display = '';
  
  if (dialog.size.width) 
  {
    rootElem.style.width = dialog.size.width + 'px';
  }

  Xinha.removeClasses(rootElem, 'panel');
  editor.removePanel(rootElem);
  document.body.appendChild(rootElem);
  
  dialog.captionBar.ondblclick = function() { dialog.attachToPanel(rootElem.side); };
  
  this.background.style.display = '';
  this.sizeBgToDialog();
};
/** 
 * @private
 * @type Object Object with width, height strings incl. "px" for CSS
 */
Xinha.Dialog.calcFullBgSize = function()
{
  var page = Xinha.pageSize();
  var viewport = Xinha.viewportSize();
  return {width:(page.x > viewport.x  ? page.x : viewport.x )  + "px",height:(page.x > viewport.y ? page.y : viewport.y ) + "px"};
};
/** Sizes the background to the size of the dialog
 *  @private
 */
Xinha.Dialog.prototype.sizeBgToDialog = function()
{
  var rootElemStyle = this.rootElem.style;
  var bgStyle = this.background.style;
  bgStyle.top = rootElemStyle.top;
  bgStyle.left = rootElemStyle.left;
  bgStyle.width = rootElemStyle.width;
  bgStyle.height = rootElemStyle.height;
};
/** Hides the background
 *  @private
 */
Xinha.Dialog.prototype.hideBackground = function()
{
  //this.background.style.display = 'none';
  Xinha.Dialog.fadeOut(this.background);
};
/** Shows the background
 *  @private
 */
Xinha.Dialog.prototype.showBackground = function()
{
  //this.background.style.display = '';
  Xinha.Dialog.fadeIn(this.background,70);
};
/** Positions the background
 *  @private
 *  @param {Object} pos Object with top, left strings incl. "px" for CSS
 */
Xinha.Dialog.prototype.posBackground = function(pos)
{
  if (this.background.style.display != 'none')
  {
    this.background.style.top  = pos.top;
    this.background.style.left = pos.left;
  }
};
/** Resizes the background
 *  @private
 *  @param {Object} size Object with width, height strings incl. "px" for CSS
 */
Xinha.Dialog.prototype.resizeBackground = function(size)
{
  if (this.background.style.display != 'none')
  {
    this.background.style.width  = size.width;
    this.background.style.height = size.height;
  }
};
/** Positions the dialog
 *  @param {Object} pos Object with top, left strings incl. "px" for CSS
 */
Xinha.Dialog.prototype.posDialog = function(pos)
{
  var st = this.rootElem.style;
  st.left = pos.left;
  st.top  = pos.top;
};
/** Resizes the dialog
 * 
 * @param {Object} size Object with width, height strings incl. "px" for CSS
 */
Xinha.Dialog.prototype.sizeDialog = function(size)
{
  var st = this.rootElem.style;
  st.height = size.height;
  st.width  = size.width;
  var width = parseInt(size.width, 10);
  var height = parseInt(size.height,10) - this.captionBar.offsetHeight;
  this.main.style.height = (height > 20) ? height : 20 + "px";
  this.main.style.width = (width > 10) ? width : 10 + 'px';
};
/** Sets the values like Xinha.Dialog.prototype.show(values)
 * @see #show
 * @param {Object} values 
 */
Xinha.Dialog.prototype.setValues = function(values)
{
  for(var i in values)
  {
    if (typeof i == 'string') 
    {
      var elems = this.getElementsByName(i);
      if (!elems) 
      {
        continue;
      }
      for(var x = 0; x < elems.length; x++)
      {
        var e = elems[x];
        switch(e.tagName.toLowerCase())
        {
          case 'select'  :
            for(var j = 0; j < e.options.length; j++)
            {
              if(typeof values[i] == 'object')
              {
                for(var k = 0; k < values[i].length; k++)
                {
                  if(values[i][k] == e.options[j].value)
                  {
                    e.options[j].selected = true;
                  }
                }
              }
              else if(values[i] == e.options[j].value)
              {
                e.options[j].selected = true;
              }
            }
          break;
          case 'textarea':
          case 'input'   :
            switch(e.getAttribute('type'))
            {
              case 'radio'   :
                if(e.value == values[i])
                {
                  e.checked = true;
                }
              break;
              case 'checkbox':
                if(typeof values[i] == 'object')
                {
                  for(j in values[i])
                  {
                    if(values[i][j] == e.value)
                    {
                      e.checked = true;
                    }
                  }
                }
                else
                {
                  if(values[i] == e.value)
                  {
                    e.checked = true;
                  }
                }
              break;
              default:
                e.value = values[i];
              break;
            }
        }
      }
    }
  }
};

/** Retrieves the values like Xinha.Dialog.prototype.hide()
 * @see #hide
 * @type Object values 
 */
Xinha.Dialog.prototype.getValues = function()
{
  var values = [ ];
  var inputs = Xinha.collectionToArray(this.rootElem.getElementsByTagName('input'))
              .append(Xinha.collectionToArray(this.rootElem.getElementsByTagName('textarea')))
              .append(Xinha.collectionToArray(this.rootElem.getElementsByTagName('select')));

  for(var x = 0; x < inputs.length; x++)
  {
    var i = inputs[x];
    if (!(i.name && this.r_id[i.name])) 
    {
      continue;
    }

    if(typeof values[this.r_id[i.name]] == 'undefined')
    {
      values[this.r_id[i.name]] = null;
    }
    var v = values[this.r_id[i.name]];

    switch(i.tagName.toLowerCase())
    {
      case 'select':
        if(i.multiple)
        {
          if(!v.push)
          {
            if(v !== null)
            {
              v = [v];
            }
            else
            {
              v = [];
            }
          }
          for(var j = 0; j < i.options.length; j++)
          {
            if(i.options[j].selected)
            {
              v.push(i.options[j].value);
            }
          }
        }
        else
        {
          if(i.selectedIndex >= 0)
          {
            v = i.options[i.selectedIndex];
          }
        }
        break;
/*
      case 'textarea':
      case 'input'   :
*/
      default:
        switch(i.type.toLowerCase())
        {
          case  'radio':
            if(i.checked)
            {
              v = i.value;
            }
          break;
          case 'checkbox':
            if(v === null)
            {
              if(this.getElementsByName(this.r_id[i.name]).length > 1)
              {
                v = [];
              }
            }
            if(i.checked)
            {
              if(v !== null && typeof v == 'object' && v.push)
              {
                v.push(i.value);
              }
              else
              {
                v = i.value;
              }
            }
          break;
          default:
            v = i.value;
          break;
        }
    }
    values[this.r_id[i.name]] = v;
  }
  return values;
};

/** Sets the localizer to use for the dialog
 *  @param function|string Either a function which takes a string as a parameter and returns 
 *    a localized string, or the name of a contact to pass to the standard Xinha localizer
 *    the "context" usually means the name of a plugin.
 */
 
Xinha.Dialog.prototype.setLocalizer = function(localizer)
{  
  var dialog = this;
  if(typeof localizer == 'function')
  {
    dialog._lc = localizer;
  }
  else if(localizer)
  {
    this._lc = function(string)
    {
      return Xinha._lc(string,localizer);
    };
  }
  else
  {
    this._lc = function(string)
    {
      return string;
    };
  }
}

/** Localizes strings in the dialog.
 * @private
 * @param {String} html The HTML to translate
 * @param {String} localizer Context for translation, usually plugin's name (optional if setLocalizer() has been used) 
 */
 
Xinha.Dialog.prototype.translateHtml = function(html,localizer)
{  
  var dialog = this;
  if(localizer) this.setLocalizer(localizer);
  
  // looking for strings of the form name='[foo]' or id="[bar]"
  html = html.replace(/((?:name)|(?:id))=(['"])\[([a-z0-9_]+)\]\2/ig,
    function(fullString, type, quote, id)
    {
      return type + "=" + quote + dialog.createId(id) + quote;
    }
    ).replace(/<l10n>(.*?)<\/l10n>/ig,
    function(fullString,translate)
    {
      return dialog._lc(translate) ;
    }
    ).replace(/\="_\((.*?)\)"/g,
    function(fullString, translate)
    {
      return '="' + dialog._lc(translate) + '"';
    }
  );
  return html;
};

/**
 * Fixup links in the elements to allow linking to Xinha resources
 * @private
 */
Xinha.Dialog.prototype.fixupDOM = function(root,plugin)
{
  var dialog = this;
  if(typeof plugin != 'string')
  {
    plugin = 'GenericPlugin';
  }

  var linkReplace = function(fullString, resource) {
    switch(resource) {
      case "editor":
        return _editor_url;
      case "plugin":
        return Xinha.getPluginDir(plugin);
      case "images":
        return dialog.editor.imgURL('images');
    };
  };

  var images = Xinha.collectionToArray(root.getElementsByTagName('img'));

  for (var index=0; index<images.length; ++index) {
    var image = images[index];
    var reference = image.getAttribute('src');
    if (reference) {
      var fixedReference = reference.replace(/^\[(editor|plugin|images)\]/, linkReplace);
      if (fixedReference != reference) {
        image.setAttribute('src', fixedReference);
      }
    }
  }

  var links = Xinha.collectionToArray(root.getElementsByTagName('a'));

  for (var index=0; index<links.length; ++index) {
    var link = links[index];
    var reference = link.getAttribute('href');
    if (reference) {
      var fixedReference = reference.replace(/^\[(editor|plugin|images)\]/, linkReplace);
      if (fixedReference != reference) {
        link.setAttribute('href', fixedReference);
      }
    }
  }

};

/** Use this function when adding an element with a new ID/name to a 
 *  dialog after it has already been created. This function ensures
 *  that the dialog has the id/name stored in its reverse-lookup table
 *  (which is required for form values to be properly returned by
 *  Xinha.Dialog.hide).
 * 
 * @param {id} the id (or name) to add 
 *
 * Returns the internal ID to which the passed in ID maps
 *
 * TODO: createId is a really awful name, but I can't think of anything better...
 */
Xinha.Dialog.prototype.createId = function(id)
{
  var dialog = this;
  if (typeof dialog.id[id] == 'undefined')
  {
    dialog.id[id] = Xinha.uniq('Dialog');
    dialog.r_id[dialog.id[id]] = id;
  }
  return dialog.id[id];
};

/** When several modeless dialogs are shown, one can be brought to front with this function (as happens on mouseclick) 
 * 
 * @param {XinhaDialog} dialog The dialog to activate
 */

Xinha.Dialog.activateModeless = function(dialog)
{
  if (Xinha.Dialog.activeModeless == dialog || dialog.attached ) 
  {
    return;
  }
  
  if (Xinha.Dialog.activeModeless )
  {
    Xinha.Dialog.activeModeless.rootElem.style.zIndex = parseInt(Xinha.Dialog.activeModeless.rootElem.style.zIndex, 10) -10;
  }
  Xinha.Dialog.activeModeless = dialog;

  Xinha.Dialog.activeModeless.rootElem.style.zIndex = parseInt(Xinha.Dialog.activeModeless.rootElem.style.zIndex, 10) + 10;
};
/** Set opacity cross browser 
 * 
 * @param {DomNode} el The element to set the opacity
 * @param {Object} value opacity value (percent)
 */
Xinha.Dialog.setOpacity = function(el,value)
{
    if (typeof el.style.filter != 'undefined')
    {
        el.style.filter = (value < 100) ?  'alpha(opacity='+value+')' : '';
    }
    else
    {
        el.style.opacity = value/100;
    }
};
/** Fade in an element
 * 
 * @param {DomNode} el The element to fade
 * @param {Number} delay Time for one step in ms
 * @param {Number} endOpacity stop when this value is reached (percent)
 * @param {Number} step Fade this much per step (percent)
 */
Xinha.Dialog.fadeIn = function(el,endOpacity,callback, delay,step)
{
    delay = delay || 1;
    step = step || 25;
    endOpacity = endOpacity || 100;
    el.op = el.op || 0;
    var op = el.op;
    if (el.style.display == 'none')
    {
        Xinha.Dialog.setOpacity(el,0);
        el.style.display = '';
    }
    if (op < endOpacity)
    {
        el.op += step;
        Xinha.Dialog.setOpacity(el,op);
        el.timeOut = setTimeout(function(){Xinha.Dialog.fadeIn(el, endOpacity, callback, delay, step);},delay);
    }
    else
    {
        Xinha.Dialog.setOpacity(el,endOpacity);
        el.op = endOpacity;
        el.timeOut = null;
        if (typeof callback == 'function') 
        {
          callback.call();
        }
    }
};
/** Fade out an element
 * 
 * @param {DomNode} el The element to fade
 * @param {Number} delay Time for one step in ms
 * @param {Number} step Fade this much per step (percent)
 */
Xinha.Dialog.fadeOut = function(el,delay,step)
{
    delay = delay || 1;
    step = step || 30;
    if (typeof el.op == 'undefined') 
    {
      el.op = 100;
    }
    var op = el.op;

    if (op >= 0)
    {
        el.op -= step;
        Xinha.Dialog.setOpacity(el,op);
        el.timeOut = setTimeout(function(){Xinha.Dialog.fadeOut(el,delay,step);},delay);
    }
    else
    {
        Xinha.Dialog.setOpacity(el,0);
        el.style.display = 'none';
        el.op = 0;
        el.timeOut = null;
    }
};
