/**
 * @fileOverview PersistentStorage PersistentStorage.js file.
 * This plugin is a rethinking of the ExtendFileManager plugin written and
 * designed by Wei Zhuo, Afru, Krzysztof Kotowicz, Raimund Meyer.
 *
 * @author Douglas Mayle <douglas@openplans.org>
 * @version 1.0
 */

/**
 * The global namespace
 * @name window
 */
(function($) {
/**
 * @name PersistentStorage
 * @class Provides an interface for persistant storage options to the user.
 * @param editor A reference to the Xinha Editor this plugin instance is attached to.
 */
var PersistentStorage = window.PersistentStorage = function (editor) {
  /**
   * @memberOf Xinha
   */
  this.editor = editor;
  var self = this;
}

/**
 * Plugin Metadata
 * @namespace
 * @static
 */
PersistentStorage._pluginInfo = {
    /** Plugin name */
    name          : "PersistentStorage",
    /** Plugin version */
    version       : "1.0",
    /** Plugin author */
    developer     : "Douglas Mayle",
    /** Plugin author's website */
    developer_url : "http://douglas.mayle.org",
    /** Plugin license */
    license       : "BSD"
};

/**
 * The list of backend modules registered with this instance.
 */
PersistentStorage.prototype._backends = {};

/**
 * The list of user config files received from backends.
 */
PersistentStorage.prototype._userconfigs = [];

/**
 * The name of the currently active backend.
 */
PersistentStorage.prototype._activeBackend = '';

/**
 * The value of the type filter that's currently in effect.
 */
PersistentStorage.prototype._typeFilter = '';

/**
 * The currently displayed view type.
 */
PersistentStorage.prototype._viewType = 'thumbnail';

/**
 * On Xinha activation, this get's called so that we can prepare any resources
 * we need to function.
 */
PersistentStorage.prototype.onGenerateOnce = function () {
  // We use _prepareDialog to asynchronously load the dialog markup and then
  // perform necessary processing.
  this._prepareDialog();
};

function addClass(element, className) {
  var classRegex = new RegExp(' ?' + className + ' ?');

  if (!classRegex.test(element.className)) {
    element.className += ' ' + className;
  }
}

function removeClass(element, className) {
  var classRegex = new RegExp(' ?' + className + ' ?');

  if (classRegex.test(element.className)) {
    element.className = element.className.replace(classRegex, ' ');
  }
}

function toggleClass(element, className) {
  var classRegex = new RegExp(' ?' + className + ' ?');

  if (classRegex.test(element.className)) {
    element.className = element.className.replace(classRegex, ' ');
  } else {
    element.className += ' ' + className;
  }
}

/**
 * Once we're sure we have a backend that supports document functionality,
 * we'll load the interface that exposes it.
 */
PersistentStorage.prototype._registerDocumentUI = function () {
  if (this._documentEnabled) {
    // No need to repeated rebuild the UI.
    return;
  }

  this._documentEnabled = true;
  var self = this;

  var editor = this.editor;

  editor.config.registerButton({
      id       : 'newdocument',
      tooltip  : Xinha._lc( 'New Document', 'PersistentStorage' ),
      image    : [_editor_url + editor.config.imgURL + 'ed_buttons_main.png',0,5],
      textMode : true,
      action   : function() { self.newDocument(); }
    }
  );
  editor.config.registerButton({
      id       : 'opendocument',
      tooltip  : Xinha._lc( 'Open Document', 'PersistentStorage' ),
      image    : [_editor_url + editor.config.imgURL + 'ed_buttons_main.png',1,5],
      textMode : true,
      action   : function() { self.openDialog(); }
    }
  );
  editor.config.registerButton({
      id       : 'savedocument',
      tooltip  : Xinha._lc( 'Save Document', 'PersistentStorage' ),
      image    : [_editor_url + editor.config.imgURL + 'ed_buttons_main.png',9,1],
      textMode : true,
      action   : function() { self.saveDialog(); }
    }
  );
  editor.config.addToolbarElement('newdocument', 'fullscreen', 1);
  editor.config.addToolbarElement('opendocument', 'newdocument', 1);
  editor.config.addToolbarElement('savedocument', 'opendocument', 1);

  // Since this is after the editor load, we have to trigger an udate manually...
  editor._rebuildToolbar();
};

/**
 * Backend storage plugins should call this method so that they can be exposed
 * to the user.  Because of possible quirks in plugin loading, you should use
 * the following example code when registering.
 * @param name The name of the module
 * @param module The module instance
 * @param config Configuration information that tells us about the module
 * @param user_config AN object representing the user configuration loaded from
 * this module.
 * @example

 * PSBackend.prototype.onGenerateOnce = function () {
 *   // Register with the Persistent Storage plugin.
 *   this._registerBackend();
 * };
 * PSBackend.prototype._registerBackend = function(timeWaited) {
 *   var editor = this.editor;
 *   var self = this;
 * 
 *   if (!timeWaited) {
 *     timeWaited = 0;
 *   }
 * 
 *   // Retry over a period of ten seconds to register.  We back off exponentially
 *   // to limit resouce usage in the case of misconfiguration.
 *   var registerTimeout = 10000;
 * 
 *   if (timeWaited > registerTimeout) {
 *     // This is most likely a configuration error.  We're loaded and
 *     // PersistentStorage is not.
 *     return;
 *   }
 * 
 *   if (!editor.plugins['PersistentStorage'] ||
 *       !editor.plugins['PersistentStorage'].instance ||
 *       !editor.plugins['PersistentStorage'].instance.ready) {
 * 
 *     window.setTimeout(function() {
 *       self._registerBackend(timeWaited ? timeWaited*2 : 50);
 *     }, timeWaited ? timeWaited : 50);
 *
 *     return;
 *   }
 *   var PS = editor.plugins['PersistentStorage'].instance;
 *   
 *   // Support user configuration. This loading should be moved into PersistentStorage...
 *   this.loadDocument({URL:'', name:'config.js', key:'/config.js'}, function(json) {
 *                    var userconfig = json ? eval('(' + json + ')') : false;
 *                    PS.registerBackend('PSLocal', self, self.config, userconfig);
 *                    });
 */
PersistentStorage.prototype.registerBackend = function (name, module, config, user_config) {
  this._backends[name] = {module: module, config: config, name: name};

  // TODO I'd like something more than just whoever calls back first wins for ordering.
  if (!this._activeBackend) {
    this.setBackend(name);
  }

  if (config.capabilities.upload_operations &&
      config.capabilities.file_operations) {
    this._registerDocumentUI();
  }

  // Handle user configuration
  if (user_config) {
    this._userconfigs.push(user_config);
    this.configureUser();
  }

  this.updatePlacesDisplay();
}

/**
 * Go through the list of user configs and reconfigure Xinha.
 */
PersistentStorage.prototype.configureUser = function () {
  // Temp code does not handle user affinity
  var self = this;
  for (var index=0; index<this._userconfigs.length; ++index) {
    // A user config can have various options, we'll deal with each in turn.
    var config = this._userconfigs[index];

    for (var pluginName in (config.plugins || {})) {
      // Try to load each of the plugins requested by the user.
      Xinha.loadPlugin(pluginName, function() {
                // On successful load, we'll register the plugin with this instance.
                self.editor.registerPlugins([pluginName]);
                Xinha.refreshPlugin(self.editor.plugins[pluginName].instance);
                // Now that
                self.editor._rebuildToolbar();
              }, config.plugins[pluginName]);
    }
  }
}

/**
 * Present the user with a clean slate document.  Someday, we'll cycle through
 * a list of templates in storage.
 */
PersistentStorage.prototype.newDocument = function () {
  if (this.editor._doc.body.lastChild) {
    // There's already some content, let's make sure.
    if (!confirm(Xinha._lc('This will erase any unsaved content.  If you\'re certain, please click OK to continue.', 'PersistentStorage'))) {
      return;
    }
  }
  while (this.editor._doc.body.lastChild) {
    this.editor._doc.body.removeChild(this.editor._doc.body.lastChild);
  }
}

/**
 * Present a save dialog to the user.
 */
PersistentStorage.prototype.saveDialog = function () {
  var self = this;

  // Setup the dialog title
  var title = this.dialog.getElementById("h1");
  title.innerHTML = Xinha._lc('Save Document', 'PersistentStorage');

  // Setup the confirmation button and action.
  var button = this.dialog.getElementById("confirm");
  button.value = Xinha._lc('Save', 'PersistentStorage');
  button.onclick = function() {
    self.saveDocument();
  };

  // Enable and disable the buttons with filename input
  var nameinput = this.dialog.getElementById("filename");

  button.disabled = nameinput.value ? false : true;

  nameinput.onkeyup = nameinput.onkeypress = nameinput.onchange = function() {

    button.disabled = nameinput.value ? false : true;
  }

  this.showDialog({typeFilter:['document','html','text','folder'], styleFor:'documentsave'});
}

/**
 * Use the selected backend to save the current document.
 */
PersistentStorage.prototype.saveDocument = function () {
  var editor = this.editor;
  var module = this._backends[this._activeBackend].module;

  var filename = this.dialog.getElementById("filename");
  module.saveDocument(module.viewFilter, filename.value, editor.getHTML(), function() {
    module.cache = null;
  });

  this.dialog.hide();
}

/**
 * Present an open dialog to the user.
 */
PersistentStorage.prototype.openDialog = function () {
  var self = this;

  // Setup the dialog title
  var title = this.dialog.getElementById("h1");
  title.innerHTML = Xinha._lc('Open Document', 'PersistentStorage');

  // Setup the confirmation button and action.
  var button = this.dialog.getElementById("confirm");
  button.value = Xinha._lc('Open', 'PersistentStorage');
  button.onclick = function() {
    self.openDocument();
  };

  this.showDialog({typeFilter:['document','html','text','folder'], styleFor:'documentload'});
}

/**
 * Use the selected backend to load the selected entry into the editor.
 */
PersistentStorage.prototype.openDocument = function () {
  var editor = this.editor;

  var module = this._backends[this._activeBackend].module;
  module.loadDocument(this.selectedEntry[0], function(documentSource) {
                   editor.setHTML(documentSource);
                 });

  this.dialog.hide();
}

/**
 * @param options Configuration options that tweak the display of the dialog.
 * @param options.typeFilter {String[]} Object types to allow in the display.
 * Possibilities are 'image', 'media', 'document', and 'folder'.
 * @param options.styleFor {String} Possible stylings of the dialog.  This can
 * be 'insertion', 'link', 'documentsave', or 'documentload'.
 * @example
 * Interface Mockup
 *
 * ****************************************************************************
 * *                                                                          *
 * * #Places## #File Browser################################################# *
 * * #       # # ______________          ________________ ________________  # *
 * * #  ===  # # |/subdir     |  ^(Up)   |*Create Folder| |View as Icons^|  # *
 * * #  |*|  # # --------------          ---------------- ----------------  # *
 * * #  |*|  # #                                                            # *
 * * #  ===  # # ***********  ***********  ***********  ***********         # *
 * * #Server # # *  Thumb  *  *  Thumb  *  *  Thumb  *  *  Thumb  *         # *
 * * #       # # *         *  *         *  *         *  *         *         # *
 * * #  /\   # # *(Edit)   *  *(Edit)   *  *(Edit)   *  *(Edit)   *         # *
 * * # <  >  # # *(Delete) *  *(Delete) *  *(Delete) *  *(Delete) *         # *
 * * #  \/   # # ***********  ***********  ***********  ***********         # *
 * * # Gears # #                                                            # *
 * * #       # ############################################################## *
 * * #       #                                                                *
 * * #       # *Import-(Collapsed)******************************************* *
 * * #       #                                                                *
 * * #       # #File Name -or- File Details################################## *
 * * #       # #                                                            # *
 * * #       # #                                                            # *
 * * #       # #                                                            # *
 * * #       # #                                                            # *
 * * ######### ############################################################## *
 * *                                                                          *
 * ****************************************************************************
 */

PersistentStorage.prototype.showDialog = function (options) {
  // Create a reference to this PS instance to allow for asynchronous
  // continuation (if we're not ready.)
  var self = this;

  if (!this.ready) {
    window.setTimeout(function() {self.showDialog(options);}, 80);
    return;
  }
  
  // We hide and show the various elements of the dialog depending on usage.
  removeClass(this.dialog.getElementById("WWW"), 'hidden');
  addClass(this.dialog.getElementById("namefield"), 'hidden');
  addClass(this.dialog.getElementById("placeWww"), 'hidden');
  addClass(this.dialog.getElementById("placeBackend"), 'hidden');

  switch (options.styleFor) {
    case 'link':
      removeClass(this.dialog.getElementById("placeWww"), 'hidden');
      this.updatePlacesDisplay('shared_publish');
      break;
    case 'insertion':
      removeClass(this.dialog.getElementById("placeBackend"), 'hidden');
      this.updatePlacesDisplay('shared_publish');
      break;
    case 'documentsave':
      addClass(this.dialog.getElementById("WWW"), 'hidden');
      removeClass(this.dialog.getElementById("namefield"), 'hidden');
      removeClass(this.dialog.getElementById("placeBackend"), 'hidden');
      this.updatePlacesDisplay('file_operations');
      break;
    case 'documentload':
      addClass(this.dialog.getElementById("WWW"), 'hidden');
      removeClass(this.dialog.getElementById("placeBackend"), 'hidden');
      this.updatePlacesDisplay('file_operations');
      break;
  }

  var directories = this.dialog.getElementById("filters");
  var fileBrowser = this.dialog.getElementById("fileList");

  // Store the type filter so that view updates will correctly filter the contents.
  this._typeFilter = options.typeFilter;

  var module = this._backends[this._activeBackend].module;

  this.updateFileBrowser();
}

/**
 * Take the list of filters and build the contents of the select element.
 * @param filters {Filter[]} An array of filter choices to display to the user.
 * @param filters[n].value The unique key that represents this filter to the backend.
 * @param filters[n].display {String} A text string to display to the user to
 * represent the given filter.
 * @param select {HTMLElement} The select node to update.
 */
PersistentStorage.prototype.displayFilters = function(filters, viewFilter) {
  // Clear out the previous directory listing.
  var select = this.dialog.getElementById("filters");
  while (select.lastChild) {
    select.removeChild(select.lastChild);
  }

  // For each element in the array, we extract out the display text and the
  // value and put them into an option element to add to the select element.
  for (var index=0; index<filters.length; ++index) {
    var option = document.createElement('option');
    option.setAttribute("value", filters[index].value);
    if (filters[index].value == viewFilter) {
      option.setAttribute("selected", 'selected');
    }

    var text = document.createTextNode(filters[index].display);
    option.appendChild(text);

    select.appendChild(option);
  }
}

/**
 * Given an entry, build an html UI element.
 * @param entry An object representing a file entry.
 * @param entry.URL The URL where this entry is located, if available,
 * @param entry.thumbURL The URL of an image that represents a thumbnail
 * preview of this file.
 * @param entry.$type {String} The type of this entry.  This can be one of:
 * 'image', 'media', 'folder', 'document', 'html', or 'text'
 * @param entry.name The name of this entry to display to the user.
 */
PersistentStorage.prototype.buildThumbnail = function(entry) {
  var self = this;

  // We store the active module so that any elements built with data from this
  // module can call back to it.
  var module = this._backends[this._activeBackend].module;

  var fileBlock = document.createElement('div');
  fileBlock.entry = entry;
  fileBlock.className = 'file';


  // Use a closure here to make sure that the onclick handler has the
  // right binding,
  Xinha._addEvent(fileBlock, "click", function(ev) {
    ev = ev || window.event;
    Xinha._stopEvent(ev);
    if (self.selectedEntry) {
      // Unselect
      removeClass(self.selectedEntry[1], 'selected');
      if (self.selectedEntry[1] == fileBlock) {
        // Allow click to toggle.
        self.selectedEntry = null;
        return;
      }
    }
    self.selectedEntry = [entry, fileBlock];
    self.selectedEntry[1].className += ' selected';

    var filename = self.dialog.getElementById('filename');
    filename.value = entry.name;

    var confirmbutton = self.dialog.getElementById('confirm');
    confirmbutton.disabled = false;
  });

  var image = document.createElement('img');
  image.className = 'icon';
  if (entry.$type == 'image') {
    image.className = 'thumb';
    image.setAttribute('src', entry.URL);
  } else if (entry.$type == 'folder') {
    image.setAttribute('src', entry.thumbURL || entry.URL || this.editor.imgURL('images/tango/32x32/places/folder.png'));
  } else if (entry.$type == 'document') {
    image.setAttribute('src', entry.thumbURL || entry.URL || this.editor.imgURL('images/tango/32x32/mimetypes/text-html.png'));
  } else if (entry.$type == 'html') {
    image.setAttribute('src', entry.thumbURL || entry.URL || this.editor.imgURL('images/tango/32x32/mimetypes/text-html.png'));
  } else if (entry.$type == 'txt') {
    image.setAttribute('src', entry.thumbURL || entry.URL || this.editor.imgURL('images/tango/32x32/mimetypes/text-g-generic.png'));
  } else {
    image.setAttribute('src', entry.thumbURL || entry.URL || this.editor.imgURL('images/tango/32x32/mimetypes/x-office-document.png'));
  }
    image = fileBlock.appendChild(image);


  var deleteIcon = document.createElement('img');
  deleteIcon.className = 'action delete';
  deleteIcon.setAttribute('alt', Xinha._lc('Delete', 'PersistentStorage'));
  deleteIcon.setAttribute('title', Xinha._lc('Delete', 'PersistentStorage'));
  deleteIcon.setAttribute('src', this.editor.imgURL('images/tango/16x16/places/user-trash.png'));
  deleteIcon = fileBlock.appendChild(deleteIcon);

  Xinha._addEvent(deleteIcon, "click", function(ev) {
    ev = ev || window.event;
    Xinha._stopEvent(ev);
    module.deleteEntry(entry, function(success) {
      module.cache = null;
      if (success) {
        fileBlock.parentNode.removeChild(fileBlock);
      } else {
        alert('Error deleting file ' + entry.name);
      }
    });
  });


  var fileName = document.createElement('p');
  fileName.className = 'filename';
  var fileNameText = document.createTextNode(entry.name);
  fileNameText = fileName.appendChild(fileNameText);
  fileName = fileBlock.appendChild(fileName);

  // Use a closure here to make sure that the onclick handler has the
  // right binding,
  fileName.onclick = function(fileName) {
    return function(ev) {
      ev = ev || window.event;
      Xinha._stopEvent(ev);
      fileName.style.border = '1px solid red';
      return false;
    };
  }(fileName);

  var copyIcon = document.createElement('img');
  copyIcon.className = 'action copy';
  copyIcon.setAttribute('src', this.editor.imgURL('images/tango/16x16/actions/edit-copy.png'));
  copyIcon.setAttribute('alt', Xinha._lc('Copy', 'PersistentStorage'));
  copyIcon.setAttribute('title', Xinha._lc('Copy', 'PersistentStorage'));

  Xinha._addEvent(copyIcon, "click", function(ev) {
    ev = ev || window.event;
    Xinha._stopEvent(ev);
    module.copyEntry(entry, function(success, newentry) {
      module.cache = null;
      if (success) {
        fileBlock.parentNode.appendChild(self.buildThumbnail(newentry));
      } else {
        alert('Error copying file ' + entry.name);
      }
    });
  });

  copyIcon = fileBlock.appendChild(copyIcon);

  return fileBlock;
}

/**
 * Build up the file browser display in the element.
 * @param metadata {Entry[]} A list of entries to display in the file browser.
 * @param metadata[n].URL The URL where this entry is located, if available,
 * @param metadata[n].thumbURL The URL of an image that represents a thumbnail
 * preview of this file.
 * @param metadata[n].$type {String} The type of this entry.  This can be one of:
 * 'image', 'media', 'folder', 'document', 'html', or 'text'
 * @param metadata[n].name The name of this entry to display to the user.
 */
PersistentStorage.prototype.displayBrowser = function(metadata) {

  // The div element that contains the file browser.
  var div = this.dialog.getElementById("fileList");

  // Clear out the previous directory listing.
  while (div.lastChild) {
    div.removeChild(div.lastChild);
  }

  var editor = this.editor;
  var self = this;

  for (var index=0; index<metadata.length; ++index) {
      switch (this._viewType) {
        case 'listing':
          break;
        case 'thumbnail':
        default:
          var thumb = div.appendChild(this.buildThumbnail(metadata[index]));

          // If jQuery is loaded with the draggable and droppable UI controls,
          // we'll enhance our interface.
          if ($ && $.ui && $.ui.draggable && $.ui.droppable) {
            // Make the objects draggable, but don't allow them to just hang
            // around.
            $(thumb).draggable({revert:true});

            // All folders become drop targets to allow for easier moving
            // around.
            if (metadata[index].$type == 'folder') {
              // Just in case Xinha is used on a page with other draggables, we
              // need to limit the targets we accept.
              $(thumb).droppable({accept: '*',
                drop: function(e, ui) {

                  // Set up a reference to the drop target so that we can move
                  // all dragged items into it.
                  var target = this;

                  ui.draggable.each(function() {
                    // We extract the entry reference from the DOM node.
                    var file = this;
                    var entry = file.entry;

                    // TODO: Whoagh... What if our backend is a drop target?
                    var module = self._backends[self._activeBackend].module;

                    // This only works intra-module...
                    module.moveEntry(entry, target.entry, function(success) {
                      module.cache = null;
                      if (success) {
                        file.parentNode.removeChild(file);
                      } else {
                        alert('Error deleting file ' + entry.name);
                      }
                    });
                  });
                }
              });
            }
          }
      }
  }

  // If the browser is empty, we'll display an empty message
  if (!div.firstChild) {
    var message = document.createTextNode('No files found');
    div.appendChild(message);
  }
}

/**
 * Update the file browser according to the latest criteria.
 */
PersistentStorage.prototype.updateFileBrowser = function() {
  var self = this;

  var module = this._backends[this._activeBackend].module;

  var buildDialog = function(cache) {
    // We cache the module data to save server roundtrips.
    module.cache = cache;

    // The backends are expected to process their cached data to create
    // two data structures for us.  The first one is a list of filters
    // used to keep the amount of data manageable in the interface.
    // (e.g. directories, or tags, etc.)
    var filters = module.getFilters(module.cache);

    // Now that we have valid filter data, we setup the initial filter.
    var resetFilter = true;
    if (module.viewFilter) {
      for (var index=0; index<filters.length; ++index) {
        if (filters[index].value == module.viewFilter) {
          resetFilter = false;
        }
      }
    }

    if (resetFilter) {
      module.viewFilter = filters[0].value;
    }

    // The second data structure we need is a list of metadata about
    // each file so that we can generate the appropriate listing.
    var metadata = module.getMetadata(module.cache, module.viewFilter, self._typeFilter);

    self.dialog.show();

    self.displayFilters(filters, module.viewFilter);
    self.displayBrowser(metadata);
  };

  if (module.cache) {
    buildDialog(module.cache);
    return;
  }

  module.loadData(buildDialog);
}

/**
 * Prepare all external resources necessary to display the dialog.
 */
PersistentStorage.prototype._prepareDialog = function() {
  var self = this;
  var editor = this.editor;

  if (!this.html) {
    Xinha._getback(Xinha.getPluginDir("PersistentStorage") + "/dialog.html",
                   function(getback) { 
                     self.html = getback;
                     self._prepareDialog();
                   });
    return;
  }

  this.dialog = new Xinha.Dialog(editor, this.html, "PersistentStorage", 
                                 {width: 800,
                                  closeOnEscape: true,
                                  resizable: true,
                                  centered: true,
                                  modal: true
                                 });


  // The cancel dialog just hides the dialog.
  this.dialog.getElementById("cancel").onclick = function() {
    self.dialog.hide();
  };

  // Directory creation prompts the user for a directory name.
  this.dialog.getElementById("dirCreate").onclick = function() {
    var dirname = prompt(Xinha._lc('Please enter the name of the directory you\'d like to create.', 'PersistentStorage'));
    if (dirname) {
      var module = self._backends[self._activeBackend].module;
      module.makeFolder(module.viewFilter, dirname, function(success) {
        if (success) {
          module.cache = null;
          self.updateFileBrowser();
        }
      });
    }
  };

  // The import field is hidden by default, and clicking on it toggles that state.
  var importLegend = this.dialog.getElementById("importlegend");
  var importDiv = this.dialog.getElementById("import");
  importLegend.onclick = function() {
    toggleClass(importDiv, 'collapsed');
  };

  // Since the WWW icons is a static element, we hook up it's click handler
  // here, rather than when showing the dialog
  var iconWWW = this.dialog.getElementById("WWW");
  Xinha._addEvent(iconWWW, "click", function(ev) {
    ev = ev || window.event;
    Xinha._stopEvent(ev);

    for (var indexEl=iconWWW.parentNode.firstChild; indexEl; indexEl = indexEl.nextSibling) {
      if (1 != indexEl.nodeType) {
        // We only process HTML nodes.
        continue;
      }

      removeClass(indexEl, 'selected');
    }

    addClass(iconWWW, 'selected');
    addClass(self.dialog.getElementById("placeBackend"), 'hidden');
    removeClass(self.dialog.getElementById("placeWww"), 'hidden');
  });

  // Clicking inside of the file browser will deselect all files.
  var fileBrowser = this.dialog.getElementById("fileList");
  Xinha._addEvent(fileBrowser, "click", function(ev) {
    for (var file=fileBrowser.firstChild; file; file=file.nextSibling) {
      removeClass(file, 'selected');
    }
  });

  // Setup the filter control so that user selection will refilter the shown
  // files.
  var filterControl = this.dialog.getElementById("filters");
  filterControl.onchange = function() {
    var module = self._backends[self._activeBackend].module;
    module.viewFilter = filterControl.options[filterControl.selectedIndex].value;
    self.updateFileBrowser();
  };

  // Setup the viewtype control to change the internals of the file list.
  var viewType = this.dialog.getElementById("viewType");
  viewType.onchange = function() {
    self._viewType = viewType.options[viewType.selectedIndex].value;
    self.updateFileBrowser();
  }

  this.ready = true;
};

/**
 * Update the list of available backends.
 */
PersistentStorage.prototype.updatePlacesDisplay = function(capability) {
  var self = this;
  var placesBrowser = this.dialog.getElementById("placesList");

  // Clear out any empty text nodes in the places browser.
  while (placesBrowser.firstChild && 3 == placesBrowser.firstChild.nodeType) {
    placesBrowser.removeChild(placesBrowser.firstChild);
  }

  var WWW = placesBrowser.firstChild;
  while (WWW.nextSibling) {
    placesBrowser.removeChild(WWW.nextSibling);
  }

  for (var backendName in this._backends) {
    if (capability && !this._backends[backendName].config.capabilities[capability]) {
      continue;
    }


    var thumb = placesBrowser.appendChild(this.placesIcon(this._backends[backendName]));

    if ($ && $.ui && $.ui.draggable && $.ui.droppable) {
        // Just in case Xinha is used on a page with other draggables, we
        // need to limit the targets we accept.
        $(thumb).droppable({accept: '*',
          drop: function(e, ui) {

            // Set up a reference to the drop target so that we can move
            // all dragged items into it.
            var target = this;

            ui.draggable.each(function() {
              // We extract the entry reference from the DOM node.
              var file = this;
              var entry = file.entry;

              // TODO: Whoagh... What if our backend is a drop target?
              var module = self._backends[self._activeBackend].module;

              module.loadDocument(entry, function(documentSource) {
                target.backend.module.saveDocument(module.viewFilter, entry.name, documentSource, function() {
                  target.backend.module.cache = null;
                  module.deleteEntry(entry, function(success) {
                    module.cache = null;
                    if (success) {
                      file.parentNode.removeChild(file);
                    } else {
                      alert('Error deleting file ' + entry.name);
                    }
                  });
                });
              });
            });
          }
        });
    }
  }
}

/**
 * Set the currently active backend
 */
PersistentStorage.prototype.setBackend = function(backendName) {
  if (this._activeBackend == backendName) {
    // Save ourselves some work.
    return;
  }
  this._activeBackend = backendName;
  var module = this._backends[this._activeBackend];
  if (module.config.capabilities.import_operations) {
    Xinha._removeClass(this.dialog.getElementById("import"),'hidden');

    var importUI = this.dialog.getElementById("importui");
    while (importUI.firstChild) {
      // Clear out the import UI of any other backend.
      importUI.removeChild(importUI.firstChild);
    }

    module.module.buildImportUI(this.dialog, importUI);
  } else {
    Xinha._addClass(this.dialog.getElementById("import"),'hidden');
  }
}

/**
 * Build a UI representation of the backend.
 */
PersistentStorage.prototype.placesIcon = function(backend) {
  var self = this;

  var placesBlock = document.createElement('div');
  placesBlock.backend = backend;
  placesBlock.className = 'file';
  if (this._activeBackend == backend.name) {
    placesBlock.className += ' selected';
  }


  // Use a closure here to make sure that the onclick handler has the
  // right binding,
  Xinha._addEvent(placesBlock, "click", function(ev) {
    ev = ev || window.event;
    Xinha._stopEvent(ev);

    for (var indexEl=placesBlock.parentNode.firstChild; indexEl; indexEl = indexEl.nextSibling) {
      if (1 != indexEl.nodeType) {
        // We only process HTML nodes.
        continue;
      }

      removeClass(indexEl, 'selected');
    }

    addClass(placesBlock, 'selected');
    removeClass(self.dialog.getElementById("placeBackend"), 'hidden');
    addClass(self.dialog.getElementById("placeWww"), 'hidden');
    self.setBackend(backend.name);
    self.updateFileBrowser();
  });

  var image = document.createElement('img');
  image.className = 'icon';
  image.setAttribute('src', backend.config.thumbURL || this.editor.imgURL('images/tango/32x32/places/network-server.png'));
  image = placesBlock.appendChild(image);


  var placesName = document.createElement('p');
  placesName.className = 'filename';
  var placesNameText = document.createTextNode(backend.config.displayName || backend.name);
  placesNameText = placesName.appendChild(placesNameText);
  placesName = placesBlock.appendChild(placesName);

  return placesBlock;
}
/**
 * The Xinha text editor
 * @name Xinha
 */
var Xinha = window.Xinha;
/**
 * We override the native _insertImage function of Xinha.  We call the instance
 * version of the method in order to setup the proper context.
 * @param image TODO Figure out what this is :-D
 */
Xinha.prototype._insertImage = function(image) {
  var editor = this;
  var PS = editor.plugins['PersistentStorage'].instance;

  // Setup the dialog title
  var title = PS.dialog.getElementById("h1");
  title.innerHTML = Xinha._lc('Insert Image', 'PersistentStorage');

  // Setup the confirmation button and action.
  var button = PS.dialog.getElementById("confirm");
  button.value = Xinha._lc('Insert', 'PersistentStorage');
  button.onclick = function() {
    PS.insertImage();
  };

  PS.showDialog({typeFilter:['image','folder'],styleFor:'insertion'});
}

/**
 * Take a selected image from the dialog and insert it into the document.
 */
PersistentStorage.prototype.insertImage = function() {
  var editor = this.editor;

  this.dialog.hide();

  var image = editor._doc.createElement('img');
  if (this.selectedEntry) {
    image.setAttribute('src', this.selectedEntry[0].URL);
  } else {
    image.setAttribute('src', this.dialog.getElementById("URL").value);
  }
  for (var prop in {width:'',height:'',margin:'',padding:'',border:'',borderColor:'',backgroundColor:''}) {
    var val =  this.dialog.getElementById('image_' + prop).value.replace(/^\s+$/,'');
    if (val) {
      image.style[prop] = val;
    }
  }
  editor.insertNodeAtSelection(image);

  var range = editor.createRange(editor.getSelection());
  range.collapse(false);

}
})(window.jQuery);
//Xinha.prototype._linkFile = function(link) {
//
//    var editor = this;
//    var outparam = {"editor" : this, param : null};
//    if (typeof link == "undefined") {
//        link = this.getParentElement();
//        if (link) {
//            if (/^img$/i.test(link.tagName))
//                link = link.parentNode;
//            if (!/^a$/i.test(link.tagName))
//                link = null;
//        }
//    }
//    if (!link) {
//        var sel = editor.getSelection();
//        var range = editor.createRange(sel);
//        var compare = 0;
//        if (Xinha.is_ie) {
//            if ( sel.type == "Control" )
//                compare = range.length;
//            else
//                compare = range.compareEndPoints("StartToEnd", range);
//        } else {
//            compare = range.compareBoundaryPoints(range.START_TO_END, range);
//        }
//        if (compare == 0) {
//            alert(Xinha._lc("You must select some text before making a new link.", 'PersistentStorage'));
//            return;
//        }
//        outparam.param = {
//            f_href : '',
//            f_title : '',
//            f_target : '',
//            f_usetarget : editor.config.makeLinkShowsTarget,
//            baseHref: editor.config.baseHref
//        };
//    } else
//        outparam.param = {
//            f_href   : Xinha.is_ie ? link.href : link.getAttribute("href"),
//            f_title  : link.title,
//            f_target : link.target,
//            f_usetarget : editor.config.makeLinkShowsTarget,
//            baseHref: editor.config.baseHref
//        };
//
//    Dialog(this.config.PersistentStorage.manager+'&mode=link', function(param){
//        if (!param)
//            return false;
//        var a = link;
//        if (!a) try {
//            editor._doc.execCommand("createlink", false, param.f_href);
//            a = editor.getParentElement();
//            var sel = editor.getSelection();
//            var range = editor.createRange(sel);
//            if (!Xinha.is_ie) {
//                a = range.startContainer;
//                if (!/^a$/i.test(a.tagName)) {
//                    a = a.nextSibling;
//                    if (a == null)
//                        a = range.startContainer.parentNode;
//                }
//            }
//        } catch(e) {}
//        else {
//            var href = param.f_href.trim();
//            editor.selectNodeContents(a);
//            if (href == "") {
//                editor._doc.execCommand("unlink", false, null);
//                editor.updateToolbar();
//                return false;
//            }
//            else {
//                a.href = href;
//            }
//        }
//        if (!(a && /^a$/i.test(a.tagName)))
//            return false;
//        a.target = param.f_target.trim();
//        a.title = param.f_title.trim();
//        editor.selectNodeContents(a);
//        editor.updateToolbar();
//    }, outparam);
//};
//
//function shortSize(cssSize)
//{
//    if(/ /.test(cssSize))
//    {
//        var sizes = cssSize.split(' ');
//        var useFirstSize = true;
//        for(var i = 1; i < sizes.length; i++)
//        {
//            if(sizes[0] != sizes[i])
//            {
//                useFirstSize = false;
//                break;
//            }
//        }
//        if(useFirstSize) cssSize = sizes[0];
//    }
//
//    return cssSize;
//}
//
//function convertToHex(color) {
//
//    if (typeof color == "string" && /, /.test.color)
//        color = color.replace(/, /, ','); // rgb(a, b) => rgb(a,b)
//
//    if (typeof color == "string" && / /.test.color) { // multiple values
//        var colors = color.split(' ');
//        var colorstring = '';
//        for (var i = 0; i < colors.length; i++) {
//            colorstring += Xinha._colorToRgb(colors[i]);
//            if (i + 1 < colors.length)
//                colorstring += " ";
//        }
//        return colorstring;
//    }
//
//    return Xinha._colorToRgb(color);
//}
//});

// Xinha._lc("Hello  There A ")
// Xinha._lc("Hello \" There B \"")
// Xinha._lc('Hello    There C ')
// Xinha._lc('Hello \' There D \'')


