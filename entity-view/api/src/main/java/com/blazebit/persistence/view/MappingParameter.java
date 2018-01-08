/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface MappingParameter {

    /**
     * The name of the query parameter the annotated getter or parameter should map to.
     *
     * @return The name of the query parameter
     */
    String value();
}
