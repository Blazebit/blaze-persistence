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
import java.util.Arrays;
import java.util.List;

import com.blazebit.persistence.Keyset;
import com.blazebit.persistence.impl.OrderByExpression;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class AbstractKeysetLink implements KeysetLink {

    private final KeysetMode keysetMode;

    public AbstractKeysetLink(KeysetMode keysetMode) {
        this.keysetMode = keysetMode;
    }

    protected void validate(Keyset keyset, List<OrderByExpression> orderByExpressions) {
        if (keyset == null) {
            throw new IllegalArgumentException("Invalid null keyset given!");
        }

        Serializable[] key = keyset.getTuple();

        if (key == null || key.length == 0) {
            throw new IllegalArgumentException("Invalid empty keyset key given!");
        }

        if (key.length != orderByExpressions.size()) {
            throw new IllegalArgumentException("The given keyset key [" + Arrays.deepToString(key) + "] does not fit the order by expressions "
                + orderByExpressions + "!");
        }

        // Unfortunately we can't check types here so we will have to trust the JPA provider to do that
        // Still it would be nice to give the user a more informative message if types were wrong
    }

    @Override
    public KeysetMode getKeysetMode() {
        return keysetMode;
    }
}
