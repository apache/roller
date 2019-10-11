// Paste Plain Text plugin for Xinha

// Distributed under the same terms as Xinha itself.
// This notice MUST stay intact for use (see license.txt).

function PasteText(editor) {
	this.editor = editor;
	var cfg = editor.config;
	var self = this;

	cfg.registerButton({
		id       : "pastetext",
		tooltip  : this._lc("Paste as Plain Text"),
		image    : editor.imgURL("ed_paste_text.gif", "PasteText"),
		textMode : false,
		action   : function() { self.show(); }
	});

	cfg.addToolbarElement("pastetext", ["paste", "killword", "superclean"], 1);
}

PasteText._pluginInfo = {
	name          : "PasteText",
	version       : "1.2",
	developer     : "Michael Harris",
	developer_url : "http://www.jonesadvisorygroup.com",
	c_owner       : "Jones Advisory Group",
	sponsor       : "Jones International University",
	sponsor_url   : "http://www.jonesinternational.edu",
	license       : "htmlArea"
};

PasteText.prototype._lc = function(string) {
	return Xinha._lc(string, 'PasteText');
};

Xinha.Config.prototype.PasteText =
{
	showParagraphOption : true,
	newParagraphDefault :true
}

PasteText.prototype.onGenerateOnce = function()
{
	var self = PasteText;
	if (self.loading) return;
	self.loading = true;
	Xinha._getback(Xinha.getPluginDir("PasteText") + '/popups/paste_text.html', function(getback) { self.html = getback;});
};

PasteText.prototype._prepareDialog = function()
{
	var self = this;
	var editor = this.editor;

	var self = this;

/// Now we have everything we need, so we can build the dialog.
	this.dialog = new Xinha.Dialog(editor, PasteText.html, 'PasteText',{width:350})

	// Connect the OK and Cancel buttons
	this.dialog.getElementById('ok').onclick = function() {self.apply();}

	this.dialog.getElementById('cancel').onclick = function() { self.dialog.hide()};

	// do some tweaking 
	if (editor.config.PasteText.showParagraphOption)
	{
		this.dialog.getElementById("paragraphOption").style.display = "";
	}
	if (editor.config.PasteText.newParagraphDefault)
	{
		this.dialog.getElementById("insertParagraphs").checked = true;
	}

	// we can setup a custom function that cares for sizes etc. when the dialog is resized
	this.dialog.onresize = function ()
	{
		this.getElementById("inputArea").style.height = 
		parseInt(this.height,10) // the actual height of the dialog
		- this.getElementById('h1').offsetHeight // the title bar
		- this.getElementById('buttons').offsetHeight // the buttons
		- parseInt(this.rootElem.style.paddingBottom,10) // we have a padding at the bottom, gotta take this into acount
		+ 'px'; // don't forget this ;)
		
		this.getElementById("inputArea").style.width =(this.width - 2) + 'px'; // and the width

	}
};

PasteText.prototype.show = function()
{
	if (!this.dialog) this._prepareDialog();

	// here we can pass values to the dialog
	// each property pair consists of the "name" of the input we want to populate, and the value to be set
	var inputs =
	{
		inputArea : '' // we want the textarea always to be empty on showing
	}
	// now calling the show method of the Xinha.Dialog object to set the values and show the actual dialog
	this.dialog.show(inputs);

	// Init the sizes (only if we have set up the custom resize function)
	this.dialog.onresize();

	this.dialog.getElementById("inputArea").focus();
};

// and finally ... take some action
PasteText.prototype.apply = function()
{
	// the hide method of the dialog object returns the values of the inputs AND hides the dialog
	// could also use this.dialog.getValues() here and hide it at the end
	var returnValues = this.dialog.hide();
	
	var html = returnValues.inputArea;
	var insertParagraphs = returnValues.insertParagraphs;
	html = html.replace(/</g, "&lt;");
	html = html.replace(/>/g, "&gt;");
	if ( returnValues.insertParagraphs)
	{
		html = html.replace(/\t/g,"&nbsp;&nbsp;&nbsp;&nbsp;");
		html = html.replace(/\n+/g,"</p><p>");
		html="<p>" + html + "</p>";
		if (Xinha.is_ie)
		{
			this.editor.insertHTML(html);
		}
		else
		{
      this.editor.insertHTML(html);
			// this.editor.execCommand("inserthtml",false,html);
		}
	}
	else
	{
		html = html.replace(/\n/g,"<br />");
		this.editor.insertHTML(html);
	}
};