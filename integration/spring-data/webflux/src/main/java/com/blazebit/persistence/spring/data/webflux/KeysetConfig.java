/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.webflux;

import com.blazebit.persistence.spring.data.repository.KeysetPageable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Required configuration for a {@link KeysetPageable} that specifies the keyset element type.
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface KeysetConfig {

    /**
     * The keyset element type. Usually the entity type or the DTO type representing the result element type.
     *
     * @return The keyset element type.
     */
    Class<?> value() default void.class;

    /**
     * Alias for {@link #value()}.
     *
     * @return The keyset element type
     */
    Class<?> keysetClass() default void.class;

    /**
     * The query parameter name for the previous offset parameter.
     *
     * @return The previous offset query parameter name
     * @since 1.3.0
     */
    String previousOffsetName() default "";

    /**
     * The query parameter name for the previous page parameter.
     *
     * @return The previous page query parameter name
     */
    String previousPageName() default "";

    /**
     * The query parameter name for the previous page size parameter.
     *
     * @return The previous page size query parameter name
     */
    String previousPageSizeName() default "";

    /**
     * The query parameter name for the lowest keyset parameter.
     *
     * @return The lowest keyset query parameter name
     */
    String lowestName() default "";

    /**
     * The query parameter name for the highest keyset parameter.
     *
     * @return The highest keyset query parameter name
     */
    String highestName() default "";
}
