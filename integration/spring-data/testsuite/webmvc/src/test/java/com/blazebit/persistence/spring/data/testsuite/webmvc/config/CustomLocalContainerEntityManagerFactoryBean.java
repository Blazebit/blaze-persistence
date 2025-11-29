/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.testsuite.webmvc.config;

import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import java.util.Properties;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
public class CustomLocalContainerEntityManagerFactoryBean extends LocalContainerEntityManagerFactoryBean {

    public static Properties properties;

    @Override
    protected EntityManagerFactory createNativeEntityManagerFactory() throws PersistenceException {
        setJpaProperties(properties);
        return super.createNativeEntityManagerFactory();
    }
}
