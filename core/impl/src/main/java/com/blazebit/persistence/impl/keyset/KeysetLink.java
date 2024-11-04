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
public interface KeysetLink {

    public void initialize(List<OrderByExpression> orderByExpressions);

    public Keyset getKeyset();

    public KeysetMode getKeysetMode();
}
