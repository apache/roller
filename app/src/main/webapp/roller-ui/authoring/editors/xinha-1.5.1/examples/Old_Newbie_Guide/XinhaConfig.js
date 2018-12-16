xinha_editors = null;
xinha_init    = null;
xinha_config  = null;
xinha_plugins = null;

// This contains the names of textareas we will make into Xinha editors
xinha_init = xinha_init ? xinha_init : function()
{
   /** STEP 1 ***************************************************************
   * First, specify the textareas that shall be turned into Xinhas. 
   * For each one add the respective id to the xinha_editors array.
   * I you want add more than on textarea, keep in mind that these 
   * values are comma seperated BUT there is no comma after the last value.
   * If you are going to use this configuration on several pages with different
   * textarea ids, you can add them all. The ones that are not found on the
   * current page will just be skipped.
   ************************************************************************/

  xinha_editors = xinha_editors ? xinha_editors :
  [
    'myTextArea', 'anotherOne'
  ];
  
  /** STEP 2 ***************************************************************
   * Now, what are the plugins you will be using in the editors on this
   * page.  List all the plugins you will need, even if not all the editors
   * will use all the plugins.
   *
   * The list of plugins below is a good starting point, but if you prefer
   * a simpler editor to start with then you can use the following 
   * 
   * xinha_plugins = xinha_plugins ? xinha_plugins : [ ];
   *
   * which will load no extra plugins at all.
   ************************************************************************/

  xinha_plugins = xinha_plugins ? xinha_plugins :
  [
   'CharacterMap',
   'ContextMenu',
   'FancySelects',
   'SmartReplace',
   'Stylist',
   'Linker',
   'SuperClean',
   'TableOperations',
   'ListOperations',
   'WebKitResize'
  ];
  
         // THIS BIT OF JAVASCRIPT LOADS THE PLUGINS, NO TOUCHING  :)
         if(!Xinha.loadPlugins(xinha_plugins, xinha_init)) return;


  /** STEP 3 ***************************************************************
   * We create a default configuration to be used by all the editors.
   * If you wish to configure some of the editors differently this will be
   * done in step 5.
   *
   * If you want to modify the default config you might do something like this.
   *
   *   xinha_config = new Xinha.Config();
   *   xinha_config.width  = '640px';
   *   xinha_config.height = '420px';
   *
   *
   * For a list of the available configuration options, see:
   * http://trac.xinha.org/wiki/Documentation/ConfigVariablesList
   *
   *************************************************************************/

   xinha_config = xinha_config ? xinha_config() : new Xinha.Config();
        
  //this is the standard toolbar, feel free to remove buttons as you like
  xinha_config.toolbar =
  [
    ["popupeditor"],
    ["separator","formatblock","fontname","fontsize","bold","italic","underline","strikethrough"],
    ["separator","forecolor","hilitecolor","textindicator"],
    ["separator","subscript","superscript"],
    ["linebreak","separator","justifyleft","justifycenter","justifyright","justifyfull"],
    ["separator","insertorderedlist","insertunorderedlist","outdent","indent"],
    ["separator","inserthorizontalrule","createlink","insertimage","inserttable"],
    ["linebreak","separator","undo","redo","selectall","print"], (Xinha.is_gecko ? [] : ["cut","copy","paste","overwrite","saveas"]),
    ["separator","killword","clearfonts","removeformat","toggleborders","splitblock","lefttoright", "righttoleft"],
    ["separator","htmlmode","showhelp","about"]
  ];

   // To adjust the styling inside the editor, we can load an external stylesheet like this
   // NOTE : YOU MUST GIVE AN ABSOLUTE URL
   xinha_config.pageStyleSheets = [ _editor_url + "examples/files/full_example.css" ];
   //if you're using Stylist, import a stylesheet like this
   xinha_config.stylistLoadStylesheet(_editor_url + "examples/files/stylist.css");


  /** STEP 4 ***************************************************************
   * We first create editors for the textareas.
   *
   * You can do this in two ways, either
   *
   *   xinha_editors   = Xinha.makeEditors(xinha_editors, xinha_config, xinha_plugins);
   *
   * if you want all the editor objects to use the same set of plugins, OR;
   *
   *   xinha_editors = Xinha.makeEditors(xinha_editors, xinha_config);
   *   xinha_editors.myTextArea.registerPlugins(['Stylist']);
   *   xinha_editors.anotherOne.registerPlugins(['CSS','SuperClean']);
   *
   * if you want to use a different set of plugins for one or more of the
   * editors.
   ************************************************************************/

  xinha_editors   = Xinha.makeEditors(xinha_editors, xinha_config, xinha_plugins);

  /** STEP 5 ***************************************************************
   * If you want to change the configuration variables of any of the
   * editors,  this is the place to do that, for example you might want to
   * change the width and height of one of the editors, like this...
   *
   *   xinha_editors.myTextArea.config.width  = '640px';
   *   xinha_editors.myTextArea.config.height = '480px';
   *
   ************************************************************************/


  /** STEP 6 ***************************************************************
   * Finally we "start" the editors, this turns the textareas into
   * Xinha editors.
   ************************************************************************/

  Xinha.startEditors(xinha_editors);
}

Xinha.addOnloadHandler(xinha_init); // this executes the xinha_init function on page load
                                     // and does not interfere with window.onload properties set by other scripts

