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
package org.tightblog.bloggerui.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// https://reflectoring.io/bean-validation-with-spring-boot/
public class ValidationErrorResponse {

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Violation> errors;

    public List<Violation> getErrors() {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        return errors;
    }

    public ValidationErrorResponse() {
    }

    public ValidationErrorResponse(List<Violation> errors) {
        for (Violation error : errors) {
            getErrors().add(error);
        }
    }

    public static ResponseEntity<ValidationErrorResponse> badRequest(String errorMessage) {
        return badRequest(new Violation(errorMessage));
    }

    public static ResponseEntity<ValidationErrorResponse> badRequest(Violation error) {
        return badRequest(Collections.singletonList(error));
    }

    public static ResponseEntity<ValidationErrorResponse> badRequest(List<Violation> errors) {
        return ResponseEntity.badRequest().body(new ValidationErrorResponse(errors));
    }
}
