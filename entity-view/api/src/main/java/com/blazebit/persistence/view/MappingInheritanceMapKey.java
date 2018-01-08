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
 * Defines the subtype mappings for a map attribute with a subview key.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface MappingInheritanceMapKey {

    /**
     * Specifies that the base type is not considered in the inheritance subtype selection, but only it's subtypes.
     *
     * @return Whether the base type should be considered for inheritance subtype selection
     */
    boolean onlySubtypes() default false;

    /**
     * The subtype mappings of this annotated map attribute's key subview type that should be considered for inheritance.
     *
     * @return The subtype mappings
     */
    MappingInheritanceSubtype[] value();
}
