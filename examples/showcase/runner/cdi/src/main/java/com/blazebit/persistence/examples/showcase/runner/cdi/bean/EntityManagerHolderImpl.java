/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.showcase.runner.cdi.bean;

import com.blazebit.persistence.examples.showcase.base.bean.EntityManagerHolder;
import org.apache.deltaspike.jpa.api.transaction.TransactionScoped;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

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
