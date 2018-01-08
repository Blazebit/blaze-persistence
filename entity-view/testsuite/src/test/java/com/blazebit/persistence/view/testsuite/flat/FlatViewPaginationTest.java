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

import javax.persistence.EntityManager;
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
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(PersonFlatView.class);
        evm = cfg.createEntityViewManager(cbf);
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
