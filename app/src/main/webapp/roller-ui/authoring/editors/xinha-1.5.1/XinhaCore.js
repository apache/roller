 
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
    --  Developers - Coding Style: 
    --         Before you are going to work on Xinha code, please see http://trac.xinha.org/wiki/Documentation/StyleGuide
    --
    --  $HeadURL: http://svn.xinha.org/trunk/XinhaCore.js $
    --  $LastChangedDate: 2018-03-29 11:13:25 +1300 (Thu, 29 Mar 2018) $
    --  $LastChangedRevision: 1433 $
    --  $LastChangedBy: gogo $
    --------------------------------------------------------------------------*/
/*jslint regexp: false, rhino: false, browser: true, bitwise: false, forin: true, adsafe: false, evil: true, nomen: false, 
glovar: false, debug: false, eqeqeq: false, passfail: false, sidebar: false, laxbreak: false, on: false, cap: true, 
white: false, widget: false, undef: true, plusplus: false*/
/*global  Dialog , _editor_css , _editor_icons, _editor_lang , _editor_skin , _editor_url, dumpValues, ActiveXObject, HTMLArea, _editor_lcbackend*/

/** Information about the Xinha version 
 * @type Object
 */
Xinha.version =
{
  'Release'   : 'Trunk',
  'Head'      : '$HeadURL: http://svn.xinha.org/trunk/XinhaCore.js $'.replace(/^[^:]*:\s*(.*)\s*\$$/, '$1'),
  'Date'      : '$LastChangedDate: 2018-03-29 11:13:25 +1300 (Thu, 29 Mar 2018) $'.replace(/^[^:]*:\s*([0-9\-]*) ([0-9:]*) ([+0-9]*) \((.*)\)\s*\$/, '$4 $2 $3'),
  'Revision'  : '$LastChangedRevision: 1433 $'.replace(/^[^:]*:\s*(.*)\s*\$$/, '$1'),
  'RevisionBy': '$LastChangedBy: gogo $'.replace(/^[^:]*:\s*(.*)\s*\$$/, '$1')
};

