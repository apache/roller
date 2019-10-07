/**
 * ExtendedFileManager extended-file-manager.js file.
 * Authors: Wei Zhuo, Afru, Krzysztof Kotowicz, Raimund Meyer
 * Modified by: Krzysztof Kotowicz <koto@webworkers.pl>
 * Version: Updated on 08-01-2005 by Afru
 * Version: Modified on 20-06-2006 by Krzysztof Kotowicz
 * Version: Updated on 29-10-2006 by Raimund Meyer
 * Version: Updated on 20-01-2008 by Raimund Meyer
 * Package: ExtendedFileManager (EFM 1.4)
 */

/**
 * For installation details see Readme.txt in the plugin folder
 *
 */

function ExtendedFileManager(editor)
{

    this.editor = editor;

    var cfg = editor.config;
    var toolbar = cfg.toolbar;
    var self = this;
    
    if (cfg.ExtendedFileManager.use_linker) {
        cfg.registerButton({
            id        : "linkfile",
            tooltip   : Xinha._lc("Insert File Link",'ExtendedFileManager'),
            image     : Xinha.getPluginDir('ExtendedFileManager') + '/img/ed_linkfile.gif',
            textMode  : false,
            action    : function(editor) {
                    editor._linkFile();
                  }
            });
        cfg.addToolbarElement("linkfile", "createlink", 1);
        };

    var manager = editor.config.ExtendedFileManager.backend + '__function=manager';
    if(cfg.ExtendedFileManager.backend_config != null)
    {
      manager += '&backend_config='
        + encodeURIComponent(cfg.ExtendedFileManager.backend_config);
      manager += '&backend_config_hash='
        + encodeURIComponent(cfg.ExtendedFileManager.backend_config_hash);
      manager += '&backend_config_secret_key_location='
        + encodeURIComponent(cfg.ExtendedFileManager.backend_config_secret_key_location);
    }

    if(cfg.ExtendedFileManager.backend_data != null)
    {
        for ( var i in cfg.ExtendedFileManager.backend_data )
        {
            manager += '&' + i + '=' + encodeURIComponent(cfg.ExtendedFileManager.backend_data[i]);
        }
    }
    cfg.ExtendedFileManager.manager = manager;
}

ExtendedFileManager._pluginInfo = {
    name          : "ExtendedFileManager",
    version       : "1.4",
    developer     : "Afru, Krzysztof Kotowicz, Raimund Meyer",
    developer_url : "http://xinha.org",
    license       : "htmlArea"
};

Xinha.Config.prototype.ExtendedFileManager =
{
  'use_linker': true,
  'backend'    : Xinha.getPluginDir('ExtendedFileManager') + '/backend.php?__plugin=ExtendedFileManager&',
  'backend_data' : null,
  // deprecated keys, use passing data through e.g. xinha_pass_to_php_backend()
  'backend_config'     : null,
  'backend_config_hash': null,
  'backend_config_secret_key_location': 'Xinha:ExtendedFileManager'
};

// Over ride the _insertImage function in htmlarea.js.
// Open up the ExtendedFileManger script instead.
if(typeof ImageManager == 'undefined')
{
Xinha.prototype._insertImage = function(image) {

    var editor = this;  // for nested functions
    var outparam = {"editor" : this, param : null};
    
    if (typeof image == "undefined") {
        image = this.getParentElement();
        if (image && !/^img$/i.test(image.tagName))
            image = null;
    }

    if (image) {
        outparam.param = {
            f_url    : Xinha.is_ie ? image.src : image.getAttribute("src"),
            f_alt    : image.alt,
            f_title  : image.title,
            f_border : image.style.borderWidth ? image.style.borderWidth : image.border,
            f_align  : image.align,
            f_width  : image.width,
            f_height  : image.height,
            f_padding: image.style.padding,
            f_margin : image.style.margin,
            f_backgroundColor: image.style.backgroundColor,
            f_borderColor: image.style.borderColor,
            baseHref: editor.config.baseHref
        };

        // compress 'top right bottom left' syntax into one value if possible
        outparam.param.f_border = shortSize(outparam.param.f_border);
        outparam.param.f_padding = shortSize(outparam.param.f_padding);
        outparam.param.f_margin = shortSize(outparam.param.f_margin);

        // convert rgb() calls to rgb hex
        outparam.param.f_backgroundColor = convertToHex(outparam.param.f_backgroundColor);
        outparam.param.f_borderColor = convertToHex(outparam.param.f_borderColor);

    }
    Dialog(this.config.ExtendedFileManager.manager, function(param){
        if (!param)
        {   // user must have pressed Cancel
            return false;
        }

        var img = image;
        if (!img) {
        	if ( !param.f_url ) return false;
            if (Xinha.is_ie) {
                var sel = editor.getSelection();
                var range = editor.createRange(sel);
                editor._doc.execCommand("insertimage", false, param.f_url);
                img = range.parentElement();
                // wonder if this works...
                if (img.tagName.toLowerCase() != "img") {
                    img = img.previousSibling;
                }

            } else {
                img = document.createElement('img');
                img.src = param.f_url;
                editor.insertNodeAtSelection(img);
            }

        } else {
        	if ( !param.f_url ) { // delete the image if empty url passed
        		img.parentNode.removeChild(img);
        		editor.updateToolbar();
        		return false;
        	} else {
                img.src = param.f_url;
        	}
        }

        img.alt = img.alt ? img.alt : '';
        
        for (field in param)
        {
            var value = param[field];
            switch (field)
            {
                case "f_alt"    : img.alt    = value; break;
                case "f_title"  : img.title = value; break;
                case "f_border" : 
                    if (value)
                    {
                        img.style.borderWidth = /[^0-9]/.test(value) ? value : (value != '') ? (parseInt(value) + 'px') : '';
                        if(img.style.borderWidth && !img.style.borderStyle)
                        {
                            img.style.borderStyle = 'solid';
                        }
                        else if (!img.style.borderWidth)
                        {
                        	img.style.border = '';
                        }
                    }
                break;
                case "f_borderColor": img.style.borderColor =  value; break;
                case "f_backgroundColor": img.style.backgroundColor = value; break;
                case "f_align"  : img.align  = value; break;
                case "f_width"  : 
                    if ( parseInt( value ) > 0 )
                    {
                        img.width = parseInt(value);
                    }
                break;
                case "f_height"  : 
                    if ( parseInt( value ) > 0 )
                    {
                        img.height = parseInt(value);
                    }
                break;
                case "f_padding": img.style.padding =
                                          /[^0-9]/.test(value) ? value : (value != '') ? (parseInt(value) + 'px') :''; break;
                case "f_margin": img.style.margin =
                                          /[^0-9]/.test(value) ? value : (value != '') ? (parseInt(value) + 'px') :''; break;
            }
        }

    }, outparam);

};
}

