/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.webmvc;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds the value of a URI template variable or a path segment
 * containing the template parameter to the entity view id of the
 * entity view typed parameter that this annotation is placed on.
 * <p>
 * The type of the annotated parameter must be an entity view type.
 *
 * @author Moritz Becker
 * @author Eugen Mayer
 * @since 1.6.9
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface EntityViewId {
    /**
     * Alias for {@link #name}.
     *
     * @return The name of the path variable to bind to.
     */
    @AliasFor("name")
    String value() default "";

    /**
     * The name of the path variable to bind to.
     *
     * @return The name of the path variable to bind to.
     */
    @AliasFor("value")
    String name() default "";
}
