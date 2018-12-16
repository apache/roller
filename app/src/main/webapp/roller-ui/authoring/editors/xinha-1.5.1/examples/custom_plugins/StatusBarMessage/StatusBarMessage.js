// A sily little demo plugin that puts a message in the status bar

function StatusBarMessage(editor) {
  this.editor = editor;
}

StatusBarMessage._pluginInfo = {
  name          : "StatusBarMessage",
  version       : "0.1",
  developer     : "James Sleeman",
  license       : "htmlArea"
};

StatusBarMessage.prototype._lc = function(string) {
  return Xinha._lc(string, "StatusBarMessage");
};

StatusBarMessage.prototype.onGenerateOnce = function() {
  var self = this;
  if (this.message==null) {
      var message = self.editor.registerStatusWidget('StatusBarMessage', ['wysiwyg']);
      this.message = message;
  }
};

StatusBarMessage.prototype.onUpdateToolbar = function() {
  this.message.innerHTML = this._lc("Hello World, This Is The Custom Plugin Speaking");
};

