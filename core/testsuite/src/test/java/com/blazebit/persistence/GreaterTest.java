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
import com.blazebit.persistence.spi.Criteria;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author ccbem
 */
public class GreaterTest extends AbstractPersistenceTest {
    @Test
    public void testGt(){
        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class, "d");
        criteria.where("d.age").gt(20);
        
        assertEquals("FROM Document d WHERE d.age > :param_0", criteria.getQueryString());
    }
    
    @Test(expected = NullPointerException.class)
    public void testGtNull(){
        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class, "d");
        criteria.where("d.age").gt(null);        
    }
    
    @Test
    public void testGtExpression(){
        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class, "d");
        criteria.where("d.age").gtExpression("d.owner.name");
        
        assertEquals("FROM Document d LEFT JOIN d.owner owner WHERE d.age > owner.name", criteria.getQueryString());
    }
    
    @Test(expected = NullPointerException.class)
    public void testGtExpressionNull(){
        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class, "d");
        criteria.where("d.age").gtExpression(null);        
    }
    
    @Test
    public void testGe(){
        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class, "d");
        criteria.where("d.age").ge(20);
        
        assertEquals("FROM Document d WHERE d.age >= :param_0", criteria.getQueryString());
    }
    
    @Test(expected = NullPointerException.class)
    public void testGeNull(){
        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class, "d");
        criteria.where("d.age").ge(null);        
    }
    
    @Test
    public void testGeExpression(){
        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class, "d");
        criteria.where("d.age").geExpression("d.owner.name");
        
        assertEquals("FROM Document d LEFT JOIN d.owner owner WHERE d.age >= owner.name", criteria.getQueryString());
    }
    
    @Test(expected = NullPointerException.class)
    public void testGeExpressionNull(){
        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class, "d");
        criteria.where("d.age").geExpression(null);        
    }
}
