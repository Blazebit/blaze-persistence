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

package com.blazebit.persistence.view.testsuite.correlation.embedding;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus4;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate43;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate50;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.impl.ConfigurationProperties;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class EmbeddingViewRootCorrelationTest extends AbstractEntityViewTest {

    @Test
    // NOTE: Datenucleus issue: https://github.com/datanucleus/datanucleus-api-jpa/issues/77
    @Category({ NoDatanucleus.class })
    public void testSubqueryCorrelationNormal() {
        testCorrelation(DocumentEmbeddingCorrelationViewSubqueryNormal.class, null);
    }

    @Test
    // NOTE: Datenucleus issue: https://github.com/datanucleus/datanucleus-api-jpa/issues/77
    @Category({ NoDatanucleus.class })
    public void testSubqueryCorrelationId() {
        testCorrelation(DocumentEmbeddingCorrelationViewSubqueryId.class, null);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoDatanucleus4.class, NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class})
    public void testSubqueryBatchedCorrelationNormalSize2() {
        testCorrelation(DocumentEmbeddingCorrelationViewSubqueryNormal.class, 2);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoDatanucleus4.class, NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class})
    public void testSubqueryBatchedCorrelationIdSize2() {
        testCorrelation(DocumentEmbeddingCorrelationViewSubqueryId.class, 2);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoDatanucleus4.class, NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class})
    public void testSubqueryBatchedCorrelationNormalSize4() {
        testCorrelation(DocumentEmbeddingCorrelationViewSubqueryNormal.class, 4);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoDatanucleus4.class, NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class})
    public void testSubqueryBatchedCorrelationIdSize4() {
        testCorrelation(DocumentEmbeddingCorrelationViewSubqueryId.class, 4);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class})
    public void testSubqueryBatchedCorrelationNormalSize20() {
        testCorrelation(DocumentEmbeddingCorrelationViewSubqueryNormal.class, 20);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class})
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
    // NOTE: Requires entity joins which are supported since Hibernate 5.1, Datanucleus 5 and latest Eclipselink
    // NOTE: Eclipselink renders a cross join at the wrong position in the SQL
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus4.class, NoOpenJPA.class, NoEclipselink.class })
    public void testJoinCorrelationNormal() {
        testCorrelation(DocumentEmbeddingCorrelationViewJoinNormal.class, null);
    }

    @Test
    // NOTE: Requires entity joins which are supported since Hibernate 5.1, Datanucleus 5 and latest Eclipselink
    // NOTE: Eclipselink renders a cross join at the wrong position in the SQL
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus4.class, NoOpenJPA.class, NoEclipselink.class })
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
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(entityView);
        cfg.addEntityView(SimpleDocumentEmbeddingCorrelatedView.class);
        cfg.addEntityView(SimplePersonEmbeddingCorrelatedSubView.class);
        EntityViewManager evm = cfg.createEntityViewManager(cbf);

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
