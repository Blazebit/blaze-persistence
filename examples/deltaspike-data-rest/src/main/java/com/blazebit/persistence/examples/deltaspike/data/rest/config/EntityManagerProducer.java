/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.deltaspike.data.rest.config;

import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnit;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
@jakarta.ejb.Singleton
@jakarta.ejb.Startup
public class EntityManagerProducer {

    // inject your entity manager factory
    @Produces
    @PersistenceUnit
    private static EntityManagerFactory entityManagerFactory;
    @Produces
    @PersistenceContext
    private static EntityManager entityManager;

}
