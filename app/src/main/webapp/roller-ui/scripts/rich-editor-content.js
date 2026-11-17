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
 * plugin and document-level elements, on* attributes, or javascript: and
 * vbscript: URLs in any attribute. It returns true if the HTML can't be parsed.
 */
var RollerRichEditor = (function () {

    // jQuery runs <script> elements it inserts; <base>, <meta> and <link> act
    // on the whole page; <noscript> parses differently in the inert document,
    // which has scripting off, so its contents can't be checked; SVG <set> and
    // <animate> can change other attributes after loading.
    var ACTIVE_ELEMENTS = "script, iframe, frame, frameset, object, embed, applet, "
        + "base, meta, link, noscript, set, animate";

    // control characters and whitespace that browsers ignore inside a URL scheme
    var IGNORED_IN_SCHEME = /[\s\u0000-\u001f\u007f-\u00a0\u200b-\u200f\u2060\ufeff]/g;

    function hasActiveUrl(value) {
        var compact = value.replace(IGNORED_IN_SCHEME, "").toLowerCase();
        return compact.indexOf("javascript:") !== -1 || compact.indexOf("vbscript:") !== -1;
    }

    function hasActiveNodes(root) {
        if (root.querySelector(ACTIVE_ELEMENTS)) {
            return true;
        }
        var elements = root.querySelectorAll("*");
        for (var i = 0; i < elements.length; i++) {
            var element = elements[i];
            var attributes = element.attributes;
            for (var j = 0; j < attributes.length; j++) {
                var name = attributes[j].name.toLowerCase();
                if (name.indexOf("on") === 0 || hasActiveUrl(attributes[j].value)) {
                    return true;
                }
            }
            // template contents are a separate fragment that querySelector skips
            if (element.content && element.content.nodeType === 11 && hasActiveNodes(element.content)) {
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
            var doc = new DOMParser().parseFromString(html, "text/html");
            return !doc || !doc.documentElement || hasActiveNodes(doc);
        } catch (e) {
            return true;
        }
    }

    return {hasActiveHtml: hasActiveHtml};
}());
