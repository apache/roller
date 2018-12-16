// BackgroundImage plugin for Xinha
// Sponsored by http://www.schaffrath-neuemedien.de
// Implementation by Udo Schmal
// based on TinyMCE (http://tinymce.moxiecode.com/) Distributed under LGPL by Moxiecode Systems AB
//
// Distributed under the same terms as Xinha itself.
// This notice MUST stay intact for use (see license.txt).

function BackgroundImage(editor) {
  this.editor = editor;
  var cfg = editor.config;
  var self = this;
  cfg.registerButton({
    id       : "bgImage",
    tooltip  : Xinha._lc("Set page background image",  'BackgroundImage'),
    image    : editor.imgURL("ed_bgimage.gif", "BackgroundImage"),
    textMode : false,
    action   : function(editor) {
      self.show();
    }
  })
  cfg.addToolbarElement("bgImage", "inserthorizontalrule", 1);
}

BackgroundImage._pluginInfo = {
  name          : "BackgroundImage",
  version       : "1.0",
  developer     : "Udo Schmal",
  developer_url : "http://www.schaffrath-neuemedien.de/",
  c_owner       : "Udo Schmal & Schaffrath NeueMedien",
  sponsor       : "L.N.Schaffrath NeueMedien",
  sponsor_url   : "http://www.schaffrath-neuemedien.de.de/",
  license       : "htmlArea"
};

BackgroundImage.prototype.onGenerateOnce = function(editor){
  // Load assets
  var self = BackgroundImage;
  if (self.loading) return;

  // A list of jpgs that are expected to be in ./backgrounds with thumbnails.
  var backgrounds = {blufur:'',palecnvs:'', ppplcnvs:'', ylwsand:''};

  self.loading = true;
  self.methodsReady = true;

  // When we get back from loading the dialog, we'll process our image template to handle as many images as specified.
  Xinha._getback(Xinha.getPluginDir('BackgroundImage') + '/dialog.html', function(getback) {

    // Replace the template line with one line per image.
    self.html = getback.replace(/<template>(.*?)<\/template>/ig, function(fullString, template) {
      var replacement = '';

      for (bg in backgrounds)
      {
        var thumbURL = Xinha.getPluginDir('BackgroundImage') + '/backgrounds/thumbnails/' + bg + '.jpg';
        var imageURL = Xinha.getPluginDir('BackgroundImage') + '/backgrounds/' + bg + '.jpg';
        replacement += template.replace(/%thumbnail%/,thumbURL).replace(/%image%/,imageURL);
      }
      return replacement;
    });
    self.dialogReady = true;
  });
}

BackgroundImage.prototype.onUpdateToolbar = function(editor){
  // Keep our toolbar image greyed until we're fully loaded.
  if (!(BackgroundImage.dialogReady && BackgroundImage.methodsReady))
  {
    this.editor._toolbarObjects.BackgroundImage.state("enabled", false);
  }
  else this.onUpdateToolbar = null;

}

BackgroundImage.prototype.prepareDialog = function(editor){
  var self = this;
  var editor = this.editor;

  var dialog = this.dialog = new Xinha.Dialog(editor, BackgroundImage.html, 'Xinha',{width:400})
  //
  // Hookup the buttons with our actions
  dialog.getElementById('set').onclick = function() {self.apply();}
  dialog.getElementById('delete').onclick = function() {self.deleteBg();}
  dialog.getElementById('cancel').onclick = function() { self.dialog.hide()};
  
  this.dialogReady = true;
}

BackgroundImage.prototype.show = function(editor){
 if (!this.dialog) this.prepareDialog();

  var editor = this.editor;

  // After clearing the background property, it returns the current URL, and so
  // we need to check the extension to find out if it really has a background.
  if (editor._doc.body.background.split('.').pop() in {jpg:'', gif:'', png:'', jpeg:'', tiff:''})
  {
    var background = editor._doc.body.background;
  }
  else
  {
    var background = '';
  }
  var values = 
  {
    "background"      : background
  }

  // now calling the show method of the Xinha.Dialog object to set the values and show the actual dialog
  this.dialog.show(values);
}

BackgroundImage.prototype.deleteBg = function(){
  var editor = this.editor;
  this.dialog.hide();

  if (Xinha.is_ie)
    editor.focusEditor();

    editor._doc.body.background = "";
}

BackgroundImage.prototype.apply = function(){
  var editor = this.editor;
  var doc = editor._doc;
  
  // selection is only restored on dialog.hide()
  var param = this.dialog.hide();
  // assign the given arguments
  
  if (Xinha.is_ie)
    editor.focusEditor();

  doc.body.background = param.background;
};
