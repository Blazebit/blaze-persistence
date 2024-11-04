/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.embedded.collection;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.embedded.AbstractEmbeddedMappingTest;
import com.blazebit.persistence.view.testsuite.embedded.collection.model.DocumentDetailEmbeddableCollectionView;
import com.blazebit.persistence.view.testsuite.embedded.collection.model.DocumentDetailCollectionView;
import com.blazebit.persistence.view.testsuite.embedded.collection.model.DocumentViewWithEmbeddedCollection;
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
public class EmbeddedMappingCollectionTest extends AbstractEmbeddedMappingTest {

    EntityViewManager evm;

    @Before
    public void prepare() {
        this.evm = build(
                DocumentDetailCollectionView.class,
                DocumentDetailEmbeddableCollectionView.class,
                DocumentViewWithEmbeddedCollection.class
        );
    }

    @Test
    public void multipleEmbeddingsProduceCorrectResults() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "e")
            .orderByAsc("id");
        EntityViewSetting<DocumentViewWithEmbeddedCollection, CriteriaBuilder<DocumentViewWithEmbeddedCollection>> setting = EntityViewSetting.create(DocumentViewWithEmbeddedCollection.class);
        CriteriaBuilder<DocumentViewWithEmbeddedCollection> cb = evm.applySetting(setting, criteria);
        List<DocumentViewWithEmbeddedCollection> results = cb.getResultList();

        assertEquals(2, results.size());

        assertEquals(doc1.getId(), results.get(0).getId());
        assertEquals(1, results.get(0).getDetails().size());
        assertEquals(1, results.get(0).getEmbeddedDetails().size());
        assertEquals(doc1.getId(), results.get(0).getDetails().get(0).getId());
        assertEquals("doc1", results.get(0).getDetails().get(0).getName());
        assertEquals("doc1", results.get(0).getEmbeddedDetails().get(0).getName());

        assertEquals(doc2.getId(), results.get(1).getId());
        assertEquals(1, results.get(1).getDetails().size());
        assertEquals(1, results.get(1).getEmbeddedDetails().size());
        assertEquals(doc2.getId(), results.get(1).getDetails().get(0).getId());
        assertEquals("doc2", results.get(1).getDetails().get(0).getName());
        assertEquals("doc2", results.get(1).getEmbeddedDetails().get(0).getName());
    }

    @Test
    public void filteringOnEmbeddedAttributeWorks() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "e")
                .orderByAsc("id");
        EntityViewSetting<DocumentViewWithEmbeddedCollection, CriteriaBuilder<DocumentViewWithEmbeddedCollection>> setting = EntityViewSetting.create(DocumentViewWithEmbeddedCollection.class);
        setting.addAttributeFilter("details.name", "doc1");
        setting.addAttributeFilter("embeddedDetails.name", "doc1");
        CriteriaBuilder<DocumentViewWithEmbeddedCollection> cb = evm.applySetting(setting, criteria);
        List<DocumentViewWithEmbeddedCollection> results = cb.getResultList();

        assertEquals(1, results.size());

        assertEquals(doc1.getId(), results.get(0).getId());
        assertEquals(1, results.get(0).getDetails().size());
        assertEquals(1, results.get(0).getEmbeddedDetails().size());
        assertEquals(doc1.getId(), results.get(0).getDetails().get(0).getId());
        assertEquals("doc1", results.get(0).getDetails().get(0).getName());
        assertEquals("doc1", results.get(0).getEmbeddedDetails().get(0).getName());
    }
}
