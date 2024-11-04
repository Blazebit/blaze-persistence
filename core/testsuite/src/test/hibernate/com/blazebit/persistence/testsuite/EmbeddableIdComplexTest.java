/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.hibernate.EmbeddableIdTestEntity;
import org.junit.Test;

import javax.persistence.Tuple;

import static org.junit.Assert.assertEquals;

/**
 * Datanucleus, EclipseLink, OpenJPA do not support relations in embedded id
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class EmbeddableIdComplexTest extends AbstractCoreTest {
    
    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
            IntIdEntity.class,
            EmbeddableIdTestEntity.class
        };
    }
    
    /* ManyToOne */
    
    @Test
    public void testSelectEmbeddedIdSingleValuedAssociationId() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(EmbeddableIdTestEntity.class, "e")
            .select("id.intIdEntity.id");
        String expectedQuery = "SELECT e.id.intIdEntity.id FROM EmbeddableIdTestEntity e";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }
    
    @Test
    public void testSelectEmbeddedIdJoinedProperty() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(EmbeddableIdTestEntity.class, "e")
            .select("id.intIdEntity.name");
        String expectedQuery = "SELECT intIdEntity_1.name FROM EmbeddableIdTestEntity e "
            + "JOIN e.id.intIdEntity intIdEntity_1";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }
    
    @Test
    public void testSelectEmbeddedIdManyToOne() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(EmbeddableIdTestEntity.class, "e")
            .select("id.intIdEntity");
        String expectedQuery = "SELECT intIdEntity_1 FROM EmbeddableIdTestEntity e "
            + "JOIN e.id.intIdEntity intIdEntity_1";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }

    /* ElementCollection */
    
    @Test
    public void testEmbeddableInEmbeddedIdJoin(){
        CriteriaBuilder<EmbeddableIdTestEntity> crit = cbf.create(em, EmbeddableIdTestEntity.class, "e")
                .select("e.id.localizedEntity.someValue");
        
        assertEquals("SELECT e.id.localizedEntity.someValue FROM EmbeddableIdTestEntity e", crit.getQueryString());
        crit.getResultList();
    }
    
}
