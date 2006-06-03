/*
 * Created on May 29, 2003
 */
package org.roller.presentation.velocity.plugins.jspwiki;

import com.ecyrd.jspwiki.providers.FileSystemProvider;

/**
 * Override page provider so that Wiki links always appear as hyperlinks
 * to the external Wiki's Wiki.jsp?page=MyPageName page. Without this, they
 * would appear as links tp Edit.jsp?page=MyPageName, which is not quite as
 * nice. Eventually, it may be a good idea to use JSPWiki's XML-RPC interface
 * to figure out if the page exists or not.
 * @author David M Johnson
 */
public class RollerPageProvider extends FileSystemProvider
{
    public boolean pageExists(String arg0)
    {
        return true;
    }
}
