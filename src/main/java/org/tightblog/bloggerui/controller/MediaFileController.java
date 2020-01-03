package org.tightblog.bloggerui.controller;

import com.fasterxml.jackson.databind.node.TextNode;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.tightblog.bloggerui.model.SuccessResponse;
import org.tightblog.bloggerui.model.Violation;
import org.tightblog.service.MediaManager;
import org.tightblog.service.URLService;
import org.tightblog.service.UserManager;
import org.tightblog.service.WeblogManager;
import org.tightblog.domain.MediaDirectory;
import org.tightblog.domain.MediaFile;
import org.tightblog.domain.User;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.tightblog.dao.MediaDirectoryDao;
import org.tightblog.dao.MediaFileDao;
import org.tightblog.dao.UserDao;
import org.tightblog.dao.WeblogDao;
import org.tightblog.bloggerui.model.ValidationErrorResponse;

import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class MediaFileController {
    private static Logger log = LoggerFactory.getLogger(MediaFileController.class);

    private WeblogDao weblogDao;
    private MediaDirectoryDao mediaDirectoryDao;
    private MediaFileDao mediaFileDao;
    private WeblogManager weblogManager;
    private UserManager userManager;
    private UserDao userDao;
    private MediaManager mediaManager;
    private MessageSource messages;
    private URLService urlService;

    @Autowired
    public MediaFileController(WeblogDao weblogDao, MediaDirectoryDao mediaDirectoryDao,
                               MediaFileDao mediaFileDao, WeblogManager weblogManager,
                               UserManager userManager, UserDao userDao, MediaManager mediaManager,
                               URLService urlService, MessageSource messages) {
        this.weblogDao = weblogDao;
        this.mediaDirectoryDao = mediaDirectoryDao;
        this.mediaFileDao = mediaFileDao;
        this.weblogManager = weblogManager;
        this.userManager = userManager;
        this.userDao = userDao;
        this.mediaManager = mediaManager;
        this.urlService = urlService;
        this.messages = messages;
    }

    @GetMapping(value = "/tb-ui/authoring/rest/weblog/{id}/mediadirectories")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.Weblog), #id, 'EDIT_DRAFT')")
    public List<MediaDirectory> getMediaDirectories(@PathVariable String id, Principal p) {
        return mediaDirectoryDao.findByWeblog(weblogDao.getOne(id))
                        .stream()
                        .peek(md -> {
                            md.setMediaFiles(null);
                            md.setWeblog(null);
                        })
                        .sorted(Comparator.comparing(MediaDirectory::getName))
                        .collect(Collectors.toList());
    }

    @GetMapping(value = "/tb-ui/authoring/rest/mediadirectories/{id}/files")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.MediaDirectory), #id, 'EDIT_DRAFT')")
    public List<MediaFile> getMediaDirectoryContents(@PathVariable String id, Principal p) {
        MediaDirectory md = mediaDirectoryDao.getOne(id);
        return md.getMediaFiles()
                .stream()
                .peek(mf -> {
                    mf.setCreator(null);
                    mf.setPermalink(urlService.getMediaFileURL(mf.getDirectory().getWeblog(), mf.getId()));
                    mf.setThumbnailURL(urlService.getMediaFileThumbnailURL(mf.getDirectory().getWeblog(),
                            mf.getId()));
                })
                .sorted(Comparator.comparing(MediaFile::getName))
                .collect(Collectors.toList());
    }

    @GetMapping(value = "/tb-ui/authoring/rest/mediafile/{id}")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.MediaFile), #id, 'POST')")
    public MediaFile getMediaFile(@PathVariable String id, Principal p) {
        MediaFile mf = mediaFileDao.getOne(id);
        mf.setCreator(null);
        mf.setPermalink(urlService.getMediaFileURL(mf.getDirectory().getWeblog(), mf.getId()));
        mf.setThumbnailURL(urlService.getMediaFileThumbnailURL(mf.getDirectory().getWeblog(),
                mf.getId()));
        return mf;
    }

    @PostMapping(value = "/tb-ui/authoring/rest/mediafiles", consumes = {"multipart/form-data"})
    public ResponseEntity postMediaFile(Principal p, @Valid @RequestPart("mediaFileData") MediaFile mediaFileData,
                                        Locale locale,
                                        @RequestPart(name = "uploadFile", required = false) MultipartFile uploadedFile)
            throws IOException {

        MediaFile mf = mediaFileDao.findByIdOrNull(mediaFileData.getId());

        // Check user permissions
        User user = userDao.findEnabledByUserName(p.getName());

        // new media file?
        if (mf == null) {
            if (uploadedFile == null) {
                return ValidationErrorResponse.badRequest("Upload File must be provided.");
            }

            mf = new MediaFile();
            mf.setCreator(user);

            if (mediaFileData.getDirectory() == null) {
                return ValidationErrorResponse.badRequest("Media Directory was not provided.");
            }
            MediaDirectory dir = mediaDirectoryDao.findByIdOrNull(mediaFileData.getDirectory().getId());
            if (dir == null) {
                return ValidationErrorResponse.badRequest("Specified media directory could not be found.");
            }
            mf.setDirectory(dir);
        }

        if (user == null || mf.getDirectory() == null
                || !userManager.checkWeblogRole(user, mf.getDirectory().getWeblog(), WeblogRole.POST)) {
            return ResponseEntity.status(403).contentType(MediaType.TEXT_PLAIN)
                    .body(messages.getMessage("error.title.403", null,
                    locale));
        }

        MediaFile fileWithSameName = mf.getDirectory().getMediaFile(mediaFileData.getName());
        if (fileWithSameName != null && !fileWithSameName.getId().equals(mediaFileData.getId())) {

            return ValidationErrorResponse.badRequest(
                    messages.getMessage("mediaFile.error.duplicateName", null, locale));
        }

        // update media file with new metadata
        mf.setName(mediaFileData.getName());
        mf.setAltText(mediaFileData.getAltText());
        mf.setTitleText(mediaFileData.getTitleText());
        mf.setAnchor(mediaFileData.getAnchor());
        mf.setNotes(mediaFileData.getNotes());

        Map<String, List<String>> errors = new HashMap<>();

        try {
            mediaManager.saveMediaFile(mf, uploadedFile, user, errors);

            if (errors.size() > 0) {
                List<Violation> violations = new ArrayList<>();
                for (Map.Entry<String, List<String>> msg : errors.entrySet()) {
                    violations.add(new Violation(messages.getMessage(msg.getKey(),
                            msg.getValue() == null ? new String[0] : msg.getValue().toArray(), locale)));
                }
                return ValidationErrorResponse.badRequest(violations);
            }

            return ResponseEntity.ok(mf);
        } catch (IOException e) {
            log.error("Error uploading file {} from user {}", mf.getName(), user.getUserName(), e);
            throw e;
        }
    }

    @PutMapping(value = "/tb-ui/authoring/rest/weblog/{weblogId}/mediadirectories")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.Weblog), #weblogId, 'OWNER')")
    public ResponseEntity addMediaDirectory(@PathVariable String weblogId, @RequestBody TextNode directoryName,
                                    Principal p, Locale locale) {
        try {
            Weblog weblog = weblogDao.getOne(weblogId);
            MediaDirectory newDir = mediaManager.createMediaDirectory(weblog, directoryName.asText().trim());
            return SuccessResponse.textMessage(newDir.getId());
        } catch (IllegalArgumentException e) {
            return ValidationErrorResponse.badRequest(messages.getMessage(e.getMessage(), null, locale));
        }
    }

    @DeleteMapping(value = "/tb-ui/authoring/rest/mediadirectory/{id}")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.MediaDirectory), #id, 'OWNER')")
    public ResponseEntity deleteMediaDirectory(@PathVariable String id, Principal p, Locale locale) {

        MediaDirectory itemToRemove = mediaDirectoryDao.getOne(id);
        Weblog weblog = itemToRemove.getWeblog();
        mediaManager.removeAllFiles(itemToRemove);
        weblog.getMediaDirectories().remove(itemToRemove);
        weblogManager.saveWeblog(weblog, false);

        return SuccessResponse.textMessage(messages.getMessage("mediaFileView.deleteFolder.success",
                null, locale));
   }

    @PostMapping(value = "/tb-ui/authoring/rest/mediafiles/weblog/{weblogId}")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.Weblog), #weblogId, 'OWNER')")
    public ResponseEntity deleteMediaFiles(@PathVariable String weblogId, @RequestBody List<String> fileIdsToDelete,
                                 Principal p, Locale locale) {

        if (fileIdsToDelete != null && fileIdsToDelete.size() > 0) {
            Weblog weblog = weblogDao.getOne(weblogId);
            for (String fileId : fileIdsToDelete) {
                MediaFile mediaFile = mediaFileDao.findByIdOrNull(fileId);
                if (mediaFile != null && weblog.equals(mediaFile.getDirectory().getWeblog())) {
                    mediaManager.removeMediaFile(weblog, mediaFile);
                }
            }
            // setting false as even with refresh any blog articles using deleted images will have broken images
            weblogManager.saveWeblog(weblog, false);
            return SuccessResponse.textMessage(messages.getMessage("mediaFileView.delete.success",
                    null, locale));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping(value = "/tb-ui/authoring/rest/mediafiles/weblog/{weblogId}/todirectory/{directoryId}")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.Weblog), #weblogId, 'OWNER')")
    public ResponseEntity moveMediaFiles(@PathVariable String weblogId, @PathVariable String directoryId,
                               @RequestBody List<String> fileIdsToMove, Principal p, Locale locale) {

        if (fileIdsToMove != null && fileIdsToMove.size() > 0) {
            Weblog weblog = weblogDao.getOne(weblogId);
            MediaDirectory targetDirectory = mediaDirectoryDao.findByIdOrNull(directoryId);
            if (weblog != null && targetDirectory != null && weblog.equals(targetDirectory.getWeblog())) {
                for (String fileId : fileIdsToMove) {
                    MediaFile mediaFile = mediaFileDao.findByIdOrNull(fileId);
                    if (mediaFile != null && weblog.equals(mediaFile.getDirectory().getWeblog())) {
                        mediaManager.moveMediaFiles(Collections.singletonList(mediaFile), targetDirectory);
                    }
                }
                return SuccessResponse.textMessage(messages.getMessage("mediaFileView.move.success",
                        null, locale));
            }
        }
        return ResponseEntity.notFound().build();
    }

}
