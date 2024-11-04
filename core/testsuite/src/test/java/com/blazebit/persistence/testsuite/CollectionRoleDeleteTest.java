/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.DeleteCriteriaBuilder;
import com.blazebit.persistence.ReturningResult;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoH2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.entity.IndexedEmbeddable;
import com.blazebit.persistence.testsuite.entity.IndexedNode;
import com.blazebit.persistence.testsuite.entity.KeyedEmbeddable;
import com.blazebit.persistence.testsuite.entity.KeyedNode;
import com.blazebit.persistence.testsuite.entity.Parent;
import com.blazebit.persistence.testsuite.entity.Root;
import com.blazebit.persistence.testsuite.entity.Sub1;
import com.blazebit.persistence.testsuite.entity.Sub1Sub1;
import com.blazebit.persistence.testsuite.entity.Sub1Sub2;
import com.blazebit.persistence.testsuite.entity.Sub2;
import com.blazebit.persistence.testsuite.entity.Sub2Sub1;
import com.blazebit.persistence.testsuite.entity.Sub2Sub2;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Before;
import org.junit.Ignore;
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
public class CollectionRoleDeleteTest extends AbstractCoreTest {

    @Before
    public void setUp() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                Root r = new Root(1, "r");
                IndexedNode i1 = new IndexedNode(2, r);
                KeyedNode k1 = new KeyedNode(3, r);

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
            IndexedEmbeddable.class,
            Parent.class,
            Sub1.class,
            Sub2.class,
            Sub1Sub1.class,
            Sub1Sub2.class,
            Sub2Sub1.class,
            Sub2Sub2.class
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
    public void deleteIndexed() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                DeleteCriteriaBuilder<Root> criteria = cbf.deleteCollection(em, Root.class, "r", "indexedNodes");
                criteria.where("INDEX(indexedNodes)").eq(0);
                criteria.where("r.id").eq(1);
                criteria.where("r.indexedNodes.id").eq(2);

                assertEquals("DELETE FROM Root(indexedNodes) r"
                        + " WHERE INDEX(r.indexedNodes) = :param_0 AND r.id = :param_1 AND r.indexedNodes.id = :param_2", criteria.getQueryString());
                int updated = criteria.executeUpdate();
                Root r = getRoot(em);

