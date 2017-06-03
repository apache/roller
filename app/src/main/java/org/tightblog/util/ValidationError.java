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
package org.tightblog.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.ArrayList;
import java.util.List;

/**
 * Based on code from Rafael Borowiec,
 * http://blog.codeleak.pl/2013/09/request-body-validation-in-spring-mvc-3.2.html
 */
public class ValidationError {

    public static ValidationError createValidationError(MethodArgumentNotValidException e) {
        return ValidationError.fromBindingErrors(e.getBindingResult());
    }

    public static ValidationError fromBindingErrors(Errors errors) {
        ValidationError error = new ValidationError(errors.getErrorCount());
        for (ObjectError objectError : errors.getAllErrors()) {
            error.addValidationError(objectError.getDefaultMessage());
        }
        return error;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> errors = new ArrayList<>();

    private final String errorMessage;

    public ValidationError(int errorCount) {
        this.errorMessage = "Validation failed. " + errorCount + " error(s)";
    }

    public ValidationError(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void addValidationError(String error) {
        errors.add(error);
    }

    public List<String> getErrors() {
        return errors;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
