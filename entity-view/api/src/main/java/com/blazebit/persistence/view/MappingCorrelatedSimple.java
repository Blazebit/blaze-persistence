/*
 * Copyright 2014 - 2017 Blazebit.
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
 * Maps the annotated attribute as correlation attribute with a simple declarative mapping of the correlation.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface MappingCorrelatedSimple {

    /**
     * The entity class which is correlated.
     *
     * @return The correlated entity class
     */
    Class<?> correlated();

    /**
     * The expression which is the basis for correlation.
     *
     * @return The expression
     */
    String correlationBasis();

    /**
     * The alias to use for the correlation key in the correlation expression.
     *
     * @return The expression
     */
    String correlationKeyAlias() default "correlationKey";

    /**
     * The expression to use for correlating the entity type to the correlation basis.
     *
     * @return The expression
     */
    String correlationExpression();

    /**
     * The expression for the result mapping of the correlated entity type.
     *
     * @return The expression
     */
    String correlationResult() default "";

    /**
     * The associations of the entity that should be fetched.
     * This is only valid if the mapping refers to an entity and is mapped as attribute with the original type.
     *
     * @return The JPQL expression mapping
     * @since 1.2.0
     */
    String[] fetches() default {};

    /**
     * The fetch strategy to use for correlation.
     *
     * @return The correlation fetch strategy
     */
    FetchStrategy fetch() default FetchStrategy.SELECT;
}
