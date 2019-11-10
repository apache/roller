package org.tightblog.bloggerui.model;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class SuccessResponse {

    public static ResponseEntity<String> textMessage(String message) {
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(message);
    }

}
