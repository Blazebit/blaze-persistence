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

package com.blazebit.persistence.criteria;

import javax.persistence.criteria.Root;

/**
 * An extended version of {@link Root}.
 *
 * @param <X> the entity type referenced by the root
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface BlazeRoot<X> extends Root<X>, BlazeFrom<X, X> {

    /**
     * Treats this from object as the given subtype. This will not cause a separate join but return a wrapper,
     * that can be used for further joins.
     *
     * @param type type to be downcast to
     * @param <T>  The target treat type
     * @return The treated from object
     */
    <T extends X> BlazeRoot<T> treatAs(Class<T> type);
}
