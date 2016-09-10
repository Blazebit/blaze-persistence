/*
 * Copyright 2014 Blazebit.
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
 * Maps the annotated attribute as correlation attribute.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface MappingCorrelated {

    /**
     * The expression which is the basis for correlation.
     *
     * @return The expression
     */
    String correlationBasis();

    /**
     * The absolute expression result mapping of the correlation.
     *
     * @return The expression
     */
    String correlationResult();

    /**
     * The class which provides the correlation provider.
     *
     * @return The correlation provider
     */
    Class<? extends CorrelationProvider> correlator();

    /**
     * The strategy to use for correlation.
     *
     * @return The correlation strategy
     */
    CorrelationStrategy strategy() default CorrelationStrategy.JOIN;
}
