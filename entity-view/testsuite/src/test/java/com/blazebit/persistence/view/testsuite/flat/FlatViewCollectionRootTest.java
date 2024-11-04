/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.flat;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.flat.model.NameObjectView;
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
 * @since 1.6.8
 */
public class FlatViewCollectionRootTest extends AbstractEntityViewTest {

    protected EntityViewManager evm;

    @Before
    public void initEvm() {
        evm = build(
                NameObjectView.class
        );
    }

    @Before
    public void setUp() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                Person o1 = new Person("pers1");
                em.persist(o1);

                Document doc1 = new Document("doc1");
                doc1.setOwner(o1);
                em.persist(doc1);
            }
        });
    }

    @Test
    public void queryFlatView() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d")
                .orderByAsc("id");
        CriteriaBuilder<NameObjectView> cb = evm.applySetting(EntityViewSetting.create(NameObjectView.class), criteria, "names");
        List<NameObjectView> results = cb.getResultList();

        assertEquals(0, results.size());
    }

}
