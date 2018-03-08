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

package com.blazebit.persistence.impl.keyset;

import java.io.Serializable;

import com.blazebit.persistence.Keyset;
import com.blazebit.persistence.KeysetPage;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class KeysetPageImpl implements KeysetPage {

    private static final long serialVersionUID = 1L;

    private final int firstResult;
    private final int maxResults;
    private final Keyset lowest;
    private final Keyset highest;

    public KeysetPageImpl(int firstResult, int maxResults, Serializable[] lowest, Serializable[] highest) {
        this(firstResult, maxResults, new KeysetImpl(lowest), new KeysetImpl(highest));
    }

    public KeysetPageImpl(int firstResult, int maxResults, Keyset lowest, Keyset highest) {
        this.firstResult = firstResult;
        this.maxResults = maxResults;
        this.lowest = lowest;
        this.highest = highest;
    }

    @Override
    public int getFirstResult() {
        return firstResult;
    }

    @Override
    public int getMaxResults() {
        return maxResults;
    }

    @Override
    public Keyset getLowest() {
        return lowest;
    }

    @Override
    public Keyset getHighest() {
        return highest;
    }
}
