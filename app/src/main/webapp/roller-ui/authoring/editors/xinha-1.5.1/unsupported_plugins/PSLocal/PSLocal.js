/**
 * PSLocal PSLocal.js file.
 * This plugin is a Gears based local persistent storage backend.
 */
(function() {
var PSLocal = window.PSLocal = function PSLocal(editor) {
  this.editor = editor;

  this.config = {
    capabilities: {
      directory_operations: true,
      file_operations: true,
      image_operations: false,
      upload_operations: true,
      import_operations: false,
      user_publish: false,
      shared_publish: false,
      user_storage: true
    },
    displayName: 'Local'
  }
}

PSLocal._pluginInfo = {
    name          : "PSLocal",
    version       : "2.0",
    developer     : "Douglas Mayle",
    developer_url : "http://xinha.org",
    license       : "BSD"
};

PSLocal.prototype.onGenerateOnce = function () {
  // We use _prepareBackend to asynchronously load the Gears backend.
  this._prepareBackend();
};

PSLocal.prototype._showGearsButton = function() {
  var self = this;
  var editor = this.editor;

  editor.config.registerButton({
      id       : 'localstorage',
      tooltip  : Xinha._lc( 'Learn About Local Storage', 'PSLocal' ),
      image    : [_editor_url + editor.config.imgURL + 'ed_buttons_main.png',2,8],
      textMode : true,
      action   : function() { self.learnDialog(); }
    }
  );
  editor.config.addToolbarElement('localstorage', 'fullscreen', 1);

  // Since this is after the editor load, we have to trigger an udate manually...
  editor._rebuildToolbar();
}

PSLocal.prototype._prepareBackend = function() {
  var self = this;
  var editor = this.editor;

  if (!this.gears_init) {
    this.gears_init = true;
    Xinha._loadback(Xinha.getPluginDir("PSLocal") + "/gears_init.js",
                   function() { 
                     self._prepareBackend();
                   });
    return;
  }

  if (!window.google || !google.gears) {
    // No gears, so no need to register.  We'll register a button, however, to
    // enable users to learn about the support we offer.
    this._showGearsButton();
    return;
  }

  if (!google.gears.factory.hasPermission) {
    if (!google.gears.factory.getPermission('Xinha', editor.imgURL('/images/xinha-small-icon.gif'), Xinha._lc( 'Enable Gears in order to use local document storage and configuration.', 'PSLocal' ))) {
      // The user has denied access to Gears.  We'll give them a UI to allow
      // them to change their mind.
      this._showGearsButton();
      return;
    }
  }

  this.workerPool = google.gears.factory.create('beta.workerpool', '1.0');  

  this.remoteStorageWorker = this.workerPool.createWorkerFromUrl("http://xinhadocs.org/worker.js");

  this._registerBackend();
}

PSLocal.prototype.learnDialog = function(timeWaited) {
  var self = this;
  var editor = this.editor;

  if (!this.html) {
    Xinha._getback(Xinha.getPluginDir("PSLocal") + "/dialog.html",
                   function(getback) { 
                     self.html = getback;
                     self.learnDialog();
                   });
    return;
  }

  if (this.dialog) {
    this.dialog.show();
    return;
  }

  this.dialog = new Xinha.Dialog(editor, this.html, "PersistentStorage", 
                                 {width: 700,
                                  closeOnEscape: true,
                                  resizable: true,
                                  centered: true,
                                  modal: true
                                 });

  var link = this.dialog.getElementById('GearsLink');

  // Tack on our URL so that Google will return here after installation.
  link.href += location.href;

  var button = this.dialog.getElementById("confirm");
  if (window.google && google.gears) {
    Xinha._addClass(this.dialog.getElementById('InstallText'), 'hidden');
    // The user has gears installed, but has denied us access to it, so we'll
    // give them the option to change their mind.
    button.value = Xinha._lc('Enable', 'PSLocal');

    button.onclick = function() {
      // The user gave us permission, so we'll reload the page.
      if (confirm(Xinha._lc('This will reload the page, causing you to lose any unsaved work.  Press "OK" to reload.', 'PSLocal' ))) {
        window.location.reload(true);
      }
    }
  } else {
    Xinha._addClass(this.dialog.getElementById('EnableText'), 'hidden');
    // Gears isn't installed, so we'll build the dialog to prompt installation.
    button.value = Xinha._lc('Install', 'PSLocal');

    button.onclick = function() {
      location.href = link.href;
    }
  }

  var cancel = this.dialog.getElementById('cancel');
  cancel.onclick = function() {
    self.dialog.hide();
  }

  this.dialog.show();
}

PSLocal.prototype._registerBackend = function(timeWaited) {
  var editor = this.editor;
  var self = this;

  if (!timeWaited) {
    timeWaited = 0;
  }

  // Retry over a period of ten seconds to register.  We back off exponentially
  // to limit resouce usage in the case of misconfiguration.
  var registerTimeout = 10000;

  if (timeWaited > registerTimeout) {
    // This is most likely a configuration error.  We're loaded and
    // PersistentStorage is not.
    return;
  }

  if (!editor.plugins['PersistentStorage'] ||
      !editor.plugins['PersistentStorage'].instance ||
      !editor.plugins['PersistentStorage'].instance.ready) {

    window.setTimeout(function() {self._registerBackend(timeWaited ? timeWaited*2 : 50);}, timeWaited ? timeWaited : 50);
    return;
  }
  var PS = editor.plugins['PersistentStorage'].instance;
  var self = this;
  
  this.config.thumbURL = this.editor.imgURL('images/tango/32x32/places/folder.png');
  this.loadDocument({URL:'', name:'config.js', key:'/config.js'}, function(json) {
                   var userconfig = json ? eval('(' + json + ')') : false;
                   PS.registerBackend('PSLocal', self, self.config, userconfig);
                   });
}

PSLocal.prototype.loadDocument = function(entry, asyncCallback) {

  this.workerPool.onmessage = function(a, b, message) {
    if (!message.body || !message.body.authorized) {
      // Fail
      asyncCallback('');
    }

    if (message.body.response) {
      asyncCallback(message.body.response);
    } else if (entry.URL) {
      Xinha._getback(entry.URL,
                 function(documentSource) { 
                   asyncCallback(documentSource);
                 });
    } else {
      // Oops, no data :-(
      asyncCallback('');
    }
  }

  this.workerPool.sendMessage({func: 'loadDocument', entry: entry}, this.remoteStorageWorker);
}

PSLocal.prototype.loadData = function (asyncCallback) {
  this.workerPool.onmessage = function(a, b, message) {
    if (!message.body || !message.body.authorized) {
      // Fail
      asyncCallback('');
    }

    asyncCallback({dirs: message.body.dirs, files: message.body.files});
  }

  this.workerPool.sendMessage({func: 'loadData'}, this.remoteStorageWorker);
}

PSLocal.prototype.getFilters = function(filedata) {
  // Clear out the previous directory listing.
  var filters = [], paths = {};
  var dirList = filedata.dirs;

  for (var index=0; index<dirList.length; ++index) {
    filters.push({value:dirList[index],display:dirList[index]});
  }

  // We can't return an empty filter list.
  if (!filters.length) {
    filters.push({value:'/',display:'/'});
  }

  return filters;
}

PSLocal.prototype.getMetadata = function(filedata, filterPath) {
  var editor = this.editor;
  var self = this;

  var metadata = [];
  var fileList = filedata.files;

  for (var index=0; index<fileList.length; ++index) {
      // Gah perform file splitting here..
    var pathpart = fileList[index].fullpath.split('/');
    if (pathpart.length > 2) {
      pathpart = pathpart.slice(0,pathpart.length-1).join('/');
    } else {
      pathpart = '/';
    }

    var filepart = fileList[index].fullpath.split('/').slice(-1)[0];
    if (filterPath == pathpart) {
      metadata.push({
        URL: fileList[index].url,
        thumbURL: editor.imgURL('images/tango/32x32/mimetypes/text-x-generic.png'),
        name: filepart,
        key: fileList[index].fullpath,
        $type: fileList[index].filetype
      });
    }
  }

  var dirList = filedata.dirs;

  for (var index=0; index<dirList.length; ++index) {
    // We have to be careful to filter out when filterPath IS the current intry in dirList
    if (1 == filterPath.length) {
        // If the filter path is the root '/' (ie. length of one) then we
        // select the directory if a) It is not also of length one (ie. '/')
        // and b) has only one slash in it.  (We check the number of slashes by
        // splitting the string by '/' and subtracting one from the length.)
        var matches = dirList[index].length > 1 && dirList[index].split('/').length == 2;
    } else {
        // Chop the last component of the directory and compare against the filter.
        var matches = dirList[index].split('/').slice(0,-1).join('/') == filterPath;
    }
    if (matches) {
      metadata.push({
        name: dirList[index].split('/').slice(-1),
        key: dirList[index],
        $type: 'folder'
      });
    }
  }

  return metadata;
}

PSLocal.prototype.saveDocument = function(parentpath, filename, documentSource, asyncCallback) {
  this.workerPool.onmessage = function(a, b, message) {
    if (!message.body || !message.body.authorized) {
      // Fail
      asyncCallback(false);
    }

    if (asyncCallback) {
      asyncCallback(message.body.response);
    }
  }

  this.workerPool.sendMessage({func: 'saveDocument', parentpath: parentpath, filename: filename, content:documentSource}, this.remoteStorageWorker);
}
PSLocal.prototype.deleteEntry = function(entry, asyncCallback) {
  this.workerPool.onmessage = function(a, b, message) {
    if (!message.body || !message.body.authorized) {
      // Fail
      asyncCallback(false);
    }

    if (asyncCallback) {
      asyncCallback(message.body.response);
    }
  }

  this.workerPool.sendMessage({func: 'deleteEntry', entry: entry}, this.remoteStorageWorker);
}

PSLocal.prototype.makeFolder = function(currentPath, folderName, asyncCallback) {
  this.workerPool.onmessage = function(a, b, message) {
    if (!message.body || !message.body.authorized) {
      // Fail
      asyncCallback(false);
    }

    if (asyncCallback) {
      asyncCallback(true);
    }
  }

  this.workerPool.sendMessage({func: 'makeFolder', parentpath: currentPath, dirname: folderName}, this.remoteStorageWorker);
}

PSLocal.prototype.copyEntry = function(entry, asyncCallback) {
  this.workerPool.onmessage = function(a, b, message) {
    if (!message.body || !message.body.authorized) {
      // Fail
      asyncCallback(false);
    }

    if (asyncCallback) {
      asyncCallback(message.body.response, message.body.entry);
    }
  }

  this.workerPool.sendMessage({func: 'copyEntry', entry: entry}, this.remoteStorageWorker);
}

})();
