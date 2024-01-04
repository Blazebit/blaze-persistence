/*
 * Copyright 2014 - 2024 Blazebit.
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

package com.blazebit.persistence.view.testsuite.pagination;

import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.Sorters;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.basic.model.IdHolderView;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.6.10
 */
public class PaginationTest extends AbstractEntityViewTest {

    protected EntityViewManager evm;

    @Before
    public void initEvm() {
        evm = build(DocumentViewInterface.class, PersonIdView.class);
    }
    
    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                doc1 = new Document("doc1");
                Person o1 = new Person("pers1");

                doc1.setAge(10);
                doc1.setOwner(o1);

                doc1.getContacts().put(1, o1);
                doc1.getContacts2().put(2, o1);

                em.persist(o1);
                em.persist(doc1);
                em.persist(new Document("doc2", o1));
            }
        });
    }

    @Before
    public void setUp() {
        doc1 = cbf.create(em, Document.class).where("name").eq("doc1").getSingleResult();
    }
    
    private Document doc1;
    
    @Test
    @Category({ NoEclipselink.class, NoDatanucleus.class, NoOpenJPA.class })
    // TODO: report eclipselink does not support subqueries in functions
    public void testPagination() {
        EntityViewSetting<DocumentViewInterface, PaginatedCriteriaBuilder<DocumentViewInterface>> settings = EntityViewSetting.create(DocumentViewInterface.class, 0, 1)
            .withAttributeSorter("number", Sorters.ascending())
            .withAttributeSorter("id", Sorters.ascending());
        List<DocumentViewInterface> page = evm.applySetting(settings, cbf.create(em, Document.class)).getResultList();
        assertEquals(1, page.size());
    }

    @EntityView(Document.class)
    public interface DocumentViewInterface extends IdHolderView<Long> {

        public String getName();

        @Mapping("COALESCE(FUNCTION('cast_long', idx), id)")
        public Long getNumber();

        @Mapping(fetch = FetchStrategy.SUBSELECT)
        Set<PersonIdView> getContacts();

    }
    @EntityView(Person.class)
    public interface PersonIdView extends IdHolderView<Long> {

    }
}
