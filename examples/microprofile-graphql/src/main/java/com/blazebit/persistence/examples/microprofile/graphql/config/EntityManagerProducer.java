/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.microprofile.graphql.config;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;

/**
 * @author Christian Beikov
 * @since 1.6.2
 */
@ApplicationScoped
public class EntityManagerProducer {

    // inject your entity manager factory
    @Produces
    @PersistenceUnit
    private static EntityManagerFactory entityManagerFactory;
    @Produces
    @PersistenceContext
    private static EntityManager entityManager;

}
