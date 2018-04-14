// Abbreviation plugin for Xinha
// Implementation by Udo Schmal & Schaffrath NeueMedien
// Original Author - Udo Schmal
//
// (c) Udo Schmal & Schaffrath NeueMedien 2004
// Distributed under the same terms as HTMLArea itself.
// This notice MUST stay intact for use (see license.txt).

function Abbreviation(editor) {
  this.editor = editor;
  var cfg = editor.config;
  var self = this;

  // register the toolbar buttons provided by this plugin
  cfg.registerButton({
    id       : "abbreviation",
    tooltip  : Xinha._lc("Abbreviation", "Abbreviation"),
    image    : editor.imgURL("ed_abbreviation.gif", "Abbreviation"),
    textMode : false,
    action   : function(editor) {
                 self.show();
               }
  });
  cfg.addToolbarElement("abbreviation", "inserthorizontalrule", 1);
}

Abbreviation._pluginInfo = {
  name          : "Abbreviation",
  version       : "1.0",
  developer     : "Udo Schmal",
  developer_url : "",
  sponsor       : "L.N.Schaffrath NeueMedien",
  sponsor_url   : "http://www.schaffrath-neuemedien.de/",
  c_owner       : "Udo Schmal & Schaffrath-NeueMedien",
  license       : "htmlArea"
};

// Fills in the text field if the acronym is either known (i.e., in the [lang].js file)
// or if we're editing an existing abbreviation.
Abbreviation.prototype.fillText = function() {
  var editor = this.editor;
  var text = this.html.toUpperCase();
  var abbr = Xinha.getPluginDir(this.constructor.name) + "/abbr/" + _editor_lang + ".js";
  var abbrData = Xinha._geturlcontent(abbr);

  if (abbrData) {
    eval('abbrObj = ' + abbrData);
    if (abbrObj != "") {
      var dest = this.dialog.getElementById("title");
      dest.value = this.title || "";
      for (var i in abbrObj) {
        same = (i.toUpperCase()==text);
        if (same)
          dest.value = abbrObj[i];
      }
    }
  }
}

Abbreviation.prototype.onGenerateOnce = function(editor) {
  this.editor.addEditorStylesheet(Xinha.getPluginDir('Abbreviation') + '/abbreviation.css');
  this.methodsReady = true; //remove this?
  var self = Abbreviation;
  Xinha._getback(Xinha.getPluginDir('Abbreviation') + '/dialog.html', function(getback) { self.html = getback; self.dialogReady = true; });
};

Abbreviation.prototype.OnUpdateToolbar = function(editor) {
  if (!(Abbreviation.dialogReady && Abbreviation.methodsReady))
  {
    this.editor._toolbarObjects.Abbreviation.state("enabled", false);
  }
  else this.onUpdateToolbar = null;
}

Abbreviation.prototype.prepareDialog = function(html) {
  var self = this;
  var editor = this.editor;
  var dialog = this.dialog = new Xinha.Dialog(editor, Abbreviation.html, 'Xinha', {width: 260, height:140});

  dialog.getElementById('ok').onclick = function() { self.apply(); };
  dialog.getElementById('delete').onclick = function() { self.ondelete(); };
  dialog.getElementById('cancel').onclick = function() { self.dialog.hide(); };
  
  this.dialogReady = true;
}

Abbreviation.prototype.show = function(editor) {
  var editor = this.editor;
  this.html = editor.getSelectedHTML();
  if (!this.dialog) this.prepareDialog();
  var self = this;
  var doc = editor._doc;
  var sel  = editor._getSelection();
  var range  = editor._createRange(sel);
  var abbr = editor._activeElement(sel);
  
  if(!(abbr != null && abbr.tagName.toLowerCase() == "abbr")) {
    abbr = editor._getFirstAncestor(sel, 'abbr');
  }
  this.abbr = abbr;
  
  if (abbr) this.title = abbr.title;
  this.fillText();

  this.dialog.getElementById("inputs").onsubmit = function() {
    self.apply();
    return false;
  }

  this.dialog.show();
  this.dialog.getElementById("title").select();
}

Abbreviation.prototype.apply = function() {
  var editor = this.editor;
  var doc = editor._doc;
  var abbr = this.abbr;
  var html = this.html;
  var param = this.dialog.hide();

  if ( param ) {
    var title = param["title"];
    if (title == "" || title == null) {
      if (abbr) {
        var child = abbr.innerHTML;
        abbr.parentNode.removeChild(abbr);
        editor.insertHTML(child); // FIX: This doesn't work in Safari 3 
      }
      return;
    }
    try {
      if (!abbr) {
        abbr = doc.createElement("abbr");
        abbr.title = title;
        abbr.innerHTML = html;
        if (Xinha.is_ie) {
          range.pasteHTML(abbr.outerHTML);
        } else {
          editor.insertNodeAtSelection(abbr);
        }
      } else {
        abbr.title = title;
      }
    }
    catch (e) { }
  }
}


Abbreviation.prototype.ondelete = function() {
  this.dialog.getElementById('title').value = "";
  this.apply();
}