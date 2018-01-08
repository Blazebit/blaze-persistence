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

package com.blazebit.persistence.testsuite.tx;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;


public class TxSupport {

    private TxSupport() {
    }

    public static void transactional(EntityManager em, TxVoidWork r) {
        EntityTransaction tx = em.getTransaction();
        boolean success = false;
        try {
            tx.begin();
            r.work(em);
            if (tx.getRollbackOnly()) {
                tx.rollback();
            } else {
                tx.commit();
            }
            success = true;
        } finally {
            if (!success) {
                tx.rollback();
            }
        }
    }

    public static <V> V transactional(EntityManager em, TxWork<V> c) {
        EntityTransaction tx = em.getTransaction();
        boolean success = false;
        try {
            tx.begin();
            V result = c.work(em);
            if (tx.getRollbackOnly()) {
                tx.rollback();
            } else {
                tx.commit();
            }
            success = true;
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (!success) {
                tx.rollback();
            }
        }
    }
}
