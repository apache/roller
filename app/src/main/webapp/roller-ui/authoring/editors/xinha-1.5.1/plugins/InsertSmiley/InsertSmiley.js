/*---------------------------------------*\
 Insert Smiley Plugin for Xinha
 -----------------------------------------
 original author: Ki Master George (kimastergeorge@gmail.com)
 rewritten: James Sleeman (james@gogo.co.nz)
\*---------------------------------------*/

InsertSmiley._pluginInfo = {
  name          : "InsertSmiley",
  version       : "2.0",
  developer     : "V 1.0 Ki Master George, V2.0 James Sleeman",
  developer_url : "http://www.gogo.co.nz/",
  c_owner       : "Ki Master George, James Sleeman",
  sponsor       : "Gogo Internet Services",
  sponsor_url   : "http://www.gogo.co.nz/",
  license       : "htmlArea"
};

/** To configure the InsertSmiley plugin.
 *
 * Set xinha_config.InsertSmiley.smileys to EITHER
 *
 *  * A URL to a file called smileys.js, which follows the format
 *    you can view in smileys/smileys.js
 *
 *  * A URL to a file called smileys.php (or whatever other language)
 *    which outputs a file in the same way as smileys/smileys.php
 *
 *  * A javascript the same as you will find in smileys/smileys.js
 *
 *  By defaut the static smileys/smileys.js file is used, you may 
 *  wish to set this like so
 *
 *  xinha_config.InsertSmiley.smileys = _editor_url+'/plugins/InsertSmiley/smileys/smileys.php';
 * 
 *  And then you can add new smileys just by dumping all the images into the
 *      /xinha/plugins/InsertSmiley/smileys/
 *  folder and they will automatically be made available.
 * 
 */
 
Xinha.Config.prototype.InsertSmiley=  {
  smileys : '' 
};

Xinha.loadStyle('dialog.css', 'InsertSmiley');

function InsertSmiley(editor) {
  this.editor  = editor;
  this.smileys = false;
  this.dialog  = false;
   
  var cfg = editor.config;
  var self = this;  
  
  // register the toolbar buttons provided by this plugin
  cfg.registerButton({
    id       : "insertsmiley",
    tooltip  : this._lc("Insert Smiley"),
    image    : editor.imgURL("ed_smiley.gif", "InsertSmiley"),
    textMode : false,
    action   : function(editor) {
                 self.buttonPress(editor);
               }
  });
  
  cfg.addToolbarElement("insertsmiley", "inserthorizontalrule", 1);
}

InsertSmiley.prototype.onGenerateOnce = function()
{
  // Back compat for ray's smiley config in changeset:904 ( ticket:1093 )
  if(this.editor.config.InsertSmiley.smileyURL)
  {
    var smileys = [ ];
    var smileylist = Xinha._geturlcontent(Xinha.getPluginDir("InsertSmiley") + '/smileys.txt');
    var smileyURL = this.editor.config.InsertSmiley.smileyURL;
    
    smileylist = smileylist.match(/^.+$/mg);
    for(var i = 0; i < smileylist.length; i++)
    {
      smileys[smileys.length] = { title: smileylist[i],  src:   smileyURL + encodeURIComponent(smileylist[i]) };                   
    }
    this.editor.config.InsertSmiley.smileys = smileys;
  }
  
  
  this.loadAssets();
};

InsertSmiley.prototype._lc = function(string) {
  return Xinha._lc(string, 'InsertSmiley');
};

/** Load the dialog and js files.
 * 
 */
 
InsertSmiley.prototype.loadAssets = function()
{
  var self = this;
  if (self.loading) return;
  
  if(typeof this.editor.config.InsertSmiley.smileys != 'string')
  {
    // Must be a smiley definition itself
    this.smileys     = this.editor.config.InsertSmiley.smileys;
    this.smiley_base = _editor_url; // Doesn't make a lot of sense, we assume the smileys will have absolute paths anyway
  }
  else
  {
    var smileys_def = 
      this.editor.config.InsertSmiley.smileys 
        ? this.editor.config.InsertSmiley.smileys // URL to the smileys.js file, or somethign that creates it
        : (Xinha.getPluginDir("InsertSmiley") + '/smileys/smileys.js'); // our own default one
        
    Xinha._getback( 
      smileys_def, 
      function(sm) { self.smileys = eval(sm); self.smileys_base = smileys_def.replace(/\/[^\/]+(\?.*)?$/, '/'); } 
    );    
  }
    
  Xinha._getback( Xinha.getPluginDir("InsertSmiley") + '/dialog.html', function(html) { self.makeDialog(html); } );
}

/** Make the dialog
 *
 *  @note It is really important that this happens throuh the onGenerateOnce event
 *   (in this case it goes onGenerateOnce -> loadAssets -> makeDialog)
 *  because otherwise creating the dialog can fail in mysterious ways (silent 
 *  javascript exceptions in Gecko etc).
 */
 
InsertSmiley.prototype.makeDialog = function(html)
{
  var self = this;
  
  // We can not make the dialog until the smileys are loaded, so wait a bit.
  if(!this.smileys) 
  {  
    window.setTimeout(function() { self.makeDialog(html); }, 1000); 
    return; 
  }
  
  var dialog = new Xinha.Dialog(this.editor, html, 'InsertSmiley', {width:155, height:100}, {modal:false});
    
  var src;
  var s;
  var a;
  
  for(var i = 0; i < this.smileys.length; i++)
  {
    src = this.smileys[i].src.match(/^(([a-zA-Z]+:\/\/)|\/)/) ? 
          this.smileys[i].src : this.smileys_base + this.smileys[i].src;
    s = document.createElement('img');
    s.src = src;
    s.alt = dialog._lc(this.smileys[i].title); 
    s.style.border = 'none';
    
    a = document.createElement('a');
    a.href= 'javascript:void(0);';
    a.smiley_src = src;
    a.title = dialog._lc(this.smileys[i].title);
    
    // This is where we insert the smiley into the HTML
    Xinha.addDom0Event(a, 'click', function(ev) { self.editor.insertHTML('<img src="'+this.smiley_src+'" alt="'+this.title+'" />'); Xinha._stopEvent(window.event ? event : ev); return false; });
    
    a.appendChild(s);
    dialog.getElementById('smileys').appendChild(a);
    
    a = null; s = null; src = null;
  }
  
  this.dialog = dialog;
  return true;
}

/** Show the dialog.
 */
 
InsertSmiley.prototype.buttonPress = function(editor) {
  if(this.dialog) 
  {
    this.dialog.show();    
  }
  else
  { 
    // Should put something here!
  }
  return;
};