/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.showcase.runner.cdi.transaction;

import org.apache.deltaspike.core.util.metadata.AnnotationInstanceProvider;
import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.transaction.Transactional;

/**
 * Integrates DeltaSpike's @Transactional feature with javax.transaction.Transactional
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class JavaxTransactionalExtension implements Extension {

    public <T> void onAnnotatedType(@Observes @WithAnnotations(Transactional.class) ProcessAnnotatedType<T> annotatedType) {
        AnnotatedTypeBuilder<T> builder = new AnnotatedTypeBuilder<T>()
                .readFromType(annotatedType.getAnnotatedType())
                .addToClass(AnnotationInstanceProvider.of(org.apache.deltaspike.jpa.api.transaction.Transactional.class));
        annotatedType.setAnnotatedType(builder.create());
    }

}
