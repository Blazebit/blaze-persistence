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
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author ccbem
 */
public class SelectTest extends AbstractPersistenceTest {
    @Test
    public void testSelectNonJoinable(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("nonJoinable");
        
        assertEquals("SELECT d.nonJoinable FROM Document d", criteria.getQueryString());
    }
    
    @Test
    public void testSelectNonJoinablePrefixed(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("d.nonJoinable");
        
        assertEquals("SELECT d.nonJoinable FROM Document d", criteria.getQueryString());
    }
    
    @Test
    public void testSelectJoinable(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("partners");
        
        assertEquals("SELECT partners FROM Document d LEFT JOIN d.partners partners", criteria.getQueryString());
    }
    
    @Test
    public void testSelectJoinablePrefixed(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("d.partners");
        
        assertEquals("SELECT partners FROM Document d LEFT JOIN d.partners partners", criteria.getQueryString());
    }
    
    @Test
    public void testSelectScalarExpression(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("d.partners + 1");
        
        assertEquals("SELECT partners+1 FROM Document d LEFT JOIN d.partners partners", criteria.getQueryString());
    }
    
    @Test
    public void testSelectMultiple(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("d.partners").select("d.versions");
        
        assertEquals("SELECT partners, versions FROM Document d LEFT JOIN d.partners partners LEFT JOIN d.versions versions", criteria.getQueryString());
    }
    
    @Test
    public void testSelectAlias(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("d.partners", "p").where("p").eq(2);
        
        assertEquals("SELECT partners AS p FROM Document d LEFT JOIN d.partners partners WHERE partners = :param_0", criteria.getQueryString());
    }
    
    @Test
    public void testSelectAliasReplacement(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("d.partners", "p").where("partners").eq(2);
        
        assertEquals("SELECT partners AS p FROM Document d LEFT JOIN d.partners partners WHERE partners = :param_0", criteria.getQueryString());
    }
    
    @Test
    public void testSelectAliasJoin(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("d.partners", "p").where("p").eq(2);
        
        assertEquals("SELECT partners AS p FROM Document d LEFT JOIN d.partners partners WHERE partners = :param_0", criteria.getQueryString());
    }
    
    @Test
    public void testSelectAliasJoin2(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("d.versions.date", "x").where("SIZE(d.partners)").eq(2);
        
        assertEquals("SELECT versions.date AS x FROM Document d LEFT JOIN d.partners partners LEFT JOIN d.versions versions WHERE SIZE(d.partners) = :param_0", criteria.getQueryString());
    }
    
    @Test
    public void testSelectAliasJoin3(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("d").select("C.name").innerJoin("d.versions", "B").innerJoin("B.document", "C");
        
        assertEquals("SELECT d, C.name FROM Document d JOIN d.versions B JOIN B.document C", criteria.getQueryString());
    }
    
    @Test
    public void testSelectAliasJoin4(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("d").select("C.name", "X").innerJoin("d.versions", "B").innerJoin("B.document", "C").where("X").eqExpression("B.id");
        
        assertEquals("SELECT d, C.name AS X FROM Document d JOIN d.versions B JOIN B.document C WHERE C.name = B.id", criteria.getQueryString());
    }
    
    @Test
    public void testSelectAliasJoin5(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("d.partners.name");
        
        assertEquals("SELECT partners.name FROM Document d LEFT JOIN d.partners partners", criteria.getQueryString());
    }
    
    @Test
    public void testSelectAliasJoin6(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "a");
        criteria.select("a.versions");
        
        assertEquals("SELECT versions FROM Document a LEFT JOIN a.versions versions", criteria.getQueryString());
    }
    
    @Test
    public void testSelectAliasJoin7(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        
        // we have already solved this for join aliases so we should also solve it here
        criteria.select("test.name", "fieldAlias").where("test.name").eq("bla").join("owner", "test", JoinType.LEFT, false);
        
        assertEquals("SELECT test.name AS fieldAlias FROM Document d LEFT JOIN d.owner test WHERE test.name = :param_0", criteria.getQueryString());
    }
    
    @Test
    public void testAmbiguousAliasReplacement(){
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.select("name", "name").where("d.name").eq("abc").getQueryString();
        
        String expected = "SELECT d.name AS name FROM Document d WHERE d.name = :param_0";
        
        assertEquals(expected, crit.getQueryString());
    }
        
    @Test(expected = IllegalArgumentException.class)
    public void testSelectSingleEmpty(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSelectMultipleEmpty(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("", "");
    }
    
    @Test(expected = NullPointerException.class)
    public void testSelectNull(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select((String) null);
    }
    
    @Test(expected = NullPointerException.class)
    public void testSelectArrayNull(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select((String) null);
    }
    
    @Test
    public void removeMe(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("contacts");
    }
}
