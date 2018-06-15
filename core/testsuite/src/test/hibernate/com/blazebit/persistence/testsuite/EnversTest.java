/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.parser.EntityMetamodel;
import org.hibernate.envers.Audited;
import org.junit.Test;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.metamodel.EntityType;

import java.util.List;
import java.util.Set;

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
        EntityType<?> auditedEntity = metamodel.getEntity(A.class.getName() + "_AUD");

        assertNull(auditedEntity.getJavaType());
        assertNotNull(auditedEntity.getAttribute("originalId"));
        List<String> resultList = cbf.create(em, String.class)
                .from(auditedEntity, "ent")
                .select("ent.name")
                .leftJoinOn("ent", A.class, "a")
                    .on("a.id").eqExpression("ent.a_id")
                .end()
                .leftJoinOn("ent", auditedEntity, "aOld")
                    .on("aOld.a_id").eqExpression("ent.a_id")
                .end()
                .getResultList();
        assertEquals(0L, resultList.size());
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
