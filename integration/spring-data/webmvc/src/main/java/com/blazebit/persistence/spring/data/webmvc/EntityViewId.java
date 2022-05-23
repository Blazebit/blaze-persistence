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

package com.blazebit.persistence.spring.data.webmvc;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds the value of a URI template variable or a path segment
 * containing the template parameter to the entity view id of the
 * entity view typed parameter that this annotation is placed on.
 * <p>
 * The type of the annotated parameter must be an entity view type.
 *
 * @author Moritz Becker
 * @since 1.5.0
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface EntityViewId {
    /**
     * Alias for {@link #name}.
     *
     * @return The name of the path variable to bind to.
     */
    @AliasFor("name")
    String value() default "";

    /**
     * The name of the path variable to bind to.
     *
     * @return The name of the path variable to bind to.
     */
    @AliasFor("value")
    String name() default "";
}
