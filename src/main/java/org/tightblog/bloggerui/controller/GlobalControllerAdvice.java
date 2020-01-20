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
package org.tightblog.bloggerui.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.tightblog.bloggerui.model.ValidationErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.tightblog.bloggerui.model.Violation;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Locale;
import java.util.UUID;

@ControllerAdvice
public class GlobalControllerAdvice {

    private static Logger log = LoggerFactory.getLogger(GlobalControllerAdvice.class);

    @Autowired
    private MessageSource messages;

    // for request bodies: https://reflectoring.io/bean-validation-with-spring-boot/
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ValidationErrorResponse handleException(final MethodArgumentNotValidException e) {
        final ValidationErrorResponse error = new ValidationErrorResponse();
        for (final FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            error.getErrors().add(new Violation(fieldError.getField(), fieldError.getDefaultMessage()));
        }
        return error;
    }

    // for path and query variables:
    // https://reflectoring.io/bean-validation-with-spring-boot/
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ValidationErrorResponse handleException(final ConstraintViolationException e) {
        final ValidationErrorResponse error = new ValidationErrorResponse();
        for (final ConstraintViolation<?> violation : e.getConstraintViolations()) {
            error.getErrors().add(new Violation(violation.getPropertyPath().toString(), violation.getMessage()));
        }
        return error;
    }

    // for system errors
    @ExceptionHandler(value = Exception.class)
    // avoiding use of ResponseStatus as it activates Tomcat HTML page (see
    // ResponseStatus JavaDoc)
    public ResponseEntity<ValidationErrorResponse> handleException(final Exception ex, final Locale locale) {
        final UUID errorUUID = UUID.randomUUID();
        log.error("Internal Server Error (ID: {}) processing REST call", errorUUID, ex);

        final ValidationErrorResponse error = new ValidationErrorResponse();
        error.getErrors().add(new Violation(messages.getMessage(
                "generic.error.check.logs", new Object[] {errorUUID}, locale)));

        return ResponseEntity.status(500).body(error);
    }
}
