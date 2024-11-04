/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.fetch.subview;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.Sorters;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.fetch.subview.model.DocumentSelectSubviewTestView;
import com.blazebit.persistence.view.testsuite.fetch.subview.model.DocumentSubselectSubviewTestView;
import com.blazebit.persistence.view.testsuite.fetch.subview.model.PersonSelectSubview;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SubviewFetchTest extends AbstractEntityViewTest {

    protected Document doc1;

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                doc1 = new Document("doc1");

                Person o1 = new Person("pers1");

                doc1.setOwner(o1);

                doc1.getStrings().add("s1");
                doc1.getStrings().add("s2");

                em.persist(o1);

                em.persist(doc1);

                o1.setPartnerDocument(doc1);
            }
        });
    }

    @Before
    public void setUp() {
        doc1 = cbf.create(em, Document.class).where("name").eq("doc1").fetch("partners").getResultList().get(0);
    }

    @Test
    // NOTE: Eclipselink and Datanucleus don't support the single valued id access optimization which causes a cyclic join dependency
    @Category({ NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class })
    public void testSubqueryFetchOptional() {
        EntityViewManager evm = build(
                DocumentSelectSubviewTestView.class,
                PersonSelectSubview.class
        );

        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d").orderByAsc("id");
        EntityViewSetting<DocumentSelectSubviewTestView, CriteriaBuilder<DocumentSelectSubviewTestView>> setting = EntityViewSetting.create(DocumentSelectSubviewTestView.class);
        CriteriaBuilder<DocumentSelectSubviewTestView> cb = evm.applySetting(setting, criteria);
        List<DocumentSelectSubviewTestView> results = cb.getResultList();

        assertEquals(1, results.size());
    }

    @Test
    // NOTE: Eclipselink and Datanucleus don't support the single valued id access optimization which causes a cyclic join dependency
    @Category({ NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class })
    public void testSubselectFetchWithSorter() {
        EntityViewManager evm = build(DocumentSubselectSubviewTestView.class);

        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d").orderByAsc("id");
        EntityViewSetting<DocumentSubselectSubviewTestView, CriteriaBuilder<DocumentSubselectSubviewTestView>> setting = EntityViewSetting.create(DocumentSubselectSubviewTestView.class);
        setting.addAttributeSorter("name", Sorters.ascending());
        CriteriaBuilder<DocumentSubselectSubviewTestView> cb = evm.applySetting(setting, criteria);
        List<DocumentSubselectSubviewTestView> results = cb.getResultList();

        assertEquals(1, results.size());
    }
}
