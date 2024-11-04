/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.spi.ExtendedAttribute;
import com.blazebit.persistence.spi.ExtendedManagedType;
import org.junit.Test;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.io.Serializable;

import static org.junit.Assert.assertEquals;

/**
 * @author Christian Beikov
 * @since 1.6.6
 */
public class Issue1436Test extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{ OrderItem.class, Item.class };
    }

    @Test
    public void testBuild() {
        ExtendedAttribute attribute = cbf.getService(EntityMetamodel.class)
                .getManagedType(ExtendedManagedType.class, OrderItem.class)
                .getAttribute("item.id1");
        assertEquals(1, attribute.getColumnNames().length);
    }

    @Entity
    public static class Item implements Serializable {
        @Id
        Long id1;
        @Id
        Long id2;

        public Item() {
        }
    }

    @Entity
    public static class OrderItem implements Serializable {
        @Id
        Long id;
        @Id
        @ManyToOne
        Item item;

        public OrderItem() {
        }
    }
}
