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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author ccbem
 */
public class InTest extends AbstractPersistenceTest {
    @Test
    public void testIn(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        List<Integer> ages = new ArrayList<Integer>(Arrays.asList(new Integer[]{1,2,3,4,5}));
        criteria.where("d.age").in(ages);
        
        assertEquals("FROM Document d WHERE d.age IN (:param_0)", criteria.getQueryString());
    }
    
    @Test(expected = NullPointerException.class)
    public void testInNull(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.age").in(null);   
    }
    
    @Test
    public void testNotIn(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        List<Integer> ages = new ArrayList<Integer>(Arrays.asList(new Integer[]{1,2,3,4,5}));
        criteria.where("d.age").notIn(ages);
        assertEquals("FROM Document d WHERE NOT d.age IN (:param_0)", criteria.getQueryString());
    }
    
    @Test(expected = NullPointerException.class)
    public void testNotInNull(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.age").notIn(null);
    }
    
}
