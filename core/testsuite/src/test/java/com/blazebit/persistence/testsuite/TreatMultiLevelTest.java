/*
 * Copyright 2014 - 2020 Blazebit.
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
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Parent;
import com.blazebit.persistence.testsuite.entity.Sub1;
import com.blazebit.persistence.testsuite.entity.Sub1Sub1;
import com.blazebit.persistence.testsuite.entity.Sub1Sub2;
import com.blazebit.persistence.testsuite.entity.Sub2;
import com.blazebit.persistence.testsuite.entity.Sub2Sub1;
import com.blazebit.persistence.testsuite.entity.Sub2Sub2;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.List;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class TreatMultiLevelTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
                Parent.class,
                Sub1.class,
                Sub2.class,
                Sub1Sub1.class,
                Sub1Sub2.class,
                Sub2Sub1.class,
                Sub2Sub2.class
        };
    }

    @Test
    // NOTE: Datanucleus does not support root treats properly with joined inheritance. Maybe a bug? TODO: report the error
    @Category({ NoDatanucleus.class })
    public void implicitJoinTreatedRoot() {
        CriteriaBuilder<Integer> criteria = cbf.create(em, Integer.class);
        criteria.from(Parent.class, "p");
        criteria.select("TREAT(p AS Sub1).number");
        assertEquals("SELECT " + treatRoot("p", Sub1.class, "number") + " FROM Parent p", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    // NOTE: Datanucleus does not seem to support this kind of model? TODO: report the error
    // NOTE: EclipseLink is just fundamentally broken in this regard...
    @Category({ NoDatanucleus.class, NoEclipselink.class })
    public void multiTreatWithDiscriminatorColumn() {
        // Given
        Sub1 sub1 = new Sub1();
        sub1.setSub1Value(11);
        Sub2 sub2 = new Sub2();
        sub2.setSub2Value(9);
        em.persist(sub1);
        em.persist(sub2);

        // When
        CriteriaBuilder<Parent> criteria = cbf.create(em, Parent.class);
        List<Parent> result = criteria.from(Parent.class, "p")
                .where("COALESCE(TREAT(p AS Sub1).sub1Value, TREAT(p AS Sub2).sub2Value)").gtExpression("10")
                .getResultList();

        // Then
        assertEquals(1, result.size());
        assertTrue(result.contains(sub1));
    }
}
