/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package ${package}.sample;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Disposes;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Workaround for WELD-2245 which prevents the use of @ApplicationScoped for EntityManagerFactory directly
 */
@ApplicationScoped
public class EntityManagerFactoryHolder {

    private EntityManagerFactory emf;

    @PostConstruct
    public void init() {
        this.emf = Persistence.createEntityManagerFactory("default", null);
    }

    @PreDestroy
    public void destroy() {
        if (emf.isOpen()) {
            emf.close();
        }
    }

    @Produces
    @RequestScoped
    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void dispose(@Disposes EntityManager entityManager) {
        if (entityManager.isOpen()) {
            entityManager.close();
        }
    }

    @Produces
    public EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }
}
