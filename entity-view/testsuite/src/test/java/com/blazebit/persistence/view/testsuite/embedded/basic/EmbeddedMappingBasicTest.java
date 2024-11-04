/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
        this.evm = build(
                DocumentDetailView.class,
                DocumentDetailEmbeddableView.class,
                DocumentViewWithEmbedded.class
        );
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
