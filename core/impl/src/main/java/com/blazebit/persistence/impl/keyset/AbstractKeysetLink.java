/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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

        // We treat a null tuple specially to support reverse scanning of a result list
        if (key != null) {
            if (key.length == 0) {
                throw new IllegalArgumentException("Invalid empty keyset key given!");
            }

            if (key.length != orderByExpressions.size()) {
                throw new IllegalArgumentException("The given keyset key [" + Arrays.deepToString(key) + "] does not fit the order by expressions "
                        + orderByExpressions + "!");
            }
        }

        // Unfortunately we can't check types here so we will have to trust the JPA provider to do that
        // Still it would be nice to give the user a more informative message if types were wrong
    }

    @Override
    public KeysetMode getKeysetMode() {
        return keysetMode;
    }
}
