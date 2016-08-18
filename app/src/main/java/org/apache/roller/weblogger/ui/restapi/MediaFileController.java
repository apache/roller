package org.apache.roller.weblogger.ui.restapi;

import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.jpa.JPAPersistenceStrategy;
import org.apache.roller.weblogger.pojos.MediaDirectory;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class MediaFileController {

    @Autowired
    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    @Autowired
    private UserManager userManager;

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    @Autowired
    private MediaFileManager mediaFileManager;

    public void setMediaFileManager(MediaFileManager mediaFileManager) {
        this.mediaFileManager = mediaFileManager;
    }

    @Autowired
    private JPAPersistenceStrategy persistenceStrategy = null;

    public void setPersistenceStrategy(JPAPersistenceStrategy persistenceStrategy) {
        this.persistenceStrategy = persistenceStrategy;
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/weblog/{id}/mediadirectories", method = RequestMethod.GET)
    public List<MediaDirectory> getMediaDirectories(@PathVariable String id, Principal p, HttpServletResponse response) {
        Weblog weblog = weblogManager.getWeblog(id);
        if (weblog != null && userManager.checkWeblogRole(p.getName(), weblog.getHandle(), WeblogRole.EDIT_DRAFT)) {
            List<MediaDirectory> temp =
            mediaFileManager.getMediaDirectories(weblogManager.getWeblog(id))
                    .stream()
                    .filter(md -> !md.getMediaFiles().isEmpty())
                    .peek(md -> { md.setMediaFiles(null); md.setWeblog(null); })
                    .sorted((m1, m2) -> m1.getName().compareTo(m2.getName()))
                    .collect(Collectors.toList());
            return temp;
        } else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/mediadirectories/{id}/files", method = RequestMethod.GET)
    public List<MediaFile> getMediaFiles(@PathVariable String id, Principal p, HttpServletResponse response) {
        MediaDirectory md = mediaFileManager.getMediaDirectory(id);
        boolean permitted = md != null && userManager.checkWeblogRole(p.getName(), md.getWeblog().getHandle(), WeblogRole.EDIT_DRAFT);
        if (permitted) {
            return md.getMediaFiles()
                    .stream()
                    .peek(mf -> mf.setCreator(null))
                    .sorted((m1, m2) -> m1.getName().compareTo(m2.getName()))
                    .collect(Collectors.toList());
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/weblog/{weblogId}/mediadirectories", method = RequestMethod.PUT)
    public String addMediaDirectory(@PathVariable String weblogId, @RequestBody TextNode directoryName,
                             Principal p, HttpServletResponse response) throws ServletException {
        try {
            // TODO: check validation of directory names work: invalid, already exists, empty.
//            addError("mediaFile.error.view.dirNameEmpty");
//            addError("mediaFile.error.view.dirNameInvalid");
//            addMessage("mediaFile.directoryCreate.success", this.newDirectoryName);
//            addError("mediaFile.directoryCreate.error.exists", this.newDirectoryName);
            Weblog weblog = weblogManager.getWeblog(weblogId);
            if (weblog != null && userManager.checkWeblogRole(p.getName(), weblog.getHandle(), WeblogRole.OWNER)) {
                try {
                    MediaDirectory newDir = mediaFileManager.createMediaDirectory(weblog, directoryName.asText().trim());
                    persistenceStrategy.flush();
                    response.setStatus(HttpServletResponse.SC_OK);
                    return newDir.getId();
                } catch (IllegalArgumentException e) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST,  e.getMessage());
                }
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
        return null;
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/mediadirectory/{id}", method = RequestMethod.DELETE)
    public void deleteMediaDirectory(@PathVariable String id, Principal p, HttpServletResponse response)
            throws ServletException {

        // addMessage("mediaFile.deleteFolder.success");
        try {
            MediaDirectory itemToRemove = mediaFileManager.getMediaDirectory(id);
            if (itemToRemove != null) {
                Weblog weblog = itemToRemove.getWeblog();
                if (userManager.checkWeblogRole(p.getName(), weblog.getHandle(), WeblogRole.OWNER)) {

                    mediaFileManager.removeMediaDirectory(itemToRemove);
                    weblogManager.saveWeblog(weblog);
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                }
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/mediafiles/weblog/{weblogId}", method = RequestMethod.POST)
    public void deleteMediaFiles(@PathVariable String weblogId, @RequestBody List<String> fileIdsToDelete,
                                 Principal p, HttpServletResponse response)
            throws ServletException {
        // addMessage("mediaFile.delete.success");
        try {
            if (fileIdsToDelete != null && fileIdsToDelete.size() > 0) {
                Weblog weblog = weblogManager.getWeblog(weblogId);
                if (weblog != null && userManager.checkWeblogRole(p.getName(), weblog.getHandle(), WeblogRole.OWNER)) {
                    for (String fileId : fileIdsToDelete) {
                        MediaFile mediaFile = mediaFileManager.getMediaFile(fileId);
                        if (mediaFile != null && weblog.equals(mediaFile.getDirectory().getWeblog())) {
                            mediaFileManager.removeMediaFile(weblog, mediaFile);
                        }
                    }
                    weblogManager.saveWeblog(weblog);

                    // flush changes
                    persistenceStrategy.flush();
                    // for some reason need to release to force a refresh of media directory.
                    // TODO: still?
                    persistenceStrategy.release();
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                }
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/mediafiles/weblog/{weblogId}/todirectory/{directoryId}", method = RequestMethod.POST)
    public void moveMediaFiles(@PathVariable String weblogId, @PathVariable String directoryId,
                               @RequestBody List<String> fileIdsToMove,
                               Principal p, HttpServletResponse response)
            throws ServletException {
        // addMessage("mediaFile.move.success");
        // addError("mediaFile.move.errors");
        try {
            if (fileIdsToMove != null && fileIdsToMove.size() > 0) {
                Weblog weblog = weblogManager.getWeblog(weblogId);
                MediaDirectory targetDirectory = mediaFileManager.getMediaDirectory(directoryId);
                if (weblog != null
                        && targetDirectory != null && weblog.equals(targetDirectory.getWeblog())
                        && userManager.checkWeblogRole(p.getName(), weblog.getHandle(), WeblogRole.OWNER)) {

                    for (String fileId : fileIdsToMove) {
                        MediaFile mediaFile = mediaFileManager.getMediaFile(fileId);
                        if (mediaFile != null && weblog.equals(mediaFile.getDirectory().getWeblog())) {
                            mediaFileManager.moveMediaFile(mediaFile, targetDirectory);
                        }
                    }
                    persistenceStrategy.flush();
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                }
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }

}
