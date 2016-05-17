/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.roller.weblogger.ui.struts2.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.dispatcher.multipart.MultiPartRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.util.WebUtils;

import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;

/*
 * Based on code from Rob Winch, http://stackoverflow.com/a/21741353/1207540
 */
public class SpringMultipartParser implements MultiPartRequest {
    private static final Logger LOG = LoggerFactory.getLogger(MultiPartRequest.class);

    private List<String> errors = new ArrayList<String>();

    private MultiValueMap<String, MultipartFile> multipartMap;

    private MultipartHttpServletRequest multipartRequest;

    private MultiValueMap<String, File> multiFileMap = new LinkedMultiValueMap<String, File>();

    public void parse(HttpServletRequest request, String saveDir)
            throws IOException {
        multipartRequest =
                WebUtils.getNativeRequest(request, MultipartHttpServletRequest.class);

        if(multipartRequest == null) {
            LOG.warn("Unable to MultipartHttpServletRequest");
            errors.add("Unable to MultipartHttpServletRequest");
            return;
        }
        multipartMap = multipartRequest.getMultiFileMap();
        for(Entry<String, List<MultipartFile>> fileEntry : multipartMap.entrySet()) {
            String fieldName = fileEntry.getKey();
            for(MultipartFile file : fileEntry.getValue()) {
                File temp = File.createTempFile("upload", ".dat");
                file.transferTo(temp);
                multiFileMap.add(fieldName, temp);
            }
        }
    }

    public Enumeration<String> getFileParameterNames() {
        return Collections.enumeration(multipartMap.keySet());
    }

    public String[] getContentType(String fieldName) {
        List<MultipartFile> files = multipartMap.get(fieldName);
        if(files == null) {
            return null;
        }
        String[] contentTypes = new String[files.size()];
        int i = 0;
        for(MultipartFile file : files) {
            contentTypes[i++] = file.getContentType();
        }
        return contentTypes;
    }

    public File[] getFile(String fieldName) {
        List<File> files = multiFileMap.get(fieldName);
        return files == null ? null : files.toArray(new File[files.size()]);
    }

    public String[] getFileNames(String fieldName) {
        List<MultipartFile> files = multipartMap.get(fieldName);
        if(files == null) {
            return null;
        }
        String[] fileNames = new String[files.size()];
        int i = 0;
        for(MultipartFile file : files) {
            fileNames[i++] = file.getOriginalFilename();
        }
        return fileNames;
    }

    public String[] getFilesystemName(String fieldName) {
        List<File> files = multiFileMap.get(fieldName);
        if(files == null) {
            return null;
        }
        String[] fileNames = new String[files.size()];
        int i = 0;
        for(File file : files) {
            fileNames[i++] = file.getName();
        }
        return fileNames;
    }

    public String getParameter(String name) {
        return multipartRequest.getParameter(name);
    }

    public Enumeration<String> getParameterNames() {
        return multipartRequest.getParameterNames();
    }

    public String[] getParameterValues(String name) {
        return multipartRequest.getParameterValues(name);
    }

    public List getErrors() {
        return errors;
    }

    public void cleanUp() {
        for(List<File> files : multiFileMap.values()) {
            for(File file : files) {
                file.delete();
            }
        }

        // Spring takes care of the original File objects
    }
}
