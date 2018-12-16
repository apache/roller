<?php
  $LocalPluginPath = dirname(dirname(dirname(__FILE__))).DIRECTORY_SEPARATOR.'plugins';
  $LocalSkinPath = dirname(dirname(dirname(__File__))).DIRECTORY_SEPARATOR.'skins';
?>
<html>
<head>

  <!--------------------------------------:noTabs=true:tabSize=2:indentSize=2:--
    --  Xinha example menu.  This file is used by full_example.html within a
    --  frame to provide a menu for generating example editors using
    --  full_example-body.html, and full_example.js.
    --
    --  $HeadURL: http://svn.xinha.org/trunk/examples/files/ext_example-menu.php $
    --  $LastChangedDate: 2018-02-19 22:14:05 +1300 (Mon, 19 Feb 2018) $
    --  $LastChangedRevision: 1406 $
    --  $LastChangedBy: gogo $
    --------------------------------------------------------------------------->

  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
  <title>Example of Xinha</title>
  <link rel="stylesheet" href="full_example.css" />
  <style type="text/css">
    h1 {font: bold 22px "Staccato222 BT", cursive;}
    form, p {margin: 0px; padding: 0px;}
    label { display:block;}
  </style>
  <script language="JavaScript" type="text/javascript">
  var settings = null;
  settings = {
    width: "auto",
    height: "auto",
    sizeIncludesBars: true,
    sizeIncludesPanels: true,
    statusBar: true,
    htmlareaPaste: false,
    mozParaHandler: "best",
    getHtmlMethod: "DOMwalk",
    undoSteps: 20,
    undoTimeout: 500,
    changeJustifyWithDirection: false,
    fullPage: false,
    pageStyle: "",
    baseHref: null,
    expandRelativeUrl: true,
    stripBaseHref: true,
    stripSelfNamedAnchors: true,
    only7BitPrintablesInURLs: true,
    sevenBitClean: false,
    killWordOnPaste: true,
    makeLinkShowsTarget: true,
    flowToolbars: true,
    stripScripts: false,
    CharacterMapMode: "popup",
    ListTypeMode: "toolbar",
    showLoading: false,
    showChar: true,
    showWord: true,
    showHtml: true
  };


    function getCookieVal (offset) {
      var endstr = document.cookie.indexOf (";", offset);
      if (endstr == -1)
        endstr = document.cookie.length;
      return unescape(document.cookie.substring(offset, endstr));
    }

    function getCookie (name) {
      var arg = name + "=";
      var alen = arg.length;
      var clen = document.cookie.length;
      var i = 0;
      while (i < clen) {
        var j = i + alen;
        if (document.cookie.substring(i, j) == arg)
          return getCookieVal (j);
        i = document.cookie.indexOf(" ", i) + 1;
        if (i == 0) break;
      }
      return null;
    }

    function setCookie (name, value) {
      var argv = setCookie.arguments;
      var argc = setCookie.arguments.length;
      var expires = (argc > 2) ? argv[2] : null;
      var path = (argc > 3) ? argv[3] : null;
      var domain = (argc > 4) ? argv[4] : null;
      var secure = (argc > 5) ? argv[5] : false;
      document.cookie = name + "=" + escape (value) +
        ((expires == null) ? "" : ("; expires=" + expires.toGMTString())) +
        ((path == null) ? "" : ("; path=" + path)) +
        ((domain == null) ? "" : ("; domain=" + domain)) +
        ((secure == true) ? "; secure" : "");
    }

  function _onResize() {
    var sHeight;
    if (window.innerHeight) sHeight = window.innerHeight;
    else if (document.body && document.body.offsetHeight) sHeight = document.body.offsetHeight;
    else return;
    if (sHeight>300) {
      sHeight = sHeight - 285;
    } else {
      sHeight = 30
    }
    var div = document.getElementById("div_plugins");
    div.style.height = sHeight + "px";
  }

function Dialog(url, action, init) {
	if (typeof init == "undefined") {
		init = window;	// pass this window object by default
	}
	if (typeof window.showModalDialog == 'function')
	{
		Dialog._return = action;
		var r = window.showModalDialog(url, init, "dialogheight=10;dialogheight=10;scroll=yes;resizable=yes");
	}
	else
	{
		Dialog._geckoOpenModal(url, action, init);
	}
};

