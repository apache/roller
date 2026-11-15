/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

/*
 * Highlights the words of a search in the search results page.
 *
 * The page marks the element that holds the results with a
 * data-search-term attribute. Each word of the term found in the text under
 * that element is wrapped in <span class="searchword">, which the page
 * styles. Only text nodes are read and new nodes created, so the term is
 * never parsed as markup.
 */
(function () {
    'use strict';

    // Elements whose text is not shown as page content
    var SKIPPED = ['SCRIPT', 'STYLE', 'NOSCRIPT', 'TEXTAREA', 'TITLE'];

    // Lucene query operators, which are not words to look for
    var OPERATORS = ['AND', 'OR', 'NOT', 'TO'];

    function wordsOf(term) {
        var words = [];
        term.split(/\s+/).forEach(function (raw) {
            // Drop query syntax around a word: +required, -excluded,
            // "phrases", (groups) and wildcards
            var word = raw.replace(/^[+\-!("]+/, '').replace(/[)"*?~^]+$/, '');
            if (word && OPERATORS.indexOf(word) < 0) {
                var lower = word.toLowerCase();
                if (words.indexOf(lower) < 0) {
                    words.push(lower);
                }
            }
        });
        // Longest first, so that a longer word wins where words overlap
        return words.sort(function (a, b) { return b.length - a.length; });
    }

    function escapeForRegExp(text) {
        return text.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    }

    function textNodesUnder(root) {
        var nodes = [];
        var walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT, null);
        var node;
        while ((node = walker.nextNode())) {
            var parent = node.parentNode;
            if (SKIPPED.indexOf(parent.nodeName) < 0 && parent.className !== 'searchword') {
                nodes.push(node);
            }
        }
        return nodes;
    }

    function highlight(root, words) {
        var pattern = new RegExp(words.map(escapeForRegExp).join('|'), 'gi');

        textNodesUnder(root).forEach(function (node) {
            var text = node.nodeValue;
            var pieces = null;
            var start = 0;
            var match;

            pattern.lastIndex = 0;
            while ((match = pattern.exec(text)) !== null) {
                pieces = pieces || document.createDocumentFragment();
                if (match.index > start) {
                    pieces.appendChild(document.createTextNode(text.substring(start, match.index)));
                }
                var span = document.createElement('span');
                span.className = 'searchword';
                span.appendChild(document.createTextNode(match[0]));
                pieces.appendChild(span);
                start = pattern.lastIndex;
            }

            if (pieces) {
                if (start < text.length) {
                    pieces.appendChild(document.createTextNode(text.substring(start)));
                }
                node.parentNode.replaceChild(pieces, node);
            }
        });
    }

    function run() {
        var results = document.querySelector('[data-search-term]');
        if (!results) {
            return;
        }
        var words = wordsOf(results.getAttribute('data-search-term') || '');
        if (words.length > 0) {
            highlight(results, words);
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', run);
    } else {
        run();
    }
}());
