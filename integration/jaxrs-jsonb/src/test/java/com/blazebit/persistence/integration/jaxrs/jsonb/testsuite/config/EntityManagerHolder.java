/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.jaxrs.jsonb.testsuite.config;

import org.apache.deltaspike.jpa.api.transaction.TransactionScoped;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * @author Moritz Becker
 * @since 1.6.4
 */
@TransactionScoped
public class EntityManagerHolder {

    private EntityManager em;

    @Inject
    @PostConstruct
    public void init(EntityManagerFactory emf) {
        em = emf.createEntityManager();
    }

    @Produces
    public EntityManager getEntityManager() {
        return em;
    }

    public void dispose(@Disposes EntityManager entityManager) {
        if (entityManager.isOpen()) {
            entityManager.close();
        }
    }
}