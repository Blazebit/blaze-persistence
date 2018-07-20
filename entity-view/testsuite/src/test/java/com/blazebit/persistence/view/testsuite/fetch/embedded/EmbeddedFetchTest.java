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

package com.blazebit.persistence.view.testsuite.fetch.embedded;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus4;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate43;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate50;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate51;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.impl.ConfigurationProperties;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.entity.EmbeddableTestEntity;
import com.blazebit.persistence.view.testsuite.entity.EmbeddableTestEntityEmbeddable;
import com.blazebit.persistence.view.testsuite.entity.EmbeddableTestEntitySimpleEmbeddable;
import com.blazebit.persistence.view.testsuite.fetch.embedded.model.EmbeddableTestEntityEmbeddableFetchSubView;
import com.blazebit.persistence.view.testsuite.fetch.embedded.model.EmbeddableTestEntityFetchAsEntityView;
import com.blazebit.persistence.view.testsuite.fetch.embedded.model.EmbeddableTestEntityFetchAsEntityViewJoin;
import com.blazebit.persistence.view.testsuite.fetch.embedded.model.EmbeddableTestEntityFetchAsEntityViewSubquery;
import com.blazebit.persistence.view.testsuite.fetch.embedded.model.EmbeddableTestEntityFetchAsEntityViewSubselect;
import com.blazebit.persistence.view.testsuite.fetch.embedded.model.EmbeddableTestEntityFetchAsViewView;
import com.blazebit.persistence.view.testsuite.fetch.embedded.model.EmbeddableTestEntityFetchAsViewViewJoin;
import com.blazebit.persistence.view.testsuite.fetch.embedded.model.EmbeddableTestEntityFetchAsViewViewSubquery;
import com.blazebit.persistence.view.testsuite.fetch.embedded.model.EmbeddableTestEntityFetchAsViewViewSubselect;
import com.blazebit.persistence.view.testsuite.fetch.embedded.model.EmbeddableTestEntitySimpleFetchView;
import com.blazebit.persistence.view.testsuite.fetch.embedded.model.IntIdEntityFetchSubView;
import com.blazebit.persistence.view.testsuite.fetch.embedded.model.IntIdEntitySimpleSubView;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Test for #601
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
// NOTE: EclipseLink doesn't support many to one relations in embedded ids
// NOTE: Element collection fetching of non-roots only got fixed in Hibernate 5.2.3: https://hibernate.atlassian.net/browse/HHH-11140
@Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class, NoEclipselink.class })
public class EmbeddedFetchTest extends AbstractEntityViewTest {

    protected EmbeddableTestEntity doc1;
    protected EmbeddableTestEntity doc2;
    protected EmbeddableTestEntity doc3;
    protected EmbeddableTestEntity doc4;

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                IntIdEntity id1 = new IntIdEntity("i1");
                IntIdEntity id2 = new IntIdEntity("i2");
                IntIdEntity id3 = new IntIdEntity("i3");
                IntIdEntity id4 = new IntIdEntity("i4");
                em.persist(id1);
                em.persist(id2);
                em.persist(id3);
                em.persist(id4);
                doc1 = new EmbeddableTestEntity(id1, "doc1");
                doc2 = new EmbeddableTestEntity(id2, "doc2");
                doc3 = new EmbeddableTestEntity(id3, "doc3");
                doc4 = new EmbeddableTestEntity(id4, "doc4");

                doc1.getEmbeddableSet().add(new EmbeddableTestEntitySimpleEmbeddable("doc1"));
                doc2.getEmbeddableSet().add(new EmbeddableTestEntitySimpleEmbeddable("doc2"));
                doc3.getEmbeddableSet().add(new EmbeddableTestEntitySimpleEmbeddable("doc3"));
                doc4.getEmbeddableSet().add(new EmbeddableTestEntitySimpleEmbeddable("doc4"));

                doc1.getEmbeddableMap().put("doc1", new EmbeddableTestEntitySimpleEmbeddable("doc1"));
                doc2.getEmbeddableMap().put("doc2", new EmbeddableTestEntitySimpleEmbeddable("doc2"));
                doc3.getEmbeddableMap().put("doc3", new EmbeddableTestEntitySimpleEmbeddable("doc3"));
                doc4.getEmbeddableMap().put("doc4", new EmbeddableTestEntitySimpleEmbeddable("doc4"));

