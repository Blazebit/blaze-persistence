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

import javax.persistence.criteria.Fetch;

/**
 * An extended version of {@link Fetch}.
 *
 * @param <Z> The source type of the fetch
 * @param <X> The target type of the fetch
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface BlazeFetch<Z, X> extends Fetch<Z, X>, BlazeFetchParent<Z, X> {

    /* Covariant overrides */

    /**
     * Like {@link Fetch#getParent()} but returns the subtype {@link BlazeFetchParent} instead.
     *
     * @return fetch parent
     */
    BlazeFetchParent<?, Z> getParent();

}
