
  /*--------------------------------------:noTabs=true:tabSize=2:indentSize=2:--
    --  Xinha (is not htmlArea) - http://xinha.gogo.co.nz/
    --
    --  Use of Xinha is granted by the terms of the htmlArea License (based on
    --  BSD license)  please read license.txt in this package for details.
    --
    --  Xinha was originally based on work by Mihai Bazon which is:
    --      Copyright (c) 2003-2004 dynarch.com.
    --      Copyright (c) 2002-2003 interactivetools.com, inc.
    --      This copyright notice MUST stay intact for use.
    --
    --  This is the standard implementation of the method for rendering HTML code from the DOM
    --
    --  The file is loaded by the Xinha Core when no alternative method (plugin) is loaded.
    --
    --
    --  $HeadURL: http://svn.xinha.org/trunk/modules/GetHtml/DOMwalk.js $
    --  $LastChangedDate: 2018-02-04 17:28:16 +1300 (Sun, 04 Feb 2018) $
    --  $LastChangedRevision: 1356 $
    --  $LastChangedBy: gogo $
    --------------------------------------------------------------------------*/
function GetHtmlImplementation(editor) {
    this.editor = editor;
}

GetHtmlImplementation._pluginInfo = {
  name          : "GetHtmlImplementation DOMwalk",
  origin        : "Xinha Core",
  version       : "$LastChangedRevision: 1356 $".replace(/^[^:]*:\s*(.*)\s*\$$/, '$1'),
  developer     : "The Xinha Core Developer Team",
  developer_url : "$HeadURL: http://svn.xinha.org/trunk/modules/GetHtml/DOMwalk.js $".replace(/^[^:]*:\s*(.*)\s*\$$/, '$1'),
  sponsor       : "",
  sponsor_url   : "",
  license       : "htmlArea"
};

// Retrieves the HTML code from the given node.	 This is a replacement for
// getting innerHTML, using standard DOM calls.
// Wrapper legacy see #442
Xinha.getHTML = function(root, outputRoot, editor)
{
  return Xinha.getHTMLWrapper(root,outputRoot,editor);
};

Xinha.emptyAttributes = " checked disabled ismap readonly nowrap compact declare selected defer multiple noresize noshade "

