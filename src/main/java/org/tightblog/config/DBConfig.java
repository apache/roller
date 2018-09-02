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
package org.tightblog.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.tightblog.business.WebloggerStaticConfig;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

@Configuration
public class DBConfig extends JpaBaseConfiguration {

    private static Logger log = LoggerFactory.getLogger(DBConfig.class);

    @Autowired
    private DataSource dataSource;

    protected DBConfig(DataSource dataSource, JpaProperties properties,
                                       ObjectProvider<JtaTransactionManager> jtaTransactionManagerProvider,
                                       ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers) {
        super(dataSource, properties, jtaTransactionManagerProvider, transactionManagerCustomizers);
    }

    @Override
    protected AbstractJpaVendorAdapter createJpaVendorAdapter() {
        return new EclipseLinkJpaVendorAdapter();
    }

    @Override
    protected Map<String, Object> getVendorProperties() {

        // Turn off dynamic weaving to disable LTW (Load Time Weaving) lookup in static weaving mode
        return Collections.singletonMap("eclipselink.weaving", "false");
    }

    @Autowired
    @Bean
    // https://docs.spring.io/spring-boot/docs/current/reference/html/howto-data-access.html#howto-use-custom-entity-manager
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean lEMF =  new LocalContainerEntityManagerFactoryBean();
        lEMF.setDataSource(dataSource);
        lEMF.setPersistenceUnitName("TightBlogPU");
        lEMF.setPersistenceXmlLocation("persistence.xml");
        lEMF.setJpaProperties(additionalProperties());
        return lEMF;
    }

    private Properties additionalProperties() {
        Properties emfProps = new Properties();
        Enumeration keys = WebloggerStaticConfig.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            if (key.startsWith("javax.persistence.") ||
                    key.startsWith("eclipselink.") ||
                    key.startsWith("hibernate.")) {
                String value = WebloggerStaticConfig.getProperty(key);
                log.info("{}: {}", key, value);
                emfProps.setProperty(key, value);
            }
        }
        return emfProps;
    }

}
