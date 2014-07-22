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
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import javax.persistence.Parameter;
import javax.persistence.TemporalType;
import javax.persistence.Tuple;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author ccbem
 */
public class ParameterAPITest extends AbstractPersistenceTest{
    @Test(expected = IllegalArgumentException.class)
    public void testIsParameterSet_noSuchParamter(){
        CriteriaBuilder<Document> crit = Criteria.from(em, Document.class);
        crit.isParameterSet("index");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSetParameter_noSuchParamter(){
        CriteriaBuilder<Document> crit = Criteria.from(em, Document.class);
        crit.setParameter("index", 0);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSetDateParameter_noSuchParamter(){
        CriteriaBuilder<Document> crit = Criteria.from(em, Document.class);
        crit.setParameter("index", new Date(), TemporalType.DATE);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSetCalendarParameter_noSuchParamter(){
        CriteriaBuilder<Document> crit = Criteria.from(em, Document.class);
        crit.setParameter("index", Calendar.getInstance(), TemporalType.DATE);
    }
    
    @Test
    public void test(){
        CriteriaBuilder<Document> crit = Criteria.from(em, Document.class);
        crit.select("contacts[:index]")
                .where("contacts[:where_index]").isNotNull()
                .where("name").eq("MyDoc")
                .where("lastModified").lt().expression(":lastModifiedFilter")
                .groupBy("age")
                .having("age").gt().expression(":minAge");
        
        assertFalse(crit.isParameterSet("index"));
        assertFalse(crit.isParameterSet("where_index"));
        assertFalse(crit.isParameterSet("minAge"));
        assertFalse(crit.isParameterSet("lastModifiedFilter"));
        assertTrue(crit.isParameterSet("param_0"));
        
        Set<? extends Parameter> params  = crit.getParameters();
        assertTrue(params.size() == 5);
        for(Parameter p : params){
            if("param_0".equals(p.getName())){
                assertTrue(p.getParameterType().equals(String.class));
            }else{
                assertTrue(p.getParameterType() == null);
            }
            assertTrue(p.getPosition() == null);
        }
        crit.setParameter("index", 1);
        crit.setParameter("where_index", 2);
        crit.setParameter("minAge", 3);
        crit.setParameter("lastModifiedFilter", new Date(), TemporalType.TIMESTAMP);
        
        assertTrue(crit.isParameterSet("index"));
        assertTrue(crit.isParameterSet("where_index"));
        assertTrue(crit.isParameterSet("minAge"));
        assertTrue(crit.isParameterSet("lastModifiedFilter"));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testReservedParameterName1(){
        Criteria.from(em, Document.class).select("contacts[:ids]");
    }
    
    @Test
    public void testUseParameterTwoTimes(){
        CriteriaBuilder<Tuple> cb = Criteria.from(em, Document.class).select(":test")
            .where("contacts[:test]").isNull();
        assertFalse(cb.isParameterSet("test"));
    }
}
