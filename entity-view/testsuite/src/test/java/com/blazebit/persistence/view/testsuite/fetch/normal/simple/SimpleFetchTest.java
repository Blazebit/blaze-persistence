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

package com.blazebit.persistence.view.testsuite.fetch.normal.simple;

import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus4;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate43;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate50;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.view.testsuite.fetch.normal.AbstractFetchTest;
import com.blazebit.persistence.view.testsuite.fetch.normal.simple.model.DocumentSimpleFetchViewJoin;
import com.blazebit.persistence.view.testsuite.fetch.normal.simple.model.DocumentSimpleFetchViewSubquery;
import com.blazebit.persistence.view.testsuite.fetch.normal.simple.model.DocumentSimpleFetchViewSubselect;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SimpleFetchTest extends AbstractFetchTest {

    @Test
    // NOTE: Datenucleus issue: https://github.com/datanucleus/datanucleus-api-jpa/issues/77
    @Category({ NoDatanucleus.class })
    public void testSubqueryFetch() {
        testCorrelation(DocumentSimpleFetchViewSubquery.class);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoDatanucleus4.class, NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class})
    public void testSubqueryBatchedCorrelationIdSize2() {
        testCorrelation(DocumentSimpleFetchViewSubquery.class, 2);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoDatanucleus4.class, NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class})
    public void testSubqueryBatchedCorrelationIdSize4() {
        testCorrelation(DocumentSimpleFetchViewSubquery.class, 4);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class})
    public void testSubqueryBatchedCorrelationIdSize20() {
        testCorrelation(DocumentSimpleFetchViewSubquery.class, 20);
    }

    @Test
    // NOTE: Eclipselink and Datanucleus don't support the single valued id access optimization which causes a cyclic join dependency
    @Category({ NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class })
    public void testSubselectFetch() {
        testCorrelation(DocumentSimpleFetchViewSubselect.class);
    }

    @Test
    // NOTE: Requires entity joins which are supported since Hibernate 5.1, Datanucleus 5 and latest Eclipselink
    // NOTE: Eclipselink renders a cross join at the wrong position in the SQL
    // NOTE: Eclipselink and Datanucleus don't support the single valued id access optimization which causes a cyclic join dependency
//    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus4.class, NoOpenJPA.class, NoEclipselink.class })
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class })
    public void testJoinFetch() {
        testCorrelation(DocumentSimpleFetchViewJoin.class);
    }
}
