/*
 * Created on Jun 8, 2004
 */
package org.roller.presentation;

import org.roller.RollerException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

/**
 * @author lance.lavandowska
 */
public class MockRollerRequest extends RollerRequest
{
    /**
     * @param req
     * @param ctx
     * @throws RollerException
     */
    public MockRollerRequest(HttpServletRequest req, ServletContext ctx) throws RollerException
    {
        super(req, ctx);
    }

}
