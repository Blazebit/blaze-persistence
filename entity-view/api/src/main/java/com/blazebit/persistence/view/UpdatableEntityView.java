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
 * Specifies that the class is an updatable entity view.
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface UpdatableEntityView {

    /**
     * Specifies the flush mode to use for the updatable entity view.
     *
     * @return The flush mode
     * @since 1.2.0
     */
    public FlushMode mode() default FlushMode.LAZY;

    /**
     * The strategy to use for flushing changes to the JPA model.
     *
     * @return The flush strategy
     * @since 1.2.0
     */
    public FlushStrategy strategy() default FlushStrategy.QUERY;

    /**
     * The lock mode to use for the updatable entity view.
     *
     * @return The lock mode
     * @since 1.2.0
     */
    public LockMode lockMode() default LockMode.AUTO;
}
