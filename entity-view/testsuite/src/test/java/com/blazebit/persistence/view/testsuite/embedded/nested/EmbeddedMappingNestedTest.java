/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.embedded.nested;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.embedded.AbstractEmbeddedMappingTest;
import com.blazebit.persistence.view.testsuite.embedded.nested.model.DocumentDetailEmbeddableNestedView;
import com.blazebit.persistence.view.testsuite.embedded.nested.model.DocumentDetailNestedView;
import com.blazebit.persistence.view.testsuite.embedded.nested.model.DocumentViewNestedEmbedded;
import com.blazebit.persistence.view.testsuite.embedded.nested.model.DocumentViewNestedEmbeddedEmbeddable;
import com.blazebit.persistence.view.testsuite.embedded.nested.model.DocumentViewWithNestedEmbedded;
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
public class EmbeddedMappingNestedTest extends AbstractEmbeddedMappingTest {

    EntityViewManager evm;

    @Before
    public void prepare() {
        this.evm = build(
                DocumentDetailNestedView.class,
                DocumentDetailEmbeddableNestedView.class,
                DocumentViewNestedEmbedded.class,
                DocumentViewNestedEmbeddedEmbeddable.class,
                DocumentViewWithNestedEmbedded.class
        );
    }

    @Test
    public void multipleEmbeddingsProduceCorrectResults() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "e")
            .orderByAsc("id");
        EntityViewSetting<DocumentViewWithNestedEmbedded, CriteriaBuilder<DocumentViewWithNestedEmbedded>> setting = EntityViewSetting.create(DocumentViewWithNestedEmbedded.class);
        CriteriaBuilder<DocumentViewWithNestedEmbedded> cb = evm.applySetting(setting, criteria);
        List<DocumentViewWithNestedEmbedded> results = cb.getResultList();

        assertEquals(2, results.size());

        assertEquals(doc1.getId(), results.get(0).getId());
        assertEquals(doc1.getId(), results.get(0).getNested().getId());
        assertEquals(doc1.getId(), results.get(0).getNested().getDetails().getId());
        assertEquals(doc1.getId(), results.get(0).getNestedEmbedded().getDetails().getId());
        assertEquals("doc1", results.get(0).getNested().getDetails().getName());
        assertEquals("doc1", results.get(0).getNested().getEmbeddedDetails().getName());
        assertEquals("doc1", results.get(0).getNestedEmbedded().getDetails().getName());
        assertEquals("doc1", results.get(0).getNestedEmbedded().getEmbeddedDetails().getName());

        assertEquals(doc2.getId(), results.get(1).getId());
        assertEquals(doc2.getId(), results.get(1).getNested().getId());
        assertEquals(doc2.getId(), results.get(1).getNested().getDetails().getId());
        assertEquals(doc2.getId(), results.get(1).getNestedEmbedded().getDetails().getId());
        assertEquals("doc2", results.get(1).getNested().getDetails().getName());
        assertEquals("doc2", results.get(1).getNested().getEmbeddedDetails().getName());
        assertEquals("doc2", results.get(1).getNestedEmbedded().getDetails().getName());
        assertEquals("doc2", results.get(1).getNestedEmbedded().getEmbeddedDetails().getName());
    }

    @Test
    public void filteringOnEmbeddedAttributeWorks() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "e")
                .orderByAsc("id");
        EntityViewSetting<DocumentViewWithNestedEmbedded, CriteriaBuilder<DocumentViewWithNestedEmbedded>> setting = EntityViewSetting.create(DocumentViewWithNestedEmbedded.class);
        setting.addAttributeFilter("nested.details.name", "doc1");
        setting.addAttributeFilter("nested.embeddedDetails.name", "doc1");
        setting.addAttributeFilter("nestedEmbedded.details.name", "doc1");
        setting.addAttributeFilter("nestedEmbedded.embeddedDetails.name", "doc1");
        CriteriaBuilder<DocumentViewWithNestedEmbedded> cb = evm.applySetting(setting, criteria);
        List<DocumentViewWithNestedEmbedded> results = cb.getResultList();

        assertEquals(1, results.size());

        assertEquals(doc1.getId(), results.get(0).getId());
        assertEquals(doc1.getId(), results.get(0).getNested().getId());
        assertEquals(doc1.getId(), results.get(0).getNested().getDetails().getId());
        assertEquals(doc1.getId(), results.get(0).getNestedEmbedded().getDetails().getId());
        assertEquals("doc1", results.get(0).getNested().getDetails().getName());
        assertEquals("doc1", results.get(0).getNested().getEmbeddedDetails().getName());
        assertEquals("doc1", results.get(0).getNestedEmbedded().getDetails().getName());
        assertEquals("doc1", results.get(0).getNestedEmbedded().getEmbeddedDetails().getName());
    }
}
