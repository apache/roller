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
 */

package org.apache.roller.weblogger.pojos;

import java.io.Serializable;
import java.sql.Timestamp;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.roller.util.UUIDGenerator;


/**
 * Ping target.   Each instance represents a possible target of a weblog update ping that we send.
 * 
 * @author <a href="mailto:anil@busybuddha.org">Anil Gangolli</a>
 */
public class PingTarget implements Serializable {

    public static final long serialVersionUID = -6354583200913127874L;

    // last use (after possible retrials) was successful
    public static final int CONDITION_OK = 0;
    // last use failed after retrials
    public static final int CONDITION_FAILING = 1;
    // disabled by failure policy after failures - editing resets
    public static final int CONDITION_DISABLED = 2;

    private String id = UUIDGenerator.generateUUID();
    private String name = null;
    private String pingUrl = null;
    private int conditionCode = -1;
    private Timestamp lastSuccess = null;
    private boolean autoEnabled = false;


    /**
     * Default empty constructor.
     */
    public PingTarget() {
    }


    /**
     * Constructor.
     *
     * @param id      the id (primary key) of this target
     * @param name    the descriptive name of this target
     * @param pingUrl the URL to which to send the ping
     * @param autoEnable if true, pings sent to target by default
     */
    public PingTarget(String id, String name, String pingUrl, boolean autoEnable) {
        //this.id = id;
        this.name = name;
        this.pingUrl = pingUrl;
        this.conditionCode = CONDITION_OK;
        this.lastSuccess = null;
        this.autoEnabled = autoEnable;
    }


    /**
     * Get the unique id of this ping target.
     *
     * @return the unique id of this ping target.
     */
    public String getId() {
        return this.id;
    }


    /**
     * Set the unique id of this ping target
     *
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }


    /**
     * get the name of this ping target.  This is a name assigned by the administrator or a user (for custom) targets.
     * It is descriptive and is not necessarily unique.
     *
     * @return the name of this ping target
     */
    public String getName() {
        return this.name;
    }


    /**
     * Set the name of this ping target.
     *
     * @param name the name of this ping target
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * Get the URL to ping.
     *
     * @return the URL to ping.
     */
    public String getPingUrl() {
        return pingUrl;
    }


    /**
     * Set the URL to ping.
     *
     * @param pingUrl
     */
    public void setPingUrl(String pingUrl) {
        this.pingUrl = pingUrl;
    }

    /**
     * Get the condition code value.  This code, in combination with the last success timestamp, provides a status
     * indicator on the ping target based on its  usage by the ping queue processor. It can be used to implement a
     * failure-based disabling policy.
     *
     * @return one of the condition codes {@link #CONDITION_OK}, {@link #CONDITION_FAILING}, {@link
     *         #CONDITION_DISABLED}.
     */
    public int getConditionCode() {
        return conditionCode;
    }

    /**
     * Set the condition code value.
     *
     * @param conditionCode the condition code value to set
     */
    public void setConditionCode(int conditionCode) {
        this.conditionCode = conditionCode;
    }


    /**
     * Get the timestamp of the last successful ping (UTC/GMT).
     *
     * @return the timestamp of the last successful ping; <code>null</code> if the target has not yet been used.
     */
    public Timestamp getLastSuccess() {
        return lastSuccess;
    }


    /**
     * Set the timestamp of the last successful ping.
     *
     * @param lastSuccess the timestamp of the last successful ping.
     */
    public void setLastSuccess(Timestamp lastSuccess) {
        this.lastSuccess = lastSuccess;
    }


    /**
     * Is this ping target enabled by default for new weblogs?
     *
     * @return true if ping target is auto enabled. false otherwise.
     */
    public boolean isAutoEnabled() {
        return autoEnabled;
    }


    /**
     * Set the auto enabled status for this ping target.  This field only
     * applies for common ping targets.
     *
     * @param autoEnabled true if the ping target should be auto enabled.
     */
    public void setAutoEnabled(boolean autoEnabled) {
        this.autoEnabled = autoEnabled;
    }


    //------------------------------------------------------- Good citizenship

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("{");
        buf.append(getId());
        buf.append(", ").append(getName());
        buf.append(", ").append(getPingUrl());
        buf.append(", ").append(getLastSuccess());
        buf.append(", ").append(isAutoEnabled());
        buf.append("}");
        return buf.toString();
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof PingTarget)) {
            return false;
        }
        PingTarget o = (PingTarget)other;
        return new EqualsBuilder()
            .append(getId(), o.getId()) 
            .isEquals();
    }
    
    public int hashCode() { 
        return new HashCodeBuilder()
            .append(getId())
            .toHashCode();
    }

}
