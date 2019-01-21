/** superClean combines HTMLTidy, Word Cleaning and font stripping into a single function
 *  it works a bit differently in how it asks for parameters */

function SuperClean(editor, args)
{
  this.editor = editor;
  var superclean = this;
  editor._superclean_on = false;
  editor.config.registerButton('superclean', this._lc("Clean up HTML"), [_editor_url +'iconsets/Tango/ed_buttons_main.png',6,4], true, function(e, objname, obj) { superclean._superClean(null, obj); });

  // See if we can find 'killword' and replace it with superclean
  editor.config.addToolbarElement("superclean", "killword", 0);
}

SuperClean._pluginInfo =
{
  name     : "SuperClean",
  version  : "1.0",
  developer: "James Sleeman, Niko Sams",
  developer_url: "http://www.gogo.co.nz/",
  c_owner      : "Gogo Internet Services",
  license      : "htmlArea",
  sponsor      : "Gogo Internet Services",
  sponsor_url  : "http://www.gogo.co.nz/"
};

SuperClean.prototype._lc = function(string) {
    return Xinha._lc(string, 'SuperClean');
};

Xinha.Config.prototype.SuperClean =
{
  // set to the URL of a handler for html tidy, this handler
  //  (see tidy.php for an example) must that a single post variable
  //  "content" which contains the HTML to tidy, and return javascript like
  //  editor.setHTML('<strong>Tidied Html</strong>')
  // it's called through XMLHTTPRequest
  'tidy_handler': Xinha.getPluginDir("SuperClean") + '/tidy.php',

  // set additional arguments needed for the tidy request 
  'tidy_args' : {},  

  //avaliable filters (these are built-in filters)
  // You can either use
  //    'filter_name' : "Label/Description String"
  // or 'filter_name' : {label: "Label", checked: true/false, filterFunction: function(html) { ... return html;} }
  // filterFunction in the second format above is optional.

  'filters': { 'tidy':          {label:Xinha._lc('General tidy up and correction of some problems.', 'SuperClean'), checked:true, fullonly: true},
               'word_clean':    {label:Xinha._lc('Clean bad HTML from Microsoft Word.', 'SuperClean'), checked:true, fullonly: true},              
               'word':          {label:Xinha._lc('Vigorously purge HTML from Microsoft Word.', 'SuperClean'), checked:false, fullonly: true},
               'remove_faces':  {label:Xinha._lc('Remove custom typefaces (font "styles").', 'SuperClean'),checked:true},
               'remove_sizes':  {label:Xinha._lc('Remove custom font sizes.', 'SuperClean'),checked:true},
               'remove_colors': {label:Xinha._lc('Remove custom text colors.', 'SuperClean'),checked:true},
               'remove_emphasis': {label:Xinha._lc('Remove emphasis and annotations.', 'SuperClean'), checked:true},
               'remove_sup_sub':  {label:Xinha._lc('Remove superscripts and subscripts.', 'SuperClean'), checked:false},
               'remove_alignment': {label:Xinha._lc('Remove alignment (left/right/justify).', 'SuperClean'),checked:true},
               'remove_all_css_classes': {label:Xinha._lc('Remove all classes (CSS).', 'SuperClean'), checked: false},
               'remove_all_css_styles':         {label:Xinha._lc('Remove all styles (CSS).', 'SuperClean'),checked:true},
               'remove_lang': {label:Xinha._lc('Remove lang attributes.', 'SuperClean'),checked:true},
               'remove_fancy_quotes': {label:Xinha._lc('Replace directional quote marks with non-directional quote marks.', 'SuperClean'), checked:false},
  //additional custom filters (defined in plugins/SuperClean/filters/word.js)
               'paragraph':{label:Xinha._lc('Remove Paragraphs', 'SuperClean'), checked:false},
               'remove_all_tags': {label:Xinha._lc('Remove All HTML Tags', 'SuperClean'), checked:false }
             },
  //if false all filters are applied, if true a dialog asks what filters should be used
  'show_dialog': true
};

