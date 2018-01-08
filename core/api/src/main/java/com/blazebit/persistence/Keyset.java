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

package com.blazebit.persistence;

import java.io.Serializable;

/**
 * An interface that represents the key set of a row.
 * Instances of this interface can be used for key set pagination.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface Keyset extends Serializable {

    /**
     * Returns the key set tuple ordered by the respective order by expressions.
     *
     * @return The key set tuple for this keyset
     */
    public Serializable[] getTuple();
}
