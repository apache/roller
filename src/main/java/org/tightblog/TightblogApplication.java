/*
 * Copyright 2018 the original author or authors.
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
package org.tightblog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@PropertySources({
    @PropertySource(value = "classpath:tightblog.properties"),
    @PropertySource(value = "classpath:tightblog-custom.properties", ignoreResourceNotFound = true),
    // optional JVM-property override (i.e., -Dtightblog.custom.config=XXX)
    @PropertySource(value = "file:${tightblog.custom.config}", ignoreResourceNotFound = true)
})
// https://stackoverflow.com/a/32087621
@EnableWebSecurity
// extending SBSI to make deployable as a WAR file
public class TightblogApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(TightblogApplication.class, args);
    }

}
