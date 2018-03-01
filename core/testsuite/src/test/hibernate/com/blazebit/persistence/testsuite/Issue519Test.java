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
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.Subselect;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class Issue519Test extends AbstractCoreTest {

    A instance;

    @Before
    public void setUp() throws Exception {
        instance = new A();
        em.persist(instance);
        em.refresh(instance);
    }

    @Test
    public void subselectEntityTest() {
        CriteriaBuilder<A> a1 = cbf.create(em, A.class)
                .from(A.class, "a")
                .where("a.b.b").eq(5l);

        String queryString = a1.getQueryString();
        List<A> resultList = a1.getResultList();
        assertTrue(resultList.contains(instance));
    }

    @Entity
    @Table(name = "A")
    public static class A {

        @Id
        @GeneratedValue
        private Long id;

        @Generated(GenerationTime.INSERT)
        @OneToOne(mappedBy = "a")
        private B b;


    }

    @Entity
    @Subselect("SELECT 5 as b, a.id AS aId FROM A")
    public static class B {

        private Long aId;

        private A a;

        private Long b;

        @Id
        public Long getaId() {
            return aId;
        }

        public void setaId(Long aId) {
            this.aId = aId;
        }

        @OneToOne
        @PrimaryKeyJoinColumn(name = "a_id")
        public A getA() {
            return a;
        }

        public void setA(A a) {
            this.a = a;
        }

        @Column
        public Long getB() {
            return b;
        }

        public void setB(Long b) {
            this.b = b;
        }
    }

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] { A.class, B.class };
    }
}
