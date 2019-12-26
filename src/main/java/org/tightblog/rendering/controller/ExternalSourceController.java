package org.tightblog.rendering.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.tightblog.domain.SharedTemplate;
import org.tightblog.domain.Template;
import org.tightblog.rendering.cache.CachedContent;
import org.tightblog.rendering.cache.LazyExpiringCache;
import org.tightblog.rendering.service.ThymeleafRenderer;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@RestController
@RequestMapping(path = ExternalSourceController.PATH)
// Validate constraint annotations on method parameters
@Validated
@ConditionalOnProperty(name = "external.github.enabled", havingValue = "true")
public class ExternalSourceController {

    private static Logger log = LoggerFactory.getLogger(ExternalSourceController.class);

    private LazyExpiringCache githubSourceCache;
    private Set<Pattern> whitelistPatterns = new HashSet<>();
    private ThymeleafRenderer thymeleafRenderer;

    public static final String PATH = "/tb-ui/rendering/external";

    private static final MediaType APP_JAVASCRIPT = new MediaType("application", "javascript");

    public ExternalSourceController(@Qualifier("standardRenderer") ThymeleafRenderer thymeleafRenderer,
                                    @Value("#{'${external.github.whitelist:}'.split(',')}") String githubSourceWhitelist,
                                    LazyExpiringCache githubSourceCache) {

        this.githubSourceCache = githubSourceCache;
        this.thymeleafRenderer = thymeleafRenderer;

        if (!StringUtils.isBlank(githubSourceWhitelist)) {
            for (String patternString : githubSourceWhitelist.trim().split("\\s*,\\s*")) {
                whitelistPatterns.add(Pattern.compile(patternString));
            }
            log.info("GitHub whitelist configured with {} patterns: {}",
                    whitelistPatterns.size(), Arrays.toString(whitelistPatterns.toArray()));
        } else {
            log.info("No whitelist defined for external GitHub source so all GitHub URLs will be allowed");
        }
    }

