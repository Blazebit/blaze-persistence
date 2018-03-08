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

import com.blazebit.persistence.ReturningResult;
import com.blazebit.persistence.UpdateCriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoH2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.entity.IndexedEmbeddable;
import com.blazebit.persistence.testsuite.entity.IndexedNode;
import com.blazebit.persistence.testsuite.entity.KeyedEmbeddable;
import com.blazebit.persistence.testsuite.entity.KeyedNode;
import com.blazebit.persistence.testsuite.entity.Root;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
// NOTE: No advanced sql support for datanucleus, eclipselink and openjpa yet
@Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
public class CollectionRoleUpdateTest extends AbstractCoreTest {

    private static final Integer I2_ID = 4;
    private static final Integer K2_ID = 5;

    @Before
    public void setUp() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                Root r = new Root(1);
                IndexedNode i1 = new IndexedNode(2);
                KeyedNode k1 = new KeyedNode(3);

                r.getIndexedNodes().add(i1);
                r.getIndexedNodesMany().add(i1);
                r.getIndexedNodesManyDuplicate().add(i1);

                r.getIndexedNodesElementCollection().add(new IndexedEmbeddable("a", "b"));

                r.getKeyedNodes().put("a", k1);
                r.getKeyedNodesMany().put("a", k1);
                r.getKeyedNodesManyDuplicate().put("a", k1);

                r.getKeyedNodesElementCollection().put("a", new KeyedEmbeddable("a", "b"));

                em.persist(i1);
                em.persist(k1);
                em.persist(r);

