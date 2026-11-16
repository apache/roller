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
 * Summernote 0.9.1, the newest release, still calls three jQuery APIs that
 * jQuery 4 removed: jQuery.now() when the editor starts, jQuery.nodeName() in
 * list handling, and .push() on a jQuery object in the table popover. This
 * restores only those three, with their jQuery 3 behavior, and only when they
 * are missing. Remove this file and its <script> tag in head.jsp once a
 * Summernote release supports jQuery 4 (summernote/summernote#4822).
 */
(function ($) {
    if (typeof $.now !== "function") {
        $.now = Date.now;
    }
    if (typeof $.nodeName !== "function") {
        $.nodeName = function (elem, name) {
            return elem.nodeName && elem.nodeName.toLowerCase() === name.toLowerCase();
        };
    }
    if (typeof $.fn.push !== "function") {
        $.fn.push = Array.prototype.push;
    }
}(jQuery));
