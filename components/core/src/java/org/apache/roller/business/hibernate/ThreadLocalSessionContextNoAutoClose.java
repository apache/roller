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

package org.apache.roller.business.hibernate;

import org.hibernate.FlushMode;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.hibernate.context.ThreadLocalSessionContext;
import org.hibernate.engine.SessionFactoryImplementor;


/**
 * This is a special Hibernate SessionContext which was taken from the Hibernate forums so that we can use it
 * to provide a way to commit our transactions while keeping the Session open for further use.  Details here ...
 *
 * http://forum.hibernate.org/viewtopic.php?t=958752
 *
 * ... which was found from a reference here ...
 *
 * http://forum.hibernate.org/viewtopic.php?t=957056
 *
 * Extends {@link ThreadLocalSessionContext} to allow for long conversations. It achieves this by setting every
 * <code>Session</code> it produces to <code>FlushMode.NEVER</code> so that it won't flush unless explicitly asked
 * to, and by preventing the session from auto-closing or unbinding from the thread after a <code>Transaction</code>
 * commit. Note that this means the application code must do these functions manually as needed!
 */
public class ThreadLocalSessionContextNoAutoClose extends ThreadLocalSessionContext {
    
    /**
     * Create a new instance.
     *
     * @param factory The <code>SessionFactoryImplementor</code> required by the super constructor.
     */
    public ThreadLocalSessionContextNoAutoClose(SessionFactoryImplementor factory) {
        super(factory);
    }
    
    
    /**
     * Returns <code>false</code> to prevent auto closing.
     *
     * @return <code>false</code> to prevent auto closing.
     */
    protected boolean isAutoCloseEnabled() {
        return false;
    }
    
    
    /**
     * Returns <code>false</code> to prevent auto flushing.
     *
     * @return <code>false</code> to prevent auto flushing.
     */
    protected boolean isAutoFlushEnabled() {
        return false;
    }
    
    
    /**
     * Uses <code>super.buildOrObtainSession()</code>, then sets the resulting <code>Session</code>'s flush mode
     * to <code>FlushMode.NEVER</code> to prevent auto-flushing.
     *
     * @return A session configured with <code>FlushMode.NEVER</code>.
     */
    protected Session buildOrObtainSession() {
        Session s = super.buildOrObtainSession();
        s.setFlushMode(FlushMode.NEVER);
        return s;
    }
    
    
    /**
     * Returns an instance of <code>CleanupSynch</code> which prevents auto closing and unbinding.
     *
     * @return A <code>CleanupSynch</code> which prevents auto closing and unbinding.
     */
    protected CleanupSynch buildCleanupSynch() {
        return new NoCleanupSynch(factory);
    }
    
    
    /**
     * A simple extension of <code>CleanupSynch</code> that prevents any cleanup from happening. No session closing or
     * unbinding.
     */
    private static class NoCleanupSynch extends ThreadLocalSessionContext.CleanupSynch {
        
        /**
         * Creates a new instance based on the given factory.
         *
         * @param factory The required <code>SessionFactory</code> that is passed to the super constructor.
         */
        public NoCleanupSynch(SessionFactory factory) {
            super(factory);
        }
        
        /**
         * Does nothing, thus helping to prevent session closing and/or unbinding.
         */
        public void beforeCompletion() {
            // do nothing
        }
        
        /**
         * Does nothing, thus helping to prevent session closing and/or unbinding.
         *
         * @param i
         */
        public void afterCompletion(int i) {
            // do nothing
        }
    }
    
}
