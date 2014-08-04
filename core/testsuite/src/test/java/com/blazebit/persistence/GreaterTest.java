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
public class GreaterTest extends AbstractCoreTest {
    @Test
    public void testGt(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.age").gt(20);
        
        assertEquals("FROM Document d WHERE d.age > :param_0", criteria.getQueryString());
    }
    
    @Test(expected = NullPointerException.class)
    public void testGtNull(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.age").gt(null);        
    }
    
    @Test
    public void testGtExpression(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.age").gtExpression("d.owner.name");
        
        assertEquals("FROM Document d JOIN d.owner owner WHERE d.age > owner.name", criteria.getQueryString());
    }
    
    @Test(expected = NullPointerException.class)
    public void testGtExpressionNull(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.age").gtExpression(null);        
    }
    
    @Test
    public void testGe(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.age").ge(20);
        
        assertEquals("FROM Document d WHERE d.age >= :param_0", criteria.getQueryString());
    }
    
    @Test(expected = NullPointerException.class)
    public void testGeNull(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.age").ge(null);        
    }
    
    @Test
    public void testGeExpression(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.age").geExpression("d.owner.name");
        
        assertEquals("FROM Document d JOIN d.owner owner WHERE d.age >= owner.name", criteria.getQueryString());
    }
    
    @Test(expected = NullPointerException.class)
    public void testGeExpressionNull(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.age").geExpression(null);        
    }
    
    @Test
    public void testGeAll(){
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.where("name").ge().all().from(Person.class, "p").select("id").where("name").eqExpression("d.name").end();
        String expected = "FROM Document d WHERE d.name >= ALL(SELECT p.id FROM Person p WHERE p.name = d.name)";
        
        Assert.assertEquals(expected, crit.getQueryString());
    }
    
    @Test
    public void testGeAny(){
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.where("name").ge().any().from(Person.class, "p").select("id").where("name").eqExpression("d.name").end();
        String expected = "FROM Document d WHERE d.name >= ANY(SELECT p.id FROM Person p WHERE p.name = d.name)";
        
        Assert.assertEquals(expected, crit.getQueryString());
    }
    
    @Test
    public void testGeOne(){
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.where("name").ge().from(Person.class, "p").select("id").where("name").eqExpression("d.name").end();
        String expected = "FROM Document d WHERE d.name >= (SELECT p.id FROM Person p WHERE p.name = d.name)";
        
        Assert.assertEquals(expected, crit.getQueryString());
    }
    
    @Test
    public void testGtAll(){
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.where("name").gt().all().from(Person.class, "p").select("id").where("name").eqExpression("d.name").end();
        String expected = "FROM Document d WHERE d.name > ALL(SELECT p.id FROM Person p WHERE p.name = d.name)";
        
        Assert.assertEquals(expected, crit.getQueryString());
    }
    
    @Test
    public void testGtAny(){
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.where("name").gt().any().from(Person.class, "p").select("id").where("name").eqExpression("d.name").end();
        String expected = "FROM Document d WHERE d.name > ANY(SELECT p.id FROM Person p WHERE p.name = d.name)";
        
        Assert.assertEquals(expected, crit.getQueryString());
    }
    
    @Test
    public void testGtOne(){
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.where("name").gt().from(Person.class, "p").select("id").where("name").eqExpression("d.name").end();
        String expected = "FROM Document d WHERE d.name > (SELECT p.id FROM Person p WHERE p.name = d.name)";
        
        Assert.assertEquals(expected, crit.getQueryString());
    }
}
