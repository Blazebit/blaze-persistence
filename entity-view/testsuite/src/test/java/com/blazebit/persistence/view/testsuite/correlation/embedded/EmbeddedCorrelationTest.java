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

package com.blazebit.persistence.view.testsuite.correlation.embedded;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate43;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate50;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntity;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityContainer;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityEmbeddable;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityId;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityNestedEmbeddable;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntitySub;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.entity.NameObject;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.correlation.embedded.model.EmbeddableTestEntityCorrelationView;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;

/**
 * @author Christian Beikov
 * @since 1.2.1
 */
public class EmbeddedCorrelationTest extends AbstractEntityViewTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
                IntIdEntity.class,
                EmbeddableTestEntity.class,
                EmbeddableTestEntitySub.class,
                EmbeddableTestEntityContainer.class,
                EmbeddableTestEntityEmbeddable.class,
                NameObject.class,
                EmbeddableTestEntityNestedEmbeddable.class
        };
    }

    @Override
    protected void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                EmbeddableTestEntityId embeddable1Id = new EmbeddableTestEntityId("1", "oldKey");
                EmbeddableTestEntity embeddable1 = new EmbeddableTestEntity();
                embeddable1.setId(embeddable1Id);
                em.persist(embeddable1);
            }
        });
    }

    @Test // For #556
    // NOTE: Requires entity joins which are supported since Hibernate 5.1, Datanucleus 5 and latest Eclipselink
    // NOTE: Eclipselink renders a cross join at the wrong position in the SQL
    // NOTE: Eclipselink and Datanucleus don't support the single valued id access optimization which causes a cyclic join dependency
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class })
    public void testEmbeddedCorrelation() {
        EntityViewManager evm = build(
                EmbeddableTestEntityCorrelationView.class,
                EmbeddableTestEntityCorrelationView.Id.class,
                EmbeddableTestEntityCorrelationView.SimpleEmbeddableTestEntityCorrelationView.class
        );

        CriteriaBuilder<EmbeddableTestEntity> criteria = cbf.create(em, EmbeddableTestEntity.class, "d");
        EntityViewSetting<EmbeddableTestEntityCorrelationView, CriteriaBuilder<EmbeddableTestEntityCorrelationView>> setting =
                EntityViewSetting.create(EmbeddableTestEntityCorrelationView.class);
        CriteriaBuilder<EmbeddableTestEntityCorrelationView> cb = evm.applySetting(setting, criteria);
        cb.getResultList();
    }

}
