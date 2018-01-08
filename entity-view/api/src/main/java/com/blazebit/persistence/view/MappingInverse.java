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

import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines the inverse mapping to use for persisting or updating elements.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface MappingInverse {

    /**
     * The path of the target type by which this attribute is mapped.
     * The default value is to reuse the value of {@link OneToOne#mappedBy()} or {@link OneToMany#mappedBy()} if there is any.
     *
     * @return The mapped by path
     */
    String mappedBy() default "";

    /**
     * The strategy to use for elements that were removed from this relation.
     * Note that inverse mappings automatically have {@link CascadeType#DELETE} activated.
     * When {@link UpdatableMapping#orphanRemoval()} is activated, only the {@link InverseRemoveStrategy#REMOVE} strategy is a valid configuration.
     *
     * @return The remove strategy
     */
    InverseRemoveStrategy removeStrategy() default InverseRemoveStrategy.SET_NULL;
}
