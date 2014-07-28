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

import static com.blazebit.persistence.AbstractPersistenceTest.cbf;
import com.blazebit.persistence.entity.Document;
import com.blazebit.persistence.entity.Person;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author ccbem
 */
public class EqTest extends AbstractPersistenceTest {
    @Test
    public void testEqualTo(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.age").eq(20);
        
        assertEquals("FROM Document d WHERE d.age = :param_0", criteria.getQueryString());
    }
    
    @Test(expected = NullPointerException.class)
    public void testEqualToNull(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.age").eq(null);
    }
    
    @Test
    public void testEqualToExpression(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.age").eqExpression("d.versions.date + 1");
        
        assertEquals("FROM Document d LEFT JOIN d.versions versions WHERE d.age = versions.date+1", criteria.getQueryString());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testEqualToEmptyExpression(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.age").eqExpression("");        
    }
    
    @Test(expected = NullPointerException.class)
    public void testEqualToNullExpression(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.age").eqExpression(null);        
    }
    
    // TODO: for subqueries
//    @Test
//    public void testEqualToAll(){
//        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
//        criteria.where("d.age").eq().all().expression("d.partners.age");
//        
//        assertEquals("FROM Document d LEFT JOIN d.partners partners WHERE d.age = ALL(partners.age)", criteria.getQueryString());
//    }
//    
//    @Test
//    public void testEqualToAny(){
//        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
//        criteria.where("d.age").eq().any().expression("d.partners.age");
//        
//        assertEquals("FROM Document d LEFT JOIN d.partners partners WHERE d.age = ANY(partners.age)", criteria.getQueryString());
//    }
    
    @Test
    public void testNotEqualTo(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.age").notEq(20);
        
        assertEquals("FROM Document d WHERE NOT d.age = :param_0", criteria.getQueryString());
    }
    
     @Test(expected = NullPointerException.class)
    public void testNotEqualToNull(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.age").notEq(null);
    }
    
    @Test
    public void testNotEqualToExpression(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.age").notEqExpression("d.versions.date + 1");
        
        assertEquals("FROM Document d LEFT JOIN d.versions versions WHERE NOT d.age = versions.date+1", criteria.getQueryString());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNotEqualToEmptyExpression(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.age").notEqExpression("");        
    }
    
    @Test(expected = NullPointerException.class)
    public void testNotEqualToNullExpression(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.age").notEqExpression(null);        
    }
    
    // TODO: for subqueries
//    @Test
//    public void testNotEqualToAll(){
//        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
//        criteria.where("d.age").notEq().all().expression("d.partners.age");
//        
//        assertEquals("FROM Document d LEFT JOIN d.partners partners WHERE d.age != ALL(partners.age)", criteria.getQueryString());
//    }
//    
//    @Test
//    public void testNotEqualToAny(){
//        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
//        criteria.where("d.age").notEq().any().expression("d.partners.age");
//        
//        assertEquals("FROM Document d LEFT JOIN d.partners partners WHERE d.age != ANY(partners.age)", criteria.getQueryString());
//    }
    
    @Test
    public void testEqAll(){
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.where("name").eq().all().from(Person.class, "p").select("id").where("name").eqExpression("d.name").end();
        String expected = "FROM Document d WHERE d.name = ALL(SELECT p.id FROM Person p WHERE p.name = d.name)";
        
        Assert.assertEquals(expected, crit.getQueryString());
    }
    
    @Test
    public void testEqAny(){
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.where("name").eq().any().from(Person.class, "p").select("id").where("name").eqExpression("d.name").end();
        String expected = "FROM Document d WHERE d.name = ANY(SELECT p.id FROM Person p WHERE p.name = d.name)";
        
        Assert.assertEquals(expected, crit.getQueryString());
    }
}
