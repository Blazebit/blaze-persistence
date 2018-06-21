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
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(DocumentJoinableCorrelationView.class);
        cfg.addEntityView(SimplePersonCorrelatedSubView.class);
        EntityViewManager evm = cfg.createEntityViewManager(cbf);

        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        EntityViewSetting<DocumentJoinableCorrelationView, CriteriaBuilder<DocumentJoinableCorrelationView>> setting =
                EntityViewSetting.create(DocumentJoinableCorrelationView.class);
        CriteriaBuilder<DocumentJoinableCorrelationView> cb = evm.applySetting(setting, criteria);
        cb.getResultList();
    }

}
