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

package com.blazebit.persistence.view.testsuite.correlation.expression;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus4;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate43;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate50;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.Sorters;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.correlation.AbstractCorrelationTest;
import com.blazebit.persistence.view.testsuite.correlation.expression.model.DocumentSimpleCorrelationViewJoinId;
import com.blazebit.persistence.view.testsuite.correlation.expression.model.DocumentSimpleCorrelationViewSubqueryId;
import com.blazebit.persistence.view.testsuite.correlation.expression.model.DocumentSimpleCorrelationViewSubselectId;
import com.blazebit.persistence.view.testsuite.correlation.model.SimpleDocumentCorrelatedView;
import com.blazebit.persistence.view.testsuite.correlation.model.SimplePersonCorrelatedSubView;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ExpressionCorrelationTest extends AbstractCorrelationTest {

    @Test
    // NOTE: Datenucleus issue: https://github.com/datanucleus/datanucleus-api-jpa/issues/77
    @Category({ NoDatanucleus.class })
    public void testSubqueryCorrelationId() {
        testCorrelation(DocumentSimpleCorrelationViewSubqueryId.class, null);
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
    public void testSubqueryBatchedCorrelationIdSize4() {
        testCorrelation(DocumentSimpleCorrelationViewSubqueryId.class, 4);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class})
    public void testSubqueryBatchedCorrelationIdSize20() {
        testCorrelation(DocumentSimpleCorrelationViewSubqueryId.class, 20);
    }

    @Test
    // NOTE: Eclipselink and Datanucleus don't support the single valued id access optimization which causes a cyclic join dependency
    @Category({ NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class })
    public void testSubselectCorrelationId() {
        testCorrelation(DocumentSimpleCorrelationViewSubselectId.class, null);
    }

    @Test
    // NOTE: Requires entity joins which are supported since Hibernate 5.1, Datanucleus 5 and latest Eclipselink
    // NOTE: Eclipselink renders a cross join at the wrong position in the SQL
    // NOTE: Eclipselink and Datanucleus don't support the single valued id access optimization which causes a cyclic join dependency
//    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus4.class, NoOpenJPA.class, NoEclipselink.class })
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class })
    public void testJoinCorrelationId() {
        testCorrelation(DocumentSimpleCorrelationViewJoinId.class, null);
    }

    @Test
    // NOTE: Requires entity joins which are supported since Hibernate 5.1, Datanucleus 5 and latest Eclipselink
    // NOTE: Eclipselink renders a cross join at the wrong position in the SQL
    // NOTE: Eclipselink and Datanucleus don't support the single valued id access optimization which causes a cyclic join dependency
//    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus4.class, NoOpenJPA.class, NoEclipselink.class })
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class })
    public void testFilterSortJoinCorrelatedSingularViewPaginated() {
        EntityViewManager evm = buildEntityViewManagerForFilter();

        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        EntityViewSetting<DocumentSimpleCorrelationViewJoinId, PaginatedCriteriaBuilder<DocumentSimpleCorrelationViewJoinId>> setting;
        setting = EntityViewSetting.create(DocumentSimpleCorrelationViewJoinId.class, 0, 1);
        setting.addAttributeFilter("correlatedOwnerView.name", "PERS2");
        setting.addAttributeSorter("correlatedOwnerView.name", Sorters.ascending());
        setting.addAttributeSorter("id", Sorters.ascending());
        PaginatedCriteriaBuilder<DocumentSimpleCorrelationViewJoinId> cb = evm.applySetting(setting, criteria);
        PagedList<DocumentSimpleCorrelationViewJoinId> results = cb.getResultList();

        assertEquals(1, results.size());
        assertEquals(3, results.getTotalSize());
    }

    @Test
    // NOTE: Requires entity joins which are supported since Hibernate 5.1, Datanucleus 5 and latest Eclipselink
    // NOTE: Eclipselink renders a cross join at the wrong position in the SQL
    // NOTE: Eclipselink and Datanucleus don't support the single valued id access optimization which causes a cyclic join dependency
