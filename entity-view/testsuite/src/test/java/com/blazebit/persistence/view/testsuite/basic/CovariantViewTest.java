/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.basic;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.persistence.EntityManager;

import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Before;
import org.junit.Test;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.basic.model.CovariantBasePersonView;
import com.blazebit.persistence.view.testsuite.basic.model.CovariantPersonView;
import com.blazebit.persistence.testsuite.entity.Person;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class CovariantViewTest extends AbstractEntityViewTest {

    protected EntityViewManager evm;

    @Before
    public void initEvm() {
        evm = build(CovariantPersonView.class);
    }

    private Person pers1;

    @Before
    public void setUp() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                pers1 = new Person("pers1");
                pers1.getLocalized().put(1, "localized1");

                em.persist(pers1);
            }
        });
    }

    @Test
    public void testCovariant() {
        CriteriaBuilder<Person> criteria = cbf.create(em, Person.class, "p")
            .orderByAsc("id");
        List<CovariantPersonView> results = evm.applySetting(EntityViewSetting.create(CovariantPersonView.class), criteria)
            .getResultList();

        assertEquals(1, results.size());
        // Doc1
        assertEquals(pers1.getName(), results.get(0).getName());
        assertEquals(pers1.getName(), ((CovariantBasePersonView<?>) results.get(0)).getName());
    }
}
