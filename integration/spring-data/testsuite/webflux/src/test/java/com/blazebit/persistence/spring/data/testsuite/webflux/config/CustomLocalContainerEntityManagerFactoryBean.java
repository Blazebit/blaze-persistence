/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.testsuite.webflux.config;

import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import java.util.Properties;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class CustomLocalContainerEntityManagerFactoryBean extends LocalContainerEntityManagerFactoryBean {

    public static Properties properties;

    @Override
    protected EntityManagerFactory createNativeEntityManagerFactory() throws PersistenceException {
        setJpaProperties(properties);
        return super.createNativeEntityManagerFactory();
    }
}
