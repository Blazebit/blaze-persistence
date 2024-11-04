/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.jaxrs.jsonb.testsuite.config;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Workaround for WELD-2245 which prevents the use of @ApplicationScoped for EntityManagerFactory directly.
 *
 * @author Moritz Becker
 * @since 1.6.4
 */
@ApplicationScoped
public class EntityManagerFactoryHolder {

    private EntityManagerFactory emf;

    @PostConstruct
    public void init() {
        this.emf = Persistence.createEntityManagerFactory("TestsuiteBase", null);
    }

    @PreDestroy
    public void destroy() {
        if (emf.isOpen()) {
            emf.close();
        }
    }

    @Produces
    public EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }
}