Dialog._parentEvent = function(ev) {
  setTimeout( function() { if (Dialog._modal && !Dialog._modal.closed) { Dialog._modal.focus() } }, 50);
  if (Dialog._modal && !Dialog._modal.closed) {
    agt = navigator.userAgent.toLowerCase();
    is_ie = ((agt.indexOf("msie") != -1) && (agt.indexOf("opera") == -1));
    if (is_ie) {
      ev.cancelBubble = true;
      ev.returnValue = false;
    } else {
      ev.preventDefault();
      ev.stopPropagation();
    }
  }
};


// should be a function, the return handler of the currently opened dialog.
Dialog._return = null;

// constant, the currently opened dialog
Dialog._modal = null;

// the dialog will read it's args from this variable
Dialog._arguments = null;

Dialog._geckoOpenModal = function(url, action, init) {
  var dlg = window.open(url, "hadialog",
            "toolbar=no,menubar=no,personalbar=no,width=10,height=10," +
            "scrollbars=no,resizable=yes,modal=yes,dependable=yes");
  Dialog._modal = dlg;
  Dialog._arguments = init;

  // capture some window's events
  function capwin(w) {
//		Xinha._addEvent(w, "click", Dialog._parentEvent);
//		Xinha._addEvent(w, "mousedown", Dialog._parentEvent);
//		Xinha._addEvent(w, "focus", Dialog._parentEvent);
  };
  // release the captured events
  function relwin(w) {
//		Xinha._removeEvent(w, "click", Dialog._parentEvent);
//		Xinha._removeEvent(w, "mousedown", Dialog._parentEvent);
//		Xinha._removeEvent(w, "focus", Dialog._parentEvent);
  };
  capwin(window);
  // capture other frames
  for (var i = 0; i < window.frames.length; capwin(window.frames[i++]));
  // make up a function to be called when the Dialog ends.
  Dialog._return = function (val) {
    if (val && action) {
      action(val);
    }
    relwin(window);
    // capture other frames
    for (var i = 0; i < window.frames.length; relwin(window.frames[i++]));
    Dialog._modal = null;
  };
};

  function fExtended () {
    Dialog("Extended.html", function(param) {
      if(param) {
        settings.width = param["width"];
        settings.height = param["height"];
        settings.sizeIncludesBars = (param["sizeIncludesBars"]=="true");
        settings.sizeIncludesPanels = (param["sizeIncludesPanels"]=="true");
        settings.statusBar = (param["statusBar"]=="true");
        settings.htmlareaPaste = (param["htmlareaPaste"]=="true");
        settings.mozParaHandler = param["mozParaHandler"];
        settings.getHtmlMethod = param["getHtmlMethod"];
        settings.undoSteps = param["undoSteps"];
        settings.undoTimeout = param["undoTimeout"];
        settings.changeJustifyWithDirection = (param["changeJustifyWithDirection"]=="true");
        settings.fullPage = (param["fullPage"]=="true");
        settings.pageStyle = param["pageStyle"];
        settings.baseHref = param["baseHref"];
        settings.expandRelativeUrl = (param["expandRelativeUrl"]=="true");
        settings.stripBaseHref = (param["stripBaseHref"]=="true");
        settings.stripSelfNamedAnchors = (param["stripSelfNamedAnchors"]=="true");
        settings.only7BitPrintablesInURLs = (param["only7BitPrintablesInURLs"]=="true");
        settings.sevenBitClean = (param["sevenBitClean"]=="true");
        settings.killWordOnPaste = (param["killWordOnPaste"]=="true");
        settings.makeLinkShowsTarget = (param["makeLinkShowsTarget"]=="true");
        settings.flowToolbars = (param["flowToolbars"]=="true");
        settings.stripScripts = (param["stripScripts"]=="true");
        settings.CharacterMapMode = param["CharacterMapMode"];
        settings.ListTypeMode = param["ListTypeMode"];
        settings.showLoading = (param["showLoading"]=="true");
        settings.showChar = (param["showChar"]=="true");
        settings.showWord = (param["showWord"]=="true");
        settings.showHtml = (param["showHtml"]=="true");
      }
    }, settings );
  }

  function init(){
    var co = getCookie('co_ext_Xinha');
    if(co!=null){
      var co_values;
      var co_entries = co.split('###');
      for (var i in co_entries) {
        co_values = co_entries[i].split('=');
        if(co_values[0]=='plugins') {
          for(var x = 0; x < document.forms[0].plugins.length; x++) {
            if(co_values[1].indexOf(document.forms[0].plugins[x].value)!=-1) {
              document.forms[0].plugins[x].checked = true;
            }
          }
        } else if(co_values[0]!='') {
          document.getElementById(co_values[0]).value = co_values[1];
        }
      }
    }
    _onResize();
  };

  window.onresize = _onResize;
  window.onload = init;
  </script>
