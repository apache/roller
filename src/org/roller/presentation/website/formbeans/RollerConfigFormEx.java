
package org.roller.presentation.website.formbeans;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionMapping;
import org.roller.RollerException;
import org.roller.pojos.RollerConfigData;
import org.roller.presentation.forms.RollerConfigForm;

import sun.security.krb5.internal.crypto.d;

/**
 * These properties are not persistent and are only needed for the UI.
 *
 * @struts.form name="rollerConfigFormEx"
 * @author Lance Lavandowska
 */
public class RollerConfigFormEx extends RollerConfigForm
{
    public RollerConfigFormEx()
    {
    }

    public RollerConfigFormEx( RollerConfigData config, java.util.Locale locale ) throws RollerException
    {
        super(config, locale);
    }

    /**
     * Override for non-primitive values
     */
    public void copyFrom(org.roller.pojos.RollerConfigData dataHolder, java.util.Locale locale) throws RollerException
    {
    	super.copyFrom(dataHolder, locale);
        fixNulls();
        this.uploadMaxFileMB = dataHolder.getUploadMaxFileMB();
        this.uploadMaxDirMB = dataHolder.getUploadMaxDirMB();
    }

    /**
     * Override for non-primitive values
     */
    public void copyTo(org.roller.pojos.RollerConfigData dataHolder, java.util.Locale locale) throws RollerException
    {
        fixNulls();
        super.copyTo(dataHolder, locale);
        dataHolder.setUploadMaxFileMB(this.uploadMaxFileMB);
        dataHolder.setUploadMaxDirMB(this.uploadMaxDirMB);
    }

    /**
     * Method allows Struts to handle empty checkboxes for booleans
     */
	public void reset(ActionMapping mapping, HttpServletRequest request) 
	{
        setAbsoluteURL( null );
        fixNulls();
	}
    
    private void fixNulls()
    {
        if (getRssUseCache() == null) setRssUseCache( Boolean.FALSE );
        if (getNewUserAllowed() == null) setNewUserAllowed( Boolean.FALSE );
        if (getEnableAggregator() == null) setEnableAggregator( Boolean.FALSE );
        if (getUploadEnabled() == null) setUploadEnabled( Boolean.FALSE );
        if (getMemDebug() == null) setMemDebug( Boolean.FALSE );
        if (getAutoformatComments() == null) setAutoformatComments( Boolean.FALSE );
        if (getEscapeCommentHtml() == null) setEscapeCommentHtml( Boolean.FALSE );
        if (getEmailComments() == null) setEmailComments( Boolean.FALSE );
        if (getEnableLinkback() == null) setEnableLinkback( Boolean.FALSE );        
        if (getEncryptPasswords() == null) setEncryptPasswords( Boolean.FALSE );        
    }
}