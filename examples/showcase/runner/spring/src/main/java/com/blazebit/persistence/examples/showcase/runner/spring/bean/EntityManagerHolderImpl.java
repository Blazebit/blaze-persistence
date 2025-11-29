/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.showcase.runner.spring.bean;

import com.blazebit.persistence.examples.showcase.base.bean.EntityManagerHolder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@Component
public class EntityManagerHolderImpl implements EntityManagerHolder {

    @PersistenceContext
    private EntityManager em;

    public EntityManager getEntityManager() {
        return em;
    }

    @PreDestroy
    public void dispose() {
        if (em.isOpen()) {
            em.close();
        }
    }
}
