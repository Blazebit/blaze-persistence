/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.correlation.nestedjoin;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.correlation.nestedjoin.model.DocumentNestedJoinView;
import com.blazebit.persistence.view.testsuite.correlation.nestedjoin.model.PersonNestedJoinSubView;
import com.blazebit.persistence.view.testsuite.correlation.nestedjoin.model.SimpleDocumentView;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class JoinableCorrelationTest extends AbstractEntityViewTest {

    // NOTE: Eclipselink renders a cross join at the wrong position in the SQL
    // NOTE: Eclipselink and Datanucleus don't support the single valued id access optimization which causes a cyclic join dependency
    @Test
    @Category({ NoEclipselink.class })
    public void testNestedJoinCorrelation() {
        EntityViewManager evm = build(
                DocumentNestedJoinView.class,
                PersonNestedJoinSubView.class,
                SimpleDocumentView.class
        );

        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        EntityViewSetting<DocumentNestedJoinView, CriteriaBuilder<DocumentNestedJoinView>> setting =
                EntityViewSetting.create(DocumentNestedJoinView.class);
        CriteriaBuilder<DocumentNestedJoinView> cb = evm.applySetting(setting, criteria);
        cb.getResultList();
    }

}
