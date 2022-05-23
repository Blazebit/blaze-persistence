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

package com.blazebit.persistence.view;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies whether an empty flat view i.e. a view without an id where all members are null should be created or if <code>null</code> should be assigned.
 *
 * By default, empty flat views are created for singular attributes and can be disabled with this annotation.
 * When annotating a singular attribute with <code>&#064;EmptyFlatViewCreation(false)</code> i.e. disable the empty flat view creation,
 * <code>null</code> will be assigned to that attribute instead of an empty flat view.
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface EmptyFlatViewCreation {
    /**
     * Returns whether to create an empty flat view for the annotated singular attribute.
     *
     * @return whether to create an empty flat view for the annotated singular attribute
     */
    boolean value();
}
