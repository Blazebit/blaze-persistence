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

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.entity.RawTypeEntity;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * This test is for issue #344
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class RawTypesTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class[]{
                RawTypeEntity.class
        };
    }

    // NOTE: Datanucleus does not support the MapKeyClass yet: https://github.com/datanucleus/datanucleus-core/issues/185
    @Test
    @Category({ NoDatanucleus.class })
    public void buildingQueryWithEntityThatUsesRawTypesWorks() {
        CriteriaBuilder<RawTypeEntity> criteria = cbf.create(em, RawTypeEntity.class, "d");
        criteria.select("d.list.id");
        criteria.select("d.set.id");
        criteria.select("d.map.id");
        criteria.select("KEY(d.map2).id");
        criteria.getQueryString();
        // Can't actually run this because the schema we used might not exist, but at least we know that building the model and query worked
//        criteria.getResultList();
    }
}
