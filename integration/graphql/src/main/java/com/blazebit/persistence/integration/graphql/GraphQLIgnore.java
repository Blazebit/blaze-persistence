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
 * If present on an entity view class or one of its methods, will cause that the GraphQL type or GraphQL field will be
 * ignored and hence will not be part of the GraphQL schema.
 *
 * Note that depending on the GraphQL runtime, it might be necessary to apply a runtime specific annotation instead.
 *
 * Consult the documentation for the various GraphQL runtime integration for details.
 *
 * @author Christian Beikov
 * @since 1.6.8
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface GraphQLIgnore {
}
