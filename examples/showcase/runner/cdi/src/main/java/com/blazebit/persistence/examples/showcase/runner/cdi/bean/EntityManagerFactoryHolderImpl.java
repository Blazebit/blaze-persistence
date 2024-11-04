/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.showcase.runner.cdi.bean;

import com.blazebit.persistence.examples.showcase.base.bean.EntityManagerFactoryHolder;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Workaround for WELD-2245 which prevents the use of @ApplicationScoped for EntityManagerFactory directly
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@ApplicationScoped
public class EntityManagerFactoryHolderImpl implements EntityManagerFactoryHolder {

    private EntityManagerFactory emf;

    @PostConstruct
    public void init() {
        this.emf = Persistence.createEntityManagerFactory("TEST-PU", null);
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
