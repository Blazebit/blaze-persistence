/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.showcase.runner.cdi.transaction;

import org.apache.deltaspike.core.util.metadata.AnnotationInstanceProvider;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.WithAnnotations;
import jakarta.transaction.Transactional;

/**
 * Integrates DeltaSpike's @Transactional feature with jakarta.transaction.Transactional
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class TransactionalExtension implements Extension {

    public <T> void onAnnotatedType(@Observes @WithAnnotations(Transactional.class) ProcessAnnotatedType<T> annotatedType) {
        annotatedType.configureAnnotatedType().add(AnnotationInstanceProvider.of(org.apache.deltaspike.jpa.api.transaction.Transactional.class));
    }

}
