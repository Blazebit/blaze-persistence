/*
 * Copyright 2014 Blazebit.
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
package com.blazebit.persistence.impl;

import com.blazebit.persistence.KeySet;
import java.io.Serializable;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class KeySetImpl implements KeySet {

    private final int firstResult;
    private final int maxResults;
    private final String[] orderByExpressions;
    private final Serializable[] lowest;
    private final Serializable[] highest;

    public KeySetImpl(int firstResult, int maxResults, String[] orderByExpressions, Serializable[] lowest, Serializable[] highest) {
        this.firstResult = firstResult;
        this.maxResults = maxResults;
        this.orderByExpressions = orderByExpressions;
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
    public Serializable[] getLowest() {
        return lowest;
    }

    @Override
    public Serializable[] getHighest() {
        return highest;
    }

    public String[] getOrderByExpressions() {
        return orderByExpressions;
    }
}