Xinha.prototype._linkFile = function(link) {

    var editor = this;
    var outparam = {"editor" : this, param : null};
    if (typeof link == "undefined") {
        link = this.getParentElement();
        if (link) {
            if (/^img$/i.test(link.tagName))
                link = link.parentNode;
            if (!/^a$/i.test(link.tagName))
                link = null;
        }
    }
    if (!link) {
        var sel = editor.getSelection();
        var range = editor.createRange(sel);
        var compare = 0;
        if (Xinha.is_ie) {
            if ( sel.type == "Control" )
                compare = range.length;
            else
                compare = range.compareEndPoints("StartToEnd", range);
        } else {
            compare = range.compareBoundaryPoints(range.START_TO_END, range);
        }
        if (compare == 0) {
            alert(Xinha._lc("You must select some text before making a new link.", 'ExtendedFileManager'));
            return;
        }
        outparam.param = {
            f_href : '',
            f_title : '',
            f_target : '',
            f_usetarget : editor.config.makeLinkShowsTarget,
            baseHref: editor.config.baseHref
        };
    } 
    else
    {      
        outparam.param = {
            f_href   : Xinha.is_ie ? link.href : link.getAttribute("href"),
            f_title  : link.title,
            f_target : link.target,
            f_usetarget : editor.config.makeLinkShowsTarget,
            baseHref: editor.config.baseHref
        };
    }

    Dialog(this.config.ExtendedFileManager.manager+'&mode=link', function(param){
        if (!param)
            return false;
        var a = link;
        if (!a) try {
            editor._doc.execCommand("createlink", false, param.f_href);
            a = editor.getParentElement();
            var sel = editor.getSelection();
            var range = editor.createRange(sel);
            if (!Xinha.is_ie) {
                a = range.startContainer;
                if (!/^a$/i.test(a.tagName)) {
                    a = a.nextSibling;
                    if (a == null)
                        a = range.startContainer.parentNode;
                }
            }
        } catch(e) {}
        else {
            var href = param.f_href.trim();
            editor.selectNodeContents(a);
            if (href == "") {
                editor._doc.execCommand("unlink", false, null);
                editor.updateToolbar();
                return false;
            }
            else {
                a.href = href;
            }
        }
        if (!(a && /^a$/i.test(a.tagName)))
            return false;
        a.target = param.f_target.trim();
        a.title = param.f_title.trim();
        editor.selectNodeContents(a);
        editor.updateToolbar();
    }, outparam);
};

function shortSize(cssSize)
{
    if(/ /.test(cssSize))
    {
        var sizes = cssSize.split(' ');
        var useFirstSize = true;
        for(var i = 1; i < sizes.length; i++)
        {
            if(sizes[0] != sizes[i])
            {
                useFirstSize = false;
                break;
            }
        }
        if(useFirstSize) cssSize = sizes[0];
    }

    return cssSize;
}

function convertToHex(color) {

    if (typeof color == "string" && /, /.test.color)
        color = color.replace(/, /, ','); // rgb(a, b) => rgb(a,b)

    if (typeof color == "string" && / /.test.color) { // multiple values
        var colors = color.split(' ');
        var colorstring = '';
        for (var i = 0; i < colors.length; i++) {
            colorstring += Xinha._colorToRgb(colors[i]);
            if (i + 1 < colors.length)
                colorstring += " ";
        }
        return colorstring;
    }

    return Xinha._colorToRgb(color);
}
