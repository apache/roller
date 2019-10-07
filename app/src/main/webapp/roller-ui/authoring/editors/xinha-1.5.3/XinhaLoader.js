var Xinha = {};

// Auto detect _editor_url if it's not set.
if (!window._editor_url) 
{
  (function() // wrap this in an ad-hoc function to avoid unecessary pollution of global namespace
  {
    // Because of the way the DOM is loaded, this is guaranteed to always pull our script tag.
    var scripts = document.getElementsByTagName('script');
    var this_script = scripts[scripts.length - 1];
  
    // We'll allow two ways to specify arguments.  We'll accept them in the
    // argument of the script, or we'll accept them embedded into our script tag.
    var args = this_script.src.split('?');
    args = args.length == 2 ? args[1].split('&') : '';
    for (var index = 0; index < args.length; ++index) 
    {
      var arg = args[index].split('=');
      if (arg.length == 2) 
      {
        switch (arg[0])
        {
          case 'lang':
          case 'icons':
          case 'skin':
          case 'url':
            window['_editor_' + arg[0]] = arg[1];
            break;
        }
      }
    }
    
    // We can grab the script innerHTML and execute that to cut down on script
    // tags.  Thanks John Resig!
    // http://ejohn.org/blog/degrading-script-tags/
    if (this_script.innerHTML.replace(/\s+/, '')) 
    {
      eval(this_script.innerHTML);
    }
    
    // Default values
    _editor_lang = window._editor_lang || 'en';
    
    // Chop off any query string.  Chop the filename off of the URL.
    _editor_url = window._editor_url || this_script.src.split('?')[0].split('/').slice(0, -1).join('/');

  })()
}
_editor_url = _editor_url.replace(/\x2f*$/, '/');

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
    Xinha.removeLoadingMessages(xinha_editors);  
    Xinha.createLoadingMessages(xinha_editors);  
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
    if (!document.getElementById(xinha_editors[i]))
    {
	  continue;
    }
    Xinha.loadingMessages.push(Xinha.createLoadingMessage(document.getElementById(xinha_editors[i])));
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
