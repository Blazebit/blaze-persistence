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
 * Maps the annotated attribute as correlation attribute with a simple declarative mapping of the correlation.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface MappingCorrelatedSimple {

    /**
     * The entity class which is correlated.
     *
     * @return The correlated entity class
     */
    Class<?> correlated();

    /**
     * The expression which is the basis for correlation.
     *
     * @return The expression
     */
    String correlationBasis();

    /**
     * The alias to use for the correlation key in the correlation expression.
     *
     * @return The expression
     */
    String correlationKeyAlias() default "correlationKey";

    /**
     * The expression to use for correlating the entity type to the correlation basis.
     *
     * @return The expression
     */
    String correlationExpression();

    /**
     * The expression for the result mapping of the correlated entity type.
     *
     * @return The expression
     */
    String correlationResult() default "";

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
