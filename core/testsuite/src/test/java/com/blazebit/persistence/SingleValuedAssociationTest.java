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
import javax.persistence.Tuple;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class SingleValuedAssociationTest extends AbstractCoreTest {
    
    @Test
    public void testSingleValuedAssociationRelativeIdAccess() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("owner.id");
        String expectedQuery = "SELECT d.owner.id FROM Document d";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }
    
    @Test
    public void testSingleValuedAssociationAbsoluteIdAccess() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("d.owner.id");
        String expectedQuery = "SELECT d.owner.id FROM Document d";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }
    
    @Test
    public void testSingleValuedAssociationIdAccessJoinOverride1() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("d.owner.id")
                .leftJoinDefault("owner", "o");
        String expectedQuery = "SELECT o.id FROM Document d LEFT JOIN d.owner o";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }
    
    @Test
    public void testSingleValuedAssociationIdAccessJoinOverride2() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("o.id")
                .leftJoinDefault("owner", "o");
        String expectedQuery = "SELECT o.id FROM Document d LEFT JOIN d.owner o";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }
    
    @Test
    public void testSingleValuedAssociationIdAccessJoinOverride3() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("o.id")
                .leftJoin("owner", "o");
        String expectedQuery = "SELECT o.id FROM Document d LEFT JOIN d.owner o";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }
}
