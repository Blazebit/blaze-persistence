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
 * If {@link FetchStrategy#SELECT} is used on a property, this annotation configures the default batching.
 * Beware that if multiple properties of an entity view use {@link FetchStrategy#SELECT},
 * they will only be loaded together in one batch if the batch sizes match.
 *
 * The batch fetch annotation can also be applied on the entity view class level which makes it's value the default for all properties.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface BatchFetch {

    /**
     * The size of the batch.
     *
     * @return The batch size
     */
    int size();
}
