/*
 * Copyright 2014 - 2017 Blazebit.
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
import com.blazebit.persistence.testsuite.base.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.category.NoDatanucleus4;
import com.blazebit.persistence.testsuite.base.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class TreatTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
                IntIdEntity.class,
                PolymorphicBase.class,
                PolymorphicSub1.class,
                PolymorphicSub2.class
        };
    }

    @Test
    // NOTE: Apparently a bug in datanucleus? TODO: report the error
    @Category({ NoDatanucleus.class })
    public void implicitJoinTreatedRoot() {
        CriteriaBuilder<Integer> criteria = cbf.create(em, Integer.class);
        criteria.from(PolymorphicBase.class, "p");
        criteria.select("TREAT(p AS PolymorphicSub1).sub1Value");
        assertEquals("SELECT " + treatRoot("p", PolymorphicSub1.class, "sub1Value") + " FROM PolymorphicBase p", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void implicitJoinTreatedRelation() {
        CriteriaBuilder<Integer> criteria = cbf.create(em, Integer.class);
        criteria.from(PolymorphicBase.class, "p");
        criteria.select("TREAT(p.parent AS PolymorphicSub1).sub1Value");
        assertEquals("SELECT " + treatRoot("parent_1", PolymorphicSub1.class, "sub1Value") + " FROM PolymorphicBase p LEFT JOIN p.parent parent_1", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    // NOTE: Datanucleus4 reports: We do not currently support JOIN to TREAT
    @Category({ NoDatanucleus4.class })
    public void joinTreatedRelation() {
        CriteriaBuilder<Integer> criteria = cbf.create(em, Integer.class);
        criteria.from(PolymorphicBase.class, "p");
        criteria.select("polymorphicSub1.sub1Value");
        criteria.innerJoin("TREAT(p.parent AS PolymorphicSub1)", "polymorphicSub1");
        assertEquals("SELECT polymorphicSub1.sub1Value FROM PolymorphicBase p JOIN " + treatJoin("p.parent", PolymorphicSub1.class) + " polymorphicSub1", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    // TODO: This is an extension of the treat grammar. Maybe we should render a cross/left join for the root path treat and then just treat on the other alias?
    // NOTE: Apparently a bug in datanucleus? TODO: report the error
    // Eclipselink does not support dereferencing of TREAT join path elements
    @Category({ NoDatanucleus.class, NoEclipselink.class})
    public void joinTreatedRoot() {
        CriteriaBuilder<Integer> criteria = cbf.create(em, Integer.class);
        criteria.from(PolymorphicBase.class, "p");
        criteria.select("parent1.name");
        criteria.innerJoin("TREAT(p AS PolymorphicSub1).parent1", "parent1");
        assertEquals("SELECT parent1.name FROM PolymorphicBase p JOIN " + treatRootJoin("p", PolymorphicSub1.class, "parent1") + " parent1", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    // TODO: This is an extension of the treat grammar. Maybe we should render a cross/left join for the root path treat and then just treat on the other alias?
    // NOTE: Apparently a bug in datanucleus? TODO: report the error
    // Eclipselink does not support dereferencing of TREAT join path elements
    @Category({ NoDatanucleus.class, NoEclipselink.class })
    public void joinTreatedRootEmbeddable() {
        CriteriaBuilder<Integer> criteria = cbf.create(em, Integer.class);
        criteria.from(PolymorphicBase.class, "p");
        criteria.select("intIdEntity.name");
        criteria.innerJoin("TREAT(p AS PolymorphicSub1).embeddable1.intIdEntity", "intIdEntity");
        assertEquals("SELECT intIdEntity.name FROM PolymorphicBase p JOIN " + treatRootJoin("p", PolymorphicSub1.class, "embeddable1.intIdEntity") + " intIdEntity", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    // TODO: This is an extension of the treat grammar. Maybe we should render a cross/left join for the root path treat and then just treat on the other alias?
    // NOTE: Apparently a bug in datanucleus? TODO: report the error
    // Eclipselink does not support dereferencing of TREAT join path elements
    @Category({ NoDatanucleus.class, NoEclipselink.class })
    public void selectTreatedRootEmbeddable() {
        CriteriaBuilder<Integer> criteria = cbf.create(em, Integer.class);
        criteria.from(PolymorphicBase.class, "p");
        criteria.select("TREAT(p AS PolymorphicSub2).embeddable2.intIdEntity.name");
        assertEquals("SELECT intIdEntity_1.name FROM PolymorphicBase p LEFT JOIN " + treatRootJoin("p", PolymorphicSub2.class, "embeddable2.intIdEntity") + " intIdEntity_1", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    // TODO: This is an extension of the treat grammar. Maybe we should render a cross/left join for the root path treat and then just treat on the other alias?
    // NOTE: Apparently a bug in datanucleus? TODO: report the error
    @Category({ NoDatanucleus.class })
    public void treatJoinTreatedRootRelation() {
        CriteriaBuilder<Integer> criteria = cbf.create(em, Integer.class);
        criteria.from(PolymorphicBase.class, "p");
        criteria.select("polymorphicSub1.sub1Value");
        criteria.innerJoin("TREAT(TREAT(p AS PolymorphicSub1).parent1 AS PolymorphicSub1)", "polymorphicSub1");
        assertEquals("SELECT polymorphicSub1.sub1Value FROM PolymorphicBase p JOIN " + treatJoin(treatRootJoin("p", PolymorphicSub1.class, "parent1"), PolymorphicSub1.class) + " polymorphicSub1", criteria.getQueryString());
        criteria.getResultList();
    }
}
