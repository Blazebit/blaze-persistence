/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDB2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus4;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoFirebird;
import com.blazebit.persistence.testsuite.base.jpa.category.NoH2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMSSQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOracle;
import com.blazebit.persistence.testsuite.base.jpa.category.NoPostgreSQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoSQLite;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.entity.Order;
import com.blazebit.persistence.testsuite.entity.OrderPosition;
import com.blazebit.persistence.testsuite.entity.OrderPositionElement;
import com.blazebit.persistence.testsuite.entity.OrderPositionHead;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.SimpleEmbeddedIdEntity;
import com.blazebit.persistence.testsuite.entity.SimpleEmbeddedIdEntityId;
import com.blazebit.persistence.testsuite.entity.Version;
import com.blazebit.persistence.testsuite.entity.Workflow;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.Tuple;

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
