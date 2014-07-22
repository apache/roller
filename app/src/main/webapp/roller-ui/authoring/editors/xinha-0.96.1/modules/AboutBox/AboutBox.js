// Paste Plain Text plugin for Xinha

// Distributed under the same terms as Xinha itself.
// This notice MUST stay intact for use (see license.txt).
(function(){
Xinha.plugins.AboutBox = AboutBox;
function AboutBox(editor) {
  this.editor = editor;
}

AboutBox._pluginInfo = {
  name          : "AboutBox",
  developer     : "The Xinha Core Developer Team"
};

AboutBox.prototype._lc = function(string) {
  return Xinha._lc(string, 'AboutBox');
};


AboutBox.prototype._prepareDialog = function()
{
  var self = this;
  var editor = this.editor;
  
  Xinha.loadStyle ('about.css', 'AboutBox', 'aboutCSS');
/// Now we have everything we need, so we can build the dialog.
  this.dialog = new Xinha.Dialog(editor, AboutBox.html, 'Xinha',{width:600})

  this.dialog.getElementById('close').onclick = function() { self.dialog.hide()};
  this.dialog.getElementById('xinha_logo').src = _editor_url + 'images/xinha_logo.gif';

  var tabs = this.dialog.getElementsByClassName('tab');
  this.currentTab = tabs[0];
  tabs.forEach(function(tab){
    //alert (tab);
    tab.onclick = function() {
      if (self.currentTab)
      {
        Xinha._removeClass(self.currentTab,'tab-current');
        self.dialog.getElementById(self.currentTab.rel).style.display = 'none';
      } 
      Xinha._addClass(tab, 'tab-current');
      tab.blur();
      self.currentTab = tab;
      self.dialog.getElementById(tab.rel).style.display = 'block';
    }
  })
  this.fillPlugins();
  this.fillVersion();
  this.dialog.onresize = function ()
  {
    this.getElementById("content").style.height = 
    parseInt(this.height,10) // the actual height of the dialog
    - this.getElementById('h1').offsetHeight // the title bar
    - this.getElementById('buttons').offsetHeight // the buttons
    - 100 // we have a padding at the bottom, gotta take this into acount
    + 'px'; // don't forget this ;)

    //this.getElementById("content").style.width =(this.width - 2) + 'px'; // and the width
  }
};
AboutBox.prototype.fillPlugins = function()
{
  var e = this.editor;
  var tbody = this.dialog.getElementById('plugins_table');
  var tr,td,a;
  var j = 0;
  for (var i in e.plugins) 
  {
    var info = e.plugins[i];
    tr = document.createElement('tr');
    if (j%2) tr.style.backgroundColor = '#e5e5e5';
    tbody.appendChild(tr);
    td = document.createElement('td');
    td.innerHTML = info.name;
    if (info.version) td.innerHTML += ' v'+info.version;
    tr.appendChild(td);
    
    td = document.createElement('td');
    if (info.developer)
    {
      if (info.developer_url)
      {
            td.innerHTML = '<a target="_blank" href="'+info.developer_url+'">'+info.developer+'</a>';
      }
      else
      {
        td.innerHTML = info.developer
      }
    }
    tr.appendChild(td);
    
    td = document.createElement('td');
    if (info.sponsor)
    {
      if (info.sponsor_url)
      {
            td.innerHTML = '<a target="_blank" href="'+info.sponsor_url+'">'+info.sponsor+'</a>';
      }
      else
      {
        td.innerHTML = info.sponsor
      }
    }
    tr.appendChild(td);
    
    td = document.createElement('td');
    if (info.license)
    {
      td.innerHTML = info.license;
    }
    else
    {
      td.innerHTML = 'htmlArea';
    }
    tr.appendChild(td);
    j++;
  }
}
AboutBox.prototype.fillVersion = function()
{
  var ver = Xinha.version;
  this.dialog.getElementById('version').innerHTML = '<pre>'
                      + '\nRelease:         ' + ver.Release + ' (' + ver.Date + ')'
                      + '\nHead:            ' + ver.Head
                      + '\nRevision:        ' + ver.Revision
                      + '\nLast Changed By: ' + ver.RevisionBy
                      + '\n' +
                      '</pre>';
}
AboutBox.prototype.show = function()
{
  var self = this;
  if (!AboutBox.html)
  {
    if (AboutBox.loading) return;
    AboutBox.loading = true;
    Xinha._getback(Xinha.getPluginDir("AboutBox") + '/dialog.html', function(getback) { AboutBox.html = getback; self.show()});
    return;
  }
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
  //this.dialog.onresize();
};
})()
