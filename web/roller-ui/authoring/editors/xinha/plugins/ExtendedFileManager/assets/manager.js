/**
 * Functions for the ExtendedFileManager, used by manager.php only
 * Authors: Wei Zhuo, Afru, Krzysztof Kotowicz
 * Version: Updated on 08-01-2005 by Afru
 * Version: Updated on 20-06-2006 by Krzysztof Kotowicz
 * Package: ExtendedFileManager (EFM 1.1.1)
 * http://www.afrusoft.com/htmlarea
 */

function comboSelectValue(c, val) {
    var ops = c.getElementsByTagName("option");
    for (var i = ops.length; --i >= 0;) {
        var op = ops[i];
        op.selected = (op.value == val);
    }
    c.value = val;
}

//Translation
function i18n(str) {
    return HTMLArea._lc(str, 'ExtendedFileManager');
}

//set the alignment options
function setAlign(align)
{
    var selection = document.getElementById('f_align');
    for(var i = 0; i < selection.length; i++)
    {
        if(selection.options[i].value == align)
        {
            selection.selectedIndex = i;
            break;
        }
    }
}

function onTargetChanged() {
  var f = document.getElementById("f_other_target");
  if (this.value == "_other") {
    f.style.visibility = "visible";
    f.select();
    f.focus();
  } else f.style.visibility = "hidden";
}

//initialise the form
init = function ()
{
    if (manager_mode == "link")
      __dlg_init(null,  {width:650,height:500});
    else
      __dlg_init(null,  {width:650,height:560});

    __dlg_translate('ExtendedFileManager');

    var uploadForm = document.getElementById('uploadForm');
    if(uploadForm) uploadForm.target = 'imgManager';

    if (manager_mode == 'image' && typeof colorPicker != "undefined" && document.getElementById('bgCol_pick')) {
        // Hookup color pickers
        var bgCol_pick = document.getElementById('bgCol_pick');
        var f_backgroundColor = document.getElementById('f_backgroundColor');
        var bgColPicker = new colorPicker({cellsize:'5px',callback:function(color){f_backgroundColor.value=color;}});
        bgCol_pick.onclick = function() { bgColPicker.open('top,right', f_backgroundColor ); }

        var bdCol_pick = document.getElementById('bdCol_pick');
        var f_borderColor = document.getElementById('f_borderColor');
        var bdColPicker = new colorPicker({cellsize:'5px',callback:function(color){f_borderColor.value=color;}});
        bdCol_pick.onclick = function() { bdColPicker.open('top,right', f_borderColor ); }
    }

    var param = window.dialogArguments;

    if(manager_mode=="image" && param)
    {
        var absoluteURL = new RegExp('^https?://');

        if (param.f_url.length > 0 && !absoluteURL.test(param.f_url) && typeof param.baseHref == "string") {
            // URL is not absolute, prepend baseHref
            param.f_url = param.baseHref + param.f_url;
        }

        // strip base_url from url
        var image_regex = new RegExp( '(https?://[^/]*)?' + base_url.replace(/\/$/, '') );
        param.f_url = param.f_url.replace( image_regex, "" );

        // The image URL may reference one of the automatically resized images
        // (when the user alters the dimensions in the picker), clean that up
        // so it looks right and we get back to a normal f_url
        var rd = _resized_dir.replace(HTMLArea.RE_Specials, '\\$1');
        var rp = _resized_prefix.replace(HTMLArea.RE_Specials, '\\$1');
        var dreg = new RegExp('^(.*/)' + rd + '/' + rp + '_([0-9]+)x([0-9]+)_([^/]+)$');

        if(dreg.test(param.f_url))
        {
          param.f_url    = RegExp.$1 + RegExp.$4;
          param.f_width  = RegExp.$2;
          param.f_height = RegExp.$3;
        }

        document.getElementById("f_url").value = param["f_url"];
        document.getElementById("f_alt").value = param["f_alt"];
        document.getElementById("f_title").value = param["f_title"];
        document.getElementById("f_border").value = param["f_border"];
        document.getElementById("f_width").value = param["f_width"];
        document.getElementById("f_height").value = param["f_height"];
        document.getElementById("f_margin").value = param["f_margin"];
        document.getElementById("f_padding").value = param["f_padding"];
        document.getElementById("f_borderColor").value = param["f_borderColor"];
        document.getElementById("f_backgroundColor").value = param["f_backgroundColor"];

        setAlign(param["f_align"]);

        document.getElementById("f_url").focus();

        document.getElementById("orginal_width").value = param["f_width"];
        document.getElementById("orginal_height").value = param["f_height"];

        // Locate to the correct directory
        var dreg = new RegExp('^(.*/)([^/]+)$');
        if (dreg.test(param['f_url']))
        {
          changeDir(RegExp.$1);
          var dirPath = document.getElementById('dirPath');
          for(var i = 0; i < dirPath.options.length; i++)
          {
            if(dirPath.options[i].value == encodeURIComponent(RegExp.$1))
            {
              dirPath.options[i].selected = true;
              break;
            }
          }
        }

        document.getElementById('f_preview').src = _backend_url + '__function=thumbs&img=' + param.f_url;

    }

    else if(manager_mode=="link" && param)
    {
        var target_select = document.getElementById("f_target");
        var use_target = true;

        var absoluteURL = new RegExp('^https?://');

        if (param.f_href.length > 0 && !absoluteURL.test(param.f_href) && typeof param.baseHref == "string") {
            // URL is not absolute, prepend baseHref
            param.f_href = param.baseHref + param.f_href;
        }

        // strip base_url from href
        var href_regex = new RegExp( '(https?://[^/]*)?' + base_url.replace(/\/$/, '') );
        param.f_href = param.f_href.replace( href_regex, "" );

        // Locate to the correct directory
        var dreg = new RegExp('^(.*/)([^/]+)$');
        if (dreg.test(param['f_href']))
        {
          changeDir(RegExp.$1);
          var dirPath = document.getElementById('dirPath');
          for(var i = 0; i < dirPath.options.length; i++)
          {
            if(dirPath.options[i].value == encodeURIComponent(RegExp.$1))
            {
              dirPath.options[i].selected = true;
              break;
            }
          }
        }

        if (param)
        {
            if ( typeof param["f_usetarget"] != "undefined" )
            {
                use_target = param["f_usetarget"];
            }
            if ( typeof param["f_href"] != "undefined" )
            {
                document.getElementById("f_href").value = param["f_href"];
                document.getElementById("f_title").value = param["f_title"];
                comboSelectValue(target_select, param["f_target"]);
                if (target_select.value != param.f_target)
                {
                    var opt = document.createElement("option");
                    opt.value = param.f_target;
                    opt.innerHTML = opt.value;
                    target_select.appendChild(opt);
                    opt.selected = true;
                }
            }
        }
        if (! use_target)
        {
            document.getElementById("f_target_label").style.visibility = "hidden";
            document.getElementById("f_target").style.visibility = "hidden";
            document.getElementById("f_target_other").style.visibility = "hidden";
        }

        var opt = document.createElement("option");
        opt.value = "_other";
        opt.innerHTML = i18n("Other");
        target_select.appendChild(opt);
        target_select.onchange = onTargetChanged;
        document.getElementById("f_href").focus();
    }
}

