/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.basic;

import org.junit.Test;
import org.mockito.Mockito;

import com.blazebit.persistence.OrderByBuilder;
import com.blazebit.persistence.view.Sorter;
import com.blazebit.persistence.view.Sorters;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class SorterTest {

    private final String expression = "name";

    @Test
    public void testAscendingNullFirst() {
        Sorter sorter = Sorters.sorter(true, true);
        verifySorter(sorter).orderBy(expression, true, true);
    }

    @Test
    public void testAscendingNullLast() {
        Sorter sorter = Sorters.sorter(true, false);
        verifySorter(sorter).orderBy(expression, true, false);
    }

    @Test
    public void testDescendingNullFirst() {
        Sorter sorter = Sorters.sorter(false, true);
        verifySorter(sorter).orderBy(expression, false, true);
    }

    @Test
    public void testDescendingNullLast() {
        Sorter sorter = Sorters.sorter(false, false);
        verifySorter(sorter).orderBy(expression, false, false);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public OrderByBuilder<?> verifySorter(Sorter sorter) {
        OrderByBuilder<?> sortable = Mockito.mock(OrderByBuilder.class);
        sorter.apply((OrderByBuilder) sortable, expression);
        return Mockito.verify(sortable);
    }
}
