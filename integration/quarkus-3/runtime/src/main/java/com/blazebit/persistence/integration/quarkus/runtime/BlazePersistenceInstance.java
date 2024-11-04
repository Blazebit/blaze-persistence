/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.integration.quarkus.runtime;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Moritz Becker
 * @since 1.6.0
 */
@Target({ TYPE, FIELD, PARAMETER, PACKAGE })
@Retention(RUNTIME)
@Documented
@Qualifier
@Repeatable(BlazePersistenceInstance.List.class)
public @interface BlazePersistenceInstance {

    String DEFAULT = BlazePersistenceInstanceUtil.DEFAULT_BLAZE_PERSISTENCE_NAME;

    String value();

    /**
     * @author Moritz Becker
     * @since 1.6.0
     */
    class BlazePersistenceInstanceLiteral extends AnnotationLiteral<BlazePersistenceInstance> implements BlazePersistenceInstance {

        private String name;

        public BlazePersistenceInstanceLiteral(String name) {
            this.name = name;
        }

        @Override
        public String value() {
            return name;
        }
    }

    /**
     * @author Moritz Becker
     * @since 1.6.0
     */
    @Target(PACKAGE)
    @Retention(RUNTIME)
    @Documented
    @interface List {

        BlazePersistenceInstance[] value();
    }
}
