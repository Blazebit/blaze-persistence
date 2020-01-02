/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.integration.graphql;

import com.blazebit.persistence.Keyset;
import com.blazebit.persistence.KeysetPage;

import java.util.List;


/**
 * A simple keyset page implementation.
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
class KeysetPageImpl implements KeysetPage {

    private final int firstResult;
    private final int maxResults;
    private final Keyset lowest;
    private final Keyset highest;

    /**
     * Constructs a new keyset page.
     *
     * @param firstResult The first result
     * @param maxResults The max results
     * @param lowest The lowest keyset
     * @param highest The highest keyset
     */
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

    @Override
    public List<Keyset> getKeysets() {
        return null;
    }
}
