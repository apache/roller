<#--
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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
 *
 * Overrides the struts2-bootstrap-plugin template, which still emits the
 * pre-Bootstrap-5 "data-toggle" attribute. Bootstrap 5 reads "data-bs-toggle"
 * and tooltips are opt-in, so roller.js initialises these.
-->
<#if attributes.tooltip?? && attributes.tooltip?has_content>
 <i class="bi bi-info-circle s2b_tooltip" data-bs-toggle="tooltip" title="${attributes.tooltip}"></i>
</#if><#t/>
