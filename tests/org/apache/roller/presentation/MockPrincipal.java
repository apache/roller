/*
 * Created on Mar 8, 2004
 */
package org.apache.roller.presentation;

public class MockPrincipal implements java.security.Principal
{
    String mName;
    public MockPrincipal(String name)
    {
        mName = name;
    }
    public String getName()
    {
        return mName;
    }
}