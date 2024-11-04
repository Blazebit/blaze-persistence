/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.jaxrs.jackson.testsuite.config;

import org.apache.deltaspike.jpa.api.transaction.TransactionScoped;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@TransactionScoped
public class EntityManagerHolder {

    private EntityManager em;

    @Inject
    private EntityManagerFactory emf;

    @PostConstruct
    public void init() {
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