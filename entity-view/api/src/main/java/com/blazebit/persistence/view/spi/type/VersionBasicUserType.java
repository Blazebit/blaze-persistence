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

package com.blazebit.persistence.view.spi.type;

/**
 * A contract for defining a custom basic type to use with entity views for versions in optimistic concurrency control.
 *
 * @param <X> The type of the user type
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface VersionBasicUserType<X> extends BasicUserType<X> {

    /**
     * Returns the next version value based on the given current version value.
     *
     * @param current The current version value that may be <code>null</code>
     * @return The next value
     */
    public X nextValue(X current);
}
