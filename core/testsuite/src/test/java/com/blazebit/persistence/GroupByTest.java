/*
 * Copyright 2015 Blazebit.
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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.entity.Document;
import com.blazebit.persistence.testsuite.base.category.NoDB2;
import com.blazebit.persistence.testsuite.base.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.category.NoFirebird;
import com.blazebit.persistence.testsuite.base.category.NoH2;
import com.blazebit.persistence.testsuite.base.category.NoMySQL;
import com.blazebit.persistence.testsuite.base.category.NoOracle;
import com.blazebit.persistence.testsuite.base.category.NoPostgreSQL;
import com.blazebit.persistence.testsuite.base.category.NoSQLite;

/**
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
public class GroupByTest extends AbstractCoreTest {

    @Test
    public void testGroupByEntitySelect() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d");
        criteria.groupBy("d.owner");
        assertEquals("SELECT d FROM Document d JOIN d.owner owner_1 GROUP BY owner_1, d.age, d.archived, d.creationDate, d.creationDate2, d.documentType, d.id, d.idx, d.intIdEntity, d.lastModified, d.lastModified2, d.name, d.nonJoinable, d.owner", criteria.getQueryString());
        criteria.getResultList();
    }
    
    /**
     * Some databases like DB2 do not support group bys with parameter markers.
     */
    @Test
    @Category({NoH2.class, NoPostgreSQL.class, NoMySQL.class, NoFirebird.class, NoOracle.class, NoSQLite.class})
    public void testSizeTransformWithImplicitParameterGroupBy1() {
    	CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d")
    			.select("SIZE(d.versions)")
    			.selectCase().when("d.age").lt(2l).thenExpression("'a'").otherwiseExpression("'b'");
    	
    	final String expected = "SELECT (SELECT COUNT(versions) FROM Document document LEFT JOIN document.versions versions WHERE document = d), CASE WHEN d.age < :param_0 THEN 'a' ELSE 'b' END FROM Document d";
    	assertEquals(expected, criteria.getQueryString());
    	criteria.getResultList();
    }
    
    // NOTE: Datanucleus does not support parameters in group by yet. see http://www.datanucleus.org/servlet/jira/browse/NUCRDBMS-1009
    @Test
    @Category({NoDB2.class, NoDatanucleus.class})
    public void testSizeTransformWithImplicitParameterGroupBy2() {
    	CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d")
    			.select("SIZE(d.versions)")
    			.selectCase().when("d.age").lt(2l).thenExpression("'a'").otherwiseExpression("'b'");
    	
    	final String expected = "SELECT COUNT(versions_1), CASE WHEN d.age < :param_0 THEN 'a' ELSE 'b' END FROM Document d LEFT JOIN d.versions versions_1 GROUP BY d.id, CASE WHEN d.age < :param_0 THEN 'a' ELSE 'b' END";
    	assertEquals(expected, criteria.getQueryString());
    	criteria.getResultList();
    }
}
