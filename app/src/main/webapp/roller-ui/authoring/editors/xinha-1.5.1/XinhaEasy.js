/* This script is used to provide a super-simple loading method for Xinha
 *
 * For just "defaults", you can be as simple as
 * 
 * <script src="XinhaEasy.js"></script>
 * 
 * And it will convert all textareas on the page.
 * 
 * See examples/Newbie.html for a complete configuration example
 * 
 */

_editor_url   = typeof _editor_url   != 'undefined' ? _editor_url  : null;
_editor_lang  = typeof _editor_lang  != 'undefined' ? _editor_lang : 'en';
_editor_skin  = typeof _editor_skin  != 'undefined' ? _editor_skin : 'silva';
_editor_icons = typeof _editor_icons != 'undefined' ? _editor_icons : null;
_editor_css   = typeof _editor_css   != 'undefined' ? _editor_css : null;

xinha_init    = null;
xinha_editors = null;
xinha_config  = null;
xinha_toolbar = null;
xinha_plugins = null;

// Auto detect _editor_url if it's not set.
(function() // wrap this in an ad-hoc function to avoid unecessary pollution of global namespace
{
  // Because of the way the DOM is loaded, this is guaranteed to always pull our script tag.
  var scripts = document.getElementsByTagName('script');
  var this_script = scripts[scripts.length - 1];

  var xinha_options = null;
  
  // We can grab the script innerHTML and execute that to cut down on script
  // tags.  Thanks John Resig!
  // http://ejohn.org/blog/degrading-script-tags/  
  if (this_script.innerHTML.replace(/\s+/, '')) 
  {
    try // DEBUGGING: Comment out this line and the catch(e) below
    {
      eval(this_script.innerHTML);
      
      
      // Because the setup options might reference _editor_url, we treat that first...
      // Chop off any query string.  Chop the filename off of the URL 
      // Leave exactly one backslash at the end of _editor_url
      _editor_url = xinha_options._editor_url || this_script.src.split('?')[0].split('/').slice(0, -1).join('/').replace(/\x2f*$/, '/');
      
      // then reload the options...
      xinha_options = eval(this_script.innerHTML);
      delete xinha_options.editor_url;
    }
    catch(e) // DEBUGGING: Comment out this line and the try below
    {      
      if(typeof console != 'undefined' && typeof console.log == 'function')
      {
        var warn = typeof console.error == 'function' ? function(w){console.error(w);} : function(w){console.log(w);};
        warn(e);
        warn("Xinha: There is a problem loading your configuration data.");
        warn("Xinha: Check for common problems like a missing comma after a configuration section, or semicolons instead of commas after configuration sections.");
        warn("Xinha: If you get really stuck, comment the try and catch lines around here and the native error might be more useful.");        
        warn("Xinha: Default configuration is being used.");
      }
      else
      {
        throw e;
      }
      xinha_options = null;      
    }
  }
  
  if(_editor_url == null)
  {
    _editor_url = this_script.src.split('?')[0].split('/').slice(0, -1).join('/');
  }
  
  // Default values
  if(xinha_options != null)
  {
    for(var i in xinha_options)
    {
      if(xinha_options[i] !== null)
      {
        window[i] = xinha_options[i];
      }
    }
  }
})()

_editor_url = _editor_url.replace(/\x2f*$/, '/');

