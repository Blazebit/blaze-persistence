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

package com.blazebit.persistence.view.testsuite.correlation.simple;

import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus4;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate43;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate50;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.view.testsuite.correlation.AbstractCorrelationTest;
import com.blazebit.persistence.view.testsuite.correlation.simple.model.DocumentSimpleCorrelationViewJoinId;
import com.blazebit.persistence.view.testsuite.correlation.simple.model.DocumentSimpleCorrelationViewJoinNormal;
import com.blazebit.persistence.view.testsuite.correlation.simple.model.DocumentSimpleCorrelationViewSubqueryId;
import com.blazebit.persistence.view.testsuite.correlation.simple.model.DocumentSimpleCorrelationViewSubqueryNormal;
import com.blazebit.persistence.view.testsuite.correlation.simple.model.DocumentSimpleCorrelationViewSubselectId;
import com.blazebit.persistence.view.testsuite.correlation.simple.model.DocumentSimpleCorrelationViewSubselectNormal;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SimpleCorrelationTest extends AbstractCorrelationTest {

    @Test
    // NOTE: Datenucleus issue: https://github.com/datanucleus/datanucleus-api-jpa/issues/77
    @Category({ NoDatanucleus.class })
    public void testSubqueryCorrelationNormal() {
        testCorrelation(DocumentSimpleCorrelationViewSubqueryNormal.class, null);
    }

    @Test
    // NOTE: Datenucleus issue: https://github.com/datanucleus/datanucleus-api-jpa/issues/77
    @Category({ NoDatanucleus.class })
    public void testSubqueryCorrelationId() {
        testCorrelation(DocumentSimpleCorrelationViewSubqueryId.class, null);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoDatanucleus4.class, NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class})
    public void testSubqueryBatchedCorrelationNormalSize2() {
        testCorrelation(DocumentSimpleCorrelationViewSubqueryNormal.class, 2);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoDatanucleus4.class, NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class})
    public void testSubqueryBatchedCorrelationIdSize2() {
        testCorrelation(DocumentSimpleCorrelationViewSubqueryId.class, 2);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoDatanucleus4.class, NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class})
    public void testSubqueryBatchedCorrelationNormalSize4() {
        testCorrelation(DocumentSimpleCorrelationViewSubqueryNormal.class, 4);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoDatanucleus4.class, NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class})
    public void testSubqueryBatchedCorrelationIdSize4() {
        testCorrelation(DocumentSimpleCorrelationViewSubqueryId.class, 4);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class})
    public void testSubqueryBatchedCorrelationNormalSize20() {
        testCorrelation(DocumentSimpleCorrelationViewSubqueryNormal.class, 20);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class})
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
    // NOTE: Requires entity joins which are supported since Hibernate 5.1, Datanucleus 5 and latest Eclipselink
    // NOTE: Eclipselink renders a cross join at the wrong position in the SQL
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus4.class, NoOpenJPA.class, NoEclipselink.class })
    public void testJoinCorrelationNormal() {
        testCorrelation(DocumentSimpleCorrelationViewJoinNormal.class, null);
    }

    @Test
    // NOTE: Requires entity joins which are supported since Hibernate 5.1, Datanucleus 5 and latest Eclipselink
    // NOTE: Eclipselink renders a cross join at the wrong position in the SQL
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus4.class, NoOpenJPA.class, NoEclipselink.class })
    public void testJoinCorrelationId() {
        testCorrelation(DocumentSimpleCorrelationViewJoinId.class, null);
    }

}
