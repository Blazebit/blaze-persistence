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

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.entity.DocumentForOneToOneJoinTable;
import com.blazebit.persistence.testsuite.entity.DocumentForOneToOne;
import com.blazebit.persistence.testsuite.entity.DocumentInfo;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.Tuple;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
// NOTE: Datanucleus generates wrong DDL for OneToOne JoinTable mappings: https://github.com/datanucleus/datanucleus-core/issues/196
@Category({ NoDatanucleus.class })
public class SingleValuedAssociationOneToOneJoinTableTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return concat(super.getEntityClasses(), new Class[]{
                DocumentForOneToOneJoinTable.class,
                DocumentForOneToOne.class,
                DocumentInfo.class
        });
    }

    @Test
    public void oneToOneJoinTableSingleValuedAssociationIsNull() {
        CriteriaBuilder<DocumentForOneToOneJoinTable> criteria = cbf.create(em, DocumentForOneToOneJoinTable.class, "d");
        criteria.where("d.documentInfoJoinTable").isNull();

        assertEquals("SELECT d FROM DocumentForOneToOneJoinTable d WHERE d.documentInfoJoinTable IS NULL", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void oneToOneJoinTableSingleValuedAssociationRelativeIdAccess() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(DocumentForOneToOneJoinTable.class, "d")
                .select("documentInfoJoinTable.id");
        String expectedQuery = "SELECT documentInfoJoinTable_1.id FROM DocumentForOneToOneJoinTable d LEFT JOIN d.documentInfoJoinTable documentInfoJoinTable_1";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }

    @Test
    public void oneToOneJoinTableSingleValuedAssociationAbsoluteIdAccess() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(DocumentForOneToOneJoinTable.class, "d")
                .select("d.documentInfoJoinTable.id");
        String expectedQuery = "SELECT documentInfoJoinTable_1.id FROM DocumentForOneToOneJoinTable d LEFT JOIN d.documentInfoJoinTable documentInfoJoinTable_1";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }

    @Test
    public void oneToOneJoinTableSingleValuedAssociationIdAccessJoinOverride1() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(DocumentForOneToOneJoinTable.class, "d")
                .select("d.documentInfoJoinTable.id")
                .leftJoinDefault("documentInfoJoinTable", "o");
        String expectedQuery = "SELECT o.id FROM DocumentForOneToOneJoinTable d LEFT JOIN d.documentInfoJoinTable o";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }

    @Test
    public void oneToOneJoinTableSingleValuedAssociationIdAccessJoinOverride2() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(DocumentForOneToOneJoinTable.class, "d")
                .select("o.id")
                .leftJoinDefault("documentInfoJoinTable", "o");
        String expectedQuery = "SELECT o.id FROM DocumentForOneToOneJoinTable d LEFT JOIN d.documentInfoJoinTable o";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }

    @Test
    public void oneToOneJoinTableSingleValuedAssociationIdAccessJoinOverride3() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(DocumentForOneToOneJoinTable.class, "d")
                .select("o.id")
                .leftJoin("documentInfoJoinTable", "o");
        String expectedQuery = "SELECT o.id FROM DocumentForOneToOneJoinTable d LEFT JOIN d.documentInfoJoinTable o";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }
}
