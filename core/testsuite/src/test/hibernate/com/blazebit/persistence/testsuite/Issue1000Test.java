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

import com.blazebit.persistence.CTE;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate43;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate50;
import com.blazebit.persistence.testsuite.entity.LongSequenceEntity;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Tuple;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.1
 */
public class Issue1000Test extends AbstractCoreTest  {

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
    @Category({
            NoHibernate50.class, NoHibernate43.class, NoHibernate42.class, // Entity join required for fallback
    })
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
