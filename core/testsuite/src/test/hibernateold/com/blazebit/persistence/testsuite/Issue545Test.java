/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.vladmihalcea.hibernate.type.basic.NullableCharacterType;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.2.1
 */
public class Issue545Test extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] { EntityWithCustomType.class };
    }

    @Before
    public void setUp() throws Exception {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                EntityWithCustomType entityWithCustomType = new EntityWithCustomType();
                entityWithCustomType.customValue = 'c';
                em.persist(entityWithCustomType);
                em.flush();
                em.clear();
            }
        });
    }

    @Test
    public void queryEntityWithCustomTypeTest() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                cbf.create(em, EntityWithCustomType.class)
                        .where("transformedCustomValue").eq('C')
                        .getSingleResult();
            }
        });
    }

    @Entity
    @Table(name = "ewct")
    @TypeDef(name = "CustomType", typeClass = NullableCharacterType.class)
    public static class EntityWithCustomType {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        @Type(type = "CustomType")
        @Column(name = "custom_value")
        private Character customValue;

        @Formula("UPPER(custom_value)")
        @Type(type = "CustomType")
        private Character transformedCustomValue;

    }

}
