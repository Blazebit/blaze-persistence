/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.fetch.normal.simple;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.view.testsuite.fetch.normal.AbstractFetchTest;
import com.blazebit.persistence.view.testsuite.fetch.normal.simple.model.DocumentSimpleFetchViewJoin;
import com.blazebit.persistence.view.testsuite.fetch.normal.simple.model.DocumentSimpleFetchViewSubquery;
import com.blazebit.persistence.view.testsuite.fetch.normal.simple.model.DocumentSimpleFetchViewSubselect;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SimpleFetchTest extends AbstractFetchTest {

    @Test
    public void testSubqueryFetch() {
        testCorrelation(DocumentSimpleFetchViewSubquery.class);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoEclipselink.class})
    public void testSubqueryBatchedCorrelationIdSize2() {
        testCorrelation(DocumentSimpleFetchViewSubquery.class, 2);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoEclipselink.class})
    public void testSubqueryBatchedCorrelationIdSize4() {
        testCorrelation(DocumentSimpleFetchViewSubquery.class, 4);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoEclipselink.class})
    public void testSubqueryBatchedCorrelationIdSize20() {
        testCorrelation(DocumentSimpleFetchViewSubquery.class, 20);
    }

    @Test
    // NOTE: Eclipselink and Datanucleus don't support the single valued id access optimization which causes a cyclic join dependency
    @Category({ NoEclipselink.class })
    public void testSubselectFetch() {
        testCorrelation(DocumentSimpleFetchViewSubselect.class);
    }

    @Test
    // NOTE: Eclipselink renders a cross join at the wrong position in the SQL
    // NOTE: Eclipselink and Datanucleus don't support the single valued id access optimization which causes a cyclic join dependency
    @Category({ NoEclipselink.class })
    public void testJoinFetch() {
        testCorrelation(DocumentSimpleFetchViewJoin.class);
    }
}
