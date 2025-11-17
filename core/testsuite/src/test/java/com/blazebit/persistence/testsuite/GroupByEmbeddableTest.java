/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.SimpleEmbeddedIdEntity;
import com.blazebit.persistence.testsuite.entity.SimpleEmbeddedIdEntityId;
import org.junit.Test;

import jakarta.persistence.Tuple;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.6.10
 */
public class GroupByEmbeddableTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
            SimpleEmbeddedIdEntity.class,
            SimpleEmbeddedIdEntityId.class
        };
    }

    // #1775
    @Test
    public void testGroupByEmbeddedId() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class);
        cb.from(SimpleEmbeddedIdEntity.class, "p1");
        cb.select("p1.id");
        cb.groupBy("p1.id");

        assertEquals("SELECT p1.id FROM SimpleEmbeddedIdEntity p1 GROUP BY p1.id.id", cb.getQueryString());
        cb.getResultList();
    }
}
