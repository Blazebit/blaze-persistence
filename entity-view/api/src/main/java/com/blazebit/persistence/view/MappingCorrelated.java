/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Maps the annotated attribute as correlation attribute.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface MappingCorrelated {

    /**
     * The expression which is the basis for correlation.
     *
     * @return The expression
     */
    String correlationBasis();

    /**
     * The expression for the result mapping of the correlation relative to the correlated alias.
     *
     * @return The expression
     */
    String correlationResult() default "";

    /**
     * The class which provides the correlation provider.
     *
     * @return The correlation provider
     */
    Class<? extends CorrelationProvider> correlator();

    /**
     * The associations of the entity that should be fetched.
     * This is only valid if the mapping refers to an entity and is mapped as attribute with the original type.
     *
     * @return The associations of the entity that should be fetched
     * @since 1.2.0
     */
    String[] fetches() default {};

    /**
     * The fetch strategy to use for correlation.
     *
     * @return The correlation fetch strategy
     */
    FetchStrategy fetch() default FetchStrategy.SELECT;
}
