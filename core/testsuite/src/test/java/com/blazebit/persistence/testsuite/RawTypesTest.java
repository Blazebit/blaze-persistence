/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import org.junit.Test;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.RawTypeEntity;

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

    @Test
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
