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

import com.blazebit.persistence.InsertCriteriaBuilder;
import com.blazebit.persistence.ReturningResult;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoH2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOracle;
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

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
// NOTE: No advanced sql support for datanucleus, eclipselink and openjpa yet
@Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
public class CollectionRoleInsertTest extends AbstractCoreTest {

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
    public void insertIndexedAccessOtherAttributes() {
        InsertCriteriaBuilder<Root> criteria = cbf.insertCollection(em, Root.class, "indexedNodes");
        try {
            criteria.bind("name");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("Only access to the owner type's id attribute"));
        }
    }

    @Test
    public void insertIndexed() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                InsertCriteriaBuilder<Root> criteria = cbf.insertCollection(em, Root.class, "indexedNodes");
                criteria.fromValues(Integer.class, "valuesAlias", Collections.singletonList(0));
                criteria.bind("id").select("1");
                criteria.bind("INDEX(indexedNodes)").select("1");
                criteria.bind("indexedNodes.id").select("4");

                assertEquals("INSERT INTO Root.indexedNodes(INDEX(_collection), _collection.id, root.id)\n"
                        + "SELECT 1, 4, 1"
                        + " FROM Integer(1 VALUES) valuesAlias"
                        + " WHERE TREAT_INTEGER(valuesAlias.value) = :valuesAlias_value_0", criteria.getQueryString());
                int updated = criteria.executeUpdate();
                Root r = getRoot(em);

                assertEquals(1, updated);
                assertEquals(2, r.getIndexedNodes().size());
                assertEquals(1, r.getIndexedNodesMany().size());
                assertEquals(1, r.getIndexedNodesManyDuplicate().size());
                assertEquals(1, r.getIndexedNodesElementCollection().size());

                assertEquals(I2_ID, r.getIndexedNodes().get(1).getId());
            }
        });
    }

    // NOTE: H2 and MySQL only support returning generated keys
    // NOTE: Oracle doesn't support the RETURNING clause for INSERT .. SELECT statements, only for INSERT .. VALUES
    // https://stackoverflow.com/questions/5325033/plsql-insert-into-with-subquery-and-returning-clause-oracle
    @Test
    @Category({ NoH2.class, NoMySQL.class, NoOracle.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void insertIndexedReturning() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                InsertCriteriaBuilder<Root> criteria = cbf.insertCollection(em, Root.class, "indexedNodes");
                criteria.fromValues(Integer.class, "valuesAlias", Collections.singletonList(0));
                criteria.bind("id").select("1");
                criteria.bind("INDEX(indexedNodes)").select("1");
                criteria.bind("indexedNodes.id").select("4");

                assertEquals("INSERT INTO Root.indexedNodes(INDEX(_collection), _collection.id, root.id)\n"
                        + "SELECT 1, 4, 1"
                        + " FROM Integer(1 VALUES) valuesAlias"
                        + " WHERE TREAT_INTEGER(valuesAlias.value) = :valuesAlias_value_0", criteria.getQueryString());
                ReturningResult<Tuple> returningResult = criteria.executeWithReturning("indexedNodes.id");
                Root r = getRoot(em);

                assertEquals(I2_ID, returningResult.getLastResult().get(0));
                assertEquals(1, returningResult.getUpdateCount());
                assertEquals(2, r.getIndexedNodes().size());
                assertEquals(1, r.getIndexedNodesMany().size());
                assertEquals(1, r.getIndexedNodesManyDuplicate().size());
                assertEquals(1, r.getIndexedNodesElementCollection().size());

                assertEquals(I2_ID, r.getIndexedNodes().get(1).getId());
            }
        });
    }

    @Test
    public void insertIndexedMany() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                InsertCriteriaBuilder<Root> criteria = cbf.insertCollection(em, Root.class, "indexedNodesMany");
                criteria.fromValues(Integer.class, "valuesAlias", Collections.singletonList(0));
                criteria.bind("id").select("1");
                criteria.bind("INDEX(indexedNodesMany)").select("1");
                criteria.bind("indexedNodesMany.id").select("4");

                assertEquals("INSERT INTO Root.indexedNodesMany(INDEX(_collection), _collection.id, root.id)\n"
                        + "SELECT 1, 4, 1"
                        + " FROM Integer(1 VALUES) valuesAlias"
                        + " WHERE TREAT_INTEGER(valuesAlias.value) = :valuesAlias_value_0", criteria.getQueryString());
                int updated = criteria.executeUpdate();
                Root r = getRoot(em);

                assertEquals(1, updated);
                assertEquals(1, r.getIndexedNodes().size());
                assertEquals(2, r.getIndexedNodesMany().size());
                assertEquals(1, r.getIndexedNodesManyDuplicate().size());
                assertEquals(1, r.getIndexedNodesElementCollection().size());

                assertEquals(I2_ID, r.getIndexedNodesMany().get(1).getId());
            }
        });
    }

    @Test
    public void insertIndexedManyDuplicate() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                InsertCriteriaBuilder<Root> criteria = cbf.insertCollection(em, Root.class, "indexedNodesManyDuplicate");
                criteria.fromValues(Integer.class, "valuesAlias", Collections.singletonList(0));
                criteria.bind("id").select("1");
                criteria.bind("INDEX(indexedNodesManyDuplicate)").select("1");
                criteria.bind("indexedNodesManyDuplicate.id").select("4");

                assertEquals("INSERT INTO Root.indexedNodesManyDuplicate(INDEX(_collection), _collection.id, root.id)\n"
                        + "SELECT 1, 4, 1"
                        + " FROM Integer(1 VALUES) valuesAlias"
                        + " WHERE TREAT_INTEGER(valuesAlias.value) = :valuesAlias_value_0", criteria.getQueryString());
                int updated = criteria.executeUpdate();
                Root r = getRoot(em);

                assertEquals(1, updated);
                assertEquals(1, r.getIndexedNodes().size());
                assertEquals(1, r.getIndexedNodesMany().size());
                assertEquals(2, r.getIndexedNodesManyDuplicate().size());
                assertEquals(1, r.getIndexedNodesElementCollection().size());

                assertEquals(I2_ID, r.getIndexedNodesManyDuplicate().get(1).getId());
            }
        });
    }

    @Test
    public void insertIndexedElementCollection() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                InsertCriteriaBuilder<Root> criteria = cbf.insertCollection(em, Root.class, "indexedNodesElementCollection");
                criteria.fromValues(Integer.class, "valuesAlias", Collections.singletonList(0));
                criteria.bind("id").select("1");
                criteria.bind("INDEX(indexedNodesElementCollection)").select("1");
                criteria.bind("indexedNodesElementCollection.value").select("'B'");
                criteria.bind("indexedNodesElementCollection.value2").select("'P'");

                assertEquals("INSERT INTO Root.indexedNodesElementCollection(INDEX(_collection), _collection.value, _collection.value2, root.id)\n"
                        + "SELECT 1, 'B', 'P', 1"
                        + " FROM Integer(1 VALUES) valuesAlias"
                        + " WHERE TREAT_INTEGER(valuesAlias.value) = :valuesAlias_value_0", criteria.getQueryString());
                int updated = criteria.executeUpdate();
                Root r = getRoot(em);

                assertEquals(1, updated);
                assertEquals(1, r.getIndexedNodes().size());
                assertEquals(1, r.getIndexedNodesMany().size());
                assertEquals(1, r.getIndexedNodesManyDuplicate().size());
                assertEquals(2, r.getIndexedNodesElementCollection().size());

                assertEquals("B", r.getIndexedNodesElementCollection().get(1).getValue());
                assertEquals("P", r.getIndexedNodesElementCollection().get(1).getValue2());
            }
        });
    }

    @Test
    public void insertKeyed() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                InsertCriteriaBuilder<Root> criteria = cbf.insertCollection(em, Root.class, "keyedNodes");
                criteria.fromValues(Integer.class, "valuesAlias", Collections.singletonList(0));
                criteria.bind("id").select("1");
                criteria.bind("KEY(keyedNodes)").select("'b'");
                criteria.bind("keyedNodes.id").select("5");

                assertEquals("INSERT INTO Root.keyedNodes(KEY(_collection), _collection.id, root.id)\n"
                        + "SELECT 'b', 5, 1"
                        + " FROM Integer(1 VALUES) valuesAlias"
                        + " WHERE TREAT_INTEGER(valuesAlias.value) = :valuesAlias_value_0", criteria.getQueryString());
                int updated = criteria.executeUpdate();
                Root r = getRoot(em);

                assertEquals(1, updated);
                assertEquals(2, r.getKeyedNodes().size());
                assertEquals(1, r.getKeyedNodesMany().size());
                assertEquals(1, r.getKeyedNodesManyDuplicate().size());
                assertEquals(1, r.getKeyedNodesElementCollection().size());

                assertEquals(K2_ID, r.getKeyedNodes().get("b").getId());
            }
        });
    }

    @Test
    public void insertKeyedMany() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                InsertCriteriaBuilder<Root> criteria = cbf.insertCollection(em, Root.class, "keyedNodesMany");
                criteria.fromValues(Integer.class, "valuesAlias", Collections.singletonList(0));
                criteria.bind("id").select("1");
                criteria.bind("KEY(keyedNodesMany)").select("'b'");
                criteria.bind("keyedNodesMany.id").select("5");

                assertEquals("INSERT INTO Root.keyedNodesMany(KEY(_collection), _collection.id, root.id)\n"
                        + "SELECT 'b', 5, 1"
                        + " FROM Integer(1 VALUES) valuesAlias"
                        + " WHERE TREAT_INTEGER(valuesAlias.value) = :valuesAlias_value_0", criteria.getQueryString());
                int updated = criteria.executeUpdate();
                Root r = getRoot(em);

                assertEquals(1, updated);
                assertEquals(1, r.getKeyedNodes().size());
                assertEquals(2, r.getKeyedNodesMany().size());
                assertEquals(1, r.getKeyedNodesManyDuplicate().size());
                assertEquals(1, r.getKeyedNodesElementCollection().size());

                assertEquals(K2_ID, r.getKeyedNodesMany().get("b").getId());
            }
        });
    }

    @Test
    public void insertKeyedManyDuplicate() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                InsertCriteriaBuilder<Root> criteria = cbf.insertCollection(em, Root.class, "keyedNodesManyDuplicate");
                criteria.fromValues(Integer.class, "valuesAlias", Collections.singletonList(0));
                criteria.bind("id").select("1");
                criteria.bind("KEY(keyedNodesManyDuplicate)").select("'b'");
                criteria.bind("keyedNodesManyDuplicate.id").select("5");

                assertEquals("INSERT INTO Root.keyedNodesManyDuplicate(KEY(_collection), _collection.id, root.id)\n"
                        + "SELECT 'b', 5, 1"
                        + " FROM Integer(1 VALUES) valuesAlias"
                        + " WHERE TREAT_INTEGER(valuesAlias.value) = :valuesAlias_value_0", criteria.getQueryString());
                int updated = criteria.executeUpdate();
                Root r = getRoot(em);

                assertEquals(1, updated);
                assertEquals(1, r.getKeyedNodes().size());
                assertEquals(1, r.getKeyedNodesMany().size());
                assertEquals(2, r.getKeyedNodesManyDuplicate().size());
                assertEquals(1, r.getKeyedNodesElementCollection().size());

                assertEquals(K2_ID, r.getKeyedNodesManyDuplicate().get("b").getId());
            }
        });
    }

    @Test
    public void insertKeyedElementCollection() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                InsertCriteriaBuilder<Root> criteria = cbf.insertCollection(em, Root.class, "keyedNodesElementCollection");
                criteria.fromValues(Integer.class, "valuesAlias", Collections.singletonList(0));
                criteria.bind("id").select("1");
                criteria.bind("KEY(keyedNodesElementCollection)").select("'b'");
                criteria.bind("keyedNodesElementCollection.value").select("'B'");
                criteria.bind("keyedNodesElementCollection.value2").select("'P'");

                assertEquals("INSERT INTO Root.keyedNodesElementCollection(KEY(_collection), _collection.value, _collection.value2, root.id)\n"
                        + "SELECT 'b', 'B', 'P', 1"
                        + " FROM Integer(1 VALUES) valuesAlias"
                        + " WHERE TREAT_INTEGER(valuesAlias.value) = :valuesAlias_value_0", criteria.getQueryString());
                int updated = criteria.executeUpdate();
                Root r = getRoot(em);

                assertEquals(1, updated);
                assertEquals(1, r.getKeyedNodes().size());
                assertEquals(1, r.getKeyedNodesMany().size());
                assertEquals(1, r.getKeyedNodesManyDuplicate().size());
                assertEquals(2, r.getKeyedNodesElementCollection().size());

                assertEquals("B", r.getKeyedNodesElementCollection().get("b").getValue());
                assertEquals("P", r.getKeyedNodesElementCollection().get("b").getValue2());
            }
        });
    }
}