                em.persist(doc1);
                em.persist(doc2);
                em.persist(doc3);
                em.persist(doc4);

                doc1.getEmbeddable().setManyToOne(doc2);
                doc2.getEmbeddable().setManyToOne(doc2);
                doc3.getEmbeddable().setManyToOne(doc2);
                doc4.getEmbeddable().setManyToOne(doc2);

                doc1.getEmbeddable().getOneToMany().add(doc3);
                doc2.getEmbeddable().getOneToMany().add(doc3);
                doc3.getEmbeddable().getOneToMany().add(doc3);
                doc4.getEmbeddable().getOneToMany().add(doc3);

                doc1.getEmbeddable().getElementCollection().put("doc1", id1);
                doc2.getEmbeddable().getElementCollection().put("doc2", id2);
                doc3.getEmbeddable().getElementCollection().put("doc3", id3);
                doc4.getEmbeddable().getElementCollection().put("doc4", id4);
            }
        });
    }

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
                EmbeddableTestEntity.class,
                EmbeddableTestEntityEmbeddable.class,
                IntIdEntity.class
        };
    }

    @Before
    public void setUp() {
        doc1 = cbf.create(em, EmbeddableTestEntity.class).where("id.key").eq("doc1").fetch("embeddableSet").fetch("embeddableMap")
                .fetch("embeddable.oneToMany").fetch("embeddable.manyToOne").fetch("embeddable.elementCollection")
                .getResultList().get(0);
        doc2 = cbf.create(em, EmbeddableTestEntity.class).where("id.key").eq("doc2").fetch("embeddableSet").fetch("embeddableMap")
                .fetch("embeddable.oneToMany").fetch("embeddable.manyToOne").fetch("embeddable.elementCollection")
                .getResultList().get(0);
        doc3 = cbf.create(em, EmbeddableTestEntity.class).where("id.key").eq("doc3").fetch("embeddableSet").fetch("embeddableMap")
                .fetch("embeddable.oneToMany").fetch("embeddable.manyToOne").fetch("embeddable.elementCollection")
                .getResultList().get(0);
        doc4 = cbf.create(em, EmbeddableTestEntity.class).where("id.key").eq("doc4").fetch("embeddableSet").fetch("embeddableMap")
                .fetch("embeddable.oneToMany").fetch("embeddable.manyToOne").fetch("embeddable.elementCollection")
                .getResultList().get(0);
    }

    @Test
    // NOTE: Datenucleus issue: https://github.com/datanucleus/datanucleus-api-jpa/issues/77
    @Category({ NoDatanucleus.class })
    public void testSubqueryFetchEntity() {
        testCorrelation(EmbeddableTestEntityFetchAsEntityViewSubquery.class);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoDatanucleus4.class, NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class})
    public void testSubqueryBatchedEntitySize2() {
        testCorrelation(EmbeddableTestEntityFetchAsEntityViewSubquery.class, 2);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoDatanucleus4.class, NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class})
    public void testSubqueryBatchedEntitySize4() {
        testCorrelation(EmbeddableTestEntityFetchAsEntityViewSubquery.class, 4);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class})
    public void testSubqueryBatchedEntitySize20() {
        testCorrelation(EmbeddableTestEntityFetchAsEntityViewSubquery.class, 20);
    }

    @Test
    // NOTE: Eclipselink and Datanucleus don't support the single valued id access optimization which causes a cyclic join dependency
    @Category({ NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class })
    public void testSubselectFetchEntity() {
        testCorrelation(EmbeddableTestEntityFetchAsEntityViewSubselect.class);
    }

    @Test
    // NOTE: Requires entity joins which are supported since Hibernate 5.1, Datanucleus 5 and latest Eclipselink
    // NOTE: Eclipselink renders a cross join at the wrong position in the SQL
    // NOTE: Eclipselink and Datanucleus don't support the single valued id access optimization which causes a cyclic join dependency
