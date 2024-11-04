/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.util;

import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.spi.CriteriaBuilderConfigurationContributor;
import com.blazebit.persistence.spi.Priority;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.assertSame;

public class CriteriaBuilderConfigurationContributorComparatorTest {

    @Test
    public void testSort() {
        ArrayList<CriteriaBuilderConfigurationContributor> contributions = new ArrayList<>();

        final A a = new A();
        final B b = new B();
        final C c = new C();
        final D d = new D();

        contributions.add(d);
        contributions.add(c);
        contributions.add(b);
        contributions.add(a);

        Collections.sort(contributions, new CriteriaBuilderConfigurationContributorComparator());
        assertSame(a, contributions.get(0));
        assertSame(c, contributions.get(1));
        assertSame(d, contributions.get(2));
        assertSame(b, contributions.get(3));
    }

    @Priority(value = 100)
    public static class A implements CriteriaBuilderConfigurationContributor {
        @Override
        public void contribute(CriteriaBuilderConfiguration criteriaBuilderConfiguration) {
        }
    }

    public static class B implements CriteriaBuilderConfigurationContributor {
        @Override
        public void contribute(CriteriaBuilderConfiguration criteriaBuilderConfiguration) {
        }
    }

    @Priority(value = 1000)
    public static class C implements CriteriaBuilderConfigurationContributor {
        @Override
        public void contribute(CriteriaBuilderConfiguration criteriaBuilderConfiguration) {
        }
    }

    @Priority(value = 1000)
    public static class D implements CriteriaBuilderConfigurationContributor {
        @Override
        public void contribute(CriteriaBuilderConfiguration criteriaBuilderConfiguration) {
        }
    }

}
