/*
 * Copyright 2014 - 2024 Blazebit.
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple default implementation for the {@link KeysetPage} interface.
 *
 * @author Christian Beikov
 * @since 1.4.1
 */
public class DefaultKeysetPage implements KeysetPage {

    private static final long serialVersionUID = 1L;

    private final int firstResult;
    private final int maxResults;
    private final Keyset lowest;
    private final Keyset highest;
    private final List<Keyset> keysets;

    /**
     * Creates a new {@link KeysetPage}.
     *
     * @param firstResult The first result
     * @param maxResults The max results
     * @param lowest The lowest keyset
     * @param highest The highest keyset
     * @param keysets All extracted keysets
     */
    public DefaultKeysetPage(int firstResult, int maxResults, Serializable[] lowest, Serializable[] highest, Serializable[][] keysets) {
        this(firstResult, maxResults, new DefaultKeyset(lowest), new DefaultKeyset(highest), keysets(keysets));
    }

    /**
     * Creates a new {@link KeysetPage}.
     *
     * @param firstResult The first result
     * @param maxResults The max results
     * @param lowest The lowest keyset
     * @param highest The highest keyset
     */
    public DefaultKeysetPage(int firstResult, int maxResults, Keyset lowest, Keyset highest) {
        this.firstResult = firstResult;
        this.maxResults = maxResults;
        this.lowest = lowest;
        this.highest = highest;
        List<Keyset> keysets = new ArrayList<>(2);
        if (lowest != null) {
            keysets.add(lowest);
        }
        if (highest != null) {
            keysets.add(highest);
        }
        this.keysets = keysets;
    }

    /**
     * Creates a new {@link KeysetPage}.
     *
     * @param firstResult The first result
     * @param maxResults The max results
     * @param lowest The lowest keyset
     * @param highest The highest keyset
     * @param keysets All extracted keysets
     */
    public DefaultKeysetPage(int firstResult, int maxResults, Keyset lowest, Keyset highest, List<Keyset> keysets) {
        this.firstResult = firstResult;
        this.maxResults = maxResults;
        this.lowest = lowest;
        this.highest = highest;
        this.keysets = keysets;
    }

    private static List<Keyset> keysets(Serializable[][] keysets) {
        if (keysets == null || keysets.length == 0) {
            return Collections.emptyList();
        }
        List<Keyset> list = new ArrayList<>(keysets.length);
        for (Serializable[] keyset : keysets) {
            list.add(new DefaultKeyset(keyset));
        }

        return list;
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
        return keysets;
    }
}
