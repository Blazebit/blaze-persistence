/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.correlation.general;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.view.testsuite.correlation.AbstractCorrelationTest;
import com.blazebit.persistence.view.testsuite.correlation.general.model.DocumentCorrelationViewJoinId;
import com.blazebit.persistence.view.testsuite.correlation.general.model.DocumentCorrelationViewJoinNormal;
import com.blazebit.persistence.view.testsuite.correlation.general.model.DocumentCorrelationViewSubqueryId;
import com.blazebit.persistence.view.testsuite.correlation.general.model.DocumentCorrelationViewSubqueryNormal;
import com.blazebit.persistence.view.testsuite.correlation.general.model.DocumentCorrelationViewSubselectId;
import com.blazebit.persistence.view.testsuite.correlation.general.model.DocumentCorrelationViewSubselectNormal;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class GeneralCorrelationTest extends AbstractCorrelationTest {

    @Test
    public void testSubqueryCorrelationNormal() {
        testCorrelation(DocumentCorrelationViewSubqueryNormal.class, null);
    }

    @Test
    public void testSubqueryCorrelationId() {
        testCorrelation(DocumentCorrelationViewSubqueryId.class, null);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoEclipselink.class})
    public void testSubqueryBatchedCorrelationNormalSize2() {
        testCorrelation(DocumentCorrelationViewSubqueryNormal.class, 2);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoEclipselink.class})
    public void testSubqueryBatchedCorrelationIdSize2() {
        testCorrelation(DocumentCorrelationViewSubqueryId.class, 2);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoEclipselink.class})
    public void testSubqueryBatchedCorrelationNormalSize4() {
        testCorrelation(DocumentCorrelationViewSubqueryNormal.class, 4);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoEclipselink.class})
    public void testSubqueryBatchedCorrelationIdSize4() {
        testCorrelation(DocumentCorrelationViewSubqueryId.class, 4);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoEclipselink.class})
    public void testSubqueryBatchedCorrelationNormalSize20() {
        testCorrelation(DocumentCorrelationViewSubqueryNormal.class, 20);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoEclipselink.class})
    public void testSubqueryBatchedCorrelationIdSize20() {
        testCorrelation(DocumentCorrelationViewSubqueryId.class, 20);
    }

    // TODO: test batch correlation expectation configuration
    // TODO: make explicit test for correlation key batching with view root usage maybe via nested subviews through collections?

    @Test
    public void testSubselectCorrelationNormal() {
        testCorrelation(DocumentCorrelationViewSubselectNormal.class, null);
    }

    @Test
    public void testSubselectCorrelationId() {
        testCorrelation(DocumentCorrelationViewSubselectId.class, null);
    }

    @Test
    // NOTE: Eclipselink renders a cross join at the wrong position in the SQL
    @Category({ NoEclipselink.class })
    public void testJoinCorrelationNormal() {
        testCorrelation(DocumentCorrelationViewJoinNormal.class, null);
    }

    @Test
    // NOTE: Eclipselink renders a cross join at the wrong position in the SQL
    @Category({ NoEclipselink.class })
    public void testJoinCorrelationId() {
        testCorrelation(DocumentCorrelationViewJoinId.class, null);
    }

}
