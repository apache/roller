/**
 Roller rendering system servlets.

 <h2>Roller Servlets</h2>

 Most rendering servlets work like this:
 <ul>
 <li>Create a request object to parse the request</li>
 <li>Determine last modified time, return not-modified (HTTP 304) if possible</li>
 <li>Return content from cache if possible</li>
 <li>Load model objects into a map suitable for renderer</li>
 <li>Call most appropriate renderer to render content</li>
 </ul>
 */
package org.apache.roller.weblogger.ui.rendering.servlets;