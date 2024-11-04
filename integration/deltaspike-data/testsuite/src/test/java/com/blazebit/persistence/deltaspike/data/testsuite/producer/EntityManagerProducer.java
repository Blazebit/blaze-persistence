/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.testsuite.producer;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class EntityManagerProducer {

    private static EntityManagerFactory emf;

    @Produces
    @RequestScoped
    public EntityManager createEm() {
        return emf.createEntityManager();
    }

    public void destroyEm(@Disposes EntityManager em) {
        if (em != null && em.isOpen()) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
    }

    @Produces
    @Singleton
    public EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    public static void setEmf( EntityManagerFactory emf) {
        EntityManagerProducer.emf = emf;
    }
}