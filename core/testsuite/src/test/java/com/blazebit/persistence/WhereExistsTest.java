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
import org.junit.Test;

/**
 *
 * @author ccbem
 */
public class WhereExistsTest extends AbstractPersistenceTest {
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
}
