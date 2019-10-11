// Charcounter for Xinha
// (c) Udo Schmal & L.N.Schaffrath NeueMedien
// Distributed under the same terms as HTMLArea itself.
// This notice MUST stay intact for use (see license.txt).

function CharCounter(editor) {
  this.editor = editor;
  this._Chars = 0;
  this._Words = 0;
  this._HTML = 0;
  this.onKeyPress = this.__onKeyPress;
}

Xinha.Config.prototype.CharCounter =
{
  'showChar': true, // show the characters count,
  'showWord': true, // show the words count,
  'showHtml': true, // show the exact html count
  'separator': ' | ', // separator used to join informations
  'maxHTML' : -1 // -1 for unlimited length, other number for limiting the length of the edited HTML
};

CharCounter._pluginInfo = {
  name          : "CharCounter",
  version       : "1.31",
  developer     : "Udo Schmal",
  developer_url : "http://www.schaffrath-neuemedien.de",
  sponsor       : "L.N.Schaffrath NeueMedien",
  sponsor_url   : "http://www.schaffrath-neuemedien.de",
  c_owner       : "Udo Schmal & L.N.Schaffrath NeueMedien",
  license       : "htmlArea"
};

CharCounter.prototype._lc = function(string) {
  return Xinha._lc(string, "CharCounter");
};


CharCounter.prototype.onGenerateOnce = function() {
  var self = this;
  if (this.charCount==null) {
      var charCount = self.editor.registerStatusWidget('CharCounter', ['wysiwyg']);
      this.charCount = charCount;
  }
};

CharCounter.prototype.__onKeyPress= function(ev) {
  if ((ev.keyCode != 8) && (ev.keyCode !=46)) { // not backspace & delete
    if (this.editor.config.CharCounter.maxHTML!=-1) {
      var contents = this.editor.getHTML();
      if (contents.length >= this.editor.config.CharCounter.maxHTML) {
        Xinha._stopEvent(ev);
        return true;
      }
    }
  }
}

CharCounter.prototype._updateCharCount= function() {
  var editor = this.editor;
  var cfg = editor.config;
  var contents = editor.getHTML();
  var string = new Array();
  if (cfg.CharCounter.showHtml) {
    string[string.length] = this._lc("HTML") + ": " + contents.length;
  }
  this._HTML = contents.length;
  if (cfg.CharCounter.showWord || cfg.CharCounter.showChar) {
    contents = contents.replace(/<\/?\s*!--[^-->]*-->/gi, "" );
    contents = contents.replace(/<(.+?)>/g, '');//Don't count HTML tags
    contents = contents.replace(/&nbsp;/gi, ' ');
    contents = contents.replace(/([\n\r\t])/g, ' ');//convert newlines and tabs into space
    contents = contents.replace(/(  +)/g, ' ');//count spaces only once
    contents = contents.replace(/&(.*);/g, ' ');//Count htmlentities as one keystroke
    contents = contents.replace(/^\s*|\s*$/g, '');//trim
  }
  if (cfg.CharCounter.showWord) {
    this._Words = 0;
    for (var x=0;x<contents.length;x++)
    {
      if (contents.charAt(x) == " " ) {this._Words++;}
    }
    if (this._Words >=1) { this._Words++; }
    string[string.length] = this._lc("Words") + ": " + this._Words ;
  }
  if (cfg.CharCounter.showChar) {
    string[string.length] = this._lc("Chars") + ": " + contents.length;
    this._Chars = contents.length;
  }
  this.charCount.innerHTML = string.join(cfg.CharCounter.separator);
};

CharCounter.prototype.onUpdateToolbar = function() {
  this.charCount.innerHTML = this._lc("... in progress");
  if(this._timeoutID) {
    window.clearTimeout(this._timeoutID);
  }
  var e = this;
  this._timeoutID = window.setTimeout(function() {e._updateCharCount();}, 1000);
};

