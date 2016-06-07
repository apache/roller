package org.apache.roller.weblogger.ui.restapi;

import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.pojos.MediaDirectory;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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

    @RequestMapping(value = "/tb-ui/authoring/rest/weblog/{handle}/mediadirectories", method = RequestMethod.GET)
    public List<MediaDirectory> getMediaDirectories(@PathVariable String handle, Principal p, HttpServletResponse response) {
        if (userManager.checkWeblogRole(p.getName(), handle, WeblogRole.EDIT_DRAFT)) {
            List<MediaDirectory> temp =
            mediaFileManager.getMediaDirectories(weblogManager.getWeblogByHandle(handle))
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
                    .peek(mf -> mf.setCreatorId(null))
                    .sorted((m1, m2) -> m1.getName().compareTo(m2.getName()))
                    .collect(Collectors.toList());
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
    }

}
