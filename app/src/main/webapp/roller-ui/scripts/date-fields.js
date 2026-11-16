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
 * Date fields in the admin UI: a readonly input.date-picker inside an .input-group,
 * optionally followed by a .date-picker-show button (opens the picker) and a
 * .date-picker-clear button (empties the field, so a filter date can be removed).
 */
$(function () {
    $(".date-picker").datepicker();

    $(document).on("click", ".date-picker-show", function () {
        $(this).closest(".input-group").find(".date-picker").datepicker("show");
    });

    $(document).on("click", ".date-picker-clear", function () {
        $(this).closest(".input-group").find(".date-picker").val("").trigger("change");
    });
});
