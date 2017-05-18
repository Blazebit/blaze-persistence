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

package com.blazebit.persistence.criteria;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.*;
import com.googlecode.catchexception.CatchException;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.Tuple;

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
    public void selectTreatedRoot() {
        BlazeCriteriaQuery<PolymorphicSub1> cq = BlazeCriteria.get(cbf, PolymorphicSub1.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        BlazeRoot<PolymorphicBase> root = cq.from(PolymorphicBase.class, "base");
        cq.select(cb.treat(root, PolymorphicSub1.class));

        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT base FROM PolymorphicBase base WHERE TYPE(base) = PolymorphicSub1", criteriaBuilder.getQueryString());
    }

    @Test
    public void multipleTreatedRootInWhere() {
        BlazeCriteriaQuery<PolymorphicBase> cq = BlazeCriteria.get(cbf, PolymorphicBase.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        BlazeRoot<PolymorphicBase> root = cq.from(PolymorphicBase.class, "base");
        cq.where(cb.or(
                cb.treat(root, PolymorphicSub1.class).get(PolymorphicSub1_.sub1Value).isNotNull(),
                cb.treat(root, PolymorphicSub2.class).get(PolymorphicSub2_.sub2Value).isNotNull()
        ));

        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);
        String whereFragment = "";
        whereFragment += treatRootWhereFragment("base", PolymorphicSub1.class, ".sub1Value IS NOT NULL", false);
        whereFragment += " OR " + treatRootWhereFragment("base", PolymorphicSub2.class, ".sub2Value IS NOT NULL", false);
        assertEquals("SELECT base FROM PolymorphicBase base" +
                " WHERE " + whereFragment, criteriaBuilder.getQueryString());
    }

    @Test
    public void treatRoot() {
        BlazeCriteriaQuery<Integer> cq = BlazeCriteria.get(cbf, Integer.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        BlazeRoot<PolymorphicBase> root = cq.from(PolymorphicBase.class, "base");
        cq.select(cb.treat(root, PolymorphicSub1.class).get(PolymorphicSub1_.sub1Value));
        cq.where(cb.treat(root, PolymorphicSub1.class).get("sub1Value").isNotNull());

        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);
        String whereFragment = treatRootWhereFragment("base", PolymorphicSub1.class, ".sub1Value IS NOT NULL", false);
        assertEquals("SELECT " + treatRoot("base", PolymorphicSub1.class, "sub1Value") + " FROM PolymorphicBase base WHERE " + whereFragment, criteriaBuilder.getQueryString());
    }

    @Test
    // Eclipselink does not support dereferencing of TREAT join path elements
    @Category({ NoDatanucleus.class, NoEclipselink.class })
    public void treatRootJoin() {
        BlazeCriteriaQuery<PolymorphicBase> cq = BlazeCriteria.get(cbf, PolymorphicBase.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        BlazeRoot<PolymorphicBase> root = cq.from(PolymorphicBase.class, "base");
        BlazeJoin<PolymorphicSub1, PolymorphicBase> join = cb.treat(root, PolymorphicSub1.class).join(PolymorphicSub1_.parent1, "p1");
        cq.select(join);

        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT p1 FROM PolymorphicBase base" +
                " JOIN " + treatRootJoin("base", PolymorphicSub1.class, "parent1") + " p1", criteriaBuilder.getQueryString());
    }

    @Test
    public void multipleDistinctTreatJoin() {
        BlazeCriteriaQuery<PolymorphicBase> cq = BlazeCriteria.get(cbf, PolymorphicBase.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        BlazeRoot<PolymorphicBase> root = cq.from(PolymorphicBase.class, "base");
        BlazeJoin<PolymorphicBase, PolymorphicBase> join = root.join(PolymorphicBase_.parent, "p1");

        cb.treat(join, PolymorphicSub1.class);
        CatchException.verifyException(cb, IllegalArgumentException.class).treat(join, PolymorphicSub2.class);
    }

    @Test
    public void treatJoin() {
        BlazeCriteriaQuery<Tuple> cq = BlazeCriteria.get(cbf, Tuple.class);
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

        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);
        String whereFragment = null;
        whereFragment = treatJoinWhereFragment(PolymorphicBase.class, "children", "child", PolymorphicSub1.class, com.blazebit.persistence.JoinType.INNER, whereFragment);
        whereFragment = treatJoinWhereFragment(PolymorphicBase.class, "list", "list", PolymorphicSub1.class, com.blazebit.persistence.JoinType.INNER, whereFragment);
        whereFragment = treatJoinWhereFragment(PolymorphicBase.class, "map", "map", PolymorphicSub1.class, com.blazebit.persistence.JoinType.INNER, whereFragment);
        whereFragment = treatJoinWhereFragment(PolymorphicBase.class, "parent", "parent", PolymorphicSub1.class, com.blazebit.persistence.JoinType.INNER, whereFragment);
        whereFragment = treatJoinWhereFragment(PolymorphicBase.class, "parent", "parent2", PolymorphicSub2.class, com.blazebit.persistence.JoinType.INNER, whereFragment);
        assertEquals("SELECT parent.sub1Value, list.sub1Value, child.sub1Value, " + joinAliasValue("map", "sub1Value") + ", INDEX(list), KEY(map)" +
                " FROM PolymorphicBase base" +
                " JOIN " + treatJoin("base.children", PolymorphicSub1.class) + " child" +
                " JOIN " + treatJoin("base.list", PolymorphicSub1.class) + " list" +
                " JOIN " + treatJoin("base.map", PolymorphicSub1.class) + " map" +
                " JOIN " + treatJoin("base.parent", PolymorphicSub1.class) + " parent" +
                " JOIN " + treatJoin("base.parent", PolymorphicSub2.class) + " parent2" + whereFragment, criteriaBuilder.getQueryString());
    }

    @Test
    // Eclipselink does not support dereferencing of TREAT join path elements
    @Category({ NoDatanucleus.class, NoEclipselink.class })
    public void joinTreatedJoinWithOnClause() {
        BlazeCriteriaQuery<Tuple> cq = BlazeCriteria.get(cbf, Tuple.class);
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

        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);
        String whereFragment = null;
        whereFragment = treatJoinWhereFragment(PolymorphicBase.class, "children", "child", PolymorphicSub1.class, com.blazebit.persistence.JoinType.INNER, whereFragment);
        whereFragment = treatJoinWhereFragment(PolymorphicBase.class, "list", "list", PolymorphicSub1.class, com.blazebit.persistence.JoinType.INNER, whereFragment);
        whereFragment = treatJoinWhereFragment(PolymorphicBase.class, "map", "map", PolymorphicSub1.class, com.blazebit.persistence.JoinType.INNER, whereFragment);
        whereFragment = treatJoinWhereFragment(PolymorphicBase.class, "parent", "parent", PolymorphicSub1.class, com.blazebit.persistence.JoinType.INNER, whereFragment);
        whereFragment = treatJoinWhereFragment(PolymorphicBase.class, "parent", "parent2", PolymorphicSub2.class, com.blazebit.persistence.JoinType.INNER, whereFragment);
        assertEquals("SELECT TYPE(parent), TYPE(list), TYPE(child), TYPE(map), TYPE(KEY(map)), TYPE(parent_1), TYPE(relation1_1) " +
                "FROM PolymorphicBase base" +
                " JOIN " + treatJoin("base.children", PolymorphicSub1.class) + " child" +
                onClause(treatJoinedConstraintFragment("child", PolymorphicSub1.class, ".sub1Value IS NOT NULL", false)) +
                " JOIN child.relation1 setRelation1" +
                " JOIN " + treatJoin("base.list", PolymorphicSub1.class) + " list" +
                onClause(treatJoinedConstraintFragment("list", PolymorphicSub1.class, ".sub1Value IS NOT NULL", false)) +
                " JOIN list.relation1 listRelation1" +
                " JOIN " + treatJoin("base.map", PolymorphicSub1.class) + " map" +
                onClause(treatJoinedConstraintFragment("map", PolymorphicSub1.class, ".sub1Value IS NOT NULL", false)) +
                " JOIN map.relation1 mapRelation1" +
                " JOIN " + treatJoin("base.parent", PolymorphicSub1.class) + " parent" +
                onClause(treatJoinedConstraintFragment("parent", PolymorphicSub1.class, ".sub1Value IS NOT NULL", false)) +
                " JOIN parent.relation1 parentRelation1" +
                " JOIN " + treatJoin("base.parent", PolymorphicSub2.class) + " parent2" +
                onClause(treatJoinedConstraintFragment("parent2", PolymorphicSub2.class, ".sub2Value IS NOT NULL", false)) +
                " JOIN parent2.relation2 parent2Relation2" +
                " LEFT JOIN base.parent parent_1" +
                " LEFT JOIN parent_1.relation1 relation1_1" + whereFragment, criteriaBuilder.getQueryString());
    }

    @Test
    public void treatPath() {
        BlazeCriteriaQuery<Integer> cq = BlazeCriteria.get(cbf, Integer.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        BlazeRoot<PolymorphicBase> root = cq.from(PolymorphicBase.class, "base");
        cq.select(cb.treat(root.get(PolymorphicBase_.parent), PolymorphicSub1.class).get(PolymorphicSub1_.sub1Value));
        cq.where(cb.treat(root.get(PolymorphicBase_.parent), PolymorphicSub1.class).get(PolymorphicSub1_.sub1Value).isNotNull());

        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);
        String whereFragment = treatRootWhereFragment("parent_1", PolymorphicSub1.class, ".sub1Value IS NOT NULL", false);
        assertEquals("SELECT " + treatRoot("parent_1", PolymorphicSub1.class, "sub1Value") +
                " FROM PolymorphicBase base" +
                " LEFT JOIN base.parent parent_1" +
                " WHERE " + whereFragment, criteriaBuilder.getQueryString());
    }

}
