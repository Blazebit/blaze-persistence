/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Moritz Becker
 * @since 1.4.0
 */
public class AliasBasedMapKeyDereferencingTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return concat(super.getEntityClasses(),
                RootEntity.class,
                MapContainerEntity.class,
                MapKeyEntity.class,
                MapValueEntity.class
        );
    }

    @Test
    @Category({NoEclipselink.class})
    public void test() {
        CriteriaBuilder<RootEntity> crit = cbf.create(em, RootEntity.class, "root")
                .innerJoin("container", "container_1")
                .select("KEY(container_1.map).id");

        assertEquals("SELECT KEY(map_1).id FROM AliasBasedMapKeyDereferencingTest$RootEntity root JOIN root.container container_1 LEFT JOIN container_1.map map_1", crit.getQueryString());
        crit.getResultList();
    }

    @Entity
    @Table(name = "root_entity")
    public static class RootEntity {
        @Id
        private Long id;
        @ManyToOne
        private MapContainerEntity container;
    }

    @Entity
    @Table(name = "map_container_entity")
    public static class MapContainerEntity {
        @Id
        private Long id;
        @OneToMany(mappedBy = "container")
        private Map<MapKeyEntity, MapValueEntity> map = new HashMap<>();
    }

    @Entity
    @Table(name = "map_key_entity")
    public static class MapKeyEntity {
        @Id
        private Long id;
    }

    @Entity
    @Table(name = "map_value_entity")
    public static class MapValueEntity {
        @Id
        private Long id;
        @ManyToOne
        private MapContainerEntity container;
    }
}
