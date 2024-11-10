/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.hateoas.webmvc.config;

import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import java.util.Properties;

/**
 * @author Moritz Becker
 * @author Eugen Mayer
 * @since 1.6.9
 */
public class CustomLocalContainerEntityManagerFactoryBean extends LocalContainerEntityManagerFactoryBean {

    public static Properties properties;

    @Override
    protected EntityManagerFactory createNativeEntityManagerFactory() throws PersistenceException {
        setJpaProperties(properties);
        return super.createNativeEntityManagerFactory();
    }
}
