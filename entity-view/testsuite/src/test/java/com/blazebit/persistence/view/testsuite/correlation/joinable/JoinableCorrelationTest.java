/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.correlation.joinable;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate43;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate50;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.correlation.joinable.model.DocumentJoinableCorrelationView;
import com.blazebit.persistence.view.testsuite.correlation.model.SimplePersonCorrelatedSubView;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 *
 * @author Christian Beikov
 * @since 1.2.1
 */
public class JoinableCorrelationTest extends AbstractEntityViewTest {

    @Test
    // NOTE: Requires entity joins which are supported since Hibernate 5.1, Datanucleus 5 and latest Eclipselink
    // NOTE: Eclipselink renders a cross join at the wrong position in the SQL
    // NOTE: Eclipselink and Datanucleus don't support the single valued id access optimization which causes a cyclic join dependency
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class })
    public void testJoinableCorrelation() {
        EntityViewManager evm = build(
                DocumentJoinableCorrelationView.class,
                SimplePersonCorrelatedSubView.class
        );

        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        EntityViewSetting<DocumentJoinableCorrelationView, CriteriaBuilder<DocumentJoinableCorrelationView>> setting =
                EntityViewSetting.create(DocumentJoinableCorrelationView.class);
        CriteriaBuilder<DocumentJoinableCorrelationView> cb = evm.applySetting(setting, criteria);
        cb.getResultList();
    }

}
