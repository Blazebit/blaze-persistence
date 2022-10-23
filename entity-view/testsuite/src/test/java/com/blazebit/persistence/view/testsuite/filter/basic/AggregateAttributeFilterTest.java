/*
 * Copyright 2014 - 2022 Blazebit.
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
