/**
  * Based on XML_Utility functions submitted by troels_kn.
  * credit also to adios, who helped with reg exps:
  * http://www.sitepoint.com/forums/showthread.php?t=201052
  * 
  * A replacement for Xinha.getHTML
  *
  * Features:
  *   - Generates XHTML code
  *   - Much faster than Xinha.getHTML
  *   - Eliminates the hacks to accomodate browser quirks
  *   - Returns correct code for Flash objects and scripts
  *   - Formats html in an indented, readable format in html mode
  *   - Preserves script and pre formatting
  *   - Preserves formatting in comments
  *   - Removes contenteditable from body tag in full-page mode
  *   - Supports only7BitPrintablesInURLs config option
  *   - Supports htmlRemoveTags config option
  */
  
function GetHtmlImplementation(editor) {
    this.editor = editor;
}

GetHtmlImplementation._pluginInfo = {
	name          : "GetHtmlImplementation TransformInnerHTML",
	version       : "1.0",
	developer     : "Nelson Bright",
	developer_url : "http://www.brightworkweb.com/",
	sponsor       : "",
    sponsor_url   : "",
	license       : "htmlArea"
};

Xinha.RegExpCache = [
/*00*/  /<\s*\/?([^\s\/>]+)[\s*\/>]/gi,//lowercase tags
/*01*/  /(\s+)_moz[^=>]*=[^\s>]*/gi,//strip _moz attributes
/*02*/  /\s*=\s*(([^'"][^>\s]*)([>\s])|"([^"]+)"|'([^']+)')/g,// find attributes
/*03*/  /\/>/g,//strip singlet terminators
/*04*/  /<(br|hr|img|input|link|meta|param|embed|area)((\s*\S*="[^"]*")*)>/g,//terminate singlet tags
/*05*/  /(<\w+\s+(\w*="[^"]*"\s+)*)(checked|compact|declare|defer|disabled|ismap|multiple|no(href|resize|shade|wrap)|readonly|selected)([\s>])/gi,//expand singlet attributes
/*06*/  /(="[^']*)'([^'"]*")/,//check quote nesting
/*07*/  /&(?=(?!(#[0-9]{2,5};|[a-zA-Z0-9]{2,6};|#x[0-9a-fA-F]{2,4};))[^<]*>)/g,//expand query ampersands not in html entities
/*08*/  /<\s+/g,//strip tagstart whitespace
/*09*/  /\s+(\/)?>/g,//trim whitespace
/*10*/  /\s{2,}/g,//trim extra whitespace
/*11*/  /\s+([^=\s]+)((="[^"]+")|([\s>]))/g,// lowercase attribute names
/*12*/  /\s+contenteditable(=[^>\s\/]*)?/gi,//strip contenteditable
/*13*/  /((href|src)=")([^\s]*)"/g, //find href and src for stripBaseHref()
/*14*/  /<\/?(div|p|h[1-6]|table|tr|td|th|ul|ol|li|dl|dt|dd|blockquote|object|br|hr|img|embed|param|pre|script|html|head|body|meta|link|title|area|input|form|textarea|select|option)[^>]*>/g,
/*15*/  /<\/(div|p|h[1-6]|table|tr|ul|ol|dl|blockquote|html|head|body|script|form|select)( [^>]*)?>/g,//blocklevel closing tag
/*16*/  /<(div|p|h[1-6]|table|tr|ul|ol|dl|blockquote|object|html|head|body|script|form|select)( [^>]*)?>/g,//blocklevel opening tag
/*17*/  /<(td|th|li|dt|dd|option|br|hr|embed|param|pre|meta|link|title|area|input|textarea)[^>]*>/g,//singlet tag or output on 1 line
/*18*/  /(^|<\/(pre|script)>)(\s|[^\s])*?(<(pre|script)[^>]*>|$)/g,//find content NOT inside pre and script tags
/*19*/  /(<pre[^>]*>)([\s\S])*?(<\/pre>)/g,//find content inside pre tags
/*20*/  /(^|<!--[\s\S]*?-->)([\s\S]*?)(?=<!--[\s\S]*?-->|$)/g,//find content NOT inside comments
/*21*/  /\S*=""/g, //find empty attributes
/*22*/  /<!--[\s\S]*?-->|<\?[\s\S]*?\?>|<\/?\w[^>]*>/g, //find all tags, including comments and php
/*23*/  /(^|<\/script>)[\s\S]*?(<script[^>]*>|$)/g //find content NOT inside script tags
];
// compile for performance; WebKit doesn't support this
var testRE = new RegExp().compile(Xinha.RegExpCache[3]);
if (typeof testRE != 'undefined') {
	for (var i=0; i<Xinha.RegExpCache.length;i++ ) {
		Xinha.RegExpCache[i] = new RegExp().compile(Xinha.RegExpCache[i]);
	}
}

/** 
  * Cleans HTML into wellformed xhtml
  */
Xinha.prototype.cleanHTML = function(sHtml) {
	var c = Xinha.RegExpCache;
	sHtml = sHtml.
		replace(c[0], function(str) { return str.toLowerCase(); } ).//lowercase tags/attribute names
		replace(c[1], ' ').//strip _moz attributes
		replace(c[12], ' ').//strip contenteditable
		replace(c[2], '="$2$4$5"$3').//add attribute quotes
		replace(c[21], ' ').//strip empty attributes
		replace(c[11], function(str, p1, p2) { return ' '+p1.toLowerCase()+p2; }).//lowercase attribute names
		replace(c[3], '>').//strip singlet terminators
		replace(c[9], '$1>').//trim whitespace
		replace(c[5], '$1$3="$3"$5').//expand singlet attributes
		replace(c[4], '<$1$2 />').//terminate singlet tags
		replace(c[6], '$1$2').//check quote nesting
		replace(c[7], '&amp;').//expand query ampersands
		replace(c[8], '<').//strip tagstart whitespace
		replace(c[10], ' ');//trim extra whitespace
	if(Xinha.is_ie && c[13].test(sHtml)) {
          sHtml = sHtml.replace(c[13],'$1'+Xinha._escapeDollars(this.stripBaseURL(RegExp.$3))+'"');
	}

	if(this.config.only7BitPrintablesInURLs) {
		if (Xinha.is_ie) c[13].test(sHtml); // oddly the test below only triggers when we call this once before (IE6), in Moz it fails if tested twice
		if ( c[13].test(sHtml)) {
			try { //Mozilla returns an incorrectly encoded value with innerHTML
                          sHtml = sHtml.replace(c[13], '$1'+Xinha._escapeDollars(decodeURIComponent(RegExp.$3).replace(/([^!-~]+|%[0-9]+)/g, function(chr) 
                                                                                                                       {return escape(chr);}))+'"');
			} catch (e) { // once the URL is escape()ed, you can't decodeURIComponent() it anymore
                          sHtml = sHtml.replace(c[13], '$1'+Xinha._escapeDollars(RegExp.$3.replace(/([^!-~]+|%[0-9]+)/g,function(chr){return escape(chr);})+'"'));
			}
		}
	}
	return sHtml;
};

/**
  * Prettyfies html by inserting linebreaks before tags, and indenting blocklevel tags
  */
Xinha.indent = function(s, sindentChar) {
	Xinha.__nindent = 0;
	Xinha.__sindent = "";
	Xinha.__sindentChar = (typeof sindentChar == "undefined") ? "  " : sindentChar;
	var c = Xinha.RegExpCache;
	if(Xinha.is_gecko) { //moz changes returns into <br> inside <pre> tags
		s = s.replace(c[19], function(str){return str.replace(/<br \/>/g,"\n")});
	}
	s = s.replace(c[18], function(strn) { //skip pre and script tags
	  strn = strn.replace(c[20], function(st,$1,$2) { //exclude comments
		string = $2.replace(/[\n\r]/gi, " ").replace(/\s+/gi," ").replace(c[14], function(str) {
			if (str.match(c[16])) {
				var s = "\n" + Xinha.__sindent + str;
				// blocklevel openingtag - increase indent
				Xinha.__sindent += Xinha.__sindentChar;
				++Xinha.__nindent;
				return s;
			} else if (str.match(c[15])) {
				// blocklevel closingtag - decrease indent
				--Xinha.__nindent;
				Xinha.__sindent = "";
				for (var i=Xinha.__nindent;i>0;--i) {
					Xinha.__sindent += Xinha.__sindentChar;
				}
				return "\n" + Xinha.__sindent + str;
			} else if (str.match(c[17])) {
				// singlet tag
				return "\n" + Xinha.__sindent + str;
			}
			return str; // this won't actually happen
		});
		return $1 + string;
	  });return strn;
    });
    //final cleanup
    s = s.replace(/^\s*/,'').//strip leading whitespace
        replace(/ +\n/g,'\n').//strip spaces at end of lines
        replace(/[\r\n]+(\s+)<\/script>/g,'\n$1</script>');//strip returns added into scripts
    return s;
};

Xinha.getHTML = function(root, outputRoot, editor) {
	var html = "";
	var c = Xinha.RegExpCache;

	if(root.nodeType == 11) {//document fragment
	    //we can't get innerHTML from the root (type 11) node, so we 
	    //copy all the child nodes into a new div and get innerHTML from the div
	    var div = document.createElement("div");
	    var temp = root.insertBefore(div,root.firstChild);
	    for (j = temp.nextSibling; j; j = j.nextSibling) { 
	    		temp.appendChild(j.cloneNode(true));
	    }
		html += temp.innerHTML.replace(c[23], function(strn) { //skip content inside script tags
			strn = strn.replace(c[22], function(tag){
				if(/^<[!\?]/.test(tag)) return tag; //skip comments and php tags
				else return editor.cleanHTML(tag)});
			return strn;
		});

	} else {

		var root_tag = (root.nodeType == 1) ? root.tagName.toLowerCase() : ''; 
		if (outputRoot) { //only happens with <html> tag in fullpage mode
			html += "<" + root_tag;
			var attrs = root.attributes; // strangely, this doesn't work in moz
			for (i = 0; i < attrs.length; ++i) {
				var a = attrs.item(i);
				if (!a.specified) {
				  continue;
				}
				var name = a.nodeName.toLowerCase();
				var value = a.nodeValue;
				html += " " + name + '="' + value + '"';
			}
			html += ">";
		}
		if(root_tag == "html") {
			innerhtml = editor._doc.documentElement.innerHTML;
		} else {
			innerhtml = root.innerHTML;
		}
		//pass tags to cleanHTML() one at a time
		//includes support for htmlRemoveTags config option
		html += innerhtml.replace(c[23], function(strn) { //skip content inside script tags
			strn = strn.replace(c[22], function(tag){
				if(/^<[!\?]/.test(tag)) return tag; //skip comments and php tags
				else if(!(editor.config.htmlRemoveTags && editor.config.htmlRemoveTags.test(tag.replace(/<([^\s>\/]+)/,'$1'))))
					return editor.cleanHTML(tag);
				else return ''});
			return strn;
		});
		//IE drops  all </li>,</dt>,</dd> tags in a list except the last one
		if(Xinha.is_ie) {
			html = html.replace(/<(li|dd|dt)( [^>]*)?>/g,'</$1><$1$2>').
				replace(/(<[uod]l[^>]*>[\s\S]*?)<\/(li|dd|dt)>/g, '$1').
				replace(/\s*<\/(li|dd|dt)>(\s*<\/(li|dd|dt)>)+/g, '</$1>').
				replace(/(<dt[\s>][\s\S]*?)(<\/d[dt]>)+/g, '$1</dt>');
		}
		if(Xinha.is_gecko)
			html = html.replace(/<br \/>\n$/, ''); //strip trailing <br> added by moz
		//Cleanup redundant whitespace before </li></dd></dt> in IE and Mozilla
		html = html.replace(/\s*(<\/(li|dd|dt)>)/g, '$1');
		if (outputRoot) {
			html += "</" + root_tag + ">";
		}
		html = Xinha.indent(html);
	};
//	html = Xinha.htmlEncode(html);

	return html;
};

/** 
  * Escapes dollar signs ($) to make them safe to use in regex replacement functions by replacing each $ in the input with $$.
  * 
  * This is advisable any time the replacement string for a call to replace() is a variable and could contain dollar signs that should not be interpreted as references to captured groups (e.g., when you want the text "$10" and not the first captured group followed by a 0).
  * See http://trac.xinha.org/ticket/1337
  */
Xinha._escapeDollars = function(str) {
  return str.replace(/\$/g, "$$$$");
};
