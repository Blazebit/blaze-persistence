/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.correlation.embedding;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.ConfigurationProperties;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.correlation.embedding.model.DocumentEmbeddingCorrelationView;
import com.blazebit.persistence.view.testsuite.correlation.embedding.model.DocumentEmbeddingCorrelationViewJoinId;
import com.blazebit.persistence.view.testsuite.correlation.embedding.model.DocumentEmbeddingCorrelationViewJoinNormal;
import com.blazebit.persistence.view.testsuite.correlation.embedding.model.DocumentEmbeddingCorrelationViewSubqueryId;
import com.blazebit.persistence.view.testsuite.correlation.embedding.model.DocumentEmbeddingCorrelationViewSubqueryNormal;
import com.blazebit.persistence.view.testsuite.correlation.embedding.model.DocumentEmbeddingCorrelationViewSubselectId;
import com.blazebit.persistence.view.testsuite.correlation.embedding.model.DocumentEmbeddingCorrelationViewSubselectNormal;
import com.blazebit.persistence.view.testsuite.correlation.embedding.model.SimpleDocumentEmbeddingCorrelatedView;
import com.blazebit.persistence.view.testsuite.correlation.embedding.model.SimplePersonEmbeddingCorrelatedSubView;
import jakarta.persistence.EntityManager;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class EmbeddingViewRootCorrelationTest extends AbstractEntityViewTest {

    @Test
    public void testSubqueryCorrelationNormal() {
        testCorrelation(DocumentEmbeddingCorrelationViewSubqueryNormal.class, null);
    }

    @Test
    public void testSubqueryCorrelationId() {
        testCorrelation(DocumentEmbeddingCorrelationViewSubqueryId.class, null);
    }

    @Test
    @Category({ NoEclipselink.class})
    public void testSubqueryBatchedCorrelationNormalSize2() {
        testCorrelation(DocumentEmbeddingCorrelationViewSubqueryNormal.class, 2);
    }

    @Test
    @Category({ NoEclipselink.class})
    public void testSubqueryBatchedCorrelationIdSize2() {
        testCorrelation(DocumentEmbeddingCorrelationViewSubqueryId.class, 2);
    }

    @Test
    @Category({ NoEclipselink.class})
    public void testSubqueryBatchedCorrelationNormalSize4() {
        testCorrelation(DocumentEmbeddingCorrelationViewSubqueryNormal.class, 4);
    }

    @Test
    @Category({ NoEclipselink.class})
    public void testSubqueryBatchedCorrelationIdSize4() {
        testCorrelation(DocumentEmbeddingCorrelationViewSubqueryId.class, 4);
    }

    @Test
    @Category({ NoEclipselink.class})
    public void testSubqueryBatchedCorrelationNormalSize20() {
        testCorrelation(DocumentEmbeddingCorrelationViewSubqueryNormal.class, 20);
    }

    @Test
    @Category({ NoEclipselink.class})
    public void testSubqueryBatchedCorrelationIdSize20() {
        testCorrelation(DocumentEmbeddingCorrelationViewSubqueryId.class, 20);
    }

    // TODO: test batch correlation expectation configuration
    // TODO: make explicit test for correlation key batching with view root usage maybe via nested subviews through collections?

    @Test
    public void testSubselectCorrelationNormal() {
        testCorrelation(DocumentEmbeddingCorrelationViewSubselectNormal.class, null);
    }

    @Test
    public void testSubselectCorrelationId() {
        testCorrelation(DocumentEmbeddingCorrelationViewSubselectId.class, null);
    }

    @Test
    // NOTE: Eclipselink renders a cross join at the wrong position in the SQL
    @Category({ NoEclipselink.class })
    public void testJoinCorrelationNormal() {
        testCorrelation(DocumentEmbeddingCorrelationViewJoinNormal.class, null);
    }

    @Test
    // NOTE: Eclipselink renders a cross join at the wrong position in the SQL
    @Category({ NoEclipselink.class })
    public void testJoinCorrelationId() {
        testCorrelation(DocumentEmbeddingCorrelationViewJoinId.class, null);
    }

    protected Document doc1;

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                doc1 = new Document("doc1");
                Document doc2 = new Document("doc2");

                Person o1 = new Person("pers1");
                Person o2 = new Person("pers2");

                doc1.setOwner(o1);
                doc2.setOwner(o2);

                em.persist(o2);
                em.persist(doc2);

                em.persist(o1);
                em.persist(doc1);

                o1.setPartnerDocument(doc2);
                o2.setPartnerDocument(doc1);
            }
        });
    }

    @Before
    public void setUp() {
        doc1 = cbf.create(em, Document.class).where("name").eq("doc1").fetch("owner.partnerDocument.owner").getResultList().get(0);
    }

    protected <T extends DocumentEmbeddingCorrelationView> void testCorrelation(Class<T> entityView, Integer batchSize) {
        EntityViewManager evm = build(
                entityView,
                SimpleDocumentEmbeddingCorrelatedView.class,
                SimplePersonEmbeddingCorrelatedSubView.class
        );

        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d").where("id").eq(doc1.getId());
        EntityViewSetting<T, CriteriaBuilder<T>> setting = EntityViewSetting.create(entityView);
        if (batchSize != null) {
            setting.setProperty(ConfigurationProperties.DEFAULT_BATCH_SIZE + ".ownerRelatedDocumentIds", batchSize);
        }
        CriteriaBuilder<T> cb = evm.applySetting(setting, criteria);
        List<T> results = cb.getResultList();

        assertEquals(1, results.size());

        // Doc1
        assertEquals(doc1.getName(), results.get(0).getName());
        assertEquals(1, results.get(0).getOwnerRelatedDocumentIds().size());
        assertEquals(1, results.get(0).getOwnerRelatedDocuments().size());
        assertEquals(1, results.get(0).getOwnerRelatedDocumentViews().size());

        SimpleDocumentEmbeddingCorrelatedView view = results.get(0).getOwnerRelatedDocumentViews().iterator().next();
        assertEquals("doc2", view.getName());
        assertEquals("PERS1", view.getOwner().getName());
        assertEquals("doc1", view.getOwner().getPartnerDocumentName());
        assertEquals(1, view.getPartners().size());
        assertEquals("PERS1", view.getPartners().iterator().next().getName());
        assertEquals("doc1", view.getPartners().iterator().next().getPartnerDocumentName());
    }

}