    @GetMapping(path = "/github/**")
    ResponseEntity<Resource> getGithubSource(HttpServletRequest request,
                                             @RequestParam(value = "start", required = false) Integer startLine,
                                             @RequestParam(value = "end", required = false) Integer endLine,
                                             @RequestParam(value = "height", required = false) Integer height,
                                             @RequestParam(value = "linenums", required = false, defaultValue = "true")
                                                   boolean showLinenums
    ) throws IOException {

        // sample servlet path:
        // /tb-ui/rendering/external/github/gmazza/blog-samples/raw/master/jaxws_handler_tutorial/...
        // client/src/main/java/client/ClientHandlers.java
        String path = request.getServletPath();

        // strip off leading "/"'s if any
        path = path.replaceFirst("/+$", "");

        // resultant desired GitHub URL to retrieve from (if /blob/ instead of /raw/, need to switch to latter):
        // https://www.github.com/gmazza/blog-samples/raw/master/jaxws_handler_tutorial/...
        // client/src/main/java/client/ClientHandlers.java

        // get portion starting from "gmazza"
        int projectPosition = StringUtils.ordinalIndexOf(path, "/", 5);

        if (projectPosition == -1) {
            return ResponseEntity.notFound().build();
        }

        // path2 = gmazza/blog-samples/raw/master/...
        String path2 =  path.substring(projectPosition + 1);

        // Can check and return from cache before whitelist.  Any change in whitelist
        // status for a request occurs only after app reboot, which means cache would be
        // empty anyway for a newly blocked URL.
        String cacheKey = generateKey(path2, startLine, endLine, height, showLinenums);
        CachedContent response = githubSourceCache.get(cacheKey, null);

        if (response == null) {

            // need to check if path2 passes whitelist if latter defined
            if (whitelistPatterns.size() > 0) {
                boolean allowed = false;

                // block attempts to move up a directory to circumvent whitelist
                if (!path2.contains("..")) {
                    for (Pattern pattern : whitelistPatterns) {
                        if (pattern.matcher(path2).matches()) {
                            allowed = true;
                            break;
                        }
                    }
                }
                if (!allowed) {
                    log.info("Requested Github path {} blocked by configured whitelist", path2);
                    return ResponseEntity.notFound().build();
                }
            }

            // change "/blob/" to "/raw/" if necessary
            int rawPositionBefore = StringUtils.ordinalIndexOf(path2, "/", 2);
            int rawPositionAfter = StringUtils.ordinalIndexOf(path2, "/", 3);
            int branchPositionAfter = StringUtils.ordinalIndexOf(path2, "/", 4);
            int lastSlash = path2.lastIndexOf("/");

            if (branchPositionAfter == -1) {
                return ResponseEntity.notFound().build();
            }

            String filePath = path2.substring(branchPositionAfter + 1);
            String fileName = path2.substring(lastSlash + 1);

            String githubRawURL = String.format("https://www.github.com/%s/raw/%s",
                    path2.substring(0, rawPositionBefore), path2.substring(rawPositionAfter + 1));

            String githubBlobURL = String.format("https://www.github.com/%s/blob/%s",
                    path2.substring(0, rawPositionBefore), path2.substring(rawPositionAfter + 1));

            RestTemplate rt = new RestTemplate();
            String sourceCode = rt.getForObject(githubRawURL, String.class, new HashMap<>());

            if (sourceCode == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> model = new HashMap<>();
            if (height != null) {
                model.put("height", height);
            }

            model.put("startLine", 1);
            model.put("showLinenums", showLinenums);
            model.put("rawUrl", githubRawURL);
            model.put("blobUrl", githubBlobURL);
            model.put("filePath", filePath);
            model.put("fileName", fileName);

            // if requested show specific lines only (\n is a line indicator), first line is 1 or 0, second line is 2.
            if (endLine != null && endLine >= 0 && (startLine == null || endLine >= startLine)) {
                int end = StringUtils.ordinalIndexOf(sourceCode, "\n", endLine);
                if (end > -1) {
                    sourceCode = sourceCode.substring(0, end);
                }
            }

            if (startLine != null && startLine > 1 && (endLine == null || startLine <= endLine)) {
                int beginning = StringUtils.ordinalIndexOf(sourceCode, "\n", startLine - 1);
                if (beginning > -1) {
                    sourceCode = sourceCode.substring(beginning);
                    model.put("startLine", startLine);
                }
            }

            model.put("githubSource", sourceCode);

            Template template = new SharedTemplate("github-source", Template.Role.JAVASCRIPT);
            response = thymeleafRenderer.render(template, model);

            String content = new String(response.getContent(), StandardCharsets.UTF_8);

            // removing all "\n": ensure document.write() content on one line (required)
            // replacing with "\\n" : place new lines in output html, suitable for line breaks under <pre> tags.
            content = content.replace("\n", "\\n");

            // important: document.write(...) must be on one line, no matter how long
            content = String.format("document.write('%s');", content);

            response.setContent(content.getBytes(StandardCharsets.UTF_8));

            githubSourceCache.put(cacheKey, response);
        }

        return ResponseEntity.ok()
                .contentType(APP_JAVASCRIPT)
                .contentLength(response.getContent().length)
                .cacheControl(CacheControl.noCache())
                .body(new ByteArrayResource(response.getContent()));
    }

    private String generateKey(String filePath, Integer startLine, Integer endLine, Integer height, boolean showLinenums) {
        StringBuilder key = new StringBuilder();
        key.append(filePath);

        Optional.ofNullable(startLine).ifPresent(s -> key.append("/s=").append(s));
        Optional.ofNullable(endLine).ifPresent(e -> key.append("/e=").append(e));
        Optional.ofNullable(height).ifPresent(h -> key.append("/h=").append(h));
        key.append("/l=").append(showLinenums);
        return key.toString();
    }
}
