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

package com.blazebit.persistence.view.testsuite.embedded.basic;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.embedded.AbstractEmbeddedMappingTest;
import com.blazebit.persistence.view.testsuite.embedded.basic.model.DocumentDetailEmbeddableView;
import com.blazebit.persistence.view.testsuite.embedded.basic.model.DocumentDetailView;
import com.blazebit.persistence.view.testsuite.embedded.basic.model.DocumentViewWithEmbedded;
import com.blazebit.persistence.testsuite.entity.Document;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class EmbeddedMappingBasicTest extends AbstractEmbeddedMappingTest {

    EntityViewManager evm;

    @Before
    public void prepare() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(DocumentDetailView.class);
        cfg.addEntityView(DocumentDetailEmbeddableView.class);
        cfg.addEntityView(DocumentViewWithEmbedded.class);
        this.evm = cfg.createEntityViewManager(cbf);
    }

    @Test
    public void multipleEmbeddingsProduceCorrectResults() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "e")
            .orderByAsc("id");
        EntityViewSetting<DocumentViewWithEmbedded, CriteriaBuilder<DocumentViewWithEmbedded>> setting = EntityViewSetting.create(DocumentViewWithEmbedded.class);
        CriteriaBuilder<DocumentViewWithEmbedded> cb = evm.applySetting(setting, criteria);
        List<DocumentViewWithEmbedded> results = cb.getResultList();

        assertEquals(2, results.size());

        assertEquals(doc1.getId(), results.get(0).getId());
        assertEquals(doc1.getId(), results.get(0).getDetails().getId());
        assertEquals("doc1", results.get(0).getDetails().getName());
        assertEquals("doc1", results.get(0).getEmbeddedDetails().getName());

        assertEquals(doc2.getId(), results.get(1).getId());
        assertEquals(doc2.getId(), results.get(1).getDetails().getId());
        assertEquals("doc2", results.get(1).getDetails().getName());
        assertEquals("doc2", results.get(1).getEmbeddedDetails().getName());
    }

    @Test
    public void filteringOnEmbeddedAttributeWorks() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "e")
                .orderByAsc("id");
        EntityViewSetting<DocumentViewWithEmbedded, CriteriaBuilder<DocumentViewWithEmbedded>> setting = EntityViewSetting.create(DocumentViewWithEmbedded.class);
        setting.addAttributeFilter("details.name", "doc1");
        setting.addAttributeFilter("embeddedDetails.name", "doc1");
        CriteriaBuilder<DocumentViewWithEmbedded> cb = evm.applySetting(setting, criteria);
        List<DocumentViewWithEmbedded> results = cb.getResultList();
        assertEquals(1, results.size());

        assertEquals(doc1.getId(), results.get(0).getId());
        assertEquals(doc1.getId(), results.get(0).getDetails().getId());
        assertEquals("doc1", results.get(0).getDetails().getName());
        assertEquals("doc1", results.get(0).getEmbeddedDetails().getName());
    }
}