function onCancel()
{
    __dlg_close(null);
    return false;
}

function onOK()
{
    if(manager_mode=="image")
    {
        // pass data back to the calling window
        var fields = ["f_url", "f_alt", "f_title", "f_align", "f_border", "f_margin", "f_padding", "f_height", "f_width", "f_borderColor", "f_backgroundColor"];
        var param = new Object();
        for (var i in fields)
        {
            var id = fields[i];
            var el = document.getElementById(id);
            if(id == "f_url" && el.value.indexOf('://') < 0 )
                param[id] = makeURL(base_url,el.value);
            else
                param[id] = el.value;
        }

        // See if we need to resize the image
        var origsize =
        {
          w:document.getElementById('orginal_width').value,
          h:document.getElementById('orginal_height').value
        }

        if(  (origsize.w != param.f_width)
          || (origsize.h != param.f_height) )
        {
          // Yup, need to resize
          var resized = HTMLArea._geturlcontent(window.opener._editor_url + 'plugins/ExtendedFileManager/' + _backend_url + '&__function=resizer&img=' + encodeURIComponent(document.getElementById('f_url').value) + '&width=' + param.f_width + '&height=' + param.f_height);

          // alert(resized);
          resized = eval(resized);
          if(resized)
          {
            param.f_url = makeURL(base_url, resized);
          }
        }

        __dlg_close(param);
        return false;
    }
    else if(manager_mode=="link")
    {
        var required = {
            // f_href shouldn't be required or otherwise removing the link by entering an empty

            // url isn't possible anymore.

            // "f_href": i18n("You must enter the URL where this link points to")

        };
        for (var i in required) {
        var el = document.getElementById(i);
            if (!el.value) {
              alert(required[i]);
              el.focus();
              return false;
            }
        }

        // pass data back to the calling window
        var fields = ["f_href", "f_title", "f_target" ];
        var param = new Object();
        for (var i in fields) {
            var id = fields[i];
            var el = document.getElementById(id);

            if(id == "f_href" && el.value.indexOf('://') < 0 )
                param[id] = makeURL(base_url,el.value);
            else
                param[id] = el.value;

        }
        if (param.f_target == "_other")
            param.f_target = document.getElementById("f_other_target").value;

//          alert(param.f_target);
          __dlg_close(param);
        return false;
    }
}

