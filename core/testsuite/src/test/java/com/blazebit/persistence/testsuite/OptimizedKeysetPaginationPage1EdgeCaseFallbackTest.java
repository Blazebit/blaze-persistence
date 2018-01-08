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

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.impl.ConfigurationProperties;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.DocumentWithNullableName;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.Version;
import com.blazebit.persistence.testsuite.entity.Workflow;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class OptimizedKeysetPaginationPage1EdgeCaseFallbackTest extends AbstractCoreTest {

    private Person o4;

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                DocumentWithNullableName doc1 = new DocumentWithNullableName("doc1");
                DocumentWithNullableName doc2 = new DocumentWithNullableName("doc2");
                DocumentWithNullableName doc3 = new DocumentWithNullableName("doc3");
                DocumentWithNullableName doc4 = new DocumentWithNullableName("doc4");

                Person o1 = new Person("Karl1");
                Person o2 = new Person("Karl2");
                Person o3 = new Person("Karl3");
                o4 = new Person("Karl4");

                doc1.setOwner(o1);
                doc2.setOwner(o2);
                doc3.setOwner(o3);
                doc4.setOwner(o4);

                em.persist(o1);
                em.persist(o2);
                em.persist(o3);
                em.persist(o4);

                em.persist(doc1);
                em.persist(doc2);
                em.persist(doc3);
                em.persist(doc4);
            }
        });
    }

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
                Document.class,
                Version.class,
                Person.class,
                Workflow.class,
                IntIdEntity.class,
                DocumentWithNullableName.class
        };
    }

    @Override
    protected CriteriaBuilderConfiguration configure(CriteriaBuilderConfiguration config) {
        config = super.configure(config);
        config.setProperty(ConfigurationProperties.OPTIMIZED_KEYSET_PREDICATE_RENDERING, "true");
        return config;
    }

    @Test
    public void keysetPaginationPage1EdgeCaseFallback() {
        CriteriaBuilder<Tuple> crit = cbf.create(em, Tuple.class).from(DocumentWithNullableName.class, "d")
                .select("d.name").select("d.owner.name");
        crit.orderByDesc("d.owner.name")
                .orderByDesc("d.name")
                .orderByAsc("d.id");
        /* query yields the following order:
         *  - doc4
         *  - doc3
         *  - doc2
         *  - doc1
         */

        PaginatedCriteriaBuilder<Tuple> pcb = crit.page(null, 0, 2);
        PagedList<Tuple> result = pcb.getResultList();

        // scroll forward
        result = crit.page(result.getKeysetPage(), 2, 2).getResultList();

        // prepend element
        em.persist(new DocumentWithNullableName("doc5", em.getReference(Person.class, o4.getId())));
        em.flush();

        // scroll backwards
        result = crit.page(result.getKeysetPage(), 0, 2).getResultList();

        assertNotNull(result.getKeysetPage());
        assertEquals(2, result.getSize());
        assertEquals("doc5", result.get(0).get(0));
        assertEquals("doc4", result.get(1).get(0));
    }
}
