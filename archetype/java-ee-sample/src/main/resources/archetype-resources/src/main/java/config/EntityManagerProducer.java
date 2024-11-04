/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package ${package}.config;

import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;

@javax.ejb.Singleton
@javax.ejb.Startup
public class EntityManagerProducer {

    // inject your entity manager factory
    @Produces
    @PersistenceUnit
    private static EntityManagerFactory entityManagerFactory;
    @Produces
    @PersistenceContext
    private static EntityManager entityManager;

}
