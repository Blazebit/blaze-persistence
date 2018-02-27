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