</head>

<body>
  <form action="ext_example-body.php" target="body" name="fsettings" id="fsettings">
  <h1>Xinha Example</h1>
    <fieldset>
      <legend>Settings</legend>
        <label>
          Number of Editors: <input type="text" name="num" id="num" value="1" style="width:25;" maxlength="2"/>
        </label>
        <label>
          Language:
          <select name="lang" id="lang">
          <option value="en">English</option>
          <option value="en">--- By Name ---</option>
<option value="ch">Chinese (ch)</option>
<option value="cz">Czech</option>
<option value="da">Danish</option>
<option value="de">German</option>
<option value="ee">Ewe</option>
<option value="el">Greek</option>
<option value="es">Spanish</option>
<option value="eu">Basque</option>
<option value="fa">Persian (Farsi)</option>
<option value="fi">Finnish</option>
<option value="fr_ca">French (Canada)</option>
<option value="fr">French</option>
<option value="zn_ch">Chinese (zn_ch)</option>
<option value="he">Hebrew</option>
<option value="hu">Hungarian</option>
<option value="it">Italian</option>
<option value="ja">Japanese</option>
<option value="lt">Lithuanian</option>
<option value="lv">Latvian</option>
<option value="nb">Norwegian</option>
<option value="nl">Dutch</option>
<option value="pl">Polish</option>
<option value="pt_br">Portuguese (Brazil)</option>
<option value="ro">Romanian</option>
<option value="ru">Russian</option>
<option value="sh">Serbo-Croatian</option>
<option value="si">Slovenian</option>
<option value="sr">Serbian</option>
<option value="vn">Swedish</option>
<option value="th">Thai</option>
<option value="tr">Turkish</option>
<option value="vn">Vietnamese</option>

          <option value="en">--- By Code ---</option>
<option value="ch">ch</option>
<option value="cz">cz</option>
<option value="da">da</option>
<option value="de">de</option>
<option value="ee">ee</option>
<option value="el">el</option>
<option value="es">es</option>
<option value="eu">eu</option>
<option value="fa">fa</option>
<option value="fi">fi</option>
<option value="fr_ca">fr_ca</option>
<option value="fr">fr</option>
<option value="zn_ch">zn_ch</option>
<option value="he">he</option>
<option value="hu">hu</option>
<option value="it">it</option>
<option value="ja">ja</option>
<option value="lt">lt</option>
<option value="lv">lv</option>
<option value="nb">nb</option>
<option value="nl">nl</option>
<option value="pl">pl</option>
<option value="pt_br">pt_br</option>
<option value="ro">ro</option>
<option value="ru">ru</option>
<option value="sh">sh</option>
<option value="si">si</option>
<option value="sr">sr</option>
<option value="sv">sv</option>
<option value="th">th</option>
<option value="tr">tr</option>
<option value="vn">vn</option>
          </select>
        </label>
        <label>
          Skin:
          <select name="skin" id="skin">
          <option value="">-- no skin --</option>
<?php
  $d = @dir($LocalSkinPath);
  while (false !== ($entry = $d->read()))  //not a dot file or directory
  { if(substr($entry,0,1) != '.')
    { echo '<option value="' . $entry . '"> ' . $entry . '</option>'."\n";
    }
  }
  $d->close();
?>
          </select>
        </label>
        <label>
          Mode:
          <select name="DocType">
            <option value="standards">Standards</option>
            <option value="almost">Almost Standards</option>
            <option value="quirks">Quirks</option>
            
          </select>
        </label>
        <label>
          Icons:
            <select name="icons">
              <option value="">Default/Skin</option>
              <option value="Classic">Classic</option>
              <option value="Crystal">Crystal</option>
              <option value="Tango">Tango</option>
            </select>
        </label>
        <center><input type="button" value="extended Settings" onClick="fExtended();" /></center>

    </fieldset>
    <fieldset>
      <legend>Plugins</legend>
      <div id="div_plugins" style="width:100%; overflow:auto">
<?php
  $d = @dir($LocalPluginPath);
  $dir_array = array();
  while (false !== ($entry = $d->read()))  //not a dot file or directory
  { if(substr($entry,0,1) != '.')
    { $dir_array[] = $entry;
    }
  }
  $d->close();
  sort($dir_array);
  foreach ($dir_array as $entry)
  { echo '<label><input type="checkbox" name="plugins" id="plugins" value="' . $entry . '"> ' . $entry . '</label>'."\n";
  }

