/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.correlation.simple;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.view.testsuite.correlation.AbstractCorrelationTest;
import com.blazebit.persistence.view.testsuite.correlation.simple.model.DocumentSimpleCorrelationViewJoinId;
import com.blazebit.persistence.view.testsuite.correlation.simple.model.DocumentSimpleCorrelationViewJoinNormal;
import com.blazebit.persistence.view.testsuite.correlation.simple.model.DocumentSimpleCorrelationViewSubqueryId;
import com.blazebit.persistence.view.testsuite.correlation.simple.model.DocumentSimpleCorrelationViewSubqueryNormal;
import com.blazebit.persistence.view.testsuite.correlation.simple.model.DocumentSimpleCorrelationViewSubselectId;
import com.blazebit.persistence.view.testsuite.correlation.simple.model.DocumentSimpleCorrelationViewSubselectNormal;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SimpleCorrelationTest extends AbstractCorrelationTest {

    @Test
    public void testSubqueryCorrelationNormal() {
        testCorrelation(DocumentSimpleCorrelationViewSubqueryNormal.class, null);
    }

    @Test
    public void testSubqueryCorrelationId() {
        testCorrelation(DocumentSimpleCorrelationViewSubqueryId.class, null);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoEclipselink.class})
    public void testSubqueryBatchedCorrelationNormalSize2() {
        testCorrelation(DocumentSimpleCorrelationViewSubqueryNormal.class, 2);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoEclipselink.class})
    public void testSubqueryBatchedCorrelationIdSize2() {
        testCorrelation(DocumentSimpleCorrelationViewSubqueryId.class, 2);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoEclipselink.class})
    public void testSubqueryBatchedCorrelationNormalSize4() {
        testCorrelation(DocumentSimpleCorrelationViewSubqueryNormal.class, 4);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoEclipselink.class})
    public void testSubqueryBatchedCorrelationIdSize4() {
        testCorrelation(DocumentSimpleCorrelationViewSubqueryId.class, 4);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoEclipselink.class})
    public void testSubqueryBatchedCorrelationNormalSize20() {
        testCorrelation(DocumentSimpleCorrelationViewSubqueryNormal.class, 20);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoEclipselink.class})
    public void testSubqueryBatchedCorrelationIdSize20() {
        testCorrelation(DocumentSimpleCorrelationViewSubqueryId.class, 20);
    }

    // TODO: test batch correlation expectation configuration
    // TODO: make explicit test for correlation key batching with view root usage maybe via nested subviews through collections?

    @Test
    public void testSubselectCorrelationNormal() {
        testCorrelation(DocumentSimpleCorrelationViewSubselectNormal.class, null);
    }

    @Test
    public void testSubselectCorrelationId() {
        testCorrelation(DocumentSimpleCorrelationViewSubselectId.class, null);
    }

    @Test
    // NOTE: Eclipselink renders a cross join at the wrong position in the SQL
    @Category({ NoEclipselink.class })
    public void testJoinCorrelationNormal() {
        testCorrelation(DocumentSimpleCorrelationViewJoinNormal.class, null);
    }

    @Test
    // NOTE: Eclipselink renders a cross join at the wrong position in the SQL
    @Category({ NoEclipselink.class })
    public void testJoinCorrelationId() {
        testCorrelation(DocumentSimpleCorrelationViewJoinId.class, null);
    }

}
