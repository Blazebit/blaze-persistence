/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import org.hibernate.annotations.Where;
import org.junit.Test;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * @author Christian Beikov
 * @since 1.6.0
 */
public class Issue1167Test extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{ BasicEntity.class };
    }

    @Entity(name = "BasicEntity")
    @Where(clause = "deleted_char <> 't'")
    public static class BasicEntity {
        @Id
        Long id;
        @Column(name = "deleted_char", length = 1, nullable = false)
        char deleted = 'f';
        @ManyToOne
        BasicEntity parent;
    }

    @Test
    public void test1() {
        cbf.create(em, BasicEntity.class).getResultList();
    }

}
