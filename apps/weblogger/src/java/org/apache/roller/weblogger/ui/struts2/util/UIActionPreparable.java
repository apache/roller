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

package org.apache.roller.weblogger.ui.struts2.util;


/**
 * A Roller specific version of the struts2 Preparable interface.
 *
 * We only have this because we don't want to use the struts2 Preparable IF due
 * to the fact that it is meant to be called much earlier in the interceptor
 * stack, namely before our custom interceptors have been processed.  While
 * that may make sense in some cases, typically we want to prepare things based
 * on the user or weblog that we are working on, so it's often of more use for
 * us to prepare *after* our custom interceptors have been processed.
 *
 * So, this duplicate of the struts2 Preparable interface is here so that we
 * can leave the default struts2 prepare() logic in place for any actions we
 * may define that want to use that, but also provide a prepare option that is
 * executed at the very end of our interceptor stack, just before the action
 * method is executed.  This way our custom prepare method can make use of 
 * anything our custom interceptors provide.
 */
public interface UIActionPreparable {
    
    public void myPrepare();
    
}