// It may be that we already have the XinhaCore.js loaded, if so, we don't need this stuff 
// and setting it would override the proper stuff.
if(typeof Xinha == 'undefined')
{
  var Xinha = {};

  Xinha.agt       = navigator.userAgent.toLowerCase();
  Xinha.is_ie    = ((Xinha.agt.indexOf("msie") != -1) && (Xinha.agt.indexOf("opera") == -1));
  Xinha.ie_version= parseFloat(Xinha.agt.substring(Xinha.agt.indexOf("msie")+5));
  Xinha.is_opera  = (Xinha.agt.indexOf("opera") != -1);
  Xinha.is_khtml  = (Xinha.agt.indexOf("khtml") != -1);
  Xinha.is_webkit  = (Xinha.agt.indexOf("applewebkit") != -1);
  Xinha.is_safari  = (Xinha.agt.indexOf("safari") != -1);
  Xinha.opera_version = navigator.appVersion.substring(0, navigator.appVersion.indexOf(" "))*1;
  Xinha.is_mac   = (Xinha.agt.indexOf("mac") != -1);
  Xinha.is_mac_ie = (Xinha.is_ie && Xinha.is_mac);
  Xinha.is_win_ie = (Xinha.is_ie && !Xinha.is_mac);
  Xinha.is_gecko  = (navigator.product == "Gecko" && !Xinha.is_safari); // Safari lies!
  Xinha.isRunLocally = document.URL.toLowerCase().search(/^file:/) != -1;
  Xinha.is_designMode = (typeof document.designMode != 'undefined' && !Xinha.is_ie); // IE has designMode, but we're not using it
  Xinha.isSupportedBrowser = Xinha.is_gecko || (Xinha.is_opera && Xinha.opera_version >= 9.1) || Xinha.ie_version >= 5.5 || Xinha.is_safari;

  Xinha.loadPlugins = function(plugins, callbackIfNotReady)
  {
    if ( !Xinha.isSupportedBrowser ) return;
    
    Xinha.loadStyle(typeof _editor_css == "string" ? _editor_css : "Xinha.css","XinhaCoreDesign");
    Xinha.createLoadingMessages(xinha_editors);
    var loadingMessages = Xinha.loadingMessages;
    Xinha._loadback(_editor_url + "XinhaCore.js",function () {
  //    Xinha.removeLoadingMessages(xinha_editors);  
  //    Xinha.createLoadingMessages(xinha_editors);  
      callbackIfNotReady() 
    });
    return false;
  }

  Xinha._loadback = function(Url, Callback, Scope, Bonus)
  {  
    var T = !Xinha.is_ie ? "onload" : 'onreadystatechange';
    var S = document.createElement("script");
    S.type = "text/javascript";
    S.src = Url;
    if ( Callback )
    {
      S[T] = function()
      {      
        if ( Xinha.is_ie && ( ! ( /loaded|complete/.test(window.event.srcElement.readyState) ) ) )
        {
          return;
        }
        
        Callback.call(Scope ? Scope : this, Bonus);
        S[T] = null;
      };
    }
    document.getElementsByTagName("head")[0].appendChild(S);
  };

  Xinha.getElementTopLeft = function(element) 
  {
    var curleft = 0;
    var curtop = 0;
    if (element.offsetParent) 
    {
      curleft = element.offsetLeft
      curtop = element.offsetTop
      while (element = element.offsetParent) 
      {
        curleft += element.offsetLeft
        curtop += element.offsetTop
      }
    }
    return { top:curtop, left:curleft };
  }

  // find X position of an element
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

  // find Y position of an element
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
  }

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
    
    return loading_sub;
  }

  Xinha.loadStyle = function(style, id)
  {
    var url = _editor_url || '';
    
    url += style;
  
    var head = document.getElementsByTagName("head")[0];
    var link = document.createElement("link");
    link.rel = "stylesheet";
    link.href = url;
    if (id) link.id = id;
    head.appendChild(link);
  };

  Xinha._lc = function(string) {return string;}

  Xinha._addEvent = function(el, evname, func) 
  {
    if ( document.addEventListener )
    {
      el.addEventListener(evname, func, true);
    }
    else
    {
      el.attachEvent("on" + evname, func);
    }
  }

  Xinha.addOnloadHandler = function (func)
  {
    // Dean Edwards/Matthias Miller/John Resig 
    // http://dean.edwards.name/weblog/2006/06/again/
    // IE part from jQuery
    
    
    var init = function ()
    {
      // quit if this function has already been called
      if (arguments.callee.done) return;
      // flag this function so we don't do the same thing twice
      arguments.callee.done = true;
      // kill the timer
      if (Xinha.onloadTimer) clearInterval(Xinha.onloadTimer);
      
      func.call();
    }
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
    else if (/WebKit/i.test(navigator.userAgent))
    {
      Xinha.onloadTimer = setInterval(function()
      {
        if (/loaded|complete/.test(document.readyState))
        {
          init(); // call the onload handler
        }
      }, 10);
    }
    else /* for Mozilla/Opera9 */
    {
      document.addEventListener("DOMContentLoaded", init, false);  
    }
  }
}

