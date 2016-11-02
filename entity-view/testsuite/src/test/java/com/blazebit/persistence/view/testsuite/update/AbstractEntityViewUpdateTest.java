/*
 * Copyright 2015 Blazebit.
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
package com.blazebit.persistence.view.testsuite.update;

import javax.persistence.EntityTransaction;

import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;

/**
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
public abstract class AbstractEntityViewUpdateTest extends AbstractEntityViewTest {

    protected void transactional(TxVoidWork work) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            work.doWork(em);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException(e);
        }
    }

    protected <T> T transactional(TxWork<T> work) {
        T result = null;
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            result = work.doWork(em);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException(e);
        }
        
        return result;
    }
}
