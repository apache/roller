/* http://www.kryogenix.org/code/browser/searchhi/ */
/* Modified 20021006 to fix query string parsing and add case insensitivity */
/* Modified 20030227 by sgala@hisitech.com to skip words with "-" and cut %2B (+) preceding pages */

function highlightWord(node,word) 
{
    // Iterate into this nodes childNodes
    if (node.hasChildNodes) 
    {
        var hi_cn;
        for (hi_cn=0;hi_cn<node.childNodes.length;hi_cn++) 
        {
            highlightWord(node.childNodes[hi_cn],word);
        }
    }
	
    // And do this node itself
    if (node.nodeType == 3) 
    { // text node
        tempNodeVal = node.nodeValue.toLowerCase();
        tempWordVal = word.toLowerCase();
        if (tempNodeVal.indexOf(tempWordVal) != -1) 
        {
            pn = node.parentNode;
            if (pn.className != "searchword") 
            {
                // word has not already been highlighted!
                nv = node.nodeValue;
                ni = tempNodeVal.indexOf(tempWordVal);
                // Create a load of replacement nodes
                before = document.createTextNode(nv.substr(0,ni));
                docWordVal = nv.substr(ni,word.length);
                // alert( "Found: " + docWordVal );
                after = document.createTextNode(nv.substr(ni+word.length));
                hiwordtext = document.createTextNode(docWordVal);
                hiword = document.createElement("span");
                hiword.className = "searchword";
                hiword.appendChild(hiwordtext);
                pn.insertBefore(before,node);
                pn.insertBefore(hiword,node);
                pn.insertBefore(after,node);
                pn.removeChild(node);
            }
        }
    }
}

function googleSearchHighlight() 
{
    if (!document.createElement) return;
    ref = document.referrer; //or URL for highlighting in place
    if (ref.indexOf('?') == -1) return;
    qs = ref.substr(ref.indexOf('?')+1);
    qsa = qs.split('&');
    for (i=0;i<qsa.length;i++) 
    {
        qsip = qsa[i].split('=');
        if (qsip.length == 1) continue;

        // q= for Google, p= for Yahoo
        // query= for JSPWiki

        if (qsip[0] == 'query' || qsip[0] == 'q') 
        {
            words = qsip[1].replace(/%2B/g,'');
            words = words.replace(/-\S+\s/g,'');
            words = unescape(words.replace(/\+/g,' ')).split(/\s+/);
            for (w=0;w<words.length;w++) {
                highlightWord(document.getElementsByTagName("body")[0],words[w]);
            }
        }
    }
}

window.onload = googleSearchHighlight;
