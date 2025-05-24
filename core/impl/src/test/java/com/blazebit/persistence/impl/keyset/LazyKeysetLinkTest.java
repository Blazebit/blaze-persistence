/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.keyset;

import com.blazebit.persistence.impl.OrderByExpression;
import com.blazebit.persistence.parser.expression.PropertyExpression;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LazyKeysetLinkTest {

    @Test
    public void testReturningCorrectValuesInTuple() {
        final PropertyExpression property1 = new PropertyExpression("property1");
        final PropertyExpression property2 = new PropertyExpression("property2");

        final Map<String, Object> keysetValues = new HashMap<>();
        keysetValues.put(property1.getProperty(), 1);
        keysetValues.put(property2.getProperty(), 2);

        final ArrayList<OrderByExpression> orderByExpressions = new ArrayList<>();
        orderByExpressions.add(new OrderByExpression(true, false, property1, false, false, false));
        orderByExpressions.add(new OrderByExpression(true, false, property2, false, false, false));

        final LazyKeysetLink lazyKeysetLink = new LazyKeysetLink(keysetValues, KeysetMode.NEXT);
        lazyKeysetLink.initialize(orderByExpressions);

        Assert.assertArrayEquals(new Serializable[] {1, 2}, lazyKeysetLink.getKeyset().getTuple());
    }

}