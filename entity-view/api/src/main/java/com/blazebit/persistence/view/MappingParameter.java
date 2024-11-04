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
 * A mapping to a parameter which is passed into a query when querying an entity view.
 * The {@linkplain MappingParameter} annotation can be applied to abstract getter methods of an interface or abstract class. It may also be applied to constructor parameters.
 *
 * <code>
 * Example 1:
 *
 * public Document(@MappingParameter("queryParam1") Integer queryParam1) { ... }
 *
 * Example 2:
 *
 * public Document(@Mapping("UPPER(name)") String upperName, @MappingParameter("queryParam1") Integer queryParam1) { ... }
 *
 * </code>
 *
 * Example 1 shows a constructor that gets the query parameter "queryParam1" injected in the constructor when querying for an entity view.
 * Example 2 is similar to example 1 but shows that a constructor can also contain parameters annotated {@linkplain Mapping} in conjunction with mapping parameters.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface MappingParameter {

    /**
     * The name of the query parameter the annotated getter or parameter should map to.
     *
     * @return The name of the query parameter
     */
    String value();
}
