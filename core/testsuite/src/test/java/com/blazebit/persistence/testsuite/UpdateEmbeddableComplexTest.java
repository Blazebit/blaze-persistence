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

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoFirebird;
import com.blazebit.persistence.testsuite.base.jpa.category.NoH2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOracle;
import com.blazebit.persistence.testsuite.base.jpa.category.NoSQLite;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntity;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityEmbeddable;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityId;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;

import javax.persistence.EntityManager;

/**
 * This kind of mapping is not required to be supported by a JPA implementation.
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.1.0
 */
public class UpdateEmbeddableComplexTest extends AbstractCoreTest {

    Document doc1;
    Document doc2;
    Document doc3;

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
            IntIdEntity.class,
            EmbeddableTestEntity.class,
            EmbeddableTestEntityEmbeddable.class,
            EmbeddableTestEntityId.class
        };
    }
    
    @Test
    // NOTE: EclipseLink doesn't support Map in embeddables: https://bugs.eclipse.org/bugs/show_bug.cgi?id=391062
    // TODO: report that datanucleus doesn't support element collection in an embeddable
    @Category({ NoH2.class, NoOracle.class, NoSQLite.class, NoFirebird.class, NoMySQL.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testUpdateWithReturningEmbeddable(){
        final String newEmbeddableTestEntityIdKey = "newKey";

        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                EmbeddableTestEntityId embeddable1Id = new EmbeddableTestEntityId("1", "oldKey");
                EmbeddableTestEntity embeddable1 = new EmbeddableTestEntity();
                embeddable1.setId(embeddable1Id);
                em.persist(embeddable1);
                em.flush();
                
                String key = cbf.update(em, EmbeddableTestEntity.class, "e")
                    .set("id.key", newEmbeddableTestEntityIdKey)
                    .executeWithReturning("id.key", String.class)
                    .getLastResult();
                
                assertEquals(newEmbeddableTestEntityIdKey, key);
            }
        });
    }
    
    @Test
    // NOTE: EclipseLink doesn't support Map in embeddables: https://bugs.eclipse.org/bugs/show_bug.cgi?id=391062
    // TODO: report that datanucleus doesn't support element collection in an embeddable
    @Category({ NoH2.class, NoOracle.class, NoSQLite.class, NoFirebird.class, NoMySQL.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testUpdateWithReturningExplicitId(){
        final String intIdEntity1Key = "1";

        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                EmbeddableTestEntityId embeddable2Id = new EmbeddableTestEntityId("1", "2");
                EmbeddableTestEntity embeddable2 = new EmbeddableTestEntity();
                
                embeddable2.setId(embeddable2Id);
                em.persist(embeddable2);
                
                EmbeddableTestEntityId embeddable1Id = new EmbeddableTestEntityId("1", intIdEntity1Key);
                EmbeddableTestEntity embeddable1 = new EmbeddableTestEntity();
                embeddable1.setId(embeddable1Id);
                EmbeddableTestEntityEmbeddable embeddable1Embeddable = new EmbeddableTestEntityEmbeddable();
                embeddable1Embeddable.setManyToOne(embeddable2);
                embeddable1.setEmbeddable(embeddable1Embeddable);
                em.persist(embeddable1);
                em.flush();

                EmbeddableTestEntityId manyToOneId = cbf.update(em, EmbeddableTestEntity.class, "e")
                    .set("id.key", "newKey")
                    .where("e.id.key").eq(intIdEntity1Key)
                    .executeWithReturning("embeddable.manyToOne.id", EmbeddableTestEntityId.class)
                    .getLastResult();
                assertEquals(embeddable2Id, manyToOneId);
            }
        });
    }
}
