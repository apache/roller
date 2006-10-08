/*------------------------------------------*\
 InsertSnippet for Xinha
 _______________________
 
 Insert HTML fragments or template variables
 
 Usage:
 1. Choose the file that contains the snippets
    You can either use a JS array (standard config: ./snippets.js) or a combination of PHP/HTML 
    where the PHP file reads the HTML file and converts it to a JS format. More convenient to maintain.
    Example:  xinha_config.InsertSnippet.snippets = _editor_url+"plugins/InsertSnippet/snippets.php"
              or
              xinha_config.InsertSnippet.snippets = "/Path/to/my/snippets.php" (has to be absolute)
 2. Edit the selected file to contain your stuff
 3. You can then include your own css
    Example: xinha_config.InsertSnippet.css = ['../../../CSS/Screen.css']; (may be relative)
 4. You can use the plugin also to insert template variables (i.e. the id in curly brackets) instead of static HTML.
    Set xinha_config.InsertSnippet.showInsertVariable true to display a choice option in the dialog
    
\*------------------------------------------*/

function InsertSnippet(editor) {
	this.editor = editor;

	var cfg = editor.config;
	var self = this;
	

	cfg.registerButton({
	id       : "insertsnippet",
	tooltip  : this._lc("Insert Snippet"),
	image    : editor.imgURL("ed_snippet.gif", "InsertSnippet"),
	textMode : false,
	action   : function(editor) {
			self.buttonPress(editor);
		}
	});
	cfg.addToolbarElement("insertsnippet", "insertimage", -1);
}

InsertSnippet._pluginInfo = {
  name          : "InsertSnippet",
  version       : "1.1",
  developer     : "Raimund Meyer",
  developer_url : "http://rheinauf.de",
  c_owner       : "Raimund Meyer",
  sponsor       : "Raimund Meyer",
  sponsor_url   : "http://ray-of-light.org/",
  license       : "htmlArea"
};

InsertSnippet.prototype._lc = function(string) {
    return HTMLArea._lc(string, 'InsertSnippet');
};

InsertSnippet.prototype.onGenerate = function() {
  var style_id = "IS-style";
  var style = this.editor._doc.getElementById(style_id);
  if (style == null) {
    style = this.editor._doc.createElement("link");
    style.id = style_id;
    style.rel = 'stylesheet';
    style.href = _editor_url + 'plugins/InsertSnippet/InsertSnippet.css';
    this.editor._doc.getElementsByTagName("HEAD")[0].appendChild(style);
  }
};

HTMLArea.Config.prototype.InsertSnippet =
{
  'snippets' : _editor_url+"plugins/InsertSnippet/snippets.js",
  'css' : ['../InsertSnippet.css'],
  'showInsertVariable': false
};
	
InsertSnippet.prototype.buttonPress = function(editor) {
		var args = editor.config;
		editor._popupDialog( "plugin://InsertSnippet/insertsnippet", function( param ) {
	
		if ( !param ) { 
	      return false;
	    }
				   	   
		eval(HTMLArea._geturlcontent(editor.config.InsertSnippet.snippets));
		editor.focusEditor();
		if (param['how'] == 'variable') {
			editor.insertHTML('{'+snippets[param["snippetnum"]].id+'}');
		} else {
			editor.insertHTML(snippets[param["snippetnum"]].HTML);
	   	}
  
    }, args);
  };