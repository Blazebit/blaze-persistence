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
public class IsMemberOfTest extends AbstractPersistenceTest {
    @Test
    public void testIsMemberOf(){
        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class, "d");
        criteria.where("d.name").isMemberOf("d.versions.document.name");
        
        assertEquals("FROM Document d LEFT JOIN d.versions versions LEFT JOIN versions.document document WHERE d.name MEMBER OF document.name", criteria.getQueryString());
    }
    
    @Test(expected = NullPointerException.class)
    public void testIsMemberOfNull(){
        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class, "d");
        criteria.where("d.name").isMemberOf(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testIsMemberOfEmpty(){
        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class, "d");
        criteria.where("d.name").isMemberOf("");
    }
    
    @Test
    public void testIsNotMemberOf(){
        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class, "d");
        criteria.where("d.name").isNotMemberOf("d.versions.document.name");
        
        assertEquals("FROM Document d LEFT JOIN d.versions versions LEFT JOIN versions.document document WHERE NOT d.name MEMBER OF document.name", criteria.getQueryString());
    }
    
    @Test(expected = NullPointerException.class)
    public void testIsNotMemberOfNull(){
        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class, "d");
        criteria.where("d.name").isNotMemberOf(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testIsNotMemberOfEmpty(){
        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class, "d");
        criteria.where("d.name").isNotMemberOf("");
    }
}
