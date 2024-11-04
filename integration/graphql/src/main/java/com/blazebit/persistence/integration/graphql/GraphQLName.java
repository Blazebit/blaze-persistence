/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.graphql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Names a GraphQL field within an entity view, or the GraphQL type for the entity view.
 *
 * Note that depending on the GraphQL runtime, it might be necessary to apply a runtime specific annotation instead,
 * or that it might be necessary to add a dedicated data fetcher for a field.
 *
 * Consult the documentation for the various GraphQL runtime integration for details.
 *
 * @author Christian Beikov
 * @since 1.6.8
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface GraphQLName {

    /**
     * The name of the GraphQL field or type.
     */
    String value();
}
