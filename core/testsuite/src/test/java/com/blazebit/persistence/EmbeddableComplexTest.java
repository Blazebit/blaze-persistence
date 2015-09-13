/*
 * Copyright 2014 Blazebit.
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

package com.blazebit.persistence;

import static org.junit.Assert.assertEquals;

import javax.persistence.Tuple;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.entity.EmbeddableTestEntity;
import com.blazebit.persistence.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.base.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.category.NoOpenJPA;

/**
 * This kind of mapping is not required to be supported by a JPA implementation.
 *
 * @author Christian Beikov
 * @since 1.0.6
 */
public class EmbeddableComplexTest extends AbstractCoreTest {
    
    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
            IntIdEntity.class,
            EmbeddableTestEntity.class
        };
    }
    
    /* ManyToOne */
    
    @Test
    @Category({NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class})
    public void testSelectEmbeddedId() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(EmbeddableTestEntity.class, "e")
            .select("id");
        String expectedQuery = "SELECT e.id FROM EmbeddableTestEntity e";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }
    
    @Test
    @Category({NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class})
    public void testSelectEmbeddedIdSingleValuedAssociationId() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(EmbeddableTestEntity.class, "e")
            .select("id.intIdEntity.id");
        String expectedQuery = "SELECT e.id.intIdEntity.id FROM EmbeddableTestEntity e";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }
    
    @Test
    @Category({NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class})
    public void testSelectEmbeddedIdJoinedProperty() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(EmbeddableTestEntity.class, "e")
            .select("id.intIdEntity.name");
        String expectedQuery = "SELECT intIdEntity_1.name FROM EmbeddableTestEntity e "
            + "JOIN e.id.intIdEntity intIdEntity_1";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }
    
    @Test
    @Category({NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class})
    public void testSelectEmbeddedIdManyToOne() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(EmbeddableTestEntity.class, "e")
            .select("id.intIdEntity");
        String expectedQuery = "SELECT intIdEntity_1 FROM EmbeddableTestEntity e "
            + "JOIN e.id.intIdEntity intIdEntity_1";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }
    
    @Test
    @Category({NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class})
    public void testSelectEmbeddableManyToOne() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(EmbeddableTestEntity.class, "e")
            .select("embeddable.manyToOne");
        String expectedQuery = "SELECT manyToOne_1 FROM EmbeddableTestEntity e "
            + "LEFT JOIN e.embeddable.manyToOne manyToOne_1";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }
    
    /* OneToMany */
    
    @Test
    @Category({NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class})
    public void testWhereEmbeddableOneToManyPropertyFilter() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(EmbeddableTestEntity.class, "e")
            .where("embeddable.oneToMany.id.key").eqExpression("''");
        String expectedQuery = "SELECT e FROM EmbeddableTestEntity e "
            + "LEFT JOIN e.embeddable.oneToMany oneToMany_1 "
            + "WHERE oneToMany_1.id.key = ''";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }
    
    @Test
    @Category({NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class})
    public void testSelectEmbeddableOneToMany() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(EmbeddableTestEntity.class, "e")
            .select("embeddable.oneToMany");
        String expectedQuery = "SELECT oneToMany_1 FROM EmbeddableTestEntity e "
            + "LEFT JOIN e.embeddable.oneToMany oneToMany_1";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }
    
    /* ElementCollection */
    
    @Test
    @Category({NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class})
    public void testWhereEmbeddableElementCollectionPropertyFilter() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(EmbeddableTestEntity.class, "e")
            .where("embeddable.elementCollection.name").eqExpression("''");
        String expectedQuery = "SELECT e FROM EmbeddableTestEntity e "
            + "LEFT JOIN e.embeddable.elementCollection elementCollection_1 "
            + "WHERE " + joinAliasValue("elementCollection_1", "name") + " = ''";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }
    
    @Test
    @Category({NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class})
    public void testSelectEmbeddableElementCollection() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(EmbeddableTestEntity.class, "e")
            .select("embeddable.elementCollection");
        String expectedQuery = "SELECT " + joinAliasValue("elementCollection_1") + " FROM EmbeddableTestEntity e "
            + "LEFT JOIN e.embeddable.elementCollection elementCollection_1";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }
}
