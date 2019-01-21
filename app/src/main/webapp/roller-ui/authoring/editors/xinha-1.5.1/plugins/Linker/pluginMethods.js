Linker.prototype._createLink = function(a)
{
  if (!this._dialog)
  {
    this._dialog = new Linker.Dialog(this);
  }
  
  if(!a && this.editor.selectionEmpty(this.editor.getSelection()))
  {       
    alert(this._lc("You must select some text before making a new link."));
    return false;
  }

  var inputs =
  {
    type:     'url',
    href:     'http://www.example.com/',
    title:    this._lc('Shows On Hover'),
    target:   '',
    p_width:  '',
    p_height: '',
    p_options: ['menubar=no','toolbar=yes','location=no','status=no','scrollbars=yes','resizeable=yes'],
    to:       'alice@example.com',
    subject:  '',
    body:     '',
    anchor:   ''
  };
  
  if(a && a.tagName.toLowerCase() == 'a')
  {
    var href =this.editor.fixRelativeLinks(a.getAttribute('href'));
    var m = href.match(/^mailto:(.*@[^?&]*)(\?(.*))?$/);
    var anchor = href.match(/^#(.*)$/);
    var title = a.getAttribute('title');
    
    if(m)
    {
      // Mailto
      inputs.type = 'mailto';
      inputs.to = m[1];
      if(m[3])
      {
        var args  = m[3].split('&');
        for(var x = 0; x<args.length; x++)
        {
          var j = args[x].match(/(subject|body)=(.*)/);
          if(j)
          {
            inputs[j[1]] = decodeURIComponent(j[2]);
          }
        }
      }
    }
    else if (anchor)
    {
      //Anchor-Link
      inputs.type = 'anchor';
      inputs.anchor = anchor[1];
      
    }
    else
    {
      if(a.getAttribute('onclick') && String(a.getAttribute('onclick')).length)
      {
        var m = a.getAttribute('onclick').match(/window\.open\(\s*this\.href\s*,\s*'([a-z0-9_]*)'\s*,\s*'([a-z0-9_=,]*)'\s*\)/i);

        // Popup Window
        inputs.href   = href ? href : '';
        inputs.title = title;
        inputs.target = 'popup';
        inputs.p_name = m[1];
        inputs.p_options = [ ];


        var args = m[2].split(',');
        for(var x = 0; x < args.length; x++)
        {
          var i = args[x].match(/(width|height)=([0-9]+)/);
          if(i)
          {
            inputs['p_' + i[1]] = parseInt(i[2]);
          }
          else
          {
            inputs.p_options.push(args[x]);
          }
        }
      }
      else
      {
        // Normal
        inputs.href   = href;
        inputs.target = a.target;
        inputs.title = title;
      }
    }
  }

  var linker = this;

  // If we are not editing a link, then we need to insert links now using execCommand
  // because for some reason IE is losing the selection between now and when doOK is
  // complete.  I guess because we are defocusing the iframe when we click stuff in the
  // linker dialog.

  this.a = a; // Why doesn't a get into the closure below, but if I set it as a property then it's fine?

  var doOK = function()
  {
    //if(linker.a) alert(linker.a.tagName);
    var a = linker.a;

    var values = linker._dialog.hide();
    var atr =
    {
      href: '',
      target:'',
      title:'',
      onclick:''
    };
    
    if(values.title == linker._lc('Shows On Hover')) 
    {
      values.title = '';
    }
    
    if(values.type == 'url')
    {
     if(values.href)
     {
       atr.href = values.href.trim();
       atr.target = values.target;
       atr.title = values.title;
       if(values.target == 'popup')
       {

         if(values.p_width)
         {
           values.p_options.push('width=' + values.p_width);
         }
         if(values.p_height)
         {
           values.p_options.push('height=' + values.p_height);
         }
         atr.onclick = 'if(window.parent && window.parent.Xinha){return false}window.open(this.href, \'' + (values.p_name.replace(/[^a-z0-9_]/i, '_')) + '\', \'' + values.p_options.join(',') + '\');return false;';
       }
     }
    }
    else if(values.type == 'anchor')
    {
      if(values.anchor)
      {
        atr.href = values.anchor.value;
      }
    }
    else
    {
      if(values.to)
      {
        atr.href = 'mailto:' + values.to;
        if(values.subject) atr.href += '?subject=' + encodeURIComponent(values.subject);
        if(values.body)    atr.href += (values.subject ? '&' : '?') + 'body=' + encodeURIComponent(values.body);
      }
    }

    if (atr.href) atr.href = atr.href.trim();

    if(a && a.tagName.toLowerCase() == 'a')
    {
      if(!atr.href)
      {
        if(confirm(linker._dialog._lc('Are you sure you wish to remove this link?')))
        {
          var p = a.parentNode;
          while(a.hasChildNodes())
          {
            p.insertBefore(a.removeChild(a.childNodes[0]), a);
          }
          p.removeChild(a);
          linker.editor.updateToolbar();
          return;
        }
      }
      else
      {
        // Update the link
        for(var i in atr)
        {
          if(String(atr[i]).length > 0)
          {
            a.setAttribute(i, atr[i]);
          }
          else
          {
            a.removeAttribute(i);
          }
        }
        
        // If we change a mailto link in IE for some hitherto unknown
        // reason it sets the innerHTML of the link to be the 
        // href of the link.  Stupid IE.
        if(Xinha.is_ie)
        {
          if(/mailto:([^?<>]*)(\?[^<]*)?$/i.test(a.innerHTML))
          {
            a.innerHTML = RegExp.$1;
          }
        }
      }
    }
    else
    {
      if(!atr.href) return true;

      // Insert a link, we let the browser do this, we figure it knows best
      var tmp = Xinha.uniq('http://www.example.com/Link');
      linker.editor._doc.execCommand('createlink', false, tmp);

      // Fix them up
      var anchors = linker.editor._doc.getElementsByTagName('a');
      for(var i = 0; i < anchors.length; i++)
      {
        var anchor = anchors[i];
        if(anchor.href == tmp)
        {
          // Found one.
          if (!a) a = anchor;
          for(var j in atr)
          {
            if(String(atr[j]).length > 0)
              anchor.setAttribute(j, atr[j]);
          }
        }
      }
    }
    linker.editor.selectNodeContents(a);
    linker.editor.updateToolbar();
  };

  this._dialog.show(inputs, doOK);

};

Linker.prototype._getSelectedAnchor = function()
{
  var sel  = this.editor.getSelection();
  var rng  = this.editor.createRange(sel);
  var a    = this.editor.activeElement(sel);
  if(a != null && a.tagName.toLowerCase() == 'a')
  {
    return a;
  }
  else
  {
    a = this.editor._getFirstAncestor(sel, 'a');
    if(a != null)
    {
      return a;
    }
  }
  return null;
};

Linker.Dialog_dTrees = [ ];

Linker.Dialog = function (linker)
{
  var  lDialog = this;
  this.Dialog_nxtid = 0;
  this.linker = linker;
  this.id = { }; // This will be filled below with a replace, nifty

  this.ready = false;

  this.dialog = false;

  // load the dTree script
  this._prepareDialog();

};

Linker.Dialog.prototype._prepareDialog = function()
{
  var lDialog = this;
  var linker = this.linker;

  var files = this.linker.files;

  // Now we have everything we need, so we can build the dialog.
  if(!linker.lConfig.dialog && Xinha.Dialog) linker.lConfig.dialog = Xinha.Dialog;
  
  var dialog = this.dialog = new linker.lConfig.dialog(linker.editor, Linker.html, 'Linker',{width:600,height:400});
  var dTreeName = Xinha.uniq('dTree_');

  this.dTree = new dTree(dTreeName, Xinha.getPluginDir("Linker") + '/dTree/');
  eval(dTreeName + ' = this.dTree');

  this.dTree.add(this.Dialog_nxtid++, -1, linker.lConfig.treeCaption , null, linker.lConfig.treeCaption);
  this.makeNodes(files, 0);

  // Put it in
  var ddTree = this.dialog.getElementById('dTree');
  //ddTree.innerHTML = this.dTree.toString();
  ddTree.innerHTML = '';
//  ddTree.style.position = 'absolute';
//  ddTree.style.left = 1 + 'px';
 // ddTree.style.top =  0 + 'px';
  ddTree.style.overflow = 'auto';
  ddTree.style.height = '300px';
  if ( Xinha.is_ie )
  {
    ddTree.style.styleFloat = "left";
  }
  else
  {
    ddTree.style.cssFloat = "left";
  }
  ddTree.style.backgroundColor = 'white';
  this.ddTree = ddTree;
  
  this.dTree._linker_premade = this.dTree.toString();

  var options = this.dialog.getElementById('options');
  //options.style.position = 'absolute';
  //options.style.top      = 0   + 'px';
  //options.style.right    = 0   + 'px';
  options.style.width    = 320 + 'px';
  options.style.overflow = 'auto';

  // Hookup the resizer
  this.dialog.rootElem.style.paddingBottom ="0";
  this.dialog.onresize = function()
    {
      var h = parseInt(dialog.height) - dialog.getElementById('h1').offsetHeight;
      var w = parseInt(dialog.width)  - 330 ;

      // An error is thrown with IE when trying to set a negative width or a negative height
      // But perhaps a width / height of 0 is not the minimum required we need to set
      if (w<0) w = 0;
      if (h<0) h = 0;
      //options.style.height =
      lDialog.ddTree.style.height = h + 'px';
      lDialog.ddTree.style.width  = w + 'px';
    }

  // Set the onclick handlers for the link type radio buttons
  var self = this;
  this.dialog.getElementById('type_url').onclick = function() {
    self.showOptionsForType('url');
  };
  this.dialog.getElementById('type_mailto').onclick = function() {
    self.showOptionsForType('mailto');
  };
  this.dialog.getElementById('type_anchor').onclick = function() {
    self.showOptionsForType('anchor');
  };

  var hidePopupOptions = function() {
    self.showOptionsForTarget('none')
  };
  this.dialog.getElementById('noTargetRadio').onclick = hidePopupOptions;
  this.dialog.getElementById('sameWindowRadio').onclick = hidePopupOptions;
  this.dialog.getElementById('newWindowRadio').onclick = hidePopupOptions;
  this.dialog.getElementById('popupWindowRadio').onclick = function() {
    self.showOptionsForTarget('popup');
  };

  this.ready = true;
  ddTree = null;
  Xinha.freeLater(lDialog, 'ddTree');
  options = null;
};

Linker.Dialog.prototype.makeNodes = function(files, parent)
{ 
  for(var i = 0; i < files.length; i++)
  {
    if(typeof files[i] == 'string')
    {
      this.dTree.add(this.Dialog_nxtid++, parent,
                     files[i].replace(/^.*\//, ''),
                     'javascript:document.getElementsByName(\'' + this.dialog.id.href + '\')[0].value=decodeURIComponent(\'' + encodeURIComponent(files[i]) + '\');document.getElementsByName(\'' + this.dialog.id.type + '\')[0].click();document.getElementsByName(\'' + this.dialog.id.href + '\')[0].focus();void(0);',
                     files[i]);
    }
    else if(typeof files[i]=="object" && files[i] && typeof files[i].length==="number") // there seems to be a strange bug in IE that requires this complicated check, see #1197
    {
      var id = this.Dialog_nxtid++;
      this.dTree.add(id, parent, files[i][0].replace(/^.*\//, ''), null, files[i][0]);
      this.makeNodes(files[i][1], id);
    }
    else if(typeof files[i] == 'object')
    {
      var id = this.Dialog_nxtid++;     
      if(files[i].title) var title = files[i].title;
      else if(files[i].url) var title = files[i].url.replace(/^.*\//, '');
      else var title = "no title defined";
      if(files[i].url) var link = 'javascript:document.getElementsByName(\'' + this.dialog.id.href + '\')[0].value=decodeURIComponent(\'' + encodeURIComponent(files[i].url) + '\');document.getElementsByName(\'' + this.dialog.id.type + '\')[0].click();document.getElementsByName(\'' + this.dialog.id.href + '\')[0].focus();void(0);';
      else var link = '';
      
      this.dTree.add(id, parent, title, link, title);
      if(files[i].children) {
        this.makeNodes(files[i].children, id);
      }
    }
  }
};

Linker.Dialog.prototype._lc = Linker.prototype._lc;

Linker.Dialog.prototype.show = function(inputs, ok, cancel)
{
  if(!this.ready)
  {
    var lDialog = this;
    window.setTimeout(function() {lDialog.show(inputs,ok,cancel);},100);
    return;
  }

  if(this.ddTree.innerHTML == '')
  {
    this.ddTree.innerHTML = this.dTree._linker_premade;
  }
  
  if(!this.linker.lConfig.canSetTarget)
  {
    this.dialog.getElementById('target_options').style.display = 'none';    
  }
  
  this.showOptionsForType(inputs.type);
  this.showOptionsForTarget(inputs.target);
  
  var anchor = this.dialog.getElementById('anchor');
  for(var i=anchor.length;i>=0;i--) {
    anchor[i] = null;
  }

  var html = this.linker.editor.getHTML();  
  var anchors = new Array();

  var m = html.match(/<a[^>]+name="([^"]+)"/gi);
  if(m)
  {
    for(i=0;i<m.length;i++)
    {
        var n = m[i].match(/name="([^"]+)"/i);
        if(!anchors.contains(n[1])) anchors.push(n[1]);
    }
  }
  m = html.match(/id="([^"]+)"/gi);
  if(m)
  {
    for(i=0;i<m.length;i++)
    {
        n = m[i].match(/id="([^"]+)"/i);
        if(!anchors.contains(n[1])) anchors.push(n[1]);
    }
  }
  
  for(i=0;i<anchors.length;i++)
  {
    var opt = new Option(anchors[i],'#'+anchors[i],false,(inputs.anchor == anchors[i]));
    anchor[anchor.length] = opt;
  }

  // Configuration for disabling the mail link functionality.
  if(this.linker.lConfig.disableMailto)
  {
    this.dialog.getElementById('mailtofieldset').style.display = "none";
  }

  // Configuration for hiding the anchor functionality.  Also, no need to show
  // the UI if there are no anchors present in the document.
  if(anchor.length==0 || this.linker.lConfig.disableAnchors)
  {
    this.dialog.getElementById('anchorfieldset').style.display = "none";

    // If we disable the other two fieldsets, we'll hide the (now) unnecessary
    // radio button.
    if (this.linker.lConfig.disableMailto)
    {
      this.dialog.getElementById('type').style.display = "none";
    }
  }

  // Disable link targets (all targets available by default)
  var disabledTargets = this.linker.lConfig.disableTargetTypes; 
  if (typeof disabledTargets == 'undefined')
  {
    disabledTargets = [];
  } 
  else if (typeof disabledTargets == 'string')
  {
    disabledTargets = [disabledTargets];
  }
  for (var i=0; i<disabledTargets.length; i++)
  {
    this.dialog.getElementById(disabledTargets[i]).style.display = "none";
  }
  if (disabledTargets.length == 3) // only one target option is visible
  {
    if (disabledTargets.contains('popupWindow'))
    {
      // There's no need to show anything, so hide the entire div
      this.dialog.getElementById('target_options').style.display = "none";
    }
    else
    {
      // Only popups are allowed, hide the radio button
      this.dialog.getElementById('popupWindowRadio').style.display = "none";
      this.showOptionsForTarget('popup');
    }
  }

  var enabledTargets = new Array();
  if (!disabledTargets.contains('noTarget'))
  {
    enabledTargets.push('noTargetRadio');
  }
  if (!disabledTargets.contains('sameWindow'))
  {
    enabledTargets.push('sameWindowRadio');
  }
  if (!disabledTargets.contains('newWindow'))
  {
    enabledTargets.push('newWindowRadio');
  }
  if (!disabledTargets.contains('popupWindow'))
  {
    enabledTargets.push('popupWindowRadio');
  }

  // if we're not editing an existing link, hide the remove link button
  if (inputs.href == 'http://www.example.com/' && inputs.to == 'alice@example.com') { 
    this.dialog.getElementById('clear').style.display = "none";
  }
  else { // 
    var clearBtn = this.dialog.getElementById('clear');
    clearBtn.style.display = "";
    if (ok)
    {
      clearBtn.onclick = function() { lDialog.removeLink(ok); };
    }
  }
  
  // It could be forced not be able to be removed, as is the case with link-picker.js
  if(!this.linker.lConfig.canRemoveLink)
  {
    this.dialog.getElementById('clear').style.display = 'none';
  }
  
  // Connect the OK and Cancel buttons
  var dialog = this.dialog;
  var lDialog = this;
  if(ok)
  {
    this.dialog.getElementById('ok').onclick = ok;
  }
  else
  {
    this.dialog.getElementById('ok').onclick = function() {lDialog.hide();};
  }

  if(cancel)
  {
    this.dialog.getElementById('cancel').onclick = cancel;
  }
  else
  {
    this.dialog.getElementById('cancel').onclick = function() { lDialog.hide()};
  }

  // Show the dialog
  this.linker.editor.disableToolbar(['fullscreen','linker']);

  this.dialog.show(inputs);

  // If we set the default radio button *before* we call dialog.show()
  // it doesn' work...
  var targetSelected = false;
  for (var i=0; i<enabledTargets.length; i++)
  {
    if (this.dialog.getElementById(enabledTargets[i]).checked == true)
    {
      targetSelected = true;
      break;
    }
  }
  // If no target is selected, select the first one by default
  if (!targetSelected && enabledTargets.length > 0)
  {
    this.dialog.getElementById(enabledTargets[0]).checked = true;
  }

  // Init the sizes
  this.dialog.onresize();
};

Linker.Dialog.prototype.hide = function()
{
  this.linker.editor.enableToolbar();
  return this.dialog.hide();
};

Linker.Dialog.prototype.removeLink = function(applyFunc)
{
  this.dialog.getElementById('href').value = "";
  this.dialog.getElementById('to').value = "";

  return applyFunc();
};

Linker.Dialog.prototype.showOptionsForType = function(type)
{
  var urlOptions = this.dialog.getElementById('urltable');
  var mailtoOptions = this.dialog.getElementById('mailtable');
  var anchorOptions = this.dialog.getElementById('anchortable');

  if (type == 'anchor')
  {
    anchorOptions.style.display = '';
    urlOptions.style.display = 'none';
    mailtoOptions.style.display = 'none';
  }
  else if (type == 'mailto')
  {
    mailtoOptions.style.display = '';
    urlOptions.style.display = 'none';
    anchorOptions.style.display = 'none';
  }
  else 
  {
    urlOptions.style.display = '';
    mailtoOptions.style.display = 'none';
    anchorOptions.style.display = 'none';
  }
};

Linker.Dialog.prototype.showOptionsForTarget = function(target)
{
  var popupOptions = this.dialog.getElementById('popuptable');
  popupOptions.style.display = target == 'popup' ? '' : 'none';
};
