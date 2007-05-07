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

package org.apache.roller.ui.authoring.struts2;

import org.apache.roller.pojos.BookmarkData;


/**
 * Bean for managing bookmark data.
 */
public class BookmarkBean {
    
    private String id = null;
    private String name = null;
    private String description = null;
    private String url = null;
    private Integer weight = new Integer(0);
    private Integer priority = new Integer(0);
    private String image = null;
    private String feedUrl = null;
    
    
    public String getId() {
        return this.id;
    }
     
    public void setId( String id ) {
        this.id = id;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName( String name ) {
        this.name = name;
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public void setDescription( String description ) {
        this.description = description;
    }
    
    public String getUrl() {
        return this.url;
    }
    
    public void setUrl( String url ) {
        this.url = url;
    }
    
    public Integer getWeight() {
        return this.weight;
    }
    
    public void setWeight( Integer weight ) {
        this.weight = weight;
    }
    
    public Integer getPriority() {
        return this.priority;
    }
    
    public void setPriority( Integer priority ) {
        this.priority = priority;
    }
    
    public String getImage() {
        return this.image;
    }
    
    public void setImage( String image ) {
        this.image = image;
    }
    
    public String getFeedUrl() {
        return this.feedUrl;
    }
    
    public void setFeedUrl( String feedUrl ) {
        this.feedUrl = feedUrl;
    }
    
    
    public void copyTo(BookmarkData dataHolder) {
        dataHolder.setName(this.name);
        dataHolder.setDescription(this.description);
        dataHolder.setUrl(this.url);
        dataHolder.setWeight(this.weight);
        dataHolder.setPriority(this.priority);
        dataHolder.setImage(this.image);
        dataHolder.setFeedUrl(this.feedUrl);
    }
    
    
    public void copyFrom(BookmarkData dataHolder) {
        this.id = dataHolder.getId();
        this.name = dataHolder.getName();
        this.description = dataHolder.getDescription();
        this.url = dataHolder.getUrl();
        this.weight = dataHolder.getWeight();
        this.priority = dataHolder.getPriority();
        this.image = dataHolder.getImage();
        this.feedUrl = dataHolder.getFeedUrl();
    }
    
}
