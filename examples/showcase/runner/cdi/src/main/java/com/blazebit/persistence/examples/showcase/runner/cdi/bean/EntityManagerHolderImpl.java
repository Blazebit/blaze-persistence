/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.showcase.runner.cdi.bean;

import com.blazebit.persistence.examples.showcase.base.bean.EntityManagerHolder;
import org.apache.deltaspike.jpa.api.transaction.TransactionScoped;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

/**
 * Workaround for WELD-2245 which prevents the use of @TransactionScoped for EntityManager directly
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
@TransactionScoped
public class EntityManagerHolderImpl implements EntityManagerHolder {

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
