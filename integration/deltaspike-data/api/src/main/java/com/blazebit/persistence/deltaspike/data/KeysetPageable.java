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

package com.blazebit.persistence.deltaspike.data;

import com.blazebit.persistence.KeysetPage;

/**
 * Like {@link Pageable} but contains keyset information.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface KeysetPageable extends Pageable {

    /**
     * Returns the keyset page information.
     *
     * @return The keyset page
     */
    public KeysetPage getKeysetPage();

    /**
     * Returns the {@linkplain KeysetPageable} that can be used to request the next {@link Page}.
     *
     * @return The {@linkplain KeysetPageable} for the next {@linkplain Page}
     */
    @Override
    public KeysetPageable next();

    /**
     * Returns the {@linkplain KeysetPageable} that can be used to request the previous {@link Page} or null if the current one already is the first one.
     *
     * @return The {@linkplain KeysetPageable} for the previous {@linkplain Page}
     */
    @Override
    public KeysetPageable previous();

    /**
     * Returns the {@linkplain KeysetPageable} that can be used to request the previous {@link Page} or the first if the current one already is the first one.
     *
     * @return The {@linkplain KeysetPageable} for the previous or first {@linkplain Page}
     */
    @Override
    public KeysetPageable previousOrFirst();

    /**
     * Returns the {@linkplain KeysetPageable} that can be used to request the first {@link Page}.
     *
     * @return The {@linkplain KeysetPageable} for the first {@linkplain Page}
     */
    @Override
    public KeysetPageable first();
}
