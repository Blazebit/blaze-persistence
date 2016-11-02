/*
 * Copyright 2014 - 2016 Blazebit.
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
package com.blazebit.persistence.criteria;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.criteria.impl.BlazeCriteria;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.entity.*;
import com.googlecode.catchexception.CatchException;
import org.junit.Ignore;
import org.junit.Test;

import javax.persistence.Tuple;
import javax.persistence.criteria.JoinType;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Ignore("Treat support is not yet implemented")
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
    public void selectTreatedRoot() {
        BlazeCriteriaQuery<PolymorphicSub1> cq = BlazeCriteria.get(em, cbf, PolymorphicSub1.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        BlazeRoot<PolymorphicBase> root = cq.from(PolymorphicBase.class, "base");
        cq.select(cb.treat(root, PolymorphicSub1.class));

        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder();
        assertEquals("SELECT TREAT(base AS PolymorphicSub1) FROM PolymorphicBase base", criteriaBuilder.getQueryString());
    }

    @Test
    public void treatRoot() {
        BlazeCriteriaQuery<Integer> cq = BlazeCriteria.get(em, cbf, Integer.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        BlazeRoot<PolymorphicBase> root = cq.from(PolymorphicBase.class, "base");
        cq.select(cb.treat(root, PolymorphicSub1.class).get(PolymorphicSub1_.sub1Value));
        cq.where(cb.treat(root, PolymorphicSub1.class).get("sub1Value").isNotNull());

        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder();
        assertEquals("SELECT TREAT(base AS PolymorphicSub1).sub1Value FROM PolymorphicBase base WHERE TREAT(base AS PolymorphicSub1).sub1Value IS NOT NULL", criteriaBuilder.getQueryString());
    }

    @Test
    public void treatJoin() {
        BlazeCriteriaQuery<Tuple> cq = BlazeCriteria.get(em, cbf, Tuple.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        BlazeRoot<PolymorphicBase> root = cq.from(PolymorphicBase.class, "base");
        BlazeJoin<PolymorphicBase, PolymorphicSub1> treatedJoin = cb.treat(root.join(PolymorphicBase_.parent, "parent"), PolymorphicSub1.class);
        BlazeJoin<PolymorphicBase, PolymorphicSub2> treatedJoin2 = cb.treat(root.join(PolymorphicBase_.parent, "parent2"), PolymorphicSub2.class);
        BlazeListJoin<PolymorphicBase, PolymorphicSub1> treatedListJoin = cb.treat(root.join(PolymorphicBase_.list, "list"), PolymorphicSub1.class);
        BlazeSetJoin<PolymorphicBase, PolymorphicSub1> treatedSetJoin = cb.treat(root.join(PolymorphicBase_.children, "child"), PolymorphicSub1.class);
        BlazeMapJoin<PolymorphicBase, String, PolymorphicSub1> treatedMapJoin = cb.treat(root.join(PolymorphicBase_.map, "map"), PolymorphicSub1.class);

        cq.multiselect(
                treatedJoin.get(PolymorphicSub1_.sub1Value),
                treatedListJoin.get(PolymorphicSub1_.sub1Value),
                treatedSetJoin.get(PolymorphicSub1_.sub1Value),
                treatedMapJoin.get(PolymorphicSub1_.sub1Value),
                treatedListJoin.index(),
                treatedMapJoin.key()
        );

        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder();
        assertEquals("SELECT parent.sub1Value, list.sub1Value, child.sub1Value, map.sub1Value, INDEX(list), KEY(map) " +
                "FROM PolymorphicBase base " +
                "JOIN TREAT(base.parent AS PolymorphicSub1) parent " +
                "JOIN TREAT(base.parent AS PolymorphicSub2) parent2 " +
                "JOIN TREAT(base.list AS PolymorphicSub1) list " +
                "JOIN TREAT(base.children AS PolymorphicSub1) child " +
                "JOIN TREAT(base.map AS PolymorphicSub1) map", criteriaBuilder.getQueryString());
    }

    @Test
    public void joinTreatedJoinWithOnClause() {
        BlazeCriteriaQuery<Tuple> cq = BlazeCriteria.get(em, cbf, Tuple.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        BlazeRoot<PolymorphicBase> root = cq.from(PolymorphicBase.class, "base");
        BlazeJoin<PolymorphicBase, PolymorphicSub1> treatedJoin = cb.treat(root.join(PolymorphicBase_.parent, "parent"), PolymorphicSub1.class);
        BlazeJoin<PolymorphicBase, PolymorphicSub2> treatedJoin2 = cb.treat(root.join(PolymorphicBase_.parent, "parent2"), PolymorphicSub2.class);
        BlazeListJoin<PolymorphicBase, PolymorphicSub1> treatedListJoin = cb.treat(root.join(PolymorphicBase_.list, "list"), PolymorphicSub1.class);
        BlazeSetJoin<PolymorphicBase, PolymorphicSub1> treatedSetJoin = cb.treat(root.join(PolymorphicBase_.children, "child"), PolymorphicSub1.class);
        BlazeMapJoin<PolymorphicBase, String, PolymorphicSub1> treatedMapJoin = cb.treat(root.join(PolymorphicBase_.map, "map"), PolymorphicSub1.class);

        treatedJoin.on(treatedJoin.get(PolymorphicSub1_.sub1Value).isNotNull()).join(PolymorphicSub1_.relation1, "parentRelation1");
        treatedJoin2.on(treatedJoin2.get(PolymorphicSub2_.sub2Value).isNotNull()).join(PolymorphicSub2_.relation2, "parent2Relation2");
        treatedListJoin.on(treatedListJoin.get(PolymorphicSub1_.sub1Value).isNotNull()).join(PolymorphicSub1_.relation1, "listRelation1");
        treatedSetJoin.on(treatedSetJoin.get(PolymorphicSub1_.sub1Value).isNotNull()).join(PolymorphicSub1_.relation1, "setRelation1");
        treatedMapJoin.on(treatedMapJoin.get(PolymorphicSub1_.sub1Value).isNotNull()).join(PolymorphicSub1_.relation1, "mapRelation1");

        cq.multiselect(
                treatedJoin.type(),
                treatedListJoin.type(),
                treatedSetJoin.type(),
                treatedMapJoin.type(),
                treatedMapJoin.key().type(),
                cb.treat(root.get(PolymorphicBase_.parent), PolymorphicSub1.class).type(),
                cb.treat(root.get(PolymorphicBase_.parent), PolymorphicSub1.class).get(PolymorphicSub1_.relation1).type()
        );

        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder();
        assertEquals("SELECT TYPE(parent), TYPE(list), TYPE(child), TYPE(map), TYPE(KEY(map)), TYPE(TREAT(base.parent AS PolymorphicSub1)), TYPE(TREAT(base.parent AS PolymorphicSub1).relation1) " +
                "FROM PolymorphicBase base " +
                "JOIN TREAT(base.parent AS PolymorphicSub1) parent " + ON_CLAUSE + " parent.sub1Value IS NOT NULL " +
                "JOIN TREAT(base.parent AS PolymorphicSub2) parent2 " + ON_CLAUSE + " parent2.sub2Value IS NOT NULL " +
                "JOIN TREAT(base.list AS PolymorphicSub1) list " + ON_CLAUSE + " list.sub1Value IS NOT NULL " +
                "JOIN TREAT(base.children AS PolymorphicSub1) child " + ON_CLAUSE + " child.sub1Value IS NOT NULL " +
                "JOIN TREAT(base.map AS PolymorphicSub1) map " + ON_CLAUSE + " map.sub1Value IS NOT NULL " +
                "JOIN parent.relation1 parentRelation1 " +
                "JOIN parent2.relation2 parent2Relation2 " +
                "JOIN list.relation1 listRelation1 " +
                "JOIN child.relation1 setRelation1 " +
                "JOIN map.relation1 mapRelation1", criteriaBuilder.getQueryString());
    }

    @Test
    public void treatPath() {
        BlazeCriteriaQuery<Integer> cq = BlazeCriteria.get(em, cbf, Integer.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        BlazeRoot<PolymorphicBase> root = cq.from(PolymorphicBase.class, "base");
        cq.select(cb.treat(root.get(PolymorphicBase_.parent), PolymorphicSub1.class).get(PolymorphicSub1_.sub1Value));
        cq.where(cb.treat(root.get(PolymorphicBase_.parent), PolymorphicSub1.class).get(PolymorphicSub1_.sub1Value).isNotNull());

        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder();
        assertEquals("SELECT TREAT(base.parent AS PolymorphicSub1).sub1Value FROM PolymorphicBase base WHERE TREAT(base.parent AS PolymorphicSub1).sub1Value IS NOT NULL", criteriaBuilder.getQueryString());
    }

}
