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

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Before;
import org.junit.Test;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.DocumentType;
import com.blazebit.persistence.testsuite.entity.Person;

/**
 *
 * @author Moritz Becker
 */
public class EnumTest extends AbstractCoreTest {

    @Override
    public void setUpOnce(){
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                Document doc1 = new Document("doc1", DocumentType.NOVEL);
                Document doc2 = new Document("Doc2", DocumentType.CONTRACT);

                Person o1 = new Person("Karl1");

                o1.getLocalized().put(1, "abra kadabra");

                doc1.setOwner(o1);
                doc2.setOwner(o1);

                em.persist(o1);

                em.persist(doc1);
                em.persist(doc2);
            }
        });
    }

    @Test
    public void testEnumLiteral(){
        CriteriaBuilder<Document> cb = cbf.create(em, Document.class, "d").where("d.documentType").eqExpression(DocumentType.class.getName() + ".NOVEL");
        String expected = "SELECT d FROM Document d WHERE d.documentType = " + DocumentType.class.getName() + ".NOVEL";
        assertEquals(expected, cb.getQueryString());
        List<Document> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
        assertEquals("doc1", resultList.get(0).getName());
    }
}
