/*------------------------------------------*\
SaveSubmit for Xinha
____________________

See README.txt for information

\*------------------------------------------*/

function SaveSubmit(editor) {
	this.editor = editor;
	this.changed = false;
	var self = this;
	var cfg = editor.config;
	this.textarea = this.editor._textArea;

	cfg.registerIcon('savesubmitchanged', Xinha.getPluginDir("SaveSubmit")+"/img/ed_save_red.gif");
	cfg.registerIcon('savesubmitunchanged', Xinha.getPluginDir("SaveSubmit")+"/img/ed_save_green.gif");

	cfg.registerButton({
	id       : "savesubmit",
	tooltip  : self._lc("Save"),
	image    : cfg.iconList.savesubmitunchanged,
	textMode : false,
	action   :  function() {
			self.save();
		}
	});
	cfg.addToolbarElement("savesubmit", "popupeditor", -1);
}

SaveSubmit.prototype._lc = function(string) {
    return Xinha._lc(string, 'SaveSubmit');
}

SaveSubmit._pluginInfo = {
  name          : "SaveSubmit",
  version       : "1.0",
  developer     : "Raimund Meyer",
  developer_url : "http://x-webservice.net",
  c_owner       : "Raimund Meyer",
  sponsor       : "",
  sponsor_url   : "",
  license       : "htmlArea"
}

SaveSubmit.prototype.onKeyPress = function(ev) {
	if ( ev.ctrlKey && this.editor.getKey(ev) == 's') {
			this.save(this.editor);
			Xinha._stopEvent(ev);
			return true;
	}
	else {
		if (!this.changed) {
			if (this.getChanged()) this.setChanged();
			return false;
		}
	}
}
SaveSubmit.prototype.onExecCommand = function (cmd) {
	if (this.changed && cmd == 'undo') { 
		if (this.initial_html == this.editor.getInnerHTML()) this.setUnChanged();
		return false;
	}
}
SaveSubmit.prototype.onUpdateToolbar = function () {
  if (!this.initial_html) this.initial_html = this.editor.getInnerHTML();
	if (!this.changed) {
		if (this.getChanged()) this.setChanged();
		return false;
	}	
}
SaveSubmit.prototype.getChanged = function() {
	if (this.initial_html === null) this.initial_html = this.editor.getInnerHTML();
	if (this.initial_html != this.editor.getInnerHTML() && this.changed == false) {
		this.changed = true;
		return true;
	}
	else return false;
}
SaveSubmit.prototype.setChanged = function() {
	this.editor._toolbarObjects.savesubmit.swapImage(this.editor.config.iconList.savesubmitchanged);
	this.editor.updateToolbar();
}
SaveSubmit.prototype.setUnChanged = function() {
	this.changed = false;
	this.editor._toolbarObjects.savesubmit.swapImage(this.editor.config.iconList.savesubmitunchanged);
}
SaveSubmit.prototype.changedReset = function() {
	this.initial_html = null;
	this.setUnChanged();
}

SaveSubmit.prototype.save =  function() {
	this.buildMessage();
	var editor = this.editor;
	var self =this;
	var form = editor._textArea.form;
	form.onsubmit();

	var name, value, content ='';
	
	for (var i=0;i<form.elements.length;i++)
	{
		if (( form.elements[i].type == 'checkbox' || form.elements[i].type == 'radio' ) && !form.elements[i].checked ) continue;

		content += ((i>0) ? '&' : '') + form.elements[i].name + '=' + encodeURIComponent(form.elements[i].value);
	}
	var url = editor._textArea.form.action || window.location.href;
	Xinha._postback(url, content, function(getback) {

		if (getback) {
			self.setMessage(getback);
			//self.setMessage(self._lc("Ready"));
			self.changedReset();
		}
		removeMessage = function() { self.removeMessage()} ;
		window.setTimeout("removeMessage()",1000);
	});
}

SaveSubmit.prototype.setMessage = function(string) {
  var textarea = this.textarea;
  if ( !document.getElementById("message_sub_" + textarea.id)) { return ; }
  var elt = document.getElementById("message_sub_" + textarea.id);
  elt.innerHTML = Xinha._lc(string, 'SaveSubmit');
}

SaveSubmit.prototype.removeMessage = function() {
  var textarea = this.textarea;
  if (!document.getElementById("message_" + textarea.id)) { return ; }
  document.body.removeChild(document.getElementById("message_" + textarea.id));
}

//ripped mokhet's loading message function
SaveSubmit.prototype.buildMessage   = function() {

	var textarea = this.textarea;
	var htmlarea = this.editor._htmlArea;
	var loading_message = document.createElement("div");
	loading_message.id = "message_" + textarea.id;
	loading_message.className = "loading";
	loading_message.style.width    = htmlarea.offsetWidth +'px' ;//(this.editor._iframe.offsetWidth != 0) ? this.editor._iframe.offsetWidth +'px' : this.editor._initial_ta_size.w;

	loading_message.style.left     = Xinha.findPosX(htmlarea) +  'px';
	loading_message.style.top      = (Xinha.findPosY(htmlarea) + parseInt(htmlarea.offsetHeight) / 2) - 50 +  'px';

	var loading_main = document.createElement("div");
	loading_main.className = "loading_main";
	loading_main.id = "loading_main_" + textarea.id;
	loading_main.appendChild(document.createTextNode(this._lc("Saving...")));

	var loading_sub = document.createElement("div");
	loading_sub.className = "loading_sub";
	loading_sub.id = "message_sub_" + textarea.id;
	loading_sub.appendChild(document.createTextNode(this._lc("in progress")));
	loading_message.appendChild(loading_main);
	loading_message.appendChild(loading_sub);
	document.body.appendChild(loading_message);
}