xinha_init = xinha_init ? xinha_init : function()
{
  // IE7 support for querySelectorAll. Supports multiple / grouped selectors
  // and the attribute selector with a "for" attribute. http://www.codecouch.com/
  // http://www.codecouch.com/2012/05/adding-document-queryselectorall-support-to-ie-7/
  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  if (!document.querySelectorAll) 
  {
    (function(d, s) {
      d=document, s=d.createStyleSheet();
      d.querySelectorAll = function(r, c, i, j, a) {
        a=d.all, c=[], r = r.replace(/\[for\b/gi, '[htmlFor').split(',');
        for (i=r.length; i--;) {
          s.addRule(r[i], 'k:v');
          for (j=a.length; j--;) a[j].currentStyle.k && c.push(a[j]);
          s.removeRule(0);
        }
        return c;
      }
    })();
  }

  if (!document.querySelector) 
  {
    document.querySelector = function(selectors) {
      var elements = document.querySelectorAll(selectors);
      return (elements.length) ? elements[0] : null;
    };
  }
  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


   /** STEP 1 ***************************************************************
   * First, specify the textareas that shall be turned into Xinhas. 
   * For each one add the respective id to the xinha_editors array.
   * I you want add more than on textarea, keep in mind that these 
   * values are comma seperated BUT there is no comma after the last value.
   * If you are going to use this configuration on several pages with different
   * textarea ids, you can add them all. The ones that are not found on the
   * current page will just be skipped.
   ************************************************************************/

  if(xinha_editors == null)
  {
    // BY default, change all textareas into Xinha
    xinha_editors = 'textarea';
  }
  
  if(typeof xinha_editors == 'string')
  {
    // A raw ID like we used to do
    if(document.getElementById(xinha_editors))
    {
      xinha_editors = [ document.getElementById(xinha_editors) ];
    }
    
    // Must be a selector, this is not supported for IE7 or lower!
    else
    {
      var selected = document.querySelectorAll(xinha_editors);
      xinha_editors = [ ];
      for(var i = 0; i < selected.length; i++)
      {
        xinha_editors.push(selected[i]);
      }
    }
  }
  
  /** STEP 2 ***************************************************************
   * Now, what are the plugins you will be using in the editors on this
   * page.  List all the plugins you will need, even if not all the editors
   * will use all the plugins.
   *
   * The list of plugins below is a good starting point, but if you prefer
   * a simpler editor to start with then you can use the following 
   * 
   * xinha_plugins = xinha_plugins ? xinha_plugins : [ ];
   *
   * which will load no extra plugins at all.
   ************************************************************************/

  function parse_plugins(xinha_plugins)
  {
    var remove_plugins = [ ];
        
    if(xinha_plugins === null)
    {
      xinha_plugins = 'common';
    }
    
    if(typeof xinha_plugins == 'string')
    {
      xinha_plugins = [ xinha_plugins ];
    }
    
    var load_plugins = xinha_plugins;
    xinha_plugins = [ ];
    for(var i = 0; i < load_plugins.length; i++)
    {
      // In case of { from: '/path/to/plugins', load: ['MootoolsFileManager'] }
      if(typeof load_plugins[i] == 'object' && typeof load_plugins[i].from == 'string')
      {
        // Resolve the "load" into a list of plugins
        var externs = parse_plugins(load_plugins[i].load);
        
        // MPush them into plugins as external plugin objects
        for(var ii = 0; ii < externs.length; ii++)
        {
          // In case you want to specify a non-default plugin file naming
          if(externs[ii].match(/\//))
          {
            xinha_plugins.push({ url: load_plugins[i].from + '/' + externs[ii] , plugin: externs[ii].replace(/.*\/([^.]+)\..*$/, '$1') });
          }
          else
          {
            xinha_plugins.push({ url: load_plugins[i].from + '/' + externs[ii] + '/' + externs[ii] + '.js', plugin: externs[ii]});
          }
        }
        continue;
      }
      
      // External plugin definition
      if(typeof load_plugins[i] == 'object' && typeof load_plugins[i].url == 'string')
      {
        xinha_plugins.push(load_plugins[i]);
        continue;
      }
      
      // In case of [ 'Plugin1', ['Plugin1', 'Plugin3'] ]
      if(typeof load_plugins[i] != 'string')
      {
        Array.prototype.push.apply(load_plugins, load_plugins[i]); 
        continue;
      }
      
      // Provide some simple plugin defintion shortcuts
      switch(load_plugins[i])
      {
        case 'loaded':
        {
          Array.prototype.push.apply(xinha_plugins,  ['CharacterMap', 'ContextMenu', 'FancySelects', 'SmartReplace', 'SuperClean', 'TableOperations', 'ListOperations', 'PreserveScripts', 'PreserveSelection', 'WebKitResize', 'Stylist', 'UnsavedChanges','QuickTag', 'PasteText', 'MootoolsFileManager', 'ListType', 'Linker', 'LangMarks', 'InsertWords', 'InsertSnppet2', 'InsertSmiley','InsertPagebreak', 'InsertNote', 'InsertAnchor', 'HtmlEntities', 'HorizontalRule', 'FindReplace', 'DefinitionList', 'CharCounter','Abbreviation'] );
        }
        break;
        
        case 'minimal':
        {
          Array.prototype.push.apply(xinha_plugins, [ 'FancySelects', 'ListOperations', 'PreserveSelection', 'WebKitResize' ]);
        } 
        break;
        
        case 'common':
        {
          Array.prototype.push.apply(xinha_plugins, [ 'CharacterMap', 'ContextMenu', 'FancySelects', 'SmartReplace', 'SuperClean', 'TableOperations', 'ListOperations', 'PreserveScripts', 'PreserveSelection', 'WebKitResize' ]);

          
        } 
        break;
        
        default:
          if(load_plugins[i].match(/^!/))
          {
            Array.prototype.push.apply(remove_plugins, parse_plugins(load_plugins[i].replace(/^!/, '')));
          }
          else
          {
            xinha_plugins.push(load_plugins[i]);
          }
          break;
      }
    }
    
    // Strip out the remove plugins, and duplicates
    var return_plugins = [ ];
    for(var i = 0; i < xinha_plugins.length; i++)
    {
      var OK = true;
      
      if(OK) for(var j = 0; j < remove_plugins.length; j++)
      {
        if(remove_plugins[j] == xinha_plugins[i]) { OK = false; break; }
      }
      
      if(OK) for(var j = 0; j < return_plugins.length; j++)
      {
        if(return_plugins[j] == xinha_plugins[i]) { OK = false; break; }
      }
      
      if(OK)
      {
        return_plugins.push(xinha_plugins[i]);
      }
    }
    xinha_plugins = return_plugins;
    
    return xinha_plugins;
  }
  
  xinha_plugins = parse_plugins(xinha_plugins);
  
  
         // THIS BIT OF JAVASCRIPT LOADS THE PLUGINS, NO TOUCHING  :)
         if(!Xinha.loadPlugins(xinha_plugins, xinha_init)) return;


  /** STEP 3 ***************************************************************
   * We create a default configuration to be used by all the editors.
   * If you wish to configure some of the editors differently this will be
   * done in step 5.
   *
   * If you want to modify the default config you might do something like this.
   *
   *   xinha_config = new Xinha.Config();
   *   xinha_config.width  = '640px';
   *   xinha_config.height = '420px';
   *
   *
   * For a list of the available configuration options, see:
   * http://trac.xinha.org/wiki/Documentation/ConfigVariablesList
   *
   *************************************************************************/

  var new_config      = new Xinha.Config();
  if(typeof xinha_config == 'function')
  {
    // If it doesn't return an object, that should still be fine
    //  due to references and such
    var returned_config = xinha_config(new_config);
    if(typeof returned_config == 'object')
    {
      new_config = returned_config;
    }     
  }
  xinha_config = new_config;

  Xinha.Config.prototype.setToolbar = function(xinha_toolbar)
  {
    var xinha_config = this;
    if(typeof xinha_toolbar == 'string' || xinha_toolbar === null)
    {
      switch(xinha_toolbar)
      {         
        case 'minimal+fonts':
          xinha_config.toolbar = [
              ["popupeditor"],
              ["separator","formatblock","fontname","fontsize","bold","italic","underline","strikethrough","superscript"],
              ["separator","forecolor","hilitecolor"],
              ["separator","justifyleft","justifycenter","justifyright"],
              ["separator","insertorderedlist","insertunorderedlist","outdent","indent"],
              ["separator","createlink","insertimage"],
            ];
        break;
        
        case 'minimal':
            xinha_config.toolbar = [
              ["popupeditor"],
              ["separator","bold","italic","underline","strikethrough","superscript"],
              ["separator","forecolor","hilitecolor"],
              ["separator","justifyleft","justifycenter","justifyright"],
              ["separator","insertorderedlist","insertunorderedlist","outdent","indent"],
              ["separator","createlink","insertimage"],
            ];
          break;     
          
        case 'supermini':
          xinha_config.toolbar = [
              
              ["separator","bold","italic","underline","strikethrough","superscript"],
              
            ];
          break;
      }
    }
    else if(typeof xinha_toolbar == 'object')
    {
      xinha_config.toolbar = xinha_toolbar;
    }
  }

  // Alias because I know I wil; type this differently
  Xinha.Config.prototype.setToolBar = Xinha.Config.prototype.setToolbar;
  
  xinha_config.setToolbar(xinha_toolbar);

  if(typeof xinha_stylesheet == 'string' && xinha_stylesheet.length )
  {
    xinha_config.pageStyleSheets = [ xinha_stylesheet ];
  }
  else if(typeof xinha_stylesheet == 'object')
  {
    xinha_config.pageStyleSheets = xinha_stylesheet ;
  }
   

  /** STEP 4 ***************************************************************
   * We first create editors for the textareas.
   *
   * You can do this in two ways, either
   *
   *   xinha_editors   = Xinha.makeEditors(xinha_editors, xinha_config, xinha_plugins);
   *
   * if you want all the editor objects to use the same set of plugins, OR;
   *
   *   xinha_editors = Xinha.makeEditors(xinha_editors, xinha_config);
   *   xinha_editors.myTextArea.registerPlugins(['Stylist']);
   *   xinha_editors.anotherOne.registerPlugins(['CSS','SuperClean']);
   *
   * if you want to use a different set of plugins for one or more of the
   * editors.
   ************************************************************************/

  if(typeof xinha_plugins_specific == 'function')
  {
    xinha_editors   = Xinha.makeEditors(xinha_editors, xinha_config);
    
    // To make it clearer for people provide a "remove" function on the array
    PluginsArray = function(arr)
    {
      PluginsArray.parentConstructor.call(this);
      for(var i = 0; i < arr.length; i++) this.push(arr[i]);
    };
    
    // Note important, extending must happen before adding more functions 
    // to the prototype.
    Xinha.extend(PluginsArray, Array);
    
    PluginsArray.prototype.remove = function(p)
    {
      if(typeof p == 'object')
      {
        for(var i = 0; i < p.length; i++)
        {
          this.remove(p[i]);
        }
        
        return this;
      }
      
      var idx = -1;
      for(var i = 0; i < this.length; i++)
      {
        
        if(p == this[i]) { idx = i; break; }        
        
      }
      
      if(idx >= 0)
      {
        this.splice(idx, 1);
      }
      
      return this;
    };
    
    PluginsArray.prototype.only    = function(p)
    {
      // Enpty ourself
      if(this.length)
      {
        this.splice(0,this.length);
      }
      
      // Add them in
      if(typeof p == 'string')
      {
        p = [ p ];
      }
      
      for(var i = 0; i < p.length; i++)
      {
        this.push(p[i]);
      }
    }
    
    for(var i in xinha_editors)
    {
      var specific_plugins = new PluginsArray(xinha_plugins);
      var plugins_returned = xinha_plugins_specific(specific_plugins, xinha_editors[i]._textArea, xinha_editors[i]);
      
      // Note that if they don't return anything, it will probably still work
      // due to references and such
      if(typeof plugins_returned == 'object')
      {
        specific_plugins = plugins_returned;
      }
      xinha_editors[i].registerPlugins(parse_plugins(specific_plugins));
    }
  }
  else
  {
    xinha_editors   = Xinha.makeEditors(xinha_editors, xinha_config, xinha_plugins);
  }
  

  /** STEP 5 ***************************************************************
   * If you want to change the configuration variables of any of the
   * editors,  this is the place to do that, for example you might want to
   * change the width and height of one of the editors, like this...
   *
   *   xinha_editors.myTextArea.config.width  = '640px';
   *   xinha_editors.myTextArea.config.height = '480px';
   *
   ************************************************************************/
  
  if(typeof xinha_config_specific == 'function')
  {
    for(var i in xinha_editors)
    {
      var returned_config =  xinha_config_specific(xinha_editors[i].config, xinha_editors[i]._textArea, xinha_editors[i]);
      
      // If the function doesn't return an object, it will stil work probably 
      // as xinha_config.XXX in the function will be working on a reference
      if(typeof returned_config == 'object')
      {
        xinha_editors[i].config = returned_config;
      }
    }
  }
  
  
  /** STEP 6 ***************************************************************
   * Finally we "start" the editors, this turns the textareas into
   * Xinha editors.
   ************************************************************************/

  Xinha.startEditors(xinha_editors);
}

Xinha.addOnloadHandler(xinha_init); // this executes the xinha_init function on page load
                                     // and does not interfere with window.onload properties set by other scripts



