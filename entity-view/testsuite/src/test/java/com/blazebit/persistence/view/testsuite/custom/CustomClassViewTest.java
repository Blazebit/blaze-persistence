/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.view.testsuite.custom;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.ViewConstructor;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class CustomClassViewTest extends AbstractEntityViewTest {

    protected EntityViewManager evm;

    @Before
    public void initEvm() {
        evm = build(
                CustomDocumentView.class,
                CustomSubDocumentView.class
        );
    }

    @EntityView(Document.class)
    static class CustomDocumentView {

        private final Long id;
        private final String name;
        private int idx;

        public CustomDocumentView(@Mapping("name") String name, @IdMapping("id") Long id, int idx) {
            this.id = id;
            this.name = name;
            setIdx(idx);
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getIdx() {
            return idx;
        }

        private void setIdx(int idx) {
            this.idx = idx;
        }
    }

    @EntityView(Document.class)
    static class CustomSubDocumentView extends CustomDocumentView {

        @Mapping("age")
        private final long age;

        public CustomSubDocumentView(String name, Long id, long age) {
            super(name, id, 0);
            this.age = age;
        }

        @ViewConstructor("init")
        public CustomSubDocumentView(@IdMapping("id") Long id, @Mapping("name") String name, int idx, long age) {
            super(name, id, idx);
            this.age = age;
        }

        public long age() {
            return age;
        }
    }

    private Document doc1;
    private Document doc2;

    @Before
    public void setUp() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                doc1 = new Document("doc1", 5L);
                doc2 = new Document("doc2", 10L);

                Person o1 = new Person("pers1");
                Person o2 = new Person("pers2");
                o1.getLocalized().put(1, "localized1");
                o2.getLocalized().put(1, "localized2");
                o1.setPartnerDocument(doc1);
                o2.setPartnerDocument(doc2);

                doc1.setOwner(o1);
                doc2.setOwner(o2);

                doc1.getContacts().put(1, o1);
                doc2.getContacts().put(1, o2);

                doc1.getContacts2().put(2, o1);
                doc2.getContacts2().put(2, o2);

                em.persist(o1);
                em.persist(o2);

                em.persist(doc1);
                em.persist(doc2);
            }
        });

        doc1 = em.find(Document.class, doc1.getId());
        doc2 = em.find(Document.class, doc2.getId());
    }

    @Test
    public void testCustomClass() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d")
            .orderByAsc("id");
        CriteriaBuilder<CustomDocumentView> cb = evm.applySetting(EntityViewSetting.create(CustomDocumentView.class), criteria);
        List<CustomDocumentView> results = cb.getResultList();

        assertEquals(2, results.size());
        // Doc1
        assertEquals(doc1.getId(), results.get(0).getId());
        assertEquals(doc1.getName(), results.get(0).getName());
        // Doc2
        assertEquals(doc2.getId(), results.get(1).getId());
        assertEquals(doc2.getName(), results.get(1).getName());
    }

    @Test
    public void testCustomSubClass() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d")
                .orderByAsc("id");
        CriteriaBuilder<CustomSubDocumentView> cb = evm.applySetting(EntityViewSetting.create(CustomSubDocumentView.class), criteria);
        List<CustomSubDocumentView> results = cb.getResultList();

        assertEquals(2, results.size());
        // Doc1
        assertEquals(doc1.getId(), results.get(0).getId());
        assertEquals(doc1.getName(), results.get(0).getName());
        assertEquals(doc1.getAge(), results.get(0).age());
        // Doc2
        assertEquals(doc2.getId(), results.get(1).getId());
        assertEquals(doc2.getName(), results.get(1).getName());
        assertEquals(doc2.getAge(), results.get(1).age());
    }
}
