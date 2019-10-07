// Character Map plugin for Xinha
// Original Author - Bernhard Pfeifer novocaine@gmx.net
Xinha.loadStyle( 'CharacterMap.css', 'CharacterMap' );

function CharacterMap( editor )
{
  this.editor = editor;
  var cfg = editor.config;
  var self = this;
  cfg.registerButton(
    {
      id       : 'insertcharacter',
      tooltip  : Xinha._lc( 'Insert special character', 'CharacterMap' ),
      image    : editor.imgURL('images/tango/16x16/apps/accessories-character-map.png'),
      textMode : false,
      action   : function() { self.show(); }
    }
  );
  cfg.addToolbarElement('insertcharacter', 'createlink', -1);

}

// configuration mode : panel or popup
Xinha.Config.prototype.CharacterMap =
{
  'mode': 'popup' // configuration mode : panel or popup
};

CharacterMap._pluginInfo =
{
  name          : "CharacterMap",
  version       : "2.0",
  developer     : "Laurent Vilday",
  developer_url : "http://www.mokhet.com/",
  c_owner       : "Xinha community",
  sponsor       : "",
  sponsor_url   : "",
  license       : "HTMLArea"
};

CharacterMap._isActive = false;


CharacterMap.prototype.addEntity = function ( entite, pos )
{
  var editor = this.editor;
  var self = this;
  var a = document.createElement( 'a' );
  Xinha._addClass( a, 'entity' );
  a.innerHTML = entite;
  a.href = 'javascript:void(0)';
  Xinha._addClass(a, (pos%2)? 'light':'dark');
  a.onclick = function()
  {
    if (Xinha.is_ie) editor.focusEditor();
    editor.insertHTML( entite );
    //self._isActive = false;
    //editor.hidePanel( editor._CharacterMap );
    return false;
  };
  this.dialog.main.appendChild( a );
  a = null;
};

CharacterMap.prototype.onGenerateOnce = function()
{
	this._prepareDialog();
};

CharacterMap.prototype._prepareDialog = function()
{
	var self = this;
	var editor = this.editor;

	var html = '<h1><l10n>Insert special character</l10n></h1>';

	// Now we have everything we need, so we can build the dialog.
	this.dialog = new Xinha.Dialog(editor, html, 'CharacterMap',{width:300},{modal:false});
	Xinha._addClass( this.dialog.rootElem, 'CharacterMap' );

	if (editor.config.CharacterMap && editor.config.CharacterMap.mode == 'panel') this.dialog.attachToPanel('right');
	
	var entites =
	[
	'&Yuml;', '&scaron;', '&#064;', '&quot;', '&iexcl;', '&cent;', '&pound;', '&curren;', '&yen;', '&brvbar;',
	'&sect;', '&uml;', '&copy;', '&ordf;', '&laquo;', '&not;', '&macr;', '&deg;', '&plusmn;', '&sup2;',
	'&sup3;', '&acute;', '&micro;', '&para;', '&middot;', '&cedil;', '&sup1;', '&ordm;', '&raquo;', '&frac14;',
	'&frac12;', '&frac34;', '&iquest;', '&times;', '&Oslash;', '&divide;', '&oslash;', '&fnof;', '&circ;',
	'&tilde;', '&ndash;', '&mdash;', '&lsquo;', '&rsquo;', '&sbquo;', '&ldquo;', '&rdquo;', '&bdquo;',
	'&dagger;', '&Dagger;', '&bull;', '&hellip;', '&permil;', '&lsaquo;', '&rsaquo;', '&euro;', '&trade;',
	'&Agrave;', '&Aacute;', '&Acirc;', '&Atilde;', '&Auml;', '&Aring;', '&AElig;', '&Ccedil;', '&Egrave;',
	'&Eacute;', '&Ecirc;', '&Euml;', '&Igrave;', '&Iacute;', '&Icirc;', '&Iuml;', '&ETH;', '&Ntilde;',
	'&Ograve;', '&Oacute;', '&Ocirc;', '&Otilde;', '&Ouml;', '&reg;', '&times;', '&Ugrave;', '&Uacute;',
	'&Ucirc;', '&Uuml;', '&Yacute;', '&THORN;', '&szlig;', '&agrave;', '&aacute;', '&acirc;', '&atilde;',
	'&auml;', '&aring;', '&aelig;', '&ccedil;', '&egrave;', '&eacute;', '&ecirc;', '&euml;', '&igrave;',
	'&iacute;', '&icirc;', '&iuml;', '&eth;', '&ntilde;', '&ograve;', '&oacute;', '&ocirc;', '&otilde;',
	'&ouml;', '&divide;', '&oslash;', '&ugrave;', '&uacute;', '&ucirc;', '&uuml;', '&yacute;', '&thorn;',
	'&yuml;', '&OElig;', '&oelig;', '&Scaron;'
	];

	for ( var i=0; i<entites.length; i++ )
	{
	  this.addEntity( entites[i], i );
	}
	
	this.ready = true;
	//this.hide();
};

CharacterMap.prototype.show = function()
{
  if(!this.ready) // if the user is too fast clicking the, we have to make them wait
	{
		var self = this;
		window.setTimeout(function() {self.show();},100);
		return;
	}
	this.dialog.toggle();
};
CharacterMap.prototype.hide = function()
{
	this.dialog.hide();
};

