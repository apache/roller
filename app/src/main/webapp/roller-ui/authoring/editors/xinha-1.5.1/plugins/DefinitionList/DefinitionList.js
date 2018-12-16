// DefinitionList plugin for Xinha
// Distributed under the same terms as Xinha itself.
// This notice MUST stay intact for use (see license.txt).


function DefinitionList(editor) {
  this.editor = editor;
  var cfg = editor.config;
  var bl = DefinitionList.btnList;
  var self = this;
  // register the toolbar buttons provided by this plugin
  for (var i = 0; i < bl.length; ++i) {
    var btn = bl[i];
    if (!btn) {
      continue;
    }
    var id = btn[0];
    cfg.registerButton(id, this._lc(btn[1]), editor.imgURL("ed_" + btn[0] + ".gif", "DefinitionList"), false,
      function(editor, id) {
        // dispatch button press event
        self.buttonPress(editor, id);
      });
  }

  // We'll insert the buttons next to the UL/OL buttons, if they exist.
  // If neither of those buttons exists, addToolbarElement puts our buttons
  // at the beginning of the toolbar, which is good enough.
  cfg.addToolbarElement("dl", ["insertunorderedlist", "insertorderedlist"], 1);
  cfg.addToolbarElement("dt", "dl", 1);
  cfg.addToolbarElement("dd", "dt", 1);
}

DefinitionList._pluginInfo = {
  name          : "DefinitionList",
  version       : "1.1",
  developer     : "Udo Schmal",
  developer_url : "",
  c_owner       : "Udo Schmal",
  license       : "htmlArea"
};

// the list of buttons added by this plugin
DefinitionList.btnList = [         // for lc_parse_strings.php
  ["dl", "definition list"],       // Xinha._lc('definition list', 'DefinitionList'); 
  ["dt", "definition term"],       // Xinha._lc('definition term', 'DefinitionList'); 
  ["dd", "definition description"] // Xinha._lc('definition description', 'DefinitionList'); 
  ];

DefinitionList.prototype._lc = function(string) {
  return Xinha._lc(string, 'DefinitionList');
};

DefinitionList.prototype.onGenerate = function() {
  this.editor.addEditorStylesheet(Xinha.getPluginDir('DefinitionList') + '/definition-list.css');
};

DefinitionList.prototype.buttonPress = function(editor,button_id) {
  var pe;
  var dx;
  if (button_id=='dl') { //definition list
    pe = editor.getParentElement();
    if( pe.tagName.toLowerCase() != 'body' ) {
      while (pe.parentNode.tagName.toLowerCase() != 'body') {
        pe = pe.parentNode;
      }
    }
    dx = editor._doc.createElement(button_id);
    dx.innerHTML = '&nbsp;';
    if( pe.tagName.toLowerCase() == 'body' ) {
      pe.appendChild(dx);
    }else if(pe.parentNode.lastChild==pe) {
      pe.parentNode.appendChild(dx);
    }else{
      pe.parentNode.insertBefore(dx,pe.nextSibling);
    }
  } else if ((button_id=='dt')||(button_id=='dd')) { //definition term or description
    pe = editor.getParentElement();
    while (pe && (pe.nodeType == 1) && (pe.tagName.toLowerCase() != 'body')) {
      if(pe.tagName.toLowerCase() == 'dl') {
        dx = editor._doc.createElement(button_id);
        dx.innerHTML = '&nbsp;';
        pe.appendChild(dx);
        break;
      }else if((pe.tagName.toLowerCase() == 'dt')||(pe.tagName.toLowerCase() == 'dd')){
        dx = editor._doc.createElement(button_id);
        dx.innerHTML = '&nbsp;';
        if(pe.parentNode.lastChild==pe) {
        pe.parentNode.appendChild(dx);
        }else{
          pe.parentNode.insertBefore(dx,pe.nextSibling);
        }
        break;
      }
      pe = pe.parentNode;
    }
    if(pe.tagName.toLowerCase() == 'body')
  alert('You can insert a definition term or description only in a definition list!');
  }
};
