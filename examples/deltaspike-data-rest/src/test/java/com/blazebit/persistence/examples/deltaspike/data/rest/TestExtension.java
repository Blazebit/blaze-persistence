/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.deltaspike.data.rest;


import com.blazebit.persistence.examples.deltaspike.data.rest.config.EntityManagerProducer;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

/**
 * In the test, we have to produce the EntityManagerFactory and EntityManager manually.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class TestExtension implements Extension {
    
    <X> void discover(@Observes ProcessAnnotatedType<X> type) {
        if (EntityManagerProducer.class == type.getAnnotatedType().getJavaClass()) {
            type.veto();
        }
    }
}