                assertEquals(1, updated);
                assertEquals(0, r.getIndexedNodes().size());
                assertEquals(1, r.getIndexedNodesMany().size());
                assertEquals(1, r.getIndexedNodesManyDuplicate().size());
                assertEquals(1, r.getIndexedNodesElementCollection().size());
            }
        });
    }

    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void deleteIndexedImplicitJoin() {
        DeleteCriteriaBuilder<Root> criteria = cbf.deleteCollection(em, Root.class, "r", "indexedNodes");
        criteria.where("r.name").eq("r");

        assertEquals("DELETE FROM Root(indexedNodes) r WHERE r.name = :param_0", criteria.getQueryString());
        int updated = criteria.executeUpdate();
        Root r = getRoot(em);

        assertEquals(1, updated);
        assertEquals(0, r.getIndexedNodes().size());
        assertEquals(1, r.getIndexedNodesMany().size());
        assertEquals(1, r.getIndexedNodesManyDuplicate().size());
        assertEquals(1, r.getIndexedNodesElementCollection().size());
    }

    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void deleteIndexedMultipleDeepImplicitJoin() {
        DeleteCriteriaBuilder<Root> criteria = cbf.deleteCollection(em, Root.class, "r", "indexedNodes");
        criteria.where("r.name").eq("r");
        criteria.where("r.indexedNodes.parent.name").eq("r");

        assertEquals("DELETE FROM Root(indexedNodes) r USING Root(indexedNodes) r LEFT JOIN r.indexedNodes.parent parent_1 WHERE r.name = :param_0 AND parent_1.name = :param_1", criteria.getQueryString());
        int updated = criteria.executeUpdate();
        Root r = getRoot(em);

        assertEquals(1, updated);
        assertEquals(0, r.getIndexedNodes().size());
        assertEquals(1, r.getIndexedNodesMany().size());
        assertEquals(1, r.getIndexedNodesManyDuplicate().size());
        assertEquals(1, r.getIndexedNodesElementCollection().size());
    }

    // TODO: Add tests for collection functions/predicates SIZE, IS EMPTY and MEMBER OF against a collection of the source "r" or target "r.indexedNodes"

    // NOTE: H2 and MySQL only support returning generated keys
    @Test
    @Category({ NoH2.class, NoMySQL.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void deleteIndexedReturning() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                DeleteCriteriaBuilder<Root> criteria = cbf.deleteCollection(em, Root.class, "r", "indexedNodes");
                criteria.where("INDEX(indexedNodes)").eq(0);
                criteria.where("r.id").eq(1);

                assertEquals("DELETE FROM Root(indexedNodes) r"
                        + " WHERE INDEX(r.indexedNodes) = :param_0 AND r.id = :param_1", criteria.getQueryString());
                ReturningResult<Tuple> returningResult = criteria.executeWithReturning("indexedNodes.id");
                Root r = getRoot(em);

                assertEquals(2, returningResult.getLastResult().get(0));
                assertEquals(1, returningResult.getUpdateCount());
                assertEquals(0, r.getIndexedNodes().size());
                assertEquals(1, r.getIndexedNodesMany().size());
                assertEquals(1, r.getIndexedNodesManyDuplicate().size());
                assertEquals(1, r.getIndexedNodesElementCollection().size());
            }
        });
    }

    @Test
    public void deleteIndexedSubquery() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                DeleteCriteriaBuilder<Root> criteria = cbf.deleteCollection(em, Root.class, "r", "indexedNodes");
                criteria.whereExists()
                        .from(Root.class, "subRoot")
                        .where("subRoot.id").eqExpression("r.id")
                        .where("subRoot.name").eq("r")
                        .where("INDEX(r.indexedNodes)").eq(0)
                        .where("r.id").eq(1)
                        .where("r.indexedNodes.id").eq(2)
                    .end();

                assertEquals("DELETE FROM Root(indexedNodes) r"
                        + " WHERE EXISTS (SELECT 1 FROM Root subRoot WHERE subRoot.id = r.id AND subRoot.name = :param_0 AND INDEX(r.indexedNodes) = :param_1 AND r.id = :param_2 AND r.indexedNodes.id = :param_3)", criteria.getQueryString());
                int updated = criteria.executeUpdate();
                Root r = getRoot(em);

                assertEquals(1, updated);
                assertEquals(0, r.getIndexedNodes().size());
                assertEquals(1, r.getIndexedNodesMany().size());
                assertEquals(1, r.getIndexedNodesManyDuplicate().size());
                assertEquals(1, r.getIndexedNodesElementCollection().size());
            }
        });
    }

    @Test
    public void deleteIndexedMany() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                DeleteCriteriaBuilder<Root> criteria = cbf.deleteCollection(em, Root.class, "r", "indexedNodesMany");
                criteria.where("INDEX(indexedNodesMany)").eq(0);
                criteria.where("r.id").eq(1);
                criteria.where("r.indexedNodesMany.id").eq(2);

                assertEquals("DELETE FROM Root(indexedNodesMany) r"
                        + " WHERE INDEX(r.indexedNodesMany) = :param_0 AND r.id = :param_1 AND r.indexedNodesMany.id = :param_2", criteria.getQueryString());
                int updated = criteria.executeUpdate();
                Root r = getRoot(em);

                assertEquals(1, updated);
                assertEquals(1, r.getIndexedNodes().size());
                assertEquals(0, r.getIndexedNodesMany().size());
                assertEquals(1, r.getIndexedNodesManyDuplicate().size());
                assertEquals(1, r.getIndexedNodesElementCollection().size());
            }
        });
    }

    @Test
    public void deleteIndexedManyDuplicate() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                DeleteCriteriaBuilder<Root> criteria = cbf.deleteCollection(em, Root.class, "r", "indexedNodesManyDuplicate");
                criteria.where("INDEX(indexedNodesManyDuplicate)").eq(0);
                criteria.where("r.id").eq(1);
                criteria.where("r.indexedNodesManyDuplicate.id").eq(2);

                assertEquals("DELETE FROM Root(indexedNodesManyDuplicate) r"
                        + " WHERE INDEX(r.indexedNodesManyDuplicate) = :param_0 AND r.id = :param_1 AND r.indexedNodesManyDuplicate.id = :param_2", criteria.getQueryString());
                int updated = criteria.executeUpdate();
                Root r = getRoot(em);

                assertEquals(1, updated);
                assertEquals(1, r.getIndexedNodes().size());
                assertEquals(1, r.getIndexedNodesMany().size());
                assertEquals(0, r.getIndexedNodesManyDuplicate().size());
                assertEquals(1, r.getIndexedNodesElementCollection().size());
            }
        });
    }

    @Test
    public void deleteIndexedElementCollection() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                DeleteCriteriaBuilder<Root> criteria = cbf.deleteCollection(em, Root.class, "r", "indexedNodesElementCollection");
                criteria.where("INDEX(indexedNodesElementCollection)").eq(0);
                criteria.where("r.id").eq(1);
                criteria.where("r.indexedNodesElementCollection.value").eq("a");
                criteria.where("r.indexedNodesElementCollection.value2").eq("b");

                assertEquals("DELETE FROM Root(indexedNodesElementCollection) r"
                        + " WHERE INDEX(r.indexedNodesElementCollection) = :param_0 AND r.id = :param_1 AND r.indexedNodesElementCollection.value = :param_2 AND r.indexedNodesElementCollection.value2 = :param_3", criteria.getQueryString());
                int updated = criteria.executeUpdate();
                Root r = getRoot(em);

                assertEquals(1, updated);
                assertEquals(1, r.getIndexedNodes().size());
                assertEquals(1, r.getIndexedNodesMany().size());
                assertEquals(1, r.getIndexedNodesManyDuplicate().size());
                assertEquals(0, r.getIndexedNodesElementCollection().size());
            }
        });
    }

    @Test
    public void deleteKeyed() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                DeleteCriteriaBuilder<Root> criteria = cbf.deleteCollection(em, Root.class, "r", "keyedNodes");
                criteria.where("KEY(keyedNodes)").eq("a");
                criteria.where("r.id").eq(1);
                criteria.where("r.keyedNodes.id").eq(3);

                assertEquals("DELETE FROM Root(keyedNodes) r"
                        + " WHERE KEY(r.keyedNodes) = :param_0 AND r.id = :param_1 AND r.keyedNodes.id = :param_2", criteria.getQueryString());
                int updated = criteria.executeUpdate();
                Root r = getRoot(em);

                assertEquals(1, updated);
                assertEquals(0, r.getKeyedNodes().size());
                assertEquals(1, r.getKeyedNodesMany().size());
                assertEquals(1, r.getKeyedNodesManyDuplicate().size());
                assertEquals(1, r.getKeyedNodesElementCollection().size());
            }
        });
    }

    @Test
    public void deleteKeyedMany() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                DeleteCriteriaBuilder<Root> criteria = cbf.deleteCollection(em, Root.class, "r", "keyedNodesMany");
                criteria.where("KEY(keyedNodesMany)").eq("a");
                criteria.where("r.id").eq(1);
                criteria.where("r.keyedNodesMany.id").eq(3);

                assertEquals("DELETE FROM Root(keyedNodesMany) r"
                        + " WHERE KEY(r.keyedNodesMany) = :param_0 AND r.id = :param_1 AND r.keyedNodesMany.id = :param_2", criteria.getQueryString());
                int updated = criteria.executeUpdate();
                Root r = getRoot(em);

                assertEquals(1, updated);
                assertEquals(1, r.getKeyedNodes().size());
                assertEquals(0, r.getKeyedNodesMany().size());
                assertEquals(1, r.getKeyedNodesManyDuplicate().size());
                assertEquals(1, r.getKeyedNodesElementCollection().size());
            }
        });
    }

    @Test
    public void deleteKeyedManyDuplicate() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                DeleteCriteriaBuilder<Root> criteria = cbf.deleteCollection(em, Root.class, "r", "keyedNodesManyDuplicate");
                criteria.where("KEY(keyedNodesManyDuplicate)").eq("a");
                criteria.where("r.id").eq(1);
                criteria.where("r.keyedNodesManyDuplicate.id").eq(3);

                assertEquals("DELETE FROM Root(keyedNodesManyDuplicate) r"
                        + " WHERE KEY(r.keyedNodesManyDuplicate) = :param_0 AND r.id = :param_1 AND r.keyedNodesManyDuplicate.id = :param_2", criteria.getQueryString());
                int updated = criteria.executeUpdate();
                Root r = getRoot(em);

                assertEquals(1, updated);
                assertEquals(1, r.getKeyedNodes().size());
                assertEquals(1, r.getKeyedNodesMany().size());
                assertEquals(0, r.getKeyedNodesManyDuplicate().size());
                assertEquals(1, r.getKeyedNodesElementCollection().size());
            }
        });
    }

    @Test
    public void deleteKeyedElementCollection() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                DeleteCriteriaBuilder<Root> criteria = cbf.deleteCollection(em, Root.class, "r", "keyedNodesElementCollection");
                criteria.where("KEY(keyedNodesElementCollection)").eq("a");
                criteria.where("r.id").eq(1);
                criteria.where("r.keyedNodesElementCollection.value").eq("a");
                criteria.where("r.keyedNodesElementCollection.value2").eq("b");

                assertEquals("DELETE FROM Root(keyedNodesElementCollection) r"
                        + " WHERE KEY(r.keyedNodesElementCollection) = :param_0 AND r.id = :param_1 AND r.keyedNodesElementCollection.value = :param_2 AND r.keyedNodesElementCollection.value2 = :param_3", criteria.getQueryString());
                int updated = criteria.executeUpdate();
                Root r = getRoot(em);

                assertEquals(1, updated);
                assertEquals(1, r.getKeyedNodes().size());
                assertEquals(1, r.getKeyedNodesMany().size());
                assertEquals(1, r.getKeyedNodesManyDuplicate().size());
                assertEquals(0, r.getKeyedNodesElementCollection().size());
            }
        });
    }
}
