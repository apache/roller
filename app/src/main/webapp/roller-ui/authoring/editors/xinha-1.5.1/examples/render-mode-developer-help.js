/** Load this javascript at the bottom of the page to output some information about what 
 *   rendering mode is being used by the browser (based on doctype detection).
 */

window.setTimeout(function(){
  if(typeof document.doctype == 'undefined' || document.doctype == null) return;
  var doctypeHelp = '\
  <fieldset style="margin-top:20px;">\
    <legend style="font-weight:bold;">Browser Rendering Mode</legend>\
    <p>Assuming that your browser is fairly modern, this page is being rendered in </p>\
    <blockquote>\
      <p>';
  
        doctypeHelp += '' + document.compatMode + ' - ';
        if(document.compatMode == 'BackCompat')
        {
          doctypeHelp += ('Quirks Mode');
        }
        else
        {
          function get_doctype()
          {
              var doctype = 
              '<!DOCTYPE ' + 
              document.doctype.name +
              (document.doctype.publicId?' PUBLIC "' +  document.doctype.publicId + '"':'') +
              (document.doctype.systemId?' "' + document.doctype.systemId + '"':'') + '>';
              return doctype;
          }
          
          var doctypeString = get_doctype();              
          
          
          switch(doctypeString)
          {
            case '<!DOCTYPE html>':
            case '<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0//EN">':
            case '<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">':
            case '<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0//EN" "http://www.w3.org/TR/html4/strict.dtd">':
            case '<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">':
            case '<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML Basic 1.0//EN" "http://www.w3.org/TR/xhtml-basic/xhtml-basic10.dtd">':
            case '<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">':
            case '<!DOCTYPE HTML PUBLIC "ISO/IEC 15445:2000//DTD HTML//EN">':
            case '<!DOCTYPE HTML PUBLIC "ISO/IEC 15445:2000//DTD HyperText Markup Language//EN">':
            case '<!DOCTYPE HTML PUBLIC "ISO/IEC 15445:1999//DTD HTML//EN">':
            case '<!DOCTYPE HTML PUBLIC "ISO/IEC 15445:1999//DTD HyperText Markup Language//EN">':
              doctypeHelp += ('Standards Mode<br/>'+Xinha.htmlEncode(doctypeString));              
              break;
              
            case '<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">':
            case '<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">':
            case '<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/1999/REC-html401-19991224/loose.dtd">':
            case '<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">':
              doctypeHelp += ('Almost Standards Mode<br/>'+Xinha.htmlEncode(doctypeString));
              break;
            
            default:
              doctypeHelp += ('Either Standards Mode, or Almost Standards Mode<br/>'+Xinha.htmlEncode(doctypeString));
              break;
          }
        }
        doctypeHelp += ('\
      </p>\
    </blockquote>\
    <p>\
      To change the mode alter the doctype on this file, '+this.document.location.pathname+', to one of\
    </p>\
    <dl>\
      <dt style="font-weight:bold;">Standards</dt>\
        <dd style="margin-bottom:10px;">&lt;!DOCTYPE html&gt;</dd>\
        \
      <dt style="font-weight:bold;">Almost Standards</dt>\
        <dd style="margin-bottom:10px;">&lt;!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd"&gt;</dd>\
        \
      <dt style="font-weight:bold;">Quirks</dt>\
        <dd style="margin-bottom:10px;">(No Doctype at all)</dd>\
    </dl>\
    <p style="border:2px solid red;text-align:center;margin:10px;padding:10px;">This information is provided by render-mode-developer-help.js, remove that script from your page to remove this help text.</p>\
  </fieldset>');
  document.getElementById('doctypeHelpText').innerHTML = doctypeHelp;
}, 500);

// Because adding the above to the document after it's loaded will quite possibly
// introduce a vertical scrollbar, of which Xinha won't be aware, we would have 
// to issue a sizeEditors
document.write('<div id="doctypeHelpText" style="height:2000px"></div>');
        