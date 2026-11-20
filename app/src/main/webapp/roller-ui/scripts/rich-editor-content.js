/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * Summernote loads an entry's HTML into the live page, where event handler
 * attributes, scripts and embedded frames run as the person editing it. Entries
 * can hold such HTML (videos, slide decks and other embeds), and Summernote
 * would also drop some of it on save, so those entries open in the plain HTML
 * textarea instead. The stored HTML is never changed.
 *
 * hasActiveHtml(html) parses the HTML the way the rich editor loads it, with
 * jQuery's html(), but in an inert standards-mode document, and returns true
 * if it contains anything the rich editor would run or can't keep: script,
 * frame, plugin, style, raw text and document-level elements, forms and form
 * controls, on* and formaction attributes, javascript: and vbscript: URLs in
 * URL attributes, Bootstrap's data attributes, markup in a link's href or in a
 * URL in the text, or markup that parses differently once serialized and
 * parsed again, as the rich editor does for undo and code view. It returns true
 * if the HTML can't be parsed.
 *
 * insertAtCursor(textArea, text) inserts a media file into the textarea
 * fallback at the cursor, or at the end when trackCursor(textArea) hasn't seen
 * the cursor placed.
 */
var RollerRichEditor = (function () {

    // jQuery runs <script> elements it inserts; <base>, <link> and <style>
    // act on the whole page, as does a <meta> that isn't microdata (see
    // isActiveElement); <noscript> parses differently in the inert document,
    // which has scripting off, so its contents can't be checked; <textarea>,
    // HTML <title>, <xmp>, <noembed>, <noframes> and <plaintext> hold raw text
    // that reads as markup when moved elsewhere; SVG <set> and <animate> can
    // change other attributes after loading. The rich editor is inside the
    // entry form, so it drops a <form>'s own tags on save, and <input>,
    // <button> and <select> would be submitted with the entry or submit it
    // themselves.
    var ACTIVE_ELEMENTS = "script, iframe, frame, frameset, object, embed, applet, "
        + "base, link, style, noscript, textarea, xmp, noembed, "
        + "noframes, plaintext, set, animate, form, input, button, select";

    var HTML_NAMESPACE = "http://www.w3.org/1999/xhtml";

    // attributes whose value the browser loads or follows as a URL
    var URL_ATTRIBUTES = ["href", "src", "action", "formaction", "xlink:href", "data",
        "poster", "background", "cite", "srcset"];

    // Bootstrap's data API acts on clicks anywhere in the page; Bootstrap 3
    // opens the dialog an element names and loads the element's link into it.
    var BOOTSTRAP_ATTRIBUTES = ["data-toggle", "data-target", "data-dismiss", "data-slide",
        "data-slide-to"];

    // When the author types a space after a word that starts like a URL,
    // Summernote's autolink parses the word as HTML to make the link's text.
    // Its words end at spaces and no-break spaces only.
    var AUTOLINKED = /^(?:[a-z][a-z0-9+,.-]*:\/\/|tel:|mailto:|xmpp:|www\.)/i;
    var WORD_BREAK = /[ \u00a0]/;

    // control characters and whitespace that browsers ignore inside a URL scheme
    var IGNORED_IN_SCHEME = /[\s\u0000-\u001f\u007f- ​-‏⁠﻿]/g;

    // jQuery's html() sets innerHTML in place, unless the HTML has one of
    // these tags or its first tag, which can be inside a comment, is a table
    // part. Then it parses the HTML in a detached element, inside the table
    // elements that part needs.
    var FIRST_TAG = /<([a-z][^\/\0>\x20\t\r\n\f]*)/i;
    var NOT_IN_PLACE = /<script|<style|<link/i;
    var TABLE_WRAPPERS = {
        thead: ["table"], tbody: ["table"], tfoot: ["table"], colgroup: ["table"],
        caption: ["table"], col: ["table", "colgroup"], tr: ["table", "tbody"],
        td: ["table", "tbody", "tr"], th: ["table", "tbody", "tr"]
    };

    // Summernote 0.9 removes these tags from the HTML when the author leaves
    // code view, and keeps an iframe only if it comes from one of a list of
    // video sites.
    var CODE_VIEW_FILTER = /<\/*(?:applet|b(?:ase|gsound|link)|embed|frame(?:set)?|ilayer|l(?:ayer|ink)|meta|object|s(?:cript|tyle)|t(?:itle|extarea)|xml)[^>]*?>/gi;
    var CODE_VIEW_IFRAME = /<iframe/i;

    // how many trips through code view in a row the check follows
    var CODE_VIEW_TRIPS = 3;

    // A form control named "attributes" or "firstChild" replaces that property
    // on its form, so these read the properties from the prototypes instead.
    function getter(prototype, name) {
        return Object.getOwnPropertyDescriptor(prototype, name).get;
    }
    var attributesOf = getter(Element.prototype, "attributes");
    var firstChildOf = getter(Node.prototype, "firstChild");
    var lastChildOf = getter(Node.prototype, "lastChild");
    var nextSiblingOf = getter(Node.prototype, "nextSibling");
    var innerHtmlOf = getter(Element.prototype, "innerHTML");
    var contentOf = getter(HTMLTemplateElement.prototype, "content");

    // the elements under a fragment or element that match a selector
    function select(root, selector) {
        var prototype = root instanceof DocumentFragment ? DocumentFragment.prototype : Element.prototype;
        return prototype.querySelectorAll.call(root, selector);
    }

    // a <template>'s contents, which are a separate fragment
    function templateContent(element) {
        return element instanceof HTMLTemplateElement ? contentOf.call(element) : null;
    }

    function hasActiveScheme(url) {
        var compact = url.replace(IGNORED_IN_SCHEME, "").toLowerCase();
        return compact.indexOf("javascript:") === 0 || compact.indexOf("vbscript:") === 0;
    }

    function hasActiveUrl(name, value) {
        if (URL_ATTRIBUTES.indexOf(name) === -1) {
            return false;
        }
        if (name !== "srcset") {
            return hasActiveScheme(value);
        }
        // each image candidate is a URL, then an optional width or density
        var candidates = value.split(",");
        for (var i = 0; i < candidates.length; i++) {
            if (hasActiveScheme(candidates[i])) {
                return true;
            }
        }
        return false;
    }

    // An SVG <title> is a tooltip, not raw text, and a <meta> with itemprop
    // and none of name, http-equiv and charset is microdata.
    function isActiveElement(element) {
        if (element.namespaceURI !== HTML_NAMESPACE) {
            return false;
        }
        var has = function (name) {
            return Element.prototype.hasAttribute.call(element, name);
        };
        return element.localName === "title" || (element.localName === "meta"
            && (!has("itemprop") || has("name") || has("http-equiv") || has("charset")));
    }

    function hasActiveNodes(root) {
        if (select(root, ACTIVE_ELEMENTS).length) {
            return true;
        }
        var elements = select(root, "*");
        for (var i = 0; i < elements.length; i++) {
            var element = elements[i];
            if (isActiveElement(element)) {
                return true;
            }
            var attributes = attributesOf.call(element);
            for (var j = 0; j < attributes.length; j++) {
                var name = attributes[j].name.toLowerCase();
                var value = attributes[j].value;
                // formaction sends the entry form somewhere else, and
                // Summernote 0.8's link popover shows a link's href as HTML
                if (name.indexOf("on") === 0 || name === "formaction" || hasActiveUrl(name, value)
                        || (name === "href" && value.indexOf("<") !== -1)
                        || BOOTSTRAP_ATTRIBUTES.indexOf(name) !== -1 || name.indexOf("data-bs-") === 0) {
                    return true;
                }
            }
            // querySelector skips template contents
            var content = templateContent(element);
            if (content && hasActiveNodes(content)) {
                return true;
            }
        }
        return false;
    }

    // whether text under a node has a URL-like word with markup in it
    function hasMarkupToAutolink(node) {
        for (var child = firstChildOf.call(node); child; child = nextSiblingOf.call(child)) {
            if (child.nodeType === Node.TEXT_NODE) {
                var words = child.nodeValue.split(WORD_BREAK);
                for (var i = 0; i < words.length; i++) {
                    if (words[i].indexOf("<") !== -1 && AUTOLINKED.test(words[i])) {
                        return true;
                    }
                }
            } else if (hasMarkupToAutolink(child)) {
                return true;
            }
        }
        return false;
    }

    // The elements under a node, with their namespaces and attributes, in
    // order. Text is left out: a parser drops a newline at the start of a
    // <pre> each time, which changes nothing that runs.
    function shape(node) {
        var out = "";
        for (var child = firstChildOf.call(node); child; child = nextSiblingOf.call(child)) {
            if (!(child instanceof Element)) {
                continue;
            }
            var attributes = attributesOf.call(child);
            out += "<" + child.namespaceURI + " " + child.localName;
            for (var i = 0; i < attributes.length; i++) {
                out += " " + attributes[i].name + "=" + JSON.stringify(attributes[i].value);
            }
            out += ">" + shape(templateContent(child) || child) + "</>";
        }
        return out;
    }

    // What jQuery's html() builds from the HTML, for jQuery 3 and 4: for each
    // parse, a holder with every element the parser made, and the element
    // whose children the rich editor gets, or null where jQuery would fail.
    function parseAsEditor(doc, html) {
        var tag = (FIRST_TAG.exec(html) || ["", ""])[1].toLowerCase();
        var wrappers = TABLE_WRAPPERS.hasOwnProperty(tag) ? TABLE_WRAPPERS[tag] : null;
        if (!wrappers && !NOT_IN_PLACE.test(html)) {
            // In place, in the rich editor inside the entry form, where the
            // parser ignores the start tag of another <form>. Outside it, the
            // same HTML shows the forms the rich editor would drop on save.
            var form = doc.createElement("form");
            var editor = form.appendChild(doc.createElement("div"));
            editor.innerHTML = html;
            var outside = doc.createElement("div");
            outside.innerHTML = html;
            return [{holder: editor, content: editor}, {holder: outside, content: outside}];
        }
        wrappers = wrappers || [];
        // jQuery 3 adds the wrappers' tags around the HTML, then takes the
        // last child at each level
        var asText = doc.createElement("div");
        asText.innerHTML = wrappers.length ? "<" + wrappers.join("><") + ">" + html
            + "</" + wrappers.slice().reverse().join("></") + ">" : html;
        var content = asText;
        for (var i = 0; i < wrappers.length && content; i++) {
            content = lastChildOf.call(content);
        }
        // jQuery 4 parses the HTML inside the wrapper elements
        var asElements = doc.createElement("div");
        var inner = asElements;
        for (var j = 0; j < wrappers.length; j++) {
            inner = inner.appendChild(doc.createElement(wrappers[j]));
        }
        inner.innerHTML = html;
        return [{holder: asText, content: content}, {holder: asElements, content: inner}];
    }

    // Whether the rich editor could run or lose something in the HTML as it
    // loads it, and as it loads it again for undo and when leaving code view.
    function isActive(doc, html, codeViewTrips) {
        var parses = parseAsEditor(doc, html);
        for (var i = 0; i < parses.length; i++) {
            var content = parses[i].content;
            if (!content || hasActiveNodes(parses[i].holder) || hasMarkupToAutolink(content)) {
                return true;
            }
            // Summernote serializes its content and loads it again for undo;
            // markup such as nested forms comes back as a different tree of
            // elements, which this check hasn't seen.
            var serialized = innerHtmlOf.call(content);
            var again = parseAsEditor(doc, serialized);
            for (var j = 0; j < again.length; j++) {
                if (!again[j].content || shape(again[j].content) !== shape(content)) {
                    return true;
                }
            }
            // Leaving code view removes tags from the serialized HTML, which
            // can turn text inside a comment or an attribute into markup.
            if (CODE_VIEW_IFRAME.test(serialized)) {
                return true;
            }
            var viewed = serialized.replace(CODE_VIEW_FILTER, "");
            if (viewed !== serialized
                    && (codeViewTrips === 0 || isActive(doc, viewed, codeViewTrips - 1))) {
                return true;
            }
        }
        return false;
    }

    function hasActiveHtml(html) {
        if (!html) {
            return false;
        }
        try {
            // a standards-mode document, like the editor page, that runs nothing
            var doc = document.implementation.createHTMLDocument("");
            return isActive(doc, html, CODE_VIEW_TRIPS);
        } catch (e) {
            return true;
        }
    }

    // A textarea's cursor is at 0 both before anyone has placed it and when
    // someone has put it at the very start, so this remembers which it is.
    function trackCursor(textArea) {
        var placed = function () {
            textArea.rollerCursorPlaced = true;
        };
        textArea.addEventListener("focus", placed);
        textArea.addEventListener("select", placed);
    }

    function insertAtCursor(textArea, text) {
        var start = textArea.selectionStart;
        var end = textArea.selectionEnd;
        if (!textArea.rollerCursorPlaced) {
            start = end = textArea.value.length;
        }
        textArea.value = textArea.value.substring(0, start) + text + textArea.value.substring(end);
        textArea.selectionStart = textArea.selectionEnd = start + text.length;
        textArea.focus();
    }

    return {hasActiveHtml: hasActiveHtml, trackCursor: trackCursor, insertAtCursor: insertAtCursor};
}());
