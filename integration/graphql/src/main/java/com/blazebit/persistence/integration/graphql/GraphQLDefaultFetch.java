/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.graphql;

import com.blazebit.persistence.view.EntityViewSetting;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Forces the annotated entity view attribute method into the list of {@link EntityViewSetting#getFetches()},
 * based on the configured GraphQL selection set conditions, when created through {@link GraphQLEntityViewSupport}.
 * Can be used to always fetch an attribute or fetch an attribute based on the presence of a GraphQL field in the
 * selection set.
 *
 * @author Christian Beikov
 * @since 1.6.15
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(GraphQLDefaultFetches.class)
public @interface GraphQLDefaultFetch {
    /**
     * Specifies the GraphQL field name that has to be present in the selection set to enable default fetching
     * of the annotated entity view attribute.
     *
     * @return The GraphQL field name based on which the fetch is activated
     */
    String ifFieldSelected() default "";
}
