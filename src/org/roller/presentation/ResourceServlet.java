package org.roller.presentation;

import java.io.*;
import java.util.Date;

import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.roller.model.RollerFactory;


/**
 * Resources servlet.  Acts as a gateway to files uploaded by users.
 *
 * Since we keep uploaded resources in a location outside of the webapp
 * context we need a way to serve them up.  This servlet assumes that 
 * resources are stored on a filesystem in the "uploads.dir" directory.
 *
 * @author Allen Gilliland
 *
 * @web.servlet name="ResourcesServlet"
 * @web.servlet-mapping url-pattern="/resources/*"
 */
public class ResourceServlet extends HttpServlet
{   
    private static Log mLogger =
            LogFactory.getFactory().getInstance(ResourceServlet.class);
    
    private String upload_dir = null;
    private ServletContext context = null;
    
    
    /** Initializes the servlet.*/
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        this.context = config.getServletContext();

        try {
            this.upload_dir = RollerFactory.getRoller().getFileManager().getUploadDir();
            mLogger.debug("upload dir is ["+this.upload_dir+"]");
        } catch(Exception e) { mLogger.warn(e); }

    }
    
    /** Destroys the servlet.
     */
    public void destroy() {
        
    }
    
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        
        String context = request.getContextPath();
        String servlet = request.getServletPath();
        String reqURI = request.getRequestURI();
        
        // calculate the path of the requested resource
        // we expect ... /<context>/<servlet>/path/to/resource
        String reqResource = reqURI.substring(servlet.length() + context.length());
        
        // now we can formulate the *real* path to the resource on the filesystem
        String resource_path = this.upload_dir + reqResource;
        File resource = new File(resource_path);

        mLogger.debug("Resource requested ["+reqURI+"]");
        mLogger.debug("Real path is ["+resource.getAbsolutePath()+"]");
        
        // do a quick check to make sure the resource exits, otherwise 404
        if(!resource.exists() || !resource.canRead()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        // does the client already have this file?  if so, then 304
        Date ifModDate = new Date(request.getDateHeader("If-Modified-Since"));
        Date lastMod = new Date(resource.lastModified());
        if(lastMod.compareTo(ifModDate) <= 0) {
            mLogger.debug("Resource unmodified ... sending 304");
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }
        
        // looks like we'll be serving up the file ... lets set some headers
        // set last-modified date so we can do if-modified-since checks
        // set the content type based on whatever is in our web.xml mime defs
        response.addDateHeader("Last-Modified", (new Date()).getTime());
        response.setContentType(this.context.getMimeType(resource.getAbsolutePath()));
        
        // ok, lets serve up the file
        byte[] buf = new byte[8192];
        int length = 0;
        OutputStream out = response.getOutputStream();
        InputStream resource_file = new FileInputStream(resource);
        while((length = resource_file.read(buf)) > 0)
            out.write(buf, 0, length);
        
        // cleanup
        out.close();
        resource_file.close();
    }
    
    
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "ResourceServlet ... serving you since 2005 ;)";
    }
    
}
