/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.hibernate;

import org.junit.Test;

import com.blazebit.persistence.CTE;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.entity.LongSequenceEntity;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.1
 */
public class Issue1000Test extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
                TimerHolder.class,
                CorrelatedTimer.class,
                Timer.class
        };
    }

    @Entity(name = "TimerHolder")
    public static class TimerHolder extends LongSequenceEntity {}

    @CTE
    @Entity(name = "CorrelatedTimer")
    public static class CorrelatedTimer extends LongSequenceEntity {}

    @Entity(name = "Timer")
    public static class Timer extends LongSequenceEntity {}

    @Test
    public void testCorrelation() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                cbf.create(em, Tuple.class)
                        .with(CorrelatedTimer.class)
                        .from(Timer.class)
                        .bind("id").select("id")
                        .end()
                        .from(TimerHolder.class, "a")
                        .leftJoinOn(CorrelatedTimer.class, "correlated_timers").on("correlated_timers.id").eqExpression("1").end()
                        .orderByAsc("a.id")
                        .orderByAsc("correlated_timers.id")
                        .select("a")
                        .select("correlated_timers")
                        .page(0, 10)
                        .getResultList();
            }
        });

    }
}
