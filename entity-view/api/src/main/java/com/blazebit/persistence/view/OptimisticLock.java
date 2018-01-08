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
 * Annotation to specify whether a change of the annotated attribute will cause optimistic locking.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface OptimisticLock {

    /**
     * Whether a change of the annotated attribute should cause an optimistic lock.
     * When <code>true</code>, a change will <b>NOT</b> cause an optimistic lock.
     *
     * @return <code>false</code> if an attribute change should cause optimistic locking, <code>true</code> otherwise
     */
    public boolean exclude() default false;
}
