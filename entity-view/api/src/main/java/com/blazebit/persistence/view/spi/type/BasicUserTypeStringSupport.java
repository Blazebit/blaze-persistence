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

package com.blazebit.persistence.view.spi.type;

/**
 * A contract for defining string support for a custom basic type to use with entity views.
 *
 * @param <X> The type of the user type
 * @author Christian Beikov
 * @since 1.5.0
 */
public interface BasicUserTypeStringSupport<X> {

    /**
     * Creates an instance of the type from the given string representation.
     *
     * @param sequence A string representation of the object
     * @return The object
     * @since 1.5.0
     */
    public X fromString(CharSequence sequence);

    /**
     * Wraps the given JPQL.Next expression such that it is converted to the internal string representation that can be read by {@link #fromString(CharSequence)}.
     *
     * @param expression The JPQL.Next expression string
     * @return The object
     * @since 1.5.0
     */
    public String toStringExpression(String expression);
}