//must be here. it is called while converting _editor_url to absolute
Xinha._resolveRelativeUrl = function( base, url )
{
  if(url.match(/^([^:]+\:)?\/\//))
  {
    return url;
  }
  else
  {
    var b = base.split("/");
    if(b[b.length - 1] === "")
    {
      b.pop();
    }
    var p = url.split("/");
    if(p[0] == ".")
    {
      p.shift();
    }
    while(p[0] == "..")
    {
      b.pop();
      p.shift();
    }
    return b.join("/") + "/" + p.join("/");
  }
};

// Automatically determine editor_url from our script src if it is not supplied
if ( typeof _editor_url == 'undefined' || _editor_url == null)
{
  // Because of the way the DOM is loaded, this is guaranteed to always pull our script tag.
  var scripts = document.getElementsByTagName('script');
  var this_script = scripts[scripts.length - 1];
  _editor_url = this_script.src.split('?')[0].split('/').slice(0, -1).join('/').replace(/\x2f*$/, '/');
}

if ( typeof _editor_url == "string" )
{
  // Leave exactly one backslash at the end of _editor_url
  _editor_url = _editor_url.replace(/\x2f*$/, '/');
  
  // convert _editor_url to absolute
  if(!_editor_url.match(/^([^:]+\:)?\//))
  {
    (function()
    {
      var tmpPath = window.location.toString().replace(/\?.*$/,'').split("/");
      tmpPath.pop();
      _editor_url = Xinha._resolveRelativeUrl(tmpPath.join("/"), _editor_url);
    })();
  }
}

// make sure we have a language
if ( typeof _editor_lang == "string" )
{
  _editor_lang = _editor_lang.toLowerCase();
}
else
{
  _editor_lang = "en";
}

// skin stylesheet to load
if ( typeof _editor_skin !== "string" )
{
  _editor_skin = "";
}

if ( typeof _editor_icons !== "string" )
{
  _editor_icons = "";
}
/**
* The list of Xinha editors on the page. May be multiple editors.
* You can access each editor object through this global variable.
*
* Example:<br />
* <code>
*	var html = __xinhas[0].getEditorContent(); // gives you the HTML of the first editor in the page
* </code>
*/
var __xinhas = [];

// browser identification
/** Cache the user agent for the following checks
 * @type String
 * @private
 */
Xinha.agt       = navigator.userAgent.toLowerCase();
/** Browser is Microsoft Internet Explorer
 * 
 * WARNING Starting with Internet Explorer 11, this no longer detects as IE, but instead 
 *           detects as Gecko.  Oddly enough, it seems to work pretty much fine under
 *           Xinha's Gecko Engine, so I don't think we should change this to match IE11
 *           and continue to just pretend it is Gecko.
 * 
 *           https://blogs.msdn.microsoft.com/ieinternals/2013/09/21/internet-explorer-11s-many-user-agent-strings/
 *         
 * WARNING Starting with Microsoft Edge (what a silly name for a browser) this no longer 
 *          detects as Gecko but instead detects as Webkit.  I can't really see any obvious
 *          behavioural differences between using Gecko and using Webkit (you can force 
 *          this by, after loading XinhaCore.js but before configuring/initialising setting
 *             Xinha.is_webkit = false; Xinha.is_gecko = true;
 * 
 *          Quoting from Wikipedia:
 *              EdgeHTML is meant to be fully compatible with the WebKit layout engine 
 *              used by Safari, Chrome and other browsers. Microsoft has stated that 
 *              "any Edge-WebKit differences are bugs that we’re interested in fixing."[24]
 * 
 *          So I think that we best have it detect as Webkit, seems that should in theory
 *          be closest to the mark (vs Gecko).  Chrome also uses the Webkit Xinha module..
 * 
 *          It should be noted that the old InternetExplorer engine absolutely does not work
 *          with edge (and probably not with IE11 I expect but I have not tried).
 * 
 * @type Boolean 
 */
Xinha.is_ie    = ((Xinha.agt.indexOf("msie") != -1) && (Xinha.agt.indexOf("opera") == -1));
/** Version Number, if browser is Microsoft Internet Explorer
 * @type Float 
 */
Xinha.ie_version= parseFloat(Xinha.agt.substring(Xinha.agt.indexOf("msie")+5));
/** Browser is Opera
 * @type Boolean 
 */
Xinha.is_opera  = (Xinha.agt.indexOf("opera") != -1);
/** Version Number, if browser is Opera 
  * @type Float 
  */
if(Xinha.is_opera && Xinha.agt.match(/opera[\/ ]([0-9.]+)/))
{
  Xinha.opera_version = parseFloat(RegExp.$1);
}
else
{
  Xinha.opera_version = 0;
}
/** Browserengine is KHTML (Konqueror, Safari)
 * @type Boolean 
 */
Xinha.is_khtml  = (Xinha.agt.indexOf("khtml") != -1);
/** Browser is WebKit
 * @type Boolean 
 */
Xinha.is_webkit  = (Xinha.agt.indexOf("applewebkit") != -1);
/** Webkit build number
 * @type Integer
 */
Xinha.webkit_version = parseInt(navigator.appVersion.replace(/.*?AppleWebKit\/([\d]).*?/,'$1'), 10);

/** Browser is Safari
 * @type Boolean 
 */
Xinha.is_safari  = (Xinha.agt.indexOf("safari") != -1);

/** Browser is Google Chrome
 * @type Boolean 
 */
Xinha.is_chrome = (Xinha.agt.indexOf("chrome") != -1);

/** OS is MacOS
 * @type Boolean 
 */
Xinha.is_mac	   = (Xinha.agt.indexOf("mac") != -1);
Xinha.is_ios	   = (Xinha.agt.indexOf("iphone") != -1) || (Xinha.agt.indexOf("ipad") != -1) ;

/** Browser is Microsoft Internet Explorer Mac
 * @type Boolean 
 */
Xinha.is_mac_ie = (Xinha.is_ie && Xinha.is_mac);
/** Browser is Microsoft Internet Explorer Windows
 * @type Boolean 
 */
Xinha.is_win_ie = (Xinha.is_ie && !Xinha.is_mac);
/** Browser engine is Gecko (Mozilla), applies also to Safari and Opera which work
 *  largely similar.
 *@type Boolean 
 */
Xinha.is_gecko  = (navigator.product == "Gecko") || Xinha.is_opera;
/** Browser engine is really Gecko, i.e. Browser is Firefox (or Netscape, SeaMonkey, Flock, Songbird, Beonex, K-Meleon, Camino, Galeon, Kazehakase, Skipstone, or whatever derivate might exist out there...)
 * @type Boolean 
 */
Xinha.is_real_gecko = (navigator.product == "Gecko" && !Xinha.is_webkit);

/** Gecko version lower than 1.9
 * @type Boolean 
 */
// http://trac.xinha.org/ticket/1620
Xinha.is_ff2 = Xinha.is_real_gecko && navigator.productSub && parseInt(navigator.productSub.substr(0,10), 10) < 20071210;

/** File is opened locally opened ("file://" protocol)
 * @type Boolean
 * @private
 */
Xinha.isRunLocally = document.URL.toLowerCase().search(/^file:/) != -1;
/** Editing is enabled by document.designMode (Gecko, Opera), as opposed to contenteditable (IE)
 * @type Boolean
 * @private
 */
Xinha.is_designMode = (typeof document.designMode != 'undefined' && !Xinha.is_ie); // IE has designMode, but we're not using it

/** Check if Xinha can run in the used browser, otherwise the textarea will be remain unchanged
 * @type Boolean
 * @private
 */
Xinha.checkSupportedBrowser = function()
{
  return Xinha.is_real_gecko || (Xinha.is_opera && Xinha.opera_version >= 9.2) || Xinha.ie_version >= 5.5 || Xinha.webkit_version >= 522;
};
/** Cache result of checking for browser support
 * @type Boolean
 * @private
 */
Xinha.isSupportedBrowser = Xinha.checkSupportedBrowser();

if ( Xinha.isRunLocally && Xinha.isSupportedBrowser)
{
  alert('Xinha *must* be installed on a web server. Locally opened files (those that use the "file://" protocol) cannot properly function. Xinha will try to initialize but may not be correctly loaded.');
}

/** Creates a new Xinha object
 * @version $Rev: 1433 $ $LastChangedDate: 2018-03-29 11:13:25 +1300 (Thu, 29 Mar 2018) $
 * @constructor
 * @param {String|DomNode}   textarea the textarea to replace; can be either only the id or the DOM object as returned by document.getElementById()
 * @param {Xinha.Config} config optional if no Xinha.Config object is passed, the default config is used
 */
function Xinha(textarea, config)
{ 
  if ( !Xinha.isSupportedBrowser )
  {
    return;
  }
  
  if ( !textarea )
  {
    throw new Error ("Tried to create Xinha without textarea specified.");
  }

  if ( typeof config == "undefined" )
  {
		/** The configuration used in the editor
		 * @type Xinha.Config
		 */
    this.config = new Xinha.Config();
  }
  else
  {
    this.config = config;
  }

  if ( typeof textarea != 'object' )
  {
    textarea = Xinha.getElementById('textarea', textarea);
  }
  /** This property references the original textarea, which is at the same time the editor in text mode
   * @type DomNode textarea
   */
  this._textArea = textarea;
  this._textArea.spellcheck = false;
  Xinha.freeLater(this, '_textArea');
  
  // 
  /** Before we modify anything, get the initial textarea size
   * @private
   * @type Object w,h 
   */
  this._initial_ta_size =
  {
    w: textarea.style.width  ? textarea.style.width  : ( textarea.offsetWidth  ? ( textarea.offsetWidth  + 'px' ) : ( textarea.cols + 'em') ),
    h: textarea.style.height ? textarea.style.height : ( textarea.offsetHeight ? ( textarea.offsetHeight + 'px' ) : ( textarea.rows + 'em') )
  };

  if ( document.getElementById("loading_" + textarea.id) || this.config.showLoading )
  {
    if (!document.getElementById("loading_" + textarea.id))
    {
      Xinha.createLoadingMessage(textarea);
    }
    this.setLoadingMessage(Xinha._lc("Constructing object"));
  }

  /** the current editing mode
  * @private 
  * @type string "wysiwyg"|"text"
  */
  this._editMode = "wysiwyg";
  /** this object holds the plugins used in the editor
  * @private 
  * @type Object
  */
  this.plugins = {};
  /** periodically updates the toolbar
  * @private 
  * @type timeout
  */
  this._timerToolbar = null;
  /** periodically takes a snapshot of the current editor content
  * @private 
  * @type timeout
  */
  this._timerUndo = null;
  /** holds the undo snapshots
  * @private 
  * @type Array
  */
  this._undoQueue = [this.config.undoSteps];
  /** the current position in the undo queue 
  * @private 
  * @type integer
  */
  this._undoPos = -1;
  /** use our own undo implementation (true) or the browser's (false) 
  * @private 
  * @type Boolean
  */
  this._customUndo = true;
  /** the document object of the page Xinha is embedded in
  * @private 
  * @type document
  */
  this._mdoc = document; // cache the document, we need it in plugins
  /** doctype of the edited document (fullpage mode)
  * @private 
  * @type string
  */
  this.doctype = '';
  /** running number that identifies the current editor
  * @public 
  * @type integer
  */
  this.__htmlarea_id_num = __xinhas.length;
  __xinhas[this.__htmlarea_id_num] = this;
	
  /** holds the events for use with the notifyOn/notifyOf system
  * @private 
  * @type Object
  */
  this._notifyListeners = {};

  // Panels
  var panels = 
  {
    right:
    {
      on: true,
      container: document.createElement('td'),
      panels: []
    },
    left:
    {
      on: true,
      container: document.createElement('td'),
      panels: []
    },
    top:
    {
      on: true,
      container: document.createElement('td'),
      panels: []
    },
    bottom:
    {
      on: true,
      container: document.createElement('td'),
      panels: []
    }
  };

  for ( var i in panels )
  {
    if(!panels[i].container) { continue; } // prevent iterating over wrong type
    panels[i].div = panels[i].container; // legacy
    panels[i].container.className = 'panels panels_' + i;
    Xinha.freeLater(panels[i], 'container');
    Xinha.freeLater(panels[i], 'div');
  }
  /** holds the panels
  * @private 
  * @type Array
  */
  // finally store the variable
  this._panels = panels;
	
  // Init some properties that are defined later
  /** The statusbar container
   * @type DomNode statusbar div
   */
  this._statusBar = null;
  /** The DOM path that is shown in the statusbar in wysiwyg mode
   * @private
   * @type DomNode
   */
  this._statusBarTree = null;
  /** The message that is shown in the statusbar in text mode
   * @private
   * @type DomNode
   */
  this._statusBarTextMode = null;
  /** Holds the items of the DOM path that is shown in the statusbar in wysiwyg mode
   * @private
   * @type Array tag names
   */
  this._statusBarItems = [];
  /** Holds the parts (table cells) of the UI (toolbar, panels, statusbar)

   * @type Object framework parts
   */
  this._framework = {};
  /** Them whole thing (table)
   * @private
   * @type DomNode
   */
  this._htmlArea = null;
  /** This is the actual editable area.<br />
   *  Technically it's an iframe that's made editable using window.designMode = 'on', respectively document.body.contentEditable = true (IE).<br />
   *  Use this property to get a grip on the iframe's window features<br />
   *
   * @type window
   */
  this._iframe = null;
  /** The document object of the iframe.<br />
  *   Use this property to perform DOM operations on the edited document
  * @type document
  */
  this._doc = null;
  /** The toolbar
   *  @private
   *  @type DomNode 
   */
  this._toolBar = this._toolbar = null; //._toolbar is for legacy, ._toolBar is better thanks.
  /** Holds the botton objects
   *  @private
   *  @type Object
   */
  this._toolbarObjects = {};
  
  //hook in config.Events as as a "plugin"
  this.plugins.Events = 
  {
    name: 'Events',
    developer : 'The Xinha Core Developer Team',
    instance: config.Events
  };
};
// ray: What is this for? Do we need it?
Xinha.onload = function() { };
Xinha.init = function() { Xinha.onload(); };

// cache some regexps
/** Identifies HTML tag names
* @type RegExp
*/
Xinha.RE_tagName  = /(<\/|<)\s*([^ \t\n>]+)/ig;
/** Exracts DOCTYPE string from HTML
* @type RegExp
*/
Xinha.RE_doctype  = /(<!doctype((.|\n)*?)>)\n?/i;
/** Finds head section in HTML
* @type RegExp
*/
Xinha.RE_head     = /<head>((.|\n)*?)<\/head>/i;
/** Finds body section in HTML
* @type RegExp
*/
Xinha.RE_body     = /<body[^>]*>((.|\n|\r|\t)*?)<\/body>/i;
/** Special characters that need to be escaped when dynamically creating a RegExp from an arbtrary string
* @private
* @type RegExp
*/
Xinha.RE_Specials = /([\/\^$*+?.()|{}\[\]])/g;
/** When dynamically creating a RegExp from an arbtrary string, some charactes that have special meanings in regular expressions have to be escaped.
*   Run any string through this function to escape reserved characters.
* @param {string} string the string to be escaped
* @returns string
*/
Xinha.escapeStringForRegExp = function (string)
{
  return string.replace(Xinha.RE_Specials, '\\$1');
};
/** Identifies email addresses
* @type RegExp
*/
Xinha.RE_email    = /^[_a-z\d\-\.]{3,}@[_a-z\d\-]{2,}(\.[_a-z\d\-]{2,})+$/i;
/** Identifies URLs
* @type RegExp
*/
Xinha.RE_url      = /(https?:\/\/)?(([a-z0-9_]+:[a-z0-9_]+@)?[a-z0-9_\-]{2,}(\.[a-z0-9_\-]{2,}){2,}(:[0-9]+)?(\/\S+)*)/i;

/** This object records for known plugins where they can be found
 *  this is used by loadPlugin to avoid having to test multiple locations
 *  when we can reasonably know where the plugin is because it is
 *  part of the distribution.
 * 
 *  This becomes important because of CoRS and a problem with Amazon S3
 *  in which it does not poroduce a necessary header to Chrome if Chrome
 *  first requests a script as part of loading a <script> and then
 *  "pings" with XMLHTTPRequest, depending on that bit of a race-condition
 *  which one hits cache first things can go wonky.
 * 
 *  By avoiding the need to ping things in the distribution, we should
 *  not have that problem I think.
 */

Xinha.pluginManifest = {
  Abbreviation:         { url: _editor_url+'plugins/Abbreviation/Abbreviation.js' },
  AboutBox:             { url: _editor_url+'modules/AboutBox/AboutBox.js' },
  BackgroundImage:      { url: _editor_url+'unsupported_plugins/BackgroundImage/BackgroundImage.js' },
  CharacterMap:         { url: _editor_url+'plugins/CharacterMap/CharacterMap.js' },
  CharCounter:          { url: _editor_url+'plugins/CharCounter/CharCounter.js' },
  ClientsideSpellcheck: { url: _editor_url+'unsupported_plugins/ClientsideSpellcheck/ClientsideSpellcheck.js' },
  ColorPicker:          { url: _editor_url+'modules/ColorPicker/ColorPicker.js' },
  ContextMenu:          { url: _editor_url+'plugins/ContextMenu/ContextMenu.js' },
  CreateLink:           { url: _editor_url+'modules/CreateLink/link.js' },
  CSSDropDowns:         { url: _editor_url+'plugins/CSSDropDowns/CSSDropDowns.js' },
  CSSPicker:            { url: _editor_url+'plugins/CSSPicker/CSSPicker.js' },
  DefinitionList:       { url: _editor_url+'plugins/DefinitionList/DefinitionList.js' },
  Dialogs:              { url: _editor_url+'modules/Dialogs/dialog.js' },
  DoubleClick:          { url: _editor_url+'unsupported_plugins/DoubleClick/DoubleClick.js' },
  DynamicCSS:           { url: _editor_url+'plugins/DynamicCSS/DynamicCSS.js' },
  EditTag:              { url: _editor_url+'plugins/EditTag/EditTag.js' },
  EncodeOutput:         { url: _editor_url+'plugins/EncodeOutput/EncodeOutput.js' },
  Equation:             { url: _editor_url+'plugins/Equation/Equation.js' },
  ExtendedFileManager:  { url: _editor_url+'unsupported_plugins/ExtendedFileManager/ExtendedFileManager.js' },
  FancySelects:         { url: _editor_url+'plugins/FancySelects/FancySelects.js' },
  Filter:               { url: _editor_url+'unsupported_plugins/Filter/Filter.js' },
  FindReplace:          { url: _editor_url+'plugins/FindReplace/FindReplace.js' },
  FormOperations:       { url: _editor_url+'plugins/FormOperations/FormOperations.js' },
  Forms:                { url: _editor_url+'plugins/Forms/Forms.js' },
  FullPage:             { url: _editor_url+'plugins/FullPage/FullPage.js' },
  FullScreen:           { url: _editor_url+'modules/FullScreen/full-screen.js' },
  Gecko:                { url: _editor_url+'modules/Gecko/Gecko.js' },
  GenericPlugin:        { url: _editor_url+'plugins/GenericPlugin/GenericPlugin.js' },
  GetHtml:              { url: _editor_url+'plugins/GetHtml/GetHtml.js' },
  HorizontalRule:       { url: _editor_url+'plugins/HorizontalRule/HorizontalRule.js' },
  HtmlEntities:         { url: _editor_url+'plugins/HtmlEntities/HtmlEntities.js' },
  HtmlTidy:             { url: _editor_url+'unsupported_plugins/HtmlTidy/HtmlTidy.js' },
  ImageManager:         { url: _editor_url+'unsupported_plugins/ImageManager/ImageManager.js' },
  InlineStyler:         { url: _editor_url+'modules/InlineStyler/InlineStyler.js' },
  InsertAnchor:         { url: _editor_url+'plugins/InsertAnchor/InsertAnchor.js' },
  InsertImage:          { url: _editor_url+'modules/InsertImage/insert_image.js' },
  InsertMarquee:        { url: _editor_url+'unsupported_plugins/InsertMarquee/InsertMarquee.js' },
  InsertNote:           { url: _editor_url+'plugins/InsertNote/InsertNote.js' },
  InsertPagebreak:      { url: _editor_url+'plugins/InsertPagebreak/InsertPagebreak.js' },
  InsertPicture:        { url: _editor_url+'unsupported_plugins/InsertPicture/InsertPicture.js' },
  InsertSmiley:         { url: _editor_url+'plugins/InsertSmiley/InsertSmiley.js' },
  InsertSnippet2:       { url: _editor_url+'plugins/InsertSnippet2/InsertSnippet2.js' },
  InsertSnippet:        { url: _editor_url+'plugins/InsertSnippet/InsertSnippet.js' },
  InsertTable:          { url: _editor_url+'modules/InsertTable/insert_table.js' },
  InsertWords:          { url: _editor_url+'plugins/InsertWords/InsertWords.js' },
  InternetExplorer:     { url: _editor_url+'modules/InternetExplorer/InternetExplorer.js' },
  LangMarks:            { url: _editor_url+'plugins/LangMarks/LangMarks.js' },
  Linker:               { url: _editor_url+'plugins/Linker/Linker.js' },
  ListOperations:       { url: _editor_url+'plugins/ListOperations/ListOperations.js' },
  ListType:             { url: _editor_url+'plugins/ListType/ListType.js' },
  MootoolsFileManager:  { url: _editor_url+'plugins/MootoolsFileManager/MootoolsFileManager.js' },
  NoteServer:           { url: _editor_url+'unsupported_plugins/NoteServer/NoteServer.js' },
  Opera:                { url: _editor_url+'modules/Opera/Opera.js' },
  PasteText:            { url: _editor_url+'plugins/PasteText/PasteText.js' },
  PersistentStorage:    { url: _editor_url+'unsupported_plugins/PersistentStorage/PersistentStorage.js' },
  PreserveScripts:      { url: _editor_url+'plugins/PreserveScripts/PreserveScripts.js' },
  PreserveSelection:    { url: _editor_url+'plugins/PreserveSelection/PreserveSelection.js' },
  PSFixed:              { url: _editor_url+'unsupported_plugins/PSFixed/PSFixed.js' },
  PSLocal:              { url: _editor_url+'unsupported_plugins/PSLocal/PSLocal.js' },
  PSServer:             { url: _editor_url+'unsupported_plugins/PSServer/PSServer.js' },
  QuickTag:             { url: _editor_url+'plugins/QuickTag/QuickTag.js' },
  SaveOnBlur:           { url: _editor_url+'plugins/SaveOnBlur/SaveOnBlur.js' },
  SaveSubmit:           { url: _editor_url+'plugins/SaveSubmit/SaveSubmit.js' },
  SetId:                { url: _editor_url+'plugins/SetId/SetId.js' },
  SmartReplace:         { url: _editor_url+'plugins/SmartReplace/SmartReplace.js' },
  SpellChecker:         { url: _editor_url+'unsupported_plugins/SpellChecker/SpellChecker.js' },
  Stylist:              { url: _editor_url+'plugins/Stylist/Stylist.js' },
  SuperClean:           { url: _editor_url+'plugins/SuperClean/SuperClean.js' },
  TableOperations:      { url: _editor_url+'plugins/TableOperations/TableOperations.js' },
  Template:             { url: _editor_url+'unsupported_plugins/Template/Template.js' },
  UnFormat:             { url: _editor_url+'unsupported_plugins/UnFormat/UnFormat.js' },
  UnsavedChanges:       { url: _editor_url+'plugins/UnsavedChanges/UnsavedChanges.js' },
  WebKitResize:         { url: _editor_url+'plugins/WebKitResize/WebKitResize.js' },
  WebKit:               { url: _editor_url+'modules/WebKit/WebKit.js' },
  WysiwygWrap:          { url: _editor_url+'plugins/WysiwygWrap/WysiwygWrap.js' }
};

/**
 * This class creates an object that can be passed to the Xinha constructor as a parameter.
 * Set the object's properties as you need to configure the editor (toolbar etc.)
 * @version $Rev: 1433 $ $LastChangedDate: 2018-03-29 11:13:25 +1300 (Thu, 29 Mar 2018) $
 * @constructor
 */
Xinha.Config = function()
{
  /** The svn revision number 
   * @type Number
   */
  this.version = Xinha.version.Revision;
  
 /** This property controls the width of the editor.<br />
  *  Allowed values are 'auto', 'toolbar' or a numeric value followed by "px".<br />
  *  <code>auto</code>: let Xinha choose the width to use.<br />
  *  <code>toolbar</code>: compute the width size from the toolbar width.<br />
  *  <code>numeric value</code>: forced width in pixels ('600px').<br />
  * 
  *  Default: <code>"auto"</code>
  * @type String
  */
  this.width  = "auto";
 /** This property controls the height of the editor.<br />
  *  Allowed values are 'auto' or a numeric value followed by px.<br />
  *  <code>"auto"</code>: let Xinha choose the height to use.<br />
  *  <code>numeric value</code>: forced height in pixels ('200px').<br />
  *  Default: <code>"auto"</code> 
  * @type String
  */
  this.height = "auto";

  /** This property allows the user to drag-resize the iframe, if 
   * the browser supports it.  Currently Chrome and Opera seem to work
   * ok with this, while Firefox doesn't support the CSS resize for iframe
   * 
   * There is a Bugzilla bug about it...
   *    https://bugzilla.mozilla.org/show_bug.cgi?id=680823
   * 
   * IE, and Edge don't support the css resize property at all.
   * 
   *  Safari 9.1.1 acts a bit weird, unusable
   *  Safari 11.0.3 works ok but you can only increase the size
   * 
   * @type Boolean
   */
  
  this.resizableEditor = false;
  
 /** Specifies whether the toolbar should be included
  *  in the size, or are extra to it.  If false then it's recommended
  *  to have the size set as explicit pixel sizes (either in Xinha.Config or on your textarea)<br />
  *
  *  Default: <code>true</code>
  *
  *  @type Boolean
  */
  this.sizeIncludesBars = true;
 /**
  * Specifies whether the panels should be included
  * in the size, or are extra to it.  If false then it's recommended
  * to have the size set as explicit pixel sizes (either in Xinha.Config or on your textarea)<br />
  *  
  *  Default: <code>true</code>
  *
  *  @type Boolean
  */
  this.sizeIncludesPanels = true;

 /**
  * each of the panels has a dimension, for the left/right it's the width
  * for the top/bottom it's the height.
  *
  * WARNING: PANEL DIMENSIONS MUST BE SPECIFIED AS PIXEL WIDTHS<br />
  *Default values:  
  *<pre>
  *	  xinha_config.panel_dimensions =
  *   {
  *	    left:   '200px', // Width
  *	    right:  '200px',
  *	    top:    '100px', // Height
  *	    bottom: '100px'
  *	  }
  *</pre>
  *  @type Object
  */
  this.panel_dimensions =
  {
    left:   '200px', // Width
    right:  '200px',
    top:    '100px', // Height
    bottom: '100px'
  };

 /**  To make the iframe width narrower than the toolbar width, e.g. to maintain
  *   the layout when editing a narrow column of text, set the next parameter (in pixels).<br />
  *
  *  Default: <code>true</code>
  *
  *  @type Integer|null
  */
  this.iframeWidth = null;
 
 /** Enable creation of the status bar?<br />
  *
  *  Default: <code>true</code>
  *
  *  @type Boolean 
  */
  this.statusBar = true;

 /** Intercept ^V and use the Xinha paste command
  *  If false, then passes ^V through to browser editor widget, which is the only way it works without problems in Mozilla<br />
  *
  *  Default: <code>false</code>
  *
  *  @type Boolean
  */
  this.htmlareaPaste = false;
  
 /** <strong>Gecko only:</strong> Let the built-in routine for handling the <em>return</em> key decide if to enter <em>br</em> or <em>p</em> tags,
  *  or use a custom implementation.<br />
  *  For information about the rules applied by Gecko, <a href="http://www.mozilla.org/editor/rules.html">see Mozilla website</a> <br />
  *  Possible values are <em>built-in</em> or <em>best</em><br />
  *
  *  Default: <code>"best"</code>
  *
  *  @type String
  */
  this.mozParaHandler = 'best'; 
  
 /** This determines the method how the HTML output is generated.
  *  There are two choices:
  * 
  *<table border="1">
  *   <tr>
  *       <td><em>DOMwalk</em></td>
  *       <td>This is the classic and proven method. It recusively traverses the DOM tree 
  *           and builds the HTML string "from scratch". Tends to be a bit slow, especially in IE.</td>
  *   </tr>
  *   <tr>
  *       <td><em>TransformInnerHTML</em></td>
  *       <td>This method uses the JavaScript innerHTML property and relies on Regular Expressions to produce
  *            clean XHTML output. This method is much faster than the other one.</td>
  *     </tr>
  * </table>
  *
  *  Default: <code>"DOMwalk"</code>
  *
  * @type String
  */
  this.getHtmlMethod = 'DOMwalk';
  
  /** Maximum size of the undo queue<br />
   *  Default: <code>20</code>
   *  @type Integer
   */
  this.undoSteps = 20;

  /** The time interval at which undo samples are taken<br />
   *  Default: <code>500</code> (1/2 sec)
   *  @type Integer milliseconds
   */
  this.undoTimeout = 500;

  /** Set this to true if you want to explicitly right-justify when setting the text direction to right-to-left<br />
   *  Default: <code>false</code>
   *  @type Boolean
   */
  this.changeJustifyWithDirection = false;

  /** If true then Xinha will retrieve the full HTML, starting with the &lt;HTML&gt; tag.<br />
   *  Default: <code>false</code>
   *  @type Boolean
   */
  this.fullPage = false;

  /** Raw style definitions included in the edited document<br />
   *  When a lot of inline style is used, perhaps it is wiser to use one or more external stylesheets.<br />
   *  To set tags P in red, H1 in blue andn A not underlined, we may do the following
   *<pre>
   * xinha_config.pageStyle =
   *  'p { color:red; }\n' +
   *  'h1 { color:bleu; }\n' +
   *  'a {text-decoration:none; }';
   *</pre>
   *  Default: <code>""</code> (empty)
   *  @type String
   */
  this.pageStyle = "";

  /** Array of external stylesheets to load. (Reference these absolutely)<br />
   *  Example<br />
   *  <pre>xinha_config.pageStyleSheets = ["/css/myPagesStyleSheet.css","/css/anotherOne.css"];</pre>
   *  Default: <code>[]</code> (empty)
   *  @type Array
   */
  this.pageStyleSheets = [];

  // specify a base href for relative links
  /** Specify a base href for relative links<br />
   *  ATTENTION: this does not work as expected and needs t be changed, see Ticket #961 <br />
   *  Default: <code>null</code>
   *  @type String|null
   */
  this.baseHref = null;

  /** If true, relative URLs (../) will be made absolute. 
   * 
   *  When the editor is in different directory depth as the edited page relative 
   *   image sources will break the display of your images.
   * 
   *  This fixes an issue where Mozilla converts the urls of images and links that 
   *   are on the same server to relative ones (../) when dragging them around in 
   *   the editor (Ticket #448)<br />
   * 
   *  Note that setting to true will not expand directly relative paths
   *   "foo/bar.html" will not be changed.  Only parental, self-referential or 
   *   semi-absolute paths with parental or self-referential components will be 
   *   adjusted, some examples of paths this will affect
   * 
   *     "/foo/bar/../foo.html" --> /foo/foo.html
   *     "/foo/./foo.html"      --> /foo/foo.html
   *     "./foo.html"       --> "foo.html"
   *     "../foo.html"      --> "/foo.html" (assuming ../ ends up at the root or lower)
   * 
   *  You can also set this to the string 'all' and this will include non-parental
   *   relative urls.  I think this is probably a bad idea as it may cause broken URLS
   *   especially when combined with stripBaseHref or if you feed in such urls in your
   *   html.
   * 
   *  Default: <code>true</code>
   *  @type Boolean|String
   */
  this.expandRelativeUrl = true;
    
 /**  We can strip the server part out of URL to make/leave them semi-absolute, reason for this
   *  is that the browsers will prefix  the server to any relative links to make them absolute, 
   *  which isn't what you want most the time.<br />
   *  Default: <code>true</code>
   *  @type Boolean
   */
  this.stripBaseHref = true;

   /**  We can strip the url of the editor page from named links (eg &lt;a href="#top"&gt;...&lt;/a&gt;) and links 
   *  that consist only of URL parameters (eg &lt;a href="?parameter=value"&gt;...&lt;/a&gt;)
   *  reason for this is that browsers tend to prefixe location.href to any href that
   *  that don't have a full url<br />
   *  Default: <code>true</code>
   *  @type Boolean
   */
  this.stripSelfNamedAnchors = true;

  /** In URLs all characters above ASCII value 127 have to be encoded using % codes<br />
   *  Default: <code>true</code>
   *  @type Boolean
   */
  this.only7BitPrintablesInURLs = true;

 
  /** If you are putting the HTML written in Xinha into an email you might want it to be 7-bit
   *  characters only.  This config option will convert all characters consuming
   *  more than 7bits into UNICODE decimal entity references (actually it will convert anything
   *  below <space> (chr 20) except cr, lf and tab and above <tilde> (~, chr 7E))<br />
   *  Default: <code>false</code>
   *  @type Boolean
   */
  this.sevenBitClean = false;


  /** Sometimes we want to be able to replace some string in the html coming in and going out
   *  so that in the editor we use the "internal" string, and outside and in the source view
   *  we use the "external" string  this is useful for say making special codes for
   *  your absolute links, your external string might be some special code, say "{server_url}"
   *  an you say that the internal represenattion of that should be http://your.server/<br />
   *  Example:  <code>{ 'html_string' : 'wysiwyg_string' }</code><br />
   *  Default: <code>{}</code> (empty)
   *  @type Object
   */
  this.specialReplacements = {}; //{ 'html_string' : 'wysiwyg_string' }
  
  /** When the user presses the Tab in the editor, Xinha will insert a span.
   *  with the given class and contents.
   * 
   *  You can set tabSpanClass to false to disable this function, in which
   *    case tabs will be disabled in Xinha (except for in lists if you load 
   *    the ListOperations plugin.
   */
  
  this.tabSpanClass    = 'xinha-tab';
  this.tabSpanContents = '&nbsp;&nbsp;&nbsp;&nbsp;';
  
  /** A filter function for the HTML used inside the editor<br />
   * Default: function (html) { return html }
   * 
   * @param {String} html The whole document's HTML content
   * @return {String} The processed HTML 
   */
  this.inwardHtml = function (html) { return html; };
  
  /** A filter function for the generated HTML<br />
   * Default: function (html) { return html }
   * 
   * @param {String} html The whole document's HTML content
   * @return {String} The processed HTML 
   */
  this.outwardHtml = function (html) { return html; };
  
  /** This setting determines whether or not the editor will be automatically activated and focused when the page loads. 
   *  If the page contains only a single editor, autofocus can be set to true to focus it. 
   *  Alternatively, if the page contains multiple editors, autofocus may be set to the ID of the text area of the editor to be focused. 
   *  For example, the following setting would focus the editor attached to the text area whose ID is "myTextArea": 
   *  <code>xinha_config.autofocus = "myTextArea";</code>
   *  Default: <code>false</code>
   *  @type Boolean|String
   */
  this.autofocus = false;
  
 /** Set to true if you want Word code to be cleaned upon Paste. This only works if 
   * you use the toolbr button to paste, not ^V. This means that due to the restrictions
   * regarding pasting, this actually has no real effect in Mozilla <br />
   *  Default: <code>true</code>
   *  @type Boolean
   */
  this.killWordOnPaste = true;

  /** Enable the 'Target' field in the Make Link dialog. Note that the target attribute is invalid in (X)HTML strict<br />
   *  Default: <code>true</code>
   *  @type Boolean
   */
  this.makeLinkShowsTarget = true;

  /** CharSet of the iframe, default is the charset of the document
   *  @type String
   */
  this.charSet = (typeof document.characterSet != 'undefined') ? document.characterSet : document.charset;

 /** Whether the edited document should be rendered in Quirksmode or Standard Compliant (Strict) Mode.<br />
   * This is commonly known as the "doctype switch"<br />
   * for details read here http://www.quirksmode.org/css/quirksmode.html
   *
   * Possible values:<br />
   *    true     :  Quirksmode is used<br />
   *    false    :  Strict mode is used<br />
   *    null (default):  the mode of the document Xinha is in is used
   * @type Boolean|null
   */
  this.browserQuirksMode = null;

  // URL-s
  this.imgURL = "images/";
  this.popupURL = "popups/";

  /** RegExp allowing to remove certain HTML tags when rendering the HTML.<br />
   *  Example: remove span and font tags
   *  <code>
   *    xinha_config.htmlRemoveTags = /span|font/;
   *  </code>
   *  Default: <code>null</code>
   *  @type RegExp|null
   */
  this.htmlRemoveTags = null;

 /** Turning this on will turn all "linebreak" and "separator" items in your toolbar into soft-breaks,
   * this means that if the items between that item and the next linebreak/separator can
   * fit on the same line as that which came before then they will, otherwise they will
   * float down to the next line.

   * If you put a linebreak and separator next to each other, only the separator will
   * take effect, this allows you to have one toolbar that works for both flowToolbars = true and false
   * infact the toolbar below has been designed in this way, if flowToolbars is false then it will
   * create explictly two lines (plus any others made by plugins) breaking at justifyleft, however if
   * flowToolbars is false and your window is narrow enough then it will create more than one line
   * even neater, if you resize the window the toolbars will reflow.  <br />
   *  Default: <code>true</code>
   *  @type Boolean
   */
  this.flowToolbars = true;
  
  /** Set to center or right to change button alignment in toolbar
   *  @type String
   */
  this.toolbarAlign = "left";
  
  /** Set to true to display the font names in the toolbar font select list in their actual font.
   *  Note that this doesn't work in IE, but doesn't hurt anything either.
   *  Default: <code>false</code>
   *  @type Boolean
   */
   this.showFontStylesInToolbar = false;
  
  /** Set to true if you want the loading panel to show at startup<br />
   *  Default: <code>false</code>
   *  @type Boolean
   */
  this.showLoading = false;
  
  /** Set to false if you want to allow JavaScript in the content, otherwise &lt;script&gt; tags are stripped out.<br />
   *  This currently only affects the "DOMwalk" getHtmlMethod.<br />
   *  Default: <code>true</code>
   *  @type Boolean
   */
  this.stripScripts = true;

 /** See if the text just typed looks like a URL, or email address
   * and link it appropriatly
   * Note: Setting this option to false only affects Mozilla based browsers.
   * In InternetExplorer this is native behaviour and cannot be turned off.<br />
   *  Default: <code>true</code>
   *  @type Boolean
   */
   this.convertUrlsToLinks = true;

 /** Set to true to hide media objects when a div-type dialog box is open, to prevent show-through
  *  Default: <code>false</code>
  *  @type Boolean
  */
  this.hideObjectsBehindDialogs = false;

 /** Size of color picker cells<br />
   * Use number + "px"<br />
   *  Default: <code>"6px"</code>
   *  @type String
   */
  this.colorPickerCellSize = '6px';
 /** Granularity of color picker cells (number per column/row)<br />
   *  Default: <code>18</code>
   *  @type Integer
   */
  this.colorPickerGranularity = 18;
 /** Position of color picker from toolbar button<br />
   *  Default: <code>"bottom,right"</code>
   *  @type String
   */
  this.colorPickerPosition = 'bottom,right';
  /** Set to true to show only websafe checkbox in picker<br />
   *  Default: <code>false</code>
   *  @type Boolean
   */
  this.colorPickerWebSafe = false;
 /** Number of recent colors to remember<br />
   *  Default: <code>20</code>
   *  @type Integer
   */
  this.colorPickerSaveColors = 20;

  /** Start up the editor in fullscreen mode<br />
   *  Default: <code>false</code>
   *  @type Boolean
   */
  this.fullScreen = false;
  
 /** You can tell the fullscreen mode to leave certain margins on each side.<br />
   *  The value is an array with the values for <code>[top,right,bottom,left]</code> in that order<br />
   *  Default: <code>[0,0,0,0]</code>
   *  @type Array
   */
  this.fullScreenMargins = [0,0,0,0];
  
  
  /** Specify the method that is being used to calculate the editor's size<br/>
    * when we return from fullscreen mode.
    *  There are two choices:
    * 
    * <table border="1">
    *   <tr>
    *       <td><em>initSize</em></td>
    *       <td>Use the internal Xinha.initSize() method to calculate the editor's 
    *       dimensions. This is suitable for most usecases.</td>
    *   </tr>
    *   <tr>
    *       <td><em>restore</em></td>
    *       <td>The editor's dimensions will be stored before going into fullscreen
    *       mode and restored when we return to normal mode, taking a possible
    *       window resize during fullscreen in account.</td>
    *     </tr>
    * </table>
    *
    * Default: <code>"initSize"</code>
    * @type String
    */
  this.fullScreenSizeDownMethod = 'initSize';
  
  /** This array orders all buttons except plugin buttons in the toolbar. Plugin buttons typically look for one 
   *  a certain button in the toolbar and place themselves next to it.
   * Default value:
   *<pre>
   *xinha_config.toolbar =
   * [
   *   ["popupeditor"],
   *   ["separator","formatblock","fontname","fontsize","bold","italic","underline","strikethrough"],
   *   ["separator","forecolor","hilitecolor","textindicator"],
   *   ["separator","subscript","superscript"],
   *   ["linebreak","separator","justifyleft","justifycenter","justifyright","justifyfull"],
   *   ["separator","insertorderedlist","insertunorderedlist","outdent","indent"],
   *   ["separator","inserthorizontalrule","createlink","insertimage","inserttable"],
   *   ["linebreak","separator","undo","redo","selectall","print"], (Xinha.is_gecko ? [] : ["cut","copy","paste","overwrite","saveas"]),
   *   ["separator","killword","clearfonts","removeformat","toggleborders","splitblock","lefttoright", "righttoleft"],
   *   ["separator","htmlmode","showhelp","about"]
   * ];
   *</pre>
   * @type Array
   */  
  this.toolbar =
  [
    ["popupeditor"],
    ["separator","formatblock","fontname","fontsize","bold","italic","underline","strikethrough"],
    ["separator","forecolor","hilitecolor","textindicator"],
    ["separator","subscript","superscript"],
    ["linebreak","separator","justifyleft","justifycenter","justifyright","justifyfull"],
    ["separator","insertorderedlist","insertunorderedlist","outdent","indent"],
    ["separator","inserthorizontalrule","createlink","insertimage","inserttable"],
    ["linebreak","separator","undo","redo","selectall","print"], (Xinha.is_gecko ? [] : ["cut","copy","paste","overwrite"]),
    ["separator","killword","clearfonts","removeformat","toggleborders","splitblock","lefttoright", "righttoleft"],
    ["separator","htmlmode","showhelp","about"]
  ];

  /** The fontnames listed in the fontname dropdown
   * Default value:
   *<pre>
   *xinha_config.fontname =
   *{
   *  "&#8212; font &#8212;" : '',
   *  "Arial"                : 'arial,helvetica,sans-serif',
   *  "Courier New"          : 'courier new,courier,monospace',
   *  "Georgia"              : 'georgia,times new roman,times,serif',
   *  "Tahoma"               : 'tahoma,arial,helvetica,sans-serif',
   *  "Times New Roman"      : 'times new roman,times,serif',
   *  "Verdana"              : 'verdana,arial,helvetica,sans-serif',
   *  "Impact"               : 'impact',
   *  "WingDings"            : 'wingdings'
   *};
   *</pre>
   * @type Object
   */
  this.fontname =
  {
    "&#8212; font &#8212;": "", // &#8212; is mdash
    "Arial"           :	'arial,helvetica,sans-serif',
    "Courier New"     :	'courier new,courier,monospace',
    "Georgia"         :	'georgia,times new roman,times,serif',
    "Tahoma"          :	'tahoma,arial,helvetica,sans-serif',
    "Times New Roman" : 'times new roman,times,serif',
    "Verdana"         :	'verdana,arial,helvetica,sans-serif',
    "Impact"          :	'impact',
    "WingDings"       : 'wingdings' 
  };

  /** The fontsizes listed in the fontsize dropdown
   * Default value:
   *<pre>
   *xinha_config.fontsize =
   *{
   *  "&#8212; size &#8212;": "",
   *  "1 (8 pt)" : "1",
   *  "2 (10 pt)": "2",
   *  "3 (12 pt)": "3",
   *  "4 (14 pt)": "4",
   *  "5 (18 pt)": "5",
   *  "6 (24 pt)": "6",
   *  "7 (36 pt)": "7"
   *};
   *</pre>
   * @type Object
   */
  this.fontsize =
  {
    "&#8212; size &#8212;": "", // &#8212; is mdash
    "1 (8 pt)" : "1",
    "2 (10 pt)": "2",
    "3 (12 pt)": "3",
    "4 (14 pt)": "4",
    "5 (18 pt)": "5",
    "6 (24 pt)": "6",
    "7 (36 pt)": "7"
  };
  /** The tags listed in the formatblock dropdown
   * Default value:
   *<pre>
   *xinha_config.formatblock =
   *{
   *  "&#8212; format &#8212;": "", // &#8212; is mdash
   *  "Heading 1": "h1",
   *  "Heading 2": "h2",
   *  "Heading 3": "h3",
   *  "Heading 4": "h4",
   *  "Heading 5": "h5",
   *  "Heading 6": "h6",
   *  "Normal"   : "p",
   *  "Address"  : "address",
   *  "Formatted": "pre"
   *}
   *</pre>
   * @type Object
   */
  this.formatblock =
  {
    "&#8212; format &#8212;": "", // &#8212; is mdash
    "Heading 1": "h1",
    "Heading 2": "h2",
    "Heading 3": "h3",
    "Heading 4": "h4",
    "Heading 5": "h5",
    "Heading 6": "h6",
    "Normal"   : "p",
    "Address"  : "address",
    "Formatted": "pre"
  };

  /** You can provide custom functions that will be used to determine which of the
   * "formatblock" options is currently active and selected in the dropdown.
   *
   * Example:
   * <pre>
   * xinha_config.formatblockDetector['h5'] = function(xinha, currentElement)
   * {
   *   if (my_special_matching_logic(currentElement)) {
   *     return true;
   *   } else {
   *     return false;
   *   }
   * };
   * </pre>
   *
   * You probably don't want to mess with this, unless you are adding new, custom
   * "formatblock" options which don't correspond to real HTML tags.  If you want
   * to do that, you can use this configuration option to tell xinha how to detect
   * when it is within your custom context.
   *
   * For more, see: http://www.coactivate.org/projects/xinha/custom-formatblock-options
   */
  this.formatblockDetector = {};

  this.dialogOptions =
  {
    'centered' : true, //true: dialog is shown in the center the screen, false dialog is shown near the clicked toolbar button
    'greyout':true, //true: when showing modal dialogs, the page behind the dialoge is greyed-out
    'closeOnEscape':true
  };
  /** You can add functions to this object to be executed on specific events
   * Example:
   * <pre>
   * xinha_config.Events.onKeyPress = function (event)
   * {
   *    //do something 
   *    return false;
   * }
   * </pre>
   * Note that <em>this</em> inside the function refers to the respective Xinha object
   * The possible function names are documented at <a href="http://trac.xinha.org/wiki/Documentation/EventHooks">http://trac.xinha.org/wiki/Documentation/EventHooks</a>
   */
  this.Events = {};
  
  /** ??
   * Default: <code>{}</code>
   * @type Object
   */
  this.customSelects = {};

  /** Switches on some debugging (only in execCommand() as far as I see at the moment)<br />
   *
   * Default: <code>false</code>
   * @type Boolean
   */
  this.debug = false;

  /** Paths to various resources loaded by Xinha during operation.
   * 
   * Note that the iframe_src is the document loaded in the iframe to start with
   * due to modern security requirements, if you are using Xinha from
   * an external server (CDN), special care needs be taken, javascript:'' 
   * which is now the default seems to be ok, but if you have problems you could
   * change this to an absolute path to a file on the server which has the page
   * that is using Xinha (ie, /myserver/blank.html ), the contents should 
   * just be a blank html page (see popups/blank.html)
   * 
   * @type Array
   */
  
  this.URIs =
  {
   "iframe_src": 'javascript:\'\'',
   "blank": _editor_url + "popups/blank.html",
   "link":  _editor_url + "modules/CreateLink/link.html",
   "insert_image": _editor_url + "modules/InsertImage/insert_image.html",
   "insert_table":  _editor_url + "modules/InsertTable/insert_table.html",
   "select_color": _editor_url + "popups/select_color.html",
   "help": _editor_url + "popups/editor_help.html"
  };

   /** The button list conains the definitions of the toolbar button. Normally, there's nothing to change here :) 
   * <div style="white-space:pre">ADDING CUSTOM BUTTONS: please read below!
   * format of the btnList elements is "ID: [ ToolTip, Icon, Enabled in text mode?, ACTION ]"
   *    - ID: unique ID for the button.  If the button calls document.execCommand
   *	    it's wise to give it the same name as the called command.
   *    - ACTION: function that gets called when the button is clicked.
   *              it has the following prototype:
   *                 function(editor, buttonName)
   *              - editor is the Xinha object that triggered the call
   *              - buttonName is the ID of the clicked button
   *              These 2 parameters makes it possible for you to use the same
   *              handler for more Xinha objects or for more different buttons.
   *    - ToolTip: tooltip, will be translated below
   *    - Icon: path to an icon image file for the button
   *            OR; you can use an 18x18 block of a larger image by supllying an array
   *            that has three elemtents, the first is the larger image, the second is the column
   *            the third is the row.  The ros and columns numbering starts at 0 but there is
   *            a header row and header column which have numbering to make life easier.
   *            See images/buttons_main.gif to see how it's done.
   *    - Enabled in text mode: if false the button gets disabled for text-only mode; otherwise enabled all the time.</div>
   * @type Object
   */
  this.btnList =
  {
    bold: [ "Bold", Xinha._lc({key: 'button_bold', string: ["ed_buttons_main.png",3,2]}, 'Xinha'), false, function(e) { e.execCommand("bold"); } ],
    italic: [ "Italic", Xinha._lc({key: 'button_italic', string: ["ed_buttons_main.png",2,2]}, 'Xinha'), false, function(e) { e.execCommand("italic"); } ],
    underline: [ "Underline", Xinha._lc({key: 'button_underline', string: ["ed_buttons_main.png",2,0]}, 'Xinha'), false, function(e) { e.execCommand("underline"); } ],
    strikethrough: [ "Strikethrough", Xinha._lc({key: 'button_strikethrough', string: ["ed_buttons_main.png",3,0]}, 'Xinha'), false, function(e) { e.execCommand("strikethrough"); } ],
    subscript: [ "Subscript", Xinha._lc({key: 'button_subscript', string: ["ed_buttons_main.png",3,1]}, 'Xinha'), false, function(e) { e.execCommand("subscript"); } ],
    superscript: [ "Superscript", Xinha._lc({key: 'button_superscript', string: ["ed_buttons_main.png",2,1]}, 'Xinha'), false, function(e) { e.execCommand("superscript"); } ],

    justifyleft: [ "Justify Left", ["ed_buttons_main.png",0,0], false, function(e) { e.execCommand("justifyleft"); } ],
    justifycenter: [ "Justify Center", ["ed_buttons_main.png",1,1], false, function(e){ e.execCommand("justifycenter"); } ],
    justifyright: [ "Justify Right", ["ed_buttons_main.png",1,0], false, function(e) { e.execCommand("justifyright"); } ],
    justifyfull: [ "Justify Full", ["ed_buttons_main.png",0,1], false, function(e) { e.execCommand("justifyfull"); } ],

    orderedlist: [ "Ordered List", ["ed_buttons_main.png",0,3], false, function(e) { e.execCommand("insertorderedlist"); } ],
    unorderedlist: [ "Bulleted List", ["ed_buttons_main.png",1,3], false, function(e) { e.execCommand("insertunorderedlist"); } ],
    insertorderedlist: [ "Ordered List", ["ed_buttons_main.png",0,3], false, function(e) { e.execCommand("insertorderedlist"); } ],
    insertunorderedlist: [ "Bulleted List", ["ed_buttons_main.png",1,3], false, function(e) { e.execCommand("insertunorderedlist"); } ],

    outdent: [ "Decrease Indent", ["ed_buttons_main.png",1,2], false, function(e) { e.execCommand("outdent"); } ],
    indent: [ "Increase Indent",["ed_buttons_main.png",0,2], false, function(e) { e.execCommand("indent"); } ],
    forecolor: [ "Font Color", ["ed_buttons_main.png",3,3], false, function(e) { e.execCommand("forecolor"); } ],
    hilitecolor: [ "Background Color", ["ed_buttons_main.png",2,3], false, function(e) { e.execCommand("hilitecolor"); } ],

    undo: [ "Undoes your last action", ["ed_buttons_main.png",4,2], false, function(e) { e.execCommand("undo"); } ],
    redo: [ "Redoes your last action", ["ed_buttons_main.png",5,2], false, function(e) { e.execCommand("redo"); } ],
    cut: [ "Cut selection", ["ed_buttons_main.png",5,0], false,  function (e, cmd) { e.execCommand(cmd); } ],
    copy: [ "Copy selection", ["ed_buttons_main.png",4,0], false,  function (e, cmd) { e.execCommand(cmd); } ],
    paste: [ "Paste from clipboard", ["ed_buttons_main.png",4,1], false,  function (e, cmd) { e.execCommand(cmd); } ],
    selectall: [ "Select all", ["ed_buttons_main.png",3,5], false, function(e) {e.execCommand("selectall");} ],

    inserthorizontalrule: [ "Horizontal Rule", ["ed_buttons_main.png",6,0], false, function(e) { e.execCommand("inserthorizontalrule"); } ],
    createlink: [ "Insert Web Link", ["ed_buttons_main.png",6,1], false, function(e) { e.execCommand("createlink"); } ],
    insertimage: [ "Insert/Modify Image", ["ed_buttons_main.png",6,3], false, function(e) { e.execCommand("insertimage"); } ],
    inserttable: [ "Insert Table", ["ed_buttons_main.png",6,2], false, function(e) { e.execCommand("inserttable"); } ],

    htmlmode: [ "Toggle HTML Source", ["ed_buttons_main.png",7,0], true, function(e) { e.execCommand("htmlmode"); } ],
    toggleborders: [ "Toggle Borders", ["ed_buttons_main.png",7,2], false, function(e) { e._toggleBorders(); } ],
    print: [ "Print document", ["ed_buttons_main.png",8,1], false, function(e) { if(Xinha.is_gecko) {e._iframe.contentWindow.print(); } else { e.focusEditor(); print(); } } ],
    saveas: [ "Save as", ["ed_buttons_main.png",9,1], false, function(e) { e.execCommand("saveas",false,"noname.htm"); } ],
    about: [ "About this editor", ["ed_buttons_main.png",8,2], true, function(e) { e.getPluginInstance("AboutBox").show(); } ],
    showhelp: [ "Help using editor", ["ed_buttons_main.png",9,2], true, function(e) { e.execCommand("showhelp"); } ],

    splitblock: [ "Split Block", "ed_splitblock.gif", false, function(e) { e._splitBlock(); } ],
    lefttoright: [ "Direction left to right", ["ed_buttons_main.png",0,2], false, function(e) { e.execCommand("lefttoright"); } ],
    righttoleft: [ "Direction right to left", ["ed_buttons_main.png",1,2], false, function(e) { e.execCommand("righttoleft"); } ],
    overwrite: [ "Insert/Overwrite", "ed_overwrite.gif", false, function(e) { e.execCommand("overwrite"); } ],

    wordclean: [ "MS Word Cleaner", ["ed_buttons_main.png",5,3], false, function(e) { e._wordClean(); } ],
    clearfonts: [ "Clear Inline Font Specifications", ["ed_buttons_main.png",5,4], true, function(e) { e._clearFonts(); } ],
    removeformat: [ "Remove formatting", ["ed_buttons_main.png",4,4], false, function(e) { e.execCommand("removeformat"); } ],
    killword: [ "Clear MSOffice tags", ["ed_buttons_main.png",4,3], false, function(e) { e.execCommand("killword"); } ]
  };
  
  /** A hash of double click handlers for the given elements, each element may have one or more double click handlers
   *  called in sequence.  The element may contain a class selector ( a.somethingSpecial )
   *  
   */
   
  this.dblclickList = 
  {
      "a": [function(e, target) {e.execCommand("createlink", false, target);}],
    "img": [function(e, target) {e._insertImage(target);}]
  };

 /**
  * HTML class attribute to apply to the <body> tag within the editor's iframe.
  * If it is not specified, no class will be set.
  * 
  *  Default: <code>null</code>
  */
  this.bodyClass = null;

 /**
  * HTML ID attribute to apply to the <body> tag within the editor's iframe.
  * If it is not specified, no ID will be set.
  * 
  *  Default: <code>null</code>
  */
  this.bodyID = null;
  
  /** A container for additional icons that may be swapped within one button (like fullscreen)
   * @private
   */
  this.iconList = 
  {
    dialogCaption : _editor_url + 'images/xinha-small-icon.gif',
    wysiwygmode : [_editor_url + 'images/ed_buttons_main.png',7,1]
  };
  // initialize tooltips from the I18N module and generate correct image path
  for ( var i in this.btnList )
  {
    var btn = this.btnList[i];
    // prevent iterating over wrong type
    if ( typeof btn != 'object' )
    {
      continue;
    } 
    if ( typeof btn[1] != 'string' )
    {
      btn[1][0] = _editor_url + this.imgURL + btn[1][0];
    }
    else
    {
      btn[1] = _editor_url + this.imgURL + btn[1];
    }
    btn[0] = Xinha._lc(btn[0]); //initialize tooltip
  }
};
/** A plugin may require more than one icon for one button, this has to be registered in order to work with the iconsets (see FullScreen)
 * 
 * @param {String} id
 * @param {String|Array} icon definition like in registerButton
 */
Xinha.Config.prototype.registerIcon = function (id, icon)
{
  this.iconList[id] = icon;
};
/** ADDING CUSTOM BUTTONS
*   ---------------------
*
*
* Example on how to add a custom button when you construct the Xinha:
*
*   var editor = new Xinha("your_text_area_id");
*   var cfg = editor.config; // this is the default configuration
*   cfg.btnList["my-hilite"] =
*	[ "Highlight selection", // tooltip
*	  "my_hilite.gif", // image
*	  false // disabled in text mode
*	  function(editor) { editor.surroundHTML('<span style="background:yellow">', '</span>'); }, // action
*	];
*   cfg.toolbar.push(["linebreak", "my-hilite"]); // add the new button to the toolbar
*
* An alternate (also more convenient and recommended) way to
* accomplish this is to use the registerButton function below.
*/
/** Helper function: register a new button with the configuration.  It can be
 * called with all 5 arguments, or with only one (first one).  When called with
 * only one argument it must be an object with the following properties: id,
 * tooltip, image, textMode, action.<br />  
 * 
 * Examples:<br />
 *<pre>
 * config.registerButton("my-hilite", "Hilite text", "my-hilite.gif", false, function(editor) {...});
 * config.registerButton({
 *      id       : "my-hilite",      // the ID of your button
 *      tooltip  : "Hilite text",    // the tooltip
 *      image    : "my-hilite.gif",  // image to be displayed in the toolbar
 *      textMode : false,            // disabled in text mode
 *      action   : function(editor) { // called when the button is clicked
 *                   editor.surroundHTML('<span class="hilite">', '</span>');
 *                 },
 *      context  : "p" or [ "p", "h1" ]  // will be disabled if outside a <p> element (in array case, <p> or <h1>)
 *    });</pre>
 */
Xinha.Config.prototype.registerButton = function(id, tooltip, image, textMode, action, context)
{
  if ( typeof id == "string" )
  {
    this.btnList[id] = [ tooltip, image, textMode, action, context ];
  }
  else if ( typeof id == "object" )
  {
    this.btnList[id.id] = [ id.tooltip, id.image, id.textMode, id.action, id.context ];
  }
  else
  {
    alert("ERROR [Xinha.Config::registerButton]:\ninvalid arguments");
    return false;
  }
};

Xinha.prototype.registerPanel = function(side, object)
{
  if ( !side )
  {
    side = 'right';
  }
  this.setLoadingMessage('Register ' + side + ' panel ');
  var panel = this.addPanel(side);
  if ( object )
  {
    object.drawPanelIn(panel);
  }
};

/** The following helper function registers a dropdown box with the editor
 * configuration.  You still have to add it to the toolbar, same as with the
 * buttons.  Call it like this:
 *
 * FIXME: add example
 */
Xinha.Config.prototype.registerDropdown = function(object)
{
  // check for existing id
//  if ( typeof this.customSelects[object.id] != "undefined" )
//  {
    // alert("WARNING [Xinha.Config::registerDropdown]:\nA dropdown with the same ID already exists.");
//  }
//  if ( typeof this.btnList[object.id] != "undefined" )
//  {
    // alert("WARNING [Xinha.Config::registerDropdown]:\nA button with the same ID already exists.");
//  }
  this.customSelects[object.id] = object;
};

/** Call this function to remove some buttons/drop-down boxes from the toolbar.
 * Pass as the only parameter a string containing button/drop-down names
 * delimited by spaces.  Note that the string should also begin with a space
 * and end with a space.  Example:
 *
 *   config.hideSomeButtons(" fontname fontsize textindicator ");
 *
 * It's useful because it's easier to remove stuff from the defaul toolbar than
 * create a brand new toolbar ;-)
 */
Xinha.Config.prototype.hideSomeButtons = function(remove)
{
  var toolbar = this.toolbar;
  for ( var i = toolbar.length; --i >= 0; )
  {
    var line = toolbar[i];
    for ( var j = line.length; --j >= 0; )
    {
      if ( remove.indexOf(" " + line[j] + " ") >= 0 )
      {
        var len = 1;
        if ( /separator|space/.test(line[j + 1]) )
        {
          len = 2;
        }
        line.splice(j, len);
      }
    }
  }
};

/** Helper Function: add buttons/drop-downs boxes with title or separator to the toolbar
 * if the buttons/drop-downs boxes doesn't allready exists.
 * id: button or selectbox (as array with separator or title)
 * where: button or selectbox (as array if the first is not found take the second and so on)
 * position:
 * -1 = insert button (id) one position before the button (where)
 * 0 = replace button (where) by button (id)
 * +1 = insert button (id) one position after button (where)
 *
 * cfg.addToolbarElement(["T[title]", "button_id", "separator"] , ["first_id","second_id"], -1);
*/

Xinha.Config.prototype.addToolbarElement = function(id, where, position)
{
  var toolbar = this.toolbar;
  var a, i, j, o, sid;
  var idIsArray = false;
  var whereIsArray = false;
  var whereLength = 0;
  var whereJ = 0;
  var whereI = 0;
  var exists = false;
  var found = false;
  // check if id and where are arrys
  if ( ( id && typeof id == "object" ) && ( id.constructor == Array ) )
  {
    idIsArray = true;
  }
  if ( ( where && typeof where == "object" ) && ( where.constructor == Array ) )
  {
    whereIsArray = true;
    whereLength = where.length;
	}

  if ( idIsArray ) //find the button/select box in input array
  {
    for ( i = 0; i < id.length; ++i )
    {
      if ( ( id[i] != "separator" ) && ( id[i].indexOf("T[") !== 0) )
      {
        sid = id[i];
      }
    }
  }
  else
  {
    sid = id;
  }
  
  for ( i = 0; i < toolbar.length; ++i ) {
    a = toolbar[i];
    for ( j = 0; j < a.length; ++j ) {
      // check if button/select box exists
      if ( a[j] == sid ) {
        return; // cancel to add elements if same button already exists
      }
    }
  }
  

  for ( i = 0; !found && i < toolbar.length; ++i )
  {
    a = toolbar[i];
    for ( j = 0; !found && j < a.length; ++j )
    {
      if ( whereIsArray )
      {
        for ( o = 0; o < whereLength; ++o )
        {
          if ( a[j] == where[o] )
          {
            if ( o === 0 )
            {
              found = true;
              j--;
              break;
            }
            else
            {
              whereI = i;
              whereJ = j;
              whereLength = o;
            }
          }
        }
      }
      else
      {
        // find the position to insert
        if ( a[j] == where )
        { 
          found = true;
          break;
        }
      }
    }
  }

  //if check found any other as the first button
  if ( !found && whereIsArray )
  { 
    if ( where.length != whereLength )
    {
      j = whereJ;
      a = toolbar[whereI];
      found = true;
    }
  }
  if ( found )
  {
    // replace the found button
    if ( position === 0 )
    {
      if ( idIsArray)
      {
        a[j] = id[id.length-1];
        for ( i = id.length-1; --i >= 0; )
        {
          a.splice(j, 0, id[i]);
        }
      }
      else
      {
        a[j] = id;
      }
    }
    else
    { 
      // insert before/after the found button
      if ( position < 0 )
      {
        j = j + position + 1; //correct position before
      }
      else if ( position > 0 )
      {
        j = j + position; //correct posion after
      }
      if ( idIsArray )
      {
        for ( i = id.length; --i >= 0; )
        {
          a.splice(j, 0, id[i]);
        }
      }
      else
      {
        a.splice(j, 0, id);
      }
    }
  }
  else
  {
    // no button found
    toolbar[0].splice(0, 0, "separator");
    if ( idIsArray)
    {
      for ( i = id.length; --i >= 0; )
      {
        toolbar[0].splice(0, 0, id[i]);
      }
    }
    else
    {
      toolbar[0].splice(0, 0, id);
    }
  }
};
/** Alias of Xinha.Config.prototype.hideSomeButtons()
* @type Function
*/
Xinha.Config.prototype.removeToolbarElement = Xinha.Config.prototype.hideSomeButtons;

/** Helper function: replace all TEXTAREA-s in the document with Xinha-s. 
* @param {Xinha.Config} optional config 
*/
Xinha.replaceAll = function(config)
{
  var tas = document.getElementsByTagName("textarea");
  // @todo: weird syntax, doesnt help to read the code, doesnt obfuscate it and doesnt make it quicker, better rewrite this part
  for ( var i = tas.length; i > 0; new Xinha(tas[--i], config).generate() )
  {
    // NOP
  }
};

/** Helper function: replaces the TEXTAREA with the given ID with Xinha. 
* @param {string} id id of the textarea to replace 
* @param {Xinha.Config} optional config 
*/
Xinha.replace = function(id, config)
{
  var ta = Xinha.getElementById("textarea", id);
  return ta ? new Xinha(ta, config).generate() : null;
};
 
/** Creates the toolbar and appends it to the _htmlarea
* @private
* @returns {DomNode} toolbar
*/
Xinha.prototype._createToolbar = function ()
{
  this.setLoadingMessage(Xinha._lc('Create Toolbar'));
  var editor = this;	// to access this in nested functions

  var toolbar = document.createElement("div");
  // ._toolbar is for legacy, ._toolBar is better thanks.
  this._toolBar = this._toolbar = toolbar;
  toolbar.className = "toolbar";  
  toolbar.align = this.config.toolbarAlign;
  
  Xinha.freeLater(this, '_toolBar');
  Xinha.freeLater(this, '_toolbar');
  
  var tb_row = null;
  var tb_objects = {};
  this._toolbarObjects = tb_objects;

	this._createToolbar1(editor, toolbar, tb_objects);
	
	// IE8 is totally retarded, if you click on a toolbar element (eg button)
	// and it doesn't have unselectable="on", then it defocuses the editor losing the selection
	// so nothing works.  Particularly prevalent with TableOperations
	function noselect(e)
	{
    if(e.tagName) e.unselectable = "on";        
    if(e.childNodes)
    {
      for(var i = 0; i < e.childNodes.length; i++) if(e.tagName) noselect(e.childNodes[i]);
    }
	}
	if(Xinha.is_ie) noselect(toolbar);
	
	
	this._htmlArea.appendChild(toolbar);      
  
  return toolbar;
};

/** FIXME : function never used, can probably be removed from source
* @private
* @deprecated
*/
Xinha.prototype._setConfig = function(config)
{
	this.config = config;
};
/** FIXME: How can this be used??
 * The only thing that used this seems to have been PersistentStorage, which is DOA
* @private
*/
Xinha.prototype._rebuildToolbar = function()
{
	this._createToolbar1(this, this._toolbar, this._toolbarObjects);

  // We only want ONE editor at a time to be active
  if ( Xinha._currentlyActiveEditor )
  {
    if ( Xinha._currentlyActiveEditor == this )
    {
      this.activateEditor();
    }
  }
  else
  {
    this.disableToolbar();
  }
};

/**
 * Create a break element to add in the toolbar
 *
 * @return {DomNode} HTML element to add
 * @private
 */
Xinha._createToolbarBreakingElement = function()
{
  var brk = document.createElement('div');
  brk.style.height = '1px';
  brk.style.width = '1px';
  brk.style.lineHeight = '1px';
  brk.style.fontSize = '1px';
  brk.style.clear = 'both';
  return brk;
};


/** separate from previous createToolBar to allow dynamic change of toolbar
 * @private
 * @return {DomNode} toolbar
 */
Xinha.prototype._createToolbar1 = function (editor, toolbar, tb_objects)
{
  // We will clean out any existing toolbar elements.
  while (toolbar.lastChild)
  {
    toolbar.removeChild(toolbar.lastChild);
  }

  var tb_row;
  // This shouldn't be necessary, but IE seems to float outside of the container
  // when we float toolbar sections, so we have to clear:both here as well
  // as at the end (which we do have to do).
  if ( editor.config.flowToolbars )
  {
    toolbar.appendChild(Xinha._createToolbarBreakingElement());
  }

  // creates a new line in the toolbar
  function newLine()
  {
    if ( typeof tb_row != 'undefined' && tb_row.childNodes.length === 0)
    {
      return;
    }

    var table = document.createElement("table");
    table.border = "0px";
    table.cellSpacing = "0px";
    table.cellPadding = "0px";
    if ( editor.config.flowToolbars )
    {
      if ( Xinha.is_ie )
      {
        table.style.styleFloat = "left";
      }
      else
      {
        table.style.cssFloat = "left";
      }
    }

    toolbar.appendChild(table);
    // TBODY is required for IE, otherwise you don't see anything
    // in the TABLE.
    var tb_body = document.createElement("tbody");
    table.appendChild(tb_body);
    tb_row = document.createElement("tr");
    tb_body.appendChild(tb_row);

    table.className = 'toolbarRow'; // meh, kinda.
  } // END of function: newLine

  // init first line
  newLine();

  // updates the state of a toolbar element.  This function is member of
  // a toolbar element object (unnamed objects created by createButton or
  // createSelect functions below).
  function setButtonStatus(id, newval)
  {
    var oldval = this[id];
    var el = this.element;
    if ( oldval != newval )
    {
      switch (id)
      {
        case "enabled":
          if ( newval )
          {
            Xinha._removeClass(el, "buttonDisabled");
            el.disabled = false;
          }
          else
          {
            Xinha._addClass(el, "buttonDisabled");
            el.disabled = true;
          }
        break;
        case "active":
          if ( newval )
          {
            Xinha._addClass(el, "buttonPressed");
          }
          else
          {
            Xinha._removeClass(el, "buttonPressed");
          }
        break;
      }
      this[id] = newval;
    }
  } // END of function: setButtonStatus

  // this function will handle creation of combo boxes.  Receives as
  // parameter the name of a button as defined in the toolBar config.
  // This function is called from createButton, above, if the given "txt"
  // doesn't match a button.
  function createSelect(txt)
  {
    var options = null;
    var el = null;
    var cmd = null;
    var customSelects = editor.config.customSelects;
    var context = null;
    var tooltip = "";
    switch (txt)
    {
      case "fontsize":
      case "fontname":
      case "formatblock":
        // the following line retrieves the correct
        // configuration option because the variable name
        // inside the Config object is named the same as the
        // button/select in the toolbar.  For instance, if txt
        // == "formatblock" we retrieve config.formatblock (or
        // a different way to write it in JS is
        // config["formatblock"].
        options = editor.config[txt];
        cmd = txt;
      break;
      default:
        // try to fetch it from the list of registered selects
        cmd = txt;
        var dropdown = customSelects[cmd];
        if ( typeof dropdown != "undefined" )
        {
          options = dropdown.options;
          context = dropdown.context;
          if ( typeof dropdown.tooltip != "undefined" )
          {
            tooltip = dropdown.tooltip;
          }
        }
        else
        {
          alert("ERROR [createSelect]:\nCan't find the requested dropdown definition");
        }
      break;
    }
    if ( options )
    {
      el = document.createElement("select");
      el.title = tooltip;
      el.style.width = 'auto';
      el.name = txt;
      var obj =
      {
        name	: txt, // field name
        element : el,	// the UI element (SELECT)
        enabled : true, // is it enabled?
        text	: false, // enabled in text mode?
        cmd	: cmd, // command ID
        state	: setButtonStatus, // for changing state
        context : context
      };
      
      Xinha.freeLater(obj);
      
      tb_objects[txt] = obj;
      
      for ( var i in options )
      {
        // prevent iterating over wrong type
        if ( typeof options[i] != 'string' )
        {
          continue;
        }
        var op = document.createElement("option");
        op.innerHTML = Xinha._lc(i);
        op.value = options[i];
        if (txt =='fontname' && editor.config.showFontStylesInToolbar)
        {
          op.style.fontFamily = options[i];
        }
        el.appendChild(op);
      }
      Xinha._addEvent(el, "change", function () { editor._comboSelected(el, txt); } );
    }
    return el;
  } // END of function: createSelect

  // appends a new button to toolbar
  function createButton(txt)
  {
    // the element that will be created
    var el, btn, obj = null;
    switch (txt)
    {
      case "separator":
        if ( editor.config.flowToolbars )
        {
          newLine();
        }
        el = document.createElement("div");
        el.className = "separator";
      break;
      case "space":
        el = document.createElement("div");
        el.className = "space";
      break;
      case "linebreak":
        newLine();
        return false;
      case "textindicator":
        el = document.createElement("div");
        el.appendChild(document.createTextNode("A"));
        el.className = "indicator";
        el.title = Xinha._lc("Current style");
        obj =
        {
          name	: txt, // the button name (i.e. 'bold')
          element : el, // the UI element (DIV)
          enabled : true, // is it enabled?
          active	: false, // is it pressed?
          text	: false, // enabled in text mode?
          cmd	: "textindicator", // the command ID
          state	: setButtonStatus // for changing state
        };
      
        Xinha.freeLater(obj);
      
        tb_objects[txt] = obj;
      break;
      default:
        btn = editor.config.btnList[txt];
    }
    if ( !el && btn )
    {
      el = document.createElement("a");
      el.style.display = 'block';
      el.href = 'javascript:void(0)';
      el.style.textDecoration = 'none';
      el.title = btn[0];
      el.className = "button";
      el.style.direction = "ltr";
      // let's just pretend we have a button object, and
      // assign all the needed information to it.
      obj =
      {
        name : txt, // the button name (i.e. 'bold')
        element : el, // the UI element (DIV)
        enabled : true, // is it enabled?
        active : false, // is it pressed?
        text : btn[2], // enabled in text mode?
        cmd	: btn[3], // the command ID
        state	: setButtonStatus, // for changing state
        context : btn[4] || null // enabled in a certain context?
      };
      Xinha.freeLater(el);
      Xinha.freeLater(obj);

      tb_objects[txt] = obj;

      // prevent drag&drop of the icon to content area
      el.ondrag = function() { return false; };

      // handlers to emulate nice flat toolbar buttons
      Xinha._addEvent(
        el,
        "mouseout",
        function(ev)
        {
          if ( obj.enabled )
          {
            //Xinha._removeClass(el, "buttonHover");
            Xinha._removeClass(el, "buttonActive");
            if ( obj.active )
            {
              Xinha._addClass(el, "buttonPressed");
            }
          }
        }
      );

      Xinha._addEvent(
        el,
        "mousedown",
        function(ev)
        {
          if ( obj.enabled )
          {
            Xinha._addClass(el, "buttonActive");
            Xinha._removeClass(el, "buttonPressed");
            Xinha._stopEvent(Xinha.is_ie ? window.event : ev);
          }
        }
      );

      // when clicked, do the following:
      Xinha._addEvent(
        el,
        "click",
        function(ev)
        {
          ev = ev || window.event;
          editor.btnClickEvent = {clientX : ev.clientX, clientY : ev.clientY};
          if ( obj.enabled )
          {
            Xinha._removeClass(el, "buttonActive");
            //Xinha._removeClass(el, "buttonHover");
            if ( Xinha.is_gecko )
            {
              editor.activateEditor();
            }
            // We pass the event to the action so they can can use it to
            // enhance the UI (e.g. respond to shift or ctrl-click)
            obj.cmd(editor, obj.name, obj, ev);
            Xinha._stopEvent(ev);
          }
        }
      );

      var i_contain = Xinha.makeBtnImg(btn[1]);
      var img = i_contain.firstChild;
      Xinha.freeLater(i_contain);
      Xinha.freeLater(img);
      
      el.appendChild(i_contain);

      obj.imgel = img;      
      obj.swapImage = function(newimg)
      {
        if ( typeof newimg != 'string' )
        {
          img.src = newimg[0];
          img.style.position = 'relative';
          img.style.top  = newimg[2] ? ('-' + (18 * (newimg[2] + 1)) + 'px') : '-18px';
          img.style.left = newimg[1] ? ('-' + (18 * (newimg[1] + 1)) + 'px') : '-18px';
        }
        else
        {
          obj.imgel.src = newimg;
          img.style.top = '0px';
          img.style.left = '0px';
        }
      };
      
    }
    else if( !el )
    {
      el = createSelect(txt);
    }

    return el;
  }

  var first = true;
  for ( var i = 0; i < this.config.toolbar.length; ++i )
  {
    if ( !first )
    {
      // createButton("linebreak");
    }
    else
    {
      first = false;
    }
    if ( this.config.toolbar[i] === null )
    {
      this.config.toolbar[i] = ['separator'];
    }
    var group = this.config.toolbar[i];

    for ( var j = 0; j < group.length; ++j )
    {
      var code = group[j];
      var tb_cell;
      if ( /^([IT])\[(.*?)\]/.test(code) )
      {
        // special case, create text label
        var l7ed = RegExp.$1 == "I"; // localized?
        var label = RegExp.$2;
        if ( l7ed )
        {
          label = Xinha._lc(label);
        }
        tb_cell = document.createElement("td");
        tb_row.appendChild(tb_cell);
        tb_cell.className = "label";
        tb_cell.innerHTML = label;
      }
      else if ( typeof code != 'function' )
      {
        var tb_element = createButton(code);
        if ( tb_element )
        {
          tb_cell = document.createElement("td");
          tb_cell.className = 'toolbarElement';
          tb_row.appendChild(tb_cell);
          tb_cell.appendChild(tb_element);
        }
        else if ( tb_element === null )
        {
          alert("FIXME: Unknown toolbar item: " + code);
        }
      }
    }
  }

  if ( editor.config.flowToolbars )
  {
    toolbar.appendChild(Xinha._createToolbarBreakingElement());
  }

  return toolbar;
};

/** creates a button (i.e. container element + image)
 * @private
 * @return {DomNode} conteainer element
 */
Xinha.makeBtnImg = function(imgDef, doc)
{
  if ( !doc )
  {
    doc = document;
  }

  if ( !doc._xinhaImgCache )
  {
    doc._xinhaImgCache = {};
    Xinha.freeLater(doc._xinhaImgCache);
  }

  var i_contain = null;
  if ( Xinha.is_ie && ( ( !doc.compatMode ) || ( doc.compatMode && doc.compatMode == "BackCompat" ) ) )
  {
    i_contain = doc.createElement('span');
    // IE10 Quirks :-/
    i_contain.style.display = 'inline-block';
  }
  else
  {
    i_contain = doc.createElement('div');
    i_contain.style.position = 'relative';
  }

  i_contain.style.overflow = 'hidden';
  i_contain.style.width = "18px";
  i_contain.style.height = "18px";
  i_contain.className = 'buttonImageContainer';

  var img = null;
  if ( typeof imgDef == 'string' )
  {
    if ( doc._xinhaImgCache[imgDef] )
    {
      img = doc._xinhaImgCache[imgDef].cloneNode();
    }
    else
    {
      if (Xinha.ie_version < 7 && /\.png$/.test(imgDef[0]))
      {
        img = doc.createElement("span");
      
        img.style.display = 'block';
        img.style.width = '18px';
        img.style.height = '18px';
        img.style.filter = 'progid:DXImageTransform.Microsoft.AlphaImageLoader(src="'+imgDef+'")';
		img.unselectable = 'on';
      }
      else
      {
        img = doc.createElement("img");
        img.src = imgDef;
      }
    }
  }
  else
  {
    if ( doc._xinhaImgCache[imgDef[0]] )
    {
      img = doc._xinhaImgCache[imgDef[0]].cloneNode();
    }
    else
    {
      if (Xinha.ie_version < 7 && /\.png$/.test(imgDef[0]))
      {
        img = doc.createElement("span");
        img.style.display = 'block';
        img.style.width = '18px';
        img.style.height = '18px';
        img.style.filter = 'progid:DXImageTransform.Microsoft.AlphaImageLoader(src="'+imgDef[0]+'")';
		img.unselectable = 'on';
      }
      else
      {
        img = doc.createElement("img");
        img.src = imgDef[0];
      }
      img.style.position = 'relative';
    }
    // @todo: Using 18 dont let us use a theme with its own icon toolbar height
    //        and width. Probably better to calculate this value 18
    //        var sizeIcon = img.width / nb_elements_per_image;
    img.style.top  = imgDef[2] ? ('-' + (18 * (imgDef[2] + 1)) + 'px') : '-18px';
    img.style.left = imgDef[1] ? ('-' + (18 * (imgDef[1] + 1)) + 'px') : '-18px';
  }
  i_contain.appendChild(img);
  return i_contain;
};
/** creates the status bar 
 * @private
 * @return {DomNode} status bar
 */
Xinha.prototype._createStatusBar = function()
{
  // TODO: Move styling into separate stylesheet
  this.setLoadingMessage(Xinha._lc('Create Statusbar'));
  var statusBar = document.createElement("div");
  statusBar.style.position = "relative";
  statusBar.className = "statusBar";
  statusBar.style.width = "100%";
  Xinha.freeLater(this, '_statusBar');

  var widgetContainer = document.createElement("div");
  widgetContainer.className = "statusBarWidgetContainer";
  widgetContainer.style.position = "absolute";
  widgetContainer.style.right = "0";
  widgetContainer.style.top = "0";
  widgetContainer.style.padding = "3px 3px 3px 10px";
  statusBar.appendChild(widgetContainer);

  // statusbar.appendChild(document.createTextNode(Xinha._lc("Path") + ": "));
  // creates a holder for the path view
  var statusBarTree = document.createElement("span");
  statusBarTree.className = "statusBarTree";
  if(Xinha.is_ios)
  {
    statusBarTree.innerHTML = Xinha._lc("Touch here first to activate editor.");
  }
  else
  {
    statusBarTree.innerHTML = Xinha._lc("Path") + ": ";
  }

  this._statusBarTree = statusBarTree;
  Xinha.freeLater(this, '_statusBarTree');
  statusBar.appendChild(statusBarTree);
  var statusBarTextMode = document.createElement("span");
  statusBarTextMode.innerHTML = Xinha.htmlEncode(Xinha._lc("You are in TEXT MODE.  Use the [<>] button to switch back to WYSIWYG."));
  statusBarTextMode.style.display = "none";

  this._statusBarTextMode = statusBarTextMode;
  Xinha.freeLater(this, '_statusBarTextMode');
  statusBar.appendChild(statusBarTextMode);

  statusBar.style.whiteSpace = "nowrap";

  var self = this;
  this.notifyOn("before_resize", function(evt, size) {
    self._statusBar.style.width = null;
  });
  this.notifyOn("resize", function(evt, size) {
    // HACK! IE6 doesn't update the width properly when resizing if it's 
    // given in pixels, but does hide the overflow content correctly when 
    // using 100% as the width. (FF, Safari and IE7 all require fixed
    // pixel widths to do the overflow hiding correctly.)
    if (Xinha.is_ie && Xinha.ie_version == 6)
    {
      self._statusBar.style.width = "100%";
    } 
    else
    {
      var width = size['width'];
      
      // ticket:1601 fixed here by 2px adjustment for borders
      //    and by setting box-sizing on .htmlarea .statusBar in Xinha.css
      self._statusBar.style.width = (width-2) + "px";
    }
  });

  this.notifyOn("modechange", function(evt, mode) {
    // Loop through all registered status bar items
    // and show them only if they're turned on for
    // the new mode.
    for (var i in self._statusWidgets)
    {
      var widget = self._statusWidgets[i];
      for (var index=0; index<widget.modes.length; index++)
      {
        if (widget.modes[index] == mode.mode)
        {
          var found = true;
        }
      }
      if (typeof found == 'undefined')
      {
        widget.block.style.display = "none";  
      }
      else
      {
        widget.block.style.display = "";
      }
    }
  });

  if ( !this.config.statusBar )
  {
    // disable it...
    statusBar.style.display = "none";
  }
  return statusBar;
};

/** Registers and inserts a new block for a widget in the status bar
 @param id unique string identifer for this block
 @param modes list of modes this block should be shown in

 @returns reference to HTML element inserted into the status bar
 */
Xinha.prototype.registerStatusWidget = function(id, modes)
{
  modes = modes || ['wysiwyg'];
  if (!this._statusWidgets)
  {
    this._statusWidgets = {};
  }

  var block = document.createElement("div");
  block.className = "statusBarWidget";
  block = this._statusBar.firstChild.appendChild(block);

  var showWidget = false;
  for (var i=0; i<modes.length; i++)
  {
    if (modes[i] == this._editMode)
    {
      showWidget = true;
    }
  }
  block.style.display = showWidget == true ? "" : "none";

  this._statusWidgets[id] = {block: block, modes: modes};
  return block;
};

/** Creates the Xinha object and replaces the textarea with it. Loads required files.
 *  @returns {Boolean}
 */
Xinha.prototype.generate = function ()
{
  if ( !Xinha.isSupportedBrowser )
  {
    return;
  }
  
  var i;
  var editor = this;  // we'll need "this" in some nested functions
  var url;
  var found = false;
  var links = document.getElementsByTagName("link");

  if (!document.getElementById("XinhaCoreDesign"))
  {
    _editor_css = (typeof _editor_css == "string") ? _editor_css : "Xinha.css";
    for(i = 0; i<links.length; i++)
    {
      if ( ( links[i].rel == "stylesheet" ) && ( links[i].href == _editor_url + _editor_css ) )
      {
        found = true;
      }
    }
    if ( !found )
    {
      Xinha.loadStyle(_editor_css,null,"XinhaCoreDesign",true);
    }
  }
  
  if ( _editor_skin !== "" && !document.getElementById("XinhaSkin"))
  {
    found = false;
    for(i = 0; i<links.length; i++)
    {
      if ( ( links[i].rel == "stylesheet" ) && ( links[i].href == _editor_url + 'skins/' + _editor_skin + '/skin.css' ) )
      {
        found = true;
      }
    }
    if ( !found )
    {
      Xinha.loadStyle('skins/' + _editor_skin + '/skin.css',null,"XinhaSkin");
    }
  }
  var callback = function() { editor.generate(); };
  // Now load a specific browser plugin which will implement the above for us.
  if (Xinha.is_ie)
  {
    url = _editor_url + 'modules/InternetExplorer/InternetExplorer.js';
    if ( !Xinha.loadPlugins([{plugin:"InternetExplorer",url:url}], callback ) )
    {            
      return false;
    }
    if (!this.plugins.InternetExplorer)
    {
      editor._browserSpecificPlugin = editor.registerPlugin('InternetExplorer');
    }
  }
  else if (Xinha.is_webkit)
  {
    url = _editor_url + 'modules/WebKit/WebKit.js';
    if ( !Xinha.loadPlugins([{plugin:"WebKit",url:url}], callback ) )
    {
      return false;
    }
    if (!this.plugins.Webkit)
    {
      editor._browserSpecificPlugin = editor.registerPlugin('WebKit');
    }
  }
  else if (Xinha.is_opera)
  {
    url = _editor_url + 'modules/Opera/Opera.js';
    if ( !Xinha.loadPlugins([{plugin:"Opera",url:url}], callback ) )
    {            
      return false;
    }
    if (!this.plugins.Opera)
    {
      editor._browserSpecificPlugin = editor.registerPlugin('Opera');
    }
  }
  else if (Xinha.is_gecko)
  {
    url = _editor_url + 'modules/Gecko/Gecko.js';
    if ( !Xinha.loadPlugins([{plugin:"Gecko",url:url}], callback ) )
    {            
      return false;
    }
    if (!this.plugins.Gecko) 
    {
      editor._browserSpecificPlugin = editor.registerPlugin('Gecko');
    }
  }

  if ( typeof Dialog == 'undefined' && !Xinha._loadback( _editor_url + 'modules/Dialogs/dialog.js', callback, this ) )
  {    
    return false;
  }

  if ( typeof Xinha.Dialog == 'undefined' &&  !Xinha._loadback( _editor_url + 'modules/Dialogs/XinhaDialog.js' , callback, this ) )
  {    
    return false;
  }
  
  url = _editor_url + 'modules/FullScreen/full-screen.js';
  if ( !Xinha.loadPlugins([{plugin:"FullScreen",url:url}], callback ))
  {
    return false;
  }
  
  url = _editor_url + 'modules/ColorPicker/ColorPicker.js';
  if ( !Xinha.loadPlugins([{plugin:"ColorPicker",url:url}], callback ) )
  {
    return false;
  }
  else if ( typeof Xinha.getPluginConstructor('ColorPicker') != 'undefined' && !this.plugins.colorPicker)
  {
    editor.registerPlugin('ColorPicker');
  }

  var toolbar = editor.config.toolbar;
  for ( i = toolbar.length; --i >= 0; )
  {
    for ( var j = toolbar[i].length; --j >= 0; )
    {
      switch (toolbar[i][j])
      {
        case "popupeditor":
        case "fullscreen":
          if (!this.plugins.FullScreen) 
          {
            editor.registerPlugin('FullScreen');
          }
        break;
        case "insertimage":
          url = _editor_url + 'modules/InsertImage/insert_image.js';
          if ( typeof Xinha.prototype._insertImage == 'undefined' && !Xinha.loadPlugins([{plugin:"InsertImage",url:url}], callback ) )
          {
            return false;
          }
          else if ( typeof Xinha.getPluginConstructor('InsertImage') != 'undefined' && !this.plugins.InsertImage)
          {
            editor.registerPlugin('InsertImage');
          }
        break;
        case "createlink":
          url = _editor_url + 'modules/CreateLink/link.js';
          if ( typeof Xinha.getPluginConstructor('Linker') == 'undefined' && !Xinha.loadPlugins([{plugin:"CreateLink",url:url}], callback ))
          {
            return false;
          }
          else if ( typeof Xinha.getPluginConstructor('CreateLink') != 'undefined' && !this.plugins.CreateLink) 
          {
            editor.registerPlugin('CreateLink');
          }
        break;
        case "inserttable":
          url = _editor_url + 'modules/InsertTable/insert_table.js';
          if ( !Xinha.loadPlugins([{plugin:"InsertTable",url:url}], callback ) )
          {
            return false;
          }
          else if ( typeof Xinha.getPluginConstructor('InsertTable') != 'undefined' && !this.plugins.InsertTable)
          {
            editor.registerPlugin('InsertTable');
          }
        break;
        case "about":
          url = _editor_url + 'modules/AboutBox/AboutBox.js';
          if ( !Xinha.loadPlugins([{plugin:"AboutBox",url:url}], callback ) )
          {
            return false;
          }
          else if ( typeof Xinha.getPluginConstructor('AboutBox') != 'undefined' && !this.plugins.AboutBox)
          {
            editor.registerPlugin('AboutBox');
          }
        break;
      }
    }
  }

  // If this is gecko, set up the paragraph handling now
  if ( Xinha.is_gecko &&  editor.config.mozParaHandler != 'built-in' )
  {
    if (  !Xinha.loadPlugins([{plugin:"EnterParagraphs",url: _editor_url + 'modules/Gecko/paraHandlerBest.js'}], callback ) )
    {
      return false;
    }
    if (!this.plugins.EnterParagraphs) 
    {
      editor.registerPlugin('EnterParagraphs');
    }
  }
  var getHtmlMethodPlugin = this.config.getHtmlMethod == 'TransformInnerHTML' ? _editor_url + 'modules/GetHtml/TransformInnerHTML.js' :  _editor_url + 'modules/GetHtml/DOMwalk.js';

  if ( !Xinha.loadPlugins([{plugin:"GetHtmlImplementation",url:getHtmlMethodPlugin}], callback))
  {
    return false;
  }
  else if (!this.plugins.GetHtmlImplementation)
  {
    editor.registerPlugin('GetHtmlImplementation');
  }
  function getTextContent(node)
  {
    return node.textContent || node.text;
  }
  if (_editor_skin)
  {
    this.skinInfo = {};
    var skinXML = Xinha._geturlcontent(_editor_url + 'skins/' + _editor_skin + '/skin.xml', true);
    if (skinXML)
    {
      var meta = skinXML.getElementsByTagName('meta');
      for (i=0;i<meta.length;i++)
      {
        this.skinInfo[meta[i].getAttribute('name')] = meta[i].getAttribute('value');
      }
      var recommendedIcons = skinXML.getElementsByTagName('recommendedIcons');
      if (!_editor_icons && recommendedIcons.length && getTextContent(recommendedIcons[0]))
      {
        _editor_icons = getTextContent(recommendedIcons[0]);
      }
    }
  }
  if (_editor_icons) 
  {
    var iconsXML = Xinha._geturlcontent(_editor_url + 'iconsets/' + _editor_icons + '/iconset.xml', true);

    if (iconsXML)
    {
      var icons = iconsXML.getElementsByTagName('icon');
      var icon, id, path, type, x, y;

      for (i=0;i<icons.length;i++)
      {
        icon = icons[i];
        id = icon.getAttribute('id');
        
        if (icon.getElementsByTagName(_editor_lang).length)
        {
          icon = icon.getElementsByTagName(_editor_lang)[0];
        }
        else
        {
          icon = icon.getElementsByTagName('default')[0];
        }
        path = getTextContent(icon.getElementsByTagName('path')[0]);
        path = (!/^\//.test(path) ? _editor_url : '') + path;
        type = icon.getAttribute('type');
        if (type == 'map')
        {
          x = parseInt(getTextContent(icon.getElementsByTagName('x')[0]), 10);
          y = parseInt(getTextContent(icon.getElementsByTagName('y')[0]), 10);
          if (this.config.btnList[id]) 
          {
            this.config.btnList[id][1] = [path, x, y];
          }
          if (this.config.iconList[id]) 
          {
            this.config.iconList[id] = [path, x, y];
          }
          
        }
        else
        {
          if (this.config.btnList[id]) 
          {
            this.config.btnList[id][1] = path;
          }
          if (this.config.iconList[id]) 
          {
            this.config.iconList[id] = path;
          }
        }
      }
    }
  }
  
  // create the editor framework, yah, table layout I know, but much easier
  // to get it working correctly this way, sorry about that, patches welcome.
  
  this.setLoadingMessage(Xinha._lc('Generate Xinha framework'));
  
  this._framework =
  {
    'table':   document.createElement('table'),
    'tbody':   document.createElement('tbody'), // IE will not show the table if it doesn't have a tbody!
    'tb_row':  document.createElement('tr'),
    'tb_cell': document.createElement('td'), // Toolbar

    'tp_row':  document.createElement('tr'),
    'tp_cell': this._panels.top.container,   // top panel

    'ler_row': document.createElement('tr'),
    'lp_cell': this._panels.left.container,  // left panel
    'ed_cell': document.createElement('td'), // editor
    'rp_cell': this._panels.right.container, // right panel

    'bp_row':  document.createElement('tr'),
    'bp_cell': this._panels.bottom.container,// bottom panel

    'sb_row':  document.createElement('tr'),
    'sb_cell': document.createElement('td')  // status bar

  };
  Xinha.freeLater(this._framework);
  
  var fw = this._framework;
  fw.table.border = "0";
  fw.table.cellPadding = "0";
  fw.table.cellSpacing = "0";

  fw.tb_row.style.verticalAlign = 'top';
  fw.tp_row.style.verticalAlign = 'top';
  fw.ler_row.style.verticalAlign= 'top';
  fw.bp_row.style.verticalAlign = 'top';
  fw.sb_row.style.verticalAlign = 'top';
  fw.ed_cell.style.position     = 'relative';

  // Put the cells in the rows        set col & rowspans
  // note that I've set all these so that all panels are showing
  // but they will be redone in sizeEditor() depending on which
  // panels are shown.  It's just here to clarify how the thing
  // is put togethor.
  fw.tb_row.appendChild(fw.tb_cell);
  fw.tb_cell.colSpan = 3;

  fw.tp_row.appendChild(fw.tp_cell);
  fw.tp_cell.colSpan = 3;

  fw.ler_row.appendChild(fw.lp_cell);
  fw.ler_row.appendChild(fw.ed_cell);
  fw.ler_row.appendChild(fw.rp_cell);

  fw.bp_row.appendChild(fw.bp_cell);
  fw.bp_cell.colSpan = 3;

  fw.sb_row.appendChild(fw.sb_cell);
  fw.sb_cell.colSpan = 3;

  // Put the rows in the table body
  fw.tbody.appendChild(fw.tb_row);  // Toolbar
  fw.tbody.appendChild(fw.tp_row); // Left, Top, Right panels
  fw.tbody.appendChild(fw.ler_row);  // Editor/Textarea
  fw.tbody.appendChild(fw.bp_row);  // Bottom panel
  fw.tbody.appendChild(fw.sb_row);  // Statusbar

  // and body in the table
  fw.table.appendChild(fw.tbody);

  var xinha = fw.table;
  this._htmlArea = xinha;
  Xinha.freeLater(this, '_htmlArea');
  xinha.className = "htmlarea";

    // create the toolbar and put in the area
  fw.tb_cell.appendChild( this._createToolbar() );

    // create the IFRAME & add to container
  var iframe = document.createElement("iframe");
  iframe.src = this.popupURL(editor.config.URIs.iframe_src);
  iframe.id = "XinhaIFrame_" + this._textArea.id;
  fw.ed_cell.appendChild(iframe);
  this._iframe = iframe;
  this._iframe.className = 'xinha_iframe';
  Xinha.freeLater(this, '_iframe');
  
    // creates & appends the status bar
  var statusbar = this._createStatusBar();
  this._statusBar = fw.sb_cell.appendChild(statusbar);


  // insert Xinha before the textarea.
  var textarea = this._textArea;
  textarea.parentNode.insertBefore(xinha, textarea);
  textarea.className = 'xinha_textarea';

  // extract the textarea and insert it into the xinha framework
  Xinha.removeFromParent(textarea);
  fw.ed_cell.appendChild(textarea);

  // if another editor is activated while this one is in text mode, toolbar is disabled   
  Xinha.addDom0Event(
  this._textArea,
  'click',
  function()
  {
  	if ( Xinha._currentlyActiveEditor != this)
  	{
  	  editor.updateToolbar();
  	}
    return true;
  });
  
  // Set up event listeners for saving the iframe content to the textarea
  if ( textarea.form )
  {
    // onsubmit get the Xinha content and update original textarea.
    Xinha.prependDom0Event(
      this._textArea.form,
      'submit',
      function()
      {
        editor.firePluginEvent('onBeforeSubmit');
        editor._textArea.value = editor.outwardHtml(editor.getHTML());
        editor.firePluginEvent('onBeforeSubmitTextArea');
        return true;
      }
    );

    var initialTAContent = textarea.value;

    // onreset revert the Xinha content to the textarea content
    Xinha.prependDom0Event(
      this._textArea.form,
      'reset',
      function()
      {
        editor.setHTML(editor.inwardHtml(initialTAContent));
        editor.updateToolbar();
        return true;
      }
    );

    // attach onsubmit handler to form.submit()
    // note: catch error in IE if any form element has id="submit"
    if ( !textarea.form.xinha_submit )
    {
      try 
      {
        textarea.form.xinha_submit = textarea.form.submit;
        textarea.form.submit = function() 
        {
          this.onsubmit();
          this.xinha_submit();
        };
      } catch(ex) {}
    }
  }

  // add a handler for the "back/forward" case -- on body.unload we save
  // the HTML content into the original textarea and restore it in its place.
  // apparently this does not work in IE?
  Xinha.prependDom0Event(
    window,
    'unload',
    function()
    {
      editor.firePluginEvent('onBeforeUnload');
      textarea.value = editor.outwardHtml(editor.getHTML());
      if (!Xinha.is_ie)
      {
        xinha.parentNode.replaceChild(textarea,xinha);
      }
      return true;
    }
  );

  // Hide textarea
  textarea.style.display = "none";

  // Initalize size
  editor.initSize();
  this.setLoadingMessage(Xinha._lc('Finishing'));
  // Add an event to initialize the iframe once loaded.
  editor._iframeLoadDone = false;
  if (iframe.src == 'javascript:\'\'' || iframe.src == '' || Xinha.is_opera)
  {
    editor.initIframe();
  }
  // I suspect we no longer need this and can just use editor.initIframe (certainly 
  // iframe.src is javascritp:'' by default.  But I will leave this for posterity
  // just in case
  else 
  {
    Xinha._addEvent(
      this._iframe,
      'load',
      function(e)
      {
        if ( !editor._iframeLoadDone )
        {
          editor._iframeLoadDone = true;
          editor.initIframe();
        }
        return true;
      }
    );
  }
};

/**
 * Size the editor according to the INITIAL sizing information.
 * config.width
 *    The width may be set via three ways
 *    auto    = the width is inherited from the original textarea
 *    toolbar = the width is set to be the same size as the toolbar
 *    <set size> = the width is an explicit size (any CSS measurement, eg 100em should be fine)
 *
 * config.height
 *    auto    = the height is inherited from the original textarea
 *    <set size> = an explicit size measurement (again, CSS measurements)
 *
 * config.sizeIncludesBars
 *    true    = the tool & status bars will appear inside the width & height confines
 *    false   = the tool & status bars will appear outside the width & height confines
 *
 * @private
 */

Xinha.prototype.initSize = function()
{
  this.setLoadingMessage(Xinha._lc('Init editor size'));
  var editor = this;
  var width = null;
  var height = null;

  switch ( this.config.width )
  {
    case 'auto':
      width = this._initial_ta_size.w;
    break;

    case 'toolbar':
      width = this._toolBar.offsetWidth + 'px';
    break;

    default :
      // @todo: check if this is better :
      // width = (parseInt(this.config.width, 10) == this.config.width)? this.config.width + 'px' : this.config.width;
      width = /[^0-9]/.test(this.config.width) ? this.config.width : this.config.width + 'px';
    break;
  }
      // @todo: check if this is better :
      // height = (parseInt(this.config.height, 10) == this.config.height)? this.config.height + 'px' : this.config.height;
  height = this.config.height == 'auto' ? this._initial_ta_size.h : /[^0-9]/.test(this.config.height) ? this.config.height : this.config.height + 'px';
  
  this.sizeEditor(width, height, this.config.sizeIncludesBars, this.config.sizeIncludesPanels);

  // why can't we use the following line instead ?
//  this.notifyOn('panel_change',this.sizeEditor);
  this.notifyOn('panel_change',function() { editor.sizeEditor(); });
};

/** Get the overall size of the editor, including toolbars and panels
 *  (if they are being considered)
 *
 *  This is used by sizeEditor to get the size of the area excluding toolbars
 *  when it sets the _htmlArea.size attribute first explicitly and then
 *  adds in toolbars/panels later.
 *
 */

Xinha.prototype.getOverallSize = function(useStylePxSize)
{
  // Originally when sizeEditor was adjusting for panel/toolbar
  // size after having set _htmlArea.style.width/height it always
  // used the offsetWidth/Height, even if it set a px width/height
  // on the style just before.
  //
  // In the interests of not-fixing-whats-not-broke, we are only
  // going to look at the style.width/height pixel size if 
  // this is in resizing mode.  It actually works ok for 
  // non-resizable editors too but I have not tested old ones.
  //
  // Note that if you use offsetWidth/Height for a resizable 
  // editor during resize, it works OK but for some reason I don't
  // quite understand the minimum size you can reach (in Chrome)
  // is not as small as when you observe style.width/height
  
  if(typeof useStylePxSize == 'undefined')
  {
    useStylePxSize = this.config.resizableEditor;
  }
  
  var size = { w: 0, h: 0, width: 0, height:0, offsetWidth: 0, offsetHeight: 0 };
  
  if(useStylePxSize && this._htmlArea.style.width.match(/px/))
  {
    size.w = parseInt(this._htmlArea.style.width.replace(/px/,''));
  }
  else
  {
    size.w = this._htmlArea.offsetWidth;
  }
  
  if(useStylePxSize && this._htmlArea.style.height.match(/px/))
  {
    size.h = parseInt(this._htmlArea.style.height.replace(/px/,''));
  }
  else
  {
    size.h = this._htmlArea.offsetHeight;
  }
  
  size.offsetHeight = size.h;
  size.height       = this._htmlArea.style.height;
  size.offsetWidth  = size.w;
  size.width        = this._htmlArea.style.width;
  
  return size;
};

/**
 *  Size the editor to a specific size, or just refresh the size (when window resizes for example)
 *  @param {string} width optional width (CSS specification)
 *  @param {string} height optional height (CSS specification)
 *  @param {Boolean} includingBars optional to indicate if the size should include or exclude tool & status bars
 *  @param {Boolean} includingPanels optional to indicate if the size should include or exclude panels
 */
Xinha.prototype.sizeEditor = function(width, height, includingBars, includingPanels)
{
  if (this._risizing) 
  {
    return;
  }
  this._risizing = true;
  
  var framework = this._framework;
  
  this.notifyOf('before_resize', {width:width, height:height});
  this.firePluginEvent('onBeforeResize', width, height);
  // We need to set the iframe & textarea to 100% height so that the htmlarea
  // isn't "pushed out" when we get it's height, so we can change them later.
  this._iframe.style.height   = '100%';
  //here 100% can lead to an effect that the editor is considerably higher in text mode
  this._textArea.style.height = '1px';
  
  this._iframe.style.width    = '0px';
  this._textArea.style.width  = '0px';

  if ( includingBars !== null )
  {
    this._htmlArea.sizeIncludesToolbars = includingBars;
  }
  if ( includingPanels !== null )
  {
    this._htmlArea.sizeIncludesPanels = includingPanels;
  }

  if ( width )
  {
    this._htmlArea.style.width = width;
    if ( !this._htmlArea.sizeIncludesPanels )
    {
      // Need to add some for l & r panels
      var rPanel = this._panels.right;
      if ( rPanel.on && rPanel.panels.length && Xinha.hasDisplayedChildren(rPanel.div) )
      {
        this._htmlArea.style.width = (this.getOverallSize().offsetWidth + parseInt(this.config.panel_dimensions.right, 10)) + 'px';
      }

      var lPanel = this._panels.left;
      if ( lPanel.on && lPanel.panels.length && Xinha.hasDisplayedChildren(lPanel.div) )
      {
        this._htmlArea.style.width = (this.getOverallSize().offsetWidth + parseInt(this.config.panel_dimensions.left, 10)) + 'px';
      }
    }
  }

  if ( height )
  {
    this._htmlArea.style.height = height;
    if ( !this._htmlArea.sizeIncludesToolbars )
    {
      // Need to add some for toolbars
      this._htmlArea.style.height = (this.getOverallSize().offsetHeight + this._toolbar.offsetHeight + this._statusBar.offsetHeight) + 'px';
    }

    if ( !this._htmlArea.sizeIncludesPanels )
    {
      // Need to add some for t & b panels
      var tPanel = this._panels.top;
      if ( tPanel.on && tPanel.panels.length && Xinha.hasDisplayedChildren(tPanel.div) )
      {
        this._htmlArea.style.height = (this.getOverallSize().offsetHeight + parseInt(this.config.panel_dimensions.top, 10)) + 'px';
      }

      var bPanel = this._panels.bottom;
      if ( bPanel.on && bPanel.panels.length && Xinha.hasDisplayedChildren(bPanel.div) )
      {
        this._htmlArea.style.height = (this.getOverallSize().offsetHeight + parseInt(this.config.panel_dimensions.bottom, 10)) + 'px';
      }
    }
  }

  // At this point we have this._htmlArea.style.width & this._htmlArea.style.height
  // which are the size for the OUTER editor area, including toolbars and panels
  // now we size the INNER area and position stuff in the right places.
  width  = this._htmlArea.offsetWidth;
  height = this._htmlArea.offsetHeight;
  
  

  // Set colspan for toolbar, and statusbar, rowspan for left & right panels, and insert panels to be displayed
  // into thier rows
  var panels = this._panels;
  var editor = this;
  var col_span = 1;

  function panel_is_alive(pan)
  {
    if ( panels[pan].on && panels[pan].panels.length && Xinha.hasDisplayedChildren(panels[pan].container) )
    {
      panels[pan].container.style.display = '';
      return true;
    }
    // Otherwise make sure it's been removed from the framework
    else
    {
      panels[pan].container.style.display='none';
      return false;
    }
  }

  if ( panel_is_alive('left') )
  {
    col_span += 1;      
  }

//  if ( panel_is_alive('top') )
//  {
    // NOP
//  }

  if ( panel_is_alive('right') )
  {
    col_span += 1;
  }

//  if ( panel_is_alive('bottom') )
//  {
    // NOP
//  }

  framework.tb_cell.colSpan = col_span;
  framework.tp_cell.colSpan = col_span;
  framework.bp_cell.colSpan = col_span;
  framework.sb_cell.colSpan = col_span;

  // Put in the panel rows, top panel goes above editor row
  if ( !framework.tp_row.childNodes.length )
  {
    Xinha.removeFromParent(framework.tp_row);
  }
  else
  {
    if ( !Xinha.hasParentNode(framework.tp_row) )
    {
      framework.tbody.insertBefore(framework.tp_row, framework.ler_row);
    }
  }

  // bp goes after the editor
  if ( !framework.bp_row.childNodes.length )
  {
    Xinha.removeFromParent(framework.bp_row);
  }
  else
  {
    if ( !Xinha.hasParentNode(framework.bp_row) )
    {
      framework.tbody.insertBefore(framework.bp_row, framework.ler_row.nextSibling);
    }
  }

  // finally if the statusbar is on, insert it
  if ( !this.config.statusBar )
  {
    Xinha.removeFromParent(framework.sb_row);
  }
  else
  {
    if ( !Xinha.hasParentNode(framework.sb_row) )
    {
      framework.table.appendChild(framework.sb_row);
    }
  }

  // Size and set colspans, link up the framework
  framework.lp_cell.style.width  = this.config.panel_dimensions.left;
  framework.rp_cell.style.width  = this.config.panel_dimensions.right;
  framework.tp_cell.style.height = this.config.panel_dimensions.top;
  framework.bp_cell.style.height = this.config.panel_dimensions.bottom;
  framework.tb_cell.style.height = this._toolBar.offsetHeight + 'px';
  framework.sb_cell.style.height = this._statusBar.offsetHeight + 'px';

  var edcellheight = height - this._toolBar.offsetHeight - this._statusBar.offsetHeight;
  if ( panel_is_alive('top') )
  {
    edcellheight -= parseInt(this.config.panel_dimensions.top, 10);
  }
  if ( panel_is_alive('bottom') )
  {
    edcellheight -= parseInt(this.config.panel_dimensions.bottom, 10);
  }
  this._iframe.style.height = edcellheight + 'px';  
  
  var edcellwidth = width;
  if ( panel_is_alive('left') )
  {
    edcellwidth -= parseInt(this.config.panel_dimensions.left, 10);
  }
  if ( panel_is_alive('right') )
  {
    edcellwidth -= parseInt(this.config.panel_dimensions.right, 10);    
  }
  var iframeWidth = this.config.iframeWidth ? parseInt(this.config.iframeWidth,10) : null; 
  this._iframe.style.width = (iframeWidth && iframeWidth < edcellwidth) ? iframeWidth + "px": edcellwidth + "px"; 

  this._textArea.style.height = this._iframe.style.height;
  this._textArea.style.width  = this._iframe.style.width;
     
  this.notifyOf('resize', {width:this._htmlArea.offsetWidth, height:this._htmlArea.offsetHeight});
  this.firePluginEvent('onResize',this._htmlArea.offsetWidth, this._htmlArea.offsetWidth);
  this._risizing = false;
};
/** FIXME: Never used, what is this for? 
* @param {string} side 
* @param {Object}
*/
Xinha.prototype.registerPanel = function(side, object)
{
  if ( !side )
  {
    side = 'right';
  }
  this.setLoadingMessage('Register ' + side + ' panel ');
  var panel = this.addPanel(side);
  if ( object )
  {
    object.drawPanelIn(panel);
  }
};
/** Creates a panel in the panel container on the specified side
* @param {String} side the panel container to which the new panel will be added<br />
*									Possible values are: "right","left","top","bottom"
* @returns {DomNode} Panel div
*/
Xinha.prototype.addPanel = function(side)
{
  var div = document.createElement('div');
  div.side = side;
  if ( side == 'left' || side == 'right' )
  {
    div.style.width  = this.config.panel_dimensions[side];
    if (this._iframe) 
    {
      div.style.height = this._iframe.style.height;
    }
  }
  Xinha.addClasses(div, 'panel');
  this._panels[side].panels.push(div);
  this._panels[side].div.appendChild(div);

  this.notifyOf('panel_change', {'action':'add','panel':div});
  this.firePluginEvent('onPanelChange','add',div);
  return div;
};
/** Removes a panel
* @param {DomNode} panel object as returned by Xinha.prototype.addPanel()
*/
Xinha.prototype.removePanel = function(panel)
{
  this._panels[panel.side].div.removeChild(panel);
  var clean = [];
  for ( var i = 0; i < this._panels[panel.side].panels.length; i++ )
  {
    if ( this._panels[panel.side].panels[i] != panel )
    {
      clean.push(this._panels[panel.side].panels[i]);
    }
  }
  this._panels[panel.side].panels = clean;
  this.notifyOf('panel_change', {'action':'remove','panel':panel});
  this.firePluginEvent('onPanelChange','remove',panel);
};
/** Hides a panel
* @param {DomNode} panel object as returned by Xinha.prototype.addPanel()
*/
Xinha.prototype.hidePanel = function(panel)
{
  if ( panel && panel.style.display != 'none' )
  {
    try { var pos = this.scrollPos(this._iframe.contentWindow); } catch(e) { }
    panel.style.display = 'none';
    this.notifyOf('panel_change', {'action':'hide','panel':panel});
    this.firePluginEvent('onPanelChange','hide',panel);
    try { this._iframe.contentWindow.scrollTo(pos.x,pos.y); } catch(e) { }
  }
};
/** Shows a panel
* @param {DomNode} panel object as returned by Xinha.prototype.addPanel()
*/
Xinha.prototype.showPanel = function(panel)
{
  if ( panel && panel.style.display == 'none' )
  {
    try { var pos = this.scrollPos(this._iframe.contentWindow); } catch(e) {}
    panel.style.display = '';
    this.notifyOf('panel_change', {'action':'show','panel':panel});
    this.firePluginEvent('onPanelChange','show',panel);
    try { this._iframe.contentWindow.scrollTo(pos.x,pos.y); } catch(e) { }
  }
};
/** Hides the panel(s) on one or more sides
* @param {Array} sides the sides on which the panels shall be hidden
*/
Xinha.prototype.hidePanels = function(sides)
{
  if ( typeof sides == 'undefined' )
  {
    sides = ['left','right','top','bottom'];
  }

  var reShow = [];
  for ( var i = 0; i < sides.length;i++ )
  {
    if ( this._panels[sides[i]].on )
    {
      reShow.push(sides[i]);
      this._panels[sides[i]].on = false;
    }
  }
  this.notifyOf('panel_change', {'action':'multi_hide','sides':sides});
  this.firePluginEvent('onPanelChange','multi_hide',sides);
};
/** Shows the panel(s) on one or more sides
* @param {Array} sides the sides on which the panels shall be hidden
*/
Xinha.prototype.showPanels = function(sides)
{
  if ( typeof sides == 'undefined' )
  {
    sides = ['left','right','top','bottom'];
  }

  var reHide = [];
  for ( var i = 0; i < sides.length; i++ )
  {
    if ( !this._panels[sides[i]].on )
    {
      reHide.push(sides[i]);
      this._panels[sides[i]].on = true;
    }
  }
  this.notifyOf('panel_change', {'action':'multi_show','sides':sides});
  this.firePluginEvent('onPanelChange','multi_show',sides);
};
/** Returns an array containig all properties that are set in an object
* @param {Object} obj
* @returns {Array}
*/
Xinha.objectProperties = function(obj)
{
  var props = [];
  for ( var x in obj )
  {
    props[props.length] = x;
  }
  return props;
};

/** Checks if editor is active
 *<br />
 * EDITOR ACTIVATION NOTES:<br />
 *  when a page has multiple Xinha editors, ONLY ONE should be activated at any time (this is mostly to
 *  work around a bug in Mozilla, but also makes some sense).  No editor should be activated or focused
 *  automatically until at least one editor has been activated through user action (by mouse-clicking in
 *  the editor).
 * @private
 * @returns {Boolean}
 */
Xinha.prototype.editorIsActivated = function()
{
  try
  {
    return Xinha.is_designMode ? this._doc.designMode == 'on' : this._doc.body.contentEditable;
  }
  catch (ex)
  {
    return false;
  }
};
/**  We need to know that at least one editor on the page has been activated
*    this is because we will not focus any editor until an editor has been activated
* @private
* @type {Boolean}
*/
Xinha._someEditorHasBeenActivated = false;
/**  Stores a reference to the currently active editor
* @private
* @type {Xinha}
*/
Xinha._currentlyActiveEditor      = null;
/** Enables one editor for editing, e.g. by a click in the editing area or after it has been 
 *  deactivated programmatically before 
 * @private
 * @returns {Boolean}
 */
Xinha.prototype.activateEditor = function()
{
  if (this.currentModal) 
  {
    return;
  }
  // We only want ONE editor at a time to be active
  if ( Xinha._currentlyActiveEditor )
  {
    if ( Xinha._currentlyActiveEditor == this )
    {
      return true;
    }
    Xinha._currentlyActiveEditor.deactivateEditor();
  }

  if ( Xinha.is_designMode && this._doc.designMode != 'on' )
  {
    try
    {
      // cannot set design mode if no display
      if ( this._iframe.style.display == 'none' )
      {
        this._iframe.style.display = '';
        this._doc.designMode = 'on';
        this._iframe.style.display = 'none';
      }
      else
      {
        this._doc.designMode = 'on';
      }

      // Opera loses some of it's event listeners when the designMode is set to on.
	  // the true just shortcuts the method to only set some listeners.
      if(Xinha.is_opera) this.setEditorEvents(true);

    } catch (ex) {}
  }
  else if ( Xinha.is_ie&& this._doc.body.contentEditable !== true )
  {
    this._doc.body.contentEditable = true;
  }

  Xinha._someEditorHasBeenActivated = true;
  Xinha._currentlyActiveEditor      = this;

  var editor = this;
  this.enableToolbar();
};
/** Disables the editor 
 * @private
 */
Xinha.prototype.deactivateEditor = function()
{
  // If the editor isn't active then the user shouldn't use the toolbar
  this.disableToolbar();

  if(Xinha.is_ios)
  {
    this._statusBarTree.innerHTML = Xinha._lc("Touch here first to activate editor.");
  }
  else
  {
    this._statusBarTree.innerHTML = Xinha._lc("Path") + ": ";
  }

  if ( Xinha.is_designMode && this._doc.designMode != 'off' )
  {
    try
    {
      this._doc.designMode = 'off';
    } catch (ex) {}
  }
  else if ( !Xinha.is_designMode && this._doc.body.contentEditable !== false )
  {
    this._doc.body.contentEditable = false;
  }

  if ( Xinha._currentlyActiveEditor != this )
  {
    // We just deactivated an editor that wasn't marked as the currentlyActiveEditor

    return; // I think this should really be an error, there shouldn't be a situation where
            // an editor is deactivated without first being activated.  but it probably won't
            // hurt anything.
  }

  Xinha._currentlyActiveEditor = false;
};
/** Creates the iframe (editable area)
 * @private
 */
Xinha.prototype.initIframe = function()
{
  this.disableToolbar();
  var doc = null;
  var editor = this;

  // It is possible that we need to wait a bit more for the iframe
  //  browsers vary a bit on how they treat the iframe load events
  //  so even though we should just be able to listen to load, maybe
  //  that doesn't work quite right, just waiting a bit if it fails
  //  and then trying again seems like a good idea.
  if(typeof editor._iframeWaitingRetries == 'undefined' )    
  {
    editor._iframeWaitingRetries = 10;
  }
 
  // For each retry we lengthen the delay, just so we don't spam the console
  //  with messages really
  var retryDelay = (625 / (editor._iframeWaitingRetries * editor._iframeWaitingRetries ) ) * 50;
  editor._iframeWaitingRetries--;

  try
  {
    if ( editor._iframe.contentDocument )
    {
      this._doc = editor._iframe.contentDocument;        
    }
    else
    {
      this._doc = editor._iframe.contentWindow.document;
    }
    doc = this._doc;
    // try later
    if ( !doc )
    {
      if( editor._iframeWaitingRetries > 0 )
      {
        Xinha.debugMsg("Still waiting for Iframe...");
        editor.setLoadingMessage(Xinha._lc("Waiting for Iframe to load..."));
        setTimeout(function() { editor.initIframe(); }, retryDelay);
        return false;
      }
      else
      {
        // Give up
        Xinha.debugMsg("Xinha: Unable to access the Iframe document object, this may be the result of a Cross-Origin restriction, very slow network, or some other issue.", 'warn');
        Xinha.debugMsg("You may wish to try changing xinha_config.URIs.iframe_src from "+editor.config.URIs.iframe_src+" to a url of a blank page on your server (eg '/blank.html') and see if that helps.", 'warn');
        editor.setLoadingMessage(Xinha._lc("Error Loading Xinha.  Developers, check the Error Console for information."));
        return false;
      }
    }
  }
  catch(ex)
  { // try later
    if(editor._iframeWaitingRetries > 0)
    {
      Xinha.debugMsg("Still waiting for Iframe...");
      editor.setLoadingMessage(Xinha._lc("Waiting for Iframe to load..."));
      setTimeout(function() { editor.initIframe(); }, retryDelay);
      return false;
    }
    else
    {
      Xinha.debugMsg(ex, 'warn');
      Xinha.debugMsg("Xinha: Unable to access the Iframe document object, this may be the result of a Cross-Origin restriction, very slow network, or some other issue.", 'warn');
      Xinha.debugMsg("You may wish to try changing xinha_config.URIs.iframe_src from "+editor.config.URIs.iframe_src+" to a url of a blank page on your server (eg '/blank.html') and see if that helps.", 'warn');
      editor.setLoadingMessage(Xinha._lc("Error Loading Xinha.  Developers, check the Error Console for information."));
      return false;
    }
  }
  
  Xinha.freeLater(this, '_doc');

  doc.open("text/html","replace");
  var html = '', doctype;
  if ( editor.config.browserQuirksMode === false )
  {
    doctype = '<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">';
  }
  else if ( editor.config.browserQuirksMode === true )
  {
    doctype = '';
  }
  else
  {
    doctype = Xinha.getDoctype(document);
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
    
    html += Xinha.addCoreCSS();
    
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
    html += "<body" + (editor.config.bodyID ? (" id=\"" + editor.config.bodyID + "\"") : '') + (editor.config.bodyClass ? (" class=\"" + editor.config.bodyClass + "\"") : '') + ">\n";
    html +=   editor.inwardHtml(editor._textArea.value);
    html += "</body>\n";
    html += "</html>";
  }
  else
  {
    html = editor.inwardHtml(editor._textArea.value);
    if ( html.match(Xinha.RE_doctype) )
    {
      editor.setDoctype(RegExp.$1);
      //html = html.replace(Xinha.RE_doctype, "");
    }
    
    //Fix Firefox problem with link elements not in right place (just before head)
    var match = html.match(/<link\s+[\s\S]*?["']\s*\/?>/gi);
    html = html.replace(/<link\s+[\s\S]*?["']\s*\/?>\s*/gi, '');
    if (match)
    {
      html = html.replace(/<\/head>/i, match.join('\n') + "\n</head>");
    }
  }
  doc.write(html);
  doc.close();
  if ( this.config.fullScreen )
  {
    this._fullScreen();
  }
  this.setEditorEvents();


  // If this IFRAME had been configured for autofocus, we'll focus it now,
  // since everything needed to do so is now fully loaded.
  if ((typeof editor.config.autofocus != "undefined") && editor.config.autofocus !== false &&
      ((editor.config.autofocus == editor._textArea.id) || editor.config.autofocus == true))
  {
    editor.activateEditor();
    editor.focusEditor();
  }
};
  
/**
 * Delay a function until the document is ready for operations.
 * See ticket:547
 * @public
 * @param {Function} f  The function to call once the document is ready
 */
Xinha.prototype.whenDocReady = function(f)
{
  var e = this;
  if ( this._doc && this._doc.body )
  {
    f();
  }
  else
  {
    setTimeout(function() { e.whenDocReady(f); }, 50);
  }
};


/** Switches editor mode between wysiwyg and text (HTML)
 * @param {String} mode optional "textmode" or "wysiwyg", if omitted, toggles between modes.
 */
Xinha.prototype.setMode = function(mode)
{
  var html;
  if ( typeof mode == "undefined" )
  {
    mode = this._editMode == "textmode" ? "wysiwyg" : "textmode";
  }
  switch ( mode )
  {
    case "textmode":
      this.firePluginEvent('onBeforeMode', 'textmode');
      this._toolbarObjects.htmlmode.swapImage(this.config.iconList.wysiwygmode); 
      this.setCC("iframe");
      html = this.outwardHtml(this.getHTML());
      this.setHTML(html);

      // Hide the iframe
      this.deactivateEditor();
      this._iframe.style.display   = 'none';
      this._textArea.style.display = '';

      if ( this.config.statusBar )
      {
        this._statusBarTree.style.display = "none";
        this._statusBarTextMode.style.display = "";
      }

      this.notifyOf('modechange', {'mode':'text'});
      this.firePluginEvent('onMode', 'textmode');
      this.findCC("textarea");
    break;

    case "wysiwyg":
      this.firePluginEvent('onBeforeMode', 'wysiwyg');
      this._toolbarObjects.htmlmode.swapImage([this.imgURL('images/ed_buttons_main.png'),7,0]); 
      this.setCC("textarea");
      html = this.inwardHtml(this.getHTML());
      this.deactivateEditor();
      this.setHTML(html);
      this._iframe.style.display   = '';
      this._textArea.style.display = "none";
      this.activateEditor();
      if ( this.config.statusBar )
      {
        this._statusBarTree.style.display = "";
        this._statusBarTextMode.style.display = "none";
      }
      
      this.notifyOf('modechange', {'mode':'wysiwyg'});
      this.firePluginEvent('onMode', 'wysiwyg');
      this.findCC("iframe");
    break;

    default:
      alert("Mode <" + mode + "> not defined!");
      return false;
  }
  this._editMode = mode;
};
/** Sets the HTML in fullpage mode. Actually the whole iframe document is rewritten.
 * @private
 * @param {String} html
 */
Xinha.prototype.setFullHTML = function(html)
{
  var save_multiline = RegExp.multiline;
  RegExp.multiline = true;
  if ( html.match(Xinha.RE_doctype) )
  {
    this.setDoctype(RegExp.$1);
   // html = html.replace(Xinha.RE_doctype, "");
  }
  RegExp.multiline = save_multiline;
  // disabled to save body attributes see #459
  if ( 0 )
  {
    if ( html.match(Xinha.RE_head) )
    {
      this._doc.getElementsByTagName("head")[0].innerHTML = RegExp.$1;
    }
    if ( html.match(Xinha.RE_body) )
    {
      this._doc.getElementsByTagName("body")[0].innerHTML = RegExp.$1;
    }
  }
  else
  {
    // FIXME - can we do this without rewriting the entire document
    //  does the above not work for IE?
    var reac = this.editorIsActivated();
    if ( reac )
    {
      this.deactivateEditor();
    }
    var html_re = /<html>((.|\n)*?)<\/html>/i;
    html = html.replace(html_re, "$1");
    this._doc.open("text/html","replace");
    this._doc.write(html);
    this._doc.close();
    if ( reac )
    {
      this.activateEditor();
    }        
    this.setEditorEvents();
    return true;
  }
};
/** Initialize some event handlers
 * @private
 */
Xinha.prototype.setEditorEvents = function(resetting_events_for_opera)
{
  var editor=this;
  var doc = this._doc;

  editor.whenDocReady(
    function()
    {
      if(!resetting_events_for_opera) {
      // if we have multiple editors some bug in Mozilla makes some lose editing ability
      if(!Xinha.is_ios)
      {
      Xinha._addEvents(
        doc,
        ["mousedown"],
        function()
        {
          editor.activateEditor();
          return true;
        }
      );
      }
      else
      {         
        Xinha._addEvents(
          editor._statusBar,
          ["click"],
          function()
          {           
            editor.activateEditor();
            editor.focusEditor();
            return true;
          }
        );
      }

      if (Xinha.is_ie)
      { // #1019 Cusor not jumping to editable part of window when clicked in IE, see also #1039
        Xinha._addEvent(
        editor._doc.getElementsByTagName("html")[0],
        "click",
          function()
          {
            if (editor._iframe.contentWindow.event.srcElement.tagName.toLowerCase() == 'html') // if  clicked below the text (=body), the text cursor does not appear, see #1019
            {
               var r = editor._doc.body.createTextRange();
               r.collapse();
               r.select();
               //setTimeout (function () { r.collapse();  r.select();},100); // won't do without timeout, dunno why
             }
             return true;
          }
        );
      }
      }

      // intercept some events; for updating the toolbar & keyboard handlers
      Xinha._addEvents(
        doc,
        ["keydown", "keypress", "mousedown", "mouseup", "drag"],
        function (event)
        {
          return editor._editorEvent(Xinha.is_ie ? editor._iframe.contentWindow.event : event);
        }
      );
      
      Xinha._addEvents(
        doc, 
        ["dblclick"],
        function (event)
        {
          return editor._onDoubleClick(Xinha.is_ie ? editor._iframe.contentWindow.event : event);
        }
      );
      
      if(resetting_events_for_opera) return;

      // FIXME - this needs to be cleaned up and use editor.firePluginEvent
      //  I don't like both onGenerate and onGenerateOnce, we should only
      //  have onGenerate and it should only be called when the editor is 
      //  generated (once and only once)
      // check if any plugins have registered refresh handlers
      for ( var i in editor.plugins )
      {
        var plugin = editor.plugins[i].instance;
        Xinha.refreshPlugin(plugin);
      }

      // specific editor initialization
      if ( typeof editor._onGenerate == "function" )
      {
        editor._onGenerate();
      }
      
      if(Xinha.hasAttribute(editor._textArea, 'onxinhaready'))
      {               
        (function() { eval(editor._textArea.getAttribute('onxinhaready')) }).call(editor.textArea);
      }
      
      //ticket #1407 IE8 fires two resize events on one actual resize, seemingly causing an infinite loop (but not  when Xinha is in an frame/iframe) 
      Xinha.addDom0Event(window, 'resize', function(e) 
      {
        if (Xinha.ie_version > 7 && !window.parent)
        {
          if (editor.execResize)
          {
            editor.sizeEditor(); 
            editor.execResize = false;
          }
          else
          {
            editor.execResize = true;
          }
        }
        else
        {
          editor.sizeEditor(); 
        }
      });      
      
      
      if(typeof editor.config.resizableEditor != 'undefined' && editor.config.resizableEditor)
      {
        editor._iframe.style.resize = 'both';
        var lastResize    = [0,0];
        Xinha._addEvent(editor._iframe.contentWindow, 'resize', function(){
          if(lastResize[0] == editor._iframe.style.width && lastResize[1] == editor._iframe.style.height)
          {
            return;
          }
          
          lastResize = [ editor._iframe.style.width, editor._iframe.style.height ];          
          editor.sizeEditor(editor._iframe.style.width, editor._iframe.style.height, false, false);
        });
      }
      
    
      editor.removeLoadingMessage();
    }
  );
};
  
/***************************************************
 *  Category: PLUGINS
 ***************************************************/
/** Plugins may either reside in the golbal scope (not recommended) or in Xinha.plugins. 
 *  This function looks in both locations and is used to check the loading status and finally retrieve the plugin's constructor
 * @private
 * @type {Function|undefined}
 * @param {String} pluginName
 */
Xinha.getPluginConstructor = function(pluginName)
{
  return Xinha.plugins[pluginName] || window[pluginName];
};

/** Create the specified plugin and register it with this Xinha
 *  return the plugin created to allow refresh when necessary.<br />
 *  <strong>This is only useful if Xinha is generated without using Xinha.makeEditors()</strong>
 */
Xinha.prototype.registerPlugin = function()
{
  if (!Xinha.isSupportedBrowser)
  {
    return;
  }
  var plugin = arguments[0];

  // We can only register plugins that have been succesfully loaded
  if ( plugin === null || typeof plugin == 'undefined' || (typeof plugin == 'string' && Xinha.getPluginConstructor(plugin) == 'undefined') )
  {
    return false;
  }
  var args = [];
  for ( var i = 1; i < arguments.length; ++i )
  {
    args.push(arguments[i]);
  }
  return this.registerPlugin2(plugin, args);
};
/** This is the variant of the function above where the plugin arguments are
 * already packed in an array.  Externally, it should be only used in the
 * full-screen editor code, in order to initialize plugins with the same
 * parameters as in the opener window.
 * @private
 */
Xinha.prototype.registerPlugin2 = function(plugin, args)
{
  if ( typeof plugin == "string" && typeof Xinha.getPluginConstructor(plugin) == 'function' )
  {
    var pluginName = plugin;
    plugin = Xinha.getPluginConstructor(plugin);
  }
  if ( typeof plugin == "undefined" )
  {
    /* FIXME: This should never happen. But why does it do? */
    return false;
  }
  if (!plugin._pluginInfo) 
  {
    plugin._pluginInfo = 
    {
      name: pluginName
    };
  }
  var obj;
  if ( (typeof plugin == 'function') && (obj = new plugin(this, args)) )
  {
    var clone = {};
    var info = plugin._pluginInfo;
    for ( var i in info )
    {
      clone[i] = info[i];
    }
    clone.instance = obj;
    clone.args = args;
    this.plugins[plugin._pluginInfo.name] = clone;
    return obj;
  }
  else
  {
    Xinha.debugMsg("Can't register plugin " + plugin.toString() + ".", 'warn');
  }
};


/** Dynamically returns the directory from which the plugins are loaded<br />
 *  This could be overridden to change the dir<br />
 *  @TODO: Wouldn't this be better as a config option?
 * @private
 * @param {String} pluginName
 * @param {Boolean} return the directory for an unsupported plugin
 * @returns {String} path to plugin
 */
Xinha.getPluginDir = function(plugin, forceUnsupported)
{
  if (Xinha.externalPlugins[plugin])
  {
    return Xinha.externalPlugins[plugin][0];
  }
  
  // This is ued by multiStageLoader when it's trying to find a plugin
  // after it's tried the normal directory, so as long as it's in the pluginManifest
  // then this shouldn't be hit, but just incase we will respect this request
  if (forceUnsupported)
  {
    return _editor_url + "unsupported_plugins/" + plugin ;
  }
  
  // Just in case we fudge the pluginManifest for a given plugin
  // pull the directory from there
  if(typeof Xinha.pluginManifest[plugin] != 'undefined')
  {
    return Xinha.pluginManifest[plugin].url.replace(/\/[a-zA-Z0-9_-]+\.js$/, '');
  }
  
  if (forceUnsupported ||
      // If the plugin is fully loaded, it's supported status is already set.
      (Xinha.getPluginConstructor(plugin) && (typeof Xinha.getPluginConstructor(plugin).supported != 'undefined') && !Xinha.getPluginConstructor(plugin).supported))
  {
    return _editor_url + "unsupported_plugins/" + plugin ;
  }
  return _editor_url + "plugins/" + plugin ;
};
/** Static function that loads the given plugin
 * @param {String} pluginName
 * @param {Function} callback function to be called when file is loaded
 * @param {String} plugin_file URL of the file to load
 * @returns {Boolean} true if plugin loaded, false otherwise
 */
Xinha.loadPlugin = function(pluginName, callback, url)
{
  if (!Xinha.isSupportedBrowser) 
  {
    return;
  }
  Xinha.setLoadingMessage (Xinha._lc("Loading plugin $plugin", 'Xinha', {plugin: pluginName}));

  // Might already be loaded
  if ( typeof Xinha.getPluginConstructor(pluginName) != 'undefined' )
  {
    if ( callback )
    {
      callback(pluginName);
    }
    return true;
  }
  Xinha._pluginLoadStatus[pluginName] = 'loading';

  /** This function will try to load a plugin in multiple passes.  It tries to
   * load the plugin from either the plugin or unsupported directory, using
   * both naming schemes in this order:
   * 1. /plugins -> CurrentNamingScheme
   * 2. /plugins -> old-naming-scheme
   * 3. /unsupported -> CurrentNamingScheme
   * 4. /unsupported -> old-naming-scheme
   */
  function multiStageLoader(stage,pluginName)
  {
    var nextstage, dir, file, success_message;
    switch (stage)
    {
      case 'start':
        nextstage = 'old_naming';
        dir = Xinha.getPluginDir(pluginName);
        file = pluginName + ".js";
        break;
      case 'old_naming':
        nextstage = 'unsupported';
        dir = Xinha.getPluginDir(pluginName);
        file = pluginName.replace(/([a-z])([A-Z])([a-z])/g, function (str, l1, l2, l3) { return l1 + "-" + l2.toLowerCase() + l3; }).toLowerCase() + ".js";
        success_message = 'You are using an obsolete naming scheme for the Xinha plugin '+pluginName+'. Please rename '+file+' to '+pluginName+'.js';
        break;
      case 'unsupported':
        nextstage = 'unsupported_old_name';
        dir = Xinha.getPluginDir(pluginName, true);
        file = pluginName + ".js";
        success_message = 'You are using the unsupported Xinha plugin '+pluginName+'. If you wish continued support, please see http://trac.xinha.org/wiki/Documentation/UnsupportedPlugins';
        break;
      case 'unsupported_old_name':
        nextstage = '';
        dir = Xinha.getPluginDir(pluginName, true);
        file = pluginName.replace(/([a-z])([A-Z])([a-z])/g, function (str, l1, l2, l3) { return l1 + "-" + l2.toLowerCase() + l3; }).toLowerCase() + ".js";
        success_message = 'You are using the unsupported Xinha plugin '+pluginName+'. If you wish continued support, please see http://trac.xinha.org/wiki/Documentation/UnsupportedPlugins';
        break;
      default:
        Xinha._pluginLoadStatus[pluginName] = 'failed';
        Xinha.debugMsg('Xinha was not able to find the plugin '+pluginName+'. Please make sure the plugin exists.', 'warn');
        Xinha.debugMsg('If '+pluginName+' is located in unsupported_plugins you should also check unsupported_plugins/.htaccess for possible access restrictions.', 'warn');
        
        return;
    }
    var url = dir + "/" + file;

    // This is a callback wrapper that allows us to set the plugin's status
    // once it loads.
    function statusCallback(pluginName)
    {
      Xinha.getPluginConstructor(pluginName).supported = stage.indexOf('unsupported') !== 0;
      callback(pluginName);
    }

    // To speed things up, we start loading the script file before pinging it.
    // If the load fails, we'll just clean up afterwards.
    Xinha._loadback(url, statusCallback, this, pluginName); 

    Xinha.ping(url,
               // On success, we'll display a success message if there is one.
               function()
               {
                 if (success_message) 
                 {
                   Xinha.debugMsg(success_message);
                 }
               },
               // On failure, we'll clean up the failed load and try the next stage
               function()
               {
                 Xinha.removeFromParent(document.getElementById(url));
                 multiStageLoader(nextstage, pluginName);
               });
  }
  
  if(!url)
  {
    if (Xinha.externalPlugins[pluginName])
    {
      Xinha._loadback(Xinha.externalPlugins[pluginName][0]+Xinha.externalPlugins[pluginName][1], callback, this, pluginName);
    }
    else if(Xinha.pluginManifest[pluginName])
    {
      Xinha._loadback(Xinha.pluginManifest[pluginName].url, callback, this, pluginName);
    }
    else
    {
      var editor = this;
      multiStageLoader('start',pluginName);
    }
  }
  else
  {
    Xinha._loadback(url, callback, this, pluginName);
  }
  
  return false;
};
/** Stores a status for each loading plugin that may be one of "loading","ready", or "failed"
 * @private
 * @type {Object} 
 */
Xinha._pluginLoadStatus = {};
/** Stores the paths to plugins that are not in the default location
 * @private
 * @type {Object}
 */
Xinha.externalPlugins = {};
/** The namespace for plugins
 * @private
 * @type {Object}
 */
Xinha.plugins = {};

/** Static function that loads the plugins (see xinha_plugins in NewbieGuide)
 * @param {Array} plugins
 * @param {Function} callbackIfNotReady function that is called repeatedly until all files are
 * @param {String} optional url URL of the plugin file; obviously plugins should contain only one item if url is given
 * @returns {Boolean} true if all plugins are loaded, false otherwise
 */
Xinha.loadPlugins = function(plugins, callbackIfNotReady,url)
{
  if (!Xinha.isSupportedBrowser) 
  {
    return;
  }
  //Xinha.setLoadingMessage (Xinha._lc("Loading plugins"));
  var m,i;
  for (i=0;i<plugins.length;i++)
  {
    if (typeof plugins[i] == 'object')
    {
      m = plugins[i].url.match(/(.*)(\/[^\/]*)$/);
      Xinha.externalPlugins[plugins[i].plugin] = [m[1],m[2]];
      plugins[i] = plugins[i].plugin;
    }
  }
  
  // Rip the ones that are loaded and look for ones that have failed
  var retVal = true;
  var nuPlugins = Xinha.cloneObject(plugins);
  for (i=0;i<nuPlugins.length;i++ )
  {
    var p = nuPlugins[i];
    
    if (p == 'FullScreen' && !Xinha.externalPlugins.FullScreen)
    {
      continue; //prevent trying to load FullScreen plugin from the plugins folder
    } 
   
    if ( typeof Xinha._pluginLoadStatus[p] == 'undefined')
    {
      // Load it
      Xinha.loadPlugin(p,
        function(plugin)
        {
          Xinha.setLoadingMessage (Xinha._lc("Finishing"));

          if ( typeof Xinha.getPluginConstructor(plugin) != 'undefined' )
          {
            Xinha._pluginLoadStatus[plugin] = 'ready';
          }
          else
          {
            Xinha._pluginLoadStatus[plugin] = 'failed';
          }
        }, url);
      retVal = false;
    }
    else if ( Xinha._pluginLoadStatus[p] == 'loading')
    {
      retVal = false;
    }
  }
  
  // All done, just return
  if ( retVal )
  {
    return true;
  }

  // Waiting on plugins to load, return false now and come back a bit later
  // if we have to callback
  if ( callbackIfNotReady )
  {
    setTimeout(function() 
    { 
      if ( Xinha.loadPlugins(plugins, callbackIfNotReady) ) 
      { 
        callbackIfNotReady(); 
      } 
    }, 50);
  }
  return retVal;
};

// 
/** Refresh plugin by calling onGenerate or onGenerateOnce method.
 * @private
 * @param {PluginInstance} plugin
 */
Xinha.refreshPlugin = function(plugin)
{
  if ( plugin && typeof plugin.onGenerate == "function" )
  {
    plugin.onGenerate();
  }
  if ( plugin && typeof plugin.onGenerateOnce == "function" )
  {
    //#1392: in fullpage mode this function is called recusively by setFullHTML() when it is used to set the editor content
	// this is a temporary fix, that should better be handled by a better implemetation of setFullHTML
	plugin._ongenerateOnce = plugin.onGenerateOnce;
    delete(plugin.onGenerateOnce);
	plugin._ongenerateOnce();
	delete(plugin._ongenerateOnce);
  }
};

/** Call a method of all plugins which define the method using the supplied arguments.<br /><br />
 *
 *  Example: <code>editor.firePluginEvent('onExecCommand', 'paste')</code><br />
 *           The plugin would then define a method<br />
 *           <code>PluginName.prototype.onExecCommand = function (cmdID, UI, param) {do something...}</code><br /><br />
 *           The following methodNames are currently available:<br />
 *  <table border="1">
 *    <tr>
 *       <th>methodName</th><th>Parameters</th>
 *     </tr>
 *     <tr>
 *       <td>onExecCommand</td><td> cmdID, UI, param</td>
 *     </tr>
 *     <tr>
 *       <td>onKeyPress</td><td>ev</td>
 *     </tr> 
 *     <tr>
 *       <td>onMouseDown</td><td>ev</td>
 *     </tr>
 * </table><br /><br />
 *  
 *  The browser specific plugin (if any) is called last.  The result of each call is 
 *  treated as boolean.  A true return means that the event will stop, no further plugins
 *  will get the event, a false return means the event will continue to fire.
 *
 *  @param {String} methodName
 *  @param {mixed} arguments to pass to the method, optional [2..n] 
 *  @returns {Boolean}
 */

Xinha.prototype.firePluginEvent = function(methodName)
{
  // arguments is not a real array so we can't just .shift() it unfortunatly.
  var argsArray = [ ];
  for(var i = 1; i < arguments.length; i++)
  {
    argsArray[i-1] = arguments[i];
  }
  
  for ( i in this.plugins )
  {
    var plugin = this.plugins[i].instance;
    
    if(i == 'Events')
    {
      // 'Events' is a dummy plugin for events passed into config.Events
      //   so make sure we actually reference that config object in case
      //   it had been overwritten entirely (ticket:1602)
      plugin = this.config.Events;
    }

    // Skip the browser specific plugin
    if (plugin == this._browserSpecificPlugin) 
    {
      continue;
    }
    if ( plugin && typeof plugin[methodName] == "function" )
    {
      var thisArg = (i == 'Events') ? this : plugin;
      if ( plugin[methodName].apply(thisArg, argsArray) )
      {
        return true;
      }
    }
  }
  
  // Now the browser speific
  plugin = this._browserSpecificPlugin;
  if ( plugin && typeof plugin[methodName] == "function" )
  {
    if ( plugin[methodName].apply(plugin, argsArray) )
    {
      return true;
    }
  }
  return false;
};
/** Adds a stylesheet to the document
 * @param {String} style name of the stylesheet file
 * @param {String} plugin optional name of a plugin; if passed this function looks for the stylesheet file in the plugin directory 
 * @param {String} id optional a unique id for identifiing the created link element, e.g. for avoiding double loading 
 *                 or later removing it again
 */
Xinha.loadStyle = function(style, plugin, id,prepend)
{
  var url = _editor_url || '';
  if ( plugin )
  {
    url = Xinha.getPluginDir( plugin ) + "/";
  }
  url += style;
  // @todo: would not it be better to check the first character instead of a regex ?
  // if ( typeof style == 'string' && style.charAt(0) == '/' )
  // {
  //   url = style;
  // }
  if ( /^\//.test(style) )
  {
    url = style;
  }
  var head = document.getElementsByTagName("head")[0];
  var link = document.createElement("link");
  link.rel = "stylesheet";
  link.href = url;
  link.type = "text/css";
  if (id)
  {
    link.id = id;
  }
  if (prepend && head.getElementsByTagName('link')[0])
  {
    head.insertBefore(link,head.getElementsByTagName('link')[0]);
  }
  else
  {
    head.appendChild(link);
  }
  
};

/** Adds a script to the document
 *
 * Warning: Browsers may cause the script to load asynchronously.
 *
 * @param {String} style name of the javascript file
 * @param {String} plugin optional name of a plugin; if passed this function looks for the stylesheet file in the plugin directory 
 *
 */
Xinha.loadScript = function(script, plugin, callback)
{
  var url = _editor_url || '';
  if ( plugin )
  {
    url = Xinha.getPluginDir( plugin ) + "/";
  }
  url += script;
  // @todo: would not it be better to check the first character instead of a regex ?
  // if ( typeof style == 'string' && style.charAt(0) == '/' )
  // {
  //   url = style;
  // }
  if ( /^\//.test(script) )
  {
    url = script;
  }
  
  Xinha._loadback(url, callback);
  
};

/** Load one or more assets, sequentially, where an asset is a CSS file, or a javascript file.
 *  
 * Example Usage:
 *
 * Xinha.includeAssets( 'foo.css', 'bar.js', [ 'foo.css', 'MyPlugin' ], { type: 'text/css', url: 'foo.php', plugin: 'MyPlugin } );
 *
 * Alternative usage, use Xinha.includeAssets() to make a loader, then use loadScript, loadStyle and whenReady methods
 * on your loader object as and when you wish, you can chain the calls if you like.
 *
 * You may add any number of callbacks using .whenReady() multiple times.
 *
 *   var myAssetLoader = Xinha.includeAssets();
 *       myAssetLoader.loadScript('foo.js', 'MyPlugin')
 *                    .loadStyle('foo.css', 'MyPlugin')
 *                    .loadScriptOnce('bar.js', 'MyPlugin')
 *                    .loadScriptIf( true, 'narf.js', 'MyPlugin')
 *                    .loadScriptOnceIf( false, 'zort.js', 'MyPlugin')
 *                    .loadStyleIf( 1 > 0, 'flurp.css', 'MyPlugin')
 *                    .whenReady(function(){ doSomethingCool(); });
 * 
 */

Xinha.includeAssets = function()
{
  var assetLoader = { pendingAssets: [ ], loaderRunning: false, loaderPaused: false, loadedAssets: [ ] };
  
  assetLoader.callbacks = [ ];
  
  assetLoader.isAlreadyLoaded = function(url, plugin)
  {
    for(var i = 0; i < this.loadedAssets.length; i++)
    {
      if(this.loadedAssets[i].url == url && this.loadedAssets[i].plugin == plugin)
      {
        if(!this.loaderRunning) this.loadNext();
        return this; // Already done (or in process)
      }
    }
    
    for(var i = 0; i < this.pendingAssets.length; i++)
    {
      if(this.pendingAssets[i].url == url && this.pendingAssets[i].plugin == plugin)
      {
        if(!this.loaderRunning) this.loadNext();
        return this; // Already pending
      }
    }
    
    return false;
  };
  
  assetLoader.loadNext = function()
  {  
    var self = this;
    this.loaderRunning = true;
    
    if(this.pendingAssets.length)
    {
      if(this.loaderPaused)
      {
        this.loaderRunning = false;
        return;
      }
      var nxt = this.pendingAssets[0];
      this.pendingAssets.splice(0,1); // Remove 1 element
      switch(nxt.type)
      {
        case 'command/abort':
          if((typeof nxt.condition != 'function' && nxt.condition) || (typeof nxt.condition == 'function' && nxt.condition()))
          {
            // Abort is in the context of the chained loader, we might have more things added
            //  in our own list and those should continue to be executed
            nxt.chainLoader.abort();
          }
          else
          {
            nxt.chainLoader.resume();
          }
          return this.loadNext();

        case 'text/css':
          this.loadedAssets.push(nxt);
          Xinha.loadStyle(nxt.url, nxt.plugin);
          return this.loadNext();
        
        case 'text/javascript':          
          this.loadedAssets.push(nxt);
          Xinha.loadScript(nxt.url, nxt.plugin, function() { self.loadNext(); });
          return this;
      }
    }
    else
    {
      this.loaderRunning = false;
      this.runCallback();      
      return this;
    }
  };
  
  assetLoader.loadScriptAlways = function(url, plugin)
  {
    if(!url) return this;
    
    var self = this;
    
    this.pendingAssets.push({ 'type': 'text/javascript', 'url': url, 'plugin': plugin });
    if(!this.loaderRunning) this.loadNext();
    
    return this;
  };
  
  assetLoader.loadScriptOnce = function(url, plugin)
  {
    if(this.isAlreadyLoaded(url, plugin)) 
    {
      return this;
    }
        
    return this.loadScriptAlways(url, plugin);
  }
  
  assetLoader.loadScript = function(url, plugin)
  {
    return this.loadScriptOnce(url, plugin);
  };
  
  assetLoader.loadStyleAlways = function(url, plugin)
  {
    var self = this;
    
    this.pendingAssets.push({ 'type': 'text/css', 'url': url, 'plugin': plugin });
    if(!this.loaderRunning) this.loadNext();
    
    return this;    
  };
  
  assetLoader.loadStyleOnce = function(url, plugin)
  {
    if(this.isAlreadyLoaded(url, plugin)) 
    {
      return this;
    }
        
    return this.loadStyleAlways(url, plugin);
  };
  
  assetLoader.loadStyle = function(url, plugin)
  {
    return this.loadStyleOnce(url, plugin);
  };
  
  assetLoader.whenReady = function(callback) 
  {
    this.callbacks.push(callback);    
    if(!this.loaderRunning) this.loadNext();
    
    return this;    
  };
  
  assetLoader.runCallback = function()
  {
    while(this.callbacks.length)
    { 
      var _callback = this.callbacks.splice(0,1);
      _callback[0]();
      _callback = null;
    }
    return this;
  };
  
  assetLoader.loadScriptIf = function(condition, url, plugin)
  {
    if(condition) this.loadScript(url, plugin);
    return this;
  };
  
  assetLoader.loadScriptOnceIf = function(condition, url, plugin)
  {
    if(condition) this.loadScriptOnce(url, plugin);
    return this;
  };
  
  assetLoader.loadStyleIf = function(condition, url, plugin)
  {
    if(condition) this.loadStyle(url, plugin);
    return this;
  };
    
  assetLoader.abortIf = function(condition)
  {
    // We use a dummy asset to insert, and return a chainable loader which will be started 
    //  only if the condition is met (evaluated after previous assets have been loaded)
    // 
    // Note that this means you can do
    //  
    //  myAssetLoader.loadScript('blah.js')
    //               .abortIf(function(){ return true ; } )
    //               .loadStyle('blah.css');
    //
    //  myAssetLoader.loadScript('foo.js');
    //  
    // and even though the first chain was aborted after loading blah.js and blah.css 
    //   never gets loaded, foo.js still does get loaded because it's in a different 
    //   chain of assets.
    //
    // For a  practical example, see loadLibrary where jQuery is tested after it loads
    //  and if it doesn't then the chained (assuming jQuery) scripts are not loaded.
    //
    var waitAsset = { type: 'command/abort', condition: condition, url: null, plugin: null, chainLoader: Xinha.includeAssets() };
    this.pendingAssets.push( waitAsset );
    waitAsset.chainLoader.pause();
    return waitAsset.chainLoader;
  };
  
  assetLoader.pause = function() 
  {
    this.loaderPaused = true;
  };
  
  assetLoader.resume = function() 
  {
    this.loaderPaused = false;
    if(!this.loaderRunning) this.loadNext();
  };
  
  assetLoader.abort = function()
  {
    this.pendingAssets = [ ];
    this.callbacks     = [ ];
    this.loaderPaused = false;
    return this;
  };
  
  for(var i = 0 ; i < arguments.length; i++)
  {
    if(typeof arguments[i] == 'string')
    {
      if(arguments[i].match(/\.css$/i))
      {
        assetLoader.loadStyle(arguments[i]);
      }
      else 
      {
        assetLoader.loadScript(arguments[i]);
      }
    }
    else if(arguments[i].type)
    {
      if(arguments[i].type.match(/text\/css/i))
      {
        assetLoader.loadStyle(arguments[i].url, arguments[i].plugin);
      }
      else if(arguments[i].type.match(/text\/javascript/i))
      {
        assetLoader.loadScript(arguments[i].url, arguments[i].plugin);
      }
    }
    else if(arguments[i].length >= 1)
    {
      if(arguments[i][0].match(/\.css$/i))
      {
        assetLoader.loadStyle(arguments[i][0], arguments[i][1]);
      }
      else 
      {
        assetLoader.loadScript(arguments[i][0], arguments[i][1]);
      }
    }
  }
  
  return assetLoader;
}

Xinha._libraryAssetLoader = Xinha.includeAssets();
Xinha.loadLibrary = function(libraryName, minVersion, maxVersion)
{
  switch(libraryName.toLowerCase())
  {
    case 'jquery':
      if(typeof jQuery == 'undefined')
      {
        // jQuery can be problematic if it (jQuery itself) fails to load
        //  due to old browsers, so we will return a chained loader which
        //  aborts anything chained on it if jQuery doesn't come into 
        //  existence properly.
        return Xinha._libraryAssetLoader.loadScriptOnce('libraries/jquery-3.3.1.js')
                                        .abortIf(function(){ return typeof jQuery == 'undefined'; });
      }
      break;
      
    case 'mootools':
      if(typeof MooTools == 'undefined')
      {
        Xinha._libraryAssetLoader.loadScriptOnce('libraries/MooTools-Core-1.6.0.js')
                                 .loadScriptOnce('libraries/MooTools-More-1.6.0.js');
      }
      break;
      
    default:
      Xinha.debugMsg("Unknown library "+libraryName+", libraries need to be handled by Xinha.loadLibrary, add code there.", 'warn'); 
      break;
  }
  
  // We return the loader so that the callee can do whenReady() on it
  //  if they want.
  return Xinha._libraryAssetLoader;
}

/***************************************************
 *  Category: EDITOR UTILITIES
 ***************************************************/
/** Utility function: Outputs the structure of the edited document */
Xinha.prototype.debugTree = function()
{
  var ta = document.createElement("textarea");
  ta.style.width = "100%";
  ta.style.height = "20em";
  ta.value = "";
  function debug(indent, str)
  {
    for ( ; --indent >= 0; )
    {
      ta.value += " ";
    }
    ta.value += str + "\n";
  }
  function _dt(root, level)
  {
    var tag = root.tagName.toLowerCase(), i;
    var ns = Xinha.is_ie ? root.scopeName : root.prefix;
    debug(level, "- " + tag + " [" + ns + "]");
    for ( i = root.firstChild; i; i = i.nextSibling )
    {
      if ( i.nodeType == 1 )
      {
        _dt(i, level + 2);
      }
    }
  }
  _dt(this._doc.body, 0);
  document.body.appendChild(ta);
};
/** Extracts the textual content of a given node
 * @param {DomNode} el
 */

Xinha.getInnerText = function(el)
{
  var txt = '', i;
  for ( i = el.firstChild; i; i = i.nextSibling )
  {
    if ( i.nodeType == 3 )
    {
      txt += i.data;
    }
    else if ( i.nodeType == 1 )
    {
      txt += Xinha.getInnerText(i);
    }
  }
  return txt;
};
/** Cleans dirty HTML from MS word; always cleans the whole editor content
 *  @TODO: move this in a separate file
 *  @TODO: turn this into a static function that cleans a given string
 */
Xinha.prototype._wordClean = function()
{
  var editor = this;
  var stats =
  {
    empty_tags : 0,
    cond_comm  : 0,
    mso_elmts  : 0,
    mso_class  : 0,
    mso_style  : 0,
    mso_xmlel  : 0,
    orig_len   : this._doc.body.innerHTML.length,
    T          : new Date().getTime()
  };
  var stats_txt =
  {
    empty_tags : "Empty tags removed: ",
    cond_comm  : "Conditional comments removed",
    mso_elmts  : "MSO invalid elements removed",
    mso_class  : "MSO class names removed: ",
    mso_style  : "MSO inline style removed: ",
    mso_xmlel  : "MSO XML elements stripped: "
  };

  function showStats()
  {
    var txt = "Xinha word cleaner stats: \n\n";
    for ( var i in stats )
    {
      if ( stats_txt[i] )
      {
        txt += stats_txt[i] + stats[i] + "\n";
      }
    }
    txt += "\nInitial document length: " + stats.orig_len + "\n";
    txt += "Final document length: " + editor._doc.body.innerHTML.length + "\n";
    txt += "Clean-up took " + ((new Date().getTime() - stats.T) / 1000) + " seconds";
    alert(txt);
  }

  function clearClass(node)
  {
    var newc = node.className.replace(/(^|\s)mso.*?(\s|$)/ig, ' ');
    if ( newc != node.className )
    {
      node.className = newc;
      if ( !/\S/.test(node.className))
      {
        node.removeAttribute("class");
        ++stats.mso_class;
      }
    }
  }

  function clearStyle(node)
  {
    var declarations = node.style.cssText.split(/\s*;\s*/);
    for ( var i = declarations.length; --i >= 0; )
    {
      if ( /^mso|^tab-stops/i.test(declarations[i]) || /^margin\s*:\s*0..\s+0..\s+0../i.test(declarations[i]) )
      {
        ++stats.mso_style;
        declarations.splice(i, 1);
      }
    }
    node.style.cssText = declarations.join("; ");
  }

  function removeElements(el)
  {
    if (('link' == el.tagName.toLowerCase() &&
        (el.attributes && /File-List|Edit-Time-Data|themeData|colorSchemeMapping/.test(el.attributes.rel.nodeValue))) ||
        /^(style|meta)$/i.test(el.tagName))
    {
      Xinha.removeFromParent(el);
      ++stats.mso_elmts;
      return true;
    }
    return false;
  }

  function checkEmpty(el)
  {
    // @todo : check if this is quicker
    //  if (!['A','SPAN','B','STRONG','I','EM','FONT'].contains(el.tagName) && !el.firstChild)
    if ( /^(a|span|b|strong|i|em|font|div|p)$/i.test(el.tagName) && !el.firstChild)
    {
      Xinha.removeFromParent(el);
      ++stats.empty_tags;
      return true;
    }
    return false;
  }

  function parseTree(root)
  {
    clearClass(root);
    clearStyle(root);
    var next;
    for (var i = root.firstChild; i; i = next )
    {
      next = i.nextSibling;
      if ( i.nodeType == 1 && parseTree(i) )
      {
        if ((Xinha.is_ie && root.scopeName != 'HTML') || (!Xinha.is_ie && /:/.test(i.tagName)))
        {
          // Nowadays, Word spits out tags like '<o:something />'.  Since the
          // document being cleaned might be HTML4 and not XHTML, this tag is
          // interpreted as '<o:something /="/">'.  For HTML tags without
          // closing elements (e.g. IMG) these two forms are equivalent.  Since
          // HTML does not recognize these tags, however, they end up as
          // parents of elements that should be their siblings.  We reparent
          // the children and remove them from the document.
          for (var index=i.childNodes && i.childNodes.length-1; i.childNodes && i.childNodes.length && i.childNodes[index]; --index)
          {
            if (i.nextSibling)
            {
              i.parentNode.insertBefore(i.childNodes[index],i.nextSibling);
            }
            else
            {
              i.parentNode.appendChild(i.childNodes[index]);
            }
          }
          Xinha.removeFromParent(i);
          continue;
        }
        if (checkEmpty(i))
        {
          continue;
        }
        if (removeElements(i))
        {
          continue;
        }
      }
      else if (i.nodeType == 8)
      {
        // 8 is a comment node, and can contain conditional comments, which
        // will be interpreted by IE as if they were not comments.
        if (/(\s*\[\s*if\s*(([gl]te?|!)\s*)?(IE|mso)\s*(\d+(\.\d+)?\s*)?\]>)/.test(i.nodeValue))
        {
          // We strip all conditional comments directly from the tree.
          Xinha.removeFromParent(i);
          ++stats.cond_comm;
        }
      }
    }
    return true;
  }
  parseTree(this._doc.body);
  // showStats();
  // this.debugTree();
  // this.setHTML(this.getHTML());
  // this.setHTML(this.getInnerHTML());
  // this.forceRedraw();
  this.updateToolbar();
};

/** Removes &lt;font&gt; tags; always cleans the whole editor content
 *  @TODO: move this in a separate file
 *  @TODO: turn this into a static function that cleans a given string
 */
Xinha.prototype._clearFonts = function()
{
  var D = this.getInnerHTML();

  if ( confirm(Xinha._lc("Would you like to clear font typefaces?")) )
  {
    D = D.replace(/face="[^"]*"/gi, '');
    D = D.replace(/font-family:[^;}"']+;?/gi, '');
  }

  if ( confirm(Xinha._lc("Would you like to clear font sizes?")) )
  {
    D = D.replace(/size="[^"]*"/gi, '');
    D = D.replace(/font-size:[^;}"']+;?/gi, '');
  }

  if ( confirm(Xinha._lc("Would you like to clear font colours?")) )
  {
    D = D.replace(/color="[^"]*"/gi, '');
    D = D.replace(/([^\-])color:[^;}"']+;?/gi, '$1');
  }

  D = D.replace(/(style|class)="\s*"/gi, '');
  D = D.replace(/<(font|span)\s*>/gi, '');
  this.setHTML(D);
  this.updateToolbar();
};

Xinha.prototype._splitBlock = function()
{
  this._doc.execCommand('formatblock', false, 'div');
};

/** Sometimes the display has to be refreshed to make DOM changes visible (?) (Gecko bug?)  */
Xinha.prototype.forceRedraw = function()
{
  this._doc.body.style.visibility = "hidden";
  this._doc.body.style.visibility = "";
  // this._doc.body.innerHTML = this.getInnerHTML();
};

/** Focuses the iframe window. 
 * @returns {document} a reference to the editor document
 */
Xinha.prototype.focusEditor = function()
{
  switch (this._editMode)
  {
    // notice the try { ... } catch block to avoid some rare exceptions in FireFox
    // (perhaps also in other Gecko browsers). Manual focus by user is required in
    // case of an error. Somebody has an idea?
    case "wysiwyg" :
      try
      {
        // We don't want to focus the field unless at least one field has been activated.
        if ( Xinha._someEditorHasBeenActivated )
        {
          this.activateEditor(); // Ensure *this* editor is activated
          this._iframe.contentWindow.focus(); // and focus it
        }
      } catch (ex) {}
    break;
    case "textmode":
      try
      {
        this._textArea.focus();
      } catch (e) {}
    break;
    default:
      alert("ERROR: mode " + this._editMode + " is not defined");
  }
  return this._doc;
};

/** Takes a snapshot of the current text (for undo)
 * @private
 */
Xinha.prototype._undoTakeSnapshot = function()
{
  ++this._undoPos;
  if ( this._undoPos >= this.config.undoSteps )
  {
    // remove the first element
    this._undoQueue.shift();
    --this._undoPos;
  }
  
  var snapshotData = { 
    'txt'        : null,
    'caretInBody': false
  };
  
  
  // Caret preservation, when you hit undo, ideally put the caret back where it was.
  // To accomplish this we figure out what NODE (Text Node typically) and offset into 
  // it had the caret save this into the parent element of that node if it has one
  // as an attribute, and attach a classname to be able to find it again
  // then in the actual undo function we look for that class name after dropping 
  // the HTML back into place, and reverse the process
  
  // Note that IE <11 does not support this, FF <3.6 won't either, 
  // nor Safari <5.1, Chrome I dont' know it does currently I don't know how long ago
  // Opera same, don't know how long but it works currently
  // If a browser doesn't work with it, it won't cause a problem, it just won't 
  // preserve the caret.
  
  try
  {
    // Insert a marker so we know where we are
    var sel = this.getSelection();
    var caretNode    = sel.focusNode;
    if(caretNode)
    {
      var caretOffset  = sel.focusOffset
      var rng = this.createRange(sel);
      var caretParent   = this.getParentElement(sel);
      
      var caretRestorationData = false;
      
      switch(caretNode.nodeType)
      {
        case 3:  // TEXT
        case 4:  // CDATA
        case 8:  // COMMENT
          // We need to record which child node the focus node is of the parent
          //  default to the end
          var whichChild = caretParent.childNodes.length-1;
          for(var i = 0; i < caretParent.childNodes.length; i++)
          {
            if(caretParent.childNodes[i] == caretNode)
            {
              whichChild = i;
            }
          }
          caretRestorationData = {
            caretChild:   whichChild,
            caretOffset:  caretOffset
          };
          break;
        
        case 1:
          // Ehhhm, not sure.  This would be the case if the selection is an image, table whatever
          //
          // For example <p><img></p> with img selected you get caretNode = img, caretParent = p, 
          // caretOffset = 0 (0th child of the p)
          //
          // I was going to make this handled so it would select the image again, but actually
          // I think it works just fine without this, it feels natural-enough anyway.
          break;
      }
      
      if(caretRestorationData)
      {
        if(caretParent == this._doc.body)
        {
          // Body is tricky because it won't be included in the snapshot or restoration
          // unless fullPage mode is being used, since there is only one body then we
          // can record it in the snapshot data and we know where to put it in undo
          snapshotData.caretInBody            = true;
          snapshotData.caretRestorationData   = JSON.stringify(caretRestorationData);
        }
        else
        {
          // For other elements encode the caret data in the element itself
          // so we can be sure we are looking at the right one when we find it
          Xinha.addClass(caretParent, 'xinha-undo-caret');
          caretParent.setAttribute('xinha-undo-caret', JSON.stringify(caretRestorationData));
        }
      }
      
      // Debug helper
      //console.log({n: caretNode, p: caretOffset, r: rng, e: caretParent});
    }
  }
  catch(e)
  {
    // Browser doesn't support something.  I'm not going to try and support
    // very old browsers for this caret preservation feature
    
    // Old IE doesn't support
    if(Xinha.is_gecko || Xinha.is_webkit)
    {
      Xinha.debugMsg('Caret preservation code for undo snapshot failed. If your browser is modern, developers need to check it out in XinhaCore.js (search for caret preservation).','warn');
    }
  }
  
  // use the faster method (getInnerHTML);
  var take = true;
  snapshotData.txt = this.getInnerHTML();
  
  // Find all carets we might have added (in theory, 0 or 1, but always a possibility of more)
  var existingCarets = Xinha.getElementsByClassName(this._doc.body, 'xinha-undo-caret');
  
  // Remove them all
  for(var i = 0; i < existingCarets.length; i++)
  {
    Xinha.removeClass(existingCarets[i], 'xinha-undo-caret');
    existingCarets[i].removeAttribute('xinha-undo-caret');
  }
    
  if ( this._undoPos > 0 )
  {
    take = (this._undoQueue[this._undoPos - 1].txt != snapshotData.txt);
  }
  if ( take )
  {
    this._undoQueue[this._undoPos] = snapshotData;
  }
  else
  {
    this._undoPos--;
  }
};
/** Custom implementation of undo functionality
 * @private
 */
Xinha.prototype.undo = function()
{
  if ( this._undoPos > 0 )
  {
    var snapshotData = this._undoQueue[--this._undoPos];
    if ( snapshotData.txt )
    {
      this.setHTML(snapshotData.txt);
      this._restoreCaretForUndoRedo(snapshotData);
    }
    else
    {
      ++this._undoPos;
    }
  }
};

/** Custom implementation of redo functionality
 * @private
 */
Xinha.prototype.redo = function()
{
  if ( this._undoPos < this._undoQueue.length - 1 )
  {
    var snapshotData = this._undoQueue[++this._undoPos];
    if ( snapshotData.txt )
    {
      this.setHTML(snapshotData.txt);
      this._restoreCaretForUndoRedo(snapshotData);
    }
    else
    {
      --this._undoPos;
    }
  }
};

/** Used by undo and redo to restore a saved caret position.
 * 
 *  Undo and redo must have already set the html when they call this.
 * 
 * @private
 * @param mixed snapshotData as recorded by _undoTakeSnapshot
 */

Xinha.prototype._restoreCaretForUndoRedo = function(snapshotData)
{
  // Caret restoration
  try
  {
    // If the snapped caret was actually in the body as it's parent
    //  (ie text with no containing element except body)
    // push that data back into the body element so we can treat it as
    // any other element
    if(snapshotData.caretInBody)
    {
      Xinha.addClass(this._doc.body, 'xinha-undo-caret');
      this._doc.body.setAttribute('xinha-undo-caret', snapshotData.caretRestorationData);
    }
    
    // Find the caret data we might have recorded in the html
    var caretParents = Xinha.getElementsByClassName(this._doc.body,'xinha-undo-caret');
    
    // Body itself may be the one
    if(Xinha._hasClass(this._doc.body, 'xinha-undo-caret'))
    {
      caretParents[caretParents.length] = this._doc.body;
    }
            
    // Just in case some bug happened and there was more than one caret saved
    //  we will do them all to clear them, but there should only really be 0 or 1
    for(var i = 0; i < caretParents.length; i++)
    {
      if(caretParents[i].getAttribute('xinha-undo-caret').length)
      {
        var caretRestorationData = JSON.parse(caretParents[i].getAttribute('xinha-undo-caret'));
        
        if(caretParents[i].childNodes.length > caretRestorationData.caretChild)
        {
          var rng = this.createRange();
          rng.setStart(caretParents[i].childNodes[caretRestorationData.caretChild], caretRestorationData.caretOffset);
          rng.collapse(true); // collapse to the start, although end would be ok I think, should be the same
          
          var sel = this.getSelection();
          sel.removeAllRanges();
          sel.addRange(rng);
        }
      }
      
      Xinha.removeClass(caretParents[i], 'xinha-undo-caret');
      caretParents[i].removeAttribute('xinha-undo-caret');
    }
  }
  catch(e)
  {
    // Browser doesn't support something, I'm not going to try and 
    //  implement this on old browsers.
    if(Xinha.is_gecko || Xinha.is_webkit)
    {
      Xinha.debugMsg('Caret restoration code for undo failed. If your browser is modern, developers should check it out in XinhaCore.js (search for caret restoration).','warn');
    }
  }
}

/** Disables (greys out) the buttons of the toolbar
 * @param {Array} except this array contains ids of toolbar objects that will not be disabled
 */
Xinha.prototype.disableToolbar = function(except)
{
  if ( this._timerToolbar )
  {
    clearTimeout(this._timerToolbar);
  }
  if ( typeof except == 'undefined' )
  {
    except = [ ];
  }
  else if ( typeof except != 'object' )
  {
    except = [except];
  }

  for ( var i in this._toolbarObjects )
  {
    var btn = this._toolbarObjects[i];
    if ( except.contains(i) )
    {
      continue;
    }
    // prevent iterating over wrong type
    if ( typeof btn.state != 'function' )
    {
      continue;
    }
    btn.state("enabled", false);
  }
};
/** Enables the toolbar again when disabled by disableToolbar() */
Xinha.prototype.enableToolbar = function()
{
  this.updateToolbar();
};

/** Updates enabled/disable/active state of the toolbar elements, the statusbar and other things
 *  This function is called on every key stroke as well as by a timer on a regular basis.<br />
 *  Plugins have the opportunity to implement a prototype.onUpdateToolbar() method, which will also
 *  be called by this function.
 * @param {Boolean} noStatus private use Exempt updating of statusbar
 */
// FIXME : this function needs to be splitted in more functions.
// It is actually to heavy to be understable and very scary to manipulate
Xinha.prototype.updateToolbar = function(noStatus)
{
  if (this.suspendUpdateToolbar)
  {
    return;
  }
  var doc = this._doc;
  var text = (this._editMode == "textmode");
  var ancestors = null;
  if ( !text )
  {
    ancestors = this.getAllAncestors();
    if ( this.config.statusBar && !noStatus )
    {
      while ( this._statusBarItems.length )
      { 
        var item = this._statusBarItems.pop();
        item.el = null;
        item.editor = null;
        item.onclick = null;
        item.oncontextmenu = null;
        item._xinha_dom0Events.click = null;
        item._xinha_dom0Events.contextmenu = null;
        item = null;
      }

      this._statusBarTree.innerHTML = ' ';
      this._statusBarTree.appendChild(document.createTextNode(Xinha._lc("Path") + ": ")); 
      for ( var i = ancestors.length; --i >= 0; )
      {
        var el = ancestors[i];
        if ( !el )
        {
          // hell knows why we get here; this
          // could be a classic example of why
          // it's good to check for conditions
          // that are impossible to happen ;-)
          continue;
        }
        var a = document.createElement("a");
        a.href = "javascript:void(0);";
        a.el = el;
        a.editor = this;
        this._statusBarItems.push(a);
        Xinha.addDom0Event(
          a,
          'click',
          function() {
            this.blur();
            this.editor.selectNodeContents(this.el);
            this.editor.updateToolbar(true);
            return false;
          }
        );
        Xinha.addDom0Event(
          a,
          'contextmenu',
          function()
          {
            // TODO: add context menu here
            this.blur();
            var info = "Inline style:\n\n";
            info += this.el.style.cssText.split(/;\s*/).join(";\n");
            alert(info);
            return false;
          }
        );
        var txt = el.tagName.toLowerCase();
        switch (txt)
        {
          case 'b':
            txt = 'strong';
          break;
          case 'i':
            txt = 'em';
          break;
          case 'strike':
            txt = 'del';
          break;
        }
        if (typeof el.style != 'undefined')
        {
          a.title = el.style.cssText;
        }
        if ( el.id )
        {
          txt += "#" + el.id;
        }
        if ( el.className )
        {
          txt += "." + el.className;
        }
        a.appendChild(document.createTextNode(txt));
        this._statusBarTree.appendChild(a);
        if ( i !== 0 )
        {
          this._statusBarTree.appendChild(document.createTextNode(String.fromCharCode(0xbb)));
        }
        Xinha.freeLater(a);
      }
    }
  }

  for ( var cmd in this._toolbarObjects )
  {
    var btn = this._toolbarObjects[cmd];
    var inContext = true;
    // prevent iterating over wrong type
    if ( typeof btn.state != 'function' )
    {
      continue;
    }
    if ( btn.context && !text )
    {
      var contexts = typeof btn.context == 'object' ? btn.context : [ btn.context ];
      for(var context_i = 0; context_i < contexts.length; context_i++)
      {
        inContext = false;
        var context = contexts[context_i];
        var attrs = [];
        if ( /(.*)\[(.*?)\]/.test(context) )
        {
          context = RegExp.$1;
          attrs = RegExp.$2.split(",");
        }
        context = context.toLowerCase();
        var match = (context == "*");
        for ( var k = 0; k < ancestors.length; ++k )
        {
          if ( !ancestors[k] )
          {
            // the impossible really happens.
            continue;
          }
          if ( match || ( ancestors[k].tagName.toLowerCase() == context ) )
          {
            inContext = true;
            var contextSplit = null;
            var att = null;
            var comp = null;
            var attVal = null;
            for ( var ka = 0; ka < attrs.length; ++ka )
            {
              contextSplit = attrs[ka].match(/(.*)(==|!=|===|!==|>|>=|<|<=)(.*)/);
              att = contextSplit[1];
              comp = contextSplit[2];
              attVal = contextSplit[3];

              if (!eval(ancestors[k][att] + comp + attVal))
              {
                inContext = false;
                break;
              }
            }
            if ( inContext )
            {
              break;
            }
          }
        }
        
        if(inContext) break;
      }
    }
    btn.state("enabled", (!text || btn.text) && inContext);
    if ( typeof cmd == "function" )
    {
      continue;
    }
    // look-it-up in the custom dropdown boxes
    var dropdown = this.config.customSelects[cmd];
    if ( ( !text || btn.text ) && ( typeof dropdown != "undefined" ) )
    {
      dropdown.refresh(this);
      continue;
    }
    switch (cmd)
    {
      case "fontname":
      case "fontsize":
        if ( !text )
        {
          try
          {
            var value = ("" + doc.queryCommandValue(cmd)).toLowerCase();
            if ( !value )
            {
              btn.element.selectedIndex = 0;
              break;
            }

            // HACK -- retrieve the config option for this
            // combo box.  We rely on the fact that the
            // variable in config has the same name as
            // button name in the toolbar.
            var options = this.config[cmd];
            var selectedValue = null;
            for ( var j in options )
            {
            // FIXME: the following line is scary.
              // console.log(j + ': ' + options[j].replace(/,\s*/g, ', ').substr(0, value.length).toLowerCase() + ' =? ' + value);
              
              if ( ( j.toLowerCase() == value ) || ( options[j].substr(0, value.length).toLowerCase() == value ) )
              {
                selectedValue = options[j];
                break;
              }
              
              // The fontname is troublesome, 'foo,bar' may become 'foo, bar' in the element
              // and 'foo bar, foobar' may become '"foo bar", foobar'
              
              if(cmd == 'fontname')
              {
                var fixedOpt = options[j].replace(/,\s*/g, ', ');
                fixedOpt= fixedOpt.replace(/(^|,\s)([a-z]+(\s[a-z]+)+)(,|$)/, '$1"$2"$4');
                
                // Debugging to work out why things are not matching!
                // console.log(fixedOpt.substr(0, value.length).toLowerCase() + '=?' + value );
                
                if ( ( fixedOpt.substr(0, value.length).toLowerCase() == value ) )
                {
                  selectedValue = options[j];
                  break;
                }
              }
            }
            
            if(selectedValue != null)
            {
              // Find the selected value in the toolbar element
              for(var i = 0; i < this._toolbarObjects[cmd].element.options.length; i++)
              {
                if(this._toolbarObjects[cmd].element.options[i].value == selectedValue)
                {
                  this._toolbarObjects[cmd].element.selectedIndex = i;
                  break;
                }
              }
            }
            else
            {
              this._toolbarObjects[cmd].element.selectedIndex = 0;
            }
          } catch(ex) {}
        }
      break;

      // It's better to search for the format block by tag name from the
      //  current selection upwards, because IE has a tendancy to return
      //  things like 'heading 1' for 'h1', which breaks things if you want
      //  to call your heading blocks 'header 1'.  Stupid MS.
      case "formatblock":
        var blocks = [];
        for ( var indexBlock in this.config.formatblock )
        {
	  var blockname = this.config.formatblock[indexBlock];
          // prevent iterating over wrong type
          if ( typeof blockname  == 'string' )
          {
            blocks[blocks.length] = this.config.formatblockDetector[blockname] || blockname;
          }
        }

        var match = this._getFirstAncestorAndWhy(this.getSelection(), blocks);
        var deepestAncestor = match[0];

        if ( deepestAncestor )
        {
          // Find the selected value in the toolbar element
          deepestAncestor = deepestAncestor.tagName.toLowerCase();
          for(var i = 0; i < this._toolbarObjects[cmd].element.options.length; i++)
          {
            if(this._toolbarObjects[cmd].element.options[i].value == deepestAncestor)
            {
              this._toolbarObjects[cmd].element.selectedIndex = i;
              break;
            }
          }
        }
        else
        {
          btn.element.selectedIndex = 0;
        }
      break;

      case "textindicator":
        if ( !text )
        {
          try
          {
            var style = btn.element.style;
            style.backgroundColor = Xinha._makeColor(doc.queryCommandValue(Xinha.is_ie ? "backcolor" : "hilitecolor"));
            if ( /transparent/i.test(style.backgroundColor) )
            {
              // Mozilla
              style.backgroundColor = Xinha._makeColor(doc.queryCommandValue("backcolor"));
            }
            style.color = Xinha._makeColor(doc.queryCommandValue("forecolor"));
            style.fontFamily = doc.queryCommandValue("fontname");
            style.fontWeight = doc.queryCommandState("bold") ? "bold" : "normal";
            style.fontStyle = doc.queryCommandState("italic") ? "italic" : "normal";
          } catch (ex) {
            // alert(e + "\n\n" + cmd);
          }
        }
      break;

      case "htmlmode":
        btn.state("active", text);
      break;

      case "lefttoright":
      case "righttoleft":
        var eltBlock = this.getParentElement();
        while ( eltBlock && !Xinha.isBlockElement(eltBlock) )
        {
          eltBlock = eltBlock.parentNode;
        }
        if ( eltBlock )
        {
          btn.state("active", (eltBlock.style.direction == ((cmd == "righttoleft") ? "rtl" : "ltr")));
        }
      break;

      default:
        cmd = cmd.replace(/(un)?orderedlist/i, "insert$1orderedlist");
        try
        {
          btn.state("active", (!text && doc.queryCommandState(cmd)));
        } catch (ex) {}
      break;
    }
  }
  // take undo snapshots
  if ( this._customUndo && !this._timerUndo )
  {
    this._undoTakeSnapshot();
    var editor = this;
    this._timerUndo = setTimeout(function() { editor._timerUndo = null; }, this.config.undoTimeout);
  }
  this.firePluginEvent('onUpdateToolbar');
};

/** Returns a editor object referenced by the id or name of the textarea or the textarea node itself
 * For example to retrieve the HTML of an editor made out of the textarea with the id "myTextArea" you would do<br />
 * <code>
 *	 var editor = Xinha.getEditor("myTextArea");
 *   var html = editor.getEditorContent(); 
 * </code>
 * @returns {Xinha|null} 
 * @param {String|DomNode} ref id or name of the textarea or the textarea node itself
 */
Xinha.getEditor = function(ref)
{
  for ( var i = __xinhas.length; i--; )
  {
    var editor = __xinhas[i];
    if ( editor && ( editor._textArea.id == ref || editor._textArea.name == ref || editor._textArea == ref ) )
    {
      return editor;
    }
  }
  return null;
};
/** Sometimes one wants to call a plugin method directly, e.g. from outside the editor.
 * This function returns the respective editor's instance of a plugin.
 * For example you might want to have a button to trigger SaveSubmit's save() method:<br />
 * <code>
 *	 &lt;button type="button" onclick="Xinha.getEditor('myTextArea').getPluginInstance('SaveSubmit').save();return false;"&gt;Save&lt;/button&gt;
 * </code>
 * @returns {PluginObject|null} 
 * @param {String} plugin name of the plugin
 */
Xinha.prototype.getPluginInstance = function (plugin)
{
  if (this.plugins[plugin])
  {
    return this.plugins[plugin].instance;
  }
  else
  {
    return null;
  }
};

/** If the given or current selection comprises or is enclosed by 
 *   one of the given tag names, return the deepest encloser.
 * 
 *  Example
 * 
 *    <ul> <li> [Caret Here] </li> </li>
 * 
 * editor.getElementIsOrEnclosingSelection(['ul','li'])
 * 
 * will return the list element
 * 
 */

Xinha.prototype.getElementIsOrEnclosingSelection = function(types, sel)
{
  if(sel == null) 
  {
    sel = this.getSelection();
  }
  
  var currentElement = this.activeElement(sel) ? this.activeElement(sel) : this.getParentElement(sel);
  
  
  var typeMatched = false;
  if(!(typeof currentElement.tagName == 'undefined'))
  {
    for(var i = 0; i < types.length; i++)
    {
      if(currentElement.tagName.toLowerCase().match(types[i].toLowerCase()))
      {
        typeMatched = true; 
        break;
      }
    }
  }
  
  // It wasn't the selected element, see if there is an ancestor
  if(!typeMatched)
  {
    currentElement = null;
    currentElement = this._getFirstAncestor(sel, types);
  }
  
  return currentElement;
};

/** Returns an array with all the ancestor nodes of the selection or current cursor position.
* @returns {Array}
*/
Xinha.prototype.getAllAncestors = function()
{
  var p = this.getParentElement();
  var a = [];
  while ( p && (p.nodeType == 1) && ( p.tagName.toLowerCase() != 'body' ) )
  {
    a.push(p);
    p = p.parentNode;
  }
  a.push(this._doc.body);
  return a;
};

/** Traverses the DOM upwards and returns the first element that is of one of the specified types
 *  @param {Selection} sel  Selection object as returned by getSelection
 *  @param {Array|String} types Array of matching criteria.  Each criteria is either a string containing the tag name, or a callback used to select the element.
 *  @returns {DomNode|null} 
 */
Xinha.prototype._getFirstAncestor = function(sel, types)
{
  return this._getFirstAncestorAndWhy(sel, types)[0];
};

/** Traverses the DOM upwards and returns the first element that is one of the specified types,
 *  and which (of the specified types) the found element successfully matched.
 *  @param {Selection} sel  Selection object as returned by getSelection
 *  @param {Array|String} types Array of matching criteria.  Each criteria is either a string containing the tag name, or a callback used to select the element.
 *  @returns {Array} The array will look like [{DomNode|null}, {Integer|null}] -- that is, it always contains two elements.  The first element is the element that matched, or null if no match was found. The second is the numerical index that can be used to identify which element of the "types" was responsible for the match.  It will be null if no match was found.  It will also be null if the "types" argument was omitted. 
 */
Xinha.prototype._getFirstAncestorAndWhy = function(sel, types)
{
  var prnt = this.activeElement(sel);
  if ( prnt === null )
  {
    // Hmm, I think Xinha.getParentElement() would do the job better?? - James
    try
    {
      prnt = (Xinha.is_ie ? this.createRange(sel).parentElement() : this.createRange(sel).commonAncestorContainer);
    }
    catch(ex)
    {
      return [null, null];
    }
  }

  if ( typeof types == 'string' )
  {
    types = [types];
  }
  return this._getFirstAncestorForNodeAndWhy(prnt, types);
};
 
Xinha.prototype._getFirstAncestorForNodeAndWhy = function(node, types) {
  var prnt = node.parentNode;

  while ( prnt )
  {
    if ( prnt.nodeType == 1 )
    {
      if ( types === null )
      {
	return [prnt, null];
      }
      for (var index=0; index<types.length; ++index) {
        if (typeof types[index] == 'string' && types[index] == prnt.tagName.toLowerCase()){
          // Criteria is a tag name.  It matches
	  return [prnt, index];
      }
        else if (typeof types[index] == 'function' && types[index](this, prnt)) {
          // Criteria is a callback.  It matches
	  return [prnt, index];
        }
      }

      if ( prnt.tagName.toLowerCase() == 'body' )
      {
        break;
      }
      if ( prnt.tagName.toLowerCase() == 'table' )
      {
        break;
      }
    }
    prnt = prnt.parentNode;
  }

  return [null, null];
};

/** Traverses the DOM upwards and returns the first element that is a block level element
 *  @param {Selection} sel  Selection object as returned by getSelection
 *  @returns {DomNode|null} 
 */
Xinha.prototype._getAncestorBlock = function(sel)
{
  // Scan upwards to find a block level element that we can change or apply to
  var prnt = (Xinha.is_ie ? this.createRange(sel).parentElement : this.createRange(sel).commonAncestorContainer);

  while ( prnt && ( prnt.nodeType == 1 ) )
  {
    switch ( prnt.tagName.toLowerCase() )
    {
      case 'div':
      case 'p':
      case 'address':
      case 'blockquote':
      case 'center':
      case 'del':
      case 'ins':
      case 'pre':
      case 'h1':
      case 'h2':
      case 'h3':
      case 'h4':
      case 'h5':
      case 'h6':
      case 'h7':
        // Block Element
        return prnt;

      case 'body':
      case 'noframes':
      case 'dd':
      case 'li':
      case 'th':
      case 'td':
      case 'noscript' :
        // Halting element (stop searching)
        return null;

      default:
        // Keep lookin
        break;
    }
  }

  return null;
};

/** What's this? does nothing, has to be removed
 * 
 * @deprecated
 */
Xinha.prototype._createImplicitBlock = function(type)
{
  // expand it until we reach a block element in either direction
  // then wrap the selection in a block and return
  var sel = this.getSelection();
  if ( Xinha.is_ie )
  {
    sel.empty();
  }
  else
  {
    sel.collapseToStart();
  }

  var rng = this.createRange(sel);

  // Expand UP

  // Expand DN
};



/**
 *  Call this function to surround the existing HTML code in the selection with
 *  your tags.  FIXME: buggy! Don't use this 
 * @todo: when will it be deprecated ? Can it be removed already ?
 * @private (tagged private to not further promote use of this function)
 * @deprecated
 */
Xinha.prototype.surroundHTML = function(startTag, endTag)
{
  var html = this.getSelectedHTML();
  // the following also deletes the selection
  this.insertHTML(startTag + html + endTag);
};

/** Return true if we have some selection
 *  @returns {Boolean} 
 */
Xinha.prototype.hasSelectedText = function()
{
  // FIXME: come _on_ mishoo, you can do better than this ;-)
  return this.getSelectedHTML() !== '';
};

/***************************************************
 *  Category: EVENT HANDLERS
 ***************************************************/

/** onChange handler for dropdowns in toolbar 
 *  @private
 *  @param {DomNode} el Reference to the SELECT object
 *  @param {String} txt  The name of the select field, as in config.toolbar
 *  @returns {DomNode|null} 
 */
Xinha.prototype._comboSelected = function(el, txt)
{
  this.focusEditor();
  var value = el.options[el.selectedIndex].value;
  switch (txt)
  {
    case "fontname":
    case "fontsize":
      this.execCommand(txt, false, value);
    break;
    case "formatblock":
      // Mozilla inserts an empty tag (<>) if no parameter is passed  
      if ( !value )
      {
      	this.updateToolbar();
      	break;
      }
      if( !Xinha.is_gecko || value !== 'blockquote' )
      {
        value = "<" + value + ">";
      }
      this.execCommand(txt, false, value);
    break;
    default:
      // try to look it up in the registered dropdowns
      var dropdown = this.config.customSelects[txt];
      if ( typeof dropdown != "undefined" )
      {
        dropdown.action(this, value, el, txt);
      }
      else
      {
        alert("FIXME: combo box " + txt + " not implemented");
      }
    break;
  }
};

/** Open a popup to select the hilitecolor or forecolor
 * @private
 * @param {String} cmdID The commande ID (hilitecolor or forecolor)
 */
Xinha.prototype._colorSelector = function(cmdID)
{
  var editor = this;	// for nested functions

  // backcolor only works with useCSS/styleWithCSS (see mozilla bug #279330 & Midas doc)
  // and its also nicer as <font>
  if ( Xinha.is_gecko )
  {
    try
    {
     editor._doc.execCommand('useCSS', false, false); // useCSS deprecated & replaced by styleWithCSS 
     editor._doc.execCommand('styleWithCSS', false, true); 

    } catch (ex) {}
  }
  
  var btn = editor._toolbarObjects[cmdID].element;
  var initcolor;
  if ( cmdID == 'hilitecolor' )
  {
    if ( Xinha.is_ie )
    {
      cmdID = 'backcolor';
      initcolor = Xinha._colorToRgb(editor._doc.queryCommandValue("backcolor"));
    }
    else
    {
      initcolor = Xinha._colorToRgb(editor._doc.queryCommandValue("hilitecolor"));
    }
  }
  else
  {
  	initcolor = Xinha._colorToRgb(editor._doc.queryCommandValue("forecolor"));
  }
  var cback = function(color) { editor._doc.execCommand(cmdID, false, color); };
  if ( Xinha.is_ie )
  {
    var range = editor.createRange(editor.getSelection());
    cback = function(color)
    {
      range.select();
      editor._doc.execCommand(cmdID, false, color);
    };
  }
  var picker = new Xinha.colorPicker(
  {
  	cellsize:editor.config.colorPickerCellSize,
  	callback:cback,
  	granularity:editor.config.colorPickerGranularity,
  	websafe:editor.config.colorPickerWebSafe,
  	savecolors:editor.config.colorPickerSaveColors
  });
  picker.open(editor.config.colorPickerPosition, btn, initcolor);
};

/** This is a wrapper for the browser's execCommand function that handles things like 
 *  formatting, inserting elements, etc.<br />
 *  It intercepts some commands and replaces them with our own implementation.<br />
 *  It provides a hook for the "firePluginEvent" system ("onExecCommand").<br /><br />
 *  For reference see:<br />
 *     <a href="http://www.mozilla.org/editor/midas-spec.html">Mozilla implementation</a><br />
 *     <a href="http://msdn.microsoft.com/workshop/author/dhtml/reference/methods/execcommand.asp">MS implementation</a>
 *
 *  @see Xinha#firePluginEvent
 *  @param {String} cmdID command to be executed as defined in the browsers implemantations or Xinha custom
 *  @param {Boolean} UI for compatibility with the execCommand syntax; false in most (all) cases
 *  @param {Mixed} param Some commands require parameters
 *  @returns {Boolean} always false 
 */
Xinha.prototype.execCommand = function(cmdID, UI, param)
{
  var editor = this;	// for nested functions
  this.focusEditor();
  cmdID = cmdID.toLowerCase();
  
  // See if any plugins want to do something special
  if(this.firePluginEvent('onExecCommand', cmdID, UI, param))
  {
    this.updateToolbar();
    return false;
  }

  switch (cmdID)
  {
    case "htmlmode":
      this.setMode();
    break;

    case "hilitecolor":
    case "forecolor":
      this._colorSelector(cmdID);
    break;

    case "createlink":
      this._createLink();
    break;

    case "undo":
    case "redo":
      if (this._customUndo)
      {
        this[cmdID]();
      }
      else
      {
        this._doc.execCommand(cmdID, UI, param);
      }
    break;

    case "inserttable":
      this._insertTable();
    break;

    case "insertimage":
      this._insertImage();
    break;

    case "showhelp":
      this._popupDialog(editor.config.URIs.help, null, this);
    break;

    case "killword":
      this._wordClean();
    break;

    case "cut":
    case "copy":
    case "paste":
      this._doc.execCommand(cmdID, UI, param);
      if ( this.config.killWordOnPaste )
      {
        this._wordClean();
      }
    break;
    case "lefttoright":
    case "righttoleft":
      if (this.config.changeJustifyWithDirection) 
      {
        this._doc.execCommand((cmdID == "righttoleft") ? "justifyright" : "justifyleft", UI, param);
      }
      var dir = (cmdID == "righttoleft") ? "rtl" : "ltr";
      var el = this.getParentElement();
      while ( el && !Xinha.isBlockElement(el) )
      {
        el = el.parentNode;
      }
      if ( el )
      {
        if ( el.style.direction == dir )
        {
          el.style.direction = "";
        }
        else
        {
          el.style.direction = dir;
        }
      }
    break;
    
    case 'justifyleft'  :
    case 'justifyright' :
      cmdID.match(/^justify(.*)$/);
      var ae = this.activeElement(this.getSelection());      
      if(ae && ae.tagName.toLowerCase() == 'img')
      {
        ae.align = ae.align == RegExp.$1 ? '' : RegExp.$1;
      }
      else
      {
        this._doc.execCommand(cmdID, UI, param);
      }
    break;
    
    default:
      try
      {
        this._doc.execCommand(cmdID, UI, param);
      }
      catch(ex)
      {
        if ( this.config.debug )
        {
          alert(ex + "\n\nby execCommand(" + cmdID + ");");
        }
      }
    break;
  }

  this.updateToolbar();
  return false;
};

/** A generic event handler for things that happen in the IFRAME's document.<br />
 *  It provides two hooks for the "firePluginEvent" system:<br />
 *   "onKeyPress"<br />
 *   "onMouseDown"
 *  @see Xinha#firePluginEvent
 *  @param {Event} ev
 */
Xinha.prototype._editorEvent = function(ev)
{
  var editor = this;

  //call events of textarea
  if ( typeof editor._textArea['on'+ev.type] == "function" )
  {
    editor._textArea['on'+ev.type](ev);
  }
  

  var isShortCut = this.isShortCut(ev);
  
  // Order of priority is something like...
  // Keydown Cancels
  //   KeyPress Cancels
  //      ShortCut Cancels 
  //         (built in shortcuts)
  //         KeyUp
  
  if(ev.type == 'keydown')
  {
    if(editor.firePluginEvent('onKeyDown', ev))
    {
      return false;
    }
    
    // If this is a shortcut, fire it as a keypress but when the key down
    //  happens, this is because some browsers (various versions and platform combinations even!) 
    //  fire press and some do not, so we can only rely on keydown here
    //  we will suppress an actual shortcut press a bit further down to avoid duplicates
    if(isShortCut)
    {
      if(editor.firePluginEvent('onKeyPress', ev))
      {
        return false;
      }
      
      // Also we have a special onShortCut which only fires for them
      if(editor.firePluginEvent('onShortCut', ev, isShortCut))
      {
        return false;
      }
      
      this._shortCuts(ev);
    }
  }
  
  // Shortcuts were fired as a keydown masquerading as a keypress already
  // so don't fire them again on an actual keypress.  Additionally
  // browsers differ slightly in what they call a keypress, to be sure we 
  // issue keyress on a standard set of things we pass off to isKeyPressEvent
  // to do some more indepth checking, it will a boolean, some keypress will
  // be invalid and skiped, some keydown will be repeated as a keypress because
  // that browser wouldn't normally issue a keypress.  Sigh.
  if(!isShortCut && this.isKeyPressEvent(ev))
  {
    if(editor.firePluginEvent('onKeyPress', ev))
    {
      return false;
    }
  }
  
  // At last, something we can rely on!
  if(ev.type == 'keyup')
  {
    if(editor.firePluginEvent('onKeyUp', ev))
    {
      return false;
    }
  }

  if ( ev.type == 'mousedown' )
  {
    if(editor.firePluginEvent('onMouseDown', ev))
    {
      return false;
    }
  }

  if ( ev.type == 'mouseup' )
  {
    if(editor.firePluginEvent('onMouseUp', ev))
    {
      return false;
    }
  }
  
  // update the toolbar state after some time
  if ( editor._timerToolbar )
  {
    clearTimeout(editor._timerToolbar);
  }
  if (!this.suspendUpdateToolbar)
  {
  editor._timerToolbar = setTimeout(
    function()
    {
      editor.updateToolbar();
      editor._timerToolbar = null;
    },
    250);
  }
};

/** Handle double click events.
 *  See dblclickList in the config.
 */
 
Xinha.prototype._onDoubleClick = function(ev)
{
  var editor=this;
  var target = Xinha.is_ie ? ev.srcElement : ev.target;
  var tag = target.tagName;
  var className = target.className;
  if (tag) {
    tag = tag.toLowerCase();
    if (className && (this.config.dblclickList[tag+"."+className] != undefined))
      this.config.dblclickList[tag+"."+className][0](editor, target);
    else if (this.config.dblclickList[tag] != undefined)
      this.config.dblclickList[tag][0](editor, target);
  };
};

/** Handles ctrl + key shortcuts 
 *  @TODO: make this mor flexible
 *  @private
 *  @param {Event} ev
 */
Xinha.prototype._shortCuts = function (ev)
{
  var key = this.getKey(ev).toLowerCase();
  var cmd = null;
  var value = null;
  switch (key)
  {
    // simple key commands follow

    case 'b': cmd = "bold"; break;
    case 'i': cmd = "italic"; break;
    case 'u': cmd = "underline"; break;
    case 's': cmd = "strikethrough"; break;
    case 'l': cmd = "justifyleft"; break;
    case 'e': cmd = "justifycenter"; break;
    case 'r': cmd = "justifyright"; break;
    case 'j': cmd = "justifyfull"; break;
    case 'z': cmd = "undo"; break;
    case 'y': cmd = "redo"; break;
    case 'v': cmd = "paste"; break;
    case 'n':
    cmd = "formatblock";
    value = "p";
    break;

    case '0': cmd = "killword"; break;

    // headings
    case '1':
    case '2':
    case '3':
    case '4':
    case '5':
    case '6':
    cmd = "formatblock";
    value = "h" + key;
    break;
  }
  if ( cmd )
  {
    // execute simple command
    this.execCommand(cmd, false, value);
    Xinha._stopEvent(ev);
  }
};
/** Changes the type of a given node
 *  @param {DomNode} el The element to convert
 *  @param {String} newTagName The type the element will be converted to
 *  @returns {DomNode} A reference to the new element
 */
Xinha.prototype.convertNode = function(el, newTagName)
{
  var newel = this._doc.createElement(newTagName);
  while ( el.firstChild )
  {
    newel.appendChild(el.firstChild);
  }
  return newel;
};

/** Scrolls the editor iframe to a given element or to the cursor
 *  @param {DomNode} e optional The element to scroll to; if ommitted, element the element the cursor is in
 */
Xinha.prototype.scrollToElement = function(e)
{
  if(!e)
  {
    e = this.getParentElement();
    if(!e)
    {
      return;
    }
  }
  
  // This was at one time limited to Gecko only, but I see no reason for it to be. - James
  var position = Xinha.getElementTopLeft(e);  
  this._iframe.contentWindow.scrollTo(position.left, position.top);
};

/** Scroll the viewport so that the given element is viewable
 *  if it is not already so.
 */

Xinha.prototype.scrollElementIntoViewport = function(e)
{
  if(!e)
  {
    e = this.getParentElement();
    if(!e)
    {
      return;
    }
  }
  
  // This was at one time limited to Gecko only, but I see no reason for it to be. - James
  var position = Xinha.getElementTopLeft(e);  
  var currentScroll = this.scrollPos(this._iframe.contentWindow);
  var currentSize   = Xinha.viewportSize(this._iframe.contentWindow);
  
  var canSeeX = [currentScroll.x, currentScroll.x + (currentSize.x*0.9)];
  var canSeeY = [currentScroll.y, currentScroll.y + (currentSize.y*0.9)];
  if(   canSeeX[0] <= position.left
    &&  canSeeX[1] >= position.left
    &&  canSeeY[0] <= position.top
    &&  canSeeY[1] >= position.top
  )
  {
    // The item is in the viewport, no scroll
    return;
  }
  
  this._iframe.contentWindow.scrollTo(position.left, Math.min(position.top,position.top-(currentSize.y*0.1)));
};

/** Get the edited HTML
 *  
 *  @public
 *  @returns {String} HTML content
 */
Xinha.prototype.getEditorContent = function()
{
  return this.outwardHtml(this.getHTML());
};

/** Completely change the HTML inside the editor
 *
 *  @public
 *  @param {String} html new content
 */
Xinha.prototype.setEditorContent = function(html)
{
  this.setHTML(this.inwardHtml(html));
};
/** Saves the contents of all Xinhas to their respective textareas
 *  @public 
 */
Xinha.updateTextareas = function()
{
  var e;
  for (var i=0;i<__xinhas.length;i++)
  {
    e = __xinhas[i];
    e._textArea.value = e.getEditorContent();
  }
}
/** Get the raw edited HTML, should not be used without Xinha.prototype.outwardHtml()
 *  
 *  @private
 *  @returns {String} HTML content
 */
Xinha.prototype.getHTML = function()
{
  var html = '';
  switch ( this._editMode )
  {
    case "wysiwyg":
      if ( !this.config.fullPage )
      {
        html = Xinha.getHTML(this._doc.body, false, this).trim();
      }
      else
      {
        html = this.doctype + "\n" + Xinha.getHTML(this._doc.documentElement, true, this);
      }
    break;
    case "textmode":
      html = this._textArea.value;
    break;
    default:
      alert("Mode <" + this._editMode + "> not defined!");
      return false;
  }
  return html;
};

/** Performs various transformations of the HTML used internally, complement to Xinha.prototype.inwardHtml()  
 *  Plugins can provide their own, additional transformations by defining a plugin.prototype.outwardHtml() implematation,
 *  which is called by this function
 *
 *  @private
 *  @see Xinha#inwardHtml
 *  @param {String} html
 *  @returns {String} HTML content
 */
Xinha.prototype.outwardHtml = function(html)
{
  for ( var i in this.plugins )
  {
    var plugin = this.plugins[i].instance;    
    if ( plugin && typeof plugin.outwardHtml == "function" )
    {
      html = plugin.outwardHtml(html);
    }
  }
  
  html = html.replace(/<(\/?)b(\s|>|\/)/ig, "<$1strong$2");
  html = html.replace(/<(\/?)i(\s|>|\/)/ig, "<$1em$2");
  html = html.replace(/<(\/?)strike(\s|>|\/)/ig, "<$1del$2");
  
  // remove disabling of inline event handle inside Xinha iframe
  html = html.replace(/(<[^>]*on(click|mouse(over|out|up|down))=['"])if\(window\.parent &amp;&amp; window\.parent\.Xinha\)\{return false\}/gi,'$1');

  // Figure out what our server name is, and how it's referenced
  var serverBase = location.href.replace(/(https?:\/\/[^\/]*)\/.*/, '$1') + '/';

  // IE puts this in can't figure out why
  //  leaving this in the core instead of InternetExplorer 
  //  because it might be something we are doing so could present itself
  //  in other browsers - James 
  html = html.replace(/https?:\/\/null\//g, serverBase);

  // Make semi-absolute links to be truely absolute
  //  we do this just to standardize so that special replacements knows what
  //  to expect
  html = html.replace(/((href|src|background)=[\'\"])\/+/ig, '$1' + serverBase);

  html = this.outwardSpecialReplacements(html);

  html = this.fixRelativeLinks(html);

  if ( this.config.sevenBitClean )
  {
    html = html.replace(/[^ -~\r\n\t]/g, function(c) { return (c != Xinha.cc) ? '&#'+c.charCodeAt(0)+';' : c; });
  }

  //prevent execution of JavaScript (Ticket #685)
  html = html.replace(/(<script[^>]*((type=[\"\']text\/)|(language=[\"\'])))(freezescript)/gi,"$1javascript");

  // If in fullPage mode, strip the coreCSS
  if(this.config.fullPage)
  {
    html = Xinha.stripCoreCSS(html);
  }

  if (typeof this.config.outwardHtml == 'function' )
  {
    html = this.config.outwardHtml(html);
  }

  return html;
};

/** Performs various transformations of the HTML to be edited 
 *  Plugins can provide their own, additional transformations by defining a plugin.prototype.inwardHtml() implematation,
 *  which is called by this function
 *  
 *  @private
 *  @see Xinha#outwardHtml
 *  @param {String} html  
 *  @returns {String} transformed HTML
 */
Xinha.prototype.inwardHtml = function(html)
{  
  for ( var i in this.plugins )
  {
    var plugin = this.plugins[i].instance;    
    if ( plugin && typeof plugin.inwardHtml == "function" )
    {
      html = plugin.inwardHtml(html);
    }    
  }
    
  // Both IE and Gecko use strike instead of del (#523)
  html = html.replace(/<(\/?)del(\s|>|\/)/ig, "<$1strike$2");

  // disable inline event handle inside Xinha iframe but only if they are not empty attributes
  html = html.replace(/(<[^>]*on(click|mouse(over|out|up|down))=')([^']+')/gi,'$1if(window.parent &amp;&amp; window.parent.Xinha){return false}$4');
  html = html.replace(/(<[^>]*on(click|mouse(over|out|up|down))=")([^"]+")/gi,'$1if(window.parent &amp;&amp; window.parent.Xinha){return false}$4');
  
  html = this.inwardSpecialReplacements(html);

  html = html.replace(/(<script)(?![^>]*((type=[\"\']text\/)|(language=[\"\']))javascript[\"\'])/gi,'$1 type="text/javascript"');
  html = html.replace(/(<script[^>]*((type=[\"\']text\/)|(language=[\"\'])))(javascript)/gi,"$1freezescript");

  // For IE's sake, make any URLs that are semi-absolute (="/....") to be
  // truely absolute
  var nullRE = new RegExp('((href|src|background)=[\'"])/+', 'gi');
  html = html.replace(nullRE, '$1' + location.href.replace(/(https?:\/\/[^\/]*)\/.*/, '$1') + '/');

  html = this.fixRelativeLinks(html);
  
  // If in fullPage mode, add the coreCSS
  if(this.config.fullPage)
  {
    html = Xinha.addCoreCSS(html);
  }

  if (typeof this.config.inwardHtml == 'function' )
  {
    html = this.config.inwardHtml(html);
  }

  return html;
};
/** Apply the replacements defined in Xinha.Config.specialReplacements
 *  
 *  @private
 *  @see Xinha#inwardSpecialReplacements
 *  @param {String} html
 *  @returns {String}  transformed HTML
 */
Xinha.prototype.outwardSpecialReplacements = function(html)
{
  for ( var i in this.config.specialReplacements )
  {
    var from = this.config.specialReplacements[i];
    var to   = i; // why are declaring a new variable here ? Seems to be better to just do : for (var to in config)
    // prevent iterating over wrong type
    if ( typeof from.replace != 'function' || typeof to.replace != 'function' )
    {
      continue;
    } 
    // alert('out : ' + from + '=>' + to);
    var reg = new RegExp(Xinha.escapeStringForRegExp(from), 'g');
    html = html.replace(reg, to.replace(/\$/g, '$$$$'));
    //html = html.replace(from, to);
  }
  return html;
};
/** Apply the replacements defined in Xinha.Config.specialReplacements
 *  
 *  @private
 *  @see Xinha#outwardSpecialReplacements
 *  @param {String} html
 *  @returns {String}  transformed HTML
 */
Xinha.prototype.inwardSpecialReplacements = function(html)
{
  // alert("inward");
  for ( var i in this.config.specialReplacements )
  {
    var from = i; // why are declaring a new variable here ? Seems to be better to just do : for (var from in config)
    var to   = this.config.specialReplacements[i];
    // prevent iterating over wrong type
    if ( typeof from.replace != 'function' || typeof to.replace != 'function' )
    {
      continue;
    }
    // alert('in : ' + from + '=>' + to);
    //
    // html = html.replace(reg, to);
    // html = html.replace(from, to);
    var reg = new RegExp(Xinha.escapeStringForRegExp(from), 'g');
    html = html.replace(reg, to.replace(/\$/g, '$$$$')); // IE uses doubled dollar signs to escape backrefs, also beware that IE also implements $& $_ and $' like perl.
  }
  return html;
};
/** Transforms the paths in src & href attributes
 *  
 *  @private
 *  @see Xinha.Config#expandRelativeUrl
 *  @see Xinha.Config#stripSelfNamedAnchors
 *  @see Xinha.Config#stripBaseHref
 *  @see Xinha.Config#baseHref
 *  @param {String} html 
 *  @returns {String} transformed HTML
 */
Xinha.prototype.fixRelativeLinks = function(html)
{

  if ( typeof this.config.stripSelfNamedAnchors != 'undefined' && this.config.stripSelfNamedAnchors )
  {
    var stripRe = new RegExp("((href|src|background)=\")("+Xinha.escapeStringForRegExp(window.unescape(document.location.href.replace(/&/g,'&amp;'))) + ')([#?][^\'" ]*)', 'g');
    html = html.replace(stripRe, '$1$4');
  }

  if ( typeof this.config.stripBaseHref != 'undefined' && this.config.stripBaseHref )
  {
    var baseRe = null;
    if ( typeof this.config.baseHref != 'undefined' && this.config.baseHref !== null )
    {
      baseRe = new RegExp( "((href|src|background|action)=\")(" + Xinha.escapeStringForRegExp(this.config.baseHref.replace(/([^\/]\/)(?=.+\.)[^\/]*$/, "$1")) + ")", 'g' );
	  html = html.replace(baseRe, '$1');
    }
    baseRe = new RegExp( "((href|src|background|action)=\")(" +  Xinha.escapeStringForRegExp(document.location.href.replace( /^(https?:\/\/[^\/]*)(.*)/, '$1' )) + ")", 'g' );
    html = html.replace(baseRe, '$1');
  }

  // Rewrite 2018 expandRelativeUrl
  //  note that this must be done AFTER stripBaseHref otherwise we can get
  //  absolute urls, which won't be expanded
  if ( typeof this.config.expandRelativeUrl != 'undefined' && this.config.expandRelativeUrl ) 
  {
    if(html == null) return '';
    
    // These are the regexes for matching the urls globally
    var url_regexp_global = [ 
     /(src|href|background|action)(=")([^"]+?)((?:(?:#|\?).*)?")/gi,
     /(src|href|background|action)(=')([^']+?)((?:(?:#|\?).*)?')/gi
    ];
    
    // And singly (you can not change a regex flag after creation)
    var url_regexp_single = [ 
     /(src|href|background|action)(=")([^"]+?)((?:(?:#|\?).*)?")/i,
     /(src|href|background|action)(=')([^']+?)((?:(?:#|\?).*)?')/i
    ];
    
    // Find a list of urls in the document to inspect
    //  we want urls that include a relative component
    //
    //  that is ./,  ../, or start with a basename
    //   (dirname or filename)
    //
    //  excluded are urls with a protocol (http://...)
    //               urls with a same-protocol (//...)
    //
    
    var candidates = [ ];
    
    for(var i = 0; i < url_regexp_global.length ; i++)
    {

            
      var all_urls   =  html.match(url_regexp_global[i]);
      if(!all_urls) continue;
      
      for(var j = 0; j < all_urls.length; j++)
      {
        if(all_urls[j].match(/=["']([a-z]+:)?\/\//)) continue; // Exclude: '//...' and 'https://'
        if(! (this.config.expandRelativeUrl == 'all'))
        {
          // If there is no parental or self-referential path component
          if(all_urls[j].match(/=["'][a-z0-9_-]/i))    continue; // Exclude: 'relativedir/foo.html'
        }
        if(
             ( !all_urls[j].match(/=["']\//)       ) // Starts with something other than a slash
          || ( all_urls[j].match(/=["']\.{1,2}\//) ) // Starts with a ./ or ../
          || ( all_urls[j].match(/\/\.{1,2}\//   ) ) // Includes a ./ or ../
          || ( all_urls[j].match(/\/{2,}/   )      ) // Includes repeated /, we will clean these up too
        )
        {
          // We have to run the match again to get the parts
          candidates[candidates.length] =  all_urls[j].match(url_regexp_single[i]);
        }
      }
    }
          
    // Get our PATH for the page that is doing the editing something like /foo/bar/xinha.php
    var ourpath = document.location.pathname;
    
    // Remove our basename --> /foo/bar/
    ourpath = ourpath.replace(/\/[^/]+$/, '/');
    
    // For each of the candidate urls, fix them
    for(var i = 0; i < candidates.length; i++)
    {
      var src = candidates[i];
      
      var lastHtml = html;
      
      // Add the relative url to our path if it is not semi-absolute
      var fixedUrl = (!src[3].match(/^\//)) ? ourpath + src[3] : src[3];
      
      // Remove any /./ components
      fixedUrl = fixedUrl.replace(/\/\.\//g, '/');
      
      // Reduce any multiple slash
      fixedUrl = fixedUrl.replace(/\/{2,}/g, '/');
      
      // Remove any /[something]/../ components
      //  (this makes /foo/../bar become /bar correctly
      //  repeat this until either there are no more /../
      //  or there has been no further change
      var lastFixedUrl;
      do
      {
        lastFixedUrl = fixedUrl;
        fixedUrl = fixedUrl.replace(/\/[^/]+\/\.\.\//g, '/');          
      } while( lastFixedUrl != fixedUrl );
      
      // Now if we have any /../ left, they should be invalid, so kill those
      fixedUrl = fixedUrl.replace('/../', '/');
      
      // And that's all
      html = html.replace( src[0], src[1]+src[2]+fixedUrl+src[4]);  
    }
  }
  
  return html;
};

/** retrieve the HTML (fastest version, but uses innerHTML)
 *  
 *  @private
 *  @returns {String} HTML content
 */
Xinha.prototype.getInnerHTML = function()
{
  if ( !this._doc.body )
  {
    return '';
  }
  var html = "";
  switch ( this._editMode )
  {
    case "wysiwyg":
      if ( !this.config.fullPage )
      {
        // return this._doc.body.innerHTML;
        html = this._doc.body.innerHTML;
      }
      else
      {
        html = this.doctype + "\n" + this._doc.documentElement.innerHTML;
      }
    break;
    case "textmode" :
      html = this._textArea.value;
    break;
    default:
      alert("Mode <" + this._editMode + "> not defined!");
      return false;
  }

  return html;
};

/** Completely change the HTML inside
 *
 *  @private
 *  @param {String} html new content, should have been run through inwardHtml() first
 */
Xinha.prototype.setHTML = function(html)
{
  if ( !this.config.fullPage )
  {
    this._doc.body.innerHTML = html;
  }
  else
  {
    this.setFullHTML(html);
  }
  this._textArea.value = html;
};

/** sets the given doctype (useful only when config.fullPage is true)
 *  
 *  @private
 *  @param {String} doctype
 */
Xinha.prototype.setDoctype = function(doctype)
{
  this.doctype = doctype;
};

/***************************************************
 *  Category: UTILITY FUNCTIONS
 ***************************************************/

/** Variable used to pass the object to the popup editor window.
 *  @FIXME: Is this in use?
 *  @deprecated 
 *  @private
 *  @type {Object}
 */
Xinha._object = null;

/** Arrays are identified as "object" in typeof calls. Adding this tag to the Array prototype allows to distinguish between the two
 */
Array.prototype.isArray = true;
/** RegExps are identified as "object" in typeof calls. Adding this tag to the RegExp prototype allows to distinguish between the two
 */
RegExp.prototype.isRegExp = true;
/** function that returns a clone of the given object
 *  
 *  @private
 *  @param {Object} obj
 *  @returns {Object} cloned object
 */
Xinha.cloneObject = function(obj)
{
  if ( !obj )
  {
    return null;
  }
  var newObj = obj.isArray ? [] : {};

  // check for function and RegExp objects (as usual, IE is fucked up)
  if ( obj.constructor.toString().match( /\s*function Function\(/ ) || typeof obj == 'function' )
  {
    newObj = obj; // just copy reference to it
  }
  else if (  obj.isRegExp )
  {
    newObj = eval( obj.toString() ); //see no way without eval
  }
  else
  {
    for ( var n in obj )
    {
      var node = obj[n];
      if ( typeof node == 'object' )
      {
        newObj[n] = Xinha.cloneObject(node);
      }
      else
      {
        newObj[n] = node;
      }
    }
  }

  return newObj;
};


/** Extend one class from another, that is, make a sub class.
 *  This manner of doing it was probably first devised by Kevin Lindsey
 *
 *  http://kevlindev.com/tutorials/javascript/inheritance/index.htm
 *
 *  It has subsequently been used in one form or another by various toolkits 
 *  such as the YUI.
 *
 *  I make no claim as to understanding it really, but it works.
 * 
 *  Example Usage:
 *  {{{
 *  -------------------------------------------------------------------------
 
    // =========  MAKING THE INITIAL SUPER CLASS ===========
    
        document.write("<h1>Superclass Creation And Test</h1>");
    
        function Vehicle(name, sound)
        {    
          this.name  = name;
          this.sound = sound
        }
      
        Vehicle.prototype.pressHorn = function()
        {
          document.write(this.name + ': ' + this.sound + '<br/>');
        }
        
        var Bedford  = new Vehicle('Bedford Van', 'Honk Honk');
        Bedford.pressHorn(); // Vehicle::pressHorn() is defined
    
    
    // ========= MAKING A SUBCLASS OF A SUPER CLASS =========
    
        document.write("<h1>Subclass Creation And Test</h1>");
        
        // Make the sub class constructor first
        Car = function(name)
        {
          // This is how we call the parent's constructor, note that
          // we are using Car.parent.... not "this", we can't use this.
          Car.parentConstructor.call(this, name, 'Toot Toot');
        }
        
        // Remember the subclass comes first, then the base class, you are extending
        // Car with the methods and properties of Vehicle.
        Xinha.extend(Car, Vehicle);
        
        var MazdaMx5 = new Car('Mazda MX5');  
        MazdaMx5.pressHorn(); // Car::pressHorn() is inherited from Vehicle::pressHorn()
    
    // =========  ADDING METHODS TO THE SUB CLASS ===========

        document.write("<h1>Add Method to Sub Class And Test</h1>");
        
        Car.prototype.isACar = function()
        {
          document.write(this.name + ": Car::isACar() is implemented, this is a car! <br/>");
          this.pressHorn();
        }
       
        MazdaMx5.isACar(); // Car::isACar() is defined as above
        try      { Bedford.isACar(); } // Vehicle::isACar() is not defined, will throw this exception
        catch(e) { document.write("Bedford: Vehicle::onGettingCutOff() not implemented, this is not a car!<br/>"); }
    
    // =========  EXTENDING A METHOD (CALLING MASKED PARENT METHODS) ===========
    
        document.write("<h1>Extend/Override Inherited Method in Sub Class And Test</h1>");
        
        Car.prototype.pressHorn = function()
        { 
          document.write(this.name + ': I am going to press the horn... <br/>');
          Car.superClass.pressHorn.call(this);        
        }
        MazdaMx5.pressHorn(); // Car::pressHorn()
        Bedford.pressHorn();  // Vehicle::pressHorn()
        
    // =========  MODIFYING THE SUPERCLASS AFTER SUBCLASSING ===========
    
        document.write("<h1>Add New Method to Superclass And Test In Subclass</h1>");  
        
        Vehicle.prototype.startUp = function() { document.write(this.name + ": Vroooom <br/>"); }  
        MazdaMx5.startUp(); // Cars get the prototype'd startUp() also.
        
 *  -------------------------------------------------------------------------
 *  }}}  
 *
 *  @param subclass_constructor (optional)  Constructor function for the subclass
 *  @param superclass Constructor function for the superclass 
 */

Xinha.extend = function(subClass, baseClass) {
   function inheritance() {}
   inheritance.prototype = baseClass.prototype;

   subClass.prototype = new inheritance();
   subClass.prototype.constructor = subClass;
   subClass.parentConstructor = baseClass;
   subClass.superClass = baseClass.prototype;
}

/** Event Flushing
 *  To try and work around memory leaks in the rather broken
 *  garbage collector in IE, Xinha.flushEvents can be called
 *  onunload, it will remove any event listeners (that were added
 *  through _addEvent(s)) and clear any DOM-0 events.
 *  @private
 *
 */
Xinha.flushEvents = function()
{
  var x = 0;
  // @todo : check if Array.prototype.pop exists for every supported browsers
  var e = Xinha._eventFlushers.pop();
  while ( e )
  {
    try
    {
      if ( e.length == 3 )
      {
        Xinha._removeEvent(e[0], e[1], e[2]);
        x++;
      }
      else if ( e.length == 2 )
      {
        e[0]['on' + e[1]] = null;
        e[0]._xinha_dom0Events[e[1]] = null;
        x++;
      }
    }
    catch(ex)
    {
      // Do Nothing
    }
    e = Xinha._eventFlushers.pop();
  }
  
  /* 
    // This code is very agressive, and incredibly slow in IE, so I've disabled it.
    
    if(document.all)
    {
      for(var i = 0; i < document.all.length; i++)
      {
        for(var j in document.all[i])
        {
          if(/^on/.test(j) && typeof document.all[i][j] == 'function')
          {
            document.all[i][j] = null;
            x++;
          }
        }
      }
    }
  */
  
  // alert('Flushed ' + x + ' events.');
};
 /** Holds the events to be flushed
  * @type Array
  */
Xinha._eventFlushers = [];

if ( document.addEventListener )
{
 /** adds an event listener for the specified element and event type
 *  
 *  @public
 *  @see   Xinha#_addEvents
 *  @see   Xinha#addDom0Event
 *  @see   Xinha#prependDom0Event
 *  @param {DomNode}  el the DOM element the event should be attached to 
 *  @param {String}   evname the name of the event to listen for (without leading "on")
 *  @param {function} func the function to be called when the event is fired
 */
  Xinha._addEvent = function(el, evname, func)
  {
    el.addEventListener(evname, func, false);
    Xinha._eventFlushers.push([el, evname, func]);
  };
 
 /** removes an event listener previously added
 *  
 *  @public
 *  @see   Xinha#_removeEvents
 *  @param {DomNode}  el the DOM element the event should be removed from 
 *  @param {String}   evname the name of the event the listener should be removed from (without leading "on")
 *  @param {function} func the function to be removed
 */
  Xinha._removeEvent = function(el, evname, func)
  {
    el.removeEventListener(evname, func, false);
  };
 
 /** stops bubbling of the event, if no further listeners should be triggered
 *  
 *  @public
 *  @param {event} ev the event to be stopped
 */
  Xinha._stopEvent = function(ev)
  {
    if(ev.preventDefault)
    {  
      ev.preventDefault();
    }
    // IE9 now supports addEventListener, but does not support preventDefault.  Sigh
    else
    {
      ev.returnValue = false;
    }
    
    if(ev.stopPropagation)
    {
      ev.stopPropagation();
    }
    // IE9 now supports addEventListener, but does not support stopPropagation.  Sigh
    else
    {
      ev.cancelBubble = true;
    }
  };
}
 /** same as above, for IE
 *  
 */
else if ( document.attachEvent )
{
  Xinha._addEvent = function(el, evname, func)
  {
    el.attachEvent("on" + evname, func);
    Xinha._eventFlushers.push([el, evname, func]);
  };
  Xinha._removeEvent = function(el, evname, func)
  {
    el.detachEvent("on" + evname, func);
  };
  Xinha._stopEvent = function(ev)
  {
    try
    {
      ev.cancelBubble = true;
      ev.returnValue = false;
    }
    catch (ex)
    {
      // Perhaps we could try here to stop the window.event
      // window.event.cancelBubble = true;
      // window.event.returnValue = false;
    }
  };
}
else
{
  Xinha._addEvent = function(el, evname, func)
  {
    alert('_addEvent is not supported');
  };
  Xinha._removeEvent = function(el, evname, func)
  {
    alert('_removeEvent is not supported');
  };
  Xinha._stopEvent = function(ev)
  {
    alert('_stopEvent is not supported');
  };
}
 /** add several events at once to one element
 *  
 *  @public
 *  @see Xinha#_addEvent
 *  @param {DomNode}  el the DOM element the event should be attached to 
 *  @param {Array}    evs the names of the event to listen for (without leading "on")
 *  @param {function} func the function to be called when the event is fired
 */
Xinha._addEvents = function(el, evs, func)
{
  for ( var i = evs.length; --i >= 0; )
  {
    Xinha._addEvent(el, evs[i], func);
  }
};
 /** remove several events at once to from element
 *  
 *  @public
 *  @see Xinha#_removeEvent
 *  @param {DomNode}  el the DOM element the events should be remove from
 *  @param {Array}    evs the names of the events the listener should be removed from (without leading "on")
 *  @param {function} func the function to be removed
 */
Xinha._removeEvents = function(el, evs, func)
{
  for ( var i = evs.length; --i >= 0; )
  {
    Xinha._removeEvent(el, evs[i], func);
  }
};

/** Adds a function that is executed in the moment the DOM is ready, but as opposed to window.onload before images etc. have been loaded
*   http://dean.edwards.name/weblog/2006/06/again/
*   IE part from jQuery
*  @public
*  @author Dean Edwards/Matthias Miller/ John Resig / Diego Perini
*  @param {Function}  func the function to be executed
*  @param {Window}    scope the window that is listened to
*/
Xinha.addOnloadHandler = function (func, scope)
{
 scope = scope ? scope : window;

 var init = function ()
 {
   // quit if this function has already been called
   if (arguments.callee.done) 
   {
     return;
   }
   // flag this function so we don't do the same thing twice
   arguments.callee.done = true;
   // kill the timer
   if (Xinha.onloadTimer)
   {
     clearInterval(Xinha.onloadTimer);
   }

   func();
 };
  if (Xinha.is_ie)
  {
    // ensure firing before onload,
    // maybe late but safe also for iframes
    document.attachEvent("onreadystatechange", function(){
      if ( document.readyState === "complete" ) {
        document.detachEvent( "onreadystatechange", arguments.callee );
        init();
      }
    });
    if ( document.documentElement.doScroll && typeof window.frameElement === "undefined" ) (function(){
      if (arguments.callee.done) return;
      try {
        // If IE is used, use the trick by Diego Perini
        // http://javascript.nwbox.com/IEContentLoaded/
        document.documentElement.doScroll("left");
      } catch( error ) {
        setTimeout( arguments.callee, 0 );
        return;
      }
      // and execute any waiting functions
      init();
    })();
  }
 else if (/applewebkit|KHTML/i.test(navigator.userAgent) ) /* Safari/WebKit/KHTML */
 {
   Xinha.onloadTimer = scope.setInterval(function()
   {
     if (/loaded|complete/.test(scope.document.readyState))
     {
       init(); // call the onload handler
     }
   }, 10);
 }
 else /* for Mozilla/Opera9 */
 {
   scope.document.addEventListener("DOMContentLoaded", init, false);

 }
 Xinha._addEvent(scope, 'load', init); // incase anything went wrong
};

/**
 * Adds a standard "DOM-0" event listener to an element.
 * The DOM-0 events are those applied directly as attributes to
 * an element - eg element.onclick = stuff;
 *
 * By using this function instead of simply overwriting any existing
 * DOM-0 event by the same name on the element it will trigger as well
 * as the existing ones.  Handlers are triggered one after the other
 * in the order they are added.
 *
 * Remember to return true/false from your handler, this will determine
 * whether subsequent handlers will be triggered (ie that the event will
 * continue or be canceled).
 *  
 *  @public
 *  @see Xinha#_addEvent
 *  @see Xinha#prependDom0Event
 *  @param {DomNode}  el the DOM element the event should be attached to 
 *  @param {String}   ev the name of the event to listen for (without leading "on")
 *  @param {function} fn the function to be called when the event is fired
 */

Xinha.addDom0Event = function(el, ev, fn)
{
  Xinha._prepareForDom0Events(el, ev);
  el._xinha_dom0Events[ev].unshift(fn);
};


/** See addDom0Event, the difference is that handlers registered using
 *  prependDom0Event will be triggered before existing DOM-0 events of the
 *  same name on the same element.
 *  
 *  @public
 *  @see Xinha#_addEvent
 *  @see Xinha#addDom0Event
 *  @param {DomNode}  the DOM element the event should be attached to 
 *  @param {String}   the name of the event to listen for (without leading "on")
 *  @param {function} the function to be called when the event is fired
 */

Xinha.prependDom0Event = function(el, ev, fn)
{
  Xinha._prepareForDom0Events(el, ev);
  el._xinha_dom0Events[ev].push(fn);
};

Xinha.getEvent = function(ev)
{
  return ev || window.event;
};
/**
 * Prepares an element to receive more than one DOM-0 event handler
 * when handlers are added via addDom0Event and prependDom0Event.
 *
 * @private
 */
Xinha._prepareForDom0Events = function(el, ev)
{
  // Create a structure to hold our lists of event handlers
  if ( typeof el._xinha_dom0Events == 'undefined' )
  {
    el._xinha_dom0Events = {};
    Xinha.freeLater(el, '_xinha_dom0Events');
  }

  // Create a list of handlers for this event type
  if ( typeof el._xinha_dom0Events[ev] == 'undefined' )
  {
    el._xinha_dom0Events[ev] = [ ];
    if ( typeof el['on'+ev] == 'function' )
    {
      el._xinha_dom0Events[ev].push(el['on'+ev]);
    }

    // Make the actual event handler, which runs through
    // each of the handlers in the list and executes them
    // in the correct context.
    el['on'+ev] = function(event)
    {
      var a = el._xinha_dom0Events[ev];
      // call previous submit methods if they were there.
      var allOK = true;
      for ( var i = a.length; --i >= 0; )
      {
        // We want the handler to be a member of the form, not the array, so that "this" will work correctly
        el._xinha_tempEventHandler = a[i];
        if ( el._xinha_tempEventHandler(event) === false )
        {
          el._xinha_tempEventHandler = null;
          allOK = false;
          break;
        }
        el._xinha_tempEventHandler = null;
      }
      return allOK;
    };

    Xinha._eventFlushers.push([el, ev]);
  }
};

Xinha.prototype.notifyOn = function(ev, fn)
{
  if ( typeof this._notifyListeners[ev] == 'undefined' )
  {
    this._notifyListeners[ev] = [];
    Xinha.freeLater(this, '_notifyListeners');
  }
  this._notifyListeners[ev].push(fn);
};

Xinha.prototype.notifyOf = function(ev, args)
{
  if ( this._notifyListeners[ev] )
  {
    for ( var i = 0; i < this._notifyListeners[ev].length; i++ )
    {
      this._notifyListeners[ev][i](ev, args);
    }
  }
};

/** List of tag names that are defined as block level elements in HTML
 *  
 *  @private
 *  @see Xinha#isBlockElement
 *  @type {String}
 */
Xinha._blockTags = " body form textarea fieldset ul ol dl li div " +
"p h1 h2 h3 h4 h5 h6 quote pre table thead " +
"tbody tfoot tr td th iframe address blockquote title meta link style head ";

/** Checks if one element is in the list of elements that are defined as block level elements in HTML
 *  
 *  @param {DomNode}  el The DOM element to check
 *  @returns {Boolean}
 */
Xinha.isBlockElement = function(el)
{
  return el && el.nodeType == 1 && (Xinha._blockTags.indexOf(" " + el.tagName.toLowerCase() + " ") != -1);
};
/** List of tag names that are allowed to contain a paragraph
 *  
 *  @private
 *  @see Xinha#isParaContainer
 *  @type {String}
 */
Xinha._paraContainerTags = " body td th caption fieldset div ";
/** Checks if one element is in the list of elements that are allowed to contain a paragraph in HTML
 *  
 *  @param {DomNode}  el The DOM element to check
 *  @returns {Boolean}
 */
Xinha.isParaContainer = function(el)
{
  return el && el.nodeType == 1 && (Xinha._paraContainerTags.indexOf(" " + el.tagName.toLowerCase() + " ") != -1);
};


/** These are all the tags for which the end tag is not optional or  forbidden, taken from the list at:
 *   http: www.w3.org/TR/REC-html40/index/elements.html
 *  
 *  @private
 *  @see Xinha#needsClosingTag
 *  @type String
 */
Xinha._closingTags = " a abbr acronym address applet b bdo big blockquote button caption center cite code del dfn dir div dl em fieldset font form frameset h1 h2 h3 h4 h5 h6 i iframe ins kbd label legend map menu noframes noscript object ol optgroup pre q s samp script select small span strike strong style sub sup table textarea title tt u ul var ";

/** Checks if one element is in the list of elements for which the end tag is not optional or  forbidden in HTML
 *  
 *  @param {DomNode}  el The DOM element to check
 *  @returns {Boolean}
 */
Xinha.needsClosingTag = function(el)
{
  return el && el.nodeType == 1 && (Xinha._closingTags.indexOf(" " + el.tagName.toLowerCase() + " ") != -1);
};

/** Performs HTML encoding of some given string (converts HTML special characters to entities)
 *  
 *  @param {String}  str The unencoded input
 *  @returns {String} The encoded output
 */
Xinha.htmlEncode = function(str)
{
  if (!str)
  {
    return '';
  }  if ( typeof str.replace == 'undefined' )
  {
    str = str.toString();
  }
  // we don't need regexp for that, but.. so be it for now.
  str = str.replace(/&/ig, "&amp;");
  str = str.replace(/</ig, "&lt;");
  str = str.replace(/>/ig, "&gt;");
  str = str.replace(/\xA0/g, "&nbsp;"); // Decimal 160, non-breaking-space
  str = str.replace(/\x22/g, "&quot;");
  // \x22 means '"' -- we use hex reprezentation so that we don't disturb
  // JS compressors (well, at least mine fails.. ;)
  return str;
};

/** Strips host-part of URL which is added by browsers to links relative to server root
 *  
 *  @param {String}  string 
 *  @returns {String} 
 */
Xinha.prototype.stripBaseURL = function(string)
{
  if ( this.config.baseHref === null || !this.config.stripBaseHref )
  {
    return string;
  }
  var baseurl = this.config.baseHref.replace(/^(https?:\/\/[^\/]+)(.*)$/, '$1');
  var basere = new RegExp(baseurl);
  return string.replace(basere, "");
};

if (typeof String.prototype.trim != 'function')
{
  /** Removes whitespace from beginning and end of a string. Custom implementation for JS engines that don't support it natively
   *  
   *  @returns {String} 
   */
  String.prototype.trim = function()
  {
    return this.replace(/^\s+/, '').replace(/\s+$/, '');
  };
}

/** Creates a rgb-style rgb(r,g,b) color from a (24bit) number
 *  
 *  @param {Integer}
 *  @returns {String} rgb(r,g,b) color definition
 */
Xinha._makeColor = function(v)
{
  if ( typeof v != "number" )
  {
    // already in rgb (hopefully); IE doesn't get here.
    return v;
  }
  // IE sends number; convert to rgb.
  var r = v & 0xFF;
  var g = (v >> 8) & 0xFF;
  var b = (v >> 16) & 0xFF;
  return "rgb(" + r + "," + g + "," + b + ")";
};

/** Returns hexadecimal color representation from a number or a rgb-style color.
 *  
 *  @param {String|Integer} v rgb(r,g,b) or 24bit color definition
 *  @returns {String} #RRGGBB color definition
 */
Xinha._colorToRgb = function(v)
{
  if ( !v )
  {
    return '';
  }
  var r,g,b;
  // @todo: why declaring this function here ? This needs to be a public methode of the object Xinha._colorToRgb
  // returns the hex representation of one byte (2 digits)
  function hex(d)
  {
    return (d < 16) ? ("0" + d.toString(16)) : d.toString(16);
  }

  if ( typeof v == "number" )
  {
    // we're talking to IE here
    r = v & 0xFF;
    g = (v >> 8) & 0xFF;
    b = (v >> 16) & 0xFF;
    return "#" + hex(r) + hex(g) + hex(b);
  }

  if ( v.substr(0, 3) == "rgb" )
  {
    // in rgb(...) form -- Mozilla
    var re = /rgb\s*\(\s*([0-9]+)\s*,\s*([0-9]+)\s*,\s*([0-9]+)\s*\)/;
    if ( v.match(re) )
    {
      r = parseInt(RegExp.$1, 10);
      g = parseInt(RegExp.$2, 10);
      b = parseInt(RegExp.$3, 10);
      return "#" + hex(r) + hex(g) + hex(b);
    }
    // doesn't match RE?!  maybe uses percentages or float numbers
    // -- FIXME: not yet implemented.
    return null;
  }

  if ( v.substr(0, 1) == "#" )
  {
    // already hex rgb (hopefully :D )
    return v;
  }

  // if everything else fails ;)
  return null;
};

/** Modal popup dialogs
 *  
 *  @param {String} url URL to the popup dialog
 *  @param {Function} action A function that receives one value; this function will get called 
 *                    after the dialog is closed, with the return value of the dialog.
 *  @param {Mixed} init A variable that is passed to the popup window to pass arbitrary data
 */
Xinha.prototype._popupDialog = function(url, action, init)
{
  Dialog(this.popupURL(url), action, init);
};

/** Creates a path in the form _editor_url + "plugins/" + plugin + "/img/" + file
 *  
 *  @deprecated
 *  @param {String} file Name of the image
 *  @param {String} plugin optional If omitted, simply _editor_url + file is returned 
 *  @returns {String}
 */
Xinha.prototype.imgURL = function(file, plugin)
{
  if ( typeof plugin == "undefined" )
  {
    return _editor_url + file;
  }
  else
  {
    return Xinha.getPluginDir(plugin) + "/img/" + file;
  }
};
/** Creates a path
 *  
 *  @deprecated
 *  @param {String} file Name of the popup
 *  @returns {String}
 */
Xinha.prototype.popupURL = function(file)
{
  var url = "";
  if ( file.match(/^plugin:\/\/(.*?)\/(.*)/) )
  {
    var plugin = RegExp.$1;
    var popup = RegExp.$2;
    if ( !/\.(html?|php)$/.test(popup) )
    {
      popup += ".html";
    }
    url = Xinha.getPluginDir(plugin) + "/popups/" + popup;
  }
  else if ( file.match(/^\/.*?/) || file.match(/^[a-z]+?:/))
  {
    url = file;
  }
  else
  {
    url = _editor_url + this.config.popupURL + file;
  }
  return url;
};



/** FIX: Internet Explorer returns an item having the _name_ equal to the given
 * id, even if it's not having any id.  This way it can return a different form
 * field, even if it's not a textarea.  This workarounds the problem by
 * specifically looking to search only elements having a certain tag name.
 * @param {String} tag The tag name to limit the return to
 * @param {String} id
 * @returns {DomNode}
 */
Xinha.getElementById = function(tag, id)
{
  var el, i, objs = document.getElementsByTagName(tag);
  for ( i = objs.length; --i >= 0 && (el = objs[i]); )
  {
    if ( el.id == id )
    {
      return el;
    }
  }
  return null;
};


/** Use some CSS trickery to toggle borders on tables 
 *	@returns {Boolean} always true
 */

Xinha.prototype._toggleBorders = function()
{
  var tables = this._doc.getElementsByTagName('TABLE');
  if ( tables.length !== 0 )
  {
   if ( !this.borders )
   {    
    this.borders = true;
   }
   else
   {
     this.borders = false;
   }

   for ( var i=0; i < tables.length; i++ )
   {
     if ( this.borders )
     {
        Xinha._addClass(tables[i], 'htmtableborders');
     }
     else
     {
       Xinha._removeClass(tables[i], 'htmtableborders');
     }
   }
  }
  return true;
};
/** Adds the styles for table borders to the iframe during generation
 *  
 *  @private
 *  @see Xinha#stripCoreCSS
 *  @param {String} html optional  
 *  @returns {String} html HTML with added styles or only styles if html omitted
 */
Xinha.addCoreCSS = function(html)
{
    var coreCSS = "<style title=\"XinhaInternalCSS\" type=\"text/css\">" +
    ".htmtableborders, .htmtableborders td, .htmtableborders th {border : 1px dashed lightgrey ! important;}\n" +
    "html, body { border: 0px; } \n" +
    "body { background-color: #ffffff; } \n" +
    "img, hr { cursor: default } \n" +
    "</style>\n";
    
    if( html && /<head>/i.test(html))
    {
      return html.replace(/<head>/i, '<head>' + coreCSS);      
    }
    else if ( html)
    {
      return coreCSS + html;
    }
    else
    {
      return coreCSS;
    }
};
/** Allows plugins to add a stylesheet for internal use to the edited document that won't appear in the HTML output
 *  
 *  @see Xinha#stripCoreCSS
 *  @param {String} stylesheet URL of the styleshett to be added
 */
Xinha.prototype.addEditorStylesheet = function (stylesheet)
{
    var style = this._doc.createElement("link");
    style.rel = 'stylesheet';
    style.type = 'text/css';
    style.title = 'XinhaInternalCSS';
    style.href = stylesheet;
    this._doc.getElementsByTagName("HEAD")[0].appendChild(style);
};
/** Remove internal styles
 *  
 *  @private
 *  @see Xinha#addCoreCSS
 *  @param {String} html 
 *  @returns {String} 
 */
Xinha.stripCoreCSS = function(html)
{
  return html.replace(/<style[^>]+title="XinhaInternalCSS"(.|\n)*?<\/style>/ig, '').replace(/<link[^>]+title="XinhaInternalCSS"(.|\n)*?>/ig, ''); 
};
/** Removes one CSS class (that is one of possible more parts 
 *   separated by spaces) from a given element
 *  
 *  @see Xinha#_removeClasses
 *  @param {DomNode}  el The DOM element the class will be removed from
 *  @param {String}   className The class to be removed
 */
Xinha._removeClass = function(el, className)
{
  if ( ! ( el && el.className ) )
  {
    return;
  }
  var cls = el.className.split(" ");
  var ar = [];
  for ( var i = cls.length; i > 0; )
  {
    if ( cls[--i] != className )
    {
      ar[ar.length] = cls[i];
    }
  }
  el.className = ar.join(" ");
};
/** Adds one CSS class  to a given element (that is, it expands its className property by the given string,
 *  separated by a space)
 *  
 *  @see Xinha#addClasses
 *  @param {DomNode}  el The DOM element the class will be added to
 *  @param {String}   className The class to be added
 */
Xinha._addClass = function(el, className)
{
  // remove the class first, if already there
  Xinha._removeClass(el, className);
  el.className += " " + className;
};

/** Adds CSS classes  to a given element (that is, it expands its className property by the given string,
 *  separated by a space, thereby checking that no class is doubly added)
 *  
 *  @see Xinha#addClass
 *  @param {DomNode}  el The DOM element the classes will be added to
 *  @param {String}   classes The classes to be added
 */
Xinha.addClasses = function(el, classes)
{
  if ( el !== null )
  {
    var thiers = el.className.trim().split(' ');
    var ours   = classes.split(' ');
    for ( var x = 0; x < ours.length; x++ )
    {
      var exists = false;
      for ( var i = 0; exists === false && i < thiers.length; i++ )
      {
        if ( thiers[i] == ours[x] )
        {
          exists = true;
        }
      }
      if ( exists === false )
      {
        thiers[thiers.length] = ours[x];
      }
    }
    el.className = thiers.join(' ').trim();
  }
};

/** Removes CSS classes (that is one or more of possibly several parts 
 *   separated by spaces) from a given element
 *  
 *  @see Xinha#_removeClasses
 *  @param {DomNode}  el The DOM element the class will be removed from
 *  @param {String}   className The class to be removed
 */
Xinha.removeClasses = function(el, classes)
{
  var existing    = el.className.trim().split();
  var new_classes = [];
  var remove      = classes.trim().split();

  for ( var i = 0; i < existing.length; i++ )
  {
    var found = false;
    for ( var x = 0; x < remove.length && !found; x++ )
    {
      if ( existing[i] == remove[x] )
      {
        found = true;
      }
    }
    if ( !found )
    {
      new_classes[new_classes.length] = existing[i];
    }
  }
  return new_classes.join(' ');
};

/** Alias of Xinha._addClass()
 *  @see Xinha#_addClass
 */
Xinha.addClass       = Xinha._addClass;
/** Alias of Xinha.Xinha._removeClass()
 *  @see Xinha#_removeClass
 */
Xinha.removeClass    = Xinha._removeClass;
/** Alias of Xinha.addClasses()
 *  @see Xinha#addClasses
 */
Xinha._addClasses    = Xinha.addClasses;
/** Alias of Xinha.removeClasses()
 *  @see Xinha#removeClasses
 */
Xinha._removeClasses = Xinha.removeClasses;

/** Checks if one element has set the given className
 *  
 *  @param {DomNode}  el The DOM element to check
 *  @param {String}   className The class to be looked for
 *  @returns {Boolean}
 */
Xinha._hasClass = function(el, className)
{
  if ( ! ( el && el.className ) )
  {
    return false;
  }
  var cls = el.className.split(" ");
  for ( var i = cls.length; i > 0; )
  {
    if ( cls[--i] == className )
    {
      return true;
    }
  }
  return false;
};

/**
 * Use XMLHTTPRequest to post some data back to the server and do something
 * with the response (asyncronously!), this is used by such things as the tidy
 * functions
 * @param {String} url The address for the HTTPRequest
 * @param {Object} data The data to be passed to the server like {name:"value"}
 * @param {Function} success A function that is called when an answer is
 *                           received from the server with the responseText as argument.
 * @param {Function} failure A function that is called when we fail to receive
 *                           an answer from the server. We pass it the request object.
 */
 
/** mod_security (an apache module which scans incoming requests for potential hack attempts)
 *  has a rule which triggers when it gets an incoming Content-Type with a charset
 *  see ticket:1028 to try and work around this, if we get a failure in a postback
 *  then Xinha._postback_send_charset will be set to false and the request tried again (once)
 *  @type Boolean
 *  @private
 */ 
// 
// 
// 
Xinha._postback_send_charset = true;
/** Use XMLHTTPRequest to send some some data to the server and do something
 *  with the getback (asyncronously!)
 * @param {String} url The address for the HTTPRequest
 * @param {Function} success A function that is called when an answer is
 *                           received from the server with the responseText as argument.
 * @param {Function} failure A function that is called when we fail to receive
 *                           an answer from the server. We pass it the request object.
 */
Xinha._postback = function(url, data, success, failure)
{
  var req = null;
  req = Xinha.getXMLHTTPRequestObject();

  var content = '';
  if (typeof data == 'string')
  {
    content = data;
  }
  else if(typeof data == "object")
  {
    for ( var i in data )
    {
      content += (content.length ? '&' : '') + i + '=' + encodeURIComponent(data[i]);
    }
  }

  function callBack()
  {
    if ( req.readyState == 4 )
    {
      if ( ((req.status / 100) == 2) || Xinha.isRunLocally && req.status === 0 )
      {
        if ( typeof success == 'function' )
        {
          success(req.responseText, req);
        }
      }
      else if(Xinha._postback_send_charset)
      {        
        Xinha._postback_send_charset = false;
        Xinha._postback(url,data,success, failure);
      }
      else if (typeof failure == 'function')
      {
        failure(req);
      }
      else
      {
        alert('An error has occurred: ' + req.statusText + '\nURL: ' + url);
      }
    }
  }

  req.onreadystatechange = callBack;

  req.open('POST', url, true);
  req.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded'+(Xinha._postback_send_charset ? '; charset=UTF-8' : ''));

  req.send(content);
};

/** Use XMLHTTPRequest to receive some data from the server and do something
 * with the it (asyncronously!)
 * @param {String} url The address for the HTTPRequest
 * @param {Function} success A function that is called when an answer is
 *                           received from the server with the responseText as argument.
 * @param {Function} failure A function that is called when we fail to receive
 *                           an answer from the server. We pass it the request object.
 */
Xinha._getback = function(url, success, failure)
{
  var req = null;
  req = Xinha.getXMLHTTPRequestObject();

  function callBack()
  {
    if ( req.readyState == 4 )
    {
      if ( ((req.status / 100) == 2) || Xinha.isRunLocally && req.status === 0 )
      {
        success(req.responseText, req);
      }
      else if (typeof failure == 'function')
      {
        failure(req);
      }
      else
      {
        alert('An error has occurred: ' + req.statusText + '\nURL: ' + url);
      }
    }
  }

  req.onreadystatechange = callBack;
  req.open('GET', url, true);
  req.send(null);
};

Xinha.ping = function(url, successHandler, failHandler)
{
  var req = null;
  req = Xinha.getXMLHTTPRequestObject();

  function callBack()
  {
    if ( req.readyState == 4 )
    {
      if ( ((req.status / 100) == 2) || Xinha.isRunLocally && req.status === 0 )
      {
        if (successHandler) 
        {
          successHandler(req);
        }
      }
      else
      {
        if (failHandler) 
        {
          failHandler(req);
        }
      }
    }
  }

  // Opera seems to have some problems mixing HEAD requests with GET requests.
  // The GET is slower, so it's a net slowdown for Opera, but it keeps things
  // from breaking.
  var method = 'GET';
  req.onreadystatechange = callBack;
  req.open(method, url, true);
  req.send(null);
};

/** Use XMLHTTPRequest to receive some data from the server syncronously
 *  @param {String} url The address for the HTTPRequest
 */
Xinha._geturlcontent = function(url, returnXML)
{
  var req = null;
  req = Xinha.getXMLHTTPRequestObject();

  // Synchronous!
  req.open('GET', url, false);
  req.send(null);
  if ( ((req.status / 100) == 2) || Xinha.isRunLocally && req.status === 0 )
  {
    return (returnXML) ? req.responseXML : req.responseText;
  }
  else
  {
    return '';
  }
};


/** Use XMLHTTPRequest to send some some data to the server and return the result synchronously
 *
 * @param {String} url The address for the HTTPRequest
 * @param data the data to send, streing or array
 */
Xinha._posturlcontent = function(url, data, returnXML)
{
  var req = null;
  req = Xinha.getXMLHTTPRequestObject();

  var content = '';
  if (typeof data == 'string')
  {
    content = data;
  }
  else if(typeof data == "object")
  {
    for ( var i in data )
    {
      content += (content.length ? '&' : '') + i + '=' + encodeURIComponent(data[i]);
    }
  }

  req.open('POST', url, false);    
  req.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded'+(Xinha._postback_send_charset ? '; charset=UTF-8' : ''));
  req.send(content);
  
  if ( ((req.status / 100) == 2) || Xinha.isRunLocally && req.status === 0 )
  {
    return (returnXML) ? req.responseXML : req.responseText;
  }
  else
  {
    return '';
  }
  
};
// Unless somebody already has, make a little function to debug things

if (typeof dumpValues == 'undefined') 
{
  dumpValues = function(o)
  {
    var s = '';
    for (var prop in o) 
    {
      if (window.console && typeof window.console.log == 'function') 
      {
        if (typeof console.firebug != 'undefined') 
        {
          console.log(o);
        }
        else 
        {
          console.log(prop + ' = ' + o[prop] + '\n');
        }
      }
      else
      {
        s += prop + ' = ' + o[prop] + '\n';
      }
    }
    if (s) 
    {
      if (document.getElementById('errors'))
      {
        document.getElementById('errors').value += s;
      }
      else
      {
        var x = window.open("", "debugger");
        x.document.write('<pre>' + s + '</pre>');
      }

    }
  };
}
if ( !Array.prototype.contains )
{
  /** Walks through an array and checks if the specified item exists in it
  * @param {String} needle The string to search for
  * @returns {Boolean} True if item found, false otherwise 
  */
  Array.prototype.contains = function(needle)
  {
    var haystack = this;
    for ( var i = 0; i < haystack.length; i++ )
    {
      if ( needle == haystack[i] )
      {
        return true;
      }
    }
    return false;
  };
}

if ( !Array.prototype.indexOf )
{
  /** Walks through an array and, if the specified item exists in it, returns the position
  * @param {String} needle The string to search for
  * @returns {Integer|-1} Index position if item found, -1 otherwise (same as built in js)
  */
  Array.prototype.indexOf = function(needle)
  {
    var haystack = this;
    for ( var i = 0; i < haystack.length; i++ )
    {
      if ( needle == haystack[i] )
      {
        return i;
      }
    }
    return -1;
  };
}
if ( !Array.prototype.append )
{
  /** Adds an item to an array
   * @param {Mixed} a Item to add
   * @returns {Array} The array including the newly added item
   */
  Array.prototype.append  = function(a)
  {
    for ( var i = 0; i < a.length; i++ )
    {
      this.push(a[i]);
    }
    return this;
  };
}
/** Executes a provided function once per array element.
 *  Custom implementation for JS engines that don't support it natively
 * @source http://developer.mozilla.org/En/Core_JavaScript_1.5_Reference/Global_Objects/Array/ForEach
 * @param {Function} fn Function to execute for each element
 * @param {Object} thisObject Object to use as this when executing callback. 
 */
if (!Array.prototype.forEach)
{
  Array.prototype.forEach = function(fn /*, thisObject*/)
  {
    var len = this.length;
    if (typeof fn != "function")
    {
      throw new TypeError();
    }

    var thisObject = arguments[1];
    for (var i = 0; i < len; i++)
    {
      if (i in this)
      {
        fn.call(thisObject, this[i], i, this);
      }
    }
  };
}
/** Returns all elements within a given class name inside an element
 * @type Array
 * @param {DomNode|document} el wherein to search
 * @param {Object} className
 */
Xinha.getElementsByClassName = function(el,className)
{
  if (el.getElementsByClassName)
  {
    return Array.prototype.slice.call(el.getElementsByClassName(className));
  }
  else
  {
    var els = el.getElementsByTagName('*');
    var result = [];
    var classNames;
    for (var i=0;i<els.length;i++)
    {
      classNames = els[i].className.split(' ');
      if (classNames.contains(className)) 
      {
        result.push(els[i]);
      }
    }
    return result;
  }
};

/** Returns true if all elements of <em>a2</em> are also contained in <em>a1</em> (at least I think this is what it does)
* @param {Array} a1
* @param {Array} a2
* @returns {Boolean}
*/
Xinha.arrayContainsArray = function(a1, a2)
{
  var all_found = true;
  for ( var x = 0; x < a2.length; x++ )
  {
    var found = false;
    for ( var i = 0; i < a1.length; i++ )
    {
      if ( a1[i] == a2[x] )
      {
        found = true;
        break;
      }
    }
    if ( !found )
    {
      all_found = false;
      break;
    }
  }
  return all_found;
};
/** Walks through an array and applies a filter function to each item
* @param {Array} a1 The array to filter
* @param {Function} filterfn If this function returns true, the item is added to the new array
* @returns {Array} Filtered array
*/
Xinha.arrayFilter = function(a1, filterfn)
{
  var new_a = [ ];
  for ( var x = 0; x < a1.length; x++ )
  {
    if ( filterfn(a1[x]) )
    {
      new_a[new_a.length] = a1[x];
    }
  }
  return new_a;
};
/** Converts a Collection object to an array 
* @param {Collection} collection The array to filter
* @returns {Array} Array containing the item of collection
*/
Xinha.collectionToArray = function(collection)
{
  try
  {
    return collection.length ? Array.prototype.slice.call(collection) : []; //Collection to Array
  }
  catch(e)
  {
    // In certain implementations (*cough* IE), you can't call slice on a
    // collection.  We'll fallback to using the simple, non-native iterative
    // approach.
  }

  var array = [ ];
  for ( var i = 0; i < collection.length; i++ )
  {
    array.push(collection.item(i));
  }
  return array;
};

/** Index for Xinha.uniq function 
*	@private
*/
Xinha.uniq_count = 0;
/** Returns a string that is unique on the page
*	@param {String} prefix This string is prefixed to a running number
*   @returns {String}
*/
Xinha.uniq = function(prefix)
{
  return prefix + Xinha.uniq_count++;
};

// New language handling functions

/** Load a language file.
 *  This function should not be used directly, Xinha._lc will use it when necessary.
 *  @private
 *  @param {String} context Case sensitive context name, eg 'Xinha', 'TableOperations', ...
 *  @returns {Object}
 */
Xinha._loadlang = function(context,url)
{
  var lang;
  
  if ( typeof _editor_lcbackend == "string" )
  {
    //use backend
    url = _editor_lcbackend;
    url = url.replace(/%lang%/, _editor_lang);
    url = url.replace(/%context%/, context);
  }
  else if (!url)
  {
    //use internal files
    if ( context != 'Xinha')
    {
      url = Xinha.getPluginDir(context)+"/lang/"+_editor_lang+".js";
    }
    else
    {
      Xinha.setLoadingMessage("Loading language");
      url = _editor_url+"lang/"+_editor_lang+".js";
    }
  }

  var langData = Xinha._geturlcontent(url);
  if ( langData !== "" )
  {
    try
    {
      eval('lang = ' + langData);
    }
    catch(ex)
    {
      alert('Error reading Language-File ('+url+'):\n'+Error.toString());
      lang = {};
    }
  }
  else
  {
    Xinha.debugMsg("Unable to read Language File '"+url+"' or file blank, either does not exist (which is probably ok) or permissions issue (which is probably not ok).", 'info');
    lang = {};
  }

  return lang;
};

/** Return a localised string.
 * @param {String} string English language string. It can also contain variables in the form "Some text with $variable=replaced text$". 
 *                  This replaces $variable in "Some text with $variable" with "replaced text"
 * @param {String} context   Case sensitive context name, eg 'Xinha' (default), 'TableOperations'...
 * @param {Object} replace   Replace $variables in String, eg {foo: 'replaceText'} ($foo in string will be replaced by replaceText)
 */
Xinha._lc = function(string, context, replace)
{
  var url,ret;
  if (typeof context == 'object' && context.url && context.context)
  {
    url = context.url + _editor_lang + ".js";
    context = context.context;
  }

  var m = null;
  if (typeof string == 'string') 
  {
    m = string.match(/\$(.*?)=(.*?)\$/g);
  }
  if (m) 
  {
    if (!replace) 
    {
      replace = {};
    }
    for (var i = 0;i<m.length;i++)
    {
      var n = m[i].match(/\$(.*?)=(.*?)\$/);
      replace[n[1]] = n[2];
      string = string.replace(n[0],'$'+n[1]);
    }
  }
  if ( _editor_lang == "en" )
  {
    if ( typeof string == 'object' && string.string )
    {
      ret = string.string;
    }
    else
    {
      ret = string;
    }
  }
  else
  {
    
    if ( typeof Xinha._lc_catalog == 'undefined' )
    {
      Xinha._lc_catalog = [ ];
    }

    if ( typeof context == 'undefined' )
    {
      context = 'Xinha';
    }

    // By default we will try and load a merged language file so that the user
    //  is not loading quite so many javascript files just for language data
    if ( typeof _editor_lang_merged_file == 'undefined' || _editor_lang_merged_file === true )
    {
      _editor_lang_merged_file = _editor_url + 'lang/merged/' + _editor_lang+'.js';
    }
    
    // Allow to provide an explicitly merged translation file for testing translations 
    // (and for easy using a merged translation set)    
    if ( typeof _editor_lang_merged_file == 'string' )
    {
      // Note that if this fails to load (doesn't exist)
      //  then we will get an empty object
      Xinha._lc_catalog = Xinha._loadlang(null, _editor_lang_merged_file);
      _editor_lang_merged_file = null;
      
      // Resolve the __NEW_TRANSLATIONS__ section by pushing it's translations
      //  back one level into the catalog proper
      if(typeof Xinha._lc_catalog['__NEW_TRANSLATIONS__'])
      {
        for(var moduleName in Xinha._lc_catalog['__NEW_TRANSLATIONS__'])
        {
          for(var englishString in Xinha._lc_catalog['__NEW_TRANSLATIONS__'][moduleName])
          {
            var translatedString = Xinha._lc_catalog['__NEW_TRANSLATIONS__'][moduleName][englishString];
            if(translatedString.match(/<<([A-Za-z0-9_]+)(:.*)?>>/))
            {
              var linkedModule = RegExp.$1;
              if(typeof Xinha._lc_catalog[linkedModule] != 'undefined' && typeof Xinha._lc_catalog[linkedModule][englishString] == 'string')
              {
                translatedString = Xinha._lc_catalog[linkedModule][englishString];
              }
              else
              {
                translatedString = '';
              }
            }
            
            if(translatedString.length)
            {
              if(typeof Xinha._lc_catalog[moduleName] == 'undefined')
              {
                Xinha._lc_catalog[moduleName] = { };
              }
              Xinha._lc_catalog[moduleName][englishString] = translatedString;
            }
          }
        }
      }
    }
    
    if ( typeof Xinha._lc_catalog[context] == 'undefined' )
    {
      Xinha._lc_catalog[context] = Xinha._loadlang(context,url);
    }

    var key;
    if ( typeof string == 'object' && string.key )
    {
      key = string.key;
    }
    else if ( typeof string == 'object' && string.string )
    {
      key = string.string;
    }
    else
    {
      key = string;
    }

    if ( typeof Xinha._lc_catalog[context][key] == 'undefined' )
    {
      if ( context=='Xinha' )
      {
        // Indicate it's untranslated
        if ( typeof string == 'object' && string.string )
        {
          ret = string.string;
        }
        else
        {
          ret = string;
        }
      }
      else 
      {
        // See if the Xinha context has it, if so use that as it must be 
        //  something more global probably
        if(typeof Xinha._lc_catalog['Xinha'][key] != 'undefined')
        {
          return  Xinha._lc(string, 'Xinha', replace);
        }
        
        // See if we have it in our OBSOLETE, if so use that
        //  it might be that this means we obsoleted something mistakenly
        //  because lc_parse_strings.php didn't find it
        else if( typeof Xinha._lc_catalog[context]['__OBSOLETE__'] != 'undefined'
         && typeof Xinha._lc_catalog[context]['__OBSOLETE__'][key] != 'undefined' )
        {
          ret = Xinha._lc_catalog[context]['__OBSOLETE__'][key];
          Xinha.debugMsg("Using a translation which is marked __OBSOLETE__, likely lc_parse_strings.php did not pick something up that it should have. " + context + ": " + key, 'warn');
        }
        
        // If string is not found and context is not Xinha, fall back to Xinha
        //  now and it will do some funky stuff (above) I'm not sure exactly
        //  what the purpose of it (passing an object as string) was
        else 
        {
          return Xinha._lc(string, 'Xinha', replace);
        }
      }
    }
    else
    {
      ret = Xinha._lc_catalog[context][key];
    }
  }

  if ( typeof string == 'object' && string.replace )
  {
    replace = string.replace;
  }
  if ( typeof replace != "undefined" )
  {
    for ( i in replace )
    {
      ret = ret.replace('$'+i, replace[i]);
    }
  }

  return ret;
};
/** Walks through the children of a given element and checks if any of the are visible (= not display:none)
 * @param {DomNode} el 
 * @returns {Boolean} 
 */
Xinha.hasDisplayedChildren = function(el)
{
  var children = el.childNodes;
  for ( var i = 0; i < children.length; i++ )
  {
    if ( children[i].tagName )
    {
      if ( children[i].style.display != 'none' )
      {
        return true;
      }
    }
  }
  return false;
};

/** Load a javascript file by inserting it in the HEAD tag and eventually call a function when loaded
 *
 *  Note that this method cannot be abstracted into browser specific files
 *  because this method LOADS the browser specific files.  Hopefully it should work for most
 *  browsers as it is.
 *
 * @param {String} url               Source url of the file to load
 * @param {Object} callback optional Callback function to launch once ready 
 * @param {Object} scope    optional Application scope for the callback function
 * @param {Object} bonus    optional Arbitrary object send as a param to the callback function
 */
Xinha._loadback = function(url, callback, scope, bonus)
{  
  if ( document.getElementById(url) )
  {
    return true;
  }
  var t = !Xinha.is_ie ? "onload" : 'onreadystatechange';
  var s = document.createElement("script");
  s.type = "text/javascript";
  s.src = url;
  s.id = url;
  if ( callback )
  {
    s[t] = function()
    {      
      if (Xinha.is_ie && (!/loaded|complete/.test(window.event.srcElement.readyState)))
      {
        return;
      }
      
      callback.call(scope ? scope : this, bonus);
      s[t] = null;
    };
  }
  document.getElementsByTagName("head")[0].appendChild(s);
  return false;
};

/** Xinha's main loading function (see NewbieGuide)
 * @param {Array} editor_names
 * @param {Xinha.Config} default_config
 * @param {Array} plugin_names
 * @returns {Object} An object that contains references to all created editors indexed by the IDs of the textareas 
 */
Xinha.makeEditors = function(editor_names, default_config, plugin_names)
{
  if (!Xinha.isSupportedBrowser) 
  {
    return;
  }
  
  if ( typeof default_config == 'function' )
  {
    default_config = default_config();
  }

  var editors = {};
  var textarea;
  for ( var x = 0; x < editor_names.length; x++ )
  {
    if ( typeof editor_names[x] == 'string' ) // the regular case, an id of a textarea
    {
      textarea = Xinha.getElementById('textarea', editor_names[x] );
      if (!textarea) // the id may be specified for a textarea that is maybe on another page; we simply skip it and go on
      {
        editor_names[x] = null;
        continue;
      }
    }
	 // make it possible to pass a reference instead of an id, for example from  document.getElementsByTagName('textarea')
    else if ( typeof editor_names[x] == 'object' && editor_names[x].tagName && editor_names[x].tagName.toLowerCase() == 'textarea' )
    {
      textarea =  editor_names[x];
      if ( !textarea.id ) // we'd like to have the textarea have an id
      {
        textarea.id = 'xinha_id_' + x;
      } 
    }
    var editor = new Xinha(textarea, Xinha.cloneObject(default_config));
    editor.registerPlugins(plugin_names);
    editors[textarea.id] = editor;
  }
  return editors;
};
/** Another main loading function (see NewbieGuide)
 * @param {Object} editors As returned by Xinha.makeEditors()
 */
Xinha.startEditors = function(editors)
{
  if (!Xinha.isSupportedBrowser) 
  {
    return;
  }
  
  for ( var i in editors )
  {
    if ( editors[i].generate )
    {
      editors[i].generate();
    }
  }
};
/** Registers the loaded plugins with the editor
 * @private
 * @param {Array} plugin_names
 */
Xinha.prototype.registerPlugins = function(plugin_names)
{
  if (!Xinha.isSupportedBrowser) 
  {
    return;
  }
  
  if ( plugin_names )
  {
    for ( var i = 0; i < plugin_names.length; i++ )
    {
      this.setLoadingMessage(Xinha._lc('Register plugin $plugin', 'Xinha', {'plugin': plugin_names[i]}));
      this.registerPlugin(plugin_names[i]);
    }
  }
};

/** Utility function to base64_encode some arbitrary data, uses the builtin btoa() if it exists (Moz) 
*  @param {String} input
*  @returns {String}
*/
Xinha.base64_encode = function(input)
{
  var keyStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
  var output = "";
  var chr1, chr2, chr3;
  var enc1, enc2, enc3, enc4;
  var i = 0;

  do
  {
    chr1 = input.charCodeAt(i++);
    chr2 = input.charCodeAt(i++);
    chr3 = input.charCodeAt(i++);

    enc1 = chr1 >> 2;
    enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
    enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
    enc4 = chr3 & 63;

    if ( isNaN(chr2) )
    {
      enc3 = enc4 = 64;
    }
    else if ( isNaN(chr3) )
    {
      enc4 = 64;
    }

    output = output + keyStr.charAt(enc1) + keyStr.charAt(enc2) + keyStr.charAt(enc3) + keyStr.charAt(enc4);
  } while ( i < input.length );

  return output;
};

/** Utility function to base64_decode some arbitrary data, uses the builtin atob() if it exists (Moz)
 *  @param {String} input
 *  @returns {String}
 */
Xinha.base64_decode = function(input)
{
  var keyStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
  var output = "";
  var chr1, chr2, chr3;
  var enc1, enc2, enc3, enc4;
  var i = 0;

  // remove all characters that are not A-Z, a-z, 0-9, +, /, or =
  input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");

  do
  {
    enc1 = keyStr.indexOf(input.charAt(i++));
    enc2 = keyStr.indexOf(input.charAt(i++));
    enc3 = keyStr.indexOf(input.charAt(i++));
    enc4 = keyStr.indexOf(input.charAt(i++));

    chr1 = (enc1 << 2) | (enc2 >> 4);
    chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
    chr3 = ((enc3 & 3) << 6) | enc4;

    output = output + String.fromCharCode(chr1);

    if ( enc3 != 64 )
    {
      output = output + String.fromCharCode(chr2);
    }
    if ( enc4 != 64 )
    {
      output = output + String.fromCharCode(chr3);
    }
  } while ( i < input.length );

  return output;
};
/** Removes a node from the DOM
 *  @param {DomNode} el The element to be removed
 *  @returns {DomNode} The removed element
 */
Xinha.removeFromParent = function(el)
{
  if ( !el.parentNode )
  {
    return;
  }
  var pN = el.parentNode;
  return pN.removeChild(el);
};
/** Checks if some element has a parent node
 *  @param {DomNode} el 
 *  @returns {Boolean}
 */
Xinha.hasParentNode = function(el)
{
  if ( el.parentNode )
  {
    // When you remove an element from the parent in IE it makes the parent
    // of the element a document fragment.  Moz doesn't.
    if ( el.parentNode.nodeType == 11 )
    {
      return false;
    }
    return true;
  }

  return false;
};
/** Determines if a given element has a given attribute.  IE<8 doesn't support it nativly */
Xinha.hasAttribute = function(el,at)
{
  if(typeof el.hasAttribute == 'undefined')
  {
    var node = el.getAttributeNode(at);
    return !!(node && (node.specified || node.nodeValue));
  }
  
  return el.hasAttribute(at);
}

/** Detect the size of visible area
 *  @param {Window} scope optional When calling from a popup window, pass its window object to get the values of the popup
 *  @returns {Object} Object with Integer properties x and y
 */
Xinha.viewportSize = function(scope)
{
  scope = (scope) ? scope : window;
  var x,y;
  if (scope.innerHeight) // all except Explorer
  {
    x = scope.innerWidth;
    y = scope.innerHeight;
  }
  else if (scope.document.documentElement && scope.document.documentElement.clientHeight)
  // Explorer 6 Strict Mode
  {
    x = scope.document.documentElement.clientWidth;
    y = scope.document.documentElement.clientHeight;
  }
  else if (scope.document.body) // other Explorers
  {
    x = scope.document.body.clientWidth;
    y = scope.document.body.clientHeight;
  }
  return {'x':x,'y':y};
};
/** Detect the size of the whole document
 *  @param {Window} scope optional When calling from a popup window, pass its window object to get the values of the popup
 *  @returns {Object} Object with Integer properties x and y
 */
Xinha.pageSize = function(scope)
{
  scope = (scope) ? scope : window;
  var x,y;
 
  var test1 = scope.document.body.scrollHeight; //IE Quirks
  var test2 = scope.document.documentElement.scrollHeight; // IE Standard + Moz Here quirksmode.org errs! 

  if (test1 > test2) 
  {
    x = scope.document.body.scrollWidth;
    y = scope.document.body.scrollHeight;
  }
  else
  {
    x = scope.document.documentElement.scrollWidth;
    y = scope.document.documentElement.scrollHeight;
  }  
  return {'x':x,'y':y};
};
/** Detect the current scroll position
 *  @param {Window} scope optional When calling from a popup window, pass its window object to get the values of the popup
 *  @returns {Object} Object with Integer properties x and y
 */
Xinha.prototype.scrollPos = function(scope)
{
  scope = (scope) ? scope : window;
  var x,y;
  if (typeof scope.pageYOffset != 'undefined') // all except Explorer
  {
    x = scope.pageXOffset;
    y = scope.pageYOffset;
  }
  else if (scope.document.documentElement && typeof document.documentElement.scrollTop != 'undefined')
    // Explorer 6 Strict
  {
    x = scope.document.documentElement.scrollLeft;
    y = scope.document.documentElement.scrollTop;
  }
  else if (scope.document.body) // all other Explorers
  {
    x = scope.document.body.scrollLeft;
    y = scope.document.body.scrollTop;
  }
  return {'x':x,'y':y};
};

/** Calculate the top and left pixel position of an element in the DOM.
 *  @param  {DomNode} element HTML Element
 *  @returns {Object} Object with Integer properties top and left
 */
 
Xinha.getElementTopLeft = function(element) 
{
  var curleft = 0;
  var curtop =  0;
  if (element.offsetParent) 
  {
    curleft = element.offsetLeft;
    curtop = element.offsetTop;
    while (element = element.offsetParent) 
    {
      curleft += element.offsetLeft;
      curtop += element.offsetTop;
    }
  }
  return { top:curtop, left:curleft };
};
/** Find left pixel position of an element in the DOM.
 *  @param  {DomNode} element HTML Element
 *  @returns {Integer} 
 */
Xinha.findPosX = function(obj)
{
  var curleft = 0;
  if ( obj.offsetParent )
  {
    return Xinha.getElementTopLeft(obj).left;
  }
  else if ( obj.x )
  {
    curleft += obj.x;
  }
  return curleft;
};
/** Find top pixel position of an element in the DOM.
 *  @param  {DomNode} element HTML Element
 *  @returns {Integer} 
 */
Xinha.findPosY = function(obj)
{
  var curtop = 0;
  if ( obj.offsetParent )
  {
    return Xinha.getElementTopLeft(obj).top;    
  }
  else if ( obj.y )
  {
    curtop += obj.y;
  }
  return curtop;
};

Xinha.createLoadingMessages = function(xinha_editors)
{
  if ( Xinha.loadingMessages || !Xinha.isSupportedBrowser ) 
  {
    return;
  }
  Xinha.loadingMessages = [];
  
  for (var i=0;i<xinha_editors.length;i++)
  {
    var e = typeof xinha_editors[i] == 'string' ? document.getElementById(xinha_editors[i]) : xinha_editors[i];
    if (!e)
    {
      continue;
    }
    
    Xinha.loadingMessages.push(Xinha.createLoadingMessage(e));
  }
};

Xinha.createLoadingMessage = function(textarea,text)
{ 
  if ( document.getElementById("loading_" + textarea.id) || !Xinha.isSupportedBrowser)
  {
    return;
  }
  // Create and show the main loading message and the sub loading message for details of loading actions
  // global element
  var loading_message = document.createElement("div");
  loading_message.id = "loading_" + textarea.id;
  loading_message.className = "loading";
  
  loading_message.style.left = (Xinha.findPosX(textarea) + textarea.offsetWidth / 2) - 106 +  'px';
  loading_message.style.top = (Xinha.findPosY(textarea) + textarea.offsetHeight / 2) - 50 +  'px';
  // main static message
  var loading_main = document.createElement("div");
  loading_main.className = "loading_main";
  loading_main.id = "loading_main_" + textarea.id;
  loading_main.appendChild(document.createTextNode(Xinha._lc("Loading in progress. Please wait!")));
  // sub dynamic message
  var loading_sub = document.createElement("div");
  loading_sub.className = "loading_sub";
  loading_sub.id = "loading_sub_" + textarea.id;
  text = text ? text : Xinha._lc("Loading Core");
  loading_sub.appendChild(document.createTextNode(text));
  loading_message.appendChild(loading_main);
  loading_message.appendChild(loading_sub);
  document.body.appendChild(loading_message);
  
  Xinha.freeLater(loading_message);
  Xinha.freeLater(loading_main);
  Xinha.freeLater(loading_sub);
  
  return loading_sub;
};

Xinha.prototype.setLoadingMessage = function(subMessage, mainMessage)
{
  if ( !document.getElementById("loading_sub_" + this._textArea.id) )
  {
    return;
  }
  document.getElementById("loading_main_" + this._textArea.id).innerHTML = mainMessage ? mainMessage : Xinha._lc("Loading in progress. Please wait!");
  document.getElementById("loading_sub_" + this._textArea.id).innerHTML = subMessage;
};

Xinha.setLoadingMessage = function(string)
{
  if (!Xinha.loadingMessages) 
  {
    return;
  }
  for ( var i = 0; i < Xinha.loadingMessages.length; i++ )
  {
    Xinha.loadingMessages[i].innerHTML = string;
  }
};

Xinha.prototype.removeLoadingMessage = function()
{
  if (document.getElementById("loading_" + this._textArea.id) )
  {
   document.body.removeChild(document.getElementById("loading_" + this._textArea.id));
  }
};

Xinha.removeLoadingMessages = function(xinha_editors)
{
  for (var i=0;i< xinha_editors.length;i++)
  {
     if (!document.getElementById(xinha_editors[i])) 
     {
       continue;
     }
     var main = document.getElementById("loading_" + document.getElementById(xinha_editors[i]).id);
     main.parentNode.removeChild(main);
  }
  Xinha.loadingMessages = null;
};

/** List of objects that have to be trated on page unload in order to work around the broken 
 * Garbage Collector in IE
 * @private
 * @see Xinha#freeLater
 * @see Xinha#free
 * @see Xinha#collectGarbageForIE
 */
Xinha.toFree = [];
/** Adds objects to Xinha.toFree 
 * @param {Object} object The object to free memory
 * @param (String} prop optional  The property to release
 * @private
 * @see Xinha#toFree
 * @see Xinha#free
 * @see Xinha#collectGarbageForIE
 */
Xinha.freeLater = function(obj,prop)
{
  Xinha.toFree.push({o:obj,p:prop});
};

/** Release memory properties from object
 * @param {Object} object The object to free memory
 * @param (String} prop optional The property to release
 * @private
 * @see Xinha#collectGarbageForIE
 * @see Xinha#free
 */
Xinha.free = function(obj, prop)
{
  if ( obj && !prop )
  {
    for ( var p in obj )
    {
      Xinha.free(obj, p);
    }
  }
  else if ( obj )
  {
    if ( prop.indexOf('src') == -1 ) // if src (also lowsrc, and maybe dynsrc ) is set to null, a file named "null" is requested from the server (see #1001)
    {
      try { obj[prop] = null; } catch(x) {}
    }
  }
};

/** IE's Garbage Collector is broken very badly.  We will do our best to 
 *   do it's job for it, but we can't be perfect. Takes all objects from Xinha.free and releases sets the null
 * @private
 * @see Xinha#toFree
 * @see Xinha#free
 */

Xinha.collectGarbageForIE = function() 
{  
  Xinha.flushEvents();   
  for ( var x = 0; x < Xinha.toFree.length; x++ )
  {
    Xinha.free(Xinha.toFree[x].o, Xinha.toFree[x].p);
    Xinha.toFree[x].o = null;
  }
};


// The following methods may be over-ridden or extended by the browser specific
// javascript files.


/** Insert a node at the current selection point. 
 * @param {DomNode} toBeInserted
 */

Xinha.prototype.insertNodeAtSelection = function(toBeInserted) { Xinha.notImplemented("insertNodeAtSelection"); };

/** Get the parent element of the supplied or current selection. 
 *  @param {Selection} sel optional selection as returned by getSelection
 *  @returns {DomNode}
 */
  
Xinha.prototype.getParentElement      = function(sel) { Xinha.notImplemented("getParentElement"); };

/**
 * Returns the selected element, if any.  That is,
 * the element that you have last selected in the "path"
 * at the bottom of the editor, or a "control" (eg image)
 *
 * @returns {DomNode|null}
 */
 
Xinha.prototype.activeElement         = function(sel) { Xinha.notImplemented("activeElement"); };

/** 
 * Determines if the given selection is empty (collapsed).
 * @param {Selection} sel Selection object as returned by getSelection
 * @returns {Boolean}
 */
 
Xinha.prototype.selectionEmpty        = function(sel) { Xinha.notImplemented("selectionEmpty"); };
/** 
 * Returns a range object to be stored 
 * and later restored with Xinha.prototype.restoreSelection()
 * @returns {Range}
 */

Xinha.prototype.saveSelection = function() { Xinha.notImplemented("saveSelection"); };

/** Restores a selection previously stored
 * @param {Range} savedSelection Range object as returned by Xinha.prototype.restoreSelection()
 */
Xinha.prototype.restoreSelection = function(savedSelection)  { Xinha.notImplemented("restoreSelection"); };

/**
 * Selects the contents of the given node.  If the node is a "control" type element, (image, form input, table)
 * the node itself is selected for manipulation.
 *
 * @param {DomNode} node 
 * @param {Integer} pos  Set to a numeric position inside the node to collapse the cursor here if possible. 
 */
Xinha.prototype.selectNodeContents    = function(node,pos) { Xinha.notImplemented("selectNodeContents"); };

/** Insert HTML at the current position, deleting the selection if any. 
 *  
 *  @param {String} html
 */
 
Xinha.prototype.insertHTML            = function(html) { Xinha.notImplemented("insertHTML"); };

/** Get the HTML of the current selection.  HTML returned has not been passed through outwardHTML.
 *
 * @returns {String}
 */
Xinha.prototype.getSelectedHTML       = function() { Xinha.notImplemented("getSelectedHTML"); };

/** Get a Selection object of the current selection.  Note that selection objects are browser specific.
 *
 * @returns {Selection}
 */
 
Xinha.prototype.getSelection          = function() { Xinha.notImplemented("getSelection"); };

/** Create a Range object from the given selection.  Note that range objects are browser specific.
 *  @see Xinha#getSelection
 *  @param {Selection} sel Selection object 
 *  @returns {Range}
 */
Xinha.prototype.createRange           = function(sel) { Xinha.notImplemented("createRange"); };

/** Different browsers have subtle differences in what they call a "keypress", we try and
 *  standardise them here.  For example, Firefox calls Tab a keypress (with keyCode 9)
 *  while WebKit does not record a keypress for the Tab key.
 * 
 *  Webkit does record a keydown for tab, but typically not a keyup as it's caused a 
 *  defocus by then, and there is no keypress.
 *  
 *  For the sake of Xinha, we will define keyPress something like...
 * 
 *   "Adding Characters"
 *      letters, numbers, space, punctuation, symbols, tab, enter
 * 
 *   "Deleting Characters"
 *      delete, backspace
 * 
 *   "Shortcuts and Other Useful Things"
 *      esc, ctrl-a, ctrl-b...
 * 
 * Note that in the interests of performance, we are not too strict about removing things
 *   that should not be keypresses if they don't happen frequently, like for example,
 *   Function Keys **might** get passed as onKeyPress, depending on browser, since it's
 *   rare and plugins probably won't be looking for them anyway, there's no sense
 *   wasting cycles looking for them.
 * 
 * If you are looking for something that is not "officially" a keypress as defined above
 *   use onKeyDown and you will get notifications for EVERY key down, even if it's for
 *   example a modifier like shift (eg ctrl-shift-a would give three keydowns), which is 
 *   why keypress is a better idea if you can do that.
 * 
 * Note also that you can listen to onShortCut to only hear about those if you want, 
 *   it gets a second argument of the shortcut key already decoded for you.
 * 
 * A useful key Tester is here:
 *    https://dvcs.w3.org/hg/d4e/raw-file/tip/key-event-test.html
 * 
 * A useful reference of DOM3 key names is here:
 *    https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key/Key_Values
 * 
 * @param KeyboardEvent
 * @return boolean
 */

Xinha.prototype.isKeyPressEvent = function(keyEvent)
{
  if(typeof Xinha.DOM3NotAKeyPress == 'undefined')
  {
    // The following regular expressions match KeyboardEvent.key for keys which should NOT be 
    //  regarded as a keypress
    //
    // Note that we don't actually test them all, currently only modifier, navigation and editing
    // are actually tested and excluded, the others wont' happen frequently enough to bother
    // with testing.  It's not typically a big deal if keys slip-through the net and issue 
    // a keypress as plugins will likely just skip it as an unknown key.
    //
    // See:   https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key/Key_Values
    
    Xinha.DOM3NotAKeyPress = {
      'modifier'    : /^(Alt|CapsLock|Control|F[0-9]+|FnLock|Hyper|Meta|NumLock|ScrollLock|Shift|Super|Symbol|OS|Scroll)/,
      'navigation'  : /^(Arrow|Page|End|Home|Left|Right|Up|Down)/,
      'editing'     : /^(Insert)/,
      'ui'          : /^(Accept|Again|Attn|Cancel|ContextMenu|Execute|Find|Finish|Help|Pause|Play|Props|Select|Zoom|Pause|Apps)/,
      'device'      : /^(Brightness|Eject|LoghOff|Power|Print|Hibernate|Standby|WakeUp)/,
      'ime'         : /^(AllCandidates|Alphanumeric|CodeInput|Compose|Convert|Dead|FinalMode|Group|ModeChange|NextCandidate|NonConvert|PreviousCandidate|Process|SingleCandidate|Multi|Nonconvert|AltGraph)/,
      'korean'      : /^(Hangul|Hanja|Junja)/,
      'japanese'    : /^(Eisu|Hanakaku|Hirigana|Kana|Kanji|Katakana|Romanji|Zenkaku|RomanCharacters|HalfWidth|FullWidth)/,
      'function'    : /^(F[0-9]+|Soft[0-9]+)/,
      'phone'       : /^(AppSwitch|Call|Camera|EndCall|GoBack|GoHome|HeadsetHook|LastNumber|Notification|Manner|Voice|Exit|MozHomeScreen)/,
      'multimedia'  : /^(Channel|Media|FastFwd)/,
      'audio'       : /^(Audio|Microphone|Volume)/,
      'tv'          : /^(TV|Live)/,
      'media'       : /^(AVR|Color|ClosedCaption|Dummer|DisplaySwap|DVR|Exit|Favorite|Guide|Info|InstantReplay|Link|List|Live|Lock|Media|Navigate|Next|OnDemand|Pairing|PinP|Play|Random|RcLow|Record|RfBypass|ScanChannels|ScreenMode|Settings|SplitScreen|STBInput|Subtitle|Teletext|VideoMode|Wink|ZoomToggle|Zoom)/,
      'speech'      : /^(Speech)/,
      'document'    : /^(Close|New|Open|Print|Save|Spellcheck|Mail)/,
      'application' : /^(Launch|MediaSelect|SelectMedia)/
    };
  }

  if(keyEvent.type == 'keypress')
  {
    // Detect some things that might come in as a press and should not be a press
    
    //  DOM3 Keys
    
    if(typeof keyEvent.key != 'undefined')
    {
      if(keyEvent.key == 'Unidentified')
      {
        // Some old Gecko versions called Shift-tab Unidentified !
        if(typeof keyEvent.keyCode != 'undefined' && keyEvent.keyCode == 9) return true;
        return false;
      }

      // Modifier keys
      if(Xinha.DOM3NotAKeyPress.modifier.test(keyEvent.key))   return false;
         
      // Navigation Keys
      if(Xinha.DOM3NotAKeyPress.navigation.test(keyEvent.key)) return false;
                
      // Editing Keys
      if(Xinha.DOM3NotAKeyPress.editing.test(keyEvent.key))    return false;
           
    }
    // Old DOM3 (only Safari and old Chrome)
    /* I could not find anything reported as a press that should not be in Safari 9.1 and that 
     * is as far back as I can easily test, my thought is that dropping straight through to the 
     * legacy keys should work fine so I have disabled this
     */ 
    else if( 0 && typeof keyEvent.keyIdentifier != 'undefined' && keyEvent.keyIdentifier.length)
    {
      var kI = parseInt(keyEvent.keyIdentifier.replace(/^U\+/,''),16);
      
    }
    // Legacy Keys
    else 
    {
      if(keyEvent.charCode == 0)
      {
        if((keyEvent.keyCode >= 37 && keyEvent.keyCode <= 40)) return false; // Arrows
           
        if(   keyEvent.keyCode == 45 // Insert
          ||  keyEvent.keyCode == 36 // Home
          ||  keyEvent.keyCode == 35 // End
          ||  keyEvent.keyCode == 33 // Page Up
          ||  keyEvent.keyCode == 34 // Page Dn
          ||  keyEvent.keyCode == 19 // Pause/Break
          ||  keyEvent.keyCode == 17 // Control
          ||  keyEvent.keyCode == 18 // Alt
          ||  keyEvent.keyCode == 20 // CapsLock
          ||  keyEvent.keyCode == 91 // OS
          ||  keyEvent.keyCode == 93 // ContextMenu          
        ) return false;
        
        if( keyEvent.keyCode >= 112 && keyEvent <= 123 ) return false; // F1 through F12
        if( keyEvent.keyCode == 0) return false; // Other things (like "LaunchApp1")
      }
    }
   
    // If it wasn't a bad thing we skiped, then a keypress is a keypress
    return true;
  }
  else if(keyEvent.type == 'keydown')
  {
    // Now we get into browser specifics, we want to return true 
    //  if the keydown event is for a key which should be reported as a press
    //  and is NOT reported as  a press (otherwise press would fire twice for it)
    return this.isKeyDownThatShouldGetButDoesNotGetAKeyPressEvent(keyEvent);
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
 * @param KeyboardEvent
 * @return boolean
 */

Xinha.prototype.isKeyDownThatShouldGetButDoesNotGetAKeyPressEvent = function(keyEvent)
{
  return false;
};

/** Determines if the given key event object represents a combination of CTRL-<key>,
 *  which for Xinha is a shortcut.  Note that CTRL-ALT-<key> is not a shortcut.
 *
 *  @param    {Event} keyEvent
 *  @returns  {mixed} Either the key representing the short cut (eg ctrl-b gives 'b'), or false
 */
 
Xinha.prototype.isShortCut = function(keyEvent)
{
  if(keyEvent.ctrlKey && !keyEvent.altKey && this.getKey(keyEvent).length == 1)
  {
    return this.getKey(keyEvent);
  }
  
  return false;
};

/** Return the character (as a string) of a keyEvent  - ie, press the 'a' key and
 *  this method will return 'a', press SHIFT-a and it will return 'A'.
 * 
 *  @param   {Event} keyEvent
 *  @returns {String}
 */
                                   
Xinha.prototype.getKey = function(keyEvent) { Xinha.notImplemented("getKey"); };

/** Return the HTML string of the given Element, including the Element.
 * 
 * @param {DomNode} element HTML Element
 * @returns {String}
 */
 
Xinha.getOuterHTML = function(element) { Xinha.notImplemented("getOuterHTML"); };

/** Get a new XMLHTTPRequest Object ready to be used. 
 *
 * @returns {XMLHTTPRequest}
 */

Xinha.getXMLHTTPRequestObject = function() 
{
  try
  {    
    if (typeof XMLHttpRequest != "undefined" && typeof XMLHttpRequest.constructor == 'function' ) // Safari's XMLHttpRequest is typeof object
    {
  	  return new XMLHttpRequest();
    }
  	else if (typeof ActiveXObject == "function")
  	{
  	  return new ActiveXObject("Microsoft.XMLHTTP");
  	}
  }
  catch(e)
  {
    Xinha.notImplemented('getXMLHTTPRequestObject');
  }
};
 
// Compatability - all these names are deprecated and will be removed in a future version
/** Alias of activeElement()
 * @see Xinha#activeElement
 * @deprecated
 * @returns {DomNode|null}
 */
Xinha.prototype._activeElement  = function(sel) { return this.activeElement(sel); };
/** Alias of selectionEmpty()
 * @see Xinha#selectionEmpty
 * @deprecated
 * @param {Selection} sel Selection object as returned by getSelection
 * @returns {Boolean}
 */
Xinha.prototype._selectionEmpty = function(sel) { return this.selectionEmpty(sel); };
/** Alias of getSelection()
 * @see Xinha#getSelection
 * @deprecated
 * @returns {Selection}
 */
Xinha.prototype._getSelection   = function() { return this.getSelection(); };
/** Alias of createRange()
 * @see Xinha#createRange
 * @deprecated
 * @param {Selection} sel Selection object
 * @returns {Range}
 */
Xinha.prototype._createRange    = function(sel) { return this.createRange(sel); };
HTMLArea = Xinha;

//what is this for? Do we need it?
Xinha.init();

if ( Xinha.ie_version < 8 )
{
  Xinha.addDom0Event(window,'unload',Xinha.collectGarbageForIE);
}
/** Print some message to Firebug, Webkit, Opera, or IE8 console
 * 
 * @param {String} text
 * @param {String} level one of 'warn', 'info', or empty 
 */
Xinha.debugMsg = function(text, level)
{
  if (typeof console != 'undefined' && typeof console.log == 'function') 
  {
    if (level && level == 'warn' && typeof console.warn == 'function') 
    {
      console.warn(text);
    }
    else 
      if (level && level == 'info' && typeof console.info == 'function') 
      {
        console.info(text);
      }
      else 
      {
        console.log(text);
      }
  }
  else if (typeof opera != 'undefined' && typeof opera.postError == 'function') 
  {
    opera.postError(text);
  }
};
Xinha.notImplemented = function(methodName) 
{
  throw new Error("Method Not Implemented", "Part of Xinha has tried to call the " + methodName + " method which has not been implemented.");
};