//    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus4.class, NoOpenJPA.class, NoEclipselink.class })
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class })
    public void testFilterSortJoinCorrelatedPluralViewPaginated() {
        EntityViewManager evm = buildEntityViewManagerForFilter();

        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        EntityViewSetting<DocumentSimpleCorrelationViewJoinId, PaginatedCriteriaBuilder<DocumentSimpleCorrelationViewJoinId>> setting;
        setting = EntityViewSetting.create(DocumentSimpleCorrelationViewJoinId.class, 0, 1);
        setting.addAttributeFilter("correlatedOwnerViewList.name", "PERS2");
        setting.addAttributeSorter("correlatedOwnerViewList.name", Sorters.ascending());
        setting.addAttributeSorter("id", Sorters.ascending());
        PaginatedCriteriaBuilder<DocumentSimpleCorrelationViewJoinId> cb = evm.applySetting(setting, criteria);
        PagedList<DocumentSimpleCorrelationViewJoinId> results = cb.getResultList();

        assertEquals(1, results.size());
        assertEquals(3, results.getTotalSize());
    }

    @Test
    // NOTE: Requires entity joins which are supported since Hibernate 5.1, Datanucleus 5 and latest Eclipselink
    // NOTE: Eclipselink renders a cross join at the wrong position in the SQL
    // NOTE: Eclipselink and Datanucleus don't support the single valued id access optimization which causes a cyclic join dependency
//    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus4.class, NoOpenJPA.class, NoEclipselink.class })
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class })
    public void testFilterSortJoinCorrelatedSingularBasicPaginated() {
        EntityViewManager evm = buildEntityViewManagerForFilter();

        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        EntityViewSetting<DocumentSimpleCorrelationViewJoinId, PaginatedCriteriaBuilder<DocumentSimpleCorrelationViewJoinId>> setting;
        setting = EntityViewSetting.create(DocumentSimpleCorrelationViewJoinId.class, 0, 1);
        setting.addAttributeFilter("correlatedOwnerId", doc2.getOwner().getId());
        setting.addAttributeSorter("correlatedOwnerId", Sorters.ascending());
        setting.addAttributeSorter("id", Sorters.ascending());
        PaginatedCriteriaBuilder<DocumentSimpleCorrelationViewJoinId> cb = evm.applySetting(setting, criteria);
        PagedList<DocumentSimpleCorrelationViewJoinId> results = cb.getResultList();

        assertEquals(1, results.size());
        assertEquals(3, results.getTotalSize());
    }

    @Test
    // NOTE: Requires entity joins which are supported since Hibernate 5.1, Datanucleus 5 and latest Eclipselink
    // NOTE: Eclipselink renders a cross join at the wrong position in the SQL
    // NOTE: Eclipselink and Datanucleus don't support the single valued id access optimization which causes a cyclic join dependency
//    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus4.class, NoOpenJPA.class, NoEclipselink.class })
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class })
    public void testFilterSortJoinCorrelatedPluralBasicPaginated() {
        EntityViewManager evm = buildEntityViewManagerForFilter();

        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        EntityViewSetting<DocumentSimpleCorrelationViewJoinId, PaginatedCriteriaBuilder<DocumentSimpleCorrelationViewJoinId>> setting;
        setting = EntityViewSetting.create(DocumentSimpleCorrelationViewJoinId.class, 0, 1);
        setting.addAttributeFilter("correlatedOwnerIdList", doc2.getOwner().getId());
        setting.addAttributeSorter("correlatedOwnerIdList", Sorters.ascending());
        setting.addAttributeSorter("id", Sorters.ascending());
        PaginatedCriteriaBuilder<DocumentSimpleCorrelationViewJoinId> cb = evm.applySetting(setting, criteria);
        PagedList<DocumentSimpleCorrelationViewJoinId> results = cb.getResultList();

        assertEquals(1, results.size());
        assertEquals(3, results.getTotalSize());
    }

    private EntityViewManager buildEntityViewManagerForFilter() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(DocumentSimpleCorrelationViewJoinId.class);
        cfg.addEntityView(SimpleDocumentCorrelatedView.class);
        cfg.addEntityView(SimplePersonCorrelatedSubView.class);
        return cfg.createEntityViewManager(cbf);
    }

}
