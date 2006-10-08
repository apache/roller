// (c) dynarch.com 2003-2004
// Distributed under the same terms as HTMLArea itself.

function PopupWin(editor, title, handler, initFunction)
{
  this.editor = editor;
  this.handler = handler;
  var dlg = window.open("", "__ha_dialog", "toolbar=no,menubar=no,personalbar=no,width=600,height=600,left=20,top=40,scrollbars=no,resizable=no");
  this.window = dlg;
  var doc = dlg.document;
  this.doc = doc;
  var self = this;

  var base = document.baseURI || document.URL;
  if ( base && base.match(/(.*)\/([^\/]+)/) )
  {
    base = RegExp.$1 + "/";
  }
  // @fixme: why using a regex here and not a simple string test ?
  if ( typeof _editor_url != "undefined" && ! ( /^\//.test(_editor_url) ) && ! ( /http:\/\//.test(_editor_url) ) )
  {
    // _editor_url doesn't start with '/' which means it's relative
    // FIXME: there's a problem here, it could be http:// which
    // doesn't start with slash but it's not relative either.
    base += _editor_url;
  }
  else
  {
    base = _editor_url;
  }
 
  // @fixme: why using a regex here and not a simple string test ?
  if ( ! ( /\/$/.test(base) ) )
  {
    // base does not end in slash, add it now
    base += '/';
  }
  this.baseURL = base;

  doc.open();
  var html = "<html><head><title>" + title + "</title>\n";
  // html += "<base href='" + base + "htmlarea.js' />\n";
  html += "<style type='text/css'>@import url(" + _editor_url + "htmlarea.css);</style>\n";
  html += "<style type='text/css'>@import url(" + _editor_url + "skins/" + _editor_skin + "/skin.css);</style>\n";
  html += "</head>\n";
  html += "<body class='dialog popupwin' id='--HA-body'></body></html>";
  doc.write(html);
  doc.close();

  // sometimes I Hate Mozilla... ;-(
  function init2()
  {
    var body = doc.body;
    if ( !body )
    {
      setTimeout(init2, 25);
      return false;
    }
    dlg.title = title;
    doc.documentElement.style.padding = "0px";
    doc.documentElement.style.margin = "0px";
    var content = doc.createElement("div");
    content.className = "content";
    self.content = content;
    body.appendChild(content);
    self.element = body;
    initFunction(self);
    dlg.focus();
  }
  init2();
}

PopupWin.prototype.callHandler = function()
{
  var tags = ["input", "textarea", "select"];
  var params = {};
  for ( var ti = tags.length; --ti >= 0; )
  {
    var tag = tags[ti];
    var els = this.content.getElementsByTagName(tag);
    for ( var j = 0; j < els.length; ++j )
    {
      var el = els[j];
      var val = el.value;
      if ( el.tagName.toLowerCase() == "input" )
      {
        if ( el.type == "checkbox" )
        {
          val = el.checked;
        }
      }
      params[el.name] = val;
    }
  }
  this.handler(this, params);
  return false;
};

PopupWin.prototype.close = function()
{
  this.window.close();
};

PopupWin.prototype.addButtons = function()
{
  // @fixme: isn't self a predefined variable used to access self frame in most browsers ?
  //         if yes, then we break it here
  var self = this;
  var div = this.doc.createElement("div");
  this.content.appendChild(div);
  div.id = "buttons";
  div.className = "buttons";
  for ( var i = 0; i < arguments.length; ++i )
  {
    var btn = arguments[i];
    var button = this.doc.createElement("button");
    div.appendChild(button);
    button.innerHTML = HTMLArea._lc(btn, 'HTMLArea');
    switch (btn.toLowerCase())
    {
      case "ok":
        HTMLArea.addDom0Event(button, 'click',
          function()
          {
            self.callHandler();
            self.close();
            return false;
          }
        );
      break;
      case "cancel":
        HTMLArea.addDom0Event(button, 'click',
          function()
          {
            self.close();
            return false;
          }
        );
      break;
    }
  }
};

PopupWin.prototype.showAtElement = function()
{
  var self = this;
  // Mozilla needs some time to realize what's goin' on..
  setTimeout(function()
    {
      var w = self.content.offsetWidth + 4;
      var h = self.content.offsetHeight + 4;
      // size to content -- that's fuckin' buggy in all fuckin' browsers!!!
      // so that we set a larger size for the dialog window and then center
      // the element inside... phuck!

      // center...
      var el = self.content;
      var s = el.style;
      // s.width = el.offsetWidth + "px";
      // s.height = el.offsetHeight + "px";
      s.position = "absolute";
      s.left = parseInt((w - el.offsetWidth) / 2, 10) + "px";
      s.top = parseInt((h - el.offsetHeight) / 2, 10) + "px";
      if (HTMLArea.is_gecko)
      {
        self.window.innerWidth = w;
        self.window.innerHeight = h;
      }
      else
      {
        self.window.resizeTo(w + 8, h + 35);
      }
    },
    25);
};