package org.tightblog.ui.restapi;

import com.fasterxml.jackson.databind.node.TextNode;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
import org.tightblog.repository.MediaDirectoryRepository;
import org.tightblog.repository.MediaFileRepository;
import org.tightblog.repository.UserRepository;
import org.tightblog.repository.WeblogRepository;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;
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


    private WeblogRepository weblogRepository;
    private MediaDirectoryRepository mediaDirectoryRepository;
    private MediaFileRepository mediaFileRepository;
    private WeblogManager weblogManager;
    private UserManager userManager;
    private UserRepository userRepository;
    private MediaManager mediaManager;
    private MessageSource messages;
    private URLService urlService;

    @Autowired
    public MediaFileController(WeblogRepository weblogRepository, MediaDirectoryRepository mediaDirectoryRepository,
                               MediaFileRepository mediaFileRepository, WeblogManager weblogManager,
                               UserManager userManager, UserRepository userRepository, MediaManager mediaManager,
                               URLService urlService, MessageSource messages) {
        this.weblogRepository = weblogRepository;
        this.mediaDirectoryRepository = mediaDirectoryRepository;
        this.mediaFileRepository = mediaFileRepository;
        this.weblogManager = weblogManager;
        this.userManager = userManager;
        this.userRepository = userRepository;
        this.mediaManager = mediaManager;
        this.urlService = urlService;
        this.messages = messages;
    }

    @GetMapping(value = "/tb-ui/authoring/rest/weblog/{id}/mediadirectories")
    public List<MediaDirectory> getMediaDirectories(@PathVariable String id, Principal p, HttpServletResponse response) {
        Weblog weblog = weblogRepository.findById(id).orElse(null);
        if (weblog != null && userManager.checkWeblogRole(p.getName(), weblog, WeblogRole.EDIT_DRAFT)) {
            List<MediaDirectory> temp =
                    mediaDirectoryRepository.findByWeblog(weblogRepository.findById(id).orElse(null))
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

    @GetMapping(value = "/tb-ui/authoring/rest/mediadirectories/{id}/files")
    public List<MediaFile> getMediaDirectoryContents(@PathVariable String id, Principal p, HttpServletResponse response) {
        MediaDirectory md = mediaDirectoryRepository.findByIdOrNull(id);
        boolean permitted = md != null
                && userManager.checkWeblogRole(p.getName(), md.getWeblog(), WeblogRole.EDIT_DRAFT);
        if (permitted) {
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
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
    }

    @GetMapping(value = "/tb-ui/authoring/rest/mediafile/{id}")
    public MediaFile getMediaFile(@PathVariable String id, Principal p, HttpServletResponse response) {
        MediaFile mf = mediaFileRepository.findByIdOrNull(id);
        boolean permitted = mf != null
                && userManager.checkWeblogRole(p.getName(), mf.getDirectory().getWeblog(), WeblogRole.POST);
        if (permitted) {
            mf.setCreator(null);
            mf.setPermalink(urlService.getMediaFileURL(mf.getDirectory().getWeblog(), mf.getId()));
            mf.setThumbnailURL(urlService.getMediaFileThumbnailURL(mf.getDirectory().getWeblog(),
                    mf.getId()));
            return mf;
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
    }

    @PostMapping(value = "/tb-ui/authoring/rest/mediafiles", consumes = {"multipart/form-data"})
    public ResponseEntity postMediaFile(Principal p, @Valid @RequestPart("mediaFileData") MediaFile mediaFileData,
                                        Locale locale,
                                        @RequestPart(name = "uploadFile", required = false) MultipartFile uploadedFile)
            throws ServletException {

        MediaFile mf = mediaFileRepository.findByIdOrNull(mediaFileData.getId());
        boolean newMediaFile = mf == null;

        // Check user permissions
        User user = userRepository.findEnabledByUserName(p.getName());

        if (newMediaFile) {
            if (uploadedFile == null) {
                return ResponseEntity.badRequest().body("Upload File must be provided.");
            }

            mf = new MediaFile();
            mf.setCreator(user);

            if (mediaFileData.getDirectory() == null) {
                return ResponseEntity.badRequest().body("Media Directory was not provided.");
            }
            MediaDirectory dir = mediaDirectoryRepository.findByIdOrNull(mediaFileData.getDirectory().getId());
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

            mediaManager.saveMediaFile(mf, uploadedFile == null ? null : uploadedFile.getInputStream(), errors);

            if (errors.size() > 0) {
                Map.Entry<String, List<String>> msg = errors.entrySet().iterator().next();
                return ResponseEntity.badRequest().body(messages.getMessage(msg.getKey(), msg.getValue().toArray(),
                        locale));
            }

            return ResponseEntity.ok(mf);
        } catch (IOException e) {
            log.error("Error uploading file {} from user {}", mf.getName(), user.getUserName(), e);
            throw new ServletException(e.getMessage());
        }
    }

    @PutMapping(value = "/tb-ui/authoring/rest/weblog/{weblogId}/mediadirectories", produces = "text/plain")
    public ResponseEntity addMediaDirectory(@PathVariable String weblogId, @RequestBody TextNode directoryName,
                                    Principal p, Locale locale, HttpServletResponse response)
            throws ServletException {
        try {
            Weblog weblog = weblogRepository.findById(weblogId).orElse(null);
            if (weblog != null && userManager.checkWeblogRole(p.getName(), weblog, WeblogRole.OWNER)) {
                try {
                    MediaDirectory newDir = mediaManager.createMediaDirectory(weblog, directoryName.asText().trim());
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

    @DeleteMapping(value = "/tb-ui/authoring/rest/mediadirectory/{id}")
    public void deleteMediaDirectory(@PathVariable String id, Principal p, HttpServletResponse response)
            throws ServletException {

        try {
            MediaDirectory itemToRemove = mediaDirectoryRepository.findByIdOrNull(id);
            if (itemToRemove != null) {
                Weblog weblog = itemToRemove.getWeblog();
                if (userManager.checkWeblogRole(p.getName(), weblog, WeblogRole.OWNER)) {
                    mediaManager.removeAllFiles(itemToRemove);
                    weblog.getMediaDirectories().remove(itemToRemove);
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

    @PostMapping(value = "/tb-ui/authoring/rest/mediafiles/weblog/{weblogId}")
    public void deleteMediaFiles(@PathVariable String weblogId, @RequestBody List<String> fileIdsToDelete,
                                 Principal p, HttpServletResponse response)
            throws ServletException {

        try {
            if (fileIdsToDelete != null && fileIdsToDelete.size() > 0) {
                Weblog weblog = weblogRepository.findById(weblogId).orElse(null);
                if (weblog != null && userManager.checkWeblogRole(p.getName(), weblog, WeblogRole.OWNER)) {
                    for (String fileId : fileIdsToDelete) {
                        MediaFile mediaFile = mediaFileRepository.findByIdOrNull(fileId);
                        if (mediaFile != null && weblog.equals(mediaFile.getDirectory().getWeblog())) {
                            mediaManager.removeMediaFile(weblog, mediaFile);
                        }
                    }
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

    @PostMapping(value = "/tb-ui/authoring/rest/mediafiles/weblog/{weblogId}/todirectory/{directoryId}")
    public void moveMediaFiles(@PathVariable String weblogId, @PathVariable String directoryId,
                               @RequestBody List<String> fileIdsToMove,
                               Principal p, HttpServletResponse response)
            throws ServletException {

        try {
            if (fileIdsToMove != null && fileIdsToMove.size() > 0) {
                Weblog weblog = weblogRepository.findById(weblogId).orElse(null);
                MediaDirectory targetDirectory = mediaDirectoryRepository.findByIdOrNull(directoryId);
                if (weblog != null && targetDirectory != null && weblog.equals(targetDirectory.getWeblog()) &&
                        userManager.checkWeblogRole(p.getName(), weblog, WeblogRole.OWNER)) {

                    for (String fileId : fileIdsToMove) {
                        MediaFile mediaFile = mediaFileRepository.findByIdOrNull(fileId);
                        if (mediaFile != null && weblog.equals(mediaFile.getDirectory().getWeblog())) {
                            mediaManager.moveMediaFiles(Collections.singletonList(mediaFile), targetDirectory);
                        }
                    }
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