                IndexedNode i2 = new IndexedNode(I2_ID);
                em.persist(i2);
                KeyedNode k2 = new KeyedNode(K2_ID);
                em.persist(k2);
            }
        });
    }

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
            Root.class,
            IndexedNode.class,
            KeyedNode.class,
            KeyedEmbeddable.class,
            IndexedEmbeddable.class
        };
    }

    private Root getRoot(EntityManager em) {
        return cbf.create(em, Root.class)
                .fetch("indexedNodes", "indexedNodesMany", "indexedNodesManyDuplicate", "indexedNodesElementCollection")
                .fetch("keyedNodes", "keyedNodesMany", "keyedNodesManyDuplicate", "keyedNodesElementCollection")
                .where("id").eq(1)
                .getResultList()
                .get(0);
    }

    @Test
    public void updateIndexedAccessOtherAttributes() {
        UpdateCriteriaBuilder<Root> criteria = cbf.updateCollection(em, Root.class, "r", "indexedNodes");
        try {
            criteria.set("name", "BLA");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("Only access to the owner type's id attribute"));
        }
    }

    @Test
    public void updateIndexed() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                UpdateCriteriaBuilder<Root> criteria = cbf.updateCollection(em, Root.class, "r", "indexedNodes");
                criteria.set("INDEX(indexedNodes)", 0);
                criteria.set("indexedNodes.id", I2_ID);
                criteria.where("INDEX(indexedNodes)").eq(0);
                criteria.where("r.id").eq(1);
                criteria.where("r.indexedNodes.id").eq(2);

                assertEquals("UPDATE Root(indexedNodes) r"
                        + " SET INDEX(_collection) = :param_0,_collection.id = :param_1"
                        + " WHERE INDEX(_collection) = :param_2 AND r.id = :param_3 AND _collection.id = :param_4", criteria.getQueryString());
                int updated = criteria.executeUpdate();
                Root r = getRoot(em);

                assertEquals(1, updated);
                assertEquals(1, r.getIndexedNodes().size());
                assertEquals(1, r.getIndexedNodesMany().size());
                assertEquals(1, r.getIndexedNodesManyDuplicate().size());
                assertEquals(1, r.getIndexedNodesElementCollection().size());

                assertEquals(I2_ID, r.getIndexedNodes().get(0).getId());
            }
        });
    }

    // NOTE: H2 and MySQL only support returning generated keys
    @Test
    @Category({ NoH2.class, NoMySQL.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void updateIndexedReturning() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                UpdateCriteriaBuilder<Root> criteria = cbf.updateCollection(em, Root.class, "r", "indexedNodes");
                criteria.set("INDEX(indexedNodes)", 0);
                criteria.set("indexedNodes.id", I2_ID);
                criteria.where("INDEX(indexedNodes)").eq(0);
                criteria.where("r.id").eq(1);

                assertEquals("UPDATE Root(indexedNodes) r"
                        + " SET INDEX(_collection) = :param_0,_collection.id = :param_1"
                        + " WHERE INDEX(_collection) = :param_2 AND r.id = :param_3", criteria.getQueryString());
                ReturningResult<Tuple> returningResult = criteria.executeWithReturning("indexedNodes.id");
                Root r = getRoot(em);

                assertEquals(I2_ID, returningResult.getLastResult().get(0));
                assertEquals(1, returningResult.getUpdateCount());
                assertEquals(1, r.getIndexedNodes().size());
                assertEquals(1, r.getIndexedNodesMany().size());
                assertEquals(1, r.getIndexedNodesManyDuplicate().size());
                assertEquals(1, r.getIndexedNodesElementCollection().size());

                assertEquals(I2_ID, r.getIndexedNodes().get(0).getId());
            }
        });
    }

    @Test
    public void updateIndexedMany() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                UpdateCriteriaBuilder<Root> criteria = cbf.updateCollection(em, Root.class, "r", "indexedNodesMany");
                criteria.set("INDEX(indexedNodesMany)", 0);
                criteria.set("indexedNodesMany.id", I2_ID);
                criteria.where("INDEX(indexedNodesMany)").eq(0);
                criteria.where("r.id").eq(1);
                criteria.where("r.indexedNodesMany.id").eq(2);

                assertEquals("UPDATE Root(indexedNodesMany) r"
                        + " SET INDEX(_collection) = :param_0,_collection.id = :param_1"
                        + " WHERE INDEX(_collection) = :param_2 AND r.id = :param_3 AND _collection.id = :param_4", criteria.getQueryString());
                int updated = criteria.executeUpdate();
                Root r = getRoot(em);

                assertEquals(1, updated);
                assertEquals(1, r.getIndexedNodes().size());
                assertEquals(1, r.getIndexedNodesMany().size());
                assertEquals(1, r.getIndexedNodesManyDuplicate().size());
                assertEquals(1, r.getIndexedNodesElementCollection().size());

                assertEquals(I2_ID, r.getIndexedNodesMany().get(0).getId());
            }
        });
    }

    @Test
    public void updateIndexedManyDuplicate() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                UpdateCriteriaBuilder<Root> criteria = cbf.updateCollection(em, Root.class, "r", "indexedNodesManyDuplicate");
                criteria.set("INDEX(indexedNodesManyDuplicate)", 0);
                criteria.set("indexedNodesManyDuplicate.id", I2_ID);
                criteria.where("INDEX(indexedNodesManyDuplicate)").eq(0);
                criteria.where("r.id").eq(1);
                criteria.where("r.indexedNodesManyDuplicate.id").eq(2);

                assertEquals("UPDATE Root(indexedNodesManyDuplicate) r"
                        + " SET INDEX(_collection) = :param_0,_collection.id = :param_1"
                        + " WHERE INDEX(_collection) = :param_2 AND r.id = :param_3 AND _collection.id = :param_4", criteria.getQueryString());
                int updated = criteria.executeUpdate();
                Root r = getRoot(em);

                assertEquals(1, updated);
                assertEquals(1, r.getIndexedNodes().size());
                assertEquals(1, r.getIndexedNodesMany().size());
                assertEquals(1, r.getIndexedNodesManyDuplicate().size());
                assertEquals(1, r.getIndexedNodesElementCollection().size());

                assertEquals(I2_ID, r.getIndexedNodesManyDuplicate().get(0).getId());
            }
        });
    }

    @Test
    public void updateIndexedElementCollection() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                UpdateCriteriaBuilder<Root> criteria = cbf.updateCollection(em, Root.class, "r", "indexedNodesElementCollection");
                criteria.set("INDEX(indexedNodesElementCollection)", 0);
                criteria.set("indexedNodesElementCollection.value", "B");
                criteria.set("indexedNodesElementCollection.value2", "P");
                criteria.where("INDEX(indexedNodesElementCollection)").eq(0);
                criteria.where("r.id").eq(1);
                criteria.where("r.indexedNodesElementCollection.value").eq("a");
                criteria.where("r.indexedNodesElementCollection.value2").eq("b");

                assertEquals("UPDATE Root(indexedNodesElementCollection) r"
                        + " SET INDEX(_collection) = :param_0,_collection.value = :param_1,_collection.value2 = :param_2"
                        + " WHERE INDEX(_collection) = :param_3 AND r.id = :param_4 AND _collection.value = :param_5 AND _collection.value2 = :param_6", criteria.getQueryString());
                int updated = criteria.executeUpdate();
                Root r = getRoot(em);

                assertEquals(1, updated);
                assertEquals(1, r.getIndexedNodes().size());
                assertEquals(1, r.getIndexedNodesMany().size());
                assertEquals(1, r.getIndexedNodesManyDuplicate().size());
                assertEquals(1, r.getIndexedNodesElementCollection().size());

                assertEquals("B", r.getIndexedNodesElementCollection().get(0).getValue());
                assertEquals("P", r.getIndexedNodesElementCollection().get(0).getValue2());
            }
        });
    }

    @Test
    public void updateKeyed() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                UpdateCriteriaBuilder<Root> criteria = cbf.updateCollection(em, Root.class, "r", "keyedNodes");
                criteria.set("KEY(keyedNodes)", "b");
                criteria.set("keyedNodes.id", K2_ID);
                criteria.where("KEY(keyedNodes)").eq("a");
                criteria.where("r.id").eq(1);
                criteria.where("r.keyedNodes.id").eq(3);

                assertEquals("UPDATE Root(keyedNodes) r"
                        + " SET KEY(_collection) = :param_0,_collection.id = :param_1"
                        + " WHERE KEY(_collection) = :param_2 AND r.id = :param_3 AND _collection.id = :param_4", criteria.getQueryString());
                int updated = criteria.executeUpdate();
                Root r = getRoot(em);

                assertEquals(1, updated);
                assertEquals(1, r.getKeyedNodes().size());
                assertEquals(1, r.getKeyedNodesMany().size());
                assertEquals(1, r.getKeyedNodesManyDuplicate().size());
                assertEquals(1, r.getKeyedNodesElementCollection().size());

                assertEquals(K2_ID, r.getKeyedNodes().get("b").getId());
            }
        });
    }

    @Test
    public void updateKeyedMany() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                UpdateCriteriaBuilder<Root> criteria = cbf.updateCollection(em, Root.class, "r", "keyedNodesMany");
                criteria.set("KEY(keyedNodesMany)", "b");
                criteria.set("keyedNodesMany.id", K2_ID);
                criteria.where("KEY(keyedNodesMany)").eq("a");
                criteria.where("r.id").eq(1);
                criteria.where("r.keyedNodesMany.id").eq(3);

                assertEquals("UPDATE Root(keyedNodesMany) r"
                        + " SET KEY(_collection) = :param_0,_collection.id = :param_1"
                        + " WHERE KEY(_collection) = :param_2 AND r.id = :param_3 AND _collection.id = :param_4", criteria.getQueryString());
                int updated = criteria.executeUpdate();
                Root r = getRoot(em);

                assertEquals(1, updated);
                assertEquals(1, r.getKeyedNodes().size());
                assertEquals(1, r.getKeyedNodesMany().size());
                assertEquals(1, r.getKeyedNodesManyDuplicate().size());
                assertEquals(1, r.getKeyedNodesElementCollection().size());

                assertEquals(K2_ID, r.getKeyedNodesMany().get("b").getId());
            }
        });
    }

    @Test
    public void updateKeyedManyDuplicate() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                UpdateCriteriaBuilder<Root> criteria = cbf.updateCollection(em, Root.class, "r", "keyedNodesManyDuplicate");
                criteria.set("KEY(keyedNodesManyDuplicate)", "b");
                criteria.set("keyedNodesManyDuplicate.id", K2_ID);
                criteria.where("KEY(keyedNodesManyDuplicate)").eq("a");
                criteria.where("r.id").eq(1);
                criteria.where("r.keyedNodesManyDuplicate.id").eq(3);

                assertEquals("UPDATE Root(keyedNodesManyDuplicate) r"
                        + " SET KEY(_collection) = :param_0,_collection.id = :param_1"
                        + " WHERE KEY(_collection) = :param_2 AND r.id = :param_3 AND _collection.id = :param_4", criteria.getQueryString());
                int updated = criteria.executeUpdate();
                Root r = getRoot(em);

                assertEquals(1, updated);
                assertEquals(1, r.getKeyedNodes().size());
                assertEquals(1, r.getKeyedNodesMany().size());
                assertEquals(1, r.getKeyedNodesManyDuplicate().size());
                assertEquals(1, r.getKeyedNodesElementCollection().size());

                assertEquals(K2_ID, r.getKeyedNodesManyDuplicate().get("b").getId());
            }
        });
    }

    @Test
    public void updateKeyedElementCollection() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                UpdateCriteriaBuilder<Root> criteria = cbf.updateCollection(em, Root.class, "r", "keyedNodesElementCollection");
                criteria.set("KEY(keyedNodesElementCollection)", "b");
                criteria.set("keyedNodesElementCollection.value", "B");
                criteria.set("keyedNodesElementCollection.value2", "P");
                criteria.where("KEY(keyedNodesElementCollection)").eq("a");
                criteria.where("r.id").eq(1);
                criteria.where("r.keyedNodesElementCollection.value").eq("a");
                criteria.where("r.keyedNodesElementCollection.value2").eq("b");

                assertEquals("UPDATE Root(keyedNodesElementCollection) r"
                        + " SET KEY(_collection) = :param_0,_collection.value = :param_1,_collection.value2 = :param_2"
                        + " WHERE KEY(_collection) = :param_3 AND r.id = :param_4 AND _collection.value = :param_5 AND _collection.value2 = :param_6", criteria.getQueryString());
                int updated = criteria.executeUpdate();
                Root r = getRoot(em);

                assertEquals(1, updated);
                assertEquals(1, r.getKeyedNodes().size());
                assertEquals(1, r.getKeyedNodesMany().size());
                assertEquals(1, r.getKeyedNodesManyDuplicate().size());
                assertEquals(1, r.getKeyedNodesElementCollection().size());

                assertEquals("B", r.getKeyedNodesElementCollection().get("b").getValue());
                assertEquals("P", r.getKeyedNodesElementCollection().get("b").getValue2());
            }
        });
    }
}
