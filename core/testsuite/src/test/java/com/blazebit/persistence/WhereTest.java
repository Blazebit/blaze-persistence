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

import com.blazebit.persistence.entity.Document;
import com.blazebit.persistence.entity.Person;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author ccbem
 */
public class WhereTest extends AbstractPersistenceTest {
    
    @Test
    public void testWhereProperty(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.age").ge(25);

        assertEquals("FROM Document d WHERE d.age >= :param_0", criteria.getQueryString());
    }
    
    @Test
    public void testWherePropertyExpression(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.age + 1").ge(25);

        assertEquals("FROM Document d WHERE d.age+1 >= :param_0", criteria.getQueryString());
    }
    
    
    
    @Test
    public void testWherePath(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.partners.name").gt(0);
        
        assertEquals("FROM Document d LEFT JOIN d.partners partners WHERE partners.name > :param_0", criteria.getQueryString());
    }
    
    @Test
    public void testWherePathExpression(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.owner.ownedDocuments.age + 1").ge(25);

        assertEquals("FROM Document d JOIN d.owner owner LEFT JOIN owner.ownedDocuments ownedDocuments WHERE ownedDocuments.age+1 >= :param_0", criteria.getQueryString());
    }

    @Test
    public void testWhereAnd(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.partners.name").gt(0).where("d.versions.url").like("http://%");     
        
        assertEquals("FROM Document d LEFT JOIN d.partners partners LEFT JOIN d.versions versions WHERE partners.name > :param_0 AND versions.url LIKE :param_1", criteria.getQueryString());
    }
    
    @Test
    public void testWhereOr(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.whereOr().where("d.partners.name").gt(0).where("d.versions.url").like("http://%").endOr();   
        
        assertEquals("FROM Document d LEFT JOIN d.partners partners LEFT JOIN d.versions versions WHERE partners.name > :param_0 OR versions.url LIKE :param_1", criteria.getQueryString());
    }
    
    @Test
    public void testWhereOrAnd(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.whereOr()
                    .whereAnd()
                        .where("d.partners.name").gt(0)
                        .where("d.versions.url").like("http://%")
                    .endAnd()
                    .whereAnd()
                        .where("d.versions.date").lt(10)
                        .where("d.versions.url").like("ftp://%")
                    .endAnd()
                .endOr();   
        assertEquals("FROM Document d LEFT JOIN d.partners partners LEFT JOIN d.versions versions WHERE (partners.name > :param_0 AND versions.url LIKE :param_1) OR (versions.date < :param_2 AND versions.url LIKE :param_3)", criteria.getQueryString());
    }
    
    @Test
    public void testWhereAndOr(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.whereOr()
                    .where("d.partners.name").gt(0)
                    .where("d.versions.url").like("http://%")
                .endOr()
                .whereOr()
                    .where("d.versions.date").lt(10)
                    .where("d.versions.url").like("ftp://%")
                .endOr();   
        
        assertEquals("FROM Document d LEFT JOIN d.partners partners LEFT JOIN d.versions versions WHERE (partners.name > :param_0 OR versions.url LIKE :param_1) AND (versions.date < :param_2 OR versions.url LIKE :param_3)", criteria.getQueryString());
    }
    
    @Test
    public void testWhereOrSingleClause(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.whereOr().where("d.partners.name").gt(0).endOr();   
        
        assertEquals("FROM Document d LEFT JOIN d.partners partners WHERE partners.name > :param_0", criteria.getQueryString());
    }
    
    @Test
    public void testWhereOrWhereAndSingleClause(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.whereOr().whereAnd().where("d.versions.date").gt(0).endAnd().endOr();   
        
        assertEquals("FROM Document d LEFT JOIN d.versions versions WHERE versions.date > :param_0", criteria.getQueryString());
    }
    
