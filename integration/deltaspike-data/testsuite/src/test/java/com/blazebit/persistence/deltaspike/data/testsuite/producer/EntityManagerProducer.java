/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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