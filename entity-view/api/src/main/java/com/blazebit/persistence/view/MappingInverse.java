/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines the inverse mapping to use for persisting or updating elements.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface MappingInverse {

    /**
     * The path of the target type by which this attribute is mapped.
     * The default value is to reuse the value of {@link OneToOne#mappedBy()} or {@link OneToMany#mappedBy()} if there is any.
     *
     * @return The mapped by path
     */
    String mappedBy() default "";

    /**
     * The strategy to use for elements that were removed from this relation.
     * Note that inverse mappings automatically have {@link CascadeType#DELETE} activated.
     * When {@link UpdatableMapping#orphanRemoval()} is activated, only the {@link InverseRemoveStrategy#REMOVE} strategy is a valid configuration.
     *
     * @return The remove strategy
     */
    InverseRemoveStrategy removeStrategy() default InverseRemoveStrategy.SET_NULL;
}
