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

// The navbar's collapse button and dropdown menu, using the classes
// Bootstrap 3's CSS styles, so the theme doesn't need jQuery or Bootstrap's
// JavaScript.
(function () {
    function toggleOf(dropdown) {
        return dropdown.querySelector('[data-toggle="dropdown"]');
    }

    function closeDropdowns(except) {
        document.querySelectorAll('.dropdown.open').forEach(function (dropdown) {
            if (dropdown !== except) {
                dropdown.classList.remove('open');
                var toggle = toggleOf(dropdown);
                if (toggle) {
                    toggle.setAttribute('aria-expanded', 'false');
                }
            }
        });
    }

    // As Bootstrap 3 did, the toggle gets focus when the menu opens, so the
    // keys below work after a click in browsers that don't focus a clicked
    // link.
    function openDropdown(dropdown, toggle) {
        closeDropdowns(dropdown);
        dropdown.classList.add('open');
        toggle.setAttribute('aria-expanded', 'true');
        toggle.focus();
    }

    // The element a collapse button names in data-target, or in its href.
    function collapseTarget(toggle) {
        var selector = toggle.getAttribute('data-target') || toggle.getAttribute('href');
        if (!selector || selector === '#') {
            return null;
        }
        try {
            return document.querySelector(selector);
        } catch (e) {
            return null;
        }
    }

    document.addEventListener('click', function (event) {
        var toggle = event.target.closest ? event.target.closest('[data-toggle]') : null;
        var kind = toggle ? toggle.getAttribute('data-toggle') : null;
        var dropdown = kind === 'dropdown' ? toggle.closest('.dropdown') : null;
        if (dropdown) {
            event.preventDefault();
            if (dropdown.classList.contains('open')) {
                closeDropdowns(null);
            } else {
                openDropdown(dropdown, toggle);
            }
            return;
        }
        closeDropdowns(null);
        if (kind === 'collapse') {
            if (!toggle.getAttribute('data-target')) {
                event.preventDefault();
            }
            var target = collapseTarget(toggle);
            if (target) {
                var shown = target.classList.toggle('in');
                toggle.classList.toggle('collapsed', !shown);
                toggle.setAttribute('aria-expanded', String(shown));
            }
        }
    });

    // As Bootstrap 3's dropdown did: Arrow Up/Down or Space on the toggle
    // opens the menu, then Arrow Up/Down moves between its items, and Escape
    // closes it and returns focus to the toggle so it isn't left on a hidden
    // link. Keys typed in a form field are left alone.
    document.addEventListener('keydown', function (event) {
        var key = event.key;
        if (key !== 'Escape' && key !== 'ArrowUp' && key !== 'ArrowDown' && key !== ' ') {
            return;
        }
        var target = event.target;
        if (/^(input|textarea|select)$/i.test(target.tagName) || target.isContentEditable) {
            return;
        }
        var dropdown = target.closest ? target.closest('.dropdown') : null;
        var toggle = dropdown ? toggleOf(dropdown) : null;
        if (!toggle) {
            if (key === 'Escape') {
                closeDropdowns(null);
            }
            return;
        }
        var open = dropdown.classList.contains('open');
        if (key === 'Escape') {
            closeDropdowns(null);
            if (open) {
                toggle.focus();
            }
            return;
        }
        event.preventDefault();
        if (!open) {
            openDropdown(dropdown, toggle);
            return;
        }
        var items = Array.prototype.slice.call(
            dropdown.querySelectorAll('.dropdown-menu li:not(.disabled) a'));
        if (items.length === 0) {
            return;
        }
        var index = items.indexOf(document.activeElement);
        if (key === 'ArrowUp' && index > 0) {
            index--;
        } else if (key === 'ArrowDown' && index < items.length - 1) {
            index++;
        }
        items[Math.max(index, 0)].focus();
    });
})();