SuperClean.filterFunctions = { };

/** Helper function to strip tags from given html.
 * 
 * @param string
 * @param array|string An array pf tag names, or a tag name string
 * @param bool         true == completely remove tag and contents, false == remove tag only
 * @return string
 */

SuperClean.stripTags = function(html, tagNames, completelyRemove)
{
  if(typeof tagNames == 'string')
  {
    tagNames = [ tagNames ];
  }
  
  for(var i = 0; i < tagNames.length; i++)
  {
    var tagName = tagNames[i];
    if(completelyRemove)
    {
      html = html.replace(new RegExp('<'+tagName+'( [^>]*)?>.*?</'+tagName+'( [^>]*)?>', 'gi'), '');
    }
    else
    {
      html = html.replace(new RegExp('</?'+tagName+'( [^>]*)?>', 'gi'), '');
    }
  }
  
  return html;
}

SuperClean.stripAttributes = function(html, attributeNames)
{
  if(typeof attributeNames == 'string')
  {
    attributeNames = [ attributeNames ];
  }
  
  for(var i = 0; i < attributeNames.length; i++)
  {
    var attributeName = attributeNames[i];
    
    // @TODO - make this less likely to false-positive outside of tags
    html = html.replace(new RegExp(' ('+attributeName+'="[^"]*"|'+attributeName+'=\'[^\']*\'|'+attributeName+'=[^ >]*)', 'gi'), ' ');    
  }
  
  return html;
}

SuperClean.prototype.onGenerateOnce = function()
{

  if(this.editor.config.tidy_handler)
  {
    //for backwards compatibility
    this.editor.config.SuperClean.tidy_handler = this.editor.config.tidy_handler;
    this.editor.config.tidy_handler = null;
  }
  if(!this.editor.config.SuperClean.tidy_handler && this.editor.config.filters.tidy) {
    //unset tidy-filter if no tidy_handler
    this.editor.config.filters.tidy = null;
  }
  SuperClean.loadAssets();
  this.loadFilters();
};

SuperClean.prototype.onUpdateToolbar = function()
{ 
  if (!(SuperClean.methodsReady && SuperClean.html))
  {
    this.editor._toolbarObjects.superclean.state("enabled", false);
  }
  else this.onUpdateToolbar = null;
};

SuperClean.loadAssets = function()
{
  var self = SuperClean;
  if (self.loading) return;
  self.loading = true;
  Xinha._getback(Xinha.getPluginDir("SuperClean") + '/pluginMethods.js', function(getback) { eval(getback); self.methodsReady = true; });
  Xinha._getback( Xinha.getPluginDir("SuperClean") + '/dialog.html', function(getback) { self.html = getback; } );
};

SuperClean.prototype.loadFilters = function()
{
  var sc = this;
  //load the filter-functions
  for(var filter in this.editor.config.SuperClean.filters)
  {
    if (/^(remove_colors|remove_sizes|remove_faces|remove_lang|word_clean|remove_fancy_quotes|tidy|remove_emphasis|remove_sup_sub|remove_alignment|remove_all_css_classes|remove_all_css_styles|remove_all_tags)$/.test(filter)) continue; //skip built-in functions
    
    if(!SuperClean.filterFunctions[filter])
    {
      var filtDetail = this.editor.config.SuperClean.filters[filter];
      if(typeof filtDetail.filterFunction != 'undefined')
      {
        SuperClean.filterFunctions[filter] = filterFunction;
      }
      else
      {
        Xinha._getback(Xinha.getPluginDir("SuperClean") + '/filters/'+filter+'.js',
                      function(func) {
                        eval('SuperClean.filterFunctions.'+filter+'='+func+';');
                        sc.loadFilters();
                      });
      }
      return;
    }
  }
};