//    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus4.class, NoOpenJPA.class, NoEclipselink.class })
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class })
    public void testJoinFetchEntity() {
        testCorrelation(EmbeddableTestEntityFetchAsEntityViewJoin.class);
    }

    @Test
    // NOTE: Datenucleus issue: https://github.com/datanucleus/datanucleus-api-jpa/issues/77
    @Category({ NoDatanucleus.class })
    public void testSubqueryFetchView() {
        testCorrelation(EmbeddableTestEntityFetchAsViewViewSubquery.class);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoDatanucleus4.class, NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class})
    public void testSubqueryBatchedViewSize2() {
        testCorrelation(EmbeddableTestEntityFetchAsViewViewSubquery.class, 2);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoDatanucleus4.class, NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class})
    public void testSubqueryBatchedViewSize4() {
        testCorrelation(EmbeddableTestEntityFetchAsViewViewSubquery.class, 4);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class})
    public void testSubqueryBatchedViewSize20() {
        testCorrelation(EmbeddableTestEntityFetchAsViewViewSubquery.class, 20);
    }

    @Test
    // NOTE: Eclipselink and Datanucleus don't support the single valued id access optimization which causes a cyclic join dependency
    @Category({ NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class })
    public void testSubselectFetchView() {
        testCorrelation(EmbeddableTestEntityFetchAsViewViewSubselect.class);
    }

    @Test
    // NOTE: Requires entity joins which are supported since Hibernate 5.1, Datanucleus 5 and latest Eclipselink
    // NOTE: Eclipselink renders a cross join at the wrong position in the SQL
    // NOTE: Eclipselink and Datanucleus don't support the single valued id access optimization which causes a cyclic join dependency
    //    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus4.class, NoOpenJPA.class, NoEclipselink.class })
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class })
    public void testJoinFetchView() {
        testCorrelation(EmbeddableTestEntityFetchAsViewViewJoin.class);
    }

    protected <T extends EmbeddableTestEntitySimpleFetchView> void testCorrelation(Class<T> entityView) {
        testCorrelation(entityView, null);
    }

    protected <T extends EmbeddableTestEntitySimpleFetchView> void testCorrelation(Class<T> entityView, Integer batchSize) {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(entityView);
        cfg.addEntityView(IntIdEntitySimpleSubView.class);
        cfg.addEntityView(IntIdEntityFetchSubView.class);
        cfg.addEntityView(EmbeddableTestEntitySimpleFetchView.class);
        cfg.addEntityView(EmbeddableTestEntitySimpleFetchView.Id.class);
        cfg.addEntityView(EmbeddableTestEntityEmbeddableFetchSubView.class);
        EntityViewManager evm = cfg.createEntityViewManager(cbf);

        CriteriaBuilder<EmbeddableTestEntity> criteria = cbf.create(em, EmbeddableTestEntity.class, "d").orderByAsc("id.key");
        EntityViewSetting<T, CriteriaBuilder<T>> setting = EntityViewSetting.create(entityView);
        if (batchSize != null) {
            setting.setProperty(ConfigurationProperties.DEFAULT_BATCH_SIZE + ".manyToOne", batchSize);
        }
        CriteriaBuilder<T> cb = evm.applySetting(setting, criteria);
        List<T> results = cb.getResultList();

        Assert.assertEquals(4, results.size());

        if (EmbeddableTestEntityFetchAsEntityView.class.isAssignableFrom(entityView)) {
            assertEquals(doc1, (EmbeddableTestEntityFetchAsEntityView) results.get(0));
            assertEquals(doc2, (EmbeddableTestEntityFetchAsEntityView) results.get(1));
            assertEquals(doc3, (EmbeddableTestEntityFetchAsEntityView) results.get(2));
            assertEquals(doc4, (EmbeddableTestEntityFetchAsEntityView) results.get(3));
        } else {
            assertEquals(doc1, (EmbeddableTestEntityFetchAsViewView) results.get(0));
            assertEquals(doc2, (EmbeddableTestEntityFetchAsViewView) results.get(1));
            assertEquals(doc3, (EmbeddableTestEntityFetchAsViewView) results.get(2));
            assertEquals(doc4, (EmbeddableTestEntityFetchAsViewView) results.get(3));
        }
    }

    private void assertEquals(EmbeddableTestEntity doc, EmbeddableTestEntityFetchAsEntityView view) {
        assertSimpleEquals(doc, view);
        Assert.assertEquals(doc.getEmbeddable().getName(), view.getName());

        Assert.assertEquals(doc.getEmbeddableSet(), view.getEmbeddableSet());
        Assert.assertEquals(doc.getEmbeddableMap().size(), view.getEmbeddableMap().size());
        Assert.assertTrue(doc.getEmbeddableMap().values().containsAll(view.getEmbeddableMap()));

        Assert.assertEquals(doc.getEmbeddable().getElementCollection().size(), view.getElementCollection().size());
        Assert.assertTrue(doc.getEmbeddable().getElementCollection().values().containsAll(view.getElementCollection()));
        Assert.assertEquals(doc.getEmbeddable().getOneToMany(), view.getOneToMany());
        Assert.assertEquals(doc.getEmbeddable().getManyToOne(), view.getManyToOne());
    }

    private void assertEquals(EmbeddableTestEntity doc, EmbeddableTestEntityFetchAsViewView view) {
        assertSimpleEquals(doc, view);
        Assert.assertEquals(doc.getEmbeddable().getName(), view.getName());

        assertSetEquals(doc.getEmbeddableSet(), view.getEmbeddableSet());
        assertElementCollectionEquals(doc.getEmbeddableMap(), view.getEmbeddableMap());

        assertIntIdEquals(doc.getEmbeddable().getElementCollection(), view.getElementCollection());
        assertEntityEquals(doc.getEmbeddable().getOneToMany(), view.getOneToMany());
        assertSimpleEquals(doc.getEmbeddable().getManyToOne(), view.getManyToOne());
    }

    private void assertSimpleEquals(EmbeddableTestEntity doc, EmbeddableTestEntitySimpleFetchView view) {
        Assert.assertEquals(doc.getId().getKey(), view.getId().getKey());
    }

    private void assertSetEquals(Set<EmbeddableTestEntitySimpleEmbeddable> entities, Set<EmbeddableTestEntityEmbeddableFetchSubView> views) {
        Assert.assertEquals(entities.size(), views.size());
        OUTER: for (EmbeddableTestEntitySimpleEmbeddable entity : entities) {
            for (EmbeddableTestEntityEmbeddableFetchSubView view : views) {
                if (view.getName().equals(entity.getName())) {
                    continue OUTER;
                }
            }

            Assert.fail("could find entity: " + entity);
        }
    }

    private void assertEntityEquals(Set<EmbeddableTestEntity> entities, Set<EmbeddableTestEntitySimpleFetchView> views) {
        Assert.assertEquals(entities.size(), views.size());
        OUTER: for (EmbeddableTestEntity entity : entities) {
            for (EmbeddableTestEntitySimpleFetchView view : views) {
                if (view.getId().getKey().equals(entity.getId().getKey())) {
                    continue OUTER;
                }
            }

            Assert.fail("could find entity: " + entity);
        }
    }

    private void assertIntIdEquals(Map<String, IntIdEntity> entities, Set<IntIdEntityFetchSubView> views) {
        Assert.assertEquals(entities.size(), views.size());
        OUTER:for (Map.Entry<String, IntIdEntity> entry : entities.entrySet()) {
            for (IntIdEntityFetchSubView view : views) {
                if (view.getName().equals(entry.getValue().getName())) {
                    continue OUTER;
                }
            }
            Assert.fail("could find entity: " + entry.getValue());
        }
    }

    private void assertElementCollectionEquals(Map<String, EmbeddableTestEntitySimpleEmbeddable> entities, Set<EmbeddableTestEntityEmbeddableFetchSubView> views) {
        Assert.assertEquals(entities.size(), views.size());
        OUTER: for (Map.Entry<String, EmbeddableTestEntitySimpleEmbeddable> entry : entities.entrySet()) {
            for (EmbeddableTestEntityEmbeddableFetchSubView view : views) {
                if (view.getName().equals(entry.getValue().getName())) {
                    continue OUTER;
                }
            }
            Assert.fail("could find entity: " + entry.getValue());
        }
    }

}
