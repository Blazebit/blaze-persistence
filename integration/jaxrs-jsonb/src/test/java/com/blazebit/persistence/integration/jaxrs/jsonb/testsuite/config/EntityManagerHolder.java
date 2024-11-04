/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.jaxrs.jsonb.testsuite.config;

import org.apache.deltaspike.jpa.api.transaction.TransactionScoped;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

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