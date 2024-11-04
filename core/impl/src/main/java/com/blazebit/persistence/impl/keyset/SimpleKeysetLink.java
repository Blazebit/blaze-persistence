/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.keyset;

import java.util.List;

import com.blazebit.persistence.Keyset;
import com.blazebit.persistence.impl.OrderByExpression;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class SimpleKeysetLink extends AbstractKeysetLink {

    private final Keyset keyset;

    public SimpleKeysetLink(Keyset keyset, KeysetMode keysetMode) {
        super(keysetMode);

        if (keyset == null) {
            throw new NullPointerException("keyset");
        }

        this.keyset = keyset;
    }

    @Override
    public void initialize(List<OrderByExpression> orderByExpressions) {
        validate(keyset, orderByExpressions);
    }

    @Override
    public Keyset getKeyset() {
        return keyset;
    }
}
