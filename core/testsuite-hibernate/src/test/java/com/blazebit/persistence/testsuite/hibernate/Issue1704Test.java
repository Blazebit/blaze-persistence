/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.hibernate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Where;

import org.junit.Assert;
import org.junit.Test;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Tuple;

/**
 * @author Christian Beikov
 * @since 1.6.10
 */
public class Issue1704Test extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
            WhereEntity.class
        };
    }

    @Test
    public void testLateralLimit() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                WhereEntity e = new WhereEntity();
                e.id = 1L;
                e.name = "Test";
                em.persist(e);
            }
        });

        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class)
            .select("w").select("wLatest")
            .from(WhereEntity.class, "w")
            .leftJoinLateralEntitySubquery("w.children", "wLatest", "wSub")
                .where("wSub.name").eqExpression("w.name")
                .orderByDesc("wSub.id")
                .setMaxResults(1)
            .end();

        List<Tuple> result = cb.getResultList();
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(Long.valueOf(1L), result.get(0).get(0, WhereEntity.class).getId());
        Assert.assertNull(result.get(0).get(1));
    }

    @Entity(name = "WhereEntity")
    @Where(clause = "0=0")
    @SQLRestriction("0=0")
    public static class WhereEntity {

        private Long id;
        private String name;
        private WhereEntity parent;
        private Set<WhereEntity> children = new HashSet<>();

        @Id
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }

        @ManyToOne(fetch = FetchType.LAZY)
        public WhereEntity getParent() {
            return parent;
        }
        public void setParent(WhereEntity parent) {
            this.parent = parent;
        }

        @OneToMany(mappedBy = "parent")
        public Set<WhereEntity> getChildren() {
            return children;
        }
        public void setChildren(Set<WhereEntity> children) {
            this.children = children;
        }
    }

}
