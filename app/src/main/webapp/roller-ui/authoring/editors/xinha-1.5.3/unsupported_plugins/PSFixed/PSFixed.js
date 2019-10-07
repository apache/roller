/**
 * PSFixed PSFixed.js file.
 * This plugin is a fake persistent storage backed.  It is configured with a
 * fixed list of stored objects and presents them to the user for insertion.
 * A sample config is below:
 *
 * PSFixed.config = {
 *     'xinha.png': {
 *         $type: 'image',
 *         URL: 'http://trac.xinha.org/chrome/common/trac_banner.png'
 *     },
 *     'browser': {
 *         $type: 'folder',
 *         'firefox.png': {
 *             $type: 'image',
 *             URL: 'http://www.mozilla.com/img/tignish/firefox/vs-firefox-logo.png'
 *         }
 *     }
 * }
 */
(function() {
var PSFixed = window.PSFixed = function PSFixed(editor) {
  this.editor = editor;
}

PSFixed._pluginInfo = {
    name          : "PSFixed",
    version       : "2.0",
    developer     : "Douglas Mayle",
    developer_url : "http://xinha.org",
    license       : "BSD"
};

PSFixed.prototype.onGenerateOnce = function () {
  // We use _prepareDialog to asynchronously load the dialog markup and then
  // perform necessary processing.
  this._registerBackend();
};

PSFixed.prototype._registerBackend = function(timeWaited) {
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
  editor.plugins['PersistentStorage'].instance.registerBackend('PSFixed', this);
}

PSFixed.prototype.loadData = function (asyncCallback) {
  // We don't expect the user to set the type on the root folder, so we set it
  // ourselves.
  if (!this.config.$type) {
    this.config.$type = 'folder';
  }
  asyncCallback(this.config);
}

var treeRecurse = function treeRecurse(tree, callback, root) {
  if (typeof root == 'undefined') {
    root = '/';
    callback('/', '', tree);
  }

  for (var key in tree) {
    callback(root, key, tree[key]);

    if (tree[key].$type == 'folder') {
      treeRecurse(tree[key], callback, root + key + '/');
    }
  }
};

PSFixed.prototype.getFilters = function(dirTree) {
  var filters = [];

  treeRecurse(dirTree, function(path, key, value) {
      if (value.$type != 'folder') {
        return;
      }

      var filePath = key.length ? path + key + '/' : path;
      filters.push({
        value: filePath,
        display: filePath
      });
  });

  return filters;
}

PSFixed.prototype.getMetadata = function(dirTree, filterPath) {
  var editor = this.editor;
  var self = this;

  var metadata = [];

  treeRecurse(dirTree, function(path, key, value) {
    if (!value.$type || !key) {
      // This is a builtin property of objects, not one returned by the
      // backend.
      return;
    }

    if (path != filterPath) {
      return;
    }

    if (value.$type == 'folder') {
      metadata.push({
        URL: self.editor.imgURL('folder.gif', 'PersistentStorage'),
        name: key,
        key: path + key,
        $type: value.$type
      });
    } else {
      metadata.push({
        URL: value.URL,
        name: key,
        key: path + key,
        $type: value.$type
      });
    }
  });

  return metadata;
}

})();
