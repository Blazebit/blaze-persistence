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

package com.blazebit.persistence.view.testsuite.subview;

import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate43;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate50;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate51;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOracle;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.Table;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Only Hibernate 5.2+ supports map key dereferencing.
 * Hibernate generates a subquery in the join condition which is not supported by Oracle (ERROR: ORA-01799: a column may not be outer-joined to a subquery).
 *
 * @author Moritz Becker
 * @since 1.4.0
 */
@Category({NoDatanucleus.class, NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate50.class, NoHibernate51.class, NoEclipselink.class, NoOracle.class})
public class MapKeySubviewTest extends AbstractEntityViewTest {

    private MapKeySubviewTest.MapContainer container;

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class[] {
                RootEntity.class,
                MapContainer.class,
                MapKeyEntity.class,
                MapValueEntity.class
        };
    }

    @Override
    protected SchemaMode getSchemaMode() {
        if (getJpaProviderFamily() == JpaProviderFamily.HIBERNATE &&
                (getJpaProviderMajorVersion() == 5 && getJpaProviderMinorVersion() <= 3 ||
                        getJpaProviderMajorVersion() <= 4)
        ) {
            return SchemaMode.JDBC;
        } else {
            return super.getSchemaMode();
        }
    }

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                RootEntity root = new RootEntity(0L);
                MapContainer container = new MapContainer(1L);
                MapKeyEntity mapKey = new MapKeyEntity(2L);
                MapValueEntity mapValue = new MapValueEntity(3L);

                root.mapContainer = container;
                mapValue.mapContainer = container;
                mapValue.mapKey = mapKey;
                container.map.put(mapValue.mapKey, mapValue);

                em.persist(root);
                em.persist(container);
                em.persist(mapKey);
                em.persist(mapValue);
                MapKeySubviewTest.this.container = container;
            }
        });
    }

    @Test
    public void test() {
        EntityViewManager evm = build(
                RootEntityView.class,
                MapContainerView.class,
                MapKeyIdView.class,
                MapValueIdView.class
        );

        RootEntityView rootView = evm.applySetting(EntityViewSetting.create(RootEntityView.class), cbf.create(em, RootEntity.class)).getSingleResult();

        assertEquals(container.id, rootView.getMapContainer().getId());
        assertEquals(container.map.keySet().iterator().next().id, rootView.getMapContainer().getMap().keySet().iterator().next().getId());
    }

    @Entity(name = "RootEntity")
    @Table(name = "root_entity")
    public static class RootEntity {
        @Id
        private Long id;
        @ManyToOne
        private MapContainer mapContainer;

        public RootEntity() { }

        public RootEntity(Long id) {
            this.id = id;
        }
    }

    @Entity(name = "MapContainer")
    @Table(name = "map_container")
    public static class MapContainer {
        @Id
        private Long id;
        @ManyToMany
        @MapKey(name = "mapKey")
        private Map<MapKeyEntity, MapValueEntity> map = new HashMap<>();

        public MapContainer() { }

        public MapContainer(Long id) {
            this.id = id;
        }
    }

    @Entity(name = "MapKeyEntity")
    @Table(name = "map_key_entity")
    public static class MapKeyEntity {
        @Id
        private Long id;

        public MapKeyEntity() { }

        public MapKeyEntity(Long id) {
            this.id = id;
        }
    }

    @Entity(name = "MapValueEntity")
    @Table(name = "map_value_entity")
    public static class MapValueEntity {
        @Id
        private Long id;
        @ManyToOne(optional = false)
        private MapContainer mapContainer;
        @ManyToOne(optional = false)
        @JoinColumn(unique = true)
        private MapKeyEntity mapKey;

        public MapValueEntity() { }

        public MapValueEntity(Long id) {
            this.id = id;
        }
    }

    @EntityView(RootEntity.class)
    public interface RootEntityView {
        @IdMapping
        Long getId();

        MapContainerView getMapContainer();
    }

    @EntityView(MapContainer.class)
    public interface MapContainerView {

        @IdMapping
        Long getId();

        Map<MapKeyIdView, MapValueIdView> getMap();
    }

    @EntityView(MapKeyEntity.class)
    public interface MapKeyIdView {
        @IdMapping
        Long getId();
    }

    @EntityView(MapValueEntity.class)
    public interface MapValueIdView {
        @IdMapping
        Long getId();
    }
}