Xinha.getHTMLWrapper = function(root, outputRoot, editor, indent)
{
  var html = "";
  if ( !indent )
  {
    indent = '';
  }

  switch ( root.nodeType )
  {
    case 10:// Node.DOCUMENT_TYPE_NODE
    case 6: // Node.ENTITY_NODE
    case 12:// Node.NOTATION_NODE
      // this all are for the document type, probably not necessary
    break;

    case 2: // Node.ATTRIBUTE_NODE
      // Never get here, this has to be handled in the ELEMENT case because
      // of IE crapness requring that some attributes are grabbed directly from
      // the attribute (nodeValue doesn't return correct values), see
      //http://groups.google.com/groups?hl=en&lr=&ie=UTF-8&oe=UTF-8&safe=off&selm=3porgu4mc4ofcoa1uqkf7u8kvv064kjjb4%404ax.com
      // for information
    break;

    case 4: // Node.CDATA_SECTION_NODE
      // Mozilla seems to convert CDATA into a comment when going into wysiwyg mode,
      //  don't know about IE
      html += (Xinha.is_ie ? ('\n' + indent) : '') + '<![CDATA[' + root.data + ']]>' ;
    break;

    case 5: // Node.ENTITY_REFERENCE_NODE
      html += '&' + root.nodeValue + ';';
    break;

    case 7: // Node.PROCESSING_INSTRUCTION_NODE
      // PI's don't seem to survive going into the wysiwyg mode, (at least in moz)
      // so this is purely academic
      html += (Xinha.is_ie ? ('\n' + indent) : '') + '<'+'?' + root.target + ' ' + root.data + ' ?>';
    break;

    case 1: // Node.ELEMENT_NODE
    case 11: // Node.DOCUMENT_FRAGMENT_NODE
    case 9: // Node.DOCUMENT_NODE
      var closed;
      var i;
      var root_tag = (root.nodeType == 1) ? root.tagName.toLowerCase() : '';
      if ( ( root_tag == "script" || root_tag == "noscript" ) && editor.config.stripScripts )
      {
        break;
      }
      if ( outputRoot )
      {
        outputRoot = !(editor.config.htmlRemoveTags && editor.config.htmlRemoveTags.test(root_tag));
      }
      if ( Xinha.is_ie && root_tag == "head" )
      {
        if ( outputRoot )
        {
          html += (Xinha.is_ie ? ('\n' + indent) : '') + "<head>";
        }
        
        var save_multiline = RegExp.multiline;
        RegExp.multiline = true;
        var txt = 
        root.innerHTML
        .replace(Xinha.RE_tagName, function(str, p1, p2) { return p1 + p2.toLowerCase(); }) // lowercasize
        .replace(/\s*=\s*(([^'"][^>\s]*)([>\s])|"([^"]+)"|'([^']+)')/g, '="$2$4$5"$3') //add attribute quotes
        .replace(/<(link|meta)((\s*\S*="[^"]*")*)>([\n\r]*)/g, '<$1$2 />\n'); //terminate singlet tags
        RegExp.multiline = save_multiline;
        html += txt + '\n';
        if ( outputRoot )
        {
          html += (Xinha.is_ie ? ('\n' + indent) : '') + "</head>";
        }
        break;
      }
      else if ( outputRoot )
      {
        closed = (!(root.hasChildNodes() || Xinha.needsClosingTag(root)));
        html += ((Xinha.isBlockElement(root)) ? ('\n' + indent) : '') + "<" + root.tagName.toLowerCase();
        var attrs = root.attributes;
        
        for ( i = attrs.length-1; i >= 0; --i )
        {
          var a = attrs.item(i);
          // In certain browsers (*cough* firefox) the dom node loses
          // information if the image is currently broken.  In order to prevent
          // corrupting the height and width of image tags, we strip height and
          // width from the image rather than reporting bad information.
          if (Xinha.is_real_gecko && (root.tagName.toLowerCase() == 'img') &&
              ((a.nodeName.toLowerCase() == 'height') || (a.nodeName.toLowerCase() == 'width')))
          {
            if (!root.complete || root.naturalWidth === 0)
            {
              // This means that firefox has been unable to read the dimensions from the actual image
              continue;
            }
          }

          if(root.tagName.toLowerCase() == 'img' && a.nodeName.toLowerCase() == 'complete') continue;

          if (typeof a.nodeValue == 'object' ) continue; // see #684
          if (root.tagName.toLowerCase() == "input" 
              && root.type.toLowerCase() == "checkbox" 
              && a.nodeName.toLowerCase() == "value"
              && a.nodeValue.toLowerCase() == "on") 
          {
            continue;
          }
          if ( !a.specified 
            // IE claims these are !a.specified even though they are.  Perhaps others too?
            && !(root.tagName.toLowerCase().match(/input|option/) && a.nodeName == 'value')
            && !(root.tagName.toLowerCase().match(/area/) && a.nodeName.match(/shape|coords/i)) 
          )
          {
            continue;
          }
          var name = a.nodeName.toLowerCase();
          if ( /_moz_editor_bogus_node/.test(name) || ( name == 'class' && a.nodeValue == 'webkit-block-placeholder') )
          {
            html = "";
            break;
          }
          if ( /(_moz)|(contenteditable)|(_msh)/.test(name) )
          {
            // avoid certain attributes
            continue;
          }
          var value;
          if ( Xinha.emptyAttributes.indexOf(" "+name+" ") != -1)
          {
            value = name;
          }
          else if ( name != "style" )
          {
            // IE5.5 reports 25 when cellSpacing is
            // 1; other values might be doomed too.
            // For this reason we extract the
            // values directly from the root node.
            // I'm starting to HATE JavaScript
            // development.  Browser differences
            // suck.
            //
            // If you have a percent width in width/height then we may need to use nodeValue
            //  it may well be OK to use nodeValue all the time these days, but it's easier
            //  to just do it for these and not fix what's not broke
            if(name == "width" || name == "height")
            {
              if(a.nodeValue.match(/%$/))
              {
                value = a.nodeValue;
              }
              else
              {
                value = root[a.nodeName];
              }
            }
            // Using Gecko the values of href and src are converted to absolute links
            // unless we get them using nodeValue()            
            else if ( typeof root[a.nodeName] != "undefined" && name != "href" && name != "src" && !(/^on/.test(name)) )
            {
              value = root[a.nodeName];
            }
            else
            {
              value = a.nodeValue;
			  if (name == 'class')
			  {
			  	value = value.replace(/Apple-style-span/,'');
				if (!value) continue;
			  }
              // IE seems not willing to return the original values - it converts to absolute
              // links using a.nodeValue, a.value, a.stringValue, root.getAttribute("href")
              // So we have to strip the baseurl manually :-/
              if ( Xinha.is_ie && (name == "href" || name == "src") )
              {
                value = editor.stripBaseURL(value);
              }

              // High-ascii (8bit) characters in links seem to cause problems for some sites,
              // while this seems to be consistent with RFC 3986 Section 2.4
              // because these are not "reserved" characters, it does seem to
              // cause links to international resources not to work.  See ticket:167

              // IE always returns high-ascii characters un-encoded in links even if they
              // were supplied as % codes (it unescapes them when we pul the value from the link).

              // Hmmm, very strange if we use encodeURI here, or encodeURIComponent in place
              // of escape below, then the encoding is wrong.  I mean, completely.
              // Nothing like it should be at all.  Using escape seems to work though.
              // It's in both browsers too, so either I'm doing something wrong, or
              // something else is going on?

              if ( editor.config.only7BitPrintablesInURLs && ( name == "href" || name == "src" ) )
              {
                value = value.replace(/([^!-~]+)/g, function(match) { return escape(match); });
              }
            }
          }
          else if ( !Xinha.is_ie )
          {
            value = root.style.cssText.replace(/rgb\(.*?\)/ig,function(rgb){ return Xinha._colorToRgb(rgb) });
          }
          else if (!value) // IE8 has style in attributes (see below), but it's empty! 
          {
            continue;
          }

/* This looks wrong, http://trac.xinha.org/ticket/1391#comment:7
          if ( /^(_moz)?$/.test(value) )
          {
            // Mozilla reports some special tags
            // here; we don't need them.
            continue;
          }
*/

          html += " " + name + '="' + Xinha.htmlEncode(value) + '"';
        }
        //IE fails to put style in attributes list & cssText is UPPERCASE
        if ( Xinha.is_ie && root.style.cssText )
        {
          html += ' style="' + root.style.cssText.replace(/(^)?([^:]*):(.*?)(;|$)/g, function(m0, m1,m2,m3, m4){return m2.toLowerCase() + ':' + m3 + m4;}) + '"';
        }
        if ( Xinha.is_ie && root.tagName.toLowerCase() == "option" && root.selected )
        {
          html += ' selected="selected"';
        }
        if ( html !== "" )
        {
          if ( closed && root_tag=="p" )
          {
            //never use <p /> as empty paragraphs won't be visible
            html += ">&nbsp;</p>";
          }
          else if ( closed )
          {
            html += " />";
          }
          else
          {
            html += ">";
          }
        }
      }
      var containsBlock = false;
      if ( root_tag == "script" || root_tag == "noscript" )
      {
        if ( !editor.config.stripScripts )
        {
          if (Xinha.is_ie)
          {
            var innerText = "\n" + root.innerHTML.replace(/^[\n\r]*/,'').replace(/\s+$/,'') + '\n' + indent;
          }
          else
          {
            var innerText = (root.hasChildNodes()) ? root.firstChild.nodeValue : '';
          }
          html += innerText + '</'+root_tag+'>' + ((Xinha.is_ie) ? '\n' : '');
        }
      }
      else if (root_tag == "pre")
      {
        html += ((Xinha.is_ie) ? '\n' : '') + root.innerHTML.replace(/<br>/g,'\n') + '</'+root_tag+'>';
      }
      else
      {
        for ( i = root.firstChild; i; i = i.nextSibling )
        {
          if ( !containsBlock && i.nodeType == 1 && Xinha.isBlockElement(i) )
          {
            containsBlock = true;
          }
          html += Xinha.getHTMLWrapper(i, true, editor, indent + '  ');
        }
        if ( outputRoot && !closed )
        {
          html += (((Xinha.isBlockElement(root) && containsBlock) || root_tag == 'head' || root_tag == 'html') ? ('\n' + indent) : '') + "</" + root.tagName.toLowerCase() + ">";
        }
      }
    break;

    case 3: // Node.TEXT_NODE
      if ( /^script|noscript|style$/i.test(root.parentNode.tagName) )
      {
        html = root.data;
      }
      else if(root.data.trim() == '')
      {
        if(root.data)
        {
          html = ' ';
        }
        else
        {
          html = '';
        }
      }
      else
      {
        html = Xinha.htmlEncode(root.data);
      }
    break;

    case 8: // Node.COMMENT_NODE
      html = "<!--" + root.data + "-->";
    break;
  }
  return html;
};



