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
 * hasActiveHtml(html) parses the HTML in an inert document and returns true if
 * it contains anything the rich editor would run or can't keep: script, frame,
 * plugin, style, raw text and document-level elements, forms and form
 * controls, on* and formaction attributes, javascript: and vbscript: URLs in
 * URL attributes, or markup that parses differently once serialized and parsed
 * again, as the rich editor does for undo and code view. It returns true if
 * the HTML can't be parsed.
 *
 * insertAtCursor(textArea, text) inserts a media file into the textarea
 * fallback at the cursor, or at the end when trackCursor(textArea) hasn't seen
 * the cursor placed.
 */
var RollerRichEditor = (function () {

    // jQuery runs <script> elements it inserts; <base>, <meta>, <link> and
    // <style> act on the whole page; <noscript> parses differently in the inert document,
    // which has scripting off, so its contents can't be checked; <textarea>,
    // <title>, <xmp>, <noembed>, <noframes> and <plaintext> hold raw text that
    // reads as markup when moved elsewhere; SVG <set> and <animate> can change
    // other attributes after loading. The rich editor is inside the entry
    // form, so it drops a <form>'s own tags on save, and <input>, <button> and
    // <select> would be submitted with the entry or submit it themselves.
    var ACTIVE_ELEMENTS = "script, iframe, frame, frameset, object, embed, applet, "
        + "base, meta, link, style, noscript, textarea, title, xmp, noembed, "
        + "noframes, plaintext, set, animate, form, input, button, select";

    // attributes whose value the browser loads or follows as a URL
    var URL_ATTRIBUTES = ["href", "src", "action", "formaction", "xlink:href", "data",
        "poster", "background", "cite", "srcset"];

    // control characters and whitespace that browsers ignore inside a URL scheme
    var IGNORED_IN_SCHEME = /[\s\u0000-\u001f\u007f-\u00a0\u200b-\u200f\u2060\ufeff]/g;

    // A form control named "attributes" or "firstChild" replaces that property
    // on its form, and an image named "body" or "querySelectorAll" does on its
    // document, so these read the properties from the prototypes instead.
    function getter(prototype, name) {
        return Object.getOwnPropertyDescriptor(prototype, name).get;
    }
    var attributesOf = getter(Element.prototype, "attributes");
    var firstChildOf = getter(Node.prototype, "firstChild");
    var nextSiblingOf = getter(Node.prototype, "nextSibling");
    var contentOf = getter(HTMLTemplateElement.prototype, "content");
    var bodyOf = getter(Document.prototype, "body");

    function createElement(doc, name) {
        return Document.prototype.createElement.call(doc, name);
    }

    // the elements under a document, fragment or element that match a selector
    function select(root, selector) {
        var prototype = Element.prototype;
        if (root instanceof Document) {
            prototype = Document.prototype;
        } else if (root instanceof DocumentFragment) {
            prototype = DocumentFragment.prototype;
        }
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

    function hasActiveNodes(root) {
        if (select(root, ACTIVE_ELEMENTS).length) {
            return true;
        }
        var elements = select(root, "*");
        for (var i = 0; i < elements.length; i++) {
            var element = elements[i];
            var attributes = attributesOf.call(element);
            for (var j = 0; j < attributes.length; j++) {
                var name = attributes[j].name.toLowerCase();
                // formaction sends the entry form somewhere else
                if (name.indexOf("on") === 0 || name === "formaction"
                        || hasActiveUrl(name, attributes[j].value)) {
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

    // The rich editor is inside the entry form, where the parser ignores the
    // start tag of another <form>, so the same HTML can become different
    // elements there.
    function parseInForm(doc, html) {
        var form = createElement(doc, "form");
        var holder = form.appendChild(createElement(doc, "div"));
        holder.innerHTML = html;
        return holder;
    }

    function parseFragment(doc, html) {
        var template = createElement(doc, "template");
        template.innerHTML = html;
        return template;
    }

    function hasActiveHtml(html) {
        if (!html) {
            return false;
        }
        try {
            var doc = new DOMParser().parseFromString(html, "text/html");
            var body = doc && bodyOf.call(doc);
            if (!body || hasActiveNodes(doc)) {
                return true;
            }
            // A full document drops table cells and rows found outside a
            // table, but jQuery's html(), which Summernote uses, keeps them.
            // Template contents are parsed the same way and stay inert.
            var template = parseFragment(doc, html);
            if (hasActiveNodes(template.content)) {
                return true;
            }
            var inForm = parseInForm(doc, html);
            if (hasActiveNodes(inForm)) {
                return true;
            }
            // Summernote serializes its content and parses it again for undo
            // and code view; markup such as nested forms comes back as a
            // different tree of elements, which this check hasn't seen.
            var again = new DOMParser().parseFromString(body.innerHTML, "text/html");
            var bodyAgain = again && bodyOf.call(again);
            return !bodyAgain || shape(bodyAgain) !== shape(body)
                || shape(parseFragment(doc, template.innerHTML).content) !== shape(template.content)
                || shape(parseInForm(doc, inForm.innerHTML)) !== shape(inForm);
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
