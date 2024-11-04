/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
