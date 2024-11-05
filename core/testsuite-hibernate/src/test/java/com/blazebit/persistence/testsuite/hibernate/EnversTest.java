/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.hibernate;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.envers.Audited;

import org.junit.Test;

import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.EntityType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author Christian Beikov
 * @since 1.3.0
 */
public class EnversTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{ A.class };
    }

    @Test
    public void selectEnversEntity() {
        EntityMetamodel metamodel = cbf.getService(EntityMetamodel.class);
        EntityType<?> auditedEntity = getAuditedEntity(metamodel, A.class);

        if (auditedEntity.getJavaType() != Map.class) {
            assertNull(auditedEntity.getJavaType());
        }
        assertNotNull(auditedEntity.getAttribute("originalId"));
        List<String> resultList = cbf.create(em, String.class)
                .from(auditedEntity, "ent")
                .select("ent.name")
                .innerJoinOn("ent", A.class, "a")
                    .on("a.id").eqExpression("ent.a_id")
                .end()
                .innerJoinOn("ent", auditedEntity, "aOld")
                    .on("aOld.a_id").eqExpression("ent.a_id")
                .end()
                .getResultList();
        assertEquals(0L, resultList.size());
    }

    private EntityType<?> getAuditedEntity(EntityMetamodel metamodel, Class<?> c) {
        EntityType<?> entityType = metamodel.getEntity(c.getName() + "_AUD");
        if (entityType == null) {
            // I'm looking at you Hibernate 4.2 and 4.3 >:|
            return metamodel.getEntity(c.getName().substring(c.getPackage().getName().length() + 1) + "_AUD");
        }
        return entityType;
    }

    @Audited
    @Entity(name = "A")
    @Table(name = "A")
    public static class A {

        @Id
        private Long id;
        private String name;
        @ManyToOne
        private A a;
        @ManyToMany
        private Set<A> as;

    }
}
