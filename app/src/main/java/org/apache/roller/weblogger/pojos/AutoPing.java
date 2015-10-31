/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */

package org.apache.roller.weblogger.pojos;

import java.io.Serializable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.roller.weblogger.WebloggerCommon;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;


/**
 * Automatic ping configuration.  An instance of this class relates a weblog
 * and ping target; it indicates that the specified ping target should be pinged
 * when the corresponding weblog is changed.
 */
@Entity
@Table(name="weblog_ping_target")
@NamedQueries({
    @NamedQuery(name="AutoPing.getAll",
        query="SELECT a FROM AutoPing a"),

    @NamedQuery(name="AutoPing.getByPingTarget",
        query="SELECT a FROM AutoPing a WHERE a.pingTarget = ?1"),

    @NamedQuery(name="AutoPing.getByWeblog",
        query="SELECT a FROM AutoPing a WHERE a.weblog = ?1"),

    @NamedQuery(name="AutoPing.removeByPingTarget",
        query="DELETE FROM AutoPing a WHERE a.pingTarget = ?1"),

    @NamedQuery(name="AutoPing.removeByPingTarget&Weblog",
        query="DELETE FROM AutoPing a WHERE a.pingTarget = ?1 AND a.weblog = ?2")
})
public class AutoPing implements Serializable {

    public static final long serialVersionUID = -9105985454111986435L;

    // Unique ID of object
    private String id = WebloggerCommon.generateUUID();

    // Weblog whose changes should result in a ping to the ping target specified by this object.
    private Weblog weblog = null;

    // Get the target to be pinged when the corresponding weblog changes.
    private PingTarget pingTarget = null;

    public AutoPing() {
    }

    public AutoPing(PingTarget pingtarget, Weblog weblog) {
        this.weblog = weblog;
        this.pingTarget = pingtarget;
    }

    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ManyToOne
    @JoinColumn(name="weblogid",nullable=false)
    public Weblog getWeblog() {
        return weblog;
    }

    public void setWeblog(Weblog weblog) {
        this.weblog = weblog;
    }

    @ManyToOne
    @JoinColumn(name="pingtargetid",nullable=false)
    public PingTarget getPingTarget() {
        return pingTarget;
    }

    public void setPingTarget(PingTarget pingtarget) {
        this.pingTarget = pingtarget;
    }

    public String toString() {
        return "{ weblog: " + getWeblog().getHandle()
                + " ping target: " + getPingTarget().getPingUrl() + " }";
    }
    
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof AutoPing)) {
            return false;
        }
        AutoPing o = (AutoPing)other;
        return new EqualsBuilder()
            .append(getPingTarget(), o.getPingTarget())
            .append(getWeblog(), o.getWeblog())
            .isEquals();
    }
    
    public int hashCode() { 
        return new HashCodeBuilder().append(getId()).toHashCode();
    }
    
}
