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

package com.blazebit.persistence.view.testsuite.flat;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.Sorters;
import com.blazebit.persistence.view.metamodel.FlatViewType;
import com.blazebit.persistence.view.metamodel.ViewMetamodel;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.testsuite.flat.model.DocumentFlatEmbeddingView;
import com.blazebit.persistence.view.testsuite.flat.model.DocumentNamePerOwnerStatsIdView;
import com.blazebit.persistence.view.testsuite.flat.model.DocumentNamePerOwnerStatsView;
import com.blazebit.persistence.view.testsuite.flat.model.PersonFlatView;
import com.blazebit.persistence.view.testsuite.flat.model.UpdatableDocumentFlatView;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class FlatViewAsEmbeddedIdTest extends AbstractEntityViewTest {

    protected EntityViewManager evm;

    @Before
    public void initEvm() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(DocumentNamePerOwnerStatsIdView.class);
        cfg.addEntityView(DocumentNamePerOwnerStatsView.class);
        evm = cfg.createEntityViewManager(cbf);
    }

    @Test
    public void queryFlatView() {
        // Persist 5 documents, one has a duplicate name
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                Person o1 = new Person("pers1");
                Person o2 = new Person("pers2");
                em.persist(o1);
                em.persist(o2);

                em.persist(new Document("doc1", o1));
                em.persist(new Document("doc2", o2));
                em.persist(new Document("doc3", o1));
                em.persist(new Document("doc4", o2));
                em.persist(new Document("doc1", o1));
            }
        });

        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d")
                .groupBy("owner.name");
        EntityViewSetting<DocumentNamePerOwnerStatsView, CriteriaBuilder<DocumentNamePerOwnerStatsView>> setting;
        setting = EntityViewSetting.create(DocumentNamePerOwnerStatsView.class);
        setting.addAttributeSorter("id.name", Sorters.ascending());
        CriteriaBuilder<DocumentNamePerOwnerStatsView> cb = evm.applySetting(setting, criteria);
        List<DocumentNamePerOwnerStatsView> results = cb.getResultList();

        assertEquals(2, results.size());

        assertEquals("pers1", results.get(0).getId().getName());
        assertEquals("pers2", results.get(1).getId().getName());
        assertEquals(2L, results.get(0).getNameCount());
        assertEquals(2L, results.get(1).getNameCount());
    }

}
