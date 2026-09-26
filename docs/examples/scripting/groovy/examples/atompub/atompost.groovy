/*
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
 */

import com.sun.syndication.propono.atom.client.*
import com.sun.syndication.feed.atom.*

try {

    def authStrategy = new OAuthStrategy(
        "joe", // username
        "xxxxx", // consumer key
        "yyyyy", // consumer secret
        "HMAC-SHA1", // key type
        "http://localhost:8080/roller/roller-services/oauth/requestToken",
        "http://localhost:8080/roller/roller-services/oauth/authorize",
        "http://localhost:8080/roller/roller-services/oauth/accessToken")

    // get the AtomPub service
    def appService = AtomClientFactory.getAtomService(
        "http://localhost:8080/roller/roller-services/app", authStrategy)

    // find workspace of blog
    def blog = appService.findWorkspace("Joe's test blog") // find collecton that will accept entries
    def entries = blog.findCollection(null, "application/atom+xml;type=entry")

    // create and post an entry
    def entry = entries.createEntry()
    entry.title = "TestPost"
    def content = new Content()
    content.setValue("This is a test post. w00t!")
    entry.setContent([content])
    entries.addEntry(entry)

} catch (Exception e) {
    e.printStackTrace();
}