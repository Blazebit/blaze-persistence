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

import static org.junit.Assert.assertEquals;

import javax.persistence.Tuple;

import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate43;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate50;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate51;
import com.blazebit.persistence.testsuite.entity.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;

/**
 * This kind of mapping is not required to be supported by a JPA implementation.
 *
 * @author Christian Beikov
 * @since 1.0.6
 */
// NOTE: EclipseLink doesn't support Map in embeddables: https://bugs.eclipse.org/bugs/show_bug.cgi?id=391062
// TODO: report that datanucleus doesn't support element collection in an embeddable
@Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
public class EmbeddableComplexTest extends AbstractCoreTest {

    @Override
    protected void setUpOnce() {
        // TODO: Remove me when DataNucleus fixes map value access: https://github.com/datanucleus/datanucleus-rdbms/issues/230
        cleanDatabase();
    }
    
    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
            IntIdEntity.class,
            EmbeddableTestEntity.class,
            EmbeddableTestEntityContainer.class,
            EmbeddableTestEntityEmbeddable.class,
            NameObject.class,
            EmbeddableTestEntityNestedEmbeddable.class
        };
    }
    
    /* ManyToOne */
    
    @Test
    public void testSelectEmbeddedId() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(EmbeddableTestEntity.class, "e")
            .select("id");
        String expectedQuery = "SELECT e.id FROM EmbeddableTestEntity e";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }
    
    @Test
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
    public void testSelectEmbeddableOneToMany() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(EmbeddableTestEntity.class, "e")
            .select("embeddable.oneToMany");
        String expectedQuery = "SELECT oneToMany_1 FROM EmbeddableTestEntity e "
            + "LEFT JOIN e.embeddable.oneToMany oneToMany_1";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }
    
    @Test
    public void testSelectEmbeddedIdCollectionSize(){
        CriteriaBuilder<EmbeddableTestEntity> cb = cbf.create(em, EmbeddableTestEntity.class, "e");
        cb.select("SIZE(e.embeddable.oneToMany)");
        
        String expected = "SELECT (SELECT " + countStar() + " FROM e.embeddable.oneToMany embeddableTestEntity) FROM EmbeddableTestEntity e";
        assertEquals(expected, cb.getQueryString());
        cb.getResultList();
    }
    
    /* ElementCollection */
    
    @Test
    // NOTE: Datanucleus, EclipseLink, OpenJPA does not support relations in embedded id
    public void testWhereEmbeddableElementCollectionPropertyFilter() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(EmbeddableTestEntity.class, "e")
            .where("embeddable.elementCollection.primaryName").eqExpression("''");
        String expectedQuery = "SELECT e FROM EmbeddableTestEntity e "
            + "LEFT JOIN e.embeddable.elementCollection elementCollection_1 "
            + "WHERE " + joinAliasValue("elementCollection_1", "primaryName") + " = ''";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }
    
    @Test
    // NOTE: http://hibernate.atlassian.net/browse/HHH-10229
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class })
    public void testSelectEmbeddableElementCollection() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(EmbeddableTestEntity.class, "e")
            .select("embeddable.elementCollection");
        String expectedQuery = "SELECT " + joinAliasValue("elementCollection_1") + " FROM EmbeddableTestEntity e "
            + "LEFT JOIN e.embeddable.elementCollection elementCollection_1";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    // NOTE: http://hibernate.atlassian.net/browse/HHH-10229
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class })
    public void testSelectEmbeddableElementCollectionArraySyntax() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(EmbeddableTestEntity.class, "e")
                .select("embeddable.elementCollection['test']");
        String expectedQuery = "SELECT " + joinAliasValue("elementCollection_test_1") + " FROM EmbeddableTestEntity e "
                + "LEFT JOIN e.embeddable.elementCollection elementCollection_test_1" + onClause("KEY(elementCollection_test_1) = 'test'");
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    // NOTE: http://hibernate.atlassian.net/browse/HHH-10229
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class })
    public void testSelectEmbeddableElementCollectionArraySyntaxValue() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(EmbeddableTestEntity.class, "e")
                .select("embeddable.elementCollection['test'].primaryName");
        String expectedQuery = "SELECT " + joinAliasValue("elementCollection_test_1") + ".primaryName FROM EmbeddableTestEntity e "
                + "LEFT JOIN e.embeddable.elementCollection elementCollection_test_1" + onClause("KEY(elementCollection_test_1) = 'test'");
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    // Test for #598
    public void testSelectEmbeddableFetchElementCollection() {
        CriteriaBuilder<EmbeddableTestEntityEmbeddable> cb = cbf.create(em, EmbeddableTestEntityEmbeddable.class)
                .from(EmbeddableTestEntity.class, "e")
                .select("embeddable")
                .fetch("embeddable.elementCollection2");
        try {
            cb.getQueryString();
            Assert.fail("Expected fetch joining of element collection in embeddable to fail");
        } catch (IllegalStateException ex) {
            Assert.assertTrue(ex.getMessage().contains("Missing fetch owners: [e]"));
        }
    }

    @Test
    // Test for #598
    public void testSelectEmbeddableFetchElementCollectionJpaOnly() {
        try {
            em.createQuery("SELECT e.embeddable FROM EmbeddableTestEntity e JOIN FETCH e.embeddable.elementCollection2").getResultList();
            Assert.fail("Expected fetch joining of element collection in embeddable to fail");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().contains("embeddable.elementCollection2"));
        }
    }

    @Test
    public void testSelectEmbeddableManyToManyCollection() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(EmbeddableTestEntity.class, "e")
                .select("embeddable.manyToMany");
        String expectedQuery = "SELECT " + joinAliasValue("manyToMany_1") + " FROM EmbeddableTestEntity e "
                + "LEFT JOIN e.embeddable.manyToMany manyToMany_1";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }
    
    @Test
    public void testEmbeddableExplicitJoin(){
        CriteriaBuilder<EmbeddableTestEntity> crit = cbf.create(em, EmbeddableTestEntity.class, "e")
                .leftJoin("e.embeddable.nestedEmbeddable.nestedOneToMany", "oneToMany")
                .select("oneToMany");
        
        assertEquals("SELECT oneToMany FROM EmbeddableTestEntity e LEFT JOIN e.embeddable.nestedEmbeddable.nestedOneToMany oneToMany", crit.getQueryString());
        crit.getResultList();
    }
    
    @Test
    public void testEmbeddedIdSize1(){
        CriteriaBuilder<EmbeddableTestEntity> crit = cbf.create(em, EmbeddableTestEntity.class, "e")
                .select("SIZE(e.embeddable.oneToMany)");
        
        assertEquals("SELECT (SELECT " + countStar() + " FROM e.embeddable.oneToMany embeddableTestEntity) FROM EmbeddableTestEntity e", crit.getQueryString());
        crit.getResultList();
    }
    
    @Test
    public void testEmbeddedIdSize2(){
        CriteriaBuilder<EmbeddableTestEntityContainer> crit = cbf.create(em, EmbeddableTestEntityContainer.class, "e")
                .select("SIZE(e.embeddableTestEntities)");
        
        assertEquals("SELECT (SELECT " + countStar() + " FROM e.embeddableTestEntities embeddableTestEntity) FROM EmbeddableTestEntityContainer e", crit.getQueryString());
        crit.getResultList();
    }
    
}
