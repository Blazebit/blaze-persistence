/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.filter.basic;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.filter.basic.model.AggregateFilterDocumentView;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Christian Beikov
 * @since 1.6.8
 */
public class AggregateAttributeFilterTest extends AbstractEntityViewTest {

    @Test
    public void testAggregateAttributeFilter() {
        build(AggregateFilterDocumentView.class);

        EntityViewSetting<AggregateFilterDocumentView, CriteriaBuilder<AggregateFilterDocumentView>> setting = EntityViewSetting.create(AggregateFilterDocumentView.class);
        setting.addAttributeFilter("count", 1);
        String queryString = evm.applySetting(setting, cbf.create(em, Document.class)).getQueryString();
        assertEquals("SELECT document.age AS AggregateFilterDocumentView_age, " + countStar() + " AS AggregateFilterDocumentView_count FROM Document document GROUP BY document.age HAVING " + countStar() + " >= :param_0", queryString);
    }
}
