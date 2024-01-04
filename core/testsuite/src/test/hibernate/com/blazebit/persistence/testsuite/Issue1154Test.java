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
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.Subselect;
import org.junit.Ignore;
import org.junit.Test;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class Issue1154Test extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{ A.class, B.class, C.class };
    }

    @Test
    @Ignore("This currently fails due to https://hibernate.atlassian.net/browse/HHH-14201")
    public void reorderAssociationJoinForEntityJoinDependency() {
        CriteriaBuilder<Integer> cb = cbf.create(em, Integer.class)
                .select("1")
                .from(A.class, "a")
                .innerJoinOn(B.class, "b")
                    .on("b.a").eqExpression("a")
                .end()
                .innerJoinOn("a.c", "c")
                    .on("c.b").eqExpression("b")
                .end();

        cb.getResultList();
    }

    @Entity
    @Table(name = "A")
    public static class A {
        @Id
        private Long id;
        @ManyToOne
        private C c;
    }

    @Entity
    @Table(name = "B")
    public static class B {
        @Id
        private Long id;
        @ManyToOne
        private A a;
    }

    @Entity
    @Table(name = "C")
    public static class C {
        @Id
        private Long id;
        @ManyToOne
        private B b;
    }
}
