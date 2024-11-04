/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.basic;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate60;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntitySub;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.basic.model.EmbeddableTestEntityEmbeddableSubView;
import com.blazebit.persistence.view.testsuite.basic.model.EmbeddableTestEntityIdView;
import com.blazebit.persistence.view.testsuite.basic.model.EmbeddableTestEntitySimpleEmbeddableSubView;
import com.blazebit.persistence.view.testsuite.basic.model.EmbeddableTestEntitySubView;
import com.blazebit.persistence.view.testsuite.basic.model.EmbeddableTestEntityView;
import com.blazebit.persistence.view.testsuite.basic.model.EmbeddableTestEntityViewWithSubview;
import com.blazebit.persistence.view.testsuite.basic.model.IntIdEntityView;
import com.blazebit.persistence.view.testsuite.entity.EmbeddableTestEntity2;
import com.blazebit.persistence.view.testsuite.entity.EmbeddableTestEntityId2;
import com.blazebit.persistence.view.testsuite.entity.EmbeddableTestEntitySimpleEmbeddable2;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * This kind of mapping is not required to be supported by a JPA implementation.
 *
 * @author Christian Beikov
 * @since 1.0.6
 */
// NOTE: Only Hibernate supports this mapping
//@Category({NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class})
// NOTE: Hibernate 6.3.1 bug
@Category({NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoHibernate60.class})
public class EmbeddableTestEntityViewTest extends AbstractEntityViewTest {

