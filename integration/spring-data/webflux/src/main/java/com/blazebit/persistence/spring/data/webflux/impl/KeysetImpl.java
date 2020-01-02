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

package com.blazebit.persistence.spring.data.webflux.impl;

import com.blazebit.persistence.Keyset;

import java.io.Serializable;
import java.util.Arrays;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class KeysetImpl implements Keyset {

    private static final long serialVersionUID = 1L;

    private final Serializable[] tuple;

    public KeysetImpl(Serializable[] tuple) {
        this.tuple = tuple;
    }

    @Override
    public Serializable[] getTuple() {
        return tuple;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + Arrays.deepHashCode(this.tuple);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Keyset && Arrays.deepEquals(this.tuple, ((Keyset) obj).getTuple());
    }
}
