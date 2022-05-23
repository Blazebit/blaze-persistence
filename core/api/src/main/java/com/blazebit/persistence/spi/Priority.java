/*
 * Copyright 2014 - 2022 Blazebit.
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

package com.blazebit.persistence.spi;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The Priority annotation can be applied to classes to indicate in what order the {@link CriteriaBuilderConfigurationContributor}
 * should be registered.
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.6.2
 */
@Target(value = TYPE)
@Retention(value = RUNTIME)
@Documented
public @interface Priority {

    /**
     * The priority value. The range 0-500 is reserved for internal uses. 500 - 1000 is reserved for libraries and 1000+
     * is for user provided contributors.
     * @return The priority value.
     */
    int value();

}