?>
      </div>
    </fieldset>
    <center><button type="submit">reload editor</button></center>

        <textarea id="myTextarea0" style="display:none">
          <p>Lorem ipsum dolor sit amet, consectetuer adipiscing elit.
          Aliquam et tellus vitae justo varius placerat. Suspendisse iaculis
          velit semper dolor. Donec gravida tincidunt mi. Curabitur tristique
          ante elementum turpis. Aliquam nisl. Nulla posuere neque non
          tellus. Morbi vel nibh. Cum sociis natoque penatibus et magnis dis
          parturient montes, nascetur ridiculus mus. Nam nec wisi. In wisi.
          Curabitur pharetra bibendum lectus. </p>
<ul>
  <li><a href="http://test.com/john%27s">test</a></li>
  <li><a href="/absolute/test.html">test</a></li>
  <li><a href="/test.html">test</a></li>
  <li><a href="test.html">test</a></li>
  <li><a href="../test.html">test</a></li>
  <li></li>
  <li><a href="/absolute/test.html">test</a></li>
  <li><a href="/absolute/./test.html">test</a></li>
  <li><a href="/absolute/./../test.html">test</a></li>
  <li><a href="/absolute/.///./../test.html">test</a></li>
  <li></li>
  <li><a href="relative/test.html">test</a></li>
  <li><a href="relative/./test.html">test</a></li>
  <li><a href="relative/./../test.html">test</a></li>
  <li><a href="relative/.///./../test.html">test</a></li>
  <li></li>
  <li><a href="../test.html">test</a></li>
  <li><a href=".././test.html">test</a></li>
  <li><a href=".././../test.html">test</a></li>
  <li><a href=".././//./../test.html">test</a></li>
  <li></li>
  <li><a href="./relative/test.html">test</a></li>
  <li><a href="./relative/./test.html">test</a></li>
  <li><a href="./relative/./../test.html">test</a></li>
  <li><a href="./relative/.///./../test.html">test</a></li>
  <li></li>
  <li><a href="./relative/test.html?.///./../../../abc/def">test</a></li>
  <li><a href="./relative/./test.html#123/45/../../..///./ddd">test</a></li>
  <li><a href="./relative/./../test.html?.///./../../../abc/def">test</a></li>
  <li><a href="./relative/.///./../test.html#123/45/../../..///./ddd">test</a></li>
  <li></li>
  <li><a href="../../../../../../../../../../.././relative/test.html?.///./../../../abc/def">test</a></li>
  <li><a href="../../.././relative/./test.html#123/45/../../..///./ddd">test</a></li>
  <li><a href="../../../.././relative/./../test.html?.///./../../../abc/def">test</a></li>
  <li><a href="../../../.././relative/.///./../test.html#123/45/../../..///./ddd">test</a></li>
  
</ul>
          <ul>
            <li> Phasellus et massa sed diam viverra semper.  </li>
            <li> Mauris tincidunt felis in odio.              </li>
            <li> Nulla placerat nunc ut pede.                 </li>
            <li> Vivamus ultrices mi sit amet urna.           </li>
            <li> Quisque sed augue quis nunc laoreet volutpat.</li>
            <li> Nunc sit amet metus in tortor semper mattis. </li>
          </ul>
        </textarea>

  </form>
  <script type="text/javascript">
    top.frames["body"].location.href = document.location.href.replace(/ext_example-menu\.php.*/, 'ext_example-body.php')
    var _oldSubmitHandler = null;
    if (document.forms[0].onsubmit != null) {
      _oldSubmitHandler = document.forms[0].onsubmit;
    }
    function frame_onSubmit(){
      var thenewdate = new Date ();
      thenewdate.setTime(thenewdate.getTime() + (5*24*60*60*1000));
      var co_value = 'skin=' + document.getElementById('skin').options[document.getElementById('skin').selectedIndex].value + '###' +
                     'lang=' + document.getElementById('lang').options[document.getElementById('lang').selectedIndex].value + '###' +
                     'num=' + document.getElementById('num').value + '###';
      var s_value='';
      for(var x = 0; x < document.forms[0].plugins.length; x++) {
        if(document.forms[0].plugins[x].checked)
          s_value += document.forms[0].plugins[x].value + '/';
      }
      if(s_value!='') {
        co_value += 'plugins=' + s_value + '###'
      }
      setCookie('co_ext_Xinha', co_value, thenewdate);
      if (_oldSubmitHandler != null) {
        _oldSubmitHandler();
      }
    }
    document.forms[0].onsubmit = frame_onSubmit;
  </script>

</body>
</html>
