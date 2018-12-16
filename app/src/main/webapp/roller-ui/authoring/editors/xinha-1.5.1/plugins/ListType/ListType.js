// ListType Plugin for Xinha
// Toolbar Implementation by Mihai Bazon, http://dynarch.com/mishoo/
Xinha.loadStyle( 'ListType.css', 'ListType' );

function ListType( editor )
{
  this.editor = editor;
  var cfg = editor.config;
  var self = this;

  if ( cfg.ListType.mode == 'toolbar' )
  {
  var options = {};
    options[Xinha._lc( "Decimal numbers", "ListType" )] = "decimal";
    options[Xinha._lc( "Lower roman numbers", "ListType" )] = "lower-roman";
    options[Xinha._lc( "Upper roman numbers", "ListType" )] = "upper-roman";
    options[Xinha._lc( "Lower latin letters", "ListType" )] = "lower-alpha";
    options[Xinha._lc( "Upper latin letters", "ListType" )] = "upper-alpha";
    if (!Xinha.is_ie)
      // IE doesn't support this property; even worse, it complains
      // with a gross error message when we tried to select it,
      // therefore let's hide it from the damn "browser".
      options[Xinha._lc( "Lower greek letters", "ListType" )] = "lower-greek";
    var obj =
    {
      id            : "listtype",
      tooltip       : Xinha._lc( "Choose list style type (for ordered lists)", "ListType" ),
      options       : options,
      action        : function( editor ) { self.onSelect( editor, this ); },
      refresh       : function( editor ) { self.updateValue( editor, this ); },
      context       : "ol"
    };
    cfg.registerDropdown( obj );
    cfg.addToolbarElement( "listtype", ["insertorderedlist","orderedlist"], 1 );
  }
  else
  {
    editor._ListType = editor.addPanel( 'right' );
    Xinha.freeLater( editor, '_ListType' );
    Xinha.addClass( editor._ListType, 'ListType' );
    // hurm, ok it's pretty to use the background color for the whole panel,
    // but should not it be set by default when creating the panel ?
    Xinha.addClass( editor._ListType.parentNode, 'dialog' );

    editor.notifyOn( 'modechange',
      function(e,args)
      {
        if ( args.mode == 'text' ) editor.hidePanel( editor._ListType );
      }
    );

    var elts_ul = ['disc', 'circle', 'square', 'none'];
    var elts_ol = ['decimal', 'lower-alpha', 'upper-alpha', 'lower-roman', 'upper-roman', 'none'];
    var divglobal = document.createElement( 'div' );
    divglobal.style.height = '90px';
    var div = document.createElement( 'div' );
    this.divUL = div;
    div.style.display = 'none';
    for ( var i=0; i<elts_ul.length; i++ )
    {
      div.appendChild( this.createImage( elts_ul[i] ) );
    }
    divglobal.appendChild( div );
    var div = document.createElement( 'div' );
    this.divOL = div;
    div.style.display = 'none';
    for ( var i=0; i<elts_ol.length; i++ )
    {
      div.appendChild( this.createImage( elts_ol[i] ) );
    }
    divglobal.appendChild( div );

    editor._ListType.appendChild( divglobal );

    editor.hidePanel( editor._ListType );
  }
}

Xinha.Config.prototype.ListType =
{
  'mode': 'toolbar' // configuration mode : toolbar or panel
};

ListType._pluginInfo =
{
  name          : "ListType",
  version       : "2.1",
  developer     : "Laurent Vilday",
  developer_url : "http://www.mokhet.com/",
  c_owner       : "Xinha community",
  sponsor       : "",
  sponsor_url   : "",
  license       : "HTMLArea"
};

ListType.prototype.onSelect = function( editor, combo )
{
  var tbobj = editor._toolbarObjects[ combo.id ].element;
  var parent = editor.getParentElement();
  while (!/^ol$/i.test( parent.tagName ))
    parent = parent.parentNode;
  parent.style.listStyleType = tbobj.value;
};

ListType.prototype.updateValue = function( editor, combo )
{
  var tbobj = editor._toolbarObjects[ combo.id ].element;
  var parent = editor.getParentElement();
  while ( parent && !/^ol$/i.test( parent.tagName ) )
    parent = parent.parentNode;
  if (!parent)
  {
    tbobj.selectedIndex = 0;
    return;
  }
  var type = parent.style.listStyleType;
  if (!type)
  {
    tbobj.selectedIndex = 0;
  }
  else
  {
    for ( var i = tbobj.firstChild; i; i = i.nextSibling )
    {
      i.selected = (type.indexOf(i.value) != -1);
    }
  }
};

ListType.prototype.onUpdateToolbar = function()
{
  if ( this.editor.config.ListType.mode == 'toolbar' ) return ;
  var parent = this.editor.getParentElement();
  while ( parent && !/^[o|u]l$/i.test( parent.tagName ) )
    parent = parent.parentNode;
  if (parent && /^[o|u]l$/i.test( parent.tagName ) )
  {
    this.showPanel( parent );
  }
  else if (this.editor._ListType.style.display != 'none')
  {
    this.editor.hidePanel( this.editor._ListType );
  }
};

ListType.prototype.createImage = function( listStyleType )
{
  var self = this;
  var editor = this.editor;
  var a = document.createElement( 'a' );
  a.href = 'javascript:void(0)';
  Xinha._addClass( a, listStyleType );
  Xinha._addEvent( a, "click", function ()
    {
      var parent = editor._ListType.currentListTypeParent;
      parent.style.listStyleType = listStyleType;
      self.showActive( parent );
      return false;
    }
  );
  return a;
};

ListType.prototype.showActive = function( parent )
{
  var activeDiv = ( parent.tagName.toLowerCase() == 'ul' ) ? this.divUL : this.divOL;
  this.divUL.style.display = 'none';
  this.divOL.style.display = 'none';
  activeDiv.style.display = 'block';
  var defaultType = parent.style.listStyleType;
  if ( '' == defaultType ) defaultType = ( parent.tagName.toLowerCase() == 'ul' )? 'disc':'decimal';
  for ( var i=0; i<activeDiv.childNodes.length; i++ )
  {
    var elt = activeDiv.childNodes[i];
    if ( Xinha._hasClass( elt, defaultType ) )
    {
      Xinha._addClass( elt, 'active' );
    }
    else
    {
      Xinha._removeClass( elt, 'active' );
    }
  }
};

ListType.prototype.showPanel = function( parent )
{
  this.editor._ListType.currentListTypeParent = parent;
  this.showActive(parent);
  this.editor.showPanel( this.editor._ListType );
};