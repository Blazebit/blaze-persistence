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
 * A mapping to a JPQL expression which contains references to fields of the entity.
 * The {@linkplain Mapping} annotation can be applied to abstract getter methods of an interface or abstract class. It may also be applied to constructor parameters.
 *
 * The use of the {@linkplain Mapping} annotation is optional for getter methods. If the {@linkplain Mapping} annotation is not
 * specified for such a getter, the default values of the {@linkplain Mapping} annotation will apply. This means that getter
 * methods are automatically mapped to their attribute names.
 *
 * <code>
 * Example 1:
 *
 * public String getName();
 *
 * Example 2:
 *
 * {@literal @}Mapping("UPPER(name)")
 * public String getUpperName();
 *
 * </code>
 *
 * Example 1 shows a getter which is implicitly mapped to the attribute name which in this case is "name".
 * Example 2 shows that a mapping can contain arbitrary JPQL expressions.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface Mapping {

    /**
     * The JPQL expression mapping the annotated getter or parameter should map to.
     *
     * @return The JPQL expression mapping
     */
    String value() default "";

    /**
     * The associations of the entity that should be fetched.
     * This is only valid if the mapping refers to an entity and is mapped as attribute with the original type.
     *
     * @return The JPQL expression mapping
     * @since 1.2.0
     */
    String[] fetches() default {};

    /**
     * The fetch strategy to use for the attribute.
     *
     * @return The fetch strategy
     */
    FetchStrategy fetch() default FetchStrategy.JOIN;
}