    @Test(expected = NullPointerException.class)
    public void testWhereNull(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testWhereEmpty(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("").gt(0);
    }
    
    @Test(expected = IllegalStateException.class)
    public void testWhereNotClosed(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.age");
        criteria.where("d.owner");
    }
    
    @Test(expected = IllegalStateException.class)
    public void testWhereOrNotClosed(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.whereOr().where("d.partners.name").gt(0);        
        criteria.where("d.partners.name");
    }
    
    @Test(expected = IllegalStateException.class)
    public void testWhereAndNotClosed(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.whereOr().whereAnd().where("d.partners.name").gt(0);
        criteria.where("d.partners.name");
    }
    
    @Test
    public void testWhereExists(){
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.whereExists().from(Person.class, "p").select("id").where("name").eqExpression("d.name").end();
        String expected = "FROM Document d WHERE EXISTS (SELECT p.id FROM Person p WHERE p.name = d.name)";
        
        Assert.assertEquals(expected, crit.getQueryString());
    }
    
    @Test
    public void testWhereNotExists(){
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.whereNotExists().from(Person.class, "p").select("id").where("name").eqExpression("d.name").end();
        String expected = "FROM Document d WHERE NOT EXISTS (SELECT p.id FROM Person p WHERE p.name = d.name)";
        
        Assert.assertEquals(expected, crit.getQueryString());
    }
    
    @Test
    public void testWhereExistsAndBuilder(){
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.where("d.name").eq("test").whereOr().whereAnd().whereExists().from(Person.class, "p").select("id").where("name").eqExpression("d.name").end().endAnd().endOr();
        String expected = "FROM Document d WHERE d.name = :param_0 AND (EXISTS (SELECT p.id FROM Person p WHERE p.name = d.name))";
        
        Assert.assertEquals(expected, crit.getQueryString());
    }
    
    @Test
    public void testWhereNotExistsAndBuilder(){
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.where("d.name").eq("test").whereOr().whereAnd().whereNotExists().from(Person.class, "p").select("id").where("name").eqExpression("d.name").end().endAnd().endOr();
        String expected = "FROM Document d WHERE d.name = :param_0 AND (NOT EXISTS (SELECT p.id FROM Person p WHERE p.name = d.name))";
        
        Assert.assertEquals(expected, crit.getQueryString());
    }
    
    @Test
    public void testWhereExistsOrBuilder(){
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.whereOr().where("d.name").eq("test").whereExists().from(Person.class, "p").select("id").where("name").eqExpression("d.name").end().endOr();
        String expected = "FROM Document d WHERE d.name = :param_0 OR EXISTS (SELECT p.id FROM Person p WHERE p.name = d.name)";
        
        Assert.assertEquals(expected, crit.getQueryString());
    }
    
    @Test
    public void testWhereNotExistsOrBuilder(){
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.whereOr().where("d.name").eq("test").whereNotExists().from(Person.class, "p").select("id").where("name").eqExpression("d.name").end().endOr();
        String expected = "FROM Document d WHERE d.name = :param_0 OR NOT EXISTS (SELECT p.id FROM Person p WHERE p.name = d.name)";
        
        Assert.assertEquals(expected, crit.getQueryString());
    }
    
    @Test
    public void testWhereLeftSubquery(){
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.where().from(Person.class, "p").select("id").where("name").eqExpression("d.name").end().eqExpression("id");
        String expected = "FROM Document d WHERE (SELECT p.id FROM Person p WHERE p.name = d.name) = d.id";
        
        Assert.assertEquals(expected, crit.getQueryString());
    }
    
    @Test
    public void testWhereLeftSubqueryAndBuilder(){
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.where("d.name").eq("test").whereOr().whereAnd().where().from(Person.class, "p").select("id").where("name").eqExpression("d.name").end().eqExpression("d.owner.id").endAnd().endOr();
        String expected = "FROM Document d JOIN d.owner owner WHERE d.name = :param_0 AND ((SELECT p.id FROM Person p WHERE p.name = d.name) = owner.id)";
        
        Assert.assertEquals(expected, crit.getQueryString());
    }
    
    @Test
    public void testWhereLeftSubqueryOrBuilder(){
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.whereOr().where("d.name").eq("test").where().from(Person.class, "p").select("id").where("name").eqExpression("d.name").end().eqExpression("d.owner.id").endOr();
        String expected = "FROM Document d JOIN d.owner owner WHERE d.name = :param_0 OR (SELECT p.id FROM Person p WHERE p.name = d.name) = owner.id";
        
        Assert.assertEquals(expected, crit.getQueryString());
    }
}
