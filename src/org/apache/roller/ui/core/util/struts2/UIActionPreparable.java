/*
 * UIActionPreparable.java
 *
 * Created on April 26, 2007, 3:10 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.apache.roller.ui.core.util.struts2;


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
