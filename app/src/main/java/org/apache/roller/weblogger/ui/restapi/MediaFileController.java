package org.apache.roller.weblogger.ui.restapi;

import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.jpa.JPAPersistenceStrategy;
import org.apache.roller.weblogger.pojos.MediaDirectory;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.util.ValidationError;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
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
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@RestController
public class MediaFileController {

    private ResourceBundle bundle = ResourceBundle.getBundle("ApplicationResources");

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
                            .peek(md -> {
                                md.setMediaFiles(null);
                                md.setWeblog(null);
                            })
                            .sorted((m1, m2) -> m1.getName().compareTo(m2.getName()))
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

    @RequestMapping(value = "/tb-ui/authoring/rest/mediafile/{id}", method = RequestMethod.GET)
    public MediaFile getMediaFile(@PathVariable String id, Principal p, HttpServletResponse response) {
        MediaFile mf = mediaFileManager.getMediaFile(id);
        boolean permitted = mf != null && userManager.checkWeblogRole(p.getName(), mf.getDirectory().getWeblog().getHandle(), WeblogRole.POST);
        if (permitted) {
            mf.setCreator(null);
            return mf;
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/mediafile/{id}", method = RequestMethod.POST, consumes = {"multipart/form-data"})
    public ResponseEntity updateMediaFile(@PathVariable String id, @Valid @RequestPart("mediaFileData") MediaFile mediaFileData, Principal p,
                                       HttpServletResponse response, @RequestPart(name = "uploadFile", required = false) MultipartFile uploadedFile)
        throws ServletException {

        // does file exist and user have write access to file?
        User user = userManager.getEnabledUserByUserName(p.getName());
        MediaFile mf = mediaFileManager.getMediaFile(id);
        if (user == null || mf == null || !userManager.checkWeblogRole(user, mf.getDirectory().getWeblog(), WeblogRole.POST)) {
            return ResponseEntity.status(404).body(bundle.getString("error.title.404"));
        }

        // update media file with new metadata
        mf.setName(mediaFileData.getName());
        mf.setAltText(mediaFileData.getAltText());
        mf.setTitleText(mediaFileData.getTitleText());
        mf.setAnchor(mediaFileData.getAnchor());
        mf.setNotes(mediaFileData.getNotes());
        mf.setCreator(user);

        // Move file
        /*
            if (!getBean().getDirectoryId().equals(mediaFile.getDirectory().getId())) {
                log.debug("Processing move of {}", mediaFile.getId());
                MediaDirectory targetDirectory = mediaFileManager.getMediaDirectory(getBean().getDirectoryId());
                mediaFileManager.moveMediaFile(mediaFile, targetDirectory);
            }
        */

        try {
            // check if uploadedFile provided, update if so
            if (uploadedFile != null) {
                mf.setInputStream(uploadedFile.getInputStream());
                mf.setLength(uploadedFile.getSize());
                mf.setContentType(uploadedFile.getContentType());
                mediaFileManager.updateMediaFile(mf, mf.getInputStream());
            } else {
                mediaFileManager.updateMediaFile(mf, null);
            }
            persistenceStrategy.flush();
            // addMessage("mediaFile.update.success");
            return ResponseEntity.ok(mf);
        } catch (IOException e) {
            //log.error("Error uploading file {}", bean.getName(), e);
            //addError("mediaFileAdd.errorUploading", bean.getName());
            throw new ServletException(e.getMessage());
        }

    }

    @RequestMapping(value = "/tb-ui/authoring/rest/mediadirectory/{id}/files", method = RequestMethod.POST, consumes = {"multipart/form-data"})
    public ResponseEntity addMediaFile(@PathVariable String id, @Valid @RequestPart("mediaFileData") MediaFile mediaFileData, Principal p,
                                       HttpServletResponse response, @RequestPart("uploadFile") @NotNull @NotBlank MultipartFile uploadedFile)
            throws ServletException {

        // does user have write access to directory?
        User user = userManager.getEnabledUserByUserName(p.getName());
        MediaDirectory dir = mediaFileManager.getMediaDirectory(id);
        if (user == null || dir == null || !userManager.checkWeblogRole(user, dir.getWeblog(), WeblogRole.POST)) {
            return ResponseEntity.status(403).body(bundle.getString("error.title.403"));
        }

        mediaFileData.setDirectory(dir);
        ValidationError maybeError = advancedValidate(mediaFileData);
        if (maybeError != null) {
            return ResponseEntity.badRequest().body(maybeError);
        }

        try {
            MediaFile newMediaFile = new MediaFile();
            newMediaFile.setName(mediaFileData.getName());
            newMediaFile.setAltText(mediaFileData.getAltText());
            newMediaFile.setTitleText(mediaFileData.getTitleText());
            newMediaFile.setAnchor(mediaFileData.getAnchor());
            newMediaFile.setNotes(mediaFileData.getNotes());
            newMediaFile.setDirectory(mediaFileData.getDirectory());
            newMediaFile.setCreator(user);

            newMediaFile.setLength(uploadedFile.getSize());
            newMediaFile.setInputStream(uploadedFile.getInputStream());
            newMediaFile.setContentType(uploadedFile.getContentType());

            Map<String, List<String>> errors = new HashMap<>();
            mediaFileManager.createMediaFile(newMediaFile, errors);
    /*
            for (Map.Entry<String, List<String>> error : errors.entrySet()) {
                addError(error.getKey(), error.getValue());
            }
    */
            persistenceStrategy.flush();
            // below should not be necessary as createMediaFile refreshes the directory's
            // file listing but caching of directory's old file listing occurring somehow.
            newMediaFile.getDirectory().getMediaFiles().add(newMediaFile);

            return ResponseEntity.ok(newMediaFile);
            //addMessage("uploadFiles.uploadedFiles");
            //addMessage("uploadFiles.uploadedFile", mediaFile.getName());
            // this.pageTitle = "mediaFileAddSuccess.title";

        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }

/*
    @RequestMapping(value = "/tb-ui/authoring/rest/mediafile/{id}", method = RequestMethod.POST)
    public ResponseEntity updateMediaFile(@PathVariable String id, @Valid @RequestBody MediaFile newData, Principal p,
            HttpServletResponse response) throws ServletException {

    }

    private ResponseEntity saveMediaFile(MediaFile mediaFile, MediaFile newData, HttpServletResponse response, boolean newMediaFile) throws ServletException {

    }
*/

    private ValidationError advancedValidate(MediaFile data) {
        BindException be = new BindException(data, "new data object");

        MediaFile fileWithSameName = data.getDirectory().getMediaFile(data.getName());
        if (fileWithSameName != null && !fileWithSameName.getId().equals(data.getId())) {
            be.addError(new ObjectError("MediaFile object", bundle.getString("MediaFile.error.duplicateName")));
        }

        return be.getErrorCount() > 0 ? ValidationError.fromBindingErrors(be) : null;
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

/*
            if (!propertiesManager.getBooleanProperty("uploads.enabled")) {
                addError("error.upload.disabled");
            }
            if (uploadedFile == null || !uploadedFile.exists()) {
                addError("error.upload.nofile");
            }
*/


            Weblog weblog = weblogManager.getWeblog(weblogId);
            if (weblog != null && userManager.checkWeblogRole(p.getName(), weblog.getHandle(), WeblogRole.OWNER)) {
                try {
                    MediaDirectory newDir = mediaFileManager.createMediaDirectory(weblog, directoryName.asText().trim());
                    persistenceStrategy.flush();
                    response.setStatus(HttpServletResponse.SC_OK);
                    return newDir.getId();
                } catch (IllegalArgumentException e) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
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
