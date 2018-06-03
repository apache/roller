package org.tightblog.ui.restapi;

import com.fasterxml.jackson.databind.node.TextNode;
import org.springframework.context.MessageSource;
import org.tightblog.business.MediaFileManager;
import org.tightblog.business.UserManager;
import org.tightblog.business.WeblogManager;
import org.tightblog.business.JPAPersistenceStrategy;
import org.tightblog.pojos.MediaDirectory;
import org.tightblog.pojos.MediaFile;
import org.tightblog.pojos.User;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class MediaFileController {

    private static Logger log = LoggerFactory.getLogger(WeblogController.class);

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
    private JPAPersistenceStrategy persistenceStrategy;

    public void setPersistenceStrategy(JPAPersistenceStrategy persistenceStrategy) {
        this.persistenceStrategy = persistenceStrategy;
    }

    @Autowired
    private MessageSource messages;

    @RequestMapping(value = "/tb-ui/authoring/rest/weblog/{id}/mediadirectories", method = RequestMethod.GET)
    public List<MediaDirectory> getMediaDirectories(@PathVariable String id, Principal p, HttpServletResponse response) {
        Weblog weblog = weblogManager.getWeblog(id);
        if (weblog != null && userManager.checkWeblogRole(p.getName(), weblog.getHandle(), WeblogRole.EDIT_DRAFT)) {
            List<MediaDirectory> temp =
                    mediaFileManager.getMediaDirectories(weblogManager.getWeblog(id))
                            .stream()
                            .peek(md -> {
                                md.setMediaFiles(null);
                                md.setWeblog(null);
                            })
                            .sorted(Comparator.comparing(MediaDirectory::getName))
                            .collect(Collectors.toList());
            return temp;
        } else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/mediadirectories/{id}/files", method = RequestMethod.GET)
    public List<MediaFile> getMediaDirectoryContents(@PathVariable String id, Principal p, HttpServletResponse response) {
        MediaDirectory md = mediaFileManager.getMediaDirectory(id);
        boolean permitted = md != null
                && userManager.checkWeblogRole(p.getName(), md.getWeblog().getHandle(), WeblogRole.EDIT_DRAFT);
        if (permitted) {
            return md.getMediaFiles()
                    .stream()
                    .peek(mf -> mf.setCreator(null))
                    .sorted(Comparator.comparing(MediaFile::getName))
                    .collect(Collectors.toList());
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/mediafile/{id}", method = RequestMethod.GET)
    public MediaFile getMediaFile(@PathVariable String id, Principal p, HttpServletResponse response) {
        MediaFile mf = mediaFileManager.getMediaFile(id);
        boolean permitted = mf != null
                && userManager.checkWeblogRole(p.getName(), mf.getDirectory().getWeblog().getHandle(), WeblogRole.POST);
        if (permitted) {
            mf.setCreator(null);
            return mf;
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/mediafiles", method = RequestMethod.POST, consumes = {"multipart/form-data"})
    public ResponseEntity postMediaFile(Principal p, @Valid @RequestPart("mediaFileData") MediaFile mediaFileData,
                                        Locale locale,
                                        @RequestPart(name = "uploadFile", required = false) MultipartFile uploadedFile)
            throws ServletException {

        MediaFile mf = mediaFileManager.getMediaFile(mediaFileData.getId());
        boolean newMediaFile = mf == null;

        // Check user permissions
        User user = userManager.getEnabledUserByUserName(p.getName());

        if (newMediaFile) {
            if (uploadedFile == null) {
                return ResponseEntity.badRequest().body("Upload File must be provided.");
            }

            mf = new MediaFile();
            mf.setCreator(user);

            if (mediaFileData.getDirectory() == null) {
                return ResponseEntity.badRequest().body("Media Directory was not provided.");
            }
            MediaDirectory dir = mediaFileManager.getMediaDirectory(mediaFileData.getDirectory().getId());
            if (dir == null) {
                return ResponseEntity.badRequest().body("Specified media directory could not be found.");
            }
            mf.setDirectory(dir);
        }

        if (user == null || mf.getDirectory() == null
                || !userManager.checkWeblogRole(user, mf.getDirectory().getWeblog(), WeblogRole.POST)) {
            return ResponseEntity.status(403).body(messages.getMessage("error.title.403", null,
                    locale));
        }

        MediaFile fileWithSameName = mf.getDirectory().getMediaFile(mediaFileData.getName());
        if (fileWithSameName != null && !fileWithSameName.getId().equals(mediaFileData.getId())) {
            return ResponseEntity.badRequest().body(messages.getMessage("mediaFile.error.duplicateName",
                    null, locale));
        }

        // update media file with new metadata
        mf.setName(mediaFileData.getName());
        mf.setAltText(mediaFileData.getAltText());
        mf.setTitleText(mediaFileData.getTitleText());
        mf.setAnchor(mediaFileData.getAnchor());
        mf.setNotes(mediaFileData.getNotes());

        Map<String, List<String>> errors = new HashMap<>();

        try {
            // check if uploadedFile provided, update if so
            if (uploadedFile != null) {
                mf.setLength(uploadedFile.getSize());
                mf.setContentType(uploadedFile.getContentType());
                mf.setCreator(user);
            }

            mediaFileManager.storeMediaFile(mf, uploadedFile.getInputStream(), errors);

            if (errors.size() > 0) {
                Map.Entry<String, List<String>> msg = errors.entrySet().iterator().next();
                return ResponseEntity.badRequest().body(messages.getMessage(msg.getKey(), msg.getValue().toArray(),
                        locale));
            }

            return ResponseEntity.ok(mf);
        } catch (IOException e) {
            log.error("Error uploading file {}", mf.getName(), e);
            throw new ServletException(e.getMessage());
        }
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/weblog/{weblogId}/mediadirectories", method = RequestMethod.PUT)
    public ResponseEntity addMediaDirectory(@PathVariable String weblogId, @RequestBody TextNode directoryName,
                                    Principal p, Locale locale, HttpServletResponse response)
            throws ServletException {
        try {
            Weblog weblog = weblogManager.getWeblog(weblogId);
            if (weblog != null && userManager.checkWeblogRole(p.getName(), weblog.getHandle(), WeblogRole.OWNER)) {
                try {
                    MediaDirectory newDir = mediaFileManager.createMediaDirectory(weblog, directoryName.asText().trim());
                    response.setStatus(HttpServletResponse.SC_OK);
                    return ResponseEntity.ok(newDir.getId());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(messages.getMessage(e.getMessage(), null, locale));
                }
            } else {
                return ResponseEntity.status(403).body(messages.getMessage("error.title.403", null, locale));
            }
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/mediadirectory/{id}", method = RequestMethod.DELETE)
    public void deleteMediaDirectory(@PathVariable String id, Principal p, HttpServletResponse response)
            throws ServletException {

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

    @RequestMapping(value = "/tb-ui/authoring/rest/mediafiles/weblog/{weblogId}/todirectory/{directoryId}",
            method = RequestMethod.POST)
    public void moveMediaFiles(@PathVariable String weblogId, @PathVariable String directoryId,
                               @RequestBody List<String> fileIdsToMove,
                               Principal p, HttpServletResponse response)
            throws ServletException {

        try {
            if (fileIdsToMove != null && fileIdsToMove.size() > 0) {
                Weblog weblog = weblogManager.getWeblog(weblogId);
                MediaDirectory targetDirectory = mediaFileManager.getMediaDirectory(directoryId);
                if (weblog != null && targetDirectory != null && weblog.equals(targetDirectory.getWeblog()) &&
                        userManager.checkWeblogRole(p.getName(), weblog.getHandle(), WeblogRole.OWNER)) {

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
