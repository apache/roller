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

package org.apache.roller.weblogger.business;

import java.util.List;
import java.util.Map;
import org.apache.roller.weblogger.pojos.Weblog;


/**
 * An interface representing the Roller Planet url strategy.
 *
 * Implementations of this interface provide methods which can be used to form
 * all of the public urls used by Roller Planet.
 */
public interface URLStrategy {
    
    /**
     * Get a version of this url strategy meant for use in previewing and set
     * it to preview a given theme.
     */
    public URLStrategy getPreviewURLStrategy(String previewTheme);
    
    
    /**
     * Url to login page.
     */
    public String getLoginURL(boolean absolute);
    
    
    /**
     * Url to logout page.
     */
    public String getLogoutURL(boolean absolute);
    
    
    /**
     * Get a url to a UI action in a given namespace, optionally specifying
     * a weblogHandle parameter if that is needed by the action.
     */
    public String getActionURL(String action,
                                            String namespace,
                                            String weblogHandle,
                                            Map<String, String> parameters,
                                            boolean absolute);
    
    
    /**
     * Get a url to add a new weblog entry.
     */
    public String getEntryAddURL(String weblogHandle,
                                              boolean absolute);
    
    
    /**
     * Get a url to edit a specific weblog entry.
     */
    public String getEntryEditURL(String weblogHandle,
                                               String entryId,
                                               boolean absolute);
    
    
    /**
     * Get a url to weblog config page.
     */
    public String getWeblogConfigURL(String weblogHandle,
                                                  boolean absolute);
    
    
    /**
     * URL for OpenSearch descriptor file for site.
     */
    public String getOpenSearchSiteURL();
    

    /**
     * URL for OpenSearch descriptor file for weblog.
     */
    public String getOpenSearchWeblogURL(String weblogHandle);

    
    /**
     * Get OpenSearch compatible search URL template for weblog search feed.
     */
    public String getWeblogSearchFeedURLTemplate(Weblog weblog);

    
    /**
     * Get OpenSearch compatible search URL template for weblog search page.
     */
    public String getWeblogSearchPageURLTemplate(Weblog weblog);

    
    public String getXmlrpcURL(boolean absolute);
    
    
    public String getAtomProtocolURL(boolean absolute);
    
    
    /**
     * Get root url for a given weblog.  Optionally for a certain locale.
     */
    public String getWeblogURL(Weblog weblog,
                                            String locale,
                                            boolean absolute);
    
    
    /**
     * Get url for a single weblog entry on a given weblog.
     */
    public String getWeblogEntryURL(Weblog weblog,
                                                 String locale,
                                                 String entryAnchor,
                                                 boolean absolute);
    
    
    /**
     * Get url for a single weblog entry comments on a given weblog.
     */
    public String getWeblogCommentsURL(Weblog weblog,
                                                    String locale,
                                                    String entryAnchor,
                                                    boolean absolute);
    
    
    /**
     * Get url for a single weblog entry comment on a given weblog.
     */
    public String getWeblogCommentURL(Weblog weblog,
                                                   String locale,
                                                   String entryAnchor,
                                                   String timeStamp,
                                                   boolean absolute);
    
    
    /**
     * Get url for a collection of entries on a given weblog.
     */
    public String getWeblogCollectionURL(Weblog weblog,
                                                      String locale,
                                                      String category,
                                                      String dateString,
                                                      List tags,
                                                      int pageNum,
                                                      boolean absolute);
    
    
    /**
     * Get url for a custom page on a given weblog.
     */
    public String getWeblogPageURL(Weblog weblog,
                                                String locale,
                                                String pageLink,
                                                String entryAnchor,
                                                String category,
                                                String dateString,
                                                List tags,
                                                int pageNum,
                                                boolean absolute);
    
    
    /**
     * Get url for a feed on a given weblog.
     */
    public String getWeblogFeedURL(Weblog weblog,
                                                String locale,
                                                String type,
                                                String format,
                                                String category,
                                                String term,
                                                List tags,
                                                boolean excerpts,
                                                boolean absolute);
    
    
    /**
     * Get url to search endpoint on a given weblog.
     */
    public String getWeblogSearchURL(Weblog weblog,
                                                  String locale,
                                                  String query,
                                                  String category,
                                                  int pageNum,
                                                  boolean absolute);
    
    
    /**
     * Get url to a resource on a given weblog.
     */
    public String getWeblogResourceURL(Weblog weblog,
                                                    String filePath,
                                                    boolean absolute);
    
    
    /**
     * Get url to rsd file on a given weblog.
     */
    public String getWeblogRsdURL(Weblog weblog, boolean absolute);
    
    
    /**
     * Get url to JSON tags service url, optionally for a given weblog.
     */
    public String getWeblogTagsJsonURL(Weblog weblog, boolean absolute, int pageNum);


    /* Get URL for obtaining OAuth Request Token */
    public String getOAuthRequestTokenURL();

    /* Get URL authorizing an OAuth Request Token */
    public String getOAuthAuthorizationURL();

    /* Get URL for obtaining OAuth Access Token */
    public String getOAuthAccessTokenURL();
}
