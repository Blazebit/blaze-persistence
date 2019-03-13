/*
 * Copyright 2014 - 2019 Blazebit.
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
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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
        return concat(super.getEntityClasses(), new Class<?>[] {
                RootEntity.class,
                MapContainerEntity.class,
                MapKeyEntity.class,
                MapValueEntity.class
        });
    }

    @Test
    @Category(NoDatanucleus.class)
    public void test() {
        CriteriaBuilder<RootEntity> crit = cbf.create(em, RootEntity.class, "root")
                .innerJoin("container", "container_1")
                .select("KEY(container_1.map).id");

        assertEquals("SELECT KEY(map_1).id FROM AliasBasedMapKeyDereferencingTest$RootEntity root JOIN root.container container_1 LEFT JOIN container_1.map map_1", crit.getQueryString());
        crit.getResultList();
    }

    @Entity
    public static class RootEntity {
        @Id
        private Long id;
        @ManyToOne
        private MapContainerEntity container;
    }

    @Entity
    public static class MapContainerEntity {
        @Id
        private Long id;
        @OneToMany(mappedBy = "container")
        private Map<MapKeyEntity, MapValueEntity> map = new HashMap<>();
    }

    @Entity
    public static class MapKeyEntity {
        @Id
        private Long id;
    }

    @Entity
    public static class MapValueEntity {
        @Id
        private Long id;
        @ManyToOne
        private MapContainerEntity container;
    }
}
