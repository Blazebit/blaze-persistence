/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.embedded.entity;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate66;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.embedded.AbstractEmbeddedMappingTest;
import com.blazebit.persistence.view.testsuite.embedded.entity.model.DocumentDetailEmbeddableEntityMappingView;
import com.blazebit.persistence.view.testsuite.embedded.entity.model.DocumentDetailEntityMappingView;
import com.blazebit.persistence.view.testsuite.embedded.entity.model.DocumentViewWithEmbeddedEntityMapping;
import com.blazebit.persistence.testsuite.entity.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class EmbeddedMappingEntityTest extends AbstractEmbeddedMappingTest {

    EntityViewManager evm;

    @Before
    public void prepare() {
        this.evm = build(
                DocumentDetailEntityMappingView.class,
                DocumentDetailEmbeddableEntityMappingView.class,
                DocumentViewWithEmbeddedEntityMapping.class
        );
    }

    @Test
    public void multipleEmbeddingsProduceCorrectResults() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "e")
            .orderByAsc("id");
        EntityViewSetting<DocumentViewWithEmbeddedEntityMapping, CriteriaBuilder<DocumentViewWithEmbeddedEntityMapping>> setting = EntityViewSetting.create(DocumentViewWithEmbeddedEntityMapping.class);
        CriteriaBuilder<DocumentViewWithEmbeddedEntityMapping> cb = evm.applySetting(setting, criteria);
        List<DocumentViewWithEmbeddedEntityMapping> results = cb.getResultList();

        assertEquals(2, results.size());

        assertEquals(doc1.getId(), results.get(0).getId());
        assertEquals(doc1.getId(), results.get(0).getDetails().getId());
        assertEquals(doc1, results.get(0).getDocument());
        assertEquals(doc1, results.get(0).getDetails().getDocument());
        assertEquals(doc1, results.get(0).getEmbeddedDetails().getDocument());
        assertEquals("doc1", results.get(0).getDetails().getName());
        assertEquals("doc1", results.get(0).getEmbeddedDetails().getName());

        assertEquals(doc2.getId(), results.get(1).getId());
        assertEquals(doc2.getId(), results.get(1).getDetails().getId());
        assertEquals(doc2, results.get(1).getDocument());
        assertEquals(doc2, results.get(1).getDetails().getDocument());
        assertEquals(doc2, results.get(1).getEmbeddedDetails().getDocument());
        assertEquals("doc2", results.get(1).getDetails().getName());
        assertEquals("doc2", results.get(1).getEmbeddedDetails().getName());
    }

    // NOTE: Hibernate ORM 6.6.1 has a bug: https://hibernate.atlassian.net/browse/HHH-18773
    @Test
    @Category(NoHibernate66.class)
    public void filteringOnEmbeddedAttributeWorks() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "e")
                .orderByAsc("id");
        EntityViewSetting<DocumentViewWithEmbeddedEntityMapping, CriteriaBuilder<DocumentViewWithEmbeddedEntityMapping>> setting = EntityViewSetting.create(DocumentViewWithEmbeddedEntityMapping.class);
        setting.addAttributeFilter("details.document", em.getReference(Document.class, doc1.getId()));
        setting.addAttributeFilter("embeddedDetails.document", em.getReference(Document.class, doc1.getId()));
        setting.addAttributeFilter("document", em.getReference(Document.class, doc1.getId()));
        CriteriaBuilder<DocumentViewWithEmbeddedEntityMapping> cb = evm.applySetting(setting, criteria);
        List<DocumentViewWithEmbeddedEntityMapping> results = cb.getResultList();

        assertEquals(1, results.size());

        assertEquals(doc1.getId(), results.get(0).getId());
        assertEquals(doc1.getId(), results.get(0).getDetails().getId());
        assertEquals(doc1, results.get(0).getDocument());
        assertEquals(doc1, results.get(0).getDetails().getDocument());
        assertEquals(doc1, results.get(0).getEmbeddedDetails().getDocument());
        assertEquals("doc1", results.get(0).getDetails().getName());
        assertEquals("doc1", results.get(0).getEmbeddedDetails().getName());
    }
}
