/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.flat;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.metamodel.FlatViewType;
import com.blazebit.persistence.view.metamodel.ViewMetamodel;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.testsuite.flat.model.DocumentFlatEmbeddingView;
import com.blazebit.persistence.view.testsuite.flat.model.PersonFlatView;
import com.blazebit.persistence.view.testsuite.flat.model.UpdatableDocumentFlatView;
import org.junit.Before;
import org.junit.Test;

import jakarta.persistence.EntityManager;
import java.util.List;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class FlatViewPaginationTest extends AbstractEntityViewTest {

    protected EntityViewManager evm;

    @Before
    public void initEvm() {
        evm = build(PersonFlatView.class);
    }

    @Before
    public void setUp() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                for (int i = 1; i <= 10; i++) {
                    Person person = new Person("pers" + i);
                    em.persist(person);
                }
            }
        });
    }

    @Test
    public void paginateFlatView() {
        CriteriaBuilder<Person> criteria = cbf.create(em, Person.class, "p")
                .orderByAsc("id");
        EntityViewSetting<PersonFlatView, PaginatedCriteriaBuilder<PersonFlatView>> setting
                = EntityViewSetting.create(PersonFlatView.class, 1, 1);
        PaginatedCriteriaBuilder<PersonFlatView> cb = evm.applySetting(setting, criteria);
        PagedList<PersonFlatView> results = cb.getResultList();

        assertEquals(1, results.size());

        assertEquals("pers2", results.get(0).getName());
    }

}
