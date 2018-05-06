/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.testsuite;

import static org.junit.Assert.assertEquals;

import com.blazebit.persistence.Criteria;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.spi.DbmsDialect;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDB2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoFirebird;
import com.blazebit.persistence.testsuite.base.jpa.category.NoH2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOracle;
import com.blazebit.persistence.testsuite.base.jpa.category.NoPostgreSQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoSQLite;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMSSQL;
import com.blazebit.persistence.testsuite.entity.Document;

/**
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
public class GroupByTest extends AbstractCoreTest {

    // Datanucleus does not support grouping by a byte[] as it seems
    @Test
    @Category({ NoDatanucleus.class })
    public void testGroupByEntitySelect() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d");
        criteria.groupBy("d.owner");
        assertEquals("SELECT d FROM Document d JOIN d.owner owner_1 GROUP BY owner_1, d.age, d.archived, d.byteArray, d.creationDate, d.creationDate2, d.defaultContact, d.documentType, d.id, d.idx, d.intIdEntity, d.lastModified, d.lastModified2, d.name, d.nameContainer, d.nameObject, d.nonJoinable, d.owner, d.parent, d.responsiblePerson, d.someValue, d.version, d.wrappedByteArray", criteria.getQueryString());
        criteria.getResultList();
    }
    
    /*
     * Some databases like DB2, SQL Server and Oracle do not support group bys with parameter markers.
     * Thus, for these DBs the group by should contain all subexpressions instead.
     *
     * SQL Server bug? https://support.microsoft.com/en-us/kb/2873474#
     * For DB2, parameters in group by are problematic: https://groups.google.com/forum/#!topic/comp.databases.ibm-db2/yhg4wNk4IT0
     * Oracle does not allow parameters in the group by
     */
    @Test
    @Category({NoH2.class, NoPostgreSQL.class, NoMySQL.class, NoFirebird.class, NoSQLite.class})
    public void testSizeTransformWithImplicitParameterGroupBy1() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d")
                .select("SIZE(d.versions)")
                .selectCase().when("d.age").lt(2l).thenExpression("'a'").otherwiseExpression("'b'");

        final String expected = "SELECT " + function("COUNT_TUPLE", "versions_1.id") + ", CASE WHEN d.age < :param_0 THEN 'a' ELSE 'b' END FROM Document d LEFT JOIN d.versions versions_1 GROUP BY d.id, d.age";
        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    @Category({ NoDB2.class, NoMSSQL.class, NoOracle.class })
    public void testSizeTransformWithImplicitParameterGroupBy2() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d")
                .select("SIZE(d.versions)")
                .selectCase().when("d.age").lt(2l).thenExpression("'a'").otherwiseExpression("'b'");
        
        final String expected = "SELECT " + function("COUNT_TUPLE", "versions_1.id") + ", CASE WHEN d.age < :param_0 THEN 'a' ELSE 'b' END FROM Document d LEFT JOIN d.versions versions_1 " +
                "GROUP BY d.id, " + groupByPathExpressions("CASE WHEN d.age < :param_0 THEN 'a' ELSE 'b' END", "d.age");
        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testGroupByKeyExpression() {
        CriteriaBuilderConfiguration config = Criteria.getDefault();
        config = configure(config);
        config.registerDialect(dbms, new DelegatingDbmsDialect(cbf.getService(DbmsDialect.class)) {
            @Override
            public boolean supportsComplexGroupBy() {
                return false;
            }
        });
        cbf = config.createCriteriaBuilderFactory(em.getEntityManagerFactory());
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class);
        criteria.from(Document.class, "d")
                .select("KEY(contacts)")
                // Assert that a KEY expression is valid in group by even if the DB does not support "complex group bys"
                .groupBy("KEY(contacts)");
        assertEquals("SELECT KEY(contacts_1) FROM Document d LEFT JOIN d.contacts contacts_1 GROUP BY KEY(contacts_1)", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testOmitGroupByLiteral() {
        CriteriaBuilder<Long> cb = cbf.create(em, Long.class)
                .from(Document.class, "d")
                .select("d.id")
                .select("SIZE(d.people)")
                .select("''");

        assertEquals("SELECT d.id, " + function("count_tuple", "INDEX(people_1)") + ", '' FROM Document d LEFT JOIN d.people people_1 GROUP BY d.id", cb.getQueryString());
        cb.getResultList();
    }
}
