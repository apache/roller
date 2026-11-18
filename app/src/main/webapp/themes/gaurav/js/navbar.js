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
    function closeDropdowns(except) {
        document.querySelectorAll('.dropdown.open').forEach(function (dropdown) {
            if (dropdown !== except) {
                dropdown.classList.remove('open');
                var toggle = dropdown.querySelector('[data-toggle="dropdown"]');
                if (toggle) {
                    toggle.setAttribute('aria-expanded', 'false');
                }
            }
        });
    }

    document.addEventListener('click', function (event) {
        var toggle = event.target.closest('[data-toggle]');
        var kind = toggle ? toggle.getAttribute('data-toggle') : null;
        if (kind === 'collapse') {
            var target = document.querySelector(toggle.getAttribute('data-target'));
            if (target) {
                var shown = target.classList.toggle('in');
                toggle.classList.toggle('collapsed', !shown);
                toggle.setAttribute('aria-expanded', String(shown));
            }
        } else if (kind === 'dropdown') {
            event.preventDefault();
            var dropdown = toggle.parentNode;
            closeDropdowns(dropdown);
            var open = dropdown.classList.toggle('open');
            toggle.setAttribute('aria-expanded', String(open));
        } else {
            closeDropdowns(null);
        }
    });

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape') {
            closeDropdowns(null);
        }
    });
})();
