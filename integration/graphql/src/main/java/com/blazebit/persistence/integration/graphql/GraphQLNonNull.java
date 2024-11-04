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
 * If present on a method of an entity view class, will cause that the GraphQL type of the GraphQL field will be
 * non-null.
 *
 * Note that depending on the GraphQL runtime, it might be necessary to apply a runtime specific annotation instead.
 *
 * Consult the documentation for the various GraphQL runtime integration for details.
 *
 * @author Christian Beikov
 * @since 1.6.8
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE_PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface GraphQLNonNull {
}
