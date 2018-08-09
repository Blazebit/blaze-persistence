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

package com.blazebit.persistence.spring.data.repository;

import com.blazebit.persistence.KeysetPage;
import org.springframework.data.domain.Pageable;

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
     * Returns the offset as int.
     *
     * @return The offset as int
     * @since 1.3.0
     */
    public int getIntOffset();
}
