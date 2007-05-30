/*------------------------------------------*\
SaveSubmit for Xinha
____________________

Registers a button for submiting the Xinha form using asynchronous
postback for sending the data to the server

Usage: This should be a drop-in replacement for a normal submit button.
While saving a message is displayed to inform the user what's going on.
On successful transmission the output of the target script is shown, so it should print some information
about the success of saving.

\*------------------------------------------*/

function SaveSubmit(editor) {
	this.editor = editor;
	this.initial_html = null;
	this.changed = false;
	var self = this;
	var cfg = editor.config;
	this.textarea = this.editor._textArea;

	this.imgage_changed = _editor_url+"plugins/SaveSubmit/img/ed_save_red.gif";
	this.imgage_unchanged = _editor_url+"plugins/SaveSubmit/img/ed_save_green.gif";
	cfg.registerButton({
	id       : "savesubmit",
	tooltip  : self._lc("Save"),
	image    : this.imgage_unchanged,
	textMode : false,
	action   :  function(editor) {
			self.save(editor);
		}
	});
	cfg.addToolbarElement("savesubmit", "popupeditor", -1);
};

SaveSubmit.prototype._lc = function(string) {
    return HTMLArea._lc(string, 'SaveSubmit');
};

SaveSubmit._pluginInfo = {
  name          : "SaveSubmit",
  version       : "1.0",
  developer     : "Raimund Meyer",
  developer_url : "http://rheinauf.de",
  c_owner       : "Raimund Meyer",
  sponsor       : "",
  sponsor_url   : "",
  license       : "htmlArea"
};

SaveSubmit.prototype.onGenerate = function() {
	var self = this;
	var doc = this.editordoc = this.editor._iframe.contentWindow.document;
	HTMLArea._addEvents(doc, ["mouseup","keyup","keypress","keydown"],
			    function (event) {
			    return self.onEvent(HTMLArea.is_ie ? self.editor._iframe.contentWindow.event : event);
			    });
};

SaveSubmit.prototype.onEvent = function(ev) {

	var keyEvent = (HTMLArea.is_ie && ev.type == "keydown") || (!HTMLArea.is_ie && ev.type == "keypress");

	if (keyEvent && ev.ctrlKey && String.fromCharCode(HTMLArea.is_ie ? ev.keyCode : ev.charCode).toLowerCase() == 's') {
			this.save(this.editor);
	}
	else {
		if (!this.changed) {
			if (this.getChanged()) this.setChanged();
		};
	};
};

SaveSubmit.prototype.getChanged = function() {
	if (this.initial_html == null) this.initial_html = this.editor.getHTML();
	if (this.initial_html != this.editor.getHTML() && this.changed == false) {

		this.changed = true;
		return true;
	}
	else return false;
}
SaveSubmit.prototype.setChanged = function() {
	toolbar_objects = this.editor._toolbarObjects;
	for (var i in toolbar_objects) {
		var btn = toolbar_objects[i];
		if (btn.name == 'savesubmit') {
			btn.swapImage(this.imgage_changed);
		};
	};
	this.editor.updateToolbar();
};
SaveSubmit.prototype.changedReset = function() {
	this.changed = false;
	this.initial_html = null;
	toolbar_objects = this.editor._toolbarObjects;
	for (var i in toolbar_objects) {
		var btn = toolbar_objects[i];
		if (btn.name == 'savesubmit') {
			btn.swapImage(this.imgage_unchanged);
		};
	};
};

SaveSubmit.prototype.save =  function(editor) {
	this.buildMessage()
	var self =this;
	var form = editor._textArea.form;
	form.onsubmit();

	var content ='';

	for (var i=0;i<form.elements.length;i++)
	{
		content += ((i>0) ? '&' : '') + form.elements[i].name + '=' + encodeURIComponent(form.elements[i].value);
	}

	HTMLArea._postback(editor._textArea.form.action, content, function(getback) {

		if (getback) {
			self.setMessage(getback);
			//self.setMessage(self._lc("Ready"));
			self.changedReset();
		};
		removeMessage = function() { self.removeMessage()} ;
		window.setTimeout("removeMessage()",1000);

	});
};

SaveSubmit.prototype.setMessage = function(string) {
  var textarea = this.textarea;
  if ( !document.getElementById("message_sub_" + textarea.name)) { return ; }
  var elt = document.getElementById("message_sub_" + textarea.name);
  elt.innerHTML = HTMLArea._lc(string, 'SaveSubmit');
};

SaveSubmit.prototype.removeMessage = function() {
  var textarea = this.textarea;
  if (!document.getElementById("message_" + textarea.name)) { return ; }
  document.body.removeChild(document.getElementById("message_" + textarea.name));
};

//ripped mokhet's loading message function
SaveSubmit.prototype.buildMessage   = function() {

	var textarea = this.textarea;
	var htmlarea = this.editor._htmlArea;
	var loading_message = document.createElement("div");
	loading_message.id = "message_" + textarea.name;
	loading_message.className = "loading";
	loading_message.style.width    = htmlarea.offsetWidth +'px' ;//(this.editor._iframe.offsetWidth != 0) ? this.editor._iframe.offsetWidth +'px' : this.editor._initial_ta_size.w;

	loading_message.style.left     = HTMLArea.findPosX(htmlarea) +  'px';
	loading_message.style.top      = (HTMLArea.findPosY(htmlarea) + parseInt(htmlarea.offsetHeight) / 2) - 50 +  'px';

	var loading_main = document.createElement("div");
	loading_main.className = "loading_main";
	loading_main.id = "loading_main_" + textarea.name;
	loading_main.appendChild(document.createTextNode(this._lc("Saving...")));

	var loading_sub = document.createElement("div");
	loading_sub.className = "loading_sub";
	loading_sub.id = "message_sub_" + textarea.name;
	loading_sub.appendChild(document.createTextNode(this._lc("in progress")));
	loading_message.appendChild(loading_main);
	loading_message.appendChild(loading_sub);
	document.body.appendChild(loading_message);
};