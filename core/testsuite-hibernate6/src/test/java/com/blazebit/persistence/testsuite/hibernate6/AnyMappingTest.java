/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.testsuite.hibernate6;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.hibernate6.entity.PropertyHolder;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertTrue;

public class AnyMappingTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
            PropertyHolder.class
        };
    }

    @Test
    public void anyMapping_doesNotFailCriteriaBuilderFactoryCreation() {
        CriteriaBuilder<PropertyHolder> cb = cbf.create(em, PropertyHolder.class, "p");
        assertTrue(true);
    }
}