//similar to the Files::makeFile() in Files.php
function makeURL(pathA, pathB)
{
    if(pathA.substring(pathA.length-1) != '/')
        pathA += '/';

    if(pathB.charAt(0) == '/');
        pathB = pathB.substring(1);

    return pathA+pathB;
}

function updateDir(selection)
{
    var newDir = selection.options[selection.selectedIndex].value;
    changeDir(newDir);
}

function goUpDir()
{
    var selection = document.getElementById('dirPath');
    var currentDir = selection.options[selection.selectedIndex].text;
    if(currentDir.length < 2)
        return false;
    var dirs = currentDir.split('/');

    var search = '';

    for(var i = 0; i < dirs.length - 2; i++)
    {
        search += dirs[i]+'/';
    }

    for(var i = 0; i < selection.length; i++)
    {
        var thisDir = selection.options[i].text;
        if(thisDir == search)
        {
            selection.selectedIndex = i;
            var newDir = selection.options[i].value;
            changeDir(newDir);
            break;
        }
    }
}

function changeDir(newDir)
{
    if(typeof imgManager != 'undefined')
        imgManager.changeDir(newDir);
}

function updateView()
{
    refresh();
}

function toggleConstrains(constrains)
{
    var lockImage = document.getElementById('imgLock');
    var constrains = document.getElementById('constrain_prop');

    if(constrains.checked)
    {
        lockImage.src = "img/locked.gif";
        checkConstrains('width')
    }
    else
    {
        lockImage.src = "img/unlocked.gif";
    }
}

function checkConstrains(changed)
{
    //alert(document.form1.constrain_prop);
    var constrains = document.getElementById('constrain_prop');

    if(constrains.checked)
    {
        var obj = document.getElementById('orginal_width');
        var orginal_width = parseInt(obj.value);
        var obj = document.getElementById('orginal_height');
        var orginal_height = parseInt(obj.value);

        var widthObj = document.getElementById('f_width');
        var heightObj = document.getElementById('f_height');

        var width = parseInt(widthObj.value);
        var height = parseInt(heightObj.value);

        if(orginal_width > 0 && orginal_height > 0)
        {
            if(changed == 'width' && width > 0) {
                heightObj.value = parseInt((width/orginal_width)*orginal_height);
            }

            if(changed == 'height' && height > 0) {
                widthObj.value = parseInt((height/orginal_height)*orginal_width);
            }
        }
    }
}

function showMessage(newMessage)
{
    var message = document.getElementById('message');
    var messages = document.getElementById('messages');
    if(message.firstChild)
        message.removeChild(message.firstChild);

    message.appendChild(document.createTextNode(i18n(newMessage)));

    messages.style.display = "block";
}

function addEvent(obj, evType, fn)
{
    if (obj.addEventListener) { obj.addEventListener(evType, fn, true); return true; }
    else if (obj.attachEvent) {  var r = obj.attachEvent("on"+evType, fn);  return r;  }
    else {  return false; }
}

function doUpload()
{
    var uploadForm = document.getElementById('uploadForm');
    if(uploadForm)
        showMessage('Uploading');
}

function refresh()
{
    var selection = document.getElementById('dirPath');
    updateDir(selection);
}

function newFolder()
{
    var folder = prompt(i18n('Please enter name for new folder...'), i18n('Untitled'));
    var selection = document.getElementById('dirPath');
    var dir = selection.options[selection.selectedIndex].value;

    if(folder == thumbdir)
    {
        alert(i18n('Invalid folder name, please choose another folder name.'));
        return false;
    }

    if (folder && folder != '' && typeof imgManager != 'undefined')
        imgManager.newFolder(dir, encodeURI(folder));
}

addEvent(window, 'load', init);