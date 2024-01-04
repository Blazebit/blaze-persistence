/*
 * Copyright 2014 - 2024 Blazebit.
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
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus4;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate43;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate50;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQLOld;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.hibernate.annotations.Where;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Tuple;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    // NOTE: Old MySQL do not support lateral joins
    // NOTE: Requires entity joins which are supported since Hibernate 5.1, Datanucleus 5 and latest Eclipselink
    @Test
    @Category({ NoMySQLOld.class, NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus4.class, NoOpenJPA.class })
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