    protected EntityViewManager evm;

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
            EmbeddableTestEntity2.class,
            EmbeddableTestEntitySub.class,
            IntIdEntity.class
        };
    }
    
    @Before
    public void initEvm() {
        evm = build(
                IntIdEntityView.class,
                EmbeddableTestEntityView.class,
                EmbeddableTestEntityIdView.class,
                EmbeddableTestEntityIdView.Id.class,
                EmbeddableTestEntityViewWithSubview.class,
                EmbeddableTestEntityEmbeddableSubView.class,
                EmbeddableTestEntitySimpleEmbeddableSubView.class,
                EmbeddableTestEntitySubView.class
        );
    }

    private EmbeddableTestEntity2 entity1;
    private EmbeddableTestEntity2 entity2;

    @Before
    public void setUp() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                IntIdEntity intIdEntity1 = new IntIdEntity("1");
                entity1 = new EmbeddableTestEntity2();
                entity1.setId(new EmbeddableTestEntityId2(intIdEntity1, "1"));
                entity1.getEmbeddableSet().add(new EmbeddableTestEntitySimpleEmbeddable2("1"));
                entity1.getEmbeddableMap().put("key1", new EmbeddableTestEntitySimpleEmbeddable2("1"));
                entity1.getEmbeddable().setName("1");
                entity1.getEmbeddable().setManyToOne(null);
                entity1.getEmbeddable().getElementCollection().put("1", intIdEntity1);

                IntIdEntity intIdEntity2 = new IntIdEntity("2");
                entity2 = new EmbeddableTestEntity2();
                entity2.setId(new EmbeddableTestEntityId2(intIdEntity2, "2"));
                entity2.getEmbeddableSet().add(new EmbeddableTestEntitySimpleEmbeddable2("2"));
                entity2.getEmbeddableMap().put("key2", new EmbeddableTestEntitySimpleEmbeddable2("2"));
                entity2.getEmbeddable().setName("2");
                entity2.getEmbeddable().setManyToOne(entity1);
                entity2.getEmbeddable().getElementCollection().put("2", intIdEntity2);

                em.persist(intIdEntity1);
                em.persist(intIdEntity2);
                em.persist(entity1);
                em.persist(entity2);
            }
        });
            
        entity1 = cbf.create(em, EmbeddableTestEntity2.class)
            .fetch("id.intIdEntity", "embeddableSet", "embeddableMap", "embeddable.manyToOne", "embeddable.oneToMany", "embeddable.elementCollection")
            .where("id").eq(entity1.getId())
            .getSingleResult();
        entity2 = cbf.create(em, EmbeddableTestEntity2.class)
            .fetch("id.intIdEntity", "embeddableSet", "embeddableMap", "embeddable.manyToOne", "embeddable.oneToMany", "embeddable.elementCollection")
            .where("id").eq(entity2.getId())
            .getSingleResult();
    }

    @Test
    public void testEmbeddableViewWithEntityRelations() {
        CriteriaBuilder<EmbeddableTestEntity2> criteria = cbf.create(em, EmbeddableTestEntity2.class, "e")
            .orderByAsc("id");
        EntityViewSetting<EmbeddableTestEntityView, CriteriaBuilder<EmbeddableTestEntityView>> setting = EntityViewSetting.create(EmbeddableTestEntityView.class);
        setting.addOptionalParameter("optionalInteger", 1);
        CriteriaBuilder<EmbeddableTestEntityView> cb = evm.applySetting(setting, criteria);
        List<EmbeddableTestEntityView> results = cb.getResultList();

        assertEquals(2, results.size());
        assertEqualViewEquals(entity1, results.get(0));
        assertEqualViewEquals(entity2, results.get(1));
    }

    @Test
    public void testEmbeddableViewWithSubViewRelations() {
        CriteriaBuilder<EmbeddableTestEntity2> criteria = cbf.create(em, EmbeddableTestEntity2.class, "e")
            .orderByAsc("id");
        EntityViewSetting<EmbeddableTestEntityViewWithSubview, CriteriaBuilder<EmbeddableTestEntityViewWithSubview>> setting = EntityViewSetting.create(EmbeddableTestEntityViewWithSubview.class);
        setting.addOptionalParameter("optionalInteger", 1);
        CriteriaBuilder<EmbeddableTestEntityViewWithSubview> cb = evm.applySetting(setting, criteria);
        List<EmbeddableTestEntityViewWithSubview> results = cb.getResultList();

        assertEquals(2, results.size());
        assertEqualViewEquals(entity1, results.get(0));
        assertEqualViewEquals(entity2, results.get(1));
    }

    @Test
    public void testEmbeddableViewWithSubViewRelationsFetches() {
        CriteriaBuilder<EmbeddableTestEntity2> criteria = cbf.create(em, EmbeddableTestEntity2.class, "e")
                .orderByAsc("id");
        EntityViewSetting<EmbeddableTestEntityViewWithSubview, CriteriaBuilder<EmbeddableTestEntityViewWithSubview>> setting = EntityViewSetting.create(EmbeddableTestEntityViewWithSubview.class);
        setting.addOptionalParameter("optionalInteger", 1);
        setting.fetch("idKey");
        setting.fetch("embeddable");
        CriteriaBuilder<EmbeddableTestEntityViewWithSubview> cb = evm.applySetting(setting, criteria);
        List<EmbeddableTestEntityViewWithSubview> results = cb.getResultList();

        assertEquals(2, results.size());
        assertEquals(entity1.getId(), results.get(0).getId());
        assertEquals(entity1.getId().getKey(), results.get(0).getIdKey());
        assertEquals(entity1.getEmbeddable().getName(), results.get(0).getEmbeddable().getName());
        assertNull(results.get(0).getIdIntIdEntity());
        assertEquals(entity2.getId(), results.get(1).getId());
        assertEquals(entity2.getId().getKey(), results.get(1).getIdKey());
        assertEquals(entity2.getEmbeddable().getName(), results.get(1).getEmbeddable().getName());
        assertNull(results.get(1).getIdIntIdEntity());
    }

    @Test
    public void testEmbeddableViewWithSubViewRelationsFetchesIdView() {
        CriteriaBuilder<EmbeddableTestEntity2> criteria = cbf.create(em, EmbeddableTestEntity2.class, "e")
                .orderByAsc("id");
        EntityViewSetting<EmbeddableTestEntityIdView, CriteriaBuilder<EmbeddableTestEntityIdView>> setting = EntityViewSetting.create(EmbeddableTestEntityIdView.class);
        setting.fetch("name");
        CriteriaBuilder<EmbeddableTestEntityIdView> cb = evm.applySetting(setting, criteria);
        List<EmbeddableTestEntityIdView> results = cb.getResultList();

        assertEquals(2, results.size());
        assertEquals(entity1.getId().getIntIdEntity().getId(), results.get(0).getId().getIntIdEntityId());
        assertEquals(entity1.getId().getKey(), results.get(0).getId().getKey());
        assertNull(results.get(0).getIdKey());
        assertEquals(entity1.getEmbeddable().getName(), results.get(0).getName());
        assertEquals(entity2.getId().getIntIdEntity().getId(), results.get(1).getId().getIntIdEntityId());
        assertEquals(entity2.getId().getKey(), results.get(1).getId().getKey());
        assertNull(results.get(1).getIdKey());
        assertEquals(entity2.getEmbeddable().getName(), results.get(1).getName());
    }

    @Test
    public void testEntityViewSettingEmbeddableEntityViewRoot() {
        // Base setting
        EntityViewSetting<EmbeddableTestEntityIdView.Id, CriteriaBuilder<EmbeddableTestEntityIdView.Id>> setting =
                EntityViewSetting.create(EmbeddableTestEntityIdView.Id.class);

        // Query
        CriteriaBuilder<EmbeddableTestEntity2> cb = cbf.create(em, EmbeddableTestEntity2.class);
        evm.applySetting(setting, cb, "id")
                .getResultList();
    }
    
    private void assertEqualViewEquals(EmbeddableTestEntity2 entity, EmbeddableTestEntityView view) {
        assertEquals(entity.getId(), view.getId());
        assertEquals(entity.getId().getIntIdEntity(), view.getIdIntIdEntity());
        assertEquals(entity.getId().getIntIdEntity().getId(), view.getIdIntIdEntityId());
        assertEquals(entity.getId().getIntIdEntity().getName(), view.getIdIntIdEntityName());
        assertEquals(entity.getId().getKey(), view.getIdKey());
        assertEquals(entity.getEmbeddable().getName(), view.getEmbeddable().getName());
        assertEquals(entity.getEmbeddable().getManyToOne(), view.getEmbeddableManyToOne());
        assertEquals(entity.getEmbeddable().getOneToMany(), view.getEmbeddableOneToMany());
        assertEquals(entity.getEmbeddable().getElementCollection(), view.getEmbeddableElementCollection());
        
        Set<String> set1 = new HashSet<String>();
        for (EmbeddableTestEntitySimpleEmbeddable2 elem : entity.getEmbeddableSet()) {
            set1.add(elem.getName());
        }
        
        Set<String> set2 = new HashSet<String>();
        for (EmbeddableTestEntitySimpleEmbeddableSubView elem : view.getEmbeddableSet()) {
            set2.add(elem.getName());
        }
        
        assertEquals(set1, set2);
        
        Map<String, String> map1 = new HashMap<String, String>();
        for (Map.Entry<String, EmbeddableTestEntitySimpleEmbeddable2> entry : entity.getEmbeddableMap().entrySet()) {
            map1.put(entry.getKey(), entry.getValue().getName());
        }
        
        Map<String, String> map2 = new HashMap<String, String>();
        for (Map.Entry<String, EmbeddableTestEntitySimpleEmbeddableSubView> entry : view.getEmbeddableMap().entrySet()) {
            map2.put(entry.getKey(), entry.getValue().getName());
        }
        
        assertEquals(map1, map2);
    }
    
    private void assertEqualViewEquals(EmbeddableTestEntity2 entity, EmbeddableTestEntityViewWithSubview view) {
        assertEquals(entity.getId(), view.getId());
        if (entity.getId().getIntIdEntity() == null) {
            assertNull(view.getIdIntIdEntity());
        } else {
            assertEquals(entity.getId().getIntIdEntity().getId(), view.getIdIntIdEntity().getId());
        }
        assertEquals(entity.getId().getIntIdEntity().getId(), view.getIdIntIdEntityId());
        assertEquals(entity.getId().getIntIdEntity().getName(), view.getIdIntIdEntityName());
        assertEquals(entity.getId().getKey(), view.getIdKey());
        assertEquals(entity.getEmbeddable().getName(), view.getEmbeddable().getName());
        if (entity.getEmbeddable().getManyToOne() == null) {
            assertNull(view.getEmbeddableManyToOneView());
        } else {
            assertEquals(entity.getEmbeddable().getManyToOne().getId(), view.getEmbeddableManyToOneView().getId());
        }
        
        assertEquals(entity.getEmbeddable().getOneToMany().size(), view.getEmbeddableOneToManyView().size());
        OUTER: for (EmbeddableTestEntity2 child : entity.getEmbeddable().getOneToMany()) {
            for (EmbeddableTestEntitySubView childView : view.getEmbeddableOneToManyView()) {
                if (child.getId().equals(childView.getId())) {
                    continue OUTER;
                }
            }
            
            fail("Couldn't find child view with id: " + child.getId());
        }
        assertEquals(entity.getEmbeddable().getElementCollection().size(), view.getEmbeddableElementCollectionView().size());
        for (Map.Entry<String, IntIdEntity> childEntry : entity.getEmbeddable().getElementCollection().entrySet()) {
            IntIdEntityView childView = view.getEmbeddableElementCollectionView().get(childEntry.getKey());
            assertEquals(childEntry.getValue().getId(), childView.getId());
        }
        
        Set<String> set1 = new HashSet<String>();
        for (EmbeddableTestEntitySimpleEmbeddable2 elem : entity.getEmbeddableSet()) {
            set1.add(elem.getName());
        }
        
        Set<String> set2 = new HashSet<String>();
        for (EmbeddableTestEntitySimpleEmbeddableSubView elem : view.getEmbeddableSet()) {
            set2.add(elem.getName());
        }
        
        assertEquals(set1, set2);
        
        Map<String, String> map1 = new HashMap<String, String>();
        for (Map.Entry<String, EmbeddableTestEntitySimpleEmbeddable2> entry : entity.getEmbeddableMap().entrySet()) {
            map1.put(entry.getKey(), entry.getValue().getName());
        }
        
        Map<String, String> map2 = new HashMap<String, String>();
        for (Map.Entry<String, EmbeddableTestEntitySimpleEmbeddableSubView> entry : view.getEmbeddableMap().entrySet()) {
            map2.put(entry.getKey(), entry.getValue().getName());
        }
        
        assertEquals(map1, map2);
    }
